# TopUp Navigation Architecture

Canonical navigation for this app. Aligns with `.cursor/rules/android-multi-module-navigation.mdc` and the implemented shell.

> **Native Android only.** Library: `androidx.navigation:navigation-compose` with typed `@Serializable` routes.

---

## Ownership

| Layer | Owns | Must not own |
|-------|------|--------------|
| `:app` | `NavHostController`, `TopUpAppShell`, `AppNavGraph`, `TopLevelDestination`, `navigateToTopLevel`, deep-link entry | Feature screens, repository details |
| `:feature:*` | Routes, NavCallbacks, nested graph, screen `NavGraphBuilder` extensions | Other features, `NavHostController` |
| `:core:common` | `AppRoutes` marker | NavHost, feature imports |
| `:core:designsystem` | `AppFloatingNavBar`, `AppNavRail`, glass/drag chrome | Routing / `navigate(...)` |
| `:shared:*` | Business domain only | Navigation, Compose screens |

**Golden rule:** Only `:app` creates `NavHostController` and maps callbacks across features.

```
app → feature → shared → core
```

---

## Entry chain

```
MainActivity               // system splash held until session + theme are known
  → AppTheme(darkTheme from Settings)
  → TopUpAppShell(authState)
      → Unauthenticated → AppNavGraph(start = AuthRoutes.GraphRoute)   // no chrome
      → Authenticated   → AppNavGraph(start = TopupRoutes.GraphRoute)  // tabs + chrome
  → feature *NavGraph()
```

The shell crossfades between one navigation state per session (`signed-out` or the user id).
Signing in or out swaps the whole graph, so authenticated destinations are unreachable without a
session and a new account never inherits the previous account's back stack. Features never
navigate on sign-in/sign-out; they only change the session.

### Graphs registered in `AppNavGraph`

| Graph | Destinations | Chrome |
|-------|--------------|--------|
| `AuthRoutes` | Login, Register | Hidden |
| `TopupRoutes` | TopupScreen (wallet overview, tab root), RechargeScreen, RechargeProcessing(args), RechargeSuccess(transactionId) | Root only |
| `TransactionHistoryRoutes` | TransactionHistoryScreen (tab root), TransactionDetailScreen(transactionId) | Root only |
| `SettingsRoutes` | SettingsScreen (opened from the Top-up app bar, not a tab) | Hidden |

Recharge flow: `TopupScreen.ToRecharge` pushes `RechargeScreen` (number, operator, denominations /
packages, Proceed); `RechargeScreen.ToProcessing(route)` pushes processing; `ToSuccess` replaces it
(`popUpTo<RechargeProcessing> { inclusive = true }`); `ProcessingScreen.BackToRecharge` pops to
`RechargeScreen` with the selection intact; `SuccessScreen.BackToTopup` pops to `TopupScreen`;
`SuccessScreen.ToHistory` pops the finished flow first, then switches tabs so the Top-up tab
restores to its root. Leaving the processing screen clears its ViewModel, which cancels the
in-flight request (best effort — the backend idempotency key makes retries safe).

| File | Role |
|------|------|
| `app/.../MainActivity.kt` | Host; edge-to-edge + theme |
| `app/.../TopUpAppShell.kt` | Shell chrome (floating bar / rail) + selected tab |
| `app/.../navigation/AppNavGraph.kt` | `NavHost` + callback → navigate |
| `app/.../navigation/TopLevelDestination.kt` | Tabs, visibility, `navigateToTopLevel` |
| `core/common/.../navigation/AppRoutes.kt` | Route marker interface |
| `core/designsystem/.../components/navigation/` | Chrome UI only |
| `feature/<name>/.../presentation/navigation/` | Feature Routes / NavCallbacks / NavGraph |

---

## Feature navigation trio

Every feature with screens owns three files under `presentation/navigation/`:

```
feature/<name>/presentation/navigation/
├── {Feature}Routes.kt       # @Serializable destinations extending AppRoutes
├── {Feature}NavCallbacks.kt # sealed navigation intents (not serializable)
└── {Feature}NavGraph.kt     # NavGraphBuilder.{feature}NavGraph()
```

Screen registration is colocated with the composable (`NavGraphBuilder.{name}Screen()`).

### Routes

```kotlin
sealed class TopupRoutes : AppRoutes {
    @Serializable data object GraphRoute : TopupRoutes()
    @Serializable data object TopupScreen : TopupRoutes()
}
```

### Callbacks

```kotlin
sealed interface TopupNavCallbacks {
    data object NavigateUp : TopupNavCallbacks
    sealed interface TopupScreen : TopupNavCallbacks {
        data object ToHistory : TopupScreen
    }
}
```

### Graph

```kotlin
fun NavGraphBuilder.topupNavGraph(
    modifier: Modifier = Modifier,
    onNavRoute: (TopupNavCallbacks) -> Unit,
) {
    navigation<TopupRoutes.GraphRoute>(startDestination = TopupRoutes.TopupScreen) {
        topupScreen(modifier = modifier, onNavRoute = onNavRoute)
    }
}
```

UI and ViewModels call `onNavRoute(...)` only. **Never** import or inject `NavHostController` in features.

---

## App root graph

`AppNavGraph` is the **only** place that calls `navigate`, `navigateUp`, `popBackStack`, or `navigateToTopLevel`.

```kotlin
topupNavGraph(onNavRoute = {
    when (it) {
        TopupNavCallbacks.NavigateUp -> navController.navigateUp()
        TopupNavCallbacks.TopupScreen.ToHistory ->
            navController.navigateToTopLevel(TopLevelDestination.History)
    }
})
```

Cross-feature targets are resolved here. Do **not** add `implementation(project(":feature:other"))` for navigation.

---

## Top-level tabs & chrome

Current tabs:

| Destination | Label | Icon |
|-------------|-------|------|
| `TopLevelDestination.Topup` | Topup | `R.drawable.ic_topup` |
| `TopLevelDestination.History` | History | `R.drawable.ic_transaction_history` |

- **Compact:** floating glass bar over content (`AppFloatingNavBar`)
- **Medium+:** vertical rail (`AppNavRail`)
- Chrome visible only when `showsAppNavigation()` is true (tab **root** screens)
- Tab labels use `labelMedium`; selected = `FontWeight.SemiBold`

### Android tab switch

```kotlin
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    if (currentDestination.toTopLevelDestination() == destination) return
    navigate(destination.graphRoute) {
        popUpTo<TopupRoutes.TopupScreen> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
```

`TopupScreen` is the Android task root so system Back returns there instead of leaving History as the first reachable screen.

Start destination today: `TopupRoutes.GraphRoute`.

---

## Gradle

Modules that define routes or `NavGraphBuilder` extensions need:

- `androidx.navigation:navigation-compose`
- Kotlin Serialization plugin + `kotlinx-serialization-json`

`:app` depends on every feature whose graph it composes. Features depend on `:core:*` and `:shared:*` modules — never on each other.

---

## Anti-patterns

| ❌ Wrong | ✅ Correct |
|----------|-----------|
| `NavController` in ViewModel or feature | Callbacks from UI; navigate in `:app` |
| String-based routes | Typed `@Serializable` routes |
| Feature → feature Gradle dep for nav | Route types imported by `:app` only |
| Second `NavHost` outside app shell | One `NavHost` in `:app` |
| NavHost / AppNavGraph in `:shared:*` | Routing only in `:app` |
| Hilt/Koin `NavHostController` singleton | `rememberNavController()` in `TopUpAppShell` |

---

## Related

- [Adding a screen](adding-a-screen.md) — checklists for new screens, features, and tabs
- Project rule: `.cursor/rules/android-multi-module-navigation.mdc`
- Clean architecture: `.cursor/rules/android-clean-architecture.mdc`
