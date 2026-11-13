package utils

import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

/**
 * Type-safe, fail-fast lookups for [VersionCatalog] entries.
 */
fun VersionCatalog.requireLibrary(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow {
        IllegalStateException(
            "Library alias '$alias' is missing from version catalog '$name'. " +
                "Add it to libs.versions.toml under [libraries].",
        )
    }

fun VersionCatalog.requireBundle(alias: String): Provider<ExternalModuleDependencyBundle> =
    findBundle(alias).orElseThrow {
        IllegalStateException(
            "Bundle alias '$alias' is missing from version catalog '$name'. " +
                "Add it to libs.versions.toml under [bundles].",
        )
    }

fun VersionCatalog.requirePlugin(alias: String): Provider<PluginDependency> =
    findPlugin(alias).orElseThrow {
        IllegalStateException(
            "Plugin alias '$alias' is missing from version catalog '$name'. " +
                "Add it to libs.versions.toml under [plugins].",
        )
    }

fun VersionCatalog.requireVersion(alias: String): String =
    findVersion(alias).orElseThrow {
        IllegalStateException(
            "Version alias '$alias' is missing from version catalog '$name'. " +
                "Add it to libs.versions.toml under [versions].",
        )
    }.requiredVersion
