package com.aungthiha.topup

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.core.designsystem.locale.ProvideAppLocale
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.navigation.TopUpAppShell
import com.aungthiha.topup.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Keep the system splash until the stored session, theme, and language are known, so the
        // first frame is already the right destination in the right theme and locale.
        splashScreen.setKeepOnScreenCondition { !viewModel.uiState.value.isReady }
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (state.appearanceMode) {
                AppearanceMode.LIGHT -> false
                AppearanceMode.DARK -> true
                AppearanceMode.SYSTEM, null -> isSystemInDarkTheme()
            }
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose { }
            }
            val languageTag = state.appLanguage?.languageTag ?: "en"
            ProvideAppLocale(languageTag = languageTag) {
                AppTheme(darkTheme = darkTheme) {
                    TopUpAppShell(
                        authState = state.authState,
                        onSessionExpired = viewModel::onSessionExpired,
                    )
                }
            }
        }
    }
}
