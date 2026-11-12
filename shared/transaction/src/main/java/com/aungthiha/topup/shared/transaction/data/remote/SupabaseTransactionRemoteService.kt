package com.aungthiha.topup.shared.transaction.data.remote

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

/** Reads the caller's own history through `public.list_my_transactions` (RLS-scoped). */
internal class SupabaseTransactionRemoteService(
    private val supabase: SupabaseClient,
) : TransactionRemoteService {
    @OptIn(ExperimentalTime::class)
    override suspend fun fetch(query: TransactionQuery, limit: Int, offset: Int): List<TransactionDto> {
        val parameters = buildJsonObject {
            put("p_search", query.normalizedSearch.ifEmpty { null }?.let(::JsonPrimitive) ?: JsonNull)
            put(
                "p_operator_codes",
                if (query.operatorCodes.isEmpty()) {
                    JsonNull
                } else {
                    buildJsonArray { query.operatorCodes.sorted().forEach { add(JsonPrimitive(it)) } }
                },
            )
            put(
                "p_created_from",
                query.dateRange?.let { JsonPrimitive(Instant.fromEpochMilliseconds(it.startMillis).toString()) } ?: JsonNull,
            )
            put(
                "p_created_to",
                query.dateRange?.let { JsonPrimitive(Instant.fromEpochMilliseconds(it.endMillisExclusive).toString()) } ?: JsonNull,
            )
            put("p_oldest_first", JsonPrimitive(query.sortOrder == TransactionSortOrder.OLDEST_FIRST))
            put("p_limit", JsonPrimitive(limit))
            put("p_offset", JsonPrimitive(offset))
        }
        return supabase.postgrest
            .rpc(function = "list_my_transactions", parameters = parameters) {
                select(Columns.list(TransactionDto.COLUMNS))
            }
            .decodeList<TransactionDto>()
    }
}
