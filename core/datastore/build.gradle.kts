plugins {
    alias(libs.plugins.topup.android.library)
    alias(libs.plugins.topup.datastore)
    alias(libs.plugins.topup.coroutines)
    alias(libs.plugins.topup.koin)
}

dependencies {
    implementation(projects.core.common)
}
