package com.aungthiha.topup.core.designsystem.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * SwitchLanguageKMP-style locale hook: [Locale.setDefault], copy config + [Configuration.setLocale],
 * then [android.content.Context.createConfigurationContext] and provide via [LocalContext].
 */
object LocalAppLocale {

    private var defaultLocale: Locale? = null

    @Composable
    infix fun provides(value: String?): ProvidedValue<*> {
        val context = LocalContext.current
        val configuration = LocalConfiguration.current

        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault()
        }

        val newLocale = when {
            value.isNullOrBlank() -> defaultLocale
            else -> Locale.forLanguageTag(value.replace('_', '-'))
        }

        if (newLocale != null) {
            Locale.setDefault(newLocale)
        }

        val newConfiguration = Configuration(configuration).apply { setLocale(newLocale) }
        val localizedContext = context.createConfigurationContext(newConfiguration)
        return LocalContext provides localizedContext
    }
}
