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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ui.BatchItemStatus
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.BatchStatusCard
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumOutlinedButton
import com.rameshta.photocompressor.ui.components.PremiumPrimaryButton
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.theme.AppSpacing
import java.text.NumberFormat

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
                title = if (state.batch.isRunning) {
                    stringResource(R.string.compressing)
                } else {
                    stringResource(R.string.batch_summary)
                },
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
                        text = stringResource(R.string.cancel),
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.Cancel,
                    )
                } else {
                    if (failedCount > 0) {
                        PremiumOutlinedButton(
                            text = stringResource(R.string.retry_failed),
                            onClick = onRetryFailed,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Outlined.Refresh,
                        )
                    }
                    PremiumPrimaryButton(
                        text = stringResource(R.string.compare_results),
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
                        Text(
                            stringResource(R.string.total_progress),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(R.string.progress_percentage, localizedPercent(state.totalProgress)),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Spacer(Modifier.height(AppSpacing.xs))
                    LinearProgressIndicator(
                        progress = { state.totalProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.batch.summary?.let { summary ->
                        Spacer(Modifier.height(AppSpacing.xs))
                        Text(
                            text = if (summary.cancelled) {
                                stringResource(
                                    R.string.batch_summary_counts_cancelled,
                                    summary.successful,
                                    summary.failed,
                                )
                            } else {
                                stringResource(
                                    R.string.batch_summary_counts,
                                    summary.successful,
                                    summary.failed,
                                )
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(state.batch.items, key = { it.imageId }) { item ->
                BatchStatusCard(item = item)
            }
        }
    }
}

@Composable
private fun localizedPercent(value: Float): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        NumberFormat.getPercentInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }
    return formatter.format(value.coerceIn(0f, 1f).toDouble())
}
