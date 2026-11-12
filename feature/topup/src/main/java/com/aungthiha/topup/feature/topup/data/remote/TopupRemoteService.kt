package com.aungthiha.topup.feature.topup.data.remote

/**
 * Remote gateway for top-up catalog, prefixes, wallet, and recharge.
 * Implementation: [SupabaseTopupRemoteService].
 */
internal interface TopupRemoteService {
    /** The whole catalog (all operators + their active products) in one request. */
    suspend fun fetchCatalog(): List<OperatorDto>

    /** Offline detection prefix list mirrored from `public.operator_prefixes`. */
    suspend fun fetchOperatorPrefixes(): List<OperatorPrefixDto>

    /** RLS returns only the caller's wallet; no user id is sent. */
    suspend fun fetchWallet(): WalletDto?

    /** Throws `RestException` for non-2xx responses; its `error` holds the JSON error body. */
    suspend fun processRecharge(request: RechargeRequestDto): String
}
