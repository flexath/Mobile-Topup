# Operator Prefix Sync (Supabase → Room) Design

**Date:** 2026-11-12  
**Status:** Implemented  
**Module:** `:feature:topup` (+ `:app` Room DB wiring, Supabase migration)

## Problem

Operator detection today uses a hardcoded in-memory `OperatorPrefixTable.Default`. The assignment requires maintaining an updated local list of mobile number prefixes for offline detection. Supabase already has `public.operator_prefixes`, but it is intentionally **not** readable by clients (no `GRANT SELECT`, no RLS policy). The Android app must sync that table into Room and prefer Room for detection, while keeping `OperatorPrefixTable` as an offline fallback.

## Decisions (approved)

| Decision | Choice |
|----------|--------|
| Repository ownership | **B** — separate `OperatorPrefixRepository` + `SyncOperatorPrefixesUseCase` |
| Detection wiring | **1** — observe Room as `Flow`, combine in `RechargeViewModel`; pure sync `DetectOperatorUseCase` |
| Fallback | Keep `OperatorPrefixTable.Default` when Room has no usable rows |
| Trigger | `TopupViewModel.init` calls `syncCatalog()` **and** `syncOperatorPrefixes()` |

## Goals

1. Fetch `operator_prefixes` from Supabase and persist in Room.
2. Room is the primary source for operator detection lookups.
3. Fall back to `OperatorPrefixTable` when Room has no usable data.
4. Sync failures never wipe cache or block offline detection.
5. Once-per-process refresh (no recomposition / config-change storms).
6. Follow existing clean-architecture and catalog-sync conventions.

## Non-goals

- Removing or replacing `OperatorPrefixTable`.
- Exposing prefix sync status in the Top-up UI (silent background refresh, like catalog when cache exists).
- Writing prefixes from the client (read-only).
- Changing server-side `process_recharge` prefix validation logic.

## Architecture

```
TopupViewModel.init
  ├─ SyncCatalogUseCase → CatalogRepository.startLaunchRefresh()
  └─ SyncOperatorPrefixesUseCase → OperatorPrefixRepository.startLaunchRefresh()
         └─ TopupRemoteService.fetchOperatorPrefixes()
         └─ OperatorPrefixDao.replaceAll(...)  // only if valid non-empty

RechargeViewModel
  ├─ ObserveOperatorPrefixesUseCase → Flow<List<OperatorPrefix>>
  ├─ resolve table = Room list if usable else OperatorPrefixTable.Default
  └─ DetectOperatorUseCase(phone, table) → PhoneNumberStatus
```

Layer placement stays inside `:feature:topup` (feature-local domain/data). No new Gradle module.

## Backend changes

### Current state

- Table: `public.operator_prefixes (prefix PK, operator_code FK → operators, created_at)`
- Format check: `prefix ~ '^09[0-9]{2,3}$'`
- RLS enabled; **no** policies
- `SELECT` **not** granted to `authenticated` / `anon`
- Seeded with the PRD common-prefix dataset (~49 rows)
- SQL RLS test currently expects `select count(*) from public.operator_prefixes` to be **denied** for authenticated clients

### Migration

Add a new migration (do not edit the historical core schema migration):

1. `GRANT SELECT ON TABLE public.operator_prefixes TO authenticated;`
2. Policy: authenticated users can `SELECT` all rows (reference data; no per-user filter), matching the spirit of active operators/products policies.
3. Do **not** grant INSERT/UPDATE/DELETE to clients.
4. Update table/comment docs: prefixes are readable by authenticated clients for offline cache sync; server still owns validation via `process_recharge`.

### RLS test updates

- Remove `select count(*) from public.operator_prefixes` from the authenticated **denial** list.
- Add a positive check: authenticated role can `SELECT` prefixes (expect seeded count, currently 49).
- Keep write denials if any write attempts against prefixes are added later (optional: assert INSERT/UPDATE/DELETE denied).

Apply the migration to the linked Supabase project as part of implementation.

## Room

### Entity

```kotlin
@Entity(tableName = "operator_prefixes")
data class OperatorPrefixEntity(
    @PrimaryKey @ColumnInfo(name = "prefix") val prefix: String,
    @ColumnInfo(name = "operator_code") val operatorCode: String,
)
```

No FK to local `operators` table (prefixes may sync before/independently of catalog; detection must not depend on catalog rows).

### DAO

- `observeAll(): Flow<List<OperatorPrefixEntity>>`
- `count(): suspend Int` (optional; for tests / “usable” checks)
- `replaceAll(rows: List<OperatorPrefixEntity>)` — `@Transaction` delete-all + insert; only called by repository after validation

### Database

- Register entity + DAO on `TopUpDatabase` (`app/.../TopUpDatabase.kt`).
- Bump `version` to `2`.
- App already uses `fallbackToDestructiveMigration(dropAllTables = true)` — acceptable for this demo app; catalog will re-sync on next launch.
- Wire `operatorPrefixDao()` in `AppModule`.
- Update androidTest `CatalogDatabase` only if prefix DAO tests share it; prefer a small `OperatorPrefixDatabase` test double or extend the in-memory catalog DB for DAO tests.

## Remote API

### DTO

```kotlin
@Serializable
internal data class OperatorPrefixDto(
    @SerialName("prefix") val prefix: String,
    @SerialName("operator_code") val operatorCode: String,
)
```

### Service

Extend `TopupRemoteService` / `SupabaseTopupRemoteService`:

```kotlin
suspend fun fetchOperatorPrefixes(): List<OperatorPrefixDto>
// supabase.from("operator_prefixes").select("prefix,operator_code").decodeList()
```

No need to fetch `created_at` for the client cache.

## Domain

### Models

Reuse existing domain `OperatorPrefix` (`prefix` + `OperatorCode`) from `domain/phone/OperatorPrefixTable.kt`. Do not introduce a parallel model unless mapping needs diverge.

### Repository interface

```kotlin
interface OperatorPrefixRepository {
    /** Room-backed prefixes only (empty if never synced / wiped). */
    fun observePrefixes(): Flow<List<OperatorPrefix>>

    fun startLaunchRefresh()
    fun retryRefresh() // available for symmetry; Top-up may not surface UI retry for prefixes
}
```

### Repository implementation (mirror `CatalogRepositoryImpl`)

- `AtomicBoolean` once-per-process launch refresh.
- Generation counter + write mutex so superseded responses do not overwrite newer data.
- Runs in `ApplicationScope` + IO dispatcher (not ViewModel scope), so rotation / leaving Top-up does not cancel the write.
- Validation before write:
  - Non-empty list
  - Every `prefix` matches `^09[0-9]{2,3}$` (same idea as server check)
  - Every `operator_code` maps via `OperatorCode.fromCode`
  - Prefer longest-prefix correctness is handled at match time; store all valid rows
- On invalid/empty remote payload: do **not** clear Room; treat as failed sync (log via status if kept internal).
- On network/exception: leave Room untouched; never throw into UI.
- Optional: skip `replaceAll` when the sorted `(prefix, code)` set equals the current Room set (avoids unnecessary writes). Recommended but not required for v1 if tests stay simple.

Sync status for prefixes need not be exposed in UI. Keep an internal status or omit; detection does not wait on sync.

### Use cases

| Use case | Behavior |
|----------|----------|
| `SyncOperatorPrefixesUseCase` | `retry=false` → `startLaunchRefresh()`; `retry=true` → `retryRefresh()` |
| `ObserveOperatorPrefixesUseCase` | `repository.observePrefixes()` |
| `DetectOperatorUseCase` | `invoke(raw, prefixes: OperatorPrefixTable)` — pure; caller supplies Room-built table or `Default` |

Resolve the active table in the ViewModel (or a tiny pure helper):

```kotlin
fun resolvePrefixTable(
    roomPrefixes: List<OperatorPrefix>,
    fallback: OperatorPrefixTable = OperatorPrefixTable.Default,
): OperatorPrefixTable =
    if (roomPrefixes.isNotEmpty()) OperatorPrefixTable(roomPrefixes) else fallback
```

“Usable” = non-empty after mapping known operator codes (unknown codes dropped in the repository/mapper).

## Presentation

### TopupViewModel

In `init`, after or alongside `syncCatalog()`:

```kotlin
syncCatalog()
syncOperatorPrefixes()
refreshWallet(force = true)
```

Both repositories guard once-per-process, so recomposition / re-entry does not re-hit the network.

### RechargeViewModel

- Inject `ObserveOperatorPrefixesUseCase` and updated `DetectOperatorUseCase`.
- Combine `observePrefixes()` into the existing `uiState` pipeline (or a nested combine).
- Build `OperatorPrefixTable` via `resolvePrefixTable(roomPrefixes)`.
- Pass that table into `detectOperator(phone, table)` in `onPhoneNumberChange` and `buildUiState`.
- Do **not** call prefix sync from Recharge unless we later want retry parity; Top-up launch + existing Recharge `syncCatalog()` is enough for catalog. Prefix sync is owned by Top-up entry.  
  **Note:** Users can deep-link / navigate to Recharge after process death without Top-up. To keep detection correct after cold start into Recharge, also call `syncOperatorPrefixes()` from `RechargeViewModel.init` the same way `syncCatalog()` is already called there — still once-per-process thanks to the repository guard. This matches “integrate with existing loading flow” without a new unrelated trigger.

## Detection behavior matrix

| Room state | Network | Detection source |
|------------|---------|------------------|
| Empty | Offline / fail | `OperatorPrefixTable.Default` |
| Empty | Sync success | Room (after write) |
| Cached | Fail / offline | Room cache |
| Cached | Sync success | Updated Room |

Longest-prefix match semantics stay in `OperatorPrefixTable.match` unchanged.

## DI (Koin)

In `TopupFeatureModule`:

- `single<OperatorPrefixRepository> { OperatorPrefixRepositoryImpl(...) }`
- `factoryOf(::SyncOperatorPrefixesUseCase)`
- `factoryOf(::ObserveOperatorPrefixesUseCase)`
- Update `DetectOperatorUseCase` factory (no longer hardcodes only Default inside invoke without parameter — keep Default as default arg for tests).
- Wire new deps into `TopupViewModel` / `RechargeViewModel`.

In `AppModule`: expose `operatorPrefixDao()`.

## Testing

1. **DAO (androidTest):** `replaceAll` swaps atomically; observe emits updates.
2. **Repository (unit):** success writes; failure preserves; empty/invalid does not wipe; second `startLaunchRefresh` is no-op; generation supersession.
3. **`resolvePrefixTable` / DetectOperator:** Room preferred; empty → Default; longest match still works with Room-built table.
4. **TopupViewModelTest:** both sync use cases invoked once on init (fake repos).
5. **RLS SQL test:** authenticated SELECT allowed; writes still denied.
6. **MyanmarPhoneNumberTest:** existing tests keep using Default; add/adjust as needed for Room-supplied tables.

## Docs / comments to update

- `docs/CURRENT_STATE.md` — `operator_prefixes` is client-readable for cache sync.
- Schema comment on `operator_prefixes` in the new migration.
- README offline detection bullet — Room first, `OperatorPrefixTable` fallback.

## Risks & mitigations

| Risk | Mitigation |
|------|------------|
| Clients cannot read table today | Migration + grant + RLS before shipping client fetch |
| Destructive Room bump clears catalog | Existing launch refresh repopulates; acceptable with current app DB policy |
| Stale Room vs server | Once-per-process refresh on Top-up/Recharge entry; server still validates on recharge |
| Empty successful response wiping cache | Treat empty/invalid as failed; do not `replaceAll` |

## Open items (none blocking)

None after approvals of ownership **B** and detection approach **1**.
