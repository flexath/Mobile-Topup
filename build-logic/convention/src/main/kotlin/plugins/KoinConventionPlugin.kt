package plugins

import configuration.KoinConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Koin DI.
 *
 * Registered as: `topup.koin`
 */
class KoinConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        KoinConfigurer.configure(target)
    }
}
