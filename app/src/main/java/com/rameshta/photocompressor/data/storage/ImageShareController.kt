package com.rameshta.photocompressor.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.rameshta.photocompressor.BuildConfig
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.domain.model.ProcessedImage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ShareableImage(
    val uri: Uri,
    val mimeType: String,
)

@Singleton
class ImageShareController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun contentUriFor(image: ProcessedImage): Uri {
        val existingUri = image.filePath.toUri()
        if (existingUri.scheme == "content") {
            return requireAllowedContentUri(existingUri)
        }
        val file = requireAvailableFile(image)
        return FileProvider.getUriForFile(
            context,
            FILE_PROVIDER_AUTHORITY,
            file,
        )
    }

    fun shareableOutputFor(image: ProcessedImage): ShareableImage {
        return ShareableImage(
            uri = contentUriFor(image),
            mimeType = image.mimeType,
        )
    }

    fun shareIntent(uri: Uri, mimeType: String): Intent {
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            context.getString(R.string.share_results_chooser),
        )
    }

    fun shareOneIntent(image: ProcessedImage): Intent {
        val shareable = shareableOutputFor(image)
        return shareIntent(shareable.uri, shareable.mimeType)
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
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, image.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun requireAvailableFile(image: ProcessedImage): File {
        val file = File(image.filePath)
        require(file.isFile && file.length() > 0L) {
            "The image is no longer available."
        }
        return file
    }

    private fun requireAllowedContentUri(uri: Uri): Uri {
        val authority = uri.authority.orEmpty()
        require(authority == MediaStore.AUTHORITY || authority == FILE_PROVIDER_AUTHORITY) {
            "The image is no longer available."
        }
        return uri
    }

    private companion object {
        val FILE_PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
    }
}
