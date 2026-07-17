package com.example.compressimage.ui.editor

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.compressimage.ads.BannerAdController
import com.example.compressimage.domain.model.CompressionMode
import com.example.compressimage.domain.model.ImageFormat
import com.example.compressimage.domain.model.ResizeMode
import com.example.compressimage.domain.model.TargetSizePreset
import com.example.compressimage.domain.model.TargetSizeUnit
import com.example.compressimage.ui.PhotoCompressorUiState
import com.example.compressimage.ui.components.AdScreenScaffold
import com.example.compressimage.ui.components.FormatChip
import com.example.compressimage.ui.components.InfoRow
import com.example.compressimage.ui.percentLabel
import com.example.compressimage.util.FileSizeFormatter

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
            CenterAlignedTopAppBar(
                title = { Text("Preview and configure") },
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
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Selected images", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        InfoRow("Count", state.selectedImages.size.toString())
                        val totalBytes = state.selectedImages.sumOf { it.sizeBytes }
                        InfoRow("Original total", FileSizeFormatter.format(totalBytes))
                        state.selectedImages.firstOrNull()?.let {
                            InfoRow("First image", "${it.width} x ${it.height} • ${it.format.displayName}")
                        }
                        state.currentOutputDimension?.let {
                            InfoRow("Output resolution", "${it.width} x ${it.height}")
                        }
                    }
                }
            }

            item {
                ConfigCard(title = "Target size") {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TargetSizePreset.entries.forEach { preset ->
                            FilterChip(
                                selected = state.config.targetSize.preset == preset,
                                onClick = { onTargetPreset(preset) },
                                label = { Text(preset.label) },
                            )
                        }
                    }
                    if (state.config.targetSize.preset == TargetSizePreset.CUSTOM) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = state.config.targetSize.customValue,
                                onValueChange = onCustomTarget,
                                label = { Text("Custom size") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = !state.targetValidation.isValid,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                TargetSizeUnit.entries.forEach { unit ->
                                    AssistChip(
                                        onClick = { onCustomTargetUnit(unit) },
                                        label = { Text(unit.name) },
                                    )
                                }
                            }
                        }
                    }
                    state.targetValidation.message?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                ConfigCard(title = "Quality mode") {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompressionMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.config.compressionMode == mode,
                                onClick = { onCompressionMode(mode) },
                                label = { Text(mode.title) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.config.compressionMode.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                ConfigCard(title = "Resize") {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ResizeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.config.resize.mode == mode,
                                onClick = { onResizeMode(mode) },
                                label = { Text(mode.percentLabel()) },
                            )
                        }
                    }
                    if (state.config.resize.mode == ResizeMode.CUSTOM) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = state.config.resize.customWidth,
                                onValueChange = onCustomWidth,
                                label = { Text("Width") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = state.config.resize.customHeight,
                                onValueChange = onCustomHeight,
                                label = { Text("Height") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }
                        ToggleRow(
                            label = "Maintain aspect ratio",
                            checked = state.config.resize.maintainAspectRatio,
                            onCheckedChange = onMaintainAspect,
                        )
                        ToggleRow(
                            label = "Allow upscaling",
                            checked = state.config.resize.allowUpscale,
                            onCheckedChange = onAllowUpscale,
                        )
                    }
                    state.resizeValidation.message?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                ConfigCard(title = "Output format") {
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
                    state.alphaToJpegWarning?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("JPG background", style = MaterialTheme.typography.bodySmall)
                            listOf(
                                0xFFFFFFFF.toInt(),
                                0xFF000000.toInt(),
                                0xFF2F80ED.toInt(),
                                0xFFE53935.toInt(),
                                0xFF43A047.toInt(),
                                0xFFFDD835.toInt(),
                                0xFF9E9E9E.toInt(),
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onCompress,
                        enabled = state.selectedImages.isNotEmpty() &&
                            state.targetValidation.isValid &&
                            state.resizeValidation.isValid &&
                            !state.batch.isRunning,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compress")
                    }
                    OutlinedButton(
                        onClick = onRemoveBackground,
                        enabled = state.selectedImages.size == 1 && !state.batch.isRunning,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.AutoFixHigh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Remove background")
                    }
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
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
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
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(color))
            .border(2.dp, borderColor, CircleShape)
            .clickable(onClick = onClick),
    )
}
