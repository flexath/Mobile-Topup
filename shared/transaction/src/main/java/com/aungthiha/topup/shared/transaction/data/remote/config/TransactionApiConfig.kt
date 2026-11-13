package com.aungthiha.topup.shared.transaction.data.remote.config

/** Supabase RPC endpoint and parameter names for transaction history. */
internal object TransactionApiConfig {
    const val LIST_MY_TRANSACTIONS_RPC = "list_my_transactions"

    const val PARAM_SEARCH = "p_search"
    const val PARAM_OPERATOR_CODES = "p_operator_codes"
    const val PARAM_CREATED_FROM = "p_created_from"
    const val PARAM_CREATED_TO = "p_created_to"
    const val PARAM_OLDEST_FIRST = "p_oldest_first"
    const val PARAM_LIMIT = "p_limit"
    const val PARAM_OFFSET = "p_offset"
}
