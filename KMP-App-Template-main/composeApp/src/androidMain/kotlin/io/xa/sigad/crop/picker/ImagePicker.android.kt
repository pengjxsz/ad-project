package io.xa.sigad.crop.picker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.ImageStream
import com.attafitamim.krop.core.images.toImageSrc
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

import androidx.compose.ui.graphics.asImageBitmap
import com.attafitamim.krop.core.images.ImageStreamSrc

fun byteArrayToImageBitmap(imageData: ByteArray): ImageBitmap {
    val bitmap: Bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
    return bitmap.asImageBitmap()
}

suspend fun ByteArray.toImageSrc() = ImageStreamSrc(MemoryImageStream(this))

data class MemoryImageStream(val image: ByteArray) : ImageStream {
    override fun openStream(): InputStream = ByteArrayInputStream(image)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemoryImageStream

        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        return image.contentHashCode()
    }
}

actual suspend fun imageSrcFromPngByteArray(image: ByteArray) : ImageSrc?{
    println("come to imageSrcFromPngByteArray")
    val aImage =  image.toImageSrc()
    println("MemeoryImageSrc created")
            return aImage as ImageSrc
}

/** Creates and remembers a instance of [ImagePicker] that launches
 * [ActivityResultContracts.GetContent] and calls [onImage] when the result is available */
@Composable
actual fun rememberImagePicker(
    onImage: (uri: ImageSrc) -> Unit
): ImagePicker {
    val context = LocalContext.current
    val contract = remember { ActivityResultContracts.GetContent() }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = contract,
        onResult = { uri ->
            coroutineScope.launch {
                val imageSrc = uri?.toImageSrc(context) ?: return@launch
                onImage(imageSrc)
            }
        }
    )

    return remember {
        object : ImagePicker {
            override fun pick(mimetype: String) = launcher.launch(mimetype)
        }
    }
}

@Composable
actual fun rememberImagePicker2(
    onImage: (content: ByteArray?) -> Unit
): ImagePicker {
    val context = LocalContext.current
    val contract = remember { ActivityResultContracts.GetContent() }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = contract,
        onResult = { uri ->
            coroutineScope.launch {
                val contentResolver = context.contentResolver
                // Read the image stream
                val inputStream: InputStream? = uri?.let { contentResolver.openInputStream(it) }
                val content = if (inputStream == null) {
                     null
                }else {
                    // Decode to Bitmap
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    if (bitmap == null) {
                        null
                    }else{                // Compress Bitmap to PNG ByteArray
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream) // 100 is quality, but PNG is lossless
                        val pngBytes = outputStream.toByteArray()
                        bitmap.recycle() // Recycle bitmap as soon as possible
                        pngBytes
                    }
                }
                onImage(content)
            }
        }
    )

    return remember {
        object : ImagePicker {
            override fun pick(mimetype: String) = launcher.launch(mimetype)
        }
    }
}

actual fun bindNFC(): Boolean {
    println("bindNFC")
    return true;
}

actual fun projectScreen(image: ImageBitmap?): Boolean{
return true
}


actual fun projectScreen(image: ByteArray? ): Boolean{
    return true;
}


actual fun projectPreview(
    image: ImageBitmap?,
    algType: String,
    nBright: Int,
    nDitherPointCount: Int,
    nBaseRGB: Int
): ImageBitmap? {
    return null
}

actual fun isBound(): Boolean {
   return true
}

actual fun isDevicePKBound(): Boolean {
    return true;
}

actual fun saveRegisterInfo() {
}

class AndroidPlatform : PlatformInterface {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): PlatformInterface = AndroidPlatform()