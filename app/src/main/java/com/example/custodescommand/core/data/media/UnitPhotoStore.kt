package com.example.custodescommand.core.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.ImageDecoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Every photo belongs to one roster-unit instance. The selected content Uri is
 * consumed exactly once, while Photo Picker / OpenDocument grants read access.
 * Only the resized JPEG is retained in app-private storage.
 */
class UnitPhotoStore(context: Context) {
    private val app = context.applicationContext
    private val root = File(app.filesDir, "mini_photos")
    private val mutex = Mutex()
    private val _revision = MutableStateFlow(0L)
    val revision: StateFlow<Long> = _revision

    private fun target(id: String): File {
        require(id.matches(Regex("[A-Za-z0-9_-]{1,100}"))) {
            "Invalid roster-unit identifier."
        }
        return File(root, "$id.jpg")
    }

    fun findPhoto(id: String): File? = target(id).takeIf { it.isFile }

    suspend fun importPhoto(instanceId: String, uri: Uri) = withContext(Dispatchers.IO) {
        mutex.withLock {
            // Validate the identifier before accessing the provider.
            val destination = target(instanceId)
            val mime = app.contentResolver.getType(uri)
            require(mime == null || mime.startsWith("image/")) {
                "Please select an image file."
            }

            val staging = File.createTempFile("unit-photo-", ".input", app.cacheDir)
            try {
                copyPickedUriOnce(uri, staging)

                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(staging.absolutePath, bounds)
                var imageDecoderUsed = false

                val bitmap: Bitmap = if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                    var sample = 1
                    while (maxOf(bounds.outWidth / sample, bounds.outHeight / sample) > 1600) {
                        sample *= 2
                    }
                    BitmapFactory.decodeFile(
                        staging.absolutePath,
                        BitmapFactory.Options().apply { inSampleSize = sample }
                    ) ?: throw IOException("Unable to decode this image format. Try a JPEG or PNG.")
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Supports additional image formats (e.g., HEIF) on capable devices.
                    imageDecoderUsed = true
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(staging)) { decoder, info, _ ->
                        val maxSize = maxOf(info.size.width, info.size.height)
                        if (maxSize > 1600) {
                            val scale = 1600f / maxSize.toFloat()
                            decoder.setTargetSize(
                                (info.size.width * scale).toInt().coerceAtLeast(1),
                                (info.size.height * scale).toInt().coerceAtLeast(1)
                            )
                        }
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    throw IOException("Unsupported image. Select a JPEG or PNG using Browse files.")
                }

                // BitmapFactory does not apply EXIF rotation. ImageDecoder normally does.
                val orientation = if (imageDecoderUsed) ExifInterface.ORIENTATION_NORMAL else
                    runCatching {
                        ExifInterface(staging.absolutePath).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
                val rotation = Matrix().apply {
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                        ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                        ExifInterface.ORIENTATION_TRANSPOSE -> { postScale(-1f, 1f); postRotate(270f) }
                        ExifInterface.ORIENTATION_TRANSVERSE -> { postScale(-1f, 1f); postRotate(90f) }
                    }
                }
                val normalized = if (rotation.isIdentity) bitmap else Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, rotation, true
                )

                try {
                    check(root.exists() || root.mkdirs()) { "Cannot create photo storage directory." }
                    val atomic = AtomicFile(destination)
                    val output = atomic.startWrite()
                    try {
                        if (!normalized.compress(Bitmap.CompressFormat.JPEG, 85, output)) {
                            throw IOException("Failed to encode selected image.")
                        }
                        atomic.finishWrite(output)
                    } catch (error: Throwable) {
                        atomic.failWrite(output)
                        throw error
                    }
                    _revision.value += 1
                } finally {
                    if (normalized !== bitmap) normalized.recycle()
                    bitmap.recycle()
                }
            } finally {
                staging.delete()
            }
        }
    }

    private fun copyPickedUriOnce(uri: Uri, staging: File) {
        val resolver = app.contentResolver
        val input: InputStream = try {
            resolver.openInputStream(uri)
        } catch (_: Exception) {
            null
        } ?: runCatching {
            resolver.openFileDescriptor(uri, "r")?.let {
                ParcelFileDescriptor.AutoCloseInputStream(it)
            }
        }.getOrNull() ?: throw IOException(
            "The photo provider did not supply a readable image. Try Browse files instead."
        )
        input.use { source ->
            staging.outputStream().use { destination ->
                val buffer = ByteArray(64 * 1024)
                var bytes = 0L
                while (true) {
                    val read = source.read(buffer)
                    if (read < 0) break
                    bytes += read
                    if (bytes > 48L * 1024 * 1024) {
                        throw IOException("Image exceeds 48 MB. Choose a smaller file.")
                    }
                    destination.write(buffer, 0, read)
                }
                if (bytes == 0L) throw IOException("The selected image was empty.")
            }
        }
    }

    suspend fun deletePhoto(instanceId: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val file = target(instanceId)
            if (file.exists()) {
                if (!file.delete()) throw IOException("Could not remove the stored photo.")
                _revision.value += 1
            }
        }
    }
}
