package com.rameshta.photocompressor.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ui.asString
import com.rameshta.photocompressor.ui.components.AdScreenScaffold
import com.rameshta.photocompressor.ui.components.PremiumEmptyState
import com.rameshta.photocompressor.ui.components.PremiumErrorState
import com.rameshta.photocompressor.ui.components.PremiumIconButton
import com.rameshta.photocompressor.ui.components.PremiumLoadingState
import com.rameshta.photocompressor.ui.components.PremiumOutlinedButton
import com.rameshta.photocompressor.ui.components.PremiumPrimaryButton
import com.rameshta.photocompressor.ui.components.PremiumTopAppBar
import com.rameshta.photocompressor.ui.components.ProcessedImageCard
import com.rameshta.photocompressor.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    onShareItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
) {
    val history = (state as? HistoryUiState.Content)?.items.orEmpty()
    AdScreenScaffold(
        bannerAdController = bannerAdController,
        fullScreenAdVisible = fullScreenAdVisible,
        topBar = {
            PremiumTopAppBar(
                title = stringResource(R.string.history),
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack,
                actions = {
                    PremiumIconButton(
                        onClick = onClear,
                        icon = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.cd_clear_history),
                        enabled = history.isNotEmpty(),
                        danger = history.isNotEmpty(),
                    )
                },
            )
        },
    ) {
        when (state) {
            HistoryUiState.Loading -> HistoryLoadingState()
            HistoryUiState.Empty -> HistoryEmptyState()
            is HistoryUiState.Error -> HistoryErrorState(
                message = state.message.asString(),
                onRetry = onRetry,
            )
            is HistoryUiState.Content -> {
                val listItems = historyListItems(history)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    items(
                        listItems,
                        key = { item -> "history-${item.id}" },
                    ) { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            ProcessedImageCard(image = item, selected = false, onClick = { onOpenItem(item.id) })
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
                                PremiumPrimaryButton(
                                    text = stringResource(R.string.open),
                                    onClick = { onOpenItem(item.id) },
                                    modifier = Modifier.weight(1f),
                                )
                                PremiumOutlinedButton(
                                    text = stringResource(R.string.share),
                                    onClick = { onShareItem(item.id) },
                                    modifier = Modifier.weight(1f),
                                )
                                PremiumOutlinedButton(
                                    text = stringResource(R.string.remove),
                                    onClick = { onRemoveItem(item.id) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryLoadingState() {
    PremiumLoadingState(title = stringResource(R.string.loading_history))
}

@Composable
private fun HistoryEmptyState() {
    PremiumEmptyState(
        title = stringResource(R.string.history_empty_title),
        message = stringResource(R.string.history_empty_message),
    )
}

@Composable
private fun HistoryErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    PremiumErrorState(
        title = stringResource(R.string.history_load_error_title),
        message = message,
        onRetry = onRetry,
    )
}

internal fun historyListItems(
    history: List<HistoryItem>,
): List<HistoryItem> {
    return history
}
