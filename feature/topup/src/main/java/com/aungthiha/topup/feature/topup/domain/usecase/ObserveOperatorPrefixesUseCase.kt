package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefix
import com.aungthiha.topup.feature.topup.domain.repository.OperatorPrefixRepository
import kotlinx.coroutines.flow.Flow

class ObserveOperatorPrefixesUseCase(private val repository: OperatorPrefixRepository) {
    operator fun invoke(): Flow<List<OperatorPrefix>> = repository.observePrefixes()
}
