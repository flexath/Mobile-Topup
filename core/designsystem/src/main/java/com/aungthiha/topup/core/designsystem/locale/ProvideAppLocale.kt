package com.aungthiha.topup.core.designsystem.locale

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Applies [languageTag] for Android string resources without recreating the activity.
 *
 * [LocalContext] is replaced with [android.content.Context.createConfigurationContext]; that
 * context is not an Activity, so [LocalActivityResultRegistryOwner] is re-provided from the host.
 *
 * Avoid [androidx.appcompat.app.AppCompatDelegate.setApplicationLocales] here — it recreates
 * the activity. Persistence stays in DataStore.
 */
@SuppressLint("LocalContextConfigurationRead")
@Composable
fun ProvideAppLocale(
    languageTag: String,
    content: @Composable () -> Unit,
) {
    val activityResultRegistryOwner = requireNotNull(LocalActivityResultRegistryOwner.current) {
        "ProvideAppLocale must run under ComponentActivity setContent (ActivityResultRegistryOwner missing)"
    }

    CompositionLocalProvider(LocalAppLocale provides languageTag) {
        val localizedContext = LocalContext.current
        CompositionLocalProvider(
            LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
            LocalConfiguration provides localizedContext.resources.configuration,
        ) {
            content()
        }
    }
}
