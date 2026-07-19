package com.rameshta.photocompressor.ui.components

import android.text.format.Formatter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.domain.model.HistoryOperationType
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.ui.BatchItemStatus
import com.rameshta.photocompressor.ui.BatchItemUiState
import com.rameshta.photocompressor.ui.ProcessingStage
import com.rameshta.photocompressor.ui.asString
import com.rameshta.photocompressor.ui.theme.AppElevation
import com.rameshta.photocompressor.ui.theme.AppIconSizes
import com.rameshta.photocompressor.ui.theme.AppMotion
import com.rameshta.photocompressor.ui.theme.AppSemanticColors
import com.rameshta.photocompressor.ui.theme.AppShapes
import com.rameshta.photocompressor.ui.theme.AppSpacing
import com.rameshta.photocompressor.ui.theme.AppTouchTargets
import java.io.File
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationContentDescription: String? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                PremiumIconButton(
                    onClick = onNavigationClick,
                    icon = navigationIcon,
                    contentDescription = navigationContentDescription ?: stringResource(R.string.cd_go_back),
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && onClick != null) 0.985f else 1f,
        animationSpec = tween(AppMotion.fast),
        label = "premium-card-scale",
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) AppElevation.none else AppElevation.subtle,
        animationSpec = tween(AppMotion.fast),
        label = "premium-card-elevation",
    )
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
    }
    val cardModifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }

    if (onClick != null) {
        Card(
            modifier = cardModifier.semantics { role = Role.Button },
            onClick = onClick,
            enabled = enabled,
            shape = AppShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = BorderStroke(1.dp, borderColor),
            interactionSource = interactionSource,
        ) {
            Column(Modifier.padding(AppSpacing.md), content = content)
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = AppShapes.medium,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = BorderStroke(1.dp, borderColor),
        ) {
            Column(Modifier.padding(AppSpacing.md), content = content)
        }
    }
}

@Composable
fun PremiumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    PremiumButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        icon = icon,
        style = PremiumButtonStyle.Primary,
    )
}

@Composable
fun PremiumSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    PremiumButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        icon = icon,
        style = PremiumButtonStyle.Secondary,
    )
}

@Composable
fun PremiumTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    PremiumButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        icon = icon,
        style = PremiumButtonStyle.Tonal,
    )
}

@Composable
fun PremiumOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    PremiumButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        icon = icon,
        style = PremiumButtonStyle.Outlined,
    )
}

@Composable
fun PremiumDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    PremiumButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        icon = icon,
        style = PremiumButtonStyle.Danger,
    )
}

@Composable
private fun PremiumButtonSurface(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    icon: ImageVector?,
    style: PremiumButtonStyle,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !loading) 0.985f else 1f,
        animationSpec = tween(AppMotion.fast),
        label = "premium-button-scale",
    )
    val buttonModifier = modifier
        .defaultMinSize(minHeight = AppTouchTargets.button)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(AppIconSizes.sm),
                strokeWidth = 2.dp,
                color = when (style) {
                    PremiumButtonStyle.Primary, PremiumButtonStyle.Danger -> MaterialTheme.colorScheme.onPrimary
                    PremiumButtonStyle.Secondary, PremiumButtonStyle.Tonal, PremiumButtonStyle.Outlined -> MaterialTheme.colorScheme.primary
                },
            )
            Spacer(Modifier.width(AppSpacing.xs))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(AppIconSizes.md))
            Spacer(Modifier.width(AppSpacing.xs))
        }
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }

    when (style) {
        PremiumButtonStyle.Primary -> Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = buttonModifier,
            shape = AppShapes.pill,
            contentPadding = PaddingValues(horizontal = AppSpacing.lg),
            interactionSource = interactionSource,
            content = content,
        )
        PremiumButtonStyle.Secondary -> Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = buttonModifier,
            shape = AppShapes.pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = AppSpacing.lg),
            interactionSource = interactionSource,
            content = content,
        )
        PremiumButtonStyle.Tonal -> FilledTonalButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = buttonModifier,
            shape = AppShapes.pill,
            contentPadding = PaddingValues(horizontal = AppSpacing.lg),
            interactionSource = interactionSource,
            content = content,
        )
        PremiumButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = buttonModifier,
            shape = AppShapes.pill,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(horizontal = AppSpacing.lg),
            interactionSource = interactionSource,
            content = content,
        )
        PremiumButtonStyle.Danger -> Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = buttonModifier,
            shape = AppShapes.pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
            contentPadding = PaddingValues(horizontal = AppSpacing.lg),
            interactionSource = interactionSource,
            content = content,
        )
    }
}

@Composable
fun PremiumIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(AppTouchTargets.min),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(AppIconSizes.lg),
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                danger -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
fun PremiumLoadingState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    PremiumStateLayout(
        modifier = modifier,
        icon = Icons.Outlined.HourglassTop,
        title = title,
        message = message,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
fun PremiumEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    PremiumStateLayout(
        modifier = modifier,
        icon = Icons.Outlined.ImageNotSupported,
        title = title,
        message = message,
    ) {
        if (actionLabel != null && onAction != null) {
            PremiumTonalButton(text = actionLabel, onClick = onAction, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun PremiumErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PremiumStateLayout(
        modifier = modifier,
        icon = Icons.Outlined.ErrorOutline,
        title = title,
        message = message,
        iconTint = MaterialTheme.colorScheme.error,
    ) {
        PremiumOutlinedButton(
            text = stringResource(R.string.retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun PremiumSuccessPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(AppShapes.pill)
            .background(AppSemanticColors.success.copy(alpha = 0.14f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = AppSemanticColors.success,
            modifier = Modifier.size(AppIconSizes.sm),
        )
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PremiumStateLayout(
    icon: ImageVector,
    title: String,
    message: String?,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    action: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(AppIconSizes.lg))
        }
        Spacer(Modifier.height(AppSpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (message != null) {
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(AppSpacing.md))
        action()
    }
}

@Composable
fun SelectedImageCard(
    image: ImageInfo,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayName = image.displayName.ifBlank { stringResource(R.string.selected_image) }
    val metadata = stringResource(
        R.string.image_metadata_summary,
        localizedFileSize(image.sizeBytes),
        stringResource(R.string.image_resolution, image.width, image.height),
        image.format.localizedDisplayName(),
    )
    PremiumCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            AsyncImage(
                model = image.uriString,
                contentDescription = stringResource(R.string.preview_of_image, displayName),
                modifier = Modifier
                    .size(76.dp)
                    .clip(AppShapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AppSpacing.xxs))
                Text(
                    text = metadata,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PremiumIconButton(
                onClick = onRemove,
                icon = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.remove_image, displayName),
            )
        }
    }
}

@Composable
fun ProcessedImageCard(
    image: ProcessedImage,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayName = image.displayName.ifBlank { stringResource(R.string.processed_image) }
    val metadata = stringResource(
        R.string.image_metadata_summary,
        localizedFileSize(image.sizeBytes),
        stringResource(R.string.image_resolution, image.width, image.height),
        image.format.localizedDisplayName(),
    )
    val operationTimestamp = stringResource(
        R.string.history_operation_timestamp,
        image.operationType.localizedLabel(),
        formatTimestamp(image.createdTimestamp),
    )
    PremiumCard(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            AsyncImage(
                model = image.filePath,
                contentDescription = stringResource(R.string.processed_preview_of_image, displayName),
                modifier = Modifier
                    .size(76.dp)
                    .clip(AppShapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AppSpacing.xxs))
                Text(
                    text = operationTimestamp,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = metadata,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun FormatChip(
    format: ImageFormat,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        label = { Text(format.localizedDisplayName()) },
        leadingIcon = if (selected) {
            {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        } else {
            null
        },
        shape = AppShapes.pill,
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.merge(TextStyle(fontFeatureSettings = "tnum")),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun BatchStatusCard(
    item: BatchItemUiState,
    modifier: Modifier = Modifier,
) {
    val statusText = when {
        item.status == BatchItemStatus.RUNNING -> item.stage.localizedLabel()
        item.status == BatchItemStatus.FAILED && item.error != null -> item.error.asString()
        else -> item.status.localizedLabel()
    }
    PremiumCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name.ifBlank { stringResource(R.string.selected_image) },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = localizedPercent(item.progress),
                style = MaterialTheme.typography.labelLarge.merge(TextStyle(fontFeatureSettings = "tnum")),
                color = progressColor(item.status),
            )
        }
        Spacer(Modifier.height(AppSpacing.xs))
        LinearProgressIndicator(
            progress = { item.progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ImagePreviewBox(
    model: Any,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fit: Boolean = true,
) {
    AsyncImage(
        model = model.toCoilImageModel(),
        contentDescription = contentDescription,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(AppShapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = if (fit) ContentScale.Fit else ContentScale.Crop,
    )
}

private fun Any.toCoilImageModel(): Any {
    if (this !is String) return this
    val uri = toUri()
    return when {
        uri.scheme == "content" || uri.scheme == "file" -> uri
        startsWith("/") -> File(this)
        else -> this
    }
}

@Composable
private fun progressColor(status: BatchItemStatus): Color {
    return when (status) {
        BatchItemStatus.SUCCESS -> AppSemanticColors.success
        BatchItemStatus.FAILED -> MaterialTheme.colorScheme.error
        BatchItemStatus.CANCELLED -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.primary
    }
}

private enum class PremiumButtonStyle {
    Primary,
    Secondary,
    Tonal,
    Outlined,
    Danger,
}

@Composable
private fun localizedFileSize(bytes: Long): String {
    if (bytes < 0L) return stringResource(R.string.unknown_size)
    return Formatter.formatFileSize(LocalContext.current, bytes)
}

@Composable
private fun formatTimestamp(timestamp: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
        .format(Date(timestamp))
}

@Composable
private fun localizedPercent(value: Float): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        NumberFormat.getPercentInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }
    return formatter.format(value.coerceIn(0f, 1f).toDouble())
}

@Composable
private fun HistoryOperationType.localizedLabel(): String {
    return stringResource(
        when (this) {
            HistoryOperationType.COMPRESSED -> R.string.history_operation_compressed
            HistoryOperationType.BACKGROUND_REMOVED -> R.string.history_operation_background_removed
            HistoryOperationType.RESIZED -> R.string.history_operation_resized
            HistoryOperationType.FORMAT_CONVERTED -> R.string.history_operation_format_converted
            HistoryOperationType.ENHANCED -> R.string.history_operation_enhanced
            HistoryOperationType.COLLAGE -> R.string.history_operation_collage
        },
    )
}

@Composable
private fun BatchItemStatus.localizedLabel(): String {
    return stringResource(
        when (this) {
            BatchItemStatus.QUEUED -> R.string.queued
            BatchItemStatus.RUNNING -> R.string.processing
            BatchItemStatus.SUCCESS -> R.string.done
            BatchItemStatus.FAILED -> R.string.failed
            BatchItemStatus.CANCELLED -> R.string.cancelled
        },
    )
}

@Composable
private fun ProcessingStage.localizedLabel(): String {
    return stringResource(
        when (this) {
            ProcessingStage.READING_IMAGE -> R.string.reading_image
            ProcessingStage.OPTIMIZING_RESOLUTION -> R.string.optimizing_resolution
            ProcessingStage.COMPRESSING -> R.string.compressing
            ProcessingStage.VALIDATING_RESULT -> R.string.validating_result
            ProcessingStage.COMPLETE -> R.string.complete
        },
    )
}

@Composable
private fun ImageFormat.localizedDisplayName(): String {
    return if (this == ImageFormat.UNKNOWN) {
        stringResource(R.string.format_unknown)
    } else {
        displayName
    }
}
