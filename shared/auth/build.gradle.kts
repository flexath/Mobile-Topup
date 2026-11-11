plugins {
    alias(libs.plugins.topup.android.library)
    alias(libs.plugins.topup.ktor)
    alias(libs.plugins.topup.supabase)
    alias(libs.plugins.topup.koin)
    alias(libs.plugins.topup.coroutines)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.network)
}
