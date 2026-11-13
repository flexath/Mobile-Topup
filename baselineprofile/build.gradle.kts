plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.aungthiha.topup.baselineprofile"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Fallback when the Baseline Profile plugin does not inject runner args (e.g. some GMD paths).
        testInstrumentationRunnerArguments["targetAppId"] = "com.aungthiha.topup"
        // Keep Macrobenchmarks from failing/skip-noise during profile generation GMD runs.
        // Android Studio gutter / benchmarkRelease tasks can override this to Macrobenchmark.
        testInstrumentationRunnerArguments["androidx.benchmark.enabledRules"] = "BaselineProfile"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    targetProjectPath = ":app"

    // Enable the benchmark to run separately from the app process.
    experimentalProperties["android.experimental.self-instrumenting"] = true

    testOptions.managedDevices.localDevices {
        create("pixel9ProApi36") {
            device = "Pixel 9 Pro"
            apiLevel = 36
            // API 36 has no local aosp image; google images work for profile
            // generation on API 33+ without root (Macrobenchmark 1.2+).
            systemImageSource = "google"
        }
    }
}

baselineProfile {
    managedDevices += "pixel9ProApi36"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
