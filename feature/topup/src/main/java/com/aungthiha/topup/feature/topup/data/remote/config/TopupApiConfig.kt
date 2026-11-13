package com.aungthiha.topup.feature.topup.data.remote.config

/** Supabase PostgREST tables and Edge Function names for the top-up feature. */
internal object TopupApiConfig {
    const val OPERATORS_TABLE = "operators"
    const val OPERATOR_PREFIXES_TABLE = "operator_prefixes"
    const val WALLETS_TABLE = "wallets"
    const val PROCESS_RECHARGE_FUNCTION = "process-recharge"
}
