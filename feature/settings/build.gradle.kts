plugins {
    alias(libs.plugins.topup.android.library.compose)
    alias(libs.plugins.topup.serialization)
    alias(libs.plugins.topup.navigation)
    alias(libs.plugins.topup.koin)
    alias(libs.plugins.topup.coroutines)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.datastore)
    implementation(projects.shared.auth)

    implementation(libs.androidx.lifecycle.runtime.compose)
}
