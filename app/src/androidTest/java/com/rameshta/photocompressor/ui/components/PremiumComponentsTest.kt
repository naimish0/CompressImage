package com.rameshta.photocompressor.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rameshta.photocompressor.ui.theme.CompressImageTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PremiumComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun primaryButtonRendersAndClicks() {
        var clicks = 0

        composeRule.setContent {
            CompressImageTheme {
                PremiumPrimaryButton(
                    text = "Save image",
                    onClick = { clicks += 1 },
                    icon = Icons.Outlined.Save,
                )
            }
        }

        composeRule.onNodeWithText("Save image").assertIsDisplayed().performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun loadingButtonKeepsVisibleLabel() {
        composeRule.setContent {
            CompressImageTheme {
                PremiumPrimaryButton(
                    text = "Saving...",
                    onClick = {},
                    loading = true,
                )
            }
        }

        composeRule.onNodeWithText("Saving...").assertIsDisplayed()
    }

    @Test
    fun premiumCardRendersClickableContent() {
        var clicks = 0

        composeRule.setContent {
            CompressImageTheme {
                PremiumCard(onClick = { clicks += 1 }) {
                    androidx.compose.material3.Text("Premium card")
                }
            }
        }

        composeRule.onNodeWithText("Premium card").assertIsDisplayed().performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun loadingStateRendersVisibleContent() {
        composeRule.setContent {
            CompressImageTheme {
                PremiumLoadingState(title = "Loading history...")
            }
        }

        composeRule.onNodeWithText("Loading history...").assertIsDisplayed()
    }

    @Test
    fun emptyStateRendersVisibleContent() {
        composeRule.setContent {
            CompressImageTheme {
                PremiumEmptyState(
                    title = "No processed images yet.",
                    message = "Compressed images appear here.",
                )
            }
        }

        composeRule.onNodeWithText("No processed images yet.").assertIsDisplayed()
    }

    @Test
    fun errorStateRendersVisibleContent() {
        composeRule.setContent {
            CompressImageTheme {
                PremiumErrorState(
                    title = "History could not be loaded.",
                    message = "History unavailable",
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("History could not be loaded.").assertIsDisplayed()
    }
}
