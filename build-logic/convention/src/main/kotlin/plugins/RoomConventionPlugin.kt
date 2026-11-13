package plugins

import common.PluginIds
import configuration.RoomConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Room persistence.
 *
 * Registered as: `topup.room`
 */
class RoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target.pluginManager) {
            apply(PluginIds.KSP)
            apply(PluginIds.ANDROIDX_ROOM)
        }
        RoomConfigurer.configureGradlePlugin(target)
        RoomConfigurer.configureDependencies(target)
    }
}
