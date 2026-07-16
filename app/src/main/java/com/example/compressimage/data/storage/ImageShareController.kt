package com.example.compressimage.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.compressimage.R
import com.example.compressimage.domain.model.ProcessedImage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageShareController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun contentUriFor(image: ProcessedImage): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(image.filePath),
        )
    }

    fun shareOneIntent(image: ProcessedImage): Intent {
        val uri = contentUriFor(image)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = image.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            context.getString(R.string.share_results_chooser),
        )
    }

    fun shareManyIntent(images: List<ProcessedImage>): Intent {
        val uris = ArrayList(images.map { contentUriFor(it) })
        val mimeType = images.firstOrNull()?.mimeType ?: "image/*"
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = if (images.map { it.mimeType }.distinct().size == 1) mimeType else "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            context.getString(R.string.share_results_chooser),
        )
    }

    fun openIntent(image: ProcessedImage): Intent {
        val uri = contentUriFor(image)
        return Intent.createChooser(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, image.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            context.getString(R.string.open_image_chooser),
        )
    }
}
