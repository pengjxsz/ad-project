package io.xa.sigad.crop.picker

import io.xa.sigad.PhoneShell
//import com.attafitamim.krop.sample.toUIImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.UIImageSrc
import com.attafitamim.krop.core.utils.toImageBitmap
import io.xa.sigad.message.WebAccount
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope


import platform.UIKit.UIImagePickerController
import platform.UIKit.UIDevice
import kotlin.native.Platform

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberImagePicker(onImage: (uri: ImageSrc) -> Unit): ImagePicker {
    val imagePicker = remember {
        UIImagePickerController()
    }
    //val phoneShell = PhoneShell.sharedInstance()
    val galleryDelegate = remember {
        ImagePickerDelegate(onImage)
    }
    return remember {
        IosImagePicker(imagePicker, galleryDelegate)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberImagePicker2(onImage: (content: ByteArray?) -> Unit): ImagePicker {
    val imagePicker = remember {
        UIImagePickerController()
    }
    //val phoneShell = PhoneShell.sharedInstance()
    val galleryDelegate = remember {
        ImagePickerDelegate2(onImage)
    }
    return remember {
        IosImagePicker(imagePicker, galleryDelegate)
    }
}

actual suspend fun imageSrcFromPngByteArray(image: ByteArray) : ImageSrc?{
    val uiImage = byteArrayToUIImage(image)
    val imageSrc = UIImageSrc(uiImage!!)

    return imageSrc
}

@OptIn(ExperimentalForeignApi::class)
actual fun bindNFC(): Boolean {
    val phoneShell = PhoneShell.sharedInstance();
    phoneShell?.BindNFCDevice();

//    coroutineScope {
//        launch {
//            while (true) {
//                println("Task running every 2 seconds")
//                delay(2000L)  // delay for 2 seconds
//            }
//        }
//    }
//    LaunchedEffect(Unit) {
//        // 2. 执行非阻塞延时
//        delay(200L)
//
//        // 3. 在延时结束后更新状态，触发UI重绘
//        //showText = true
//    }
    return true;
}


@OptIn(ExperimentalForeignApi::class)
fun passDeviceInfo(phoneShell: PhoneShell){
    phoneScreenHeight = phoneShell.phoneScreenHeight
    phoneScreenWidth = phoneShell.phoneScreenWidth

    deviceScreenWidth = phoneShell.deviceScreenWidth
    deviceScreenHeight = phoneShell.deviceScreenHeight
    deviceColors = phoneShell.deviceScreenColors;

    widthHeightRatio = phoneShell.DSAdaptCoefficient
    val userId = phoneShell.userId.toString();
    val masterKey = phoneShell.masterKey.toString();
    val masterPK = phoneShell.masterPK.toString();
    val chatKey = phoneShell.chatKey.toString();
    val chatPK = phoneShell.chatPK.toString();
    val deviceChip  = phoneShell.chipIDHex.toString();
    val devicePK = phoneShell.devicePKHex.toString();

    webAccount = WebAccount(user_id=userId,
        masterKey=masterKey, masterPK = masterPK,
        chatKey = chatKey, chatPK=chatPK,
        devicePK=devicePK , deviceChip=deviceChip,
        user_nick = "nick_name",
        deviceWith = deviceScreenWidth,
        deviceHeight = deviceScreenHeight,
        device_colors = deviceColors
        )
}



@OptIn(ExperimentalForeignApi::class)
actual fun isBound(): Boolean {
   // println("enter isBound ")
    val phoneShell = PhoneShell.sharedInstance();
    if (phoneShell != null){
        if (phoneShell.isEverBound()) {
            passDeviceInfo(phoneShell)
            //println("enter isBound true, DEVICEPK is ${phoneShell.devicePKHex} ${phoneShell.masterPK}")
            return true;
        }
    }
    return false;
}

@OptIn(ExperimentalForeignApi::class)
actual fun saveRegisterInfo() {
    val phoneShell = PhoneShell.sharedInstance();
    if (phoneShell != null) {

        phoneShell.userId = webAccount.user_id;
        phoneShell.masterKey = webAccount.masterKey;
        phoneShell.masterPK = webAccount.masterPK;
        phoneShell.chatKey = webAccount.chatKey;
        phoneShell.chatPK = webAccount.chatPK;

        phoneShell.saveRegisterInfo()
        println(" phoneShell saveRegisterInfo.....")
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun isDevicePKBound(): Boolean {
    val phoneShell = PhoneShell.sharedInstance();
    if (phoneShell != null){
        if (phoneShell.devicePKHex != null ){
            passDeviceInfo(phoneShell)
            println("enter isDevicePKBound true, DEVICEPK is ${phoneShell.devicePKHex} ${phoneShell.masterPK}")
            return true;
        }
    }
    return false;
}

@OptIn(ExperimentalForeignApi::class)
actual fun projectScreen(image: ImageBitmap?): Boolean {
    println("enter projectScreen $image")

    if (image == null) {
        println("image is null")
        val phoneShell = PhoneShell.sharedInstance();
        phoneShell?.project2Screen();
        return true
    } else {
        println("prepared image")
        val uiImage = UIImageFromPng(image)
        val phoneShell = PhoneShell.sharedInstance();
        phoneShell?.project2ScreenDefault(uiImage);
        return true;
    }
}


@OptIn(ExperimentalForeignApi::class)
actual fun projectScreen(image: ByteArray?): Boolean {
    println("enter projectScreen $image")

    if (image == null) {
        return false;
    } else {
        println("prepared image")
        val uiImage = byteArrayToUIImage(image);
        val phoneShell = PhoneShell.sharedInstance();
        phoneShell?.project2ScreenDefault(uiImage);
        return true;

    }
}


@OptIn(ExperimentalForeignApi::class)
actual fun projectPreview(
    image: ImageBitmap?,
    algType: String,
    nBright: Int,
    nDitherPointCount: Int,
    nBaseRGB: Int
): ImageBitmap? {
    if (image != null) {

        //val uiImage = image.toUIImage()

//        val pixels = extractPixelData(image)
//        val uiImage = convertToUIImage(pixels, image.width, image.height)

        val uiImage = UIImageFromPng(image);


        val phoneShell = PhoneShell.sharedInstance();
        println("${phoneShell?.DSAdaptCoefficient} ${phoneShell?.phoneScreenWidth} ${phoneShell?.phoneScreenHeight}  ${phoneShell?.deviceScreenWidth} ${phoneShell?.deviceScreenHeight}  ")

        val uiImagePreview = phoneShell?.projectPreview(uiImage, "3", 100, 12, 0)
        if (uiImagePreview != null) {
            //val imageSrcPreview  = UIImageSrc(uiImagePreview)
            //      val bitmap = imageSrcPreview.toImageBitmap(COMPRESSION_QUALITY) ?: return null

            //    return imageSrcPreview.
            val a =  uiImagePreview.toImageBitmap(0.60)
            return a;
            //return uiImage?.toImageBitmap()

        }

    }
    return null;
}


class IOSPlatform: PlatformInterface {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): PlatformInterface = IOSPlatform()


//
//fun showTemporaryAlert(message: String, viewController: UIViewController) {
//    val alert = UIAlertController.alertControllerWithTitle(
//        title = "Alert",
//        message = message,
//        preferredStyle = UIAlertControllerStyleAlert
//    )
//    viewController.presentViewController(alert, animated = true, completion = null)
//
//    // Dismiss after 2 seconds
//    DispatchQueue.main.asyncAfter(deadline = DispatchTime.now() + 2.0) {
//        alert.dismissViewControllerAnimated(true, completion = null)
//    }
//}
