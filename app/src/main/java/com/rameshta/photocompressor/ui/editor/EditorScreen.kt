package com.rameshta.photocompressor.ui.editor

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.domain.model.CompressionMode
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ResizeMode
import com.rameshta.photocompressor.domain.model.TargetSizePreset
import com.rameshta.photocompressor.domain.model.TargetSizeUnit
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
import com.rameshta.photocompressor.ui.asString
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.FormatChip
import com.rameshta.photocompressor.ui.components.ImagePreviewBox
import com.rameshta.photocompressor.ui.components.InfoRow
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumOutlinedButton
import com.rameshta.photocompressor.ui.components.PremiumPrimaryButton
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.theme.AppShapes
import com.rameshta.photocompressor.ui.theme.AppSpacing
import com.rameshta.photocompressor.ui.theme.AppTouchTargets
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: PhotoCompressorUiState,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onBack: () -> Unit,
    onTargetPreset: (TargetSizePreset) -> Unit,
    onCustomTarget: (String) -> Unit,
    onCustomTargetUnit: (TargetSizeUnit) -> Unit,
    onCompressionMode: (CompressionMode) -> Unit,
    onResizeMode: (ResizeMode) -> Unit,
    onCustomWidth: (String) -> Unit,
    onCustomHeight: (String) -> Unit,
    onMaintainAspect: (Boolean) -> Unit,
    onAllowUpscale: (Boolean) -> Unit,
    onFormat: (ImageFormat) -> Unit,
    onJpegBackgroundColor: (Int) -> Unit,
    onCompress: () -> Unit,
    onRemoveBackground: () -> Unit,
) {
    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        topBar = {
            PremiumTopAppBar(
                title = stringResource(R.string.preview_and_configure),
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
                PremiumCard {
                    Text(stringResource(R.string.selected_images_heading), style = MaterialTheme.typography.titleMedium)
                    state.selectedImages.firstOrNull()?.let { first ->
                        val displayName = first.displayName.ifBlank {
                            stringResource(R.string.selected_image)
                        }
                        ImagePreviewBox(
                            model = first.uriString,
                            contentDescription = stringResource(R.string.preview_of_image, displayName),
                            fit = false,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        val totalBytes = state.selectedImages.sumOf { it.sizeBytes }
                        InfoRow(
                            stringResource(R.string.count),
                            localizedInteger(state.selectedImages.size),
                        )
                        InfoRow(
                            stringResource(R.string.original_total),
                            Formatter.formatFileSize(LocalContext.current, totalBytes),
                        )
                        state.selectedImages.firstOrNull()?.let {
                            InfoRow(
                                stringResource(R.string.first_image),
                                stringResource(
                                    R.string.image_resolution_and_format,
                                    stringResource(R.string.image_resolution, it.width, it.height),
                                    it.format.displayName,
                                ),
                            )
                        }
                        state.currentOutputDimension?.let {
                            InfoRow(
                                stringResource(R.string.output_resolution),
                                stringResource(R.string.image_resolution, it.width, it.height),
                            )
                        }
                    }
                }
            }

            item {
                ConfigCard(title = stringResource(R.string.target_size)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TargetSizePreset.entries.forEach { preset ->
                            FilterChip(
                                selected = state.config.targetSize.preset == preset,
                                onClick = { onTargetPreset(preset) },
                                label = { Text(preset.localizedLabel()) },
                            )
                        }
                    }
                    if (state.config.targetSize.preset == TargetSizePreset.CUSTOM) {
                        Spacer(Modifier.height(AppSpacing.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            OutlinedTextField(
                                value = state.config.targetSize.customValue,
                                onValueChange = onCustomTarget,
                                label = { Text(stringResource(R.string.target_size)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = !state.targetValidation.isValid,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                TargetSizeUnit.entries.forEach { unit ->
                                    FilterChip(
                                        selected = state.config.targetSize.customUnit == unit,
                                        onClick = { onCustomTargetUnit(unit) },
                                        label = { Text(unit.name) },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(AppSpacing.xs))
                        Text(
                            stringResource(R.string.target_size_note),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    state.targetValidation.message?.let {
                        Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                ConfigCard(title = stringResource(R.string.quality_mode)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompressionMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.config.compressionMode == mode,
                                onClick = { onCompressionMode(mode) },
                                label = { Text(mode.localizedTitle()) },
                            )
                        }
                    }
                    Spacer(Modifier.height(AppSpacing.xs))
                    Text(
                        text = state.config.compressionMode.localizedDescription(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                ConfigCard(title = stringResource(R.string.resize)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ResizeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.config.resize.mode == mode,
                                onClick = { onResizeMode(mode) },
                                label = { Text(mode.localizedLabel()) },
                            )
                        }
                    }
                    if (state.config.resize.mode == ResizeMode.CUSTOM) {
                        Spacer(Modifier.height(AppSpacing.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            OutlinedTextField(
                                value = state.config.resize.customWidth,
                                onValueChange = onCustomWidth,
                                label = { Text(stringResource(R.string.width)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = state.config.resize.customHeight,
                                onValueChange = onCustomHeight,
                                label = { Text(stringResource(R.string.height)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }
                        ToggleRow(
                            label = stringResource(R.string.maintain_aspect_ratio),
                            checked = state.config.resize.maintainAspectRatio,
                            onCheckedChange = onMaintainAspect,
                        )
                        ToggleRow(
                            label = stringResource(R.string.allow_upscaling),
                            checked = state.config.resize.allowUpscale,
                            onCheckedChange = onAllowUpscale,
                        )
                    }
                    state.resizeValidation.message?.let {
                        Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                ConfigCard(title = stringResource(R.string.output_format)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(ImageFormat.JPEG, ImageFormat.PNG, ImageFormat.WEBP).forEach { format ->
                            FormatChip(
                                format = format,
                                selected = state.config.outputFormat == format,
                                onClick = { onFormat(format) },
                            )
                        }
                    }
                    Spacer(Modifier.height(AppSpacing.xs))
                    Text(
                        text = stringResource(R.string.low_size_format_tip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    state.alphaToJpegWarning?.let {
                        Spacer(Modifier.height(AppSpacing.xs))
                        Text(it.asString(), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(AppSpacing.xs))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(stringResource(R.string.jpg_background), style = MaterialTheme.typography.bodySmall)
                            listOf(
                                0xFFFFFFFF.toInt(),
                                0xFF000000.toInt(),
                                0xFF2F80ED.toInt(),
                                0xFFE53935.toInt(),
                                0xFF43A047.toInt(),
                                0xFFFDD835.toInt(),
                                0xFF9E9E9E.toInt(),
                                0xFFFF9800.toInt(),
                                0xFF9C27B0.toInt(),
                                0xFF00ACC1.toInt(),
                                0xFF795548.toInt(),
                                0xFFF5F5F5.toInt(),
                                0xFF263238.toInt(),
                                0xFFFFCDD2.toInt(),
                                0xFFFFE0B2.toInt(),
                                0xFFFFF9C4.toInt(),
                                0xFFC8E6C9.toInt(),
                                0xFFB2EBF2.toInt(),
                                0xFFD1C4E9.toInt(),
                                0xFFF8BBD0.toInt(),
                                0xFFB0BEC5.toInt(),
                            ).forEach { color ->
                                ColorSwatch(
                                    color = color,
                                    selected = state.config.jpegBackgroundColor == color,
                                    onClick = { onJpegBackgroundColor(color) },
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    PremiumPrimaryButton(
                        text = stringResource(R.string.compress_image),
                        onClick = onCompress,
                        enabled = state.selectedImages.isNotEmpty() &&
                            state.targetValidation.isValid &&
                            state.resizeValidation.isValid &&
                            !state.batch.isRunning,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.PlayArrow,
                    )
                    PremiumOutlinedButton(
                        text = stringResource(R.string.remove_background),
                        onClick = onRemoveBackground,
                        enabled = state.selectedImages.size == 1 && !state.batch.isRunning,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.AutoFixHigh,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    PremiumCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(AppSpacing.sm))
        content()
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorSwatch(
    color: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val colorDescription = stringResource(
        R.string.background_color_accessibility,
        color.accessibleColorName(),
    )
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(AppTouchTargets.min)
            .clip(CircleShape)
            .background(Color(color))
            .border(2.dp, borderColor, CircleShape)
            .semantics {
                role = Role.Button
                contentDescription = colorDescription
                this.selected = selected
            }
            .clickable(onClick = onClick),
    )
}

@Composable
private fun Int.accessibleColorName(): String {
    return when (this) {
        0xFFFFFFFF.toInt(), 0xFFF5F5F5.toInt() -> stringResource(R.string.color_white)
        0xFF000000.toInt() -> stringResource(R.string.color_black)
        0xFF2F80ED.toInt() -> stringResource(R.string.color_blue)
        0xFFE53935.toInt(), 0xFFFFCDD2.toInt() -> stringResource(R.string.color_red)
        0xFF43A047.toInt(), 0xFFC8E6C9.toInt() -> stringResource(R.string.color_green)
        0xFFFDD835.toInt(), 0xFFFFF9C4.toInt() -> stringResource(R.string.color_yellow)
        0xFFFF9800.toInt(), 0xFFFFE0B2.toInt() -> stringResource(R.string.color_orange)
        0xFF9C27B0.toInt(), 0xFFD1C4E9.toInt() -> stringResource(R.string.color_purple)
        0xFF00ACC1.toInt(), 0xFFB2EBF2.toInt() -> stringResource(R.string.color_cyan)
        0xFF795548.toInt() -> stringResource(R.string.color_brown)
        0xFFF8BBD0.toInt() -> stringResource(R.string.color_pink)
        0xFF9E9E9E.toInt(), 0xFF263238.toInt(), 0xFFB0BEC5.toInt() -> stringResource(R.string.color_gray)
        else -> stringResource(R.string.custom)
    }
}

@Composable
private fun TargetSizePreset.localizedLabel(): String {
    return bytes?.let { Formatter.formatFileSize(LocalContext.current, it) }
        ?: stringResource(R.string.custom)
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
private fun CompressionMode.localizedDescription(): String {
    return stringResource(
        when (this) {
            CompressionMode.BEST_QUALITY -> R.string.quality_best_description
            CompressionMode.BALANCED -> R.string.quality_balanced_description
            CompressionMode.SMALLEST_SIZE -> R.string.quality_smallest_description
        },
    )
}

@Composable
private fun ResizeMode.localizedLabel(): String {
    return when (this) {
        ResizeMode.ORIGINAL -> stringResource(R.string.original)
        ResizeMode.PERCENT_25 -> localizedPercent(0.25)
        ResizeMode.PERCENT_50 -> localizedPercent(0.50)
        ResizeMode.PERCENT_75 -> localizedPercent(0.75)
        ResizeMode.CUSTOM -> stringResource(R.string.custom)
    }
}

@Composable
private fun localizedPercent(value: Double): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        NumberFormat.getPercentInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
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
