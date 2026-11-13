package plugins

import configuration.DatastoreConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Preference DataStore.
 *
 * Registered as: `topup.datastore`
 */
class DatastoreConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        DatastoreConfigurer.configure(target)
    }
}
