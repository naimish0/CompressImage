package com.example.compressimage.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class ConsentUiState(
    val canRequestAds: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    val requestInProgress: Boolean = false,
    val lastError: String? = null,
)

interface ConsentManager {
    val state: StateFlow<ConsentUiState>

    fun canRequestAds(): Boolean

    fun requestConsentInfoUpdate(
        activity: Activity,
        onCanRequestAds: () -> Unit,
    )

    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: () -> Unit = {},
    )
}
