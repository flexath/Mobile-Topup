package configuration

import common.PluginIds
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.implementation
import utils.libs
import utils.requireLibrary

/**
 * Firebase wiring for the Native Android `:app` module.
 *
 * Applies Google Services + Crashlytics Gradle plugins and adds the Firebase
 * BoM with Analytics and Crashlytics runtime dependencies.
 */
internal object FirebaseConfigurer {

    fun configureAndroidApplication(project: Project) {
        project.pluginManager.apply(PluginIds.GOOGLE_SERVICES)
        project.pluginManager.apply(PluginIds.FIREBASE_CRASHLYTICS)

        val firebaseBom = project.libs.requireLibrary("firebase-bom")
        val firebaseAnalytics = project.libs.requireLibrary("firebase-analytics")
        val firebaseCrashlytics = project.libs.requireLibrary("firebase-crashlytics")

        project.dependencies {
            implementation(project.dependencies.platform(firebaseBom))
            implementation(firebaseAnalytics)
            implementation(firebaseCrashlytics)
        }
    }
}
