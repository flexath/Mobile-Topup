plugins {
    alias(libs.plugins.topup.android.library.compose)
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
}
