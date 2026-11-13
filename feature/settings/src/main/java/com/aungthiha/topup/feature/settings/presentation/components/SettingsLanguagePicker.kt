package com.aungthiha.topup.feature.settings.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aungthiha.topup.core.datastore.LanguageManager
import com.aungthiha.topup.core.datastore.model.AppLanguage
import com.aungthiha.topup.core.designsystem.components.language.AppChooseLanguageBottomSheet
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.locale.TopUpDisplayLanguage
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
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
fun SettingsLanguagePicker(
    modifier: Modifier = Modifier,
    languageManager: LanguageManager = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColorScheme
    val appLanguage by languageManager
        .getAppLanguage()
        .collectAsStateWithLifecycle(initialValue = AppLanguage.ENGLISH)
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val displayLanguage = appLanguage.toDisplayLanguage()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = { showSheet = true })
            .padding(vertical = spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Image(
            painter = painterResource(displayLanguage.flagDrawable),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(spacing.control)
                .clip(MaterialTheme.appShapes.small),
        )
        Text(
            text = stringResource(displayLanguage.titleRes),
            style = MaterialTheme.appTypography.bodyLarge,
            color = colors.contentPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(AppIcons.ChevronRight),
            contentDescription = stringResource(
                com.aungthiha.topup.core.designsystem.R.string.ds_language_picker_content_description,
            ),
            tint = colors.contentSecondary,
            modifier = Modifier.size(spacing.extraLarge),
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
