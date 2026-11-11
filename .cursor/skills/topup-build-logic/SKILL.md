---
name: topup-build-logic
description: Implements and reviews TopUp Gradle convention plugins in build-logic (topup.* plugins, debug/release only, Native Android). Use when editing build-logic, module build.gradle.kts, libs.versions.toml, settings.gradle.kts, adding modules, stack plugins, signing, or AGP/Kotlin Gradle config in Mobile-Topup.
---

# TopUp Build Logic

Project-specific Gradle conventions for **Mobile-Topup** (native Android). Constraints also live in `.cursor/rules/topup-build-logic.mdc` — follow both.

## Hard constraints

- Native Android only — no KMP / Compose Multiplatform
- Build types: **`debug` + `release` only** — never add env flavors (`dev`/`stg`/`prod`)
- Plugin IDs: `topup.*` via version catalog aliases
- Java **18** via `JavaTargets`; SDK/app identity via `DefaultBuildConfig`
- Release signing: fail-hard on `RELEASE_*` keys in root `local.properties`

## Current convention plugins

| ID | Use |
|----|-----|
| `topup.android.application` | AGP app + SDK + signing + debug/release |
| `topup.android.application.compose` | App + Compose compiler/baseline deps |
| `topup.android.library` | AGP library + baseline Android/test deps |
| `topup.android.library.compose` | Library + Compose |
| `topup.serialization` | Serialization plugin + JSON |
| `topup.navigation` | Navigation Compose |
| `topup.firebase` | Google Services + Crashlytics + BoM (app) |
| `topup.room` | KSP + Room runtime/KTX/compiler + schema export |
| `topup.sqlcipher` | `sqlcipher-android` (pair with Room when encrypting) |
| `topup.datastore` | Preference DataStore |
| `topup.ktor` | Ktor OkHttp client stack (+ Serialization) |
| `topup.koin` | Koin Android (+ Compose helpers if Compose applied) |
| `topup.supabase` | Supabase BOM + postgrest/auth/realtime/storage/functions |
| `topup.coroutines` | kotlinx-coroutines core/android + test |

Pair `topup.supabase` with `topup.ktor`. Apply Android conventions before `topup.room`.

## Before coding

```
Discovery:
- [ ] build-logic/convention layout (common / configuration / plugins / utils)
- [ ] PluginIds ↔ convention/build.gradle.kts gradlePlugin ↔ libs.versions.toml
- [ ] Which module type (app vs compose lib vs plain lib)
- [ ] Module-specific deps only (projects.*, adaptive, appcompat, …)
```

## Add a module

1. `include(":…")` in root `settings.gradle.kts`
2. Create `build.gradle.kts` with the correct `topup.*` aliases only
3. Add project + module-specific dependencies — not baseline Compose/SDK wiring
4. Wire DI / nav in `:app` if needed (not in build-logic)

```kotlin
plugins {
    alias(libs.plugins.topup.android.library.compose)
    alias(libs.plugins.topup.serialization)
    alias(libs.plugins.topup.navigation)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
}
```

## Add a stack convention plugin

1. `configuration/*Configurer.kt` — apply plugin(s) + catalog deps
2. Thin `plugins/*ConventionPlugin.kt`
3. Register in `build-logic/convention/build.gradle.kts` `gradlePlugin { }`
4. Add id to `common/PluginIds.Convention`
5. Catalog entry under `[plugins]` (no version — includeBuild)
6. Optional `apply false` in root `build.gradle.kts`
7. Apply via `alias(libs.plugins.topup.…)` in consuming modules

## Change app defaults

- Identity / SDK / version / optimization package scope / signing key names → `DefaultBuildConfig`
- Signing / release optimization / defaultConfig → `AndroidApplicationConfigurer`
- Library namespace derivation → `utils.defaultNamespace()` + `AndroidLibraryConfigurer`

## After changes

```bash
./gradlew :build-logic:convention:validatePlugins
./gradlew :app:assembleDebug :app:assembleRelease
```

## Anti-patterns

| ❌ Wrong | ✅ Correct |
|----------|------------|
| Six env build types / product flavors | `debug` + `release` only |
| Raw `com.android.*` / compose in modules | `topup.*` convention aliases |
| Copy Compose BOM into every feature | Compose configurer owns baseline |
| Soft-skip missing release keystore | Fail-hard `RELEASE_*` keys |
| Port Phera KMP configurers blindly | Native `LibraryExtension` / app DSL |
