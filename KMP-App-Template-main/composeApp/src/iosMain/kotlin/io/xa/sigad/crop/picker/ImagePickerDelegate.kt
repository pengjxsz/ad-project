package io.xa.sigad.crop.picker

import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.UIImageSrc
import com.attafitamim.krop.core.utils.toByteArray
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi

class ImagePickerDelegate(
    private val onImage: (uri: ImageSrc) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            ?: return
//        val phonseshell = PhoneShell.sharedInstance()
//        println("after get phonshell $image")
//        val imagePreview = phonseshell?.projectPreview(image, "1", 100 ,12 , 0)
//        if (imagePreview!=null) {
//            val imageSrc = UIImageSrc(imagePreview)
//            picker.dismissViewControllerAnimated(true, null)
//            onImage.invoke(imageSrc)
//        }
        val imageSrc = UIImageSrc(image)
        println("imagePickDelegate $imageSrc")
            picker.dismissViewControllerAnimated(true, null)
        imageSrc?.let { onImage.invoke(it) }


//        println("after called phonshell project2Screen")

    }
}

class ImagePickerDelegate2(
    private val onImage: (content: ByteArray?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            ?: return
        val selectedImageByteArray = image.toByteArray(0.6)
        picker.dismissViewControllerAnimated(true, null)
        onImage.invoke(selectedImageByteArray)

    }
}
