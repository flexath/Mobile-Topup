package plugins

import configuration.NavigationConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Navigation Compose.
 *
 * Registered as: `topup.navigation`
 */
class NavigationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        NavigationConfigurer.configure(target)
    }
}
