plugins {
    alias(libs.plugins.topup.android.application.compose)
    alias(libs.plugins.topup.serialization)
    alias(libs.plugins.topup.navigation)
    alias(libs.plugins.topup.firebase)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.feature.topup)
    implementation(projects.feature.transactionhistory)
}
