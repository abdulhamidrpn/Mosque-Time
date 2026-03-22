package com.rpn.salatetime.data.repository

import android.content.Context
import com.rpn.salatetime.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class ImageRepositoryImpl(
    private val context: Context,
    private val httpClientx: OkHttpClient,   // OkHttp — matches your existing cacheImage impl
) : ImageRepository {

    private val cacheDir: File by lazy {
        File(context.filesDir, "mosque_images").apply { mkdirs() }
    }
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }
    // ─────────────────────────────────────────────────────────────────────────
    // cacheImage
    //
    // Filename: {prefix}_{hash12}_{originalFilename}.{ext}
    //   mosque_bg_a1b2c3d4e5f6_banner.jpg
    //   slide_01_a1b2c3d4e5f6_ramadan.jpg
    //
    // Lookup uses only the 12-char hash so prefix/name can vary freely.
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun cacheImage(
        url: String,
        forceRefresh: Boolean,
        prefix: String,
    ): File? = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            Timber.w("cacheImage: blank url — skipping")
            return@withContext null
        }

        val hash = url.toSha256()

        if (!forceRefresh) {
            val existing = findCachedFile(hash)
            if (existing != null) {
                Timber.d("cacheImage: HIT  prefix=$prefix name=${existing.name}")
                return@withContext existing
            }
            Timber.d("cacheImage: MISS prefix=$prefix url=$url")
        } else {
            Timber.d("cacheImage: FORCE-REFRESH prefix=$prefix url=$url")
        }

        downloadTo(url = url, hash = hash, prefix = prefix)
    }

    override fun getCachedFile(url: String): File? {
        if (url.isBlank()) return null
        val file = findCachedFile(url.toSha256())
        Timber.d("getCachedFile: ${if (file != null) "found → ${file.name}" else "not found"} url=$url")
        return file
    }

    override fun evictCachedFile(url: String) {
        if (url.isBlank()) return
        val file = findCachedFile(url.toSha256())
        if (file != null) {
            val ok = file.delete()
            Timber.d("evictCachedFile: deleted=$ok name=${file.name}")
        } else {
            Timber.d("evictCachedFile: no cached file found for url=$url")
        }
    }

    override suspend fun preload(urls: List<String>) = withContext(Dispatchers.IO) {
        urls.distinct()
            .filter { it.isNotBlank() }
            .map { url -> async { cacheImage(url) } }
            .awaitAll()
        Unit
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Download
    //
    // OkHttp's execute() is a blocking call — correct here because we're
    // already on Dispatchers.IO from withContext in cacheImage().
    //
    // Flow:
    //   1. Execute blocking GET
    //   2. Stream body → .tmp file
    //   3. Atomically rename .tmp → final file
    //   4. On any failure: delete .tmp, return null (cache never partially written)
    // ─────────────────────────────────────────────────────────────────────────

    private fun downloadTo(url: String, hash: String, prefix: String): File? {
        Timber.d("  ⬇ downloadTo() url=$url")

        return try {
            val request  = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            Timber.d("  ⬇ HTTP ${response.code} for $url")

            if (!response.isSuccessful) {
                Timber.w("  ✗ Download failed — HTTP ${response.code} url=$url")
                return null
            }

            val extension    = response.body?.contentType()?.subtype?.sanitize() ?: "img"
            val originalName = url.extractOriginalFilename()
            val shortHash    = hash.take(12)

            // Build readable filename
            val fileName = buildString {
                if (prefix.isNotBlank()) { append(prefix.sanitize()); append("_") }
                append(shortHash); append("_")
                append(originalName); append("."); append(extension)
            }

            val finalFile = File(cacheDir, fileName)
            val tempFile  = File(cacheDir, "$shortHash.tmp")

            Timber.d("  ⬇ Writing to temp: ${tempFile.name}")

            try {
                response.body?.byteStream()?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Delete stale version before rename so it always succeeds
                if (finalFile.exists()) {
                    Timber.d("  ⬇ Replacing existing file: ${finalFile.name}")
                    finalFile.delete()
                }

                val renamed = tempFile.renameTo(finalFile)
                if (!renamed) {
                    tempFile.delete()
                    Timber.e("  ✗ Rename failed: ${tempFile.name} → ${finalFile.name}")
                    return null
                }

                Timber.d("  ✔ Cached → name=${finalFile.name} path=${finalFile.absolutePath}")
                finalFile
            } catch (e: Exception) {
                tempFile.delete()
                Timber.e(e, "  ✗ Write failed url=$url")
                null
            } finally {
                response.body?.close()
            }
        } catch (e: Exception) {
            Timber.e(e, "  ✗ Download exception url=$url")
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Matches any cached file whose name contains the 12-char hash prefix. */
    private fun findCachedFile(hash: String): File? {
        val shortHash = hash.take(12)
        return cacheDir.listFiles()
            ?.find { it.name.contains(shortHash) && !it.name.endsWith(".tmp") }
    }

    private fun String.toSha256(): String =
        MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString("") { "%02x".format(it) }

    /**
     * Extracts the last path segment of a URL, stripped of query params and extension.
     * "https://…/storage/v1/object/public/slides/ramadan-slide.jpg?token=xyz"
     *   → "ramadan-slide"
     * Capped at 30 chars for filesystem safety.
     */
    private fun String.extractOriginalFilename(): String =
        substringAfterLast("/")
            .substringBefore("?")
            .substringBefore("#")
            .substringBeforeLast(".")
            .sanitize()
            .take(30)
            .ifBlank { "image" }

    private fun String.sanitize(): String =
        replace(Regex("[^a-zA-Z0-9._-]"), "_").trimEnd('_')
}
/*class ImageRepositoryImpOldl(
    private val context: Context
) : ImageRepository {

    private val cacheDir: File by lazy {
        File(context.cacheDir, "offline_images").apply { if (!exists()) mkdirs() }
    }

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun cacheImage(url: String): File? = withContext(Dispatchers.IO) {
        val file = File(cacheDir, safeFileName(url))
        Timber.d("Cached: fileExists ${file.exists()} Name -> ${file.absolutePath} ← $url")
        if (file.exists()) return@withContext file
        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            response.body?.use { body ->
                file.outputStream().use { out ->
                    body.byteStream().use { it.copyTo(out) }
                }
            }
            Timber.d("Cached: ${file.name} ← ${file.absolutePath}")
            file
        } catch (e: Exception) {
            null
        }
    }

    override fun getCachedFile(url: String): File? {
        val file = File(cacheDir, safeFileName(url))
        return if (file.exists()) file else null
    }

    override suspend fun preload(urls: List<String>) = withContext(Dispatchers.IO) {
        urls.filter { it.isNotBlank() }.forEach { url ->
            if (getCachedFile(url) == null) cacheImage(url)
        }
    }

    private fun safeFileName(url: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(url.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }
        val ext = when {
            url.contains(".png", ignoreCase = true) -> "png"
            url.contains(".webp", ignoreCase = true) -> "webp"
            else -> "img"
        }
        return "$hex.$ext"
    }
}*/

