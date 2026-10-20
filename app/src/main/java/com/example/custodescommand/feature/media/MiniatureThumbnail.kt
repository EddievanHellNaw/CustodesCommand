package com.example.custodescommand.feature.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.custodescommand.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MiniatureThumbnail(
    photo: File?,
    photoRevision: Long,
    modifier: Modifier = Modifier
) {
    val image by produceState<Bitmap?>(null, photo?.absolutePath, photoRevision) {
        value = withContext(Dispatchers.IO) {
            if (photo?.isFile != true) return@withContext null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(photo.absolutePath, bounds)
            val maxDimension = maxOf(bounds.outWidth, bounds.outHeight)
            val sample = generateSequence(1) { it * 2 }
                .first { maxDimension / it <= 320 }
            BitmapFactory.decodeFile(
                photo.absolutePath,
                BitmapFactory.Options().apply { inSampleSize = sample }
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = image
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Photo of your painted miniature",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_custodes_foreground),
                contentDescription = "Custodes emblem, no custom unit photo",
                modifier = Modifier.size(62.dp)
            )
        }
    }
}
