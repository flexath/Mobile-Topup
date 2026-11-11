package plugins

import configuration.KtorConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Ktor client (Android / OkHttp).
 *
 * Registered as: `topup.ktor`
 */
class KtorConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        KtorConfigurer.configure(target)
    }
}
