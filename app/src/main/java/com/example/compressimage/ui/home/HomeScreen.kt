package com.example.compressimage.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.compressimage.ui.PhotoCompressorUiState
import com.example.compressimage.ui.components.BannerAd
import com.example.compressimage.ui.components.SelectedImageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: PhotoCompressorUiState,
    onAddImages: (List<String>) -> Unit,
    onRemoveImage: (String) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50),
    ) { uris: List<Uri> ->
        persistReadAccessIfAvailable(context.contentResolver, uris)
        onAddImages(uris.map { it.toString() })
    }
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris: List<Uri> ->
        persistReadAccessIfAvailable(context.contentResolver, uris)
        onAddImages(uris.map { it.toString() })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Photo Compressor") },
                navigationIcon = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Outlined.History, contentDescription = "Open history")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Open settings")
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (state.selectedImages.isNotEmpty()) {
                    Button(
                        onClick = onOpenEditor,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Tune, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Preview and configure")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                BannerAd()
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (state.selectedImages.isEmpty()) {
                            "Select images to compress, resize, convert, save, or share."
                        } else {
                            "${state.selectedImages.size} image${if (state.selectedImages.size == 1) "" else "s"} selected"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                picker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.selectedImages.isEmpty()) "Select images" else "Add more")
                        }
                        OutlinedButton(
                            onClick = {
                                documentPicker.launch(
                                    arrayOf("image/jpeg", "image/png", "image/webp"),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Browse files")
                        }
                        if (state.selectedImages.isNotEmpty()) {
                            OutlinedButton(
                                onClick = onOpenEditor,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Configure")
                            }
                        }
                    }
                }
            }

            if (state.isLoadingSelection) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Reading image details",
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }

            state.selectionError?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (state.selectedImages.isEmpty() && !state.isLoadingSelection) {
                item {
                    FilledTonalButton(
                        onClick = {
                            documentPicker.launch(
                                arrayOf("image/jpeg", "image/png", "image/webp"),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Choose JPG, PNG, or WEBP")
                    }
                }
            }

            items(state.selectedImages, key = { it.id }) { image ->
                SelectedImageCard(
                    image = image,
                    onRemove = { onRemoveImage(image.id) },
                )
            }
        }
    }
}

private fun persistReadAccessIfAvailable(
    contentResolver: android.content.ContentResolver,
    uris: List<Uri>,
) {
    uris.forEach { uri ->
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }
}
