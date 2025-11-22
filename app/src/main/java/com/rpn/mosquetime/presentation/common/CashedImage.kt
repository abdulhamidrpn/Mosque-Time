package com.rpn.mosquetime.presentation.common


import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rpn.mosquetime.domain.repository.ImageRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File

@Composable
fun CachedImage(
    modifier: Modifier = Modifier,
    url: String,
    contentScale: ContentScale = ContentScale.Crop,
    repository: ImageRepository = koinInject<ImageRepository>()
) {
    val TAG = "CachedImage"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cachedPath by remember(url) { // 👈 reset when url changes
        mutableStateOf<File?>(repository.getCachedFile(url))
    }
    var loading by remember(url) { mutableStateOf(false) } // reset with url

    LaunchedEffect(url) {
        Log.d(TAG, "CachedImage: New url -> $url")
        if (cachedPath == null) {
            loading = true
            scope.launch {
                cachedPath = repository.cacheImage(url)
                Log.d(TAG, "CachedImage: Cached path -> $cachedPath")
                loading = false
            }
        }
    }

    Box(modifier) {
        when {
            cachedPath != null -> {
                val bmp = BitmapFactory.decodeFile(cachedPath!!.path)
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale
                    )
                } else {
                    Text("Image corrupted", modifier = Modifier.align(Alignment.Center))
                }
            }

            loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                // fallback to online coil
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
