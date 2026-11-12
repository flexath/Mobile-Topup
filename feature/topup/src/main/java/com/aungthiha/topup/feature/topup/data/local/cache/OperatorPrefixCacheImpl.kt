package com.aungthiha.topup.feature.topup.data.local.cache

import com.aungthiha.topup.feature.topup.data.local.dao.OperatorPrefixDao
import com.aungthiha.topup.feature.topup.data.mapper.toDomainOrNull
import com.aungthiha.topup.feature.topup.data.mapper.toEntity
import com.aungthiha.topup.feature.topup.domain.cache.OperatorPrefixCache
import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefix
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OperatorPrefixCacheImpl(
    private val dao: OperatorPrefixDao,
) : OperatorPrefixCache {

    override fun observePrefixes(): Flow<List<OperatorPrefix>> =
        dao.observeAll().map { rows -> rows.mapNotNull { it.toDomainOrNull() } }

    override suspend fun getPrefixes(): List<OperatorPrefix> =
        dao.getAll().mapNotNull { it.toDomainOrNull() }

    override suspend fun replacePrefixes(prefixes: List<OperatorPrefix>) {
        dao.replaceAll(prefixes.map { it.toEntity() })
    }
}
