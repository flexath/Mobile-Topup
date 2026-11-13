package com.aungthiha.topup.feature.topup.domain.cache

import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefix
import kotlinx.coroutines.flow.Flow

interface OperatorPrefixCache {
    fun observePrefixes(): Flow<List<OperatorPrefix>>

    suspend fun getPrefixes(): List<OperatorPrefix>

    suspend fun replacePrefixes(prefixes: List<OperatorPrefix>)
}
