package plugins

import configuration.CoroutinesConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for kotlinx-coroutines and Flows.
 *
 * Registered as: `topup.coroutines`
 */
class CoroutinesConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        CoroutinesConfigurer.configure(target)
    }
}
