package configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.libs
import utils.requireLibrary
import utils.testImplementation

/** Baseline unit-test dependencies for `:app`. */
internal object AndroidApplicationTestDependenciesConfigurer {

    fun configure(project: Project) {
        val junit = project.libs.requireLibrary("junit")
        project.dependencies {
            testImplementation(junit)
        }
    }
}
