package utils

import common.DefaultBuildConfig
import org.gradle.api.Project
import java.io.File
import java.util.Properties

/**
 * Credentials for signing `:app` release APKs / App Bundles from `local.properties`.
 *
 * [storeFilePath] may be absolute or relative to the Gradle project root.
 */
internal data class ReleaseSigningCredentials(
    val storeFilePath: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
) {
    fun resolveStoreFile(rootProject: Project): File {
        val trimmed = storeFilePath.trim()
        val candidate = File(trimmed)
        return if (candidate.isAbsolute) {
            candidate
        } else {
            rootProject.file(trimmed)
        }
    }
}

/**
 * Reads required Play Store signing settings from root `local.properties`.
 *
 * Fails configuration when any of the four release-signing keys is missing,
 * empty, or the keystore file does not exist — matching prior `:app` behaviour.
 */
internal fun Project.requireReleaseSigningCredentials(): ReleaseSigningCredentials {
    val propsFile = rootProject.layout.projectDirectory.file("local.properties").asFile
    if (!propsFile.exists()) {
        error(
            "Missing local.properties. Required for release signing: " +
                "${DefaultBuildConfig.RELEASE_KEYSTORE_PATH_KEY}, " +
                "${DefaultBuildConfig.RELEASE_KEYSTORE_PASSWORD_KEY}, " +
                "${DefaultBuildConfig.RELEASE_KEY_ALIAS_KEY}, " +
                "${DefaultBuildConfig.RELEASE_KEY_PASSWORD_KEY}.",
        )
    }

    val properties = Properties()
    propsFile.inputStream().use { properties.load(it) }

    fun requireSigningProp(key: String): String {
        val value = properties.getProperty(key)?.trim()
        if (value.isNullOrEmpty()) {
            error(
                "Missing `$key` in local.properties. " +
                    "Required for release signing: " +
                    "${DefaultBuildConfig.RELEASE_KEYSTORE_PATH_KEY}, " +
                    "${DefaultBuildConfig.RELEASE_KEYSTORE_PASSWORD_KEY}, " +
                    "${DefaultBuildConfig.RELEASE_KEY_ALIAS_KEY}, " +
                    "${DefaultBuildConfig.RELEASE_KEY_PASSWORD_KEY}.",
            )
        }
        return value
    }

    return ReleaseSigningCredentials(
        storeFilePath = requireSigningProp(DefaultBuildConfig.RELEASE_KEYSTORE_PATH_KEY),
        storePassword = requireSigningProp(DefaultBuildConfig.RELEASE_KEYSTORE_PASSWORD_KEY),
        keyAlias = requireSigningProp(DefaultBuildConfig.RELEASE_KEY_ALIAS_KEY),
        keyPassword = requireSigningProp(DefaultBuildConfig.RELEASE_KEY_PASSWORD_KEY),
    )
}
