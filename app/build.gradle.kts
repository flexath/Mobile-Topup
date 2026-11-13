plugins {
    alias(libs.plugins.topup.android.application.compose)
    alias(libs.plugins.topup.serialization)
    alias(libs.plugins.topup.navigation)
    alias(libs.plugins.topup.firebase)
    alias(libs.plugins.topup.room)
    alias(libs.plugins.topup.sqlcipher)
    alias(libs.plugins.topup.koin)
    alias(libs.plugins.topup.coroutines)
    alias(libs.plugins.androidx.baselineprofile)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(projects.shared.transaction)
    implementation(projects.feature.auth)
    implementation(projects.feature.topup)
    implementation(projects.feature.transactionhistory)
    implementation(projects.feature.settings)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.profileinstaller)

    baselineProfile(projects.baselineprofile)
}
