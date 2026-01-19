package io.xa.sigad.crop.picker

import androidx.compose.runtime.Composable
import com.attafitamim.krop.core.images.ImageSrc
import androidx.compose.ui.graphics.ImageBitmap
import com.attafitamim.krop.core.images.ImageBitmapSrc
import io.xa.sigad.message.WebAccount
import sigad.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.getResourceUri

//import org.jetbrains.compose.resources.resource
//
//import sigad.composeapp.generated.resources.Res

interface ImagePicker {
    /** Pick an image with [mimetype] */
    fun pick(mimetype: String = "image/*")
}
var phoneScreenWidth:Double = 0.0
var phoneScreenHeight:Double = 0.0
var deviceScreenWidth:Long = 528
var deviceScreenHeight: Long = 768
var widthHeightRatio: Double = 0.0
var deviceColors : Long = 0

var webAccount: WebAccount = WebAccount()

expect fun isBound(): Boolean
expect fun isDevicePKBound(): Boolean
expect fun saveRegisterInfo()

expect fun bindNFC(): Boolean
expect fun projectScreen(image: ImageBitmap? ): Boolean
expect fun projectScreen(image: ByteArray? ): Boolean

expect fun projectPreview( image: ImageBitmap?, algType: String,  nBright:Int, nDitherPointCount: Int, nBaseRGB: Int): ImageBitmap?

expect suspend fun imageSrcFromPngByteArray(image: ByteArray) : ImageSrc?

@Composable
expect fun rememberImagePicker(onImage: (uri: ImageSrc) -> Unit): ImagePicker

@Composable
expect fun rememberImagePicker2(onImage:  (content: ByteArray?) -> Unit): ImagePicker

interface PlatformInterface {
    val name: String
}

expect fun getPlatform(): PlatformInterface

@OptIn(InternalResourceApi::class)
suspend fun getImageSrcFromResource(resourcePath: String): ImageSrc {
    //println(".....path is :${resourcePath} ${Res.getUri(resourcePath)}")
    val a = Res.readBytes( resourcePath).decodeToImageBitmap()
    //val a =  readResourceBytes(Res.getUri(resourcePath)).decodeToImageBitmap()
    return ImageBitmapSrc(a)
}