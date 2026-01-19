package io.xa.sigad.screens.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.imageCropper
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.core.images.ImageBitmapSrc
import io.xa.sigad.crop.picker.rememberImagePicker
import io.xa.sigad.screens.detail.ImageSrcScreen
import kotlinx.coroutines.launch



@Composable
fun PickPhone() {
   //val imageCropper = rememberImageCropper()
    val navigator = LocalNavigator.currentOrThrow

    //val scope = rememberCoroutineScope()
    val imagePicker = rememberImagePicker(onImage = {
        //val imgSrc = ImageBitmapSrc(it)
        navigator.push(ImageSrcScreen(it))
    })
}

