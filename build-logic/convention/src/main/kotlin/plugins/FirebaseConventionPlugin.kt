package plugins

import configuration.FirebaseConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Firebase on the Android application module.
 *
 * Registered as: `topup.firebase`
 */
class FirebaseConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        FirebaseConfigurer.configureAndroidApplication(target)
    }
}
