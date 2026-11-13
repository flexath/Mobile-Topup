package com.aungthiha.topup.feature.auth.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aungthiha.topup.core.datastore.LanguageManager
import com.aungthiha.topup.core.datastore.model.AppLanguage
import com.aungthiha.topup.core.designsystem.components.language.AppChooseLanguageBottomSheet
import com.aungthiha.topup.core.designsystem.components.language.AppLanguageSelectionButton
import com.aungthiha.topup.core.designsystem.locale.TopUpDisplayLanguage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private fun AppLanguage.toDisplayLanguage(): TopUpDisplayLanguage = when (this) {
    AppLanguage.ENGLISH -> TopUpDisplayLanguage.ENGLISH
    AppLanguage.MYANMAR -> TopUpDisplayLanguage.MYANMAR
}

private fun TopUpDisplayLanguage.toAppLanguage(): AppLanguage = when (this) {
    TopUpDisplayLanguage.ENGLISH -> AppLanguage.ENGLISH
    TopUpDisplayLanguage.MYANMAR -> AppLanguage.MYANMAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthLanguagePicker(
    modifier: Modifier = Modifier,
    languageManager: LanguageManager = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val appLanguage by languageManager
        .getAppLanguage()
        .collectAsStateWithLifecycle(initialValue = AppLanguage.ENGLISH)
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val displayLanguage = appLanguage.toDisplayLanguage()

    Box(modifier = modifier) {
        AppLanguageSelectionButton(
            currentLanguage = displayLanguage,
            onClick = { showSheet = true },
        )
    }

    if (showSheet) {
        AppChooseLanguageBottomSheet(
            selectedLanguage = displayLanguage,
            onDismiss = { showSheet = false },
            onLanguageSelected = { chosen ->
                scope.launch {
                    languageManager.setAppLanguage(chosen.toAppLanguage())
                }
                showSheet = false
            },
        )
    }
}
