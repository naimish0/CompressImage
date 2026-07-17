package com.rameshta.photocompressor.ui.background

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.domain.model.BackgroundReplacementConfig
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.ui.BackgroundUiState
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.FormatChip
import com.rameshta.photocompressor.ui.components.InlineNativeAdvancedAd
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumOutlinedButton
import com.rameshta.photocompressor.ui.components.PremiumPrimaryButton
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.theme.AppShapes
import com.rameshta.photocompressor.ui.theme.AppSpacing
import com.rameshta.photocompressor.ui.theme.AppTouchTargets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundReplacementScreen(
    state: PhotoCompressorUiState,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onBack: () -> Unit,
    onStartRemoval: () -> Unit,
    onCancelRemoval: () -> Unit,
    onReplaceBackground: (BackgroundReplacementConfig) -> Unit,
) {
    var selectedColor by rememberSaveable { mutableStateOf<Int?>(null) }
    var outputFormat by rememberSaveable { mutableStateOf(ImageFormat.PNG) }
    var red by rememberSaveable { mutableFloatStateOf(0.0f) }
    var green by rememberSaveable { mutableFloatStateOf(0.42f) }
    var blue by rememberSaveable { mutableFloatStateOf(0.85f) }
    val customColor = Color(red, green, blue).toArgb()
    val exportInProgress = state.backgroundReplaceProgress != null

    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        hasBottomContent = state.backgroundState is BackgroundUiState.Running,
        topBar = {
            PremiumTopAppBar(
                title = "Background",
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack,
            )
        },
        bottomContent = {
            if (state.backgroundState is BackgroundUiState.Running) {
                Column(
                    Modifier,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    PremiumOutlinedButton(
                        text = "Cancel",
                        onClick = onCancelRemoval,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                when (val backgroundState = state.backgroundState) {
                    BackgroundUiState.Idle -> StartRemovalCard(onStartRemoval)
                    is BackgroundUiState.Running -> RunningCard(
                        progress = backgroundState.progress,
                        stage = backgroundState.stage.label,
                    )
                    BackgroundUiState.Cancelled -> MessageCard(
                        title = "Background removal cancelled",
                        message = "The original image was not changed. Start again when you are ready.",
                        isError = false,
                    )
                    is BackgroundUiState.Unavailable -> MessageCard(
                        title = "Background removal unavailable",
                        message = backgroundState.reason,
                        isError = false,
                    )
                    is BackgroundUiState.Error -> MessageCard(
                        title = "Background removal failed",
                        message = backgroundState.message,
                        isError = true,
                    )
                    is BackgroundUiState.Success -> {
                        val transparent = selectedColor == null
                        PremiumCard {
                            Text("Preview", style = MaterialTheme.typography.titleMedium)
                            backgroundState.image.warning?.let { warning ->
                                Text(
                                    warning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(AppShapes.medium)
                                    .then(if (transparent) Modifier.checkerboard() else Modifier.background(Color(selectedColor!!))),
                            ) {
                                AsyncImage(
                                    model = backgroundState.image.filePath,
                                    contentDescription = if (transparent) {
                                        "Transparent background preview"
                                    } else {
                                        "Image preview with selected background color"
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                            Text("Replacement color", style = MaterialTheme.typography.titleSmall)
                            ColorChoices(
                                selectedColor = selectedColor,
                                customColor = customColor,
                                onColor = { color ->
                                    selectedColor = color
                                    if (color == null && !outputFormat.supportsTransparency) {
                                        outputFormat = ImageFormat.PNG
                                    }
                                },
                            )
                            Text("Custom color", style = MaterialTheme.typography.titleSmall)
                            ColorSlider("Red", red) { value ->
                                red = value
                                selectedColor = Color(value, green, blue).toArgb()
                            }
                            ColorSlider("Green", green) { value ->
                                green = value
                                selectedColor = Color(red, value, blue).toArgb()
                            }
                            ColorSlider("Blue", blue) { value ->
                                blue = value
                                selectedColor = Color(red, green, value).toArgb()
                            }
                            Text("Export format", style = MaterialTheme.typography.titleSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                listOf(ImageFormat.PNG, ImageFormat.WEBP, ImageFormat.JPEG)
                                    .filter { selectedColor != null || it.supportsTransparency }
                                    .forEach { format ->
                                        FormatChip(
                                            format = format,
                                            selected = outputFormat == format,
                                            onClick = { outputFormat = format },
                                        )
                                    }
                            }
                            state.backgroundReplaceProgress?.let { progress ->
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            PremiumPrimaryButton(
                                text = if (exportInProgress) "Exporting..." else "Export background",
                                onClick = {
                                    onReplaceBackground(
                                        BackgroundReplacementConfig(
                                            colorArgb = selectedColor,
                                            outputFormat = outputFormat,
                                        ),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !exportInProgress,
                                loading = exportInProgress,
                                icon = Icons.Outlined.Save,
                            )
                        }
                    }
                }
            }
            if (state.backgroundState !is BackgroundUiState.Success) {
                item {
                    InlineNativeAdvancedAd(
                        bannerAdController = bannerAdController,
                        hidden = fullScreenAdVisible,
                    )
                }
            }
        }
    }
}

@Composable
private fun StartRemovalCard(onStartRemoval: () -> Unit) {
    PremiumCard {
        Text("Remove background", style = MaterialTheme.typography.titleMedium)
        Text(
            "Runs on this device and exports a transparent PNG. Images are not uploaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PremiumPrimaryButton(
            text = "Start removal",
            onClick = onStartRemoval,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.AutoFixHigh,
        )
    }
}

@Composable
private fun RunningCard(
    progress: Float,
    stage: String,
) {
    PremiumCard {
        Text("Processing", style = MaterialTheme.typography.titleMedium)
        Text(stage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun MessageCard(title: String, message: String, isError: Boolean) {
    PremiumCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = message,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColorChoices(
    selectedColor: Int?,
    customColor: Int,
    onColor: (Int?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            FilterChip(
                selected = selectedColor == null,
                onClick = { onColor(null) },
                label = { Text("Transparent") },
            )
            FilterChip(
                selected = selectedColor == customColor,
                onClick = { onColor(customColor) },
                label = { Text("Custom") },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            listOf(
                0xFFFFFFFF.toInt(),
                0xFF000000.toInt(),
                0xFF2F80ED.toInt(),
                0xFFE53935.toInt(),
                0xFF43A047.toInt(),
                0xFFFDD835.toInt(),
                0xFF9E9E9E.toInt(),
            ).forEach { color ->
                val colorName = color.accessibleColorName()
                val isSelected = selectedColor == color
                Box(
                    modifier = Modifier
                        .size(AppTouchTargets.min)
                        .clip(CircleShape)
                        .background(Color(color))
                        .border(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = "$colorName background"
                            selected = isSelected
                        }
                        .clickable { onColor(color) },
                )
            }
        }
    }
}

@Composable
private fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Slider(value = value, onValueChange = onValueChange)
    }
}

private fun Modifier.checkerboard(): Modifier = drawBehind {
    val square = 22.dp.toPx()
    val rows = (size.height / square).toInt() + 1
    val columns = (size.width / square).toInt() + 1
    for (row in 0..rows) {
        for (column in 0..columns) {
            val color = if ((row + column) % 2 == 0) Color(0xFFE4E7E7) else Color(0xFFFFFFFF)
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(column * square, row * square),
                size = Size(square, square),
            )
        }
    }
}

private fun Int.accessibleColorName(): String {
    return when (this) {
        0xFFFFFFFF.toInt() -> "White"
        0xFF000000.toInt() -> "Black"
        0xFF2F80ED.toInt() -> "Blue"
        0xFFE53935.toInt() -> "Red"
        0xFF43A047.toInt() -> "Green"
        0xFFFDD835.toInt() -> "Yellow"
        0xFF9E9E9E.toInt() -> "Gray"
        else -> "Custom"
    }
}
