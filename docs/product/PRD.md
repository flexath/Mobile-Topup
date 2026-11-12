# TopUp — Product Requirements Document (PRD)

**Project:** TopUp Android Mobile Top-up App  
**Purpose:** KBZ Bank Android Developer Coding Test  
**PRD version:** 1.1  
**Date:** 11 November 2026  
**Target delivery:** 72 hours  
**Platform:** Native Android

---

## 1. Product Overview

TopUp is a Myanmar mobile top-up application. A user creates an account or signs in with email and password, then enters a mobile number, the app detects the telecom operator, displays the operator's available denominations and data packages, and allows the user to complete a simulated recharge using that user's demo wallet balance.

Successful transactions are stored and displayed in that user's transaction history.

The implementation should demonstrate the coding-test requirements while keeping the product scope intentionally small and achievable within the 72-hour assignment.

## 2. Goals

- Authenticate users with Supabase email/password registration and login.
- Persist the authenticated session across app restarts and allow logout.
- Keep wallet balances and transactions user-specific under Supabase RLS.
- Detect supported Myanmar telecom operators from a mobile number.
- Allow users to browse and select denominations or data packages.
- Provide a simple recharge flow with a simulated wallet balance.
- Show a clear recharge success screen.
- Persist successful transactions and display them in History.
- Support transaction search, filtering, sorting, details, and receipt sharing.
- Support portrait and landscape orientations without losing screen state or reloading data.
- Demonstrate Kotlin, MVVM, Room, animations, transitions, and a maintainable modular architecture.

## 3. Out of Scope

The following are not part of this coding-test implementation:

- Real telecom-provider recharge integration.
- Real banking or payment gateway integration.
- Password reset, email verification UX beyond what Supabase requires by default, social login, MFA, or biometric unlock.
- A real bank wallet or wallet API.
- Failed or pending transaction history.
- Push notifications.
- Onboarding or welcome screens.
- Receipt image saving.
- Production-grade account recovery, admin tooling, or multi-device conflict resolution beyond a normal Supabase session.

Recharge processing is simulated through a Supabase backend function. The app must not imply that airtime or data is actually credited by a telecom provider.

## 4. Target Users

A signed-in user in Myanmar who wants to:
1. Create an account or sign in with email and password.
2. Enter a supported mobile number.
3. Identify its telecom operator automatically.
4. Choose a top-up denomination or data package.
5. Complete a simulated recharge against their own wallet balance.
6. Review or share their successful transaction confirmation later.

## 5. Supported Operators and Mobile Prefixes

The initial supported operators and the common mobile-number prefixes supplied for this project are:

### Myanma Posts and Telecommunications (MPT)

- `09 20–25`
- `09 260–268`
- `09 40–45`
- `09 47`
- `09 49`
- `09 50–56`
- `09 73`
- `09 83–89`
- `09 91`

### ATOM Myanmar (formerly Telenor)

- `09 75`
- `09 76`
- `09 77`
- `09 78`
- `09 79`

### Ooredoo Myanmar

- `09 95`
- `09 96`
- `09 97`

### Telecom International Myanmar (Mytel)

- `09 68`
- `09 69`

Operator logos are already uploaded to Supabase Storage.

Operator detection must use a locally available prefix list so that detection works offline. The app should not require a network request just to identify an operator. Store the prefix data locally and match the longest applicable prefix where prefix lengths overlap. Treat the ranges above as the project's initial common-prefix dataset; do not assume this list is exhaustive or automatically authoritative beyond the information supplied for this PRD.

## 6. Functional Requirements

### 6.1 Authentication

Use Supabase Email/Password Authentication.

#### Requirements

1. **Registration:** Users can create an account using email and password.
2. **Login:** Existing users can sign in using email and password.
3. **Session persistence:** Keep users signed in after closing and reopening the app.
4. **Logout:** Users can sign out.
5. **User-specific data:** Associate wallet balances and transactions with the authenticated user's Supabase `user_id`.
6. **Data security:** Use Supabase Row Level Security (RLS) so users can only access their own wallet and transaction records.

#### Identity and access rule

The Android app must never decide which user's wallet or transactions to access by supplying an arbitrary user ID. Supabase must derive the authenticated identity from the session and enforce access through RLS.

This keeps the implementation simple while making the wallet and transaction history genuinely user-specific.

#### Auth UX expectations

- Unauthenticated users can reach Login and Register only.
- Authenticated users reach the main Top-up / History experience.
- After logout, clear the local authenticated session and return the user to Login / Register.
- Locally cached wallet and transaction data must not be shown as another user's data after logout or account switch. Prefer clearing or isolating user-scoped local cache on logout.

### 6.2 Top-up: Phone Number Entry and Operator Detection

- The Top-up screen is the authenticated start destination inside the main app shell.
- Users enter a Myanmar local-format mobile number: `09xxxxxxxxx`.
- The input accepts typing while the number is incomplete.
- Validate the number and detect an operator only when the number is valid.
- Detect the operator using the locally stored prefix list in Section 5.
- Use the supplied common-prefix mappings for MPT, ATOM, Ooredoo, and Mytel.
- Display the detected operator's name and logo.
- If the number is invalid, show validation feedback.
- If the number is valid but its prefix is unsupported, show an unsupported-operator state.
- Do not provide manual operator selection.
- Prevent the user from proceeding when the number is invalid or unsupported.

### 6.3 Catalog: Denominations and Data Packages

- Display denominations and data packages for the detected operator.
- Present packages in a clear, comparable, selectable format.
- The catalog contains data for all four supported operators.
- Fetch the complete catalog in one unified API response.
- Start the catalog request once per cold app launch.
- Store catalog data in Room.
- Room is the source of truth observed by the UI.
- On launch, show cached Room data immediately while refreshing the catalog in the background.
- Do not block app startup while waiting for the catalog request.
- Switching operators or reopening package selection must not trigger another catalog request.
- Do not provide manual catalog refresh.
- If refresh fails but cached data exists, keep showing cached data and show a subtle offline/refresh warning.
- If the first catalog request fails and Room has no catalog data, show an error state with a Retry action. Do not allow package selection until catalog data is available.
- If a new app-launch request replaces an earlier in-flight request, only the latest request should be allowed to update the catalog.

### 6.4 Package Selection

- Show operator information and available packages together on the package-selection screen.
- Display the current wallet balance on this screen.
- Allow the user to edit the phone number on this screen.
- Re-run local operator detection when the number changes.
- If the operator remains the same, retain the package list.
- If the operator changes, display that operator's cached packages and clear the previously selected package.
- If the edited number is invalid or unsupported, prevent proceeding.
- Provide a prominent **Proceed to Recharge** action after a valid package is selected.

### 6.5 Wallet Balance

- Use a simulated wallet maintained by the Supabase backend.
- Each authenticated user has their own wallet, associated with their Supabase `user_id`.
- Initial demo wallet balance: **500,000 MMK**, configured on the backend for that user's wallet.
- Do not hardcode the initial balance in the Android app.
- Fetch the wallet balance for the current authenticated session whenever the Top-up screen opens.
- Do not pass an arbitrary user ID from the app to select whose wallet to read; rely on the session and RLS.
- Cache the latest successful balance in DataStore (`:core:datastore`) as user-scoped cache for the signed-in user.
- Expose the cached balance to the UI through a Flow.
- If the balance request fails, continue showing the last cached balance for the current user and show a subtle warning.
- After a successful recharge, update the cached/displayed balance using the balance returned by the backend.
- The selected package price is the only deduction; there is no service fee.
- If the device is offline, show the cached balance but disable recharge because the backend cannot validate or process it.
- Do not perform local wallet deductions or local recharge transactions.

### 6.6 Recharge Confirmation

Before submitting a recharge, show a confirmation dialog or screen containing:

- Phone number
- Operator
- Selected package
- Package price
- Current wallet balance
- Confirm and Cancel actions

The user must explicitly confirm before the recharge request is submitted.

### 6.7 Recharge Processing and Result

- Submit the recharge to a Supabase Edge Function.
- The backend simulates the recharge and returns Success or Failed.
- No real telecom-provider API is called.
- The processing experience should use a short animation, approximately 2–3 seconds as a UX target.
- Do not show success until the backend confirms success.
- The backend validates wallet balance.
- If balance is sufficient, the backend completes the simulated recharge and deducts the package price.
- If balance is insufficient, reject the attempt without deducting money and without creating a transaction-history record.
- For insufficient balance, show a message and remain on the package-selection screen so the user can choose a cheaper package and retry.
- No service fee is charged.
- A successful recharge opens a dedicated success screen.

**Transaction statuses in this implementation:** Success and Failed may be returned by the backend, but only successful transactions are retained and shown in the app's transaction history. Pending status is excluded from the implementation.

### 6.8 Success Screen

Show a dedicated success screen containing:

- Animated checkmark or equivalent success visual
- Clear confirmation message
- Transaction ID
- Phone number
- Operator name and logo
- Package name and details
- Amount
- Validity
- Remaining wallet balance

Provide actions to:
- Return to Top-up
- Open History
- Share the transaction receipt image

Persist the successful transaction locally in Room and remotely through the backend.

### 6.9 Transaction History

- Provide a History destination in the existing main navigation.
- Show successful transactions only for the authenticated user.
- Associate transactions with the authenticated user's Supabase `user_id`.
- Do not pass an arbitrary user ID from the app to select whose history to read; rely on the session and RLS.
- Do not show failed or pending transactions.
- Each list item should show useful summary information, including date, amount, and package details.
- Load the latest 20 transactions initially.
- Provide a **Load More** action to retrieve the next 20.
- Use Room as the UI source of truth for the current user's cached history.
- When History opens, fetch the latest history from Supabase in the background and update Room.
- Keep cached history available if synchronization fails and show a subtle sync/offline warning.
- No manual refresh action is required.

#### History Filters

Support combined filters for:
- Date range
- Operator: MPT, ATOM, Ooredoo, Mytel

There is no status filter because History contains successful transactions only.

#### History Search

- Search by phone number.
- Search by transaction ID.
- Support partial text matching.
- Search can be combined with the date-range and operator filters.

#### History Sorting

- Default sorting: newest first.
- Allow the user to switch to oldest first.
- Sorting applies to the current filtered/search result set.

### 6.10 Transaction Detail

- Tapping a history item opens a dedicated transaction detail screen.
- Display:
  - Transaction ID
  - Date and time
  - Phone number
  - Operator name and logo
  - Package name and details
  - Amount
  - Status (Success)
  - Wallet balance after the transaction
- Provide a Share action.

### 6.11 Receipt Image Sharing

- Provide Share on both the success screen and transaction detail screen.
- Generate the receipt image from the transaction detail content.
- Capture only the transaction detail content; exclude app bars and navigation.
- Include:
  - Transaction ID
  - Date and time
  - Phone number
  - Operator
  - Package name and details
  - Amount
  - Transaction status
- Exclude wallet balance.
- Do not include TopUp branding, logo, or app name.
- Open the Android Sharesheet immediately after generating the image.
- Do not provide a separate Save Image action.

### 6.12 Settings and Theme

- Add a Settings icon to the Top-up screen app bar.
- Do not add Settings to the main navigation.
- Settings initially contains:
  - Appearance: Light, Dark, System default
  - Account: signed-in email (read-only) and Logout
  - App information: TopUp and app version 1.0.0
- Persist the selected theme using DataStore.
- System default follows the Android system theme.
- The app supports both light and dark themes.
- Logout signs the user out of the Supabase session and returns them to Login / Register.

### 6.13 Launch and Navigation

- Use the standard Android splash screen with the TopUp app icon.
- After splash, check the authentication session.
- If authenticated, open the main app shell at Top-up.
- If not authenticated, open Login / Register.
- Do not show onboarding or welcome screens.
- Keep the existing two authenticated main destinations:
  - Top-up
  - History
- Preserve the existing adaptive navigation behavior for the authenticated shell.

#### Updated app flow

```
App Launch
  → Check Authentication Session
      → Authenticated → Top-up / History
      → Not authenticated → Login / Register
```

### 6.14 Orientation and State Preservation

- Support portrait and landscape orientations.
- Preserve the current screen and navigation state during rotation.
- Preserve entered phone numbers and selected packages.
- Do not reload catalog data or repeat requests just because the device rotates.
- Keep loaded wallet and transaction data available through the appropriate state holders and local storage.
- Use adaptive layouts where necessary.

### 6.15 Back Navigation During Recharge

- Allow the user to leave the processing screen using Back.
- Attempt to cancel the active client request.
- Cancellation is best-effort; it cannot guarantee that a backend operation already accepted or processed will be reversed.
- Keep this behavior simple for the coding-test scope; no background recovery or notification system is required.

## 7. Main User Flows

### Flow A — App Launch and Authentication Gate

1. User opens the app.
2. Standard Android splash screen is shown.
3. App checks for an existing Supabase authentication session.
4. If authenticated, open Top-up / History.
5. If not authenticated, open Login / Register.

### Flow B — Register or Login

1. Unauthenticated user opens Login / Register.
2. New users register with email and password.
3. Existing users sign in with email and password.
4. On success, Supabase establishes an authenticated session.
5. App navigates to the authenticated main shell at Top-up.
6. Subsequent launches restore the session without requiring login again until logout.

### Flow C — Successful Recharge

1. Authenticated user opens Top-up.
2. App displays cached catalog and that user's wallet balance when available.
3. App refreshes catalog and wallet balance in the background using the session.
4. User enters a valid Myanmar mobile number.
5. App detects and displays the operator.
6. User selects a denomination or data package.
7. User taps Proceed to Recharge.
8. App displays the confirmation details.
9. User confirms.
10. App submits the request to Supabase with the authenticated session.
11. Backend validates that user's wallet balance and simulates the recharge.
12. On success, backend deducts the package price and returns transaction details and updated balance for that user.
13. App stores the successful transaction in Room and updates the cached wallet balance.
14. App displays the success screen.
15. User can return to Top-up, open History, or share the receipt image.

### Flow D — Insufficient Balance

1. Authenticated user selects a package and confirms recharge.
2. Backend checks that user's wallet balance.
3. If the balance is insufficient, backend rejects the request.
4. No wallet deduction or history record is created.
5. App displays an insufficient-balance message.
6. User remains on package selection and may choose a cheaper package.

### Flow E — View Transaction History

1. Authenticated user opens History.
2. App displays locally cached successful transactions for that user.
3. App fetches the latest transactions from Supabase in the background using the session and RLS.
4. Room is updated and the UI reflects the latest data.
5. User can search by phone number or transaction ID.
6. User can filter by date range and operator.
7. User can sort newest-first or oldest-first.
8. User can load more transactions.
9. User taps a transaction to open its detail screen.
10. User can share the receipt image.

### Flow F — Logout

1. Authenticated user opens Settings.
2. User taps Logout.
3. App signs out of the Supabase session.
4. User-scoped local wallet/history cache is cleared or isolated so it is not shown to the next session.
5. App returns to Login / Register.

### Flow G — Offline Catalog Use

1. Authenticated user opens the app without network connectivity.
2. App displays cached catalog data from Room, if available.
3. Operator detection continues to work using local prefix data.
4. Cached wallet balance for the current user may be displayed.
5. Recharge is disabled while offline.
6. If no catalog is cached, show an error state and Retry action when connectivity is available.

## 8. Data and Persistence Requirements

| Data | Source of truth | Local persistence |
|---|---|---|
| Auth session | Supabase Auth | Supabase client session persistence |
| Operator prefix list | Local app data | Local database/storage |
| Operator catalog and packages | Supabase | Room |
| Successful transactions | Supabase/backend result, scoped by `user_id` + RLS | Room (current-user cache) |
| Wallet balance | Supabase, scoped by `user_id` + RLS | DataStore cache (current-user) |
| Theme preference | User preference | DataStore |

Room is used for the catalog and transaction history. DataStore is used for the cached wallet balance and theme preference. Wallet and transaction records must be associated with the authenticated user's Supabase `user_id`, with access enforced by RLS. The Android app must not choose another user's data by passing an arbitrary user ID.

## 9. Technical and Architecture Constraints

The implementation should align with the existing project:

- Native Android
- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Room
- DataStore
- Supabase
- Ktor for networking where appropriate
- Koin for dependency injection
- Kotlin Coroutines and Flow
- Navigation Compose
- Minimum SDK: **24**

Existing Gradle modules:

- `:app`
- `:feature:topup`
- `:feature:transactionhistory`
- `:core:common`
- `:core:designsystem`
- `:core:datastore`

Keep the existing authenticated app shell and two top-level destinations. Implement authentication UI and session gating in a suitable feature/shared module split, Top-up functionality in `:feature:topup`, and history/list/detail functionality in `:feature:transactionhistory`. Shared concerns should remain in suitable core or shared modules, following the current dependency direction.

The architecture should keep UI, domain logic, and data access separated. ViewModels should expose observable UI state and invoke domain operations through clear interfaces.

Authentication identity for wallet and transaction access must come from the Supabase session and RLS, not from an app-supplied user ID selector.

## 10. Design and Experience Requirements

- Attractive, clean, modern mobile UI.
- Consistent use of the existing design system.
- Support light and dark themes.
- Clear visual hierarchy for operator, package, price, and wallet balance.
- Clear validation, loading, offline, empty, error, and success states.
- Use animations and transitions where they add value, especially:
  - Navigation and screen transitions
  - Recharge processing
  - Success checkmark
  - Relevant list interactions
- Support portrait and landscape layouts.
- Avoid unnecessary screens and complexity.

## 11. Acceptance Criteria

The assignment implementation is considered complete when:

- [ ] App launches with the standard Android splash screen, then routes by authentication session.
- [ ] Unauthenticated users can register with email and password.
- [ ] Unauthenticated users can log in with email and password.
- [ ] Authenticated session persists after closing and reopening the app.
- [ ] Users can log out and return to Login / Register.
- [ ] Wallet balances and transactions are associated with the authenticated user's `user_id`.
- [ ] Supabase RLS prevents users from accessing another user's wallet or transactions.
- [ ] The Android app does not select wallet/history by supplying an arbitrary user ID.
- [ ] Authenticated Top-up and History navigation remains functional.
- [ ] User can enter a Myanmar local-format mobile number.
- [ ] Supported operator is detected offline from local prefixes.
- [ ] Invalid and unsupported numbers are handled clearly.
- [ ] Denominations and packages for all four operators are loaded from the unified catalog.
- [ ] Catalog is cached in Room and displayed offline when available.
- [ ] Wallet balance is fetched from Supabase for the current user and cached in DataStore.
- [ ] User can select a package and review a confirmation before submitting.
- [ ] Recharge is simulated by a Supabase backend function for the authenticated user.
- [ ] Insufficient balance does not deduct funds or create a history record.
- [ ] Successful recharge displays the required success information.
- [ ] Successful transactions are stored in Room for the current user.
- [ ] History shows that user's successful transactions only.
- [ ] History supports date and operator filters.
- [ ] History supports phone-number and transaction-ID search.
- [ ] History supports newest-first and oldest-first sorting.
- [ ] History loads 20 transactions at a time with Load More.
- [ ] Transaction detail displays all specified transaction information.
- [ ] Receipt image sharing works through Android Sharesheet.
- [ ] Settings allows Light, Dark, and System theme selection, and Logout.
- [ ] Theme preference persists across app restarts.
- [ ] Portrait and landscape are supported without unnecessary data reloads.
- [ ] Animations and transitions are present.
- [ ] Project builds and can be delivered as a source ZIP or GitHub repository.

## 12. Explicit Scope Deviations from the Assignment Brief

The original assignment lists Success, Pending, and Failed transaction statuses and asks for status filtering. For this implementation, the agreed scope is intentionally narrower in history status handling and broader in authentication:

- History displays successful transactions only.
- Failed and pending transactions are not displayed in History.
- The status filter is removed.
- Recharge processing is simulated through Supabase rather than integrated with real telecom systems.
- Supabase Email/Password Authentication, session persistence, logout, and RLS-backed user-specific wallets/transactions are included even though the original brief did not require a full auth system.

These are deliberate coding-test scope decisions, not requirements from the original assignment.

## 13. Assumptions and Items Not Defined by This PRD

The following implementation details are not fully specified here and should be kept simple:

- The exact authoritative Myanmar operator prefix dataset and update mechanism.
- The exact Supabase Auth, table, RLS policy, and Edge Function schemas.
- Whether new wallets are created by a database trigger, Edge Function, or first-login provisioner.
- The exact package fields returned by the catalog API.
- The exact visual design and spacing of each screen, including Login / Register.
- The detailed implementation of cancellation after a request reaches the backend.
- Password-reset and email-confirmation product UX beyond Supabase defaults required to make email/password auth work for the demo.

Do not expand these into production-grade systems unless required to complete the assignment.

---

**End of PRD**
