package com.example.custodescommand.feature.datasheets

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** In-app image fallback for photographed datasheets. Pinch to zoom; drag to pan. */
@Composable
fun SourcePageScreen(page: Int) {
    val context = LocalContext.current
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, page) {
        value = withContext(Dispatchers.IO) {
            try {
                context.assets.open("rules/pages/custodes_p$page.webp").use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
    var scale by remember(page) { mutableFloatStateOf(1f) }
    var offset by remember(page) { mutableStateOf(Offset.Zero) }
    val image = bitmap
    if (image == null) {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            Column {
                CircularProgressIndicator()
                Text("Loading source scan, or asset unavailable (page $page).")
            }
        }
    } else {
        Image(
            bitmap = image,
            contentDescription = "Uploaded Custodes book photo, supplied PDF page $page",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(page) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset += pan
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}
