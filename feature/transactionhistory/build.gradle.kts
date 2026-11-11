plugins {
    alias(libs.plugins.topup.android.library.compose)
    alias(libs.plugins.topup.serialization)
    alias(libs.plugins.topup.navigation)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
}
