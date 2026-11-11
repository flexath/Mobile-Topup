package configuration

import common.PluginIds
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary

/**
 * Koin for Native Android.
 *
 * Always adds `koin-android`. When the Compose compiler plugin is present,
 * also adds Compose ViewModel helpers.
 */
internal object KoinConfigurer {

    fun configure(project: Project) {
        val libs = project.libs
        project.dependencies {
            implementation(libs.requireLibrary("koin-android"))
        }

        project.pluginManager.withPlugin(PluginIds.KOTLIN_COMPOSE_COMPILER) {
            project.dependencies {
                implementation(libs.requireLibrary("koin-androidx-compose"))
                implementation(libs.requireLibrary("koin-androidx-compose-navigation"))
            }
        }
    }
}
