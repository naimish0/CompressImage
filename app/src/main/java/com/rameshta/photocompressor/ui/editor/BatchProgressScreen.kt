package com.rameshta.photocompressor.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ui.BatchItemStatus
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.BatchStatusCard
import com.rameshta.photocompressor.ui.components.InlineNativeAdvancedAd
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumOutlinedButton
import com.rameshta.photocompressor.ui.components.PremiumPrimaryButton
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.percentText
import com.rameshta.photocompressor.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchProgressScreen(
    state: PhotoCompressorUiState,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onRetryFailed: () -> Unit,
    onViewResults: () -> Unit,
) {
    val failedCount = state.batch.items.count { it.status == BatchItemStatus.FAILED }
    val successCount = state.batch.items.count { it.status == BatchItemStatus.SUCCESS }
    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        showTopBanner = true,
        showBottomBanner = true,
        hasBottomContent = true,
        topBar = {
            PremiumTopAppBar(
                title = if (state.batch.isRunning) "Compressing" else "Batch summary",
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack.takeUnless { state.batch.isRunning },
            )
        },
        bottomContent = {
            Column(
                Modifier,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                if (state.batch.isRunning) {
                    PremiumOutlinedButton(
                        text = "Cancel",
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.Cancel,
                    )
                } else {
                    if (failedCount > 0) {
                        PremiumOutlinedButton(
                            text = "Retry failed",
                            onClick = onRetryFailed,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Outlined.Refresh,
                        )
                    }
                    PremiumPrimaryButton(
                        text = "Compare results",
                        onClick = onViewResults,
                        enabled = successCount > 0,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.Visibility,
                    )
                }
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item {
                PremiumCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Total progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(state.totalProgress.percentText(), style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(AppSpacing.xs))
                    LinearProgressIndicator(
                        progress = { state.totalProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.batch.summary?.let { summary ->
                        Spacer(Modifier.height(AppSpacing.xs))
                        Text(
                            text = "Done: ${summary.successful} • Failed: ${summary.failed}" +
                                if (summary.cancelled) " • Cancelled" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                InlineNativeAdvancedAd(
                    bannerAdController = bannerAdController,
                    hidden = fullScreenAdVisible,
                )
            }

            items(state.batch.items, key = { it.imageId }) { item ->
                BatchStatusCard(item = item)
            }
        }
    }
}
