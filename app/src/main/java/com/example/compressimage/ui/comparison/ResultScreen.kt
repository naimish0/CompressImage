package com.example.compressimage.ui.comparison

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.compressimage.ads.BannerAdController
import com.example.compressimage.ads.BannerPlacement
import com.example.compressimage.domain.model.ProcessedImage
import com.example.compressimage.ui.PendingAdAction
import com.example.compressimage.ui.PhotoCompressorUiState
import com.example.compressimage.ui.components.AdScreenScaffold
import com.example.compressimage.ui.components.EmptySpaceBannerAd
import com.example.compressimage.ui.components.ImagePreviewBox
import com.example.compressimage.ui.components.InfoRow
import com.example.compressimage.ui.components.ProcessedImageCard
import com.example.compressimage.util.CompressionStatsCalculator
import com.example.compressimage.util.FileSizeFormatter
import java.util.Locale

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
    var requestedName by rememberSaveable(selected?.id) { mutableStateOf(selected?.displayName.orEmpty()) }
    val actionInProgress = state.isSaving || state.pendingAdAction !is PendingAdAction.None

    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Compare result") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Go back")
                    }
                },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ComparisonPanel(selected)
            }

            if (state.results.size > 1) {
                item {
                    Text("Batch results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                        text = warning,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Save and share", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = requestedName,
                            onValueChange = { requestedName = it },
                            label = { Text("Output filename") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onSaveSelected(requestedName) },
                                enabled = !actionInProgress,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.Save, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (state.isSaving) "Saving..." else "Save")
                            }
                            OutlinedButton(
                                onClick = onShareSelected,
                                enabled = !actionInProgress,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Share")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = onOpenImage,
                                enabled = !actionInProgress,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Open")
                            }
                            OutlinedButton(onClick = onCompressAnother, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Another")
                            }
                        }
                        if (state.results.size > 1) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = onSaveAll,
                                    enabled = !actionInProgress,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("Save all")
                                }
                                OutlinedButton(
                                    onClick = onShareAll,
                                    enabled = !actionInProgress,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("Share all")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonPanel(image: ProcessedImage) {
    BoxWithConstraints {
        if (maxWidth > 700.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PreviewColumn("Original", image.original.uriString, modifier = Modifier.weight(1f))
                PreviewColumn("Processed", image.filePath, modifier = Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewColumn("Original", image.original.uriString)
                PreviewColumn("Processed", image.filePath)
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
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        ImagePreviewBox(model = model, contentDescription = "$label image preview")
    }
}

@Composable
private fun StatsPanel(image: ProcessedImage) {
    val stats = CompressionStatsCalculator.calculate(image.original.sizeBytes, image.sizeBytes)
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            InfoRow("Original size", FileSizeFormatter.format(stats.originalSizeBytes))
            InfoRow("Processed size", FileSizeFormatter.format(stats.processedSizeBytes))
            image.requestedTargetBytes?.let { target ->
                InfoRow("Requested target", FileSizeFormatter.format(target))
            }
            image.targetReached?.let { reached ->
                InfoRow(
                    "Target status",
                    if (reached) {
                        "Reached within tolerance"
                    } else {
                        "Not safely achievable"
                    },
                )
            }
            if (stats.savedBytes >= 0) {
                InfoRow("Space saved", FileSizeFormatter.format(stats.savedBytes))
                InfoRow("Percentage saved", String.format(Locale.US, "%.1f%%", stats.percentageSaved))
            } else {
                InfoRow("Size increase", FileSizeFormatter.format(-stats.savedBytes))
                InfoRow("Percentage saved", "0%")
            }
            InfoRow("Compression ratio", String.format(Locale.US, "%.2f:1", stats.compressionRatio))
            InfoRow("Original resolution", image.original.resolutionLabel)
            InfoRow("Processed resolution", image.resolutionLabel)
            InfoRow("Output format", image.format.displayName)
            image.compressionMode?.let {
                InfoRow("Quality mode", it.title)
            }
            InfoRow("Output quality", image.outputQuality?.toString() ?: "Lossless")
        }
    }
}

@Composable
private fun EmptyResult(
    modifier: Modifier = Modifier,
    onCompressAnother: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No compressed result is available.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCompressAnother, modifier = Modifier.fillMaxWidth()) {
            Text("Select an image")
        }
    }
}
