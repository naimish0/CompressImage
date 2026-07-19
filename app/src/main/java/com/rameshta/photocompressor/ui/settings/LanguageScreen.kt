package com.rameshta.photocompressor.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.rameshta.photocompressor.BuildConfig
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.theme.AppSpacing
import java.util.Locale

internal data class AppLanguageOption(
    val languageTag: String?,
    @param:StringRes val displayNameRes: Int?,
)

private val LANGUAGE_NAME_RESOURCES = mapOf(
    "en" to R.string.language_name_en,
    "hi" to R.string.language_name_hi,
    "gu" to R.string.language_name_gu,
    "mr" to R.string.language_name_mr,
    "bn" to R.string.language_name_bn,
    "pa" to R.string.language_name_pa,
    "ta" to R.string.language_name_ta,
    "te" to R.string.language_name_te,
    "kn" to R.string.language_name_kn,
    "ml" to R.string.language_name_ml,
    "as" to R.string.language_name_as,
    "or" to R.string.language_name_or,
    "ur" to R.string.language_name_ur,
    "ru" to R.string.language_name_ru,
    "es" to R.string.language_name_es,
    "fr" to R.string.language_name_fr,
    "de" to R.string.language_name_de,
    "pt" to R.string.language_name_pt,
    "pt-BR" to R.string.language_name_pt_br,
    "it" to R.string.language_name_it,
    "id" to R.string.language_name_id,
    "ar" to R.string.language_name_ar,
    "ja" to R.string.language_name_ja,
    "ko" to R.string.language_name_ko,
    "zh-Hans" to R.string.language_name_zh_hans,
    "zh-Hant" to R.string.language_name_zh_hant,
)

private val APP_LANGUAGE_OPTIONS = listOf(
    AppLanguageOption(languageTag = null, displayNameRes = R.string.system_default),
) + supportedAppLanguageTags().map { languageTag ->
    AppLanguageOption(
        languageTag = languageTag,
        displayNameRes = LANGUAGE_NAME_RESOURCES.entries
            .firstOrNull { (knownTag, _) -> knownTag.equals(languageTag, ignoreCase = true) }
            ?.value,
    )
}

internal fun supportedAppLanguageTags(): List<String> = BuildConfig.SUPPORTED_LOCALE_TAGS
    .split(',')
    .map(String::trim)
    .filter(String::isNotEmpty)

internal fun resolvedAppLanguageTag(languageTag: String?): String? =
    appLanguageOption(languageTag).languageTag

internal fun appLanguageDisplayName(context: Context, languageTag: String?): String {
    val option = appLanguageOption(languageTag)
    option.displayNameRes?.let { return context.getString(it) }

    val supportedTag = option.languageTag ?: return context.getString(R.string.system_default)
    val locale = normalizedLocale(supportedTag) ?: return supportedTag
    return locale.getDisplayName(locale).ifBlank { supportedTag }
}

private fun appLanguageOption(languageTag: String?): AppLanguageOption {
    val systemDefault = APP_LANGUAGE_OPTIONS.first()
    val requestedLocale = normalizedLocale(languageTag) ?: return systemDefault

    APP_LANGUAGE_OPTIONS.firstOrNull { option ->
        val optionLocale = normalizedLocale(option.languageTag) ?: return@firstOrNull false
        optionLocale.toLanguageTag().equals(requestedLocale.toLanguageTag(), ignoreCase = true)
    }?.let { return it }

    return APP_LANGUAGE_OPTIONS
        .mapNotNull { option ->
            val optionLocale = normalizedLocale(option.languageTag) ?: return@mapNotNull null
            localeMatchScore(optionLocale, requestedLocale)?.let { score -> option to score }
        }
        .maxByOrNull { (_, score) -> score }
        ?.first
        ?: systemDefault
}

private fun normalizedLocale(languageTag: String?): Locale? {
    val normalizedTag = languageTag
        ?.substringBefore(',')
        ?.trim()
        ?.replace('_', '-')
        .orEmpty()
    if (normalizedTag.isEmpty()) return null

    val locale = Locale.forLanguageTag(normalizedTag)
    return locale.takeIf { it.language.isNotEmpty() && it.language != "und" }
}

private fun localeMatchScore(option: Locale, requested: Locale): Int? {
    if (!option.language.equals(requested.language, ignoreCase = true)) return null

    if (option.country.isNotEmpty() &&
        !option.country.equals(requested.country, ignoreCase = true)
    ) {
        return null
    }

    val requestedScript = requested.script.ifEmpty { likelyChineseScript(requested) }
    if (option.script.isNotEmpty() &&
        !option.script.equals(requestedScript, ignoreCase = true)
    ) {
        return null
    }

    return when {
        option.country.isNotEmpty() -> 3
        option.script.isNotEmpty() -> 2
        else -> 1
    }
}

private fun likelyChineseScript(locale: Locale): String {
    if (!locale.language.equals("zh", ignoreCase = true)) return ""
    return when (locale.country.uppercase(Locale.ROOT)) {
        "TW", "HK", "MO" -> "Hant"
        else -> "Hans"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    selectedLanguageTag: String?,
    onLanguageSelected: (String?) -> Unit,
    onBack: () -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val selectedOption = appLanguageOption(selectedLanguageTag)
    val namedOptions = APP_LANGUAGE_OPTIONS.map { option ->
        NamedLanguageOption(
            option = option,
            displayName = appLanguageDisplayName(context, option.languageTag),
        )
    }
    val normalizedQuery = searchQuery.trim()
    val visibleOptions = if (normalizedQuery.isEmpty()) {
        namedOptions
    } else {
        namedOptions.filter { namedOption ->
            val englishSearchName = namedOption.option.languageTag?.let { tag ->
                Locale.forLanguageTag(tag).getDisplayName(Locale.ENGLISH)
            } ?: context.getString(R.string.system_default)
            namedOption.displayName.contains(normalizedQuery, ignoreCase = true) ||
                namedOption.option.languageTag.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
                englishSearchName.contains(normalizedQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PremiumTopAppBar(
                title = stringResource(R.string.choose_language),
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item(key = "current-language") {
                PremiumCard(selected = true) {
                    Text(
                        text = stringResource(
                            R.string.current_language,
                            appLanguageDisplayName(context, selectedLanguageTag),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            item(key = "language-search") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.search_languages)) },
                    placeholder = { Text(stringResource(R.string.search_languages_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                )
            }

            if (visibleOptions.isEmpty()) {
                item(key = "no-languages") {
                    Text(
                        text = stringResource(R.string.no_languages_found),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.lg),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(
                    items = visibleOptions,
                    key = { it.option.languageTag ?: "system-default" },
                ) { namedOption ->
                    val selected = namedOption.option == selectedOption
                    PremiumCard(
                        selected = selected,
                        onClick = { onLanguageSelected(namedOption.option.languageTag) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = namedOption.displayName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            RadioButton(
                                selected = selected,
                                onClick = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NamedLanguageOption(
    val option: AppLanguageOption,
    val displayName: String,
)
