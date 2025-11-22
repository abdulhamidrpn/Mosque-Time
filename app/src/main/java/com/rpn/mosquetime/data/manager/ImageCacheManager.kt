package com.rpn.mosquetime.data.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

// ================================================================================================
// STEP 5: IMAGE CACHE MANAGER
// ================================================================================================

class ImageCacheManager(private val context: Context) {

    private val cacheDir = File(context.cacheDir, "images")
    private val externalCacheDir =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "mosque_images")

    init {
        cacheDir.mkdirs()
        externalCacheDir.mkdirs()
    }

    suspend fun downloadAndCacheImage(url: String, fileName: String): String? = withContext(
        Dispatchers.IO
    ) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body.let { responseBody ->
                    val file = File(getPreferredCacheDir(), fileName)
                    file.outputStream().use { output ->
                        responseBody.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    file.absolutePath
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ImageCacheManager", "Error downloading image: ${e.message}")
            null
        }
    }

    private fun getPreferredCacheDir(): File {
        return if (hasExternalStoragePermission()) {
            externalCacheDir
        } else {
            cacheDir
        }
    }

    private fun hasExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCachedImagePath(fileName: String): String? {
        val internalFile = File(cacheDir, fileName)
        val externalFile = File(externalCacheDir, fileName)

        return when {
            internalFile.exists() -> internalFile.absolutePath
            externalFile.exists() -> externalFile.absolutePath
            else -> null
        }
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
        externalCacheDir.listFiles()?.forEach { it.delete() }
    }
}
