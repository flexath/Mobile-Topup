package plugins

import common.PluginIds
import configuration.AndroidLibraryConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Android Library modules.
 *
 * Registered as: `topup.android.library`
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(PluginIds.ANDROID_LIBRARY)
        AndroidLibraryConfigurer.configure(this)
    }
}
