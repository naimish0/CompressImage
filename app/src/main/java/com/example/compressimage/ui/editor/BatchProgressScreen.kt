package com.example.compressimage.ui.editor

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.compressimage.ads.BannerAdController
import com.example.compressimage.ui.BatchItemStatus
import com.example.compressimage.ui.PhotoCompressorUiState
import com.example.compressimage.ui.components.AdScreenScaffold
import com.example.compressimage.ui.components.BatchStatusCard
import com.example.compressimage.ui.percentText

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
        hasBottomContent = true,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.batch.isRunning) "Compressing" else "Batch summary") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !state.batch.isRunning) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Go back")
                    }
                },
            )
        },
        bottomContent = {
            Column(
                Modifier,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.batch.isRunning) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Cancel, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel")
                    }
                } else {
                    if (failedCount > 0) {
                        OutlinedButton(
                            onClick = onRetryFailed,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry failed")
                        }
                    }
                    Button(
                        onClick = onViewResults,
                        enabled = successCount > 0,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Visibility, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compare results")
                    }
                }
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total progress", fontWeight = FontWeight.SemiBold)
                            Text(state.totalProgress.percentText())
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.totalProgress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        state.batch.summary?.let { summary ->
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Done: ${summary.successful} • Failed: ${summary.failed}" +
                                    if (summary.cancelled) " • Cancelled" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            items(state.batch.items, key = { it.imageId }) { item ->
                BatchStatusCard(item = item)
            }
        }
    }
}
