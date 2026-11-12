package com.aungthiha.topup.feature.topup.data.remote.service

import com.aungthiha.topup.feature.topup.data.remote.response.OperatorDto
import com.aungthiha.topup.feature.topup.data.remote.response.OperatorPrefixDto
import com.aungthiha.topup.feature.topup.data.remote.response.RechargeRequestDto
import com.aungthiha.topup.feature.topup.data.remote.response.WalletDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

internal class SupabaseTopupRemoteService(
    private val supabase: SupabaseClient,
) : TopupRemoteService {
    override suspend fun fetchCatalog(): List<OperatorDto> =
        supabase.from("operators")
            .select(Columns.raw(OperatorDto.UNIFIED_CATALOG_SELECT)) {
                order("sort_order", Order.ASCENDING)
            }
            .decodeList<OperatorDto>()

    override suspend fun fetchOperatorPrefixes(): List<OperatorPrefixDto> =
        supabase.from("operator_prefixes")
            .select(Columns.list("prefix", "operator_code")) {
                order("prefix", Order.ASCENDING)
            }
            .decodeList<OperatorPrefixDto>()

    override suspend fun fetchWallet(): WalletDto? =
        supabase.from("wallets")
            .select(Columns.list("balance_mmk", "currency"))
            .decodeSingleOrNull<WalletDto>()

    override suspend fun processRecharge(request: RechargeRequestDto): String =
        supabase.functions
            .invoke(
                function = PROCESS_RECHARGE_FUNCTION,
                body = request,
                headers = Headers.build { append(HttpHeaders.ContentType, ContentType.Application.Json.toString()) },
            )
            .bodyAsText()

    private companion object {
        const val PROCESS_RECHARGE_FUNCTION = "process-recharge"
    }
}
