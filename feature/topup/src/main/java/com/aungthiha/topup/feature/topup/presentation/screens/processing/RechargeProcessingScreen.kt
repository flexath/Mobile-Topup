package com.aungthiha.topup.feature.topup.presentation.screens.processing

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.components.AppSecondaryButton
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupNavCallbacks
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import org.koin.compose.viewmodel.koinViewModel

private const val SPINNER_ROTATION_MILLIS = 1_100
private const val PULSE_MILLIS = 900
private const val PULSE_MIN_SCALE = 0.92f
private const val RING_TINT_ALPHA = 0.12f
private const val SWEEP_DEGREES = 100f

fun NavGraphBuilder.rechargeProcessingScreen(
    modifier: Modifier,
    onNavRoute: (TopupNavCallbacks) -> Unit,
) {
    composable<TopupRoutes.RechargeProcessing> {
        val viewModel: RechargeProcessingViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(viewModel) {
            viewModel.processingEvents.collect { event ->
                when (event) {
                    is RechargeProcessingEvent.Succeeded ->
                        onNavRoute(TopupNavCallbacks.ProcessingScreen.ToSuccess(event.transactionId))
                }
            }
        }

        RechargeProcessingScreen(
            state = state,
            onRetry = viewModel::onRetry,
            onBackToTopup = { onNavRoute(TopupNavCallbacks.ProcessingScreen.BackToTopup) },
            onSessionExpired = { onNavRoute(TopupNavCallbacks.ProcessingScreen.SessionExpired) },
            modifier = modifier,
        )
    }
}

@Composable
fun RechargeProcessingScreen(
    state: RechargeProcessingUiState,
    onRetry: () -> Unit,
    onBackToTopup: () -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith fadeOut() },
            label = "processingState",
        ) { current ->
            Column(
                modifier = Modifier
                    .widthIn(max = spacing.messageMaxWidth + spacing.huge)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.section),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.large),
            ) {
                when (current) {
                    is RechargeProcessingUiState.Processing -> ProcessingContent(current)
                    is RechargeProcessingUiState.Failed -> FailureContent(
                        state = current,
                        onRetry = onRetry,
                        onBackToTopup = onBackToTopup,
                        onSessionExpired = onSessionExpired,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessingContent(state: RechargeProcessingUiState.Processing) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val transition = rememberInfiniteTransition(label = "processing")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(SPINNER_ROTATION_MILLIS, easing = LinearEasing)),
        label = "processingRotation",
    )
    val pulse by transition.animateFloat(
        initialValue = PULSE_MIN_SCALE,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), repeatMode = RepeatMode.Reverse),
        label = "processingPulse",
    )

    Box(modifier = Modifier.size(spacing.illustration * 0.6f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.06f
            drawCircle(color = colors.primary.copy(alpha = RING_TINT_ALPHA), style = Stroke(width = stroke))
            drawArc(
                color = colors.primary,
                startAngle = rotation,
                sweepAngle = SWEEP_DEGREES,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Box(
            modifier = Modifier
                .size(spacing.extraHuge)
                .scale(pulse)
                .clip(CircleShape)
                .background(colors.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(AppIcons.Smartphone),
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(spacing.extended),
            )
        }
    }
    Text(
        text = stringResource(R.string.topup_processing_title),
        style = MaterialTheme.appTypography.headlineSmall,
        color = colors.contentPrimary,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(R.string.topup_processing_message, state.phoneNumber, formatKyat(state.amountMmk)),
        style = MaterialTheme.appTypography.bodyLarge,
        color = colors.contentSecondary,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(R.string.topup_processing_note),
        style = MaterialTheme.appTypography.bodySmall,
        color = colors.contentSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun FailureContent(
    state: RechargeProcessingUiState.Failed,
    onRetry: () -> Unit,
    onBackToTopup: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val presentation = state.error.presentation()
    val message = when (val error = state.error) {
        is RechargeError.InsufficientBalance -> if (error.balanceMmk != null) {
            stringResource(
                R.string.topup_error_insufficient_message,
                formatKyat(error.balanceMmk),
                formatKyat(error.amountMmk ?: state.amountMmk),
            )
        } else {
            stringResource(R.string.topup_error_insufficient_message_generic)
        }
        else -> stringResource(presentation.message)
    }

    Box(
        modifier = Modifier
            .size(spacing.extraHuge + spacing.extraLarge)
            .clip(CircleShape)
            .background(presentation.tint().copy(alpha = RING_TINT_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(presentation.icon),
            contentDescription = null,
            tint = presentation.tint(),
            modifier = Modifier.size(spacing.ample),
        )
    }
    Text(
        text = stringResource(presentation.title),
        style = MaterialTheme.appTypography.headlineSmall,
        color = colors.contentPrimary,
        textAlign = TextAlign.Center,
    )
    Text(
        text = message,
        style = MaterialTheme.appTypography.bodyLarge,
        color = colors.contentSecondary,
        textAlign = TextAlign.Center,
    )
    when (presentation.primaryAction) {
        FailureAction.RETRY -> {
            AppPrimaryButton(text = stringResource(R.string.topup_try_again), onClick = onRetry)
            AppSecondaryButton(text = stringResource(R.string.topup_go_back), onClick = onBackToTopup)
        }
        FailureAction.CHOOSE_ANOTHER -> AppPrimaryButton(
            text = stringResource(R.string.topup_error_choose_another),
            onClick = onBackToTopup,
        )
        FailureAction.SIGN_IN -> AppPrimaryButton(
            text = stringResource(R.string.topup_error_session_action),
            onClick = onSessionExpired,
        )
    }
}

private enum class FailureAction { RETRY, CHOOSE_ANOTHER, SIGN_IN }

private enum class FailureTone { WARNING, ERROR }

private data class FailurePresentation(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @DrawableRes val icon: Int,
    val tone: FailureTone,
    val primaryAction: FailureAction,
)

@Composable
private fun FailurePresentation.tint() = when (tone) {
    FailureTone.WARNING -> MaterialTheme.appColorScheme.warning
    FailureTone.ERROR -> MaterialTheme.appColorScheme.error
}

private fun RechargeError.presentation(): FailurePresentation = when (this) {
    is RechargeError.InsufficientBalance -> FailurePresentation(
        title = R.string.topup_error_insufficient_title,
        message = R.string.topup_error_insufficient_message_generic,
        icon = AppIcons.Wallet,
        tone = FailureTone.WARNING,
        primaryAction = FailureAction.CHOOSE_ANOTHER,
    )
    RechargeError.SessionExpired -> FailurePresentation(
        title = R.string.topup_error_session_title,
        message = R.string.topup_error_session_message,
        icon = AppIcons.Lock,
        tone = FailureTone.WARNING,
        primaryAction = FailureAction.SIGN_IN,
    )
    RechargeError.ProductUnavailable -> FailurePresentation(
        title = R.string.topup_error_product_title,
        message = R.string.topup_error_product_message,
        icon = AppIcons.Receipt,
        tone = FailureTone.WARNING,
        primaryAction = FailureAction.CHOOSE_ANOTHER,
    )
    RechargeError.InvalidPhoneNumber, RechargeError.UnsupportedOperator -> FailurePresentation(
        title = R.string.topup_error_number_title,
        message = R.string.topup_error_number_message,
        icon = AppIcons.Smartphone,
        tone = FailureTone.ERROR,
        primaryAction = FailureAction.CHOOSE_ANOTHER,
    )
    RechargeError.InvalidAmount -> FailurePresentation(
        title = R.string.topup_error_amount_title,
        message = R.string.topup_error_amount_message,
        icon = AppIcons.Error,
        tone = FailureTone.ERROR,
        primaryAction = FailureAction.CHOOSE_ANOTHER,
    )
    is RechargeError.Network -> FailurePresentation(
        title = R.string.topup_error_network_title,
        message = R.string.topup_error_network_message,
        icon = AppIcons.CloudOff,
        tone = FailureTone.WARNING,
        primaryAction = if (error == NetworkError.UNAUTHORIZED) FailureAction.SIGN_IN else FailureAction.RETRY,
    )
    RechargeError.DuplicateRequest, RechargeError.WalletNotFound, RechargeError.Unknown -> FailurePresentation(
        title = R.string.topup_error_generic_title,
        message = R.string.topup_error_generic_message,
        icon = AppIcons.Error,
        tone = FailureTone.ERROR,
        primaryAction = FailureAction.RETRY,
    )
}

@Preview(showBackground = true)
@Composable
private fun ProcessingPreview() {
    AppTheme {
        RechargeProcessingScreen(
            state = RechargeProcessingUiState.Processing("09420000001", 20_000),
            onRetry = {},
            onBackToTopup = {},
            onSessionExpired = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsufficientBalancePreview() {
    AppTheme {
        RechargeProcessingScreen(
            state = RechargeProcessingUiState.Failed(
                phoneNumber = "09420000001",
                amountMmk = 30_000,
                error = RechargeError.InsufficientBalance(balanceMmk = 19_000, amountMmk = 30_000),
            ),
            onRetry = {},
            onBackToTopup = {},
            onSessionExpired = {},
        )
    }
}
