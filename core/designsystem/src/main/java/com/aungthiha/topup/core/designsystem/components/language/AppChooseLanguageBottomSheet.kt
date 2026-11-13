package com.aungthiha.topup.core.designsystem.components.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.aungthiha.topup.core.designsystem.R
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.locale.TopUpDisplayLanguage
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppChooseLanguageBottomSheet(
    selectedLanguage: TopUpDisplayLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (TopUpDisplayLanguage) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceElevated,
        contentColor = colors.contentPrimary,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spacing.large, vertical = spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            Text(
                text = stringResource(R.string.ds_language_screen_title),
                style = MaterialTheme.appTypography.titleLarge,
                color = colors.contentPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            TopUpDisplayLanguage.entries.forEach { language ->
                ChooseLanguageOptionRow(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onClick = { onLanguageSelected(language) },
                )
            }

            Spacer(modifier = Modifier.height(spacing.small))
        }
    }
}

@Composable
private fun ChooseLanguageOptionRow(
    language: TopUpDisplayLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val shapes = MaterialTheme.appShapes

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shapes.medium)
            .background(
                if (isSelected) {
                    colors.primary.copy(alpha = 0.1f)
                } else {
                    colors.surface
                },
            )
            .clickable(onClick = onClick)
            .padding(spacing.large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Image(
            painter = painterResource(language.flagDrawable),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(spacing.control)
                .clip(shapes.small),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        ) {
            Text(
                text = stringResource(language.titleRes),
                style = MaterialTheme.appTypography.titleSmall,
                color = colors.contentPrimary,
            )
            Text(
                text = stringResource(language.descriptionRes),
                style = MaterialTheme.appTypography.bodySmall,
                color = colors.contentSecondary,
            )
        }

        if (isSelected) {
            Icon(
                painter = painterResource(AppIcons.Check),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(spacing.extraLarge),
            )
        } else {
            Spacer(modifier = Modifier.width(spacing.extraLarge))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun AppChooseLanguageBottomSheetPreview() {
    AppTheme {
        AppChooseLanguageBottomSheet(
            selectedLanguage = TopUpDisplayLanguage.ENGLISH,
            onDismiss = {},
            onLanguageSelected = {},
        )
    }
}
