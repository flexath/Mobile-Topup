package configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary

/**
 * Ktor client stack for Native Android (OkHttp engine).
 *
 * Applies Serialization because `ktor-serialization-kotlinx-json` needs the
 * compiler plugin.
 */
internal object KtorConfigurer {

    fun configure(project: Project) {
        SerializationConfigurer.configure(project)

        val libs = project.libs
        project.dependencies {
            implementation(libs.requireLibrary("ktor-client-core"))
            implementation(libs.requireLibrary("ktor-client-okhttp"))
            implementation(libs.requireLibrary("ktor-client-content-negotiation"))
            implementation(libs.requireLibrary("ktor-client-logging"))
            implementation(libs.requireLibrary("ktor-client-auth"))
            implementation(libs.requireLibrary("ktor-client-encoding"))
            implementation(libs.requireLibrary("ktor-serialization-kotlinx-json"))
        }
    }
}
