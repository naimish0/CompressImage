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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumOutlinedButton
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    privacyOptionsRequired: Boolean,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onPrivacyOptions: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onBack: () -> Unit,
) {
    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        topBar = {
            PremiumTopAppBar(
                title = "Settings",
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
                SettingsCard(title = "Privacy") {
                    Text(
                        "Image processing happens entirely on your device. An internet connection may be used to load advertisements and consent information.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Selected images, generated images, filenames, content URIs, and EXIF metadata are not sent to the advertising SDK by app code.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(AppSpacing.sm))
                    PremiumOutlinedButton(
                        text = "Privacy policy",
                        onClick = onPrivacyPolicy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (privacyOptionsRequired) {
                        Spacer(Modifier.height(AppSpacing.sm))
                        PremiumOutlinedButton(
                            text = "Privacy choices",
                            onClick = onPrivacyOptions,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            item {
                SettingsCard(title = "Advertising") {
                    Text(
                        "This app uses Google AdMob. A single banner may appear on eligible content screens. Capped interstitial ads may appear only at a natural section transition after repeated completed workflows.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Interstitial ads may appear before Save, Share, and Open actions after the eligibility threshold. They do not interrupt image selection, processing, Back, or exit. App-open ads are skipped on first launch and are limited to eligible returns after the app has been in the background.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SettingsCard(title = "Background removal") {
                    Text(
                        "Background removal runs offline with a bundled ONNX U2-NetP model. Images are processed on-device and are not uploaded by this feature.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Open-source notices are packaged with the app.",
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
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PremiumTopAppBar(
                title = "Privacy policy",
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
                    "Effective July 18, 2026",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                PolicySection(
                    title = "Overview",
                    body = "Photo Compressor selects, compresses, resizes, converts, saves, opens, shares, and removes backgrounds from images. Image processing runs on your device. The internet is used only for advertising and advertising-consent services.",
                )
            }
            item {
                PolicySection(
                    title = "Images and files",
                    body = "You choose images through Android's system Photo Picker. App code does not upload selected or generated images, filenames, content URIs, EXIF metadata, or image-processing data. Generated images are shared only when you choose Share, opened only when you choose Open, and saved to device storage only when you choose Save.",
                )
            }
            item {
                PolicySection(
                    title = "Local history and retention",
                    body = "The app keeps a local history containing source and output content references, display names, MIME types, file sizes, dimensions, operation types, and timestamps. Temporary generated files may remain in the app cache. Clear History or clear the app's storage to remove app-local records and cache files. Delete saved images through your gallery or file manager. The app does not create user accounts.",
                )
            }
            item {
                PolicySection(
                    title = "Advertising and consent",
                    body = "Google AdMob provides ads and Google's User Messaging Platform provides consent choices where required. Depending on your device, region, consent, and Google settings, Google and its partners may process IP-derived approximate location, advertising or device identifiers, ad interactions, diagnostics, and consent signals. Core image processing remains available when ads are unavailable or the device is offline.",
                )
            }
            item {
                PolicySection(
                    title = "Background removal",
                    body = "Background removal uses a bundled ONNX U2-NetP model and ONNX Runtime entirely on the device. No model download or image upload is performed by this feature.",
                )
            }
            item {
                PolicySection(
                    title = "Children",
                    body = "Photo Compressor is a general-purpose utility and is not directed to children under 13. The developer does not knowingly collect personal information from children through app code.",
                )
            }
            item {
                PolicySection(
                    title = "Third parties and contact",
                    body = "Google's privacy policy applies to data handled by Google advertising services. Open-source component notices are included with the app. For privacy questions, use the developer contact published on Photo Compressor's Play Store listing. This policy may be updated when the app or its services change.",
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
