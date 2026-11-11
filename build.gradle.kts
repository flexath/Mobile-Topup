// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Keep third-party plugins on a single classpath (versions from the catalog).
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false

    // Convention plugins from included build `build-logic`.
    alias(libs.plugins.topup.android.application) apply false
    alias(libs.plugins.topup.android.application.compose) apply false
    alias(libs.plugins.topup.android.library) apply false
    alias(libs.plugins.topup.android.library.compose) apply false
    alias(libs.plugins.topup.serialization) apply false
    alias(libs.plugins.topup.navigation) apply false
    alias(libs.plugins.topup.firebase) apply false
    alias(libs.plugins.topup.room) apply false
    alias(libs.plugins.topup.sqlcipher) apply false
    alias(libs.plugins.topup.datastore) apply false
    alias(libs.plugins.topup.ktor) apply false
    alias(libs.plugins.topup.koin) apply false
    alias(libs.plugins.topup.supabase) apply false
    alias(libs.plugins.topup.coroutines) apply false
}
