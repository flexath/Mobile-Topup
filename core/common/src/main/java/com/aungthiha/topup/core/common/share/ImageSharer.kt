package com.aungthiha.topup.core.common.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.content.FileProvider
import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Shares an in-memory image through the Android Sharesheet.
 *
 * The image is written to app-private cache storage and exposed via [FileProvider] with a
 * temporary read grant, so no storage permission is needed on any API level.
 */
class ImageSharer(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
) {

    suspend fun share(bitmap: Bitmap, fileNamePrefix: String, chooserTitle: String) {
        val uri = withContext(dispatchers.io) {
            val directory = File(context.cacheDir, SHARED_IMAGES_DIRECTORY).apply { mkdirs() }
            directory.listFiles()?.forEach { it.delete() }

            val file = File(directory, "$fileNamePrefix-${System.currentTimeMillis()}.png")
            val isHardwareBitmap = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                bitmap.config == Bitmap.Config.HARDWARE
            val shareable = if (isHardwareBitmap) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
            file.outputStream().use { shareable.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
            FileProvider.getUriForFile(context, "${context.packageName}$AUTHORITY_SUFFIX", file)
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    }

    private companion object {
        const val SHARED_IMAGES_DIRECTORY = "shared_images"
        const val AUTHORITY_SUFFIX = ".shared.fileprovider"
        const val PNG_QUALITY = 100
    }
}
