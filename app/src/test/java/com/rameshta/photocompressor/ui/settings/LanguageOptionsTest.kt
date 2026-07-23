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
        assertEquals("ru", resolvedAppLanguageTag("ru-RU"))
        assertEquals("es", resolvedAppLanguageTag("es-MX"))
        assertEquals("fr", resolvedAppLanguageTag("fr-CA"))
        assertEquals("en", resolvedAppLanguageTag("en-IN"))
        assertNull(resolvedAppLanguageTag("gu-IN"))
    }

    @Test
    fun supportedRegionalAndScriptVariantsRemainDistinct() {
        assertEquals("pt-BR", resolvedAppLanguageTag("pt-BR"))
        assertEquals("pt-PT", resolvedAppLanguageTag("pt-PT"))
    }

    @Test
    fun extensionsAndLegacyRegionSeparatorsDoNotChangeMatching() {
        assertEquals("pt-BR", resolvedAppLanguageTag("pt-BR-u-nu-latn"))
        assertEquals("es", resolvedAppLanguageTag("es-MX-u-ca-gregory"))
        assertEquals("fr", resolvedAppLanguageTag("fr_CA"))
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
                "en", "de", "fr", "ja", "hi", "ru", "es", "pt-PT", "pt-BR", "it", "id",
                "ar", "ko", "ur",
            ),
            tags,
        )

        assertEquals("en", tags.first())
        assertEquals(tags.size, tags.map(String::lowercase).distinct().size)
        assertFalse(tags.any { it.equals("en-XA", ignoreCase = true) })
        assertFalse(tags.any { it.equals("ar-XB", ignoreCase = true) })
        assertTrue(tags.indexOf("pt-PT") < tags.indexOf("pt-BR"))
    }
}
