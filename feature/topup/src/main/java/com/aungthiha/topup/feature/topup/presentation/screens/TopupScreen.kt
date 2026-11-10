package com.aungthiha.topup.feature.topup.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupNavCallbacks
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes

fun NavGraphBuilder.topupScreen(
    modifier: Modifier,
    onNavRoute: (TopupNavCallbacks) -> Unit,
) {
    composable<TopupRoutes.TopupScreen> {
        TopupScreen(
            modifier = modifier,
            onNavigate = onNavRoute,
        )
    }
}

@Composable
fun TopupScreen(
    onNavigate: (TopupNavCallbacks) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.screenHorizontal),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Topup",
            style = MaterialTheme.appTypography.headlineMedium,
            color = MaterialTheme.appColorScheme.onBackground,
        )
        Button(
            onClick = { onNavigate(TopupNavCallbacks.TopupScreen.ToHistory) },
            modifier = Modifier.padding(top = spacing.large),
        ) {
            Text(text = "Go to History")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopupScreenPreview() {
    AppTheme {
        TopupScreen(onNavigate = {})
    }
}
