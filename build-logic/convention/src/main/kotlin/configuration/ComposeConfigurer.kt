package configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.applyAndroidApplicationCompose
import utils.applyAndroidLibraryCompose
import utils.debugImplementation
import utils.implementation
import utils.libs
import utils.requireLibrary
import utils.androidTestImplementation

/**
 * Wires the Compose compiler and baseline Compose dependencies for Native
 * Android application and library modules.
 */
internal object ComposeConfigurer {

    fun configureAndroidApplication(project: Project) {
        project.applyAndroidApplicationCompose()
        addApplicationComposeDependencies(project)
    }

    fun configureAndroidLibrary(project: Project) {
        project.applyAndroidLibraryCompose()
        addLibraryComposeDependencies(project)
    }

    private fun addApplicationComposeDependencies(project: Project) {
        val libs = project.libs
        val composeBom = libs.requireLibrary("androidx-compose-bom")

        project.dependencies {
            implementation(project.dependencies.platform(composeBom))
            implementation(libs.requireLibrary("androidx-activity-compose"))
            implementation(libs.requireLibrary("androidx-compose-material3"))
            implementation(libs.requireLibrary("androidx-compose-ui"))
            implementation(libs.requireLibrary("androidx-compose-ui-graphics"))
            implementation(libs.requireLibrary("androidx-compose-ui-tooling-preview"))
            implementation(libs.requireLibrary("androidx-core-ktx"))
            implementation(libs.requireLibrary("androidx-lifecycle-runtime-ktx"))

            debugImplementation(libs.requireLibrary("androidx-compose-ui-tooling"))
            debugImplementation(libs.requireLibrary("androidx-compose-ui-test-manifest"))

            androidTestImplementation(project.dependencies.platform(composeBom))
            androidTestImplementation(libs.requireLibrary("androidx-compose-ui-test-junit4"))
            androidTestImplementation(libs.requireLibrary("androidx-espresso-core"))
            androidTestImplementation(libs.requireLibrary("androidx-junit"))
        }
    }

    private fun addLibraryComposeDependencies(project: Project) {
        val libs = project.libs
        val composeBom = libs.requireLibrary("androidx-compose-bom")

        project.dependencies {
            implementation(project.dependencies.platform(composeBom))
            implementation(libs.requireLibrary("androidx-compose-material3"))
            implementation(libs.requireLibrary("androidx-compose-ui"))
            implementation(libs.requireLibrary("androidx-compose-ui-graphics"))
            implementation(libs.requireLibrary("androidx-compose-ui-tooling-preview"))

            debugImplementation(libs.requireLibrary("androidx-compose-ui-tooling"))
        }
    }
}
