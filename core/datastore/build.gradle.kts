plugins {
    alias(libs.plugins.topup.android.library)
    alias(libs.plugins.topup.datastore)
    alias(libs.plugins.topup.coroutines)
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
}
