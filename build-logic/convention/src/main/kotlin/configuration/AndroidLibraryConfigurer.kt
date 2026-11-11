package configuration

import common.DefaultBuildConfig
import common.JavaTargets
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.androidLibrary
import utils.androidTestImplementation
import utils.defaultNamespace
import utils.implementation
import utils.libs
import utils.requireLibrary
import utils.testImplementation

/**
 * Configures the AGP `com.android.library` extension and baseline Android deps.
 */
internal object AndroidLibraryConfigurer {

    fun configure(project: Project) {
        project.androidLibrary {
            namespace = project.defaultNamespace()
            compileSdk = DefaultBuildConfig.COMPILE_SDK

            defaultConfig {
                minSdk = DefaultBuildConfig.MIN_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaTargets.JAVA_VERSION
                targetCompatibility = JavaTargets.JAVA_VERSION
            }
        }

        val libs = project.libs
        project.dependencies {
            implementation(libs.requireLibrary("androidx-core-ktx"))
            testImplementation(libs.requireLibrary("junit"))
            androidTestImplementation(libs.requireLibrary("androidx-junit"))
            androidTestImplementation(libs.requireLibrary("androidx-espresso-core"))
        }
    }
}
