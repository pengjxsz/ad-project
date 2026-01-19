package io.xa.sigad.crop.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.attafitamim.krop.core.images.ImageSrc
import io.xa.sigad.message.Po_test
import io.xa.sigad.crop.picker.bindNFC
import io.xa.sigad.crop.picker.projectScreen
import io.xa.sigad.crop.picker.rememberImagePicker
import io.xa.sigad.crop.picker.rememberImagePicker2
import com.attafitamim.krop.sample.presentation.ImagesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

@Composable
fun ViewModelDemo(viewModel: ImagesViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val imagePicker = rememberImagePicker(onImage = { uri -> viewModel.setSelectedImage(uri) })
    val imagePickerSend = rememberImagePicker2(onImage = { content ->
        {
            scope.launch(Dispatchers.IO) {

                val potest = Po_test()
                println("potest created!")
                //potest.test_register()
                //potest.test_account()
                if (content != null) {
                    potest.test_PhotoSend(content)
                }
            }
        }
    })
    val imageReceived = { imageSrc: ImageSrc, cropChecked: Boolean ->

        Unit
    }
    val selected = viewModel.selectedImage.collectAsState().value;
    var status = remember { mutableStateOf<String?>("") }// To show loading state

    DemoContent(
        cropState = viewModel.imageCropper.cropState,
        loadingStatus = viewModel.imageCropper.loadingStatus,
        selectedImage = selected,//viewModel.selectedImage.collectAsState().value,
        //onPick = { imagePicker.pick() },
        //onBind = { bindNFC() },
        onProject = { projectScreen(null as ImageBitmap?) },
        //onPickSend = { imagePickerSend.pick() },
        //onReceived = imageReceived,
        status,
        modifier = modifier
    )
    viewModel.cropError.collectAsState().value?.let { error ->
        CropErrorDialog(error, onDismiss = { viewModel.cropErrorShown() })
    }
}
