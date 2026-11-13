---
name: topup-navigation
description: Implements and reviews TopUp app navigation using the project's typed Routes/NavCallbacks/AppNavGraph pattern, TopUpAppShell tabs (floating bar/rail), and docs under docs/navigation. Use when adding screens, feature graphs, top-level tabs, cross-feature navigation, shell chrome, or deep links in this Mobile-Topup repository.
---

# TopUp Navigation

Project-specific navigation for **Mobile-Topup** (native Android). Source of truth:

1. Read [docs/navigation/architecture.md](../../../docs/navigation/architecture.md) before changing navigation
2. Follow checklists in [docs/navigation/adding-a-screen.md](../../../docs/navigation/adding-a-screen.md)
3. Keep `.cursor/rules/android-multi-module-navigation.mdc` as the architectural constraint set

## Quick rules

- Only `:app` owns `NavHostController` and performs `navigate` / `navigateUp` / `navigateToTopLevel`
- Features emit `*NavCallbacks`; never depend on other features for navigation
- Feature trio: `{Feature}Routes` + `{Feature}NavCallbacks` + `{Feature}NavGraph` under `presentation/navigation/`
- Chrome UI lives in `:core:designsystem` (`AppFloatingNavBar` / `AppNavRail`); selection wiring in `TopUpAppShell`
- Tab roots: Topup + History; Android `popUpTo<TopupRoutes.TopupScreen>` for tab switches
- Modules with routes need Navigation Compose + Kotlin Serialization

## Before coding

```
Discovery:
- [ ] TopUpAppShell / AppNavGraph / TopLevelDestination
- [ ] Feature Routes / NavCallbacks / NavGraph for the affected feature
- [ ] Serialization + navigation deps on the module
- [ ] No feature → feature Gradle dependency for nav
```

## After changes

- Wire new graphs/callbacks in `app/.../navigation/AppNavGraph.kt`
- Update `TopLevelDestination` when adding tabs
- Prefer icons from `:core:designsystem` drawables
- Keep docs in `docs/navigation/` accurate if ownership or entry chain changes
