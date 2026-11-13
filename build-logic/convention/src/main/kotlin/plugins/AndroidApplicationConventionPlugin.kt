package plugins

import common.PluginIds
import configuration.AndroidApplicationConfigurer
import configuration.AndroidApplicationTestDependenciesConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for the Android Application module.
 *
 * Registered as: `topup.android.application`
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(PluginIds.ANDROID_APPLICATION)
        AndroidApplicationConfigurer.configure(this)
        AndroidApplicationTestDependenciesConfigurer.configure(this)
    }
}
