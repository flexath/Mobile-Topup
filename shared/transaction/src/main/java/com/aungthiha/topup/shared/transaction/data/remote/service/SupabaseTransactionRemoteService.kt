package com.aungthiha.topup.shared.transaction.data.remote.service

import com.aungthiha.topup.shared.transaction.data.remote.config.TransactionApiConfig
import com.aungthiha.topup.shared.transaction.data.remote.response.TransactionDto
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.model.TransactionSortOrder
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Reads the caller's own history through [TransactionApiConfig.LIST_MY_TRANSACTIONS_RPC] (RLS-scoped). */
internal class SupabaseTransactionRemoteService(
    private val supabase: SupabaseClient,
) : TransactionRemoteService {
    @OptIn(ExperimentalTime::class)
    override suspend fun fetch(query: TransactionQuery, limit: Int, offset: Int): List<TransactionDto> {
        val parameters = buildJsonObject {
            put(
                TransactionApiConfig.PARAM_SEARCH,
                query.normalizedSearch.ifEmpty { null }?.let(::JsonPrimitive) ?: JsonNull,
            )
            put(
                TransactionApiConfig.PARAM_OPERATOR_CODES,
                if (query.operatorCodes.isEmpty()) {
                    JsonNull
                } else {
                    buildJsonArray { query.operatorCodes.sorted().forEach { add(JsonPrimitive(it)) } }
                },
            )
            put(
                TransactionApiConfig.PARAM_CREATED_FROM,
                query.dateRange?.let { JsonPrimitive(Instant.fromEpochMilliseconds(it.startMillis).toString()) } ?: JsonNull,
            )
            put(
                TransactionApiConfig.PARAM_CREATED_TO,
                query.dateRange?.let { JsonPrimitive(Instant.fromEpochMilliseconds(it.endMillisExclusive).toString()) } ?: JsonNull,
            )
            put(
                TransactionApiConfig.PARAM_OLDEST_FIRST,
                JsonPrimitive(query.sortOrder == TransactionSortOrder.OLDEST_FIRST),
            )
            put(TransactionApiConfig.PARAM_LIMIT, JsonPrimitive(limit))
            put(TransactionApiConfig.PARAM_OFFSET, JsonPrimitive(offset))
        }
        return supabase.postgrest
            .rpc(function = TransactionApiConfig.LIST_MY_TRANSACTIONS_RPC, parameters = parameters) {
                select(Columns.list(TransactionDto.COLUMNS))
            }
            .decodeList<TransactionDto>()
    }
}
