package com.example.compressimage.ui.history

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.compressimage.domain.model.ProcessedImage
import com.example.compressimage.ui.components.ProcessedImageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<ProcessedImage>,
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClear: () -> Unit,
) {
    Scaffold(
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
    ) { innerPadding ->
        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(history, key = { it.id }) { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProcessedImageCard(image = item, selected = false, onClick = { onOpenItem(item.id) })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { onOpenItem(item.id) }, modifier = Modifier.weight(1f)) {
                                Text("Open")
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
