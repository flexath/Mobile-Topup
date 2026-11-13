---
name: topup-design-system
description: Implements and reviews TopUp Compose UI against the project design system (AppTheme, AppColorScheme, Manrope typography, AppSpacing/AppShapes, DeviceType). Use when building screens, components, theming, tokens, adaptive spacing, previews, or editing core/designsystem in Mobile-Topup.
---

# TopUp Design System

Project-specific Compose design system for **Mobile-Topup** (native Android). Constraints also live in `.cursor/rules/topup-design-system.mdc` — follow both.

## Source of truth

| Concern | Location |
|---------|----------|
| Entry | `core/designsystem/.../theme/Theme.kt` → `AppTheme` |
| Colors | `theme/AppColorScheme.kt` |
| Type | `theme/AppTypography.kt` (Manrope only) |
| Spacing | `theme/AppSpacing.kt` + `getAppSpacing` |
| Shapes | `theme/AppShapes.kt` |
| Locals | `theme/AppThemeProvider.kt` |
| Density | `utils/CalculatedDensity.kt` (`InitializeRatios` inside `AppTheme`) |
| Window | `utils/DeviceType.kt` |

## Hard constraints

- Wrap every screen / preview in `AppTheme { }`
- Read tokens via `MaterialTheme.appColorScheme` / `appTypography` / `appSpacing` / `appShapes` / `deviceType`
- No raw `Color(0x…)`, hard-coded `dp`, or custom font families in feature/app UI
- No Material You dynamic color
- Extend tokens in theme files before using new values
- Shared components are `App*` in `:core:designsystem`; feature-only UI stays in that feature’s `presentation/components`

## Before coding

```
Discovery:
- [ ] Does a design-system token or App* component already exist?
- [ ] New semantic color/spacing/type? Plan AppColorScheme / AppSpacing / AppTypography change first
- [ ] Screen layout uses adaptive tokens (screenHorizontal, section, …)?
- [ ] Preview wrapped in AppTheme?
```

## Workflow — consume tokens

```kotlin
@Composable
fun ExampleRow(title: String) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing

    Text(
        text = title,
        style = MaterialTheme.appTypography.titleMedium,
        color = colors.contentPrimary,
        modifier = Modifier.padding(spacing.medium),
    )
}
```

## Workflow — adaptive screen layout

1. Read `MaterialTheme.deviceType` and `MaterialTheme.appSpacing`
2. Branch on `DeviceWidthClass` (Compact / Medium / Expanded / Large / ExtraLarge)
3. Page insets → adaptive tokens; component internals → fixed primitives
4. Keep state above adaptive branches
5. Do not redefine breakpoints outside `DeviceWidthClass`

```kotlin
@Composable
fun AdaptiveFeatureScreen() {
    val deviceType = MaterialTheme.deviceType
    val spacing = MaterialTheme.appSpacing

    Box(
        Modifier
            .fillMaxSize()
            .padding(
                horizontal = spacing.screenHorizontal,
                vertical = spacing.screenVertical,
            ),
    ) {
        when (deviceType.widthClass) {
            DeviceWidthClass.Compact -> CompactContent()
            DeviceWidthClass.Medium -> FlexibleContent(useTwoPanes = deviceType.isLandscape)
            DeviceWidthClass.Expanded,
            DeviceWidthClass.Large,
            DeviceWidthClass.ExtraLarge,
            -> MultiPaneContent(paneSpacing = spacing.pane)
        }
    }
}
```

## Workflow — extend the system

1. Add token in the owning theme file (`AppColorScheme` / `AppSpacing` / `AppTypography` / `AppShapes`)
2. If color: update light **and** dark; sync Material bridge in `Theme.kt` when the role maps to M3
3. Prefer `App*Defaults` for shared component constants
4. Add `@Preview` under `AppTheme` (light/dark when relevant)
5. Consume only via `MaterialTheme.*` extensions

## Anti-patterns

| ❌ Wrong | ✅ Correct |
|----------|------------|
| `Color(0xFF174F91)` in a feature | `MaterialTheme.appColorScheme.primary` |
| `Modifier.padding(16.dp)` in a screen | `Modifier.padding(MaterialTheme.appSpacing.large)` |
| `TextStyle(fontSize = 18.sp)` | `MaterialTheme.appTypography.titleMedium` |
| `FontFamily.Default` / new fonts in features | Manrope via `appTypography` |
| `MaterialTheme { }` / dynamic color | `AppTheme { }` |
| Feature-local copy of a shared button | `AppPrimaryButton` (or similar) in designsystem |

## Verify

```bash
./gradlew :core:designsystem:compileDebugKotlin :app:compileDebugKotlin
```

When adding token tests later, assert semantic roles and `getAppSpacing` / `getDeviceType` boundaries (`599/600`, `839/840`, `1199/1200`, `1599/1600`).
