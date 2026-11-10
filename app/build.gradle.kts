import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use(::load)
    }
}

fun requireLocalProperty(name: String): String =
    localProperties.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: error(
            "Missing `$name` in local.properties. " +
                "Required for release signing: RELEASE_KEYSTORE, RELEASE_KEYSTORE_PASSWORD, " +
                "RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD.",
        )

android {
    namespace = "com.aungthiha.topup"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.aungthiha.topup"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = requireLocalProperty("RELEASE_KEYSTORE")
            val keystoreFile = rootProject.file(keystorePath)
            check(keystoreFile.isFile) {
                "Release keystore not found at: ${keystoreFile.absolutePath}"
            }
            storeFile = keystoreFile
            storePassword = requireLocalProperty("RELEASE_KEYSTORE_PASSWORD")
            keyAlias = requireLocalProperty("RELEASE_KEY_ALIAS")
            keyPassword = requireLocalProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}