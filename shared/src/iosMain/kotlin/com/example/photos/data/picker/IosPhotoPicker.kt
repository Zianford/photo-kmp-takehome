package com.example.photos.data.picker

import com.example.photos.domain.model.PickedPhoto
import com.example.photos.domain.repository.PhotoPicker
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
class IosPhotoPicker(private val presenter: () -> UIViewController) : PhotoPicker {
    private var delegate: PickerDelegate? = null

    override suspend fun pickPhotos(): List<PickedPhoto> {
        val selected = suspendCancellableCoroutine<List<PHPickerResult>> { continuation ->
            check(delegate == null) { "Photo picker is already open" }
            val configuration = PHPickerConfiguration().apply {
                filter = PHPickerFilter.imagesFilter
                selectionLimit = 10
            }
            val controller = PHPickerViewController(configuration)
            delegate = PickerDelegate { results ->
                controller.dismissViewControllerAnimated(true, completion = null)
                delegate = null
                if (continuation.isActive) continuation.resume(results)
            }
            controller.delegate = delegate
            continuation.invokeOnCancellation {
                controller.dismissViewControllerAnimated(true, completion = null)
                delegate = null
            }
            presenter().presentViewController(controller, animated = true, completion = null)
        }
        return selected.mapIndexed { index, result ->
            PickedPhoto("Photo ${index + 1}", result.loadBytes())
        }
    }

    private suspend fun PHPickerResult.loadBytes(): ByteArray = suspendCancellableCoroutine { continuation ->
        itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data: NSData?, error ->
            when {
                !continuation.isActive -> Unit
                error != null -> continuation.resumeWithException(IllegalStateException(error.localizedDescription))
                data == null -> continuation.resumeWithException(IllegalStateException("Photo data unavailable"))
                data.length > MAX_UPLOAD_BYTES.toUInt() -> continuation.resumeWithException(IllegalArgumentException("Photo is too large"))
                else -> {
                    val bytes = ByteArray(data.length.toInt())
                    if (bytes.isNotEmpty()) memcpy(bytes.refTo(0), data.bytes, data.length)
                    continuation.resume(bytes)
                }
            }
        }
    }

    private class PickerDelegate(private val onPicked: (List<PHPickerResult>) -> Unit) :
        NSObject(), PHPickerViewControllerDelegateProtocol {
        override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
            onPicked(didFinishPicking.filterIsInstance<PHPickerResult>())
        }
    }

    private companion object {
        const val MAX_UPLOAD_BYTES = 25 * 1024 * 1024
    }
}
