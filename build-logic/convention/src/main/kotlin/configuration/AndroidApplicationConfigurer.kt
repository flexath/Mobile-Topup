package configuration

import common.DefaultBuildConfig
import common.JavaTargets
import org.gradle.api.Project
import utils.androidApplication
import utils.requireReleaseSigningCredentials

/**
 * Configures the AGP `com.android.application` extension for `:app`.
 *
 * Keeps stock AGP `debug` / `release` build types. Release signing is
 * required via `local.properties` (fail-hard at configuration time).
 */
internal object AndroidApplicationConfigurer {

    fun configure(project: Project) {
        val credentials = project.requireReleaseSigningCredentials()
        val keystoreFile = credentials.resolveStoreFile(project.rootProject)
        check(keystoreFile.isFile) {
            "Release keystore not found at: ${keystoreFile.absolutePath}"
        }

        project.androidApplication {
            namespace = DefaultBuildConfig.APPLICATION_ID
            compileSdk = DefaultBuildConfig.COMPILE_SDK

            defaultConfig {
                applicationId = DefaultBuildConfig.APPLICATION_ID
                minSdk = DefaultBuildConfig.MIN_SDK
                targetSdk = DefaultBuildConfig.TARGET_SDK
                versionCode = DefaultBuildConfig.VERSION_CODE
                versionName = DefaultBuildConfig.VERSION_NAME
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            signingConfigs {
                create("release") {
                    storeFile = keystoreFile
                    storePassword = credentials.storePassword
                    keyAlias = credentials.keyAlias
                    keyPassword = credentials.keyPassword
                }
            }

            buildTypes {
                release {
                    signingConfig = signingConfigs.getByName("release")
                    optimization {
                        enable = true
                        packageScope.set(DefaultBuildConfig.RELEASE_OPTIMIZATION_PACKAGE_SCOPE)
                    }
                }
            }

            compileOptions {
                sourceCompatibility = JavaTargets.JAVA_VERSION
                targetCompatibility = JavaTargets.JAVA_VERSION
            }
        }
    }
}
