package io.xa.sigad.crop.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.core.images.ImageSrc
import io.xa.sigad.message.Po_test
import io.xa.sigad.crop.picker.bindNFC
import io.xa.sigad.crop.picker.isBound
import io.xa.sigad.crop.picker.projectPreview
import io.xa.sigad.crop.picker.projectScreen
import io.xa.sigad.crop.picker.rememberImagePicker
import io.xa.sigad.crop.picker.rememberImagePicker2
import io.xa.sigad.screens.detail.DetailFileScreenModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SimpleDemo(imageSrc: ImageSrc?=null,modifier: Modifier = Modifier) {
    val imageCropper = rememberImageCropper()
    val scope = rememberCoroutineScope()
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var error by remember { mutableStateOf<CropError?>(null) }
    val isBound = isBound()

    var status = remember { mutableStateOf<String?>("") }// To show loading state
    var cropped = remember {  mutableStateOf<Boolean>(false) }
//    val obj by screenModel?.getObject(objectId.toString())?.collectAsStateWithLifecycle(initialValue = null)
    if (imageSrc !=null && cropped.value != true) {
        println(".....launch....")
        scope.launch {
            when (val result = imageCropper.crop(imageSrc)) {
                CropResult.Cancelled -> {cropped.value=true}
                is CropError -> {error = result
                    cropped.value=true}
                is CropResult.Success -> {
                    cropped.value = true
                    selectedImage = result.bitmap
                    if (isBound)
                        selectedImage = projectPreview(selectedImage, "3", 100, 12, 0)
                }
            }
        }
    }
//    val imagePicker = rememberImagePicker(onImage = { imageSrc ->
//        scope.launch {
//            when (val result = imageCropper.crop(imageSrc)) {
//                CropResult.Cancelled -> {}
//                is CropError -> error = result
//                is CropResult.Success -> {
//                    selectedImage = result.bitmap
//                   // selectedImage!!.encodeToByteArray()
//                    println("selectedImage assigned....")
//                    if (isBound) {
//                        selectedImage = projectPreview(selectedImage, "3", 100, 12, 0)
//                        println("selectedImage previewed....$selectedImage ${selectedImage==null}")
//                    }
//                }
//            }
//        }
//    })
//
//    val imageReceived = { imageSrc: ImageSrc, cropChecked: Boolean ->
//        scope.launch {
//                when (val result = imageCropper.crop(imageSrc)) {
//                    CropResult.Cancelled -> {}
//                    is CropError -> error = result
//                    is CropResult.Success -> {
//                        selectedImage = result.bitmap
//                        println("received with swith $cropChecked")
//                        if (isBound)
//                            selectedImage = projectPreview(selectedImage, "3", 100, 12, 0)
//                    }
//                }
//
//        }
//        Unit
//    }

//    val imagePickerSend = rememberImagePicker2(onImage = { content ->
//        //imageSrc.open(imageSrc.)
//        scope.launch(Dispatchers.IO) {
//            try {
//                status.value = "sending...."
//                val potest = Po_test()
//                println("potest created!")
//                //potest.test_register()
//                //potest.test_account()
//                if (content != null) {
//                    println("send photo..................")
//                    val ret = potest.test_PhotoSend(content)
//                    println("send photo return $ret")
//                    status.value = "send!"
//                }
//            }catch(e: Exception) {
//                println("Error during chat: ${e.message}")
//                withContext(Dispatchers.Main) {
//                    status.value = "Error: ${e.message}"
//                }
//            }
//        }
//    })

    if (isBound) {
        DemoContent(
            cropState = imageCropper.cropState,
            loadingStatus = imageCropper.loadingStatus,
            selectedImage = selectedImage,
            //onPick = { imagePicker.pick() },
            //onBind = { bindNFC() },
            onProject = { projectScreen(null as ImageBitmap?) },
            //onPickSend = { imagePickerSend.pick() },
            //onReceived = imageReceived,
            status,
            modifier = modifier
        )
    } else
        Text("  没有绑定NFC投屏设备。请绑定先");
    error?.let { CropErrorDialog(it, onDismiss = { error = null }) }
}

