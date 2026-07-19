package com.rameshta.photocompressor.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.rameshta.photocompressor.BuildConfig
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleConsentManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val configuration: AdsConfiguration,
) : ConsentManager {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)
    private val _state = MutableStateFlow(currentState())
    private var consentInfoUpdateInProgress = false
    private var consentInfoUpdateCompleted = false
    private var consentFormCheckCompleted = false
    private var consentFormCheckInProgress = false
    private var consentHostActivity = WeakReference<Activity>(null)
    override val state: StateFlow<ConsentUiState> = _state.asStateFlow()

    override fun canRequestAds(): Boolean {
        return configuration.adsEnabled && consentInformation.canRequestAds()
    }

    override fun requestConsentInfoUpdate(
        activity: Activity,
        onCanRequestAds: () -> Unit,
    ) {
        consentHostActivity = WeakReference(activity)
        if (!configuration.adsEnabled) {
            _state.value = ConsentUiState()
            return
        }
        if (consentInfoUpdateInProgress) return
        if (consentInfoUpdateCompleted) {
            if (consentFormCheckCompleted) {
                if (canRequestAds()) onCanRequestAds()
            } else {
                showRequiredConsentForm(activity, onCanRequestAds)
            }
            return
        }
        consentInfoUpdateInProgress = true
        _state.update { it.copy(requestInProgress = true, lastError = null) }
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                consentInfoUpdateInProgress = false
                consentInfoUpdateCompleted = true
                refreshState(requestInProgress = false)
                if (canRequestAds()) {
                    onCanRequestAds()
                }
                currentConsentHostActivity()?.let { consentHost ->
                    showRequiredConsentForm(consentHost, onCanRequestAds)
                }
            },
            { requestError ->
                consentInfoUpdateInProgress = false
                consentInfoUpdateCompleted = false
                logDebug("Consent info update failed: ${requestError.errorCode}")
                refreshState(error = requestError, requestInProgress = false)
                if (canRequestAds()) {
                    onCanRequestAds()
                }
                currentConsentHostActivity()
                    ?.takeIf { latestHost -> latestHost !== activity }
                    ?.let { latestHost -> requestConsentInfoUpdate(latestHost, onCanRequestAds) }
            },
        )
    }

    private fun showRequiredConsentForm(
        activity: Activity,
        onCanRequestAds: () -> Unit,
    ) {
        if (consentFormCheckCompleted || consentFormCheckInProgress) {
            if (consentFormCheckCompleted && canRequestAds()) onCanRequestAds()
            return
        }
        consentFormCheckInProgress = true
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
            consentFormCheckInProgress = false
            consentFormCheckCompleted = formError == null
            if (formError != null) {
                logDebug("Consent form dismissed with error: ${formError.errorCode}")
            }
            refreshState(error = formError, requestInProgress = false)
            if (canRequestAds()) {
                onCanRequestAds()
            }
            if (formError != null) {
                currentConsentHostActivity()
                    ?.takeIf { latestHost -> latestHost !== activity }
                    ?.let { latestHost -> showRequiredConsentForm(latestHost, onCanRequestAds) }
            }
        }
    }

    override fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: () -> Unit,
    ) {
        if (!configuration.adsEnabled) {
            onDismissed()
            return
        }
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                logDebug("Privacy options form dismissed with error: ${formError.errorCode}")
            }
            refreshState(error = formError, requestInProgress = false)
            onDismissed()
        }
    }

    private fun refreshState(
        error: FormError? = null,
        requestInProgress: Boolean,
    ) {
        _state.value = currentState(requestInProgress = requestInProgress, error = error)
    }

    private fun currentState(
        requestInProgress: Boolean = false,
        error: FormError? = null,
    ): ConsentUiState {
        return ConsentUiState(
            canRequestAds = canRequestAds(),
            privacyOptionsRequired = configuration.adsEnabled &&
                consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED,
            requestInProgress = requestInProgress,
            lastError = error?.message,
        )
    }

    private fun currentConsentHostActivity(): Activity? {
        return consentHostActivity.get()?.takeIf { activity ->
            !activity.isFinishing && !activity.isDestroyed
        }
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    private companion object {
        const val TAG = "AdsConsent"
    }
}
