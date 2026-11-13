package utils

import common.PluginIds
import org.gradle.api.Project

/**
 * Applies the K2 Compose compiler + `buildFeatures.compose` for an Android
 * application module.
 */
internal fun Project.applyAndroidApplicationCompose() {
    pluginManager.apply(PluginIds.KOTLIN_COMPOSE_COMPILER)
    androidApplication {
        buildFeatures {
            compose = true
        }
    }
}

/**
 * Applies the K2 Compose compiler + `buildFeatures.compose` for an Android
 * library module.
 */
internal fun Project.applyAndroidLibraryCompose() {
    pluginManager.apply(PluginIds.KOTLIN_COMPOSE_COMPILER)
    androidLibrary {
        buildFeatures {
            compose = true
        }
    }
}
