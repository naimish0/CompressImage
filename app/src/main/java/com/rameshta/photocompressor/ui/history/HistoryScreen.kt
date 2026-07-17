package com.rameshta.photocompressor.ui.history

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.InlineHistoryAd
import com.rameshta.photocompressor.ui.components.ProcessedImageCard

typealias HistoryItem = ProcessedImage

sealed interface HistoryListItem {
    data class Content(
        val item: HistoryItem,
    ) : HistoryListItem

    data class Ad(
        val placementKey: String,
    ) : HistoryListItem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<ProcessedImage>,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    onShareItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClear: () -> Unit,
) {
    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = onClear, enabled = history.isNotEmpty()) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Clear history")
                    }
                },
            )
        },
    ) {
        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("No processed images yet.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Compressed images appear here after a successful operation.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val listItems = historyListItems(history)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    listItems,
                    key = { item ->
                        when (item) {
                            is HistoryListItem.Ad -> item.placementKey
                            is HistoryListItem.Content -> "history-${item.item.id}"
                        }
                    },
                ) { listItem ->
                    when (listItem) {
                        is HistoryListItem.Ad -> InlineHistoryAd(
                            placementKey = listItem.placementKey,
                            bannerAdController = bannerAdController,
                            hidden = fullScreenAdVisible,
                        )
                        is HistoryListItem.Content -> {
                            val item = listItem.item
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProcessedImageCard(image = item, selected = false, onClick = { onOpenItem(item.id) })
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(onClick = { onOpenItem(item.id) }, modifier = Modifier.weight(1f)) {
                                        Text("Open")
                                    }
                                    OutlinedButton(onClick = { onShareItem(item.id) }, modifier = Modifier.weight(1f)) {
                                        Text("Share")
                                    }
                                    OutlinedButton(onClick = { onRemoveItem(item.id) }, modifier = Modifier.weight(1f)) {
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun historyListItems(
    history: List<HistoryItem>,
    interval: Int = INLINE_AD_INTERVAL,
): List<HistoryListItem> {
    if (history.isEmpty() || interval <= 0) return history.map { HistoryListItem.Content(it) }
    return buildList {
        history.forEachIndexed { index, item ->
            add(HistoryListItem.Content(item))
            val position = index + 1
            val hasMoreItems = position < history.size
            if (position % interval == 0 && hasMoreItems) {
                add(HistoryListItem.Ad("history-inline-after-${item.id}"))
            }
        }
    }
}

private const val INLINE_AD_INTERVAL = 5
