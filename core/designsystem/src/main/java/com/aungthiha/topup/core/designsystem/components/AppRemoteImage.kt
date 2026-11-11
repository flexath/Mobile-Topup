package com.aungthiha.topup.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

/**
 * Operator logo loaded from Supabase Storage. Falls back to the operator's initial while loading,
 * offline without cache, or on error.
 *
 * Set [allowHardwareBitmap] to false when the logo is part of content captured into an image.
 */
@Composable
fun AppOperatorLogo(
    url: String?,
    operatorName: String,
    modifier: Modifier = Modifier,
    size: Dp = MaterialTheme.appSpacing.ample,
    allowHardwareBitmap: Boolean = true,
) {
    val colors = MaterialTheme.appColorScheme
    val shape = MaterialTheme.appShapes.medium
    var showFallback by remember(url) { mutableStateOf(true) }
    val context = LocalContext.current
    val request = remember(url, allowHardwareBitmap) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .allowHardware(allowHardwareBitmap)
            .build()
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(colors.surfaceVariant)
            .border(MaterialTheme.appSpacing.hairline, colors.outlineVariant, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (showFallback) {
            Text(
                text = operatorName.take(1).uppercase(),
                style = MaterialTheme.appTypography.titleMedium,
                color = colors.contentSecondary,
            )
        }
        if (url != null) {
            AsyncImage(
                model = request,
                contentDescription = operatorName,
                contentScale = ContentScale.Fit,
                onSuccess = { showFallback = false },
                onError = { showFallback = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.appSpacing.extraSmall),
            )
        }
    }
}
