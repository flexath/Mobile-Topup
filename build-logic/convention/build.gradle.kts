plugins {
    `kotlin-dsl`
}

group = "com.aungthiha.topup.buildlogic.convention"

tasks.validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.google.services.gradlePlugin)
    compileOnly(libs.firebase.crashlytics.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kotlin.compose.gradlePlugin)
}

// -----------------------------------------------------------------------------
// Binary convention plugins (`implementationClass`). Plugin ids MUST match
// `common.PluginIds.Convention` — this script cannot reference that object.
// -----------------------------------------------------------------------------
gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "topup.android.application"
            implementationClass = "plugins.AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "topup.android.application.compose"
            implementationClass = "plugins.AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "topup.android.library"
            implementationClass = "plugins.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "topup.android.library.compose"
            implementationClass = "plugins.AndroidLibraryComposeConventionPlugin"
        }
        register("serialization") {
            id = "topup.serialization"
            implementationClass = "plugins.SerializationConventionPlugin"
        }
        register("navigation") {
            id = "topup.navigation"
            implementationClass = "plugins.NavigationConventionPlugin"
        }
        register("firebase") {
            id = "topup.firebase"
            implementationClass = "plugins.FirebaseConventionPlugin"
        }
    }
}
