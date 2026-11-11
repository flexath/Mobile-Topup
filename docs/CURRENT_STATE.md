# TopUp — Current Application State

> **Purpose of this document:** Single context source for product/requirement decisions.
> Snapshot date: **11 Nov 2026**. Describes what exists in the codebase and the connected Supabase
> project today. Product requirements live in `docs/product/PRD.md` and
> `docs/product/Design-Specification.md`.

---

## 1. Product summary

| Item | Value |
|------|--------|
| App name | **TopUp** |
| Package / applicationId | `com.aungthiha.topup` |
| Version | `1.0.0` (versionCode `1`) |
| Platform | **Native Android only** |
| UI | Jetpack Compose + Material 3 (Manrope, `AppTheme` tokens) |
| Backend | Supabase project `ghfrbizaikttvuipalyc` (Auth, Postgres + RLS, Storage, Edge Functions) |
| Maturity | PRD v1.1 scope implemented end-to-end (simulated recharge) |

A signed-in user enters a Myanmar mobile number, the app detects the operator offline, shows that
operator's denominations / data packages from a Room-cached catalog, and completes a **simulated**
recharge against the user's demo wallet through the `process-recharge` Edge Function. Successful
transactions are cached in Room and shown in History with search, filters, sorting, details and
receipt sharing. No telecom provider or payment gateway is contacted.

---

## 2. Module map

```text
app → feature/* → shared/* → core/*
```

| Module | Role |
|--------|------|
| `:app` | `TopUpApplication` (Koin start), `MainActivity` (splash gate, theme), `MainViewModel`, `TopUpAppShell` (session-keyed graph + tab chrome), `AppNavGraph` |
| `:feature:auth` | Login / Register screens + ViewModels |
| `:feature:topup` | Top-up screen, confirmation sheet, processing, success; prefix detection; catalog (Room), wallet (DataStore), recharge (Edge Function) |
| `:feature:transactionhistory` | History list (search / operator + date filters / sort / Load More) and transaction detail with receipt sharing |
| `:feature:settings` | Appearance (System / Light / Dark), account email + logout, app information |
| `:shared:auth` | Supabase Auth repository, auth state, credential validation, sign-in/up/out use cases, `CurrentUserProvider` impl |
| `:shared:transaction` | Transaction domain + Room cache (user-scoped) + `list_my_transactions` sync; used by top-up (save) and history (read) |
| `:core:common` | `DataResult` / `DataError`, `NetworkMonitor`, session contracts (`CurrentUserProvider`, `UserScopedDataCleaner`), formatters, `ImageSharer` + FileProvider |
| `:core:network` | Single `SupabaseClient` (Auth, Postgrest, Functions, Storage), `StorageUrlResolver`, network error mapping |
| `:core:datastore` | Preference DataStore: appearance mode + per-user wallet balance cache |
| `:core:designsystem` | Theme tokens (unchanged values) + `App*` components (buttons, text field, selectable card, banner, empty state, receipt, operator logo, animated checkmark), outline icon set, `CaptureController` |

Room databases: `topup_catalog.db` (`:feature:topup`, shared non-personal data, kept on logout) and
`topup_transactions.db` (`:shared:transaction`, rows keyed by `user_id`, cleared on logout).
Schemas are exported to each module's `schemas/` directory.

---

## 3. Key flows (as implemented)

| Flow | Behaviour |
|------|-----------|
| Launch | System splash stays until the stored Supabase session and theme are read; then Login or Top-up. Session persists across restarts (supabase-kt session storage). Offline launch with a stored session stays signed in and shows cached data. |
| Register | Email + password (min 6). This project has **email confirmation enabled**, so registration shows "Check your inbox" and the user signs in after confirming. Wallet + profile are provisioned by a database trigger. |
| Catalog | One unified request (`operators` + embedded `catalog_products`) once per cold launch in an app-scoped coroutine; Room is the UI source of truth; only the newest request may write; failures keep the cache (subtle warning); Retry only when no cache exists. |
| Wallet | Cached per user in DataStore and shown immediately; refreshed from `wallets` (RLS) when Top-up becomes visible (10 s throttle avoids rotation refetches); replaced only by backend-confirmed values. |
| Detection | Local PRD prefix table, longest prefix wins, `09` + 9 digits, normalises spaces/dashes, `+959…`, and Myanmar digits. No manual operator selection. |
| Recharge | Confirmation sheet → processing route → `process-recharge`. Idempotency key generated at confirm and reused on retry. Back cancels the client request. Success caches the returned transaction + balance and opens the success screen; insufficient balance shows a message and returns to the selection. |
| History | Room list scoped to the current user; background sync of page 0 (and next pages on Load More, 20 per page) via `list_my_transactions` with the same search / operator / date / sort filters. |
| Receipt | Captures only the receipt card (no balance, no branding, no app bar/nav) to PNG in cache, shares via FileProvider + Android Sharesheet. |
| Logout | Clears wallet DataStore keys and the transactions table, signs out (falls back to local session clear when offline), and the shell swaps to the auth graph. |

---

## 4. Supabase backend (deployed)

Source of truth in the repo: `supabase/migrations/`, `supabase/functions/process-recharge/`, `supabase/config.toml`.

| Object | Notes |
|--------|-------|
| `public.profiles`, `public.wallets` | 1:1 with `auth.users`; wallet default **500,000 MMK** (bigint, whole kyat), `balance_mmk >= 0` |
| `app_private.handle_new_auth_user` + trigger `on_auth_user_created` | Idempotent profile + wallet provisioning (`on conflict do nothing`), backfill in migration |
| `public.operators`, `public.catalog_products` | MPT / ATOM / Ooredoo / Mytel with Storage logo refs (`telecom/*.jpeg`); 7 denominations per operator + 16 **demo** data packages (`is_demo = true`) |
| `public.operator_prefixes` | Server-side copy of the PRD prefix dataset used to validate recharges (no client access) |
| `public.transactions` | Successful recharges only, product snapshot columns, `unique (user_id, idempotency_key)`, index `(user_id, created_at desc, id desc)` |
| `public.process_recharge(...)` | Single-transaction recharge: wallet row lock (`FOR UPDATE`), idempotent replay, prefix/operator/product/price validation, insufficient-balance rejection without writes. `EXECUTE` granted to `service_role` only |
| `public.list_my_transactions(...)` | Security-invoker history query (search, operators, date range, sort, limit/offset) scoped by `auth.uid()` + RLS |
| Edge Function `process-recharge` | Verifies the caller with `auth.getUser(token)` (deployed with `verify_jwt = false` so it works with legacy and new API keys), validates input, calls the RPC with the server key, returns structured JSON + HTTP status (402 for insufficient balance) |
| RLS | Enabled on all six tables. Users can only `SELECT` their own profile / wallet / transactions; authenticated users can read active catalog rows; no client `INSERT/UPDATE/DELETE` grants anywhere |

Custom amounts: demo rule **1,000–50,000 MMK in steps of 100**, stored on `operators` and enforced
by `process_recharge`; the app reads the same values from the catalog for client-side validation.

Verification scripts: `supabase/tests/database/rls_and_recharge_test.sql` (rolls itself back) and
`supabase/tests/e2e/process_recharge_e2e.sh` (needs two confirmed throwaway accounts).

---

## 5. Configuration

Root `local.properties` (never committed): `sdk.dir`, `RELEASE_KEYSTORE*` keys, `SUPABASE_URL`,
`SUPABASE_ANON_KEY` (client key — the legacy anon key or the newer publishable key both work).
No secret / service-role key is used anywhere in the Android app; the Edge Function reads the
platform-provided `SUPABASE_SECRET_KEYS` (falling back to `SUPABASE_SERVICE_ROLE_KEY`).

---

## 6. Known limitations

- Release builds rely on keep rules in `app/src/main/keepRules/rules.keep` that pin Kotlin
  multifile-facade part classes (`*Kt__*`) to their packages. Without them, the R8
  `optimization.packageScope` setting repackaged some parts away from their package-private
  superclasses and the app crashed on launch with `IllegalAccessError`.
- On wide windows `InitializeRatios` halves dp tokens, so the nav rail labels wrap and icons are
  small in landscape. This is existing design-system behaviour and was not changed.
- The phone format follows the PRD exactly (`09` + 9 digits); shorter legacy numbers are rejected.
  The prefix list is the PRD's initial dataset, not an authoritative numbering plan.
- The catalog is requested once per cold launch; if that request fails (e.g. offline start) the
  saved catalog is used with a warning until the next launch (Retry exists only when no cache).
- Registration requires email confirmation while "Confirm email" is enabled in Supabase Auth.
