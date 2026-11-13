package configuration

import common.PluginIds
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary

/** Applies Kotlin Serialization and adds `kotlinx-serialization-json`. */
internal object SerializationConfigurer {

    fun configure(project: Project) {
        project.pluginManager.apply(PluginIds.KOTLIN_SERIALIZATION)

        val serializationJson = project.libs.requireLibrary("kotlinx-serialization-json")
        project.dependencies {
            implementation(serializationJson)
        }
    }
}
