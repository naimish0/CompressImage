package com.rameshta.photocompressor.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumOutlinedButton
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.theme.AppSpacing
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    privacyOptionsRequired: Boolean,
    selectedLanguageTag: String?,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onChooseLanguage: () -> Unit,
    onPrivacyOptions: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        topBar = {
            PremiumTopAppBar(
                title = stringResource(R.string.settings),
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                SettingsCard(title = stringResource(R.string.language)) {
                    Text(
                        text = stringResource(
                            R.string.current_language,
                            appLanguageDisplayName(context, selectedLanguageTag),
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(AppSpacing.sm))
                    PremiumOutlinedButton(
                        text = stringResource(R.string.choose_language),
                        onClick = onChooseLanguage,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                SettingsCard(title = stringResource(R.string.privacy)) {
                    Text(
                        text = stringResource(R.string.settings_privacy_processing),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_privacy_ad_sdk),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(AppSpacing.sm))
                    PremiumOutlinedButton(
                        text = stringResource(R.string.privacy_policy),
                        onClick = onPrivacyPolicy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (privacyOptionsRequired) {
                        Spacer(Modifier.height(AppSpacing.sm))
                        PremiumOutlinedButton(
                            text = stringResource(R.string.privacy_choices),
                            onClick = onPrivacyOptions,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            item {
                SettingsCard(title = stringResource(R.string.advertising)) {
                    Text(
                        text = stringResource(R.string.settings_ads_placement),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_ads_lifecycle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SettingsCard(title = stringResource(R.string.policy_background_removal_title)) {
                    Text(
                        text = stringResource(R.string.settings_background_model),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.open_source_notices),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val activeLocale = LocalConfiguration.current.locales[0]
    val effectiveDate = DateFormat.getDateInstance(DateFormat.LONG, activeLocale).format(
        GregorianCalendar(2026, Calendar.JULY, 19).time,
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PremiumTopAppBar(
                title = stringResource(R.string.privacy_policy),
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                Text(
                    text = stringResource(R.string.privacy_effective_date, effectiveDate),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                PolicySection(
                    title = stringResource(R.string.policy_overview_title),
                    body = stringResource(R.string.policy_overview_body),
                )
            }
            item {
                PolicySection(
                    title = stringResource(R.string.policy_images_files_title),
                    body = stringResource(R.string.policy_images_files_body),
                )
            }
            item {
                PolicySection(
                    title = stringResource(R.string.policy_local_history_title),
                    body = stringResource(R.string.policy_local_history_body),
                )
            }
            item {
                PolicySection(
                    title = stringResource(R.string.policy_advertising_consent_title),
                    body = stringResource(R.string.policy_advertising_consent_body),
                )
            }
            item {
                PolicySection(
                    title = stringResource(R.string.policy_background_removal_title),
                    body = stringResource(R.string.policy_background_removal_body),
                )
            }
            item {
                PolicySection(
                    title = stringResource(R.string.policy_children_title),
                    body = stringResource(R.string.policy_children_body),
                )
            }
            item {
                PolicySection(
                    title = stringResource(R.string.policy_third_parties_title),
                    body = stringResource(R.string.policy_third_parties_body),
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    PremiumCard {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(AppSpacing.xs))
        content()
    }
}

@Composable
private fun PolicySection(
    title: String,
    body: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
