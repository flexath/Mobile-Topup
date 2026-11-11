package common

/**
 * Centralised plugin identifiers.
 *
 * The build script (`build-logic/convention/build.gradle.kts`) cannot import
 * these constants because the build script itself is what compiles them.
 * The plugin ids registered in the `gradlePlugin { plugins { ... } }` block
 * MUST be kept in sync with [Convention] below.
 */
internal object PluginIds {

    // ---- Third-party plugins consumed by convention plugins -------------

    const val ANDROID_APPLICATION: String = "com.android.application"
    const val ANDROID_LIBRARY: String = "com.android.library"

    const val KOTLIN_COMPOSE_COMPILER: String = "org.jetbrains.kotlin.plugin.compose"
    const val KOTLIN_SERIALIZATION: String = "org.jetbrains.kotlin.plugin.serialization"

    const val KSP: String = "com.google.devtools.ksp"
    const val ANDROIDX_ROOM: String = "androidx.room"

    const val GOOGLE_SERVICES: String = "com.google.gms.google-services"
    const val FIREBASE_CRASHLYTICS: String = "com.google.firebase.crashlytics"

    // ---- Convention plugins exported by build-logic ---------------------

    object Convention {
        const val ANDROID_APPLICATION: String = "topup.android.application"
        const val ANDROID_APPLICATION_COMPOSE: String = "topup.android.application.compose"
        const val ANDROID_LIBRARY: String = "topup.android.library"
        const val ANDROID_LIBRARY_COMPOSE: String = "topup.android.library.compose"
        const val SERIALIZATION: String = "topup.serialization"
        const val NAVIGATION: String = "topup.navigation"
        const val FIREBASE: String = "topup.firebase"
        const val ROOM: String = "topup.room"
        const val SQLCIPHER: String = "topup.sqlcipher"
        const val DATASTORE: String = "topup.datastore"
        const val KTOR: String = "topup.ktor"
        const val KOIN: String = "topup.koin"
        const val SUPABASE: String = "topup.supabase"
        const val COROUTINES: String = "topup.coroutines"
    }
}
