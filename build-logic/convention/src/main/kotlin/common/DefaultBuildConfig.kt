package common

/**
 * Single source of truth for project-wide static build constants.
 *
 * TopUp keeps stock AGP `debug` / `release` build types only — no environment
 * dimension or product flavors.
 */
object DefaultBuildConfig {

    // ---- Identity ---------------------------------------------------------

    const val APPLICATION_ID: String = "com.aungthiha.topup"

    /** Used as a prefix for module-derived Android namespaces. */
    const val NAMESPACE_PREFIX: String = APPLICATION_ID

    // ---- Android SDK levels ----------------------------------------------

    const val COMPILE_SDK: Int = 37
    const val TARGET_SDK: Int = 37
    const val MIN_SDK: Int = 24

    // ---- local.properties keys (secrets / per-developer config) ----------

    /**
     * Required Play Store / upload signing for `:app` `release` builds.
     * All four keys must be present or configuration fails.
     * Path may be absolute or relative to the repository root.
     */
    const val RELEASE_KEYSTORE_PATH_KEY: String = "RELEASE_KEYSTORE"
    const val RELEASE_KEYSTORE_PASSWORD_KEY: String = "RELEASE_KEYSTORE_PASSWORD"
    const val RELEASE_KEY_ALIAS_KEY: String = "RELEASE_KEY_ALIAS"
    const val RELEASE_KEY_PASSWORD_KEY: String = "RELEASE_KEY_PASSWORD"

    /**
     * Required Supabase client config for `:app` BuildConfig fields.
     * Both keys must be present or configuration fails.
     */
    const val SUPABASE_URL_KEY: String = "SUPABASE_URL"
    const val SUPABASE_ANON_KEY_KEY: String = "SUPABASE_ANON_KEY"

    // ---- Versioning -------------------------------------------------------

    private const val VERSION_MAJOR: Int = 1
    private const val VERSION_MINOR: Int = 0
    private const val VERSION_PATCH: Int = 0

    const val VERSION_CODE: Int = 1
    const val VERSION_NAME: String = "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH"

    // ---- Release optimization package scope ------------------------------

    val RELEASE_OPTIMIZATION_PACKAGE_SCOPE: Set<String> = setOf(
        "androidx.**",
        "kotlin.**",
        "kotlinx.**",
    )
}
