package com.rpn.mosquetime.data.repository


import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.rpn.mosquetime.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class ImageRepositoryImpl(
    private val context: Context,
    private val imageLoader: ImageLoader
) : ImageRepository {

    private val cacheDir: File by lazy {
        File(context.cacheDir, "offline_images").apply { if (!exists()) mkdirs() }
    }

    override suspend fun cacheImage(url: String): File? = withContext(Dispatchers.IO) {
        val file = File(cacheDir, url.hashCode().toString() + ".jpg")
        if (file.exists()) return@withContext file

        try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request).drawable
            if (result != null) {
                val bitmap = coil.decode.DataSource.MEMORY.name // no direct conversion
            }

            // Write manually to file
            val drawable = imageLoader.execute(request).drawable ?: return@withContext null
            val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                ?: return@withContext null
            file.outputStream().use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getCachedFile(url: String): File? {
        val file = File(cacheDir, url.hashCode().toString() + ".jpg")
        return if (file.exists()) file else null
    }
}
