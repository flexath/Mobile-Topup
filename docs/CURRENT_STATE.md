# TopUp — Current Application State

> **Purpose of this document:** Single context source for product/requirement decisions.
> Snapshot date: **11 Nov 2026**. Describes what exists in the codebase today — not a roadmap.

---

## 1. Product summary

| Item | Value |
|------|--------|
| App name | **TopUp** |
| Package / applicationId | `com.aungthiha.topup` |
| Version | `1.0.0` (versionCode `1`) |
| Platform | **Native Android only** (not KMP / Flutter) |
| UI | Jetpack Compose + Material 3 |
| Maturity | Early foundation / shell — **no real top-up business flow yet** |

**Intended product (from assignment brief):** A Myanmar-style **mobile top-up** app where a user enters a phone number, the app detects the telecom operator from number prefixes (offline), shows denominations / data packages for that operator, completes a (local) recharge flow, shows a success screen, and persists / lists transactions with history filters and search.

**Origin:** KBZ Bank Android Developer Coding Test assignment (`docs/Andorid Code Test Project Assignment.pdf`). Duration target in brief: 72 hours. Deliverable: source via zip / GitHub.

---

## 2. Assignment requirements (target product)

These are the **required capabilities** from the coding-test brief. Most are **not implemented yet** (see §8).

### Spec constraints
- Language: **Kotlin**
- Architecture pattern: **MVVM** (project also targets Clean Architecture + feature modularization)
- Database: **Room**
- Animations and transitions
- Screen orientation support (data must **not** reload on rotation)

### Task 1 — Auto-detect telecom from mobile number
- Number-prefix lookup from telecom numbering plans
- Local database matching for **offline** detection
- Maintain an updated list of mobile number prefixes

### Task 2 — Denominations & data packages
- Show denomination and data packages for the detected telecom
- User-friendly structured comparison / selection UI

### Task 3 — Success, history, and detail screens
**Success screen**
- Clear confirmation with checkmark-style visual
- Show amount, plan details, validity, transaction ID, operator name

**Transaction History screen**
- List past transactions: date, amount, plan details, status (`Success` / `Pending` / `Failed`)
- Filters / sorting by date range, status, or operator
- Search bar for finding transactions

**Detailed screen**
- Telecom provider logo + name based on mobile number
- Prominent **“Proceed to Recharge”** action

### Task 4 — Local persistence
- Insert transactions into **Room** after a successful recharge

### Notes from brief
- Attractive UI + persistence layer are expected
- Use OOP / design patterns and clear interfaces
- Possible online interview after submission

---

## 3. What is built today

### Working shell
1. App launches into `MainActivity` → `AppTheme` → `TopUpAppShell`
2. Edge-to-edge window
3. Adaptive chrome:
   - **Compact width:** floating glass bottom nav bar over content
   - **Medium+ width:** vertical nav rail beside content
4. Two top-level tabs with typed Navigation Compose graphs:
   - **Topup** (start destination)
   - **History**
5. Tab switch preserves per-feature back stack (`saveState` / `restoreState`)
6. Placeholder screens with title text + a button to switch tab
7. Design-system theme tokens (colors, typography, spacing, shapes) + light/dark
8. Firebase Analytics + Crashlytics wired via convention plugin (`google-services.json` present)
9. Debug StrictMode logging in `TopUpApplication`
10. Release signing expected via `local.properties` keys (fail-hard at configure if missing)
11. Composite `build-logic/` with `topup.*` convention plugins (Android/Compose baselines + opt-in stacks)

### Not built today (high level)
- Phone number input / validation
- Telecom prefix detection
- Plans / denominations catalog
- Recharge / success / detail flows
- Room entities / DAOs / transaction persistence (Room **plugin exists**, unused in product modules)
- Domain / data layers, ViewModels, use cases, repositories
- Koin started in `Application` / module `di/` graphs (Koin **plugin exists**, not applied/wired yet)
- Networking clients or Supabase usage (Ktor / Supabase **plugins exist**, unused)
- Real history list, filters, search
- Localization beyond default English labels
- Deep links
- Shared business modules (`shared/*` planned in architecture rules but **no `shared` modules exist yet**)
- `:core:datastore` applies DataStore + Coroutines conventions but still has **no Kotlin API / prefs code**

---

## 4. Module map

Dependency direction (intended / enforced):

```text
app → feature/* → shared/* → core/*
```

### Included Gradle modules

| Module | Role today |
|--------|------------|
| `:app` | Application, `MainActivity`, `TopUpAppShell`, root `NavHost`, tab routing; Firebase via `topup.firebase` |
| `:feature:topup` | Topup tab placeholder UI + nav trio (`library.compose` + serialization + navigation) |
| `:feature:transactionhistory` | History tab placeholder UI + nav trio (same stack) |
| `:core:common` | `AppRoutes` marker interface only |
| `:core:designsystem` | Theme tokens, adaptive device utils, floating nav / rail chrome |
| `:core:datastore` | Scaffold module; `topup.datastore` + `topup.coroutines` applied — **no source APIs yet** |
| `build-logic` | Composite build; `topup.*` convention plugins (see §9) |

### Not present
- `:shared:*` modules
- Network / database / auth product modules (stack plugins available when those modules are created)
- Additional features (auth, profile, payments, etc.)

---

## 5. Architecture stance (intended vs actual)

### Intended (project rules)
- **Modularization:** `app` / `feature` / `shared` / `core`
- **Clean Architecture inside features/shared:** Presentation → Domain ← Data
- **Presentation:** Compose screens, ViewModels, UiState
- **Domain:** models, repository interfaces, use cases
- **Data:** API/Room/cache impls, mappers — keep DTOs/`*RepositoryImpl` out of presentation
- **Navigation:** Features emit `NavCallbacks`; only `:app` navigates
- **DI:** Prefer **Koin** (convention plugin ready); bindings in owning module `di/`; app composes graphs

### Actual today
- Feature modules contain **presentation/navigation + placeholder screens only**
- No `domain/`, `data/`, `di/`, ViewModels, or use cases in features
- No Room/Ktor/Supabase/Koin usage in application code yet — only Gradle convention plugins + catalog entries
- Architecture is **scaffolded at module + navigation + build-logic level**, not product-feature level

**Implication for requirements:** New product work should land as vertical slices under `:feature:topup` / `:feature:transactionhistory`, extracting multi-feature domain into `:shared:*` only when reused. Prefer MVVM + use cases to satisfy both the assignment brief and the project’s Clean Architecture rules. Apply stack conventions (`topup.room`, `topup.koin`, etc.) on the owning module when implementing those layers.

---

## 6. Navigation & UX shell (current)

### Entry chain
```text
MainActivity
  → AppTheme
  → TopUpAppShell          // rememberNavController()
  → AppNavGraph            // only cross-feature router
  → feature *NavGraph()
```

### Top-level destinations

| Tab | Label | Feature graph | Root screen |
|-----|-------|---------------|-------------|
| `Topup` | Topup | `TopupRoutes.GraphRoute` | `TopupRoutes.TopupScreen` |
| `History` | History | `TransactionHistoryRoutes.GraphRoute` | `TransactionHistoryRoutes.TransactionHistoryScreen` |

- Start destination: **Topup graph**
- Chrome visible only on tab **root** screens (`showsAppNavigation()`)
- Cross-tab navigation from placeholders: Topup → History, History → Topup
- `NavigateUp` callbacks exist but are unused by placeholders

### Feature navigation pattern (already established)
Each feature owns:
1. `{Feature}Routes.kt` — `@Serializable` destinations extending `AppRoutes`
2. `{Feature}NavCallbacks.kt` — sealed navigation intents
3. `{Feature}NavGraph.kt` — nested graph registration

Screens register via `NavGraphBuilder` extensions colocated with the composable. Features must **not** hold `NavHostController`.

Detailed navigation docs: `docs/navigation/`.

---

## 7. Design system (current)

Owned by `:core:designsystem`. Features/app consume tokens via `MaterialTheme` extensions.

| Concern | Implementation |
|---------|----------------|
| Theme entry | `AppTheme { }` |
| Colors | Semantic `AppColorScheme` (light/dark) — Aura Blue / Purple / Gold |
| Primary | `#2970FF` (Aura Blue) |
| Secondary | Purple (`#9C6BFF` light / `#B68CFF` dark) |
| Complementary | Gold `#FFD600` (highlights / progress — not primary CTA substitute) |
| Typography | **Manrope** via `appTypography` |
| Spacing / shapes | Adaptive spacing by `deviceType`; fixed shape tokens |
| Window class | `DeviceWidthClass` (Compact → rail switch at Medium+) |
| Shared components | Mostly **navigation chrome** (`AppFloatingNavBar`, `AppNavRail`, liquid-glass helpers). No full button/form/list kit yet |
| Naming | Public composables should be `App*` when added |

Screens currently use raw Material `Button` in placeholders — product UI should move to design-system components as they are added.

Cursor guidance: `.cursor/rules/topup-design-system.mdc`, `.cursor/skills/topup-design-system/`.

---

## 8. Capability matrix (requirements vs codebase)

| Capability | Status | Notes |
|------------|--------|-------|
| Kotlin + Compose app shell | ✅ Done | AGP 9 / Kotlin 2.4 stack |
| Multi-module structure | ✅ Done | app / feature / core + build-logic |
| Typed multi-module navigation | ✅ Done | 2 tabs |
| Adaptive nav chrome + animations | ✅ Partial | Tab chrome animated; no product-flow transitions yet |
| Orientation / config survival | ⚠️ Partial | Compose + nav state; no ViewModel/`rememberSaveable` product state yet |
| MVVM | ❌ Missing | No ViewModels |
| Room / local DB | ⚠️ Plugin only | `topup.room` (+ optional `topup.sqlcipher`) ready; no entities/DAOs yet |
| Preference DataStore | ⚠️ Plugin only | Applied on `:core:datastore`; no prefs API code yet |
| Koin DI | ⚠️ Plugin only | `topup.koin` ready; not started in Application |
| Ktor / Supabase | ⚠️ Plugin only | Catalog + conventions exist; unused in app code |
| Coroutines / Flows | ⚠️ Partial | Convention available; applied on `:core:datastore` only |
| Telecom prefix detection | ❌ Missing | Need offline prefix catalog |
| Denomination / package catalog UI | ❌ Missing | Per-operator plans |
| Detail screen + Proceed to Recharge | ❌ Missing | |
| Success screen | ❌ Missing | |
| Transaction history list | ❌ Missing | Placeholder tab only |
| History filters / sort / search | ❌ Missing | |
| Persist successful recharge | ❌ Missing | |
| Attractive product UI | ⚠️ Foundation only | Theme + nav chrome; screens are stubs |
| Firebase Analytics / Crashlytics | ✅ Wired | Not product-critical for assignment UX |
| Networking backend | ❌ Not required by brief | Local/offline-first implied; Supabase optional if scope expands |
| Auth / payments gateway | ❌ Not in brief | Treat as out of scope unless requirements expand |
| i18n / Myanmar locale | ❌ Not started | May matter for Myanmar operators/copy |

---

## 9. Tech stack & build-logic

From `gradle/libs.versions.toml` / `build-logic/`:

| Area | Choice |
|------|--------|
| AGP | `9.4.0` |
| Kotlin | `2.4.21` |
| Compose BOM | `2026.09.00` |
| Navigation Compose | `2.9.2` |
| kotlinx.serialization | `1.11.0` |
| kotlinx.coroutines | `1.11.0` |
| Material3 Adaptive | `1.2.0` |
| Firebase BOM | `35.1.0` |
| Room | `2.8.5` (+ KSP `2.3.12`) |
| SQLCipher | `4.20.0` |
| DataStore Preferences | `1.3.0` |
| Ktor | `3.6.1` |
| Koin | `4.2.2` |
| Supabase BOM | `3.9.0` |
| minSdk / targetSdk / compileSdk | `24` / `37` / `37` |
| Java toolchain | **18** |
| Build types | **debug + release only** (no env flavors) |

### Convention plugins (`topup.*`)

| Plugin | Role |
|--------|------|
| `topup.android.application(.compose)` | App AGP + signing + Compose baseline |
| `topup.android.library(.compose)` | Library AGP + Compose baseline |
| `topup.serialization` | Serialization plugin + JSON |
| `topup.navigation` | Navigation Compose |
| `topup.firebase` | Google Services + Crashlytics + BoM (app) |
| `topup.room` | KSP + Room runtime/KTX/compiler + schema export |
| `topup.sqlcipher` | Encrypted SQLite (`sqlcipher-android`) |
| `topup.datastore` | Preference DataStore |
| `topup.ktor` | Ktor OkHttp client (+ Serialization) |
| `topup.koin` | Koin Android (+ Compose helpers when Compose present) |
| `topup.supabase` | Supabase BOM + postgrest/auth/realtime/storage/functions |
| `topup.coroutines` | Coroutines core/android + test |

Rules/skill: `.cursor/rules/topup-build-logic.mdc`, `.cursor/skills/topup-build-logic/`.

**Release signing:** fail-hard via `RELEASE_KEYSTORE`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` in root `local.properties`.

**Supabase BuildConfig:** fail-hard via `SUPABASE_URL`, `SUPABASE_ANON_KEY` in root `local.properties` → `BuildConfig.SUPABASE_URL` / `BuildConfig.SUPABASE_ANON_KEY` on `:app` (`buildFeatures.buildConfig = true`).

---

## 10. Current user journeys

### Journey A — Launch (exists)
1. Open app
2. Land on **Topup** placeholder (“Topup” title + “Go to History”)
3. Switch to **History** via floating bar / rail or button
4. History placeholder (“History” + “Go to Topup”)
5. Switch back

### Journeys not yet possible
- Enter phone number → detect operator
- Browse plans → confirm → recharge → success
- View / filter / search past transactions
- Open transaction or operator detail → Proceed to Recharge

---

## 11. Suggested product domain objects (not coded — for planning)

Useful vocabulary when deciding requirements / data model:

| Concept | Meaning |
|---------|---------|
| `TelecomOperator` | MPT / ATOM / Ooredoo / Mytel (or assignment-defined set) |
| `NumberPrefix` | Digits that map a local number to an operator |
| `PhoneNumber` | Normalized local / MSISDN input |
| `TopupProduct` | Denomination or data package (amount, validity, type) |
| `Transaction` | Recharge attempt with status, amount, plan, operator, id, timestamps |
| `TransactionStatus` | `Success` / `Pending` / `Failed` |

Assignment implies **offline prefix DB** + **Room for transactions**. Catalog of plans may also be local seed data unless requirements specify otherwise.

---

## 12. Open decisions for requirements work

Use these when chatting about scope:

1. **Operator set & prefix tables** — which Myanmar operators and prefix lists are authoritative? How are they updated?
2. **Plan catalog source** — hardcoded seed / JSON asset / Room seed vs remote / Supabase?
3. **Is recharge real or simulated?** — Brief says insert after successful recharge; likely simulated success locally.
4. **Pending status** — When would a transaction be `Pending` if everything is local?
5. **Single feature vs split** — Keep top-up flow in `:feature:topup` and history in `:feature:transactionhistory`; put shared Room entities / operator domain in `:shared:topup` (or similar) once both need it.
6. **Detail screen placement** — Pre-recharge operator/plan detail inside topup flow vs post-history detail?
7. **Success screen** — Nested route in topup graph (chrome hidden) vs dialog?
8. **MVVM + Koin boundary** — ViewModels call use cases; Room behind repositories; start Koin from `:app`.
9. **SQLCipher** — Encrypt Room DB for transactions / prefixes, or plain Room for the coding test?
10. **Animations** — Beyond nav chrome: shared element / success checkmark / list item transitions?
11. **Out of scope** — Online payment, login, push notifications, multi-language (unless explicitly added).

---

## 13. Repository layout (quick reference)

```text
Mobile-Topup/
├── app/                      # Shell + NavHost composition
├── feature/
│   ├── topup/                # Topup tab (placeholder UI)
│   └── transactionhistory/   # History tab (placeholder UI)
├── core/
│   ├── common/               # AppRoutes
│   ├── designsystem/         # Theme + nav chrome
│   └── datastore/            # DataStore + coroutines conventions; no APIs yet
├── build-logic/              # topup.* convention plugins
├── .cursor/
│   ├── rules/                # topup-build-logic, navigation, design-system, …
│   └── skills/               # Matching project skills
└── docs/
    ├── CURRENT_STATE.md      # This file
    ├── Andorid Code Test Project Assignment.pdf
    └── navigation/           # Nav architecture how-tos
```

---

## 14. One-line status for AI assistants

**TopUp is a native Android Compose multi-module shell with themed adaptive tab navigation (Topup + History) and a full `topup.*` Gradle convention stack (Room, SQLCipher, DataStore, Ktor, Koin, Supabase, Coroutines). Product features from the KBZ mobile-topup coding assignment — prefix detection, plans, recharge success, Room-backed history with filter/search — are specified but not implemented; only placeholder screens exist, and most stack plugins are not yet applied in product modules.**
