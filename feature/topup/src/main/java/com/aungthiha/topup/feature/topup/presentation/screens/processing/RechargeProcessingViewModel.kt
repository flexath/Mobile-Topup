package com.aungthiha.topup.feature.topup.presentation.screens.processing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeRequest
import com.aungthiha.topup.feature.topup.domain.usecase.ProcessRechargeUseCase
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface RechargeProcessingUiState {
    val phoneNumber: String
    val amountMmk: Long

    data class Processing(override val phoneNumber: String, override val amountMmk: Long) : RechargeProcessingUiState

    data class Failed(
        override val phoneNumber: String,
        override val amountMmk: Long,
        val error: RechargeError,
    ) : RechargeProcessingUiState
}

sealed interface RechargeProcessingEvent {
    data class Succeeded(val transactionId: String) : RechargeProcessingEvent
}

/**
 * Owns the in-flight recharge request. The request starts once (rotation keeps this ViewModel)
 * and is cancelled when the user leaves the screen, which clears [viewModelScope].
 */
class RechargeProcessingViewModel(
    savedStateHandle: SavedStateHandle,
    private val processRecharge: ProcessRechargeUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<TopupRoutes.RechargeProcessing>()
    private val request = RechargeRequest(
        phoneNumber = route.phoneNumber,
        operatorCode = requireNotNull(OperatorCode.fromCode(route.operatorCode)),
        productId = route.productId,
        customAmountMmk = route.customAmountMmk,
        idempotencyKey = route.idempotencyKey,
    )

    private val _uiState = MutableStateFlow<RechargeProcessingUiState>(
        RechargeProcessingUiState.Processing(route.phoneNumber, route.displayAmountMmk),
    )
    val uiState: StateFlow<RechargeProcessingUiState> = _uiState.asStateFlow()

    private val events = Channel<RechargeProcessingEvent>(Channel.BUFFERED)
    val processingEvents: Flow<RechargeProcessingEvent> = events.receiveAsFlow()

    private var submission: Job? = null

    init {
        submit()
    }

    /** Retries with the same idempotency key, so an already-processed request is not charged again. */
    fun onRetry() {
        if (_uiState.value is RechargeProcessingUiState.Failed) submit()
    }

    private fun submit() {
        if (submission?.isActive == true) return
        _uiState.value = RechargeProcessingUiState.Processing(route.phoneNumber, route.displayAmountMmk)
        submission = viewModelScope.launch {
            when (val result = processRecharge(request)) {
                is DataResult.Success -> events.send(RechargeProcessingEvent.Succeeded(result.data.transaction.id))
                is DataResult.Error -> _uiState.value = RechargeProcessingUiState.Failed(
                    phoneNumber = route.phoneNumber,
                    amountMmk = route.displayAmountMmk,
                    error = result.error,
                )
                else -> Unit
            }
        }
    }
}
