package com.rameshta.photocompressor.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ui.components.AdScreenScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    keepOriginal: Boolean,
    privacyOptionsRequired: Boolean,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onKeepOriginal: (Boolean) -> Unit,
    onPrivacyOptions: () -> Unit,
    onBack: () -> Unit,
) {
    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Go back")
                    }
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SettingsCard(title = "Files") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Keep original image", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Source images are never modified or deleted unless you explicitly replace this behavior.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = keepOriginal, onCheckedChange = onKeepOriginal)
                    }
                }
            }
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
                    if (privacyOptionsRequired) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = onPrivacyOptions, modifier = Modifier.fillMaxWidth()) {
                            Text("Privacy choices")
                        }
                    }
                }
            }
            item {
                SettingsCard(title = "Ads and production setup") {
                    Text(
                        "This app uses Google AdMob for banners and interstitial advertisements. Full-screen ads are not shown before image selection, during processing, on Back, on Open, on Share, or while exiting.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Before publishing, configure production ad units, privacy messages, app-ads.txt, production signing, and final Play Console data-safety disclosures.",
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
                        "Open-source notices are packaged with the app assets and should also be reflected in the store listing or legal page as required.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}
