package configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary

/**
 * Supabase Kotlin client for Native Android.
 *
 * Adds the Supabase BOM plus common modules. Apply `topup.ktor` (and thus
 * Serialization) on the same module — Supabase requires a Ktor engine.
 */
internal object SupabaseConfigurer {

    fun configure(project: Project) {
        val libs = project.libs
        val bom = libs.requireLibrary("supabase-bom")

        project.dependencies {
            implementation(project.dependencies.platform(bom))
            implementation(libs.requireLibrary("supabase-postgrest"))
            implementation(libs.requireLibrary("supabase-auth"))
            implementation(libs.requireLibrary("supabase-realtime"))
            implementation(libs.requireLibrary("supabase-storage"))
            implementation(libs.requireLibrary("supabase-functions"))
        }
    }
}
