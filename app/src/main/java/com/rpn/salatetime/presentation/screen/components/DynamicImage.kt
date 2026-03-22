package com.rpn.salatetime.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import timber.log.Timber
import java.io.File

/**
 * Renders any image source — remote URL, local file path, or drawable res ID.
 *
 * @param source        URL, absolute file path, drawable res ID, Uri, File, or Bitmap.
 * @param modifier      Applied to the root [Box].
 * @param contentScale  How the image fills its bounds. Defaults to [ContentScale.Crop].
 * @param blurRadius    Blur intensity. `0.dp` = no blur. Typical values: 8–20.dp.
 * @param overlayAlpha  Black scrim opacity 0f–1f.
 *                      0f = none · 0.4f = TV-comfortable · 0.7f = heavy scrim.
 * @param overlayColor  Scrim colour. Defaults to [Color.Black].
 * @param placeholder   Composable shown while loading.
 * @param error         Composable shown on failure.
 */
@Composable
fun DynamicImage(
    source: Any?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    blurRadius: Dp = 0.dp,
    overlayAlpha: Float = 0f,
    overlayColor: Color = Color.Black,
    placeholder: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    },
    error: @Composable () -> Unit = {
        Box(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant))
    },
) {
    // ── Resolve source to proper format for Coil ────────────────────────────
    val resolvedSource: Any? = when {
        source == null -> {
            Timber.w("DynamicImage: source is null")
            null
        }
        source is String && source.startsWith("/") -> {
            // Local file path — convert to File object for Coil
            val file = File(source)
            if (file.exists()) {
                Timber.d("DynamicImage: Loading local file: $source")
                file
            } else {
                Timber.w("DynamicImage: Local file does not exist: $source")
                null
            }
        }
        source is String && source.isBlank() -> {
            Timber.w("DynamicImage: source string is blank")
            null
        }
        source is String -> {
            // Remote URL
            Timber.d("DynamicImage: Loading remote URL: $source")
            source
        }
        else -> {
            // Already a proper type (File, Uri, Bitmap, etc.)
            Timber.d("DynamicImage: Loading from source object: ${source::class.simpleName}")
            source
        }
    }

    Box(modifier = modifier) {

        // ── Image ─────────────────────────────────────────────────────────────
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(resolvedSource)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier),
            loading = {
                Timber.v("DynamicImage: Loading image from $source")
                placeholder()
            },
            error = {
                Timber.e("DynamicImage: Error loading image from $source")
                error()
            },
        )

        // ── Scrim overlay ─────────────────────────────────────────────────────
        // Sits on top of the (optionally blurred) image.
        // Skipped entirely when overlayAlpha == 0f — no redundant draw call.
        if (overlayAlpha > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(overlayColor.copy(alpha = overlayAlpha.coerceIn(0f, 1f)))
            )
        }
    }
}