package configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary

/** Preference DataStore for Native Android modules. */
internal object DatastoreConfigurer {

    fun configure(project: Project) {
        val preferences = project.libs.requireLibrary("androidx-datastore-preferences")
        project.dependencies {
            implementation(preferences)
        }
    }
}
