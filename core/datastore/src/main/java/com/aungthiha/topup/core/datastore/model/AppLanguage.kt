package com.aungthiha.topup.core.datastore.model

/**
 * In-app display languages backed by Android resources (`values` / `values-my`).
 */
enum class AppLanguage(val languageTag: String) {
    ENGLISH("en"),
    MYANMAR("my"),
    ;

    companion object {
        fun fromStoredName(name: String): AppLanguage =
            runCatching { valueOf(name) }.getOrElse { fromLanguageTag(name) }

        fun fromLanguageTag(tag: String): AppLanguage =
            when (tag.lowercase().substringBefore("-")) {
                "my" -> MYANMAR
                else -> ENGLISH
            }
    }
}
