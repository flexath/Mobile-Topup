plugins {
    alias(libs.plugins.topup.android.library.compose)
    alias(libs.plugins.topup.serialization)
    alias(libs.plugins.topup.navigation)
    alias(libs.plugins.topup.room)
    alias(libs.plugins.topup.ktor)
    alias(libs.plugins.topup.supabase)
    alias(libs.plugins.topup.koin)
    alias(libs.plugins.topup.coroutines)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(projects.shared.transaction)

    implementation(libs.androidx.lifecycle.runtime.compose)

    androidTestImplementation(libs.kotlinx.coroutines.test)
}
