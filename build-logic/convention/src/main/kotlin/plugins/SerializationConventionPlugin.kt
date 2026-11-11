package plugins

import configuration.SerializationConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Kotlin Serialization.
 *
 * Registered as: `topup.serialization`
 */
class SerializationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        SerializationConfigurer.configure(target)
    }
}
