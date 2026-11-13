# Adding Navigation in TopUp

Follow these checklists. Architecture details: [architecture.md](architecture.md).

---

## New screen in an existing feature

1. Add `@Serializable` route in `{Feature}Routes.kt`
2. Add callback(s) in `{Feature}NavCallbacks.kt` if the user can leave the screen
3. Add `NavGraphBuilder.{name}Screen()` next to the composable
4. Register it in `{Feature}NavGraph.kt`
5. Handle callbacks in `app/.../navigation/AppNavGraph.kt`

Cross-feature navigation: emit a callback in the source feature; target route lives in the target feature; map in `AppNavGraph` only — no feature-to-feature import.

---

## New feature with navigation

1. Create `presentation/navigation/` trio (`Routes`, `NavCallbacks`, `NavGraph`)
2. Add Compose + Navigation Compose + Kotlin Serialization to the feature module
3. Depend on `:core:common` and `:core:designsystem`
4. Register `{feature}NavGraph()` + `when` handlers in `AppNavGraph.kt`
5. Add `implementation(projects.feature.<name>)` to `:app`

---

## New top-level tab

1. Add `GraphRoute` + root screen route in the feature
2. Add `TopLevelDestination` entry (label, icon drawable, graph/root classes)
3. Wire graph in `AppNavGraph` if not already
4. Shell picks up tabs from `TopLevelDestination.entries` — update `showsAppNavigation` / `navigateToTopLevel` only if back-stack semantics change
5. Prefer icons from `:core:designsystem` drawables

---

## Full-screen flow (non-tab)

1. Nested `GraphRoute` in the owning feature
2. Chrome hides automatically when current destination is not a tab root (`showsAppNavigation`)
3. Exit with inclusive `popBackStack(route = FeatureRoutes.GraphRoute, inclusive = true)` from `AppNavGraph`

---

## Deep links (when adding)

1. Parse URI/intent in `MainActivity` (or an `:app` helper)
2. Map to an existing `@Serializable` route
3. Navigate once from the shell after `NavHost` is ready
4. Do not parse deep links in ViewModels, `:shared:*`, or feature business code
