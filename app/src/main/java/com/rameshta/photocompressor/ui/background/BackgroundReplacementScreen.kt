package com.rameshta.photocompressor.ui.background

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.domain.model.BackgroundReplacementConfig
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.ui.BackgroundUiState
import com.rameshta.photocompressor.ui.BackgroundProcessingStage
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
import com.rameshta.photocompressor.ui.asString
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.FormatChip
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
        showTopBanner = true,
        showBottomBanner = true,
        hasBottomContent = state.backgroundState is BackgroundUiState.Running,
        topBar = {
            PremiumTopAppBar(
                title = stringResource(R.string.background),
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
                        text = stringResource(R.string.cancel),
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
                        stage = backgroundState.stage.localizedLabel(),
                    )
                    BackgroundUiState.Cancelled -> MessageCard(
                        title = stringResource(R.string.background_removal_cancelled),
                        message = stringResource(R.string.background_cancelled_message),
                        isError = false,
                    )
                    is BackgroundUiState.Unavailable -> MessageCard(
                        title = stringResource(R.string.background_removal_unavailable),
                        message = backgroundState.reason.asString(),
                        isError = false,
                    )
                    is BackgroundUiState.Error -> MessageCard(
                        title = stringResource(R.string.background_removal_failed),
                        message = backgroundState.message.asString(),
                        isError = true,
                    )
                    is BackgroundUiState.Success -> {
                        val transparent = selectedColor == null
                        PremiumCard {
                            Text(stringResource(R.string.preview), style = MaterialTheme.typography.titleMedium)
                            backgroundState.image.warning?.let { warning ->
                                Text(
                                    warning.asString(),
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
                                        stringResource(R.string.transparent_background_preview)
                                    } else {
                                        stringResource(R.string.selected_background_preview)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                            Text(stringResource(R.string.replacement_color), style = MaterialTheme.typography.titleSmall)
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
                            Text(stringResource(R.string.custom_color), style = MaterialTheme.typography.titleSmall)
                            ColorSlider(stringResource(R.string.color_red), red) { value ->
                                red = value
                                selectedColor = Color(value, green, blue).toArgb()
                            }
                            ColorSlider(stringResource(R.string.color_green), green) { value ->
                                green = value
                                selectedColor = Color(red, value, blue).toArgb()
                            }
                            ColorSlider(stringResource(R.string.color_blue), blue) { value ->
                                blue = value
                                selectedColor = Color(red, green, value).toArgb()
                            }
                            Text(stringResource(R.string.export_format), style = MaterialTheme.typography.titleSmall)
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
                                text = if (exportInProgress) {
                                    stringResource(R.string.exporting)
                                } else {
                                    stringResource(R.string.export_background)
                                },
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

        }
    }
}

@Composable
private fun StartRemovalCard(onStartRemoval: () -> Unit) {
    PremiumCard {
        Text(stringResource(R.string.remove_background), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.background_offline_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PremiumPrimaryButton(
            text = stringResource(R.string.start_removal),
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
        Text(stringResource(R.string.processing), style = MaterialTheme.typography.titleMedium)
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
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            FilterChip(
                selected = selectedColor == null,
                onClick = { onColor(null) },
                label = { Text(stringResource(R.string.transparent)) },
            )
            FilterChip(
                selected = selectedColor == customColor,
                onClick = { onColor(customColor) },
                label = { Text(stringResource(R.string.custom)) },
            )
        }
        val palette = listOf(
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
            )
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            palette.chunked(5).forEach { rowColors ->
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    rowColors.forEach { color ->
                        val colorName = color.accessibleColorName()
                        val colorDescription = stringResource(
                            R.string.background_color_accessibility,
                            colorName,
                        )
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
                                    contentDescription = colorDescription
                                    selected = isSelected
                                }
                                .clickable { onColor(color) },
                        )
                    }
                }
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
private fun BackgroundProcessingStage.localizedLabel(): String = stringResource(
    when (this) {
        BackgroundProcessingStage.PREPARING_IMAGE -> R.string.preparing_image
        BackgroundProcessingStage.REMOVING_BACKGROUND -> R.string.removing_background
        BackgroundProcessingStage.REFINING_EDGES -> R.string.refining_edges
        BackgroundProcessingStage.FINALIZING -> R.string.finalizing
        BackgroundProcessingStage.COMPLETE -> R.string.complete
    },
)
