package com.rameshta.photocompressor.ui.comparison

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ads.BannerPlacement
import com.rameshta.photocompressor.domain.model.CompressionMode
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.ui.PendingAdAction
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
import com.rameshta.photocompressor.ui.asString
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.EmptySpaceBannerAd
import com.rameshta.photocompressor.ui.components.ImagePreviewBox
import com.rameshta.photocompressor.ui.components.InfoRow
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumEmptyState
import com.rameshta.photocompressor.ui.components.PremiumOutlinedButton
import com.rameshta.photocompressor.ui.components.PremiumPrimaryButton
import com.rameshta.photocompressor.ui.components.PremiumSecondaryButton
import com.rameshta.photocompressor.ui.components.PremiumSuccessPill
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.components.ProcessedImageCard
import com.rameshta.photocompressor.ui.theme.AppSpacing
import com.rameshta.photocompressor.util.CompressionStatsCalculator
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    state: PhotoCompressorUiState,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onBack: () -> Unit,
    onSelectResult: (String) -> Unit,
    onSaveSelected: (String?) -> Unit,
    onSaveAll: () -> Unit,
    onShareSelected: () -> Unit,
    onShareAll: () -> Unit,
    onOpenImage: () -> Unit,
    onCompressAnother: () -> Unit,
) {
    val selected = state.results.firstOrNull { it.id == state.selectedResultId } ?: state.results.firstOrNull()
    val fallbackOutputName = stringResource(R.string.processed_image)
    var requestedName by rememberSaveable(selected?.id) {
        mutableStateOf(selected?.displayName.orEmpty().ifBlank { fallbackOutputName })
    }
    val actionInProgress = state.isSaving || state.pendingAdAction !is PendingAdAction.None

    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        showTopBanner = true,
        showBottomBanner = true,
        topBar = {
            PremiumTopAppBar(
                title = stringResource(R.string.compare_result),
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack,
            )
        },
    ) {
        if (selected == null) {
            EmptyResult(onCompressAnother = onCompressAnother)
            return@AdScreenScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                ComparisonPanel(selected)
            }

            if (state.results.size > 1) {
                item {
                    Text(stringResource(R.string.batch_results), style = MaterialTheme.typography.titleMedium)
                }
                items(state.results, key = { it.id }) { image ->
                    ProcessedImageCard(
                        image = image,
                        selected = image.id == selected.id,
                        onClick = { onSelectResult(image.id) },
                    )
                }
            }

            item {
                StatsPanel(selected)
            }

            item {
                EmptySpaceBannerAd(
                    placement = BannerPlacement.RESULT_EMPTY_SPACE,
                    bannerAdController = bannerAdController,
                    hidden = fullScreenAdVisible,
                )
            }

            selected.warning?.let { warning ->
                item {
                    Text(
                        text = warning.asString(),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            item {
                PremiumCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        Text(stringResource(R.string.save_and_share), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = requestedName,
                            onValueChange = { requestedName = it },
                            label = { Text(stringResource(R.string.output_filename)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        ResultActionPair(
                            first = { modifier ->
                                PremiumPrimaryButton(
                                    text = if (state.isSaving) {
                                        stringResource(R.string.saving)
                                    } else {
                                        stringResource(R.string.save)
                                    },
                                    onClick = { onSaveSelected(requestedName) },
                                    enabled = !actionInProgress,
                                    loading = state.isSaving,
                                    modifier = modifier,
                                    icon = Icons.Outlined.Save,
                                )
                            },
                            second = { modifier ->
                                PremiumOutlinedButton(
                                    text = stringResource(R.string.share),
                                    onClick = onShareSelected,
                                    enabled = !actionInProgress,
                                    modifier = modifier,
                                    icon = Icons.Outlined.Share,
                                )
                            },
                        )
                        ResultActionPair(
                            first = { modifier ->
                                PremiumOutlinedButton(
                                    text = stringResource(R.string.open),
                                    onClick = onOpenImage,
                                    enabled = !actionInProgress,
                                    modifier = modifier,
                                    icon = Icons.Outlined.FolderOpen,
                                )
                            },
                            second = { modifier ->
                                PremiumSecondaryButton(
                                    text = stringResource(R.string.another),
                                    onClick = onCompressAnother,
                                    modifier = modifier,
                                    icon = Icons.Outlined.AddPhotoAlternate,
                                )
                            },
                        )
                        if (state.results.size > 1) {
                            ResultActionPair(
                                first = { modifier ->
                                    PremiumOutlinedButton(
                                        text = stringResource(R.string.save_all),
                                        onClick = onSaveAll,
                                        enabled = !actionInProgress,
                                        modifier = modifier,
                                    )
                                },
                                second = { modifier ->
                                    PremiumOutlinedButton(
                                        text = stringResource(R.string.share_all),
                                        onClick = onShareAll,
                                        enabled = !actionInProgress,
                                        modifier = modifier,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultActionPair(
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 360.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                first(Modifier.fillMaxWidth())
                second(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                first(Modifier.weight(1f))
                second(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ComparisonPanel(image: ProcessedImage) {
    PremiumCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.result_ready), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.compare_before_saving),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PremiumSuccessPill(stringResource(R.string.processed))
        }
        BoxWithConstraints {
            if (maxWidth > 700.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth()) {
                    PreviewColumn(
                        stringResource(R.string.original),
                        image.original.uriString,
                        modifier = Modifier.weight(1f),
                    )
                    PreviewColumn(
                        stringResource(R.string.processed),
                        image.filePath,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    PreviewColumn(stringResource(R.string.original), image.original.uriString)
                    PreviewColumn(stringResource(R.string.processed), image.filePath)
                }
            }
        }
    }
}

@Composable
private fun PreviewColumn(
    label: String,
    model: Any,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(AppSpacing.xs))
        ImagePreviewBox(
            model = model,
            contentDescription = stringResource(R.string.image_preview, label),
        )
    }
}

@Composable
private fun StatsPanel(image: ProcessedImage) {
    val stats = CompressionStatsCalculator.calculate(image.original.sizeBytes, image.sizeBytes)
    val context = LocalContext.current
    PremiumCard {
        Text(stringResource(R.string.details), style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            InfoRow(
                stringResource(R.string.original_size),
                Formatter.formatFileSize(context, stats.originalSizeBytes),
            )
            InfoRow(
                stringResource(R.string.processed_size),
                Formatter.formatFileSize(context, stats.processedSizeBytes),
            )
            image.requestedTargetBytes?.let { target ->
                InfoRow(
                    stringResource(R.string.requested_target),
                    Formatter.formatFileSize(context, target),
                )
            }
            image.targetReached?.let { reached ->
                InfoRow(
                    stringResource(R.string.target_status),
                    if (reached) {
                        stringResource(R.string.reached_within_tolerance)
                    } else {
                        stringResource(R.string.not_safely_achievable)
                    },
                )
            }
            if (stats.savedBytes >= 0) {
                InfoRow(
                    stringResource(R.string.space_saved),
                    Formatter.formatFileSize(context, stats.savedBytes),
                )
                InfoRow(
                    stringResource(R.string.percentage_saved),
                    stringResource(
                        R.string.percentage_saved_value,
                        localizedPercent(stats.percentageSaved),
                    ),
                )
            } else {
                InfoRow(
                    stringResource(R.string.size_increase),
                    Formatter.formatFileSize(context, -stats.savedBytes),
                )
                InfoRow(
                    stringResource(R.string.percentage_saved),
                    stringResource(R.string.percentage_saved_value, localizedPercent(0.0)),
                )
            }
            InfoRow(
                stringResource(R.string.compression_ratio),
                stringResource(
                    R.string.compression_ratio_value,
                    localizedDecimal(stats.compressionRatio),
                ),
            )
            InfoRow(
                stringResource(R.string.original_resolution),
                stringResource(R.string.image_resolution, image.original.width, image.original.height),
            )
            InfoRow(
                stringResource(R.string.processed_resolution),
                stringResource(R.string.image_resolution, image.width, image.height),
            )
            InfoRow(stringResource(R.string.output_format), image.format.localizedDisplayName())
            image.compressionMode?.let {
                InfoRow(stringResource(R.string.quality_mode), it.localizedTitle())
            }
            InfoRow(
                stringResource(R.string.output_quality),
                image.outputQuality?.let { localizedInteger(it) } ?: stringResource(R.string.lossless),
            )
        }
    }
}

@Composable
private fun EmptyResult(
    modifier: Modifier = Modifier,
    onCompressAnother: () -> Unit,
) {
    PremiumEmptyState(
        title = stringResource(R.string.no_result_title),
        message = stringResource(R.string.no_result_message),
        actionLabel = stringResource(R.string.select_an_image),
        onAction = onCompressAnother,
        modifier = modifier,
    )
}

@Composable
private fun localizedPercent(value: Double): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        NumberFormat.getPercentInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
    }
    return formatter.format(value.coerceAtLeast(0.0) / 100.0)
}

@Composable
private fun localizedDecimal(value: Double): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    return formatter.format(value)
}

@Composable
private fun localizedInteger(value: Int): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    return formatter.format(value)
}

@Composable
private fun CompressionMode.localizedTitle(): String {
    return stringResource(
        when (this) {
            CompressionMode.BEST_QUALITY -> R.string.quality_best_title
            CompressionMode.BALANCED -> R.string.quality_balanced_title
            CompressionMode.SMALLEST_SIZE -> R.string.quality_smallest_title
        },
    )
}

@Composable
private fun ImageFormat.localizedDisplayName(): String {
    return if (this == ImageFormat.UNKNOWN) {
        stringResource(R.string.format_unknown)
    } else {
        displayName
    }
}
