package com.aungthiha.topup.core.designsystem.locale

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aungthiha.topup.core.designsystem.R
import com.aungthiha.topup.core.designsystem.icons.AppIcons

/** Display languages for design-system UI; tags match [com.aungthiha.topup.core.datastore.model.AppLanguage]. */
enum class TopUpDisplayLanguage(
    val languageTag: String,
    @DrawableRes val flagDrawable: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
) {
    ENGLISH(
        languageTag = "en",
        flagDrawable = AppIcons.UkFlag,
        titleRes = R.string.ds_language_english,
        descriptionRes = R.string.ds_language_english_description,
    ),
    MYANMAR(
        languageTag = "my",
        flagDrawable = AppIcons.MyanmarFlag,
        titleRes = R.string.ds_language_myanmar,
        descriptionRes = R.string.ds_language_myanmar_description,
    ),
    ;

    companion object {
        fun fromLanguageTag(tag: String): TopUpDisplayLanguage =
            entries.firstOrNull { it.languageTag == tag.lowercase().substringBefore("-") }
                ?: ENGLISH
    }
}
