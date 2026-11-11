package plugins

import configuration.SqlCipherConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for SQLCipher.
 *
 * Registered as: `topup.sqlcipher`
 */
class SqlCipherConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        SqlCipherConfigurer.configure(target)
    }
}
