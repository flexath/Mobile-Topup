package configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary
import utils.testImplementation

/** kotlinx-coroutines (+ Android main dispatcher) and test artifacts. */
internal object CoroutinesConfigurer {

    fun configure(project: Project) {
        val libs = project.libs
        project.dependencies {
            implementation(libs.requireLibrary("kotlinx-coroutines-core"))
            implementation(libs.requireLibrary("kotlinx-coroutines-android"))
            testImplementation(libs.requireLibrary("kotlinx-coroutines-test"))
        }
    }
}
