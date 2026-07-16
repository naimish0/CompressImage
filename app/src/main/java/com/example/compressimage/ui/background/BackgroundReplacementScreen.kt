package com.example.compressimage.ui.background

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.compressimage.domain.model.BackgroundReplacementConfig
import com.example.compressimage.domain.model.ImageFormat
import com.example.compressimage.ui.BackgroundUiState
import com.example.compressimage.ui.PhotoCompressorUiState
import com.example.compressimage.ui.components.FormatChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundReplacementScreen(
    state: PhotoCompressorUiState,
    onBack: () -> Unit,
    onStartRemoval: () -> Unit,
    onReplaceBackground: (BackgroundReplacementConfig) -> Unit,
) {
    var selectedColor by rememberSaveable { mutableStateOf<Int?>(null) }
    var outputFormat by rememberSaveable { mutableStateOf(ImageFormat.PNG) }
    var red by rememberSaveable { mutableFloatStateOf(0.0f) }
    var green by rememberSaveable { mutableFloatStateOf(0.42f) }
    var blue by rememberSaveable { mutableFloatStateOf(0.85f) }
    val customColor = Color(red, green, blue).toArgb()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Background") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Go back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                when (val backgroundState = state.backgroundState) {
                    BackgroundUiState.Idle -> StartRemovalCard(onStartRemoval)
                    is BackgroundUiState.Running -> RunningCard(backgroundState.progress)
                    is BackgroundUiState.Unavailable -> MessageCard(
                        title = "Background removal unavailable",
                        message = backgroundState.reason,
                        isError = false,
                    )
                    is BackgroundUiState.Error -> MessageCard(
                        title = "Background removal failed",
                        message = backgroundState.message,
                        isError = true,
                    )
                    is BackgroundUiState.Success -> {
                        val transparent = selectedColor == null
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(if (transparent) Modifier.checkerboard() else Modifier.background(Color(selectedColor!!))),
                                ) {
                                    AsyncImage(
                                        model = backgroundState.image.filePath,
                                        contentDescription = "Transparent image preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                                Text("Replacement color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                ColorChoices(
                                    selectedColor = selectedColor,
                                    customColor = customColor,
                                    onColor = { selectedColor = it },
                                )
                                Text("Custom color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                ColorSlider("Red", red) { value ->
                                    red = value
                                    selectedColor = Color(value, green, blue).toArgb()
                                }
                                ColorSlider("Green", green) { value ->
                                    green = value
                                    selectedColor = Color(red, value, blue).toArgb()
                                }
                                ColorSlider("Blue", blue) { value ->
                                    blue = value
                                    selectedColor = Color(red, green, value).toArgb()
                                }
                                Text("Export format", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                Button(
                                    onClick = {
                                        onReplaceBackground(
                                            BackgroundReplacementConfig(
                                                colorArgb = selectedColor,
                                                outputFormat = outputFormat,
                                            ),
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Outlined.Save, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Export background")
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
private fun StartRemovalCard(onStartRemoval: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Remove background", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "This workflow is ready for an offline model or configured online provider. Images are not uploaded by this build.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onStartRemoval, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.AutoFixHigh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start")
            }
        }
    }
}

@Composable
private fun RunningCard(progress: Float) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Processing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MessageCard(title: String, message: String, isError: Boolean) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = message,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColorChoices(
    selectedColor: Int?,
    customColor: Int,
    onColor: (Int?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedColor == null,
                onClick = { onColor(null) },
                label = { Text("Transparent") },
            )
            FilterChip(
                selected = selectedColor == customColor,
                onClick = { onColor(customColor) },
                label = { Text("Custom") },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                0xFFFFFFFF.toInt(),
                0xFF000000.toInt(),
                0xFF2F80ED.toInt(),
                0xFFE53935.toInt(),
                0xFF43A047.toInt(),
                0xFFFDD835.toInt(),
                0xFF9E9E9E.toInt(),
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .border(
                            width = 2.dp,
                            color = if (selectedColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        )
                        .clickable { onColor(color) },
                )
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
