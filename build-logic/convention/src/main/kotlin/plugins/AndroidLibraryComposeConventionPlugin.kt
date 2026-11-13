package plugins

import common.PluginIds
import configuration.ComposeConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Android Library modules + Compose.
 *
 * Registered as: `topup.android.library.compose`
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(PluginIds.Convention.ANDROID_LIBRARY)
        ComposeConfigurer.configureAndroidLibrary(target)
    }
}
