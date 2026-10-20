package com.example.custodescommand

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.custodescommand.core.data.media.UnitPhotoStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class UnitPhotoStoreTest {
    @Test
    fun pickedPhotoIsStoredPrivatelyAndCanBeReplacedAndRemoved() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = UnitPhotoStore(context)
        val instanceId = "unit-photo-test"
        store.deletePhoto(instanceId)

        val source = File(context.cacheDir, "photo-test-source.png")
        try {
            val picture = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
            source.outputStream().use { picture.compress(Bitmap.CompressFormat.PNG, 100, it) }
            picture.recycle()
            store.importPhoto(instanceId, Uri.fromFile(source))
            val saved = store.findPhoto(instanceId)
            assertNotNull(saved)
            assertTrue(saved!!.exists())
            assertTrue(saved.absolutePath.startsWith(context.filesDir.absolutePath))
            val revisionAfterImport = store.revision.value

            store.importPhoto(instanceId, Uri.fromFile(source))
            assertEquals(revisionAfterImport + 1, store.revision.value)

            store.deletePhoto(instanceId)
            assertNull(store.findPhoto(instanceId))
        } finally {
            source.delete()
            store.deletePhoto(instanceId)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun photoStoreRejectsUnsafeFileName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        UnitPhotoStore(context).findPhoto("../../roster")
    }
}
