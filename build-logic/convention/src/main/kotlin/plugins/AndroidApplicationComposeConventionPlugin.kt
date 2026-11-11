package plugins

import common.PluginIds
import configuration.ComposeConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for the Android Application module + Compose.
 *
 * Registered as: `topup.android.application.compose`
 */
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(PluginIds.Convention.ANDROID_APPLICATION)
        ComposeConfigurer.configureAndroidApplication(target)
    }
}
