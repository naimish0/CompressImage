package com.rameshta.photocompressor.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageOptionsTest {
    @Test
    fun regionalLocalesResolveToSupportedBaseLanguage() {
        assertEquals("hi", resolvedAppLanguageTag("hi-IN"))
        assertEquals("gu", resolvedAppLanguageTag("gu-IN"))
        assertEquals("mr", resolvedAppLanguageTag("mr-IN"))
        assertEquals("ta", resolvedAppLanguageTag("ta-IN"))
        assertEquals("ru", resolvedAppLanguageTag("ru-RU"))
        assertEquals("es", resolvedAppLanguageTag("es-MX"))
        assertEquals("fr", resolvedAppLanguageTag("fr-CA"))
        assertEquals("en", resolvedAppLanguageTag("en-IN"))
    }

    @Test
    fun supportedRegionalAndScriptVariantsRemainDistinct() {
        assertEquals("pt-BR", resolvedAppLanguageTag("pt-BR"))
        assertEquals("pt", resolvedAppLanguageTag("pt-PT"))
        assertEquals("zh-Hans", resolvedAppLanguageTag("zh-Hans-CN"))
        assertEquals("zh-Hant", resolvedAppLanguageTag("zh-Hant-TW"))
    }

    @Test
    fun extensionsAndLegacyRegionSeparatorsDoNotChangeMatching() {
        assertEquals("pt-BR", resolvedAppLanguageTag("pt-BR-u-nu-latn"))
        assertEquals("es", resolvedAppLanguageTag("es-MX-u-ca-gregory"))
        assertEquals("fr", resolvedAppLanguageTag("fr_CA"))
    }

    @Test
    fun chineseRegionsUseLikelyScriptUnlessScriptIsExplicit() {
        assertEquals("zh-Hans", resolvedAppLanguageTag("zh-CN"))
        assertEquals("zh-Hans", resolvedAppLanguageTag("zh-SG"))
        assertEquals("zh-Hant", resolvedAppLanguageTag("zh-TW"))
        assertEquals("zh-Hant", resolvedAppLanguageTag("zh-HK"))
        assertEquals("zh-Hant", resolvedAppLanguageTag("zh-MO"))
        assertEquals("zh-Hans", resolvedAppLanguageTag("zh-Hans-TW"))
    }

    @Test
    fun unsupportedOrEmptySelectionUsesSystemDefault() {
        assertNull(resolvedAppLanguageTag(null))
        assertNull(resolvedAppLanguageTag(""))
        assertNull(resolvedAppLanguageTag("sw-KE"))
    }

    @Test
    fun productionResourceLocalesDriveSelectorWithoutPseudoLocales() {
        val tags = supportedAppLanguageTags()

        assertEquals(
            listOf(
                "en", "hi", "gu", "mr", "bn", "pa", "ta", "te", "kn", "ml", "as", "or",
                "ur", "ru", "es", "fr", "de", "pt", "pt-BR", "it", "id", "ar", "ja", "ko",
                "zh-Hans", "zh-Hant",
            ),
            tags,
        )

        assertEquals("en", tags.first())
        assertEquals(tags.size, tags.map(String::lowercase).distinct().size)
        assertFalse(tags.any { it.equals("en-XA", ignoreCase = true) })
        assertFalse(tags.any { it.equals("ar-XB", ignoreCase = true) })
        assertTrue(tags.indexOf("pt") < tags.indexOf("pt-BR"))
        assertTrue(tags.indexOf("zh-Hans") < tags.indexOf("zh-Hant"))
    }
}
