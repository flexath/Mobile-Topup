package configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary

/** Adds SQLCipher Android for encrypted Room / SQLite databases. */
internal object SqlCipherConfigurer {

    fun configure(project: Project) {
        val sqlcipher = project.libs.requireLibrary("sqlcipher-android")
        project.dependencies {
            implementation(sqlcipher)
        }
    }
}
