package com.aungthiha.topup.feature.transactionhistory.presentation.screens

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
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryNavCallbacks
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryRoutes

fun NavGraphBuilder.transactionHistoryScreen(
    modifier: Modifier,
    onNavRoute: (TransactionHistoryNavCallbacks) -> Unit,
) {
    composable<TransactionHistoryRoutes.TransactionHistoryScreen> {
        TransactionHistoryScreen(
            modifier = modifier,
            onNavigate = onNavRoute,
        )
    }
}

@Composable
fun TransactionHistoryScreen(
    onNavigate: (TransactionHistoryNavCallbacks) -> Unit,
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
            text = "History",
            style = MaterialTheme.appTypography.headlineMedium,
            color = MaterialTheme.appColorScheme.onBackground,
        )
        Button(
            onClick = { onNavigate(TransactionHistoryNavCallbacks.TransactionHistoryScreen.ToTopup) },
            modifier = Modifier.padding(top = spacing.large),
        ) {
            Text(text = "Go to Topup")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionHistoryScreenPreview() {
    AppTheme {
        TransactionHistoryScreen(onNavigate = {})
    }
}
