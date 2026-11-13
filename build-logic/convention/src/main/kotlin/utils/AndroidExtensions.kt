package utils

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

/**
 * Configures the AGP `android { ... }` DSL for an Android Application module.
 *
 * Caller MUST guarantee the `com.android.application` plugin has been applied.
 */
internal fun Project.androidApplication(action: ApplicationExtension.() -> Unit) {
    extensions.configure(ApplicationExtension::class.java, action)
}

/**
 * Configures the AGP `android { ... }` DSL for an Android Library module.
 *
 * Caller MUST guarantee the `com.android.library` plugin has been applied.
 */
internal fun Project.androidLibrary(action: LibraryExtension.() -> Unit) {
    extensions.configure(LibraryExtension::class.java, action)
}
