package com.rameshta.photocompressor

import android.app.LocaleConfig
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class LocaleResourcesInstrumentedTest {
    private val targetContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun regionalLocalesResolveToTheirSupportedBaseResources() {
        val regionalToBase = mapOf(
            "hi-IN" to "hi",
            "ru-RU" to "ru",
            "es-MX" to "es",
            "fr-CA" to "fr",
            "id-ID" to "id",
        )

        regionalToBase.forEach { (regionalTag, baseTag) ->
            assertEquals(text(baseTag), text(regionalTag))
            assertNotEquals(text("en"), text(regionalTag))
        }
    }

    @Test
    fun supportedPortugueseResourcesResolveIndependently() {
        assertEquals(text("pt-BR"), text("pt-BR-u-nu-latn"))
        assertTrue(
            listOf(
                R.string.system_default,
                R.string.settings_privacy_processing,
                R.string.save_failure,
            ).any { resourceId ->
                text("pt-BR", resourceId) != text("pt-PT", resourceId)
            },
        )
    }

    @Test
    fun englishAndUnsupportedLocalesUseDefaultEnglishResources() {
        assertEquals(text("en"), text("en-IN"))
        assertEquals(text("en"), text("sw-KE"))
    }

    @Test
    fun everyDeclaredTranslationResolvesAwayFromEnglish() {
        listOf(
            "de", "fr", "ja", "hi", "ru", "es", "pt-PT", "pt-BR", "it", "id", "ar",
            "ko", "ur",
        ).forEach { languageTag ->
            assertNotEquals("$languageTag unexpectedly used English", text("en"), text(languageTag))
        }
    }

    @Test
    fun arabicAndUrduResourcesUseRtlLayoutDirection() {
        assertEquals(View.LAYOUT_DIRECTION_RTL, localizedContext("ar-SA").resources.configuration.layoutDirection)
        assertEquals(View.LAYOUT_DIRECTION_RTL, localizedContext("ur-PK").resources.configuration.layoutDirection)
        assertNotEquals(text("en"), text("ar-SA"))
        assertNotEquals(text("en"), text("ur-PK"))
    }

    @Test
    fun generatedDebugLocaleConfigContainsProductionTagsLegacyAliasAndPseudoLocales() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resourceId = context.resources.getIdentifier(
            "_generated_res_locale_config",
            "xml",
            context.packageName,
        )
        assertTrue("Generated locale config resource was not found", resourceId != 0)

        val configuredTags = buildSet {
            val parser = context.resources.getXml(resourceId)
            try {
                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "locale") {
                        parser.getAttributeValue(ANDROID_NAMESPACE, "name")?.let(::add)
                    }
                    parser.next()
                }
            } finally {
                parser.close()
            }
        }
        val productionTags = BuildConfig.SUPPORTED_LOCALE_TAGS
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toSet()

        assertEquals(productionTags + setOf("en-XA", "ar-XB"), configuredTags)
    }

    @SdkSuppress(minSdkVersion = 33)
    @Test
    fun frameworkLocaleConfigCanonicalizesAndDeduplicatesIndonesianAlias() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val supportedLocales = requireNotNull(LocaleConfig(context).supportedLocales) {
            "Framework LocaleConfig did not return supported locales"
        }
        val supportedTags = buildList {
            for (index in 0 until supportedLocales.size()) {
                add(supportedLocales[index].toLanguageTag())
            }
        }
        val productionTags = BuildConfig.SUPPORTED_LOCALE_TAGS
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)

        assertEquals(
            productionTags.toSet() + setOf("en-XA", "ar-XB"),
            supportedTags.toSet(),
        )
        assertEquals(1, supportedTags.count { it == "id" })
        assertTrue(supportedTags.none { it == "in" })
    }

    @Test
    fun debugPseudoLocaleResourcesRemainAvailable() {
        assertNotEquals(text("en"), text("en-XA"))
        assertEquals(
            View.LAYOUT_DIRECTION_RTL,
            localizedContext("ar-XB").resources.configuration.layoutDirection,
        )
    }

    private fun text(languageTag: String, resourceId: Int = R.string.settings): String {
        return localizedContext(languageTag).getString(resourceId)
    }

    private fun localizedContext(languageTag: String): Context {
        val configuration = Configuration(targetContext.resources.configuration).apply {
            setLocales(LocaleList.forLanguageTags(languageTag))
        }
        return targetContext.createConfigurationContext(configuration)
    }

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
