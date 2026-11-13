package plugins

import configuration.SupabaseConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Supabase Kotlin client.
 *
 * Registered as: `topup.supabase`
 *
 * Pair with `topup.ktor` on the same module.
 */
class SupabaseConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        SupabaseConfigurer.configure(target)
    }
}
