package utils

import common.DefaultBuildConfig
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.getByType

fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandler.debugImplementation(dependencyNotation: Any): Dependency? =
    add("debugImplementation", dependencyNotation)

fun DependencyHandler.testImplementation(dependencyNotation: Any): Dependency? =
    add("testImplementation", dependencyNotation)

fun DependencyHandler.androidTestImplementation(dependencyNotation: Any): Dependency? =
    add("androidTestImplementation", dependencyNotation)

/**
 * Primary `libs` version catalog for this build.
 */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Derives a stable Android namespace from the module's Gradle path.
 *
 * Examples:
 *  - `:app`                        -> `com.aungthiha.topup` (use APPLICATION_ID instead)
 *  - `:feature:topup`              -> `com.aungthiha.topup.feature.topup`
 *  - `:core:designsystem`          -> `com.aungthiha.topup.core.designsystem`
 */
fun Project.defaultNamespace(): String {
    if (path == ":app") {
        return DefaultBuildConfig.APPLICATION_ID
    }

    val prefix = DefaultBuildConfig.NAMESPACE_PREFIX
    val suffix = path
        .trim(':')
        .split(':')
        .filter { it.isNotEmpty() }
        .joinToString(".") { it.lowercase().replace('-', '_') }
        .ifBlank { name.lowercase().replace('-', '_') }

    return "$prefix.$suffix"
}
