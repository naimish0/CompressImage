package com.rameshta.photocompressor.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ads.BannerPlacement
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.EmptySpaceBannerAd
import com.rameshta.photocompressor.ui.components.InlineNativeAdvancedAd
import com.rameshta.photocompressor.ui.components.PremiumCard
import com.rameshta.photocompressor.ui.components.PremiumIconButton
import com.rameshta.photocompressor.ui.components.PremiumPrimaryButton
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.components.SelectedImageCard
import com.rameshta.photocompressor.ui.theme.AppSpacing

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
    onExternalPickerOpened: () -> Unit = {},
) {
    val visibleSelectedImages = state.visibleSelectedImages
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGE_SELECTION),
    ) { uris: List<Uri> ->
        onAddImages(uris.map { it.toString() })
    }

    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        showTopBanner = true,
        hasBottomContent = visibleSelectedImages.isNotEmpty(),
        topBar = {
            PremiumTopAppBar(
                title = "Photo Compressor",
                navigationIcon = Icons.Outlined.History,
                navigationContentDescription = "Open history",
                onNavigationClick = onOpenHistory,
                actions = {
                    PremiumIconButton(
                        onClick = onOpenSettings,
                        icon = Icons.Outlined.Settings,
                        contentDescription = "Open settings",
                    )
                },
            )
        },
        bottomContent = {
            if (visibleSelectedImages.isNotEmpty()) {
                PremiumPrimaryButton(
                    text = "Preview and configure",
                    onClick = onOpenEditor,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Outlined.Tune,
                )
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
                PremiumCard {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = if (visibleSelectedImages.isEmpty()) {
                            "Compress one image, compress many images, or remove a background."
                        } else {
                            "${visibleSelectedImages.size} image${if (visibleSelectedImages.size == 1) "" else "s"} selected"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Choose JPG, PNG, or WEBP. Your images stay local unless you share or save them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        PremiumPrimaryButton(
                            text = if (visibleSelectedImages.isEmpty()) "Select images" else "Add more",
                            onClick = {
                                onExternalPickerOpened()
                                picker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Outlined.AddPhotoAlternate,
                        )
                    }
                }
            }

            item {
                EmptySpaceBannerAd(
                    placement = BannerPlacement.HOME_EMPTY_SPACE,
                    bannerAdController = bannerAdController,
                    hidden = fullScreenAdVisible,
                )
            }

            item {
                InlineNativeAdvancedAd(
                    bannerAdController = bannerAdController,
                    hidden = fullScreenAdVisible,
                )
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
                            modifier = Modifier.padding(start = AppSpacing.sm),
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

            items(visibleSelectedImages, key = { it.id }) { image ->
                SelectedImageCard(
                    image = image,
                    onRemove = { onRemoveImage(image.id) },
                )
            }
        }
    }
}

private const val MAX_IMAGE_SELECTION = 50
