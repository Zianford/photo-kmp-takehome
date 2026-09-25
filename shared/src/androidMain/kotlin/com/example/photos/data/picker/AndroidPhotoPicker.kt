package com.example.photos.data.picker

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.photos.domain.model.PickedPhoto
import com.example.photos.domain.repository.PhotoPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class AndroidPhotoPicker(private val activity: ComponentActivity) : PhotoPicker {
    private var selection: Continuation<List<Uri>>? = null
    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(10),
    ) { uris ->
        selection?.resume(uris)
        selection = null
    }

    override suspend fun pickPhotos(): List<PickedPhoto> {
        val uris = suspendCancellableCoroutine<List<Uri>> { continuation ->
            check(selection == null) { "Photo picker is already open" }
            selection = continuation
            continuation.invokeOnCancellation { selection = null }
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        return withContext(Dispatchers.IO) {
            uris.mapIndexed { index, uri ->
                PickedPhoto(nameOf(uri) ?: "Photo ${index + 1}", readPhoto(uri))
            }
        }
    }

    private fun nameOf(uri: Uri): String? = activity.contentResolver.query(
        uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }

    private fun readPhoto(uri: Uri): ByteArray {
        val input = requireNotNull(activity.contentResolver.openInputStream(uri))
        return input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (output.size() <= MAX_UPLOAD_BYTES) {
                val read = stream.read(buffer, 0, minOf(buffer.size, MAX_UPLOAD_BYTES + 1 - output.size()))
                if (read < 0) break
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        }
    }

    private companion object {
        const val MAX_UPLOAD_BYTES = 25 * 1024 * 1024
    }
}
