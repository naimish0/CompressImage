package com.rameshta.photocompressor.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ui.theme.CompressImageTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectedLanguageIsShownAndAnotherLanguageCanBeChosen() {
        var selectedLanguageTag: String? = "hi"
        setLanguageContent(selectedLanguageTag) { selectedLanguageTag = it }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val currentLabel = context.getString(
            R.string.current_language,
            context.getString(R.string.language_name_hi),
        )

        composeRule.onNodeWithText(currentLabel).assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("Russian")
        composeRule.onNodeWithText("Русский").performClick()

        composeRule.runOnIdle { assertEquals("ru", selectedLanguageTag) }
    }

    @Test
    fun searchFiltersTheLongLanguageList() {
        setLanguageContent(selectedLanguageTag = null) {}

        composeRule.onNode(hasSetTextAction()).performTextInput("Russian")

        composeRule.onNodeWithText("Русский").assertIsDisplayed()
    }

    @Test
    fun systemDefaultReturnsAnEmptyAppLocaleSelection() {
        var selectedLanguageTag: String? = "es"
        setLanguageContent(selectedLanguageTag) { selectedLanguageTag = it }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithText(context.getString(R.string.system_default)).performClick()

        composeRule.runOnIdle { assertNull(selectedLanguageTag) }
    }

    private fun setLanguageContent(
        selectedLanguageTag: String?,
        onSelected: (String?) -> Unit,
    ) {
        composeRule.setContent {
            CompressImageTheme {
                LanguageScreen(
                    selectedLanguageTag = selectedLanguageTag,
                    onLanguageSelected = onSelected,
                    onBack = {},
                )
            }
        }
    }
}
