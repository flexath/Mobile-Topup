package configuration

import androidx.room.gradle.RoomExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.ksp
import utils.libs
import utils.requireLibrary

/**
 * Room for Native Android: runtime, KTX, schema export, and KSP compiler.
 */
internal object RoomConfigurer {

    fun configureGradlePlugin(project: Project) {
        project.extensions.configure<RoomExtension> {
            schemaDirectory("${project.projectDir}/schemas")
        }
    }

    fun configureDependencies(project: Project) {
        val libs = project.libs
        project.dependencies {
            implementation(libs.requireLibrary("androidx-room-runtime"))
            implementation(libs.requireLibrary("androidx-room-ktx"))
            ksp(libs.requireLibrary("androidx-room-compiler"))
        }
    }
}
