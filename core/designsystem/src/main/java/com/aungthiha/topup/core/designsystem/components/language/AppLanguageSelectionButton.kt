package com.aungthiha.topup.core.designsystem.components.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.aungthiha.topup.core.designsystem.R
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.locale.TopUpDisplayLanguage
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing

@Composable
fun AppLanguageSelectionButton(
    currentLanguage: TopUpDisplayLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val shapes = MaterialTheme.appShapes
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .clip(shapes.small)
            .border(width = spacing.hairline, color = colors.outline, shape = shapes.small)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = spacing.relaxed, vertical = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.tight),
    ) {
        Box(
            modifier = Modifier
                .width(spacing.grand)
                .height(spacing.extraLarge)
                .clip(shapes.extraSmall)
                .border(
                    width = spacing.hairline,
                    color = colors.outline.copy(alpha = 0.45f),
                    shape = shapes.extraSmall,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(currentLanguage.flagDrawable),
                contentDescription = stringResource(R.string.ds_language_screen_title),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .matchParentSize()
                    .padding(spacing.extraExtraSmall),
            )
        }
        Icon(
            painter = painterResource(AppIcons.ChevronDown),
            contentDescription = null,
            tint = colors.contentSecondary,
            modifier = Modifier.size(spacing.generous),
        )
    }
}
