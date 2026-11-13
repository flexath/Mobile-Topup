package configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary

/** Adds Navigation Compose for Native Android modules. */
internal object NavigationConfigurer {

    fun configure(project: Project) {
        val navigationCompose = project.libs.requireLibrary("androidx-navigation-compose")
        project.dependencies {
            implementation(navigationCompose)
        }
    }
}
