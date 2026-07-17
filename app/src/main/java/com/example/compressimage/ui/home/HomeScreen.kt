package com.example.compressimage.ui.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.compressimage.ads.BannerAdController
import com.example.compressimage.ads.BannerPlacement
import com.example.compressimage.ui.PhotoCompressorUiState
import com.example.compressimage.ui.components.AdScreenScaffold
import com.example.compressimage.ui.components.EmptySpaceBannerAd
import com.example.compressimage.ui.components.SelectedImageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: PhotoCompressorUiState,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onAddImages: (List<String>) -> Unit,
    onRemoveImage: (String) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGE_SELECTION),
    ) { uris: List<Uri> ->
        onAddImages(uris.map { it.toString() })
    }

    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        hasBottomContent = state.selectedImages.isNotEmpty(),
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
        bottomContent = {
            if (state.selectedImages.isNotEmpty()) {
                Button(
                    onClick = onOpenEditor,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Preview and configure")
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
                                picker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Choose JPG, PNG, or WEBP")
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
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Choose JPG, PNG, or WEBP")
                    }
                }
                item {
                    EmptySpaceBannerAd(
                        placement = BannerPlacement.HOME_EMPTY_SPACE,
                        bannerAdController = bannerAdController,
                        hidden = fullScreenAdVisible,
                    )
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

private const val MAX_IMAGE_SELECTION = 50
