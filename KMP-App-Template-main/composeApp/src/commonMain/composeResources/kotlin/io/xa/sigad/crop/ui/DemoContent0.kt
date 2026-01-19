package io.xa.sigad.crop.ui.back


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.CropperLoading
import com.attafitamim.krop.core.crop.RectCropShape
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.images.ImageSrc
import io.xa.sigad.message.Po_test
import io.xa.sigad.message.toHex
import io.xa.sigad.crop.picker.deviceScreenHeight
import io.xa.sigad.crop.picker.deviceScreenWidth
import io.xa.sigad.crop.picker.imageSrcFromPngByteArray
import io.xa.sigad.crop.picker.phoneScreenWidth
import io.xa.sigad.crop.picker.projectScreen
import io.xa.sigad.crop.picker.widthHeightRatio
import io.xa.sigad.crop.ui.theme.KropTheme
import com.attafitamim.krop.ui.ImageCropperDialog
import io.xa.sigad.crop.ui.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

var progress1: Float = 0F
var progress2: Float = 0F
var progress3: Float = 0F
val radioOptions = listOf("Dither", "Level")

@Composable
fun MultiLayeredImage(image: ImageBitmap) {
    //println("image size ${image.width}, ${image.height}")
    val xySpace = (15 * widthHeightRatio).toInt() + 50
    val imageViewWidth = phoneScreenWidth - 2 * xySpace
    val imageViewHeight = (imageViewWidth * deviceScreenHeight / deviceScreenWidth).toInt()
    Box(
        modifier = Modifier
            .size(imageViewWidth.dp, imageViewHeight.dp)
            .clip(RoundedCornerShape(16.dp)) // Specify the round rectangle shape
            .background(Color.Gray)
        //.apply {// Add the background
        //  println(this.toString())
        //}
    ) {
        //println(Modifier.toString())
        //println(modifier.toString())
        Image(
            bitmap = image,
            contentDescription = "Multi-layered Image",
            Modifier.fillMaxSize(),
            // Image will fill the Box
            contentScale = ContentScale.Crop
        )


    }
}


@Composable
fun SendButton(
    onClick: () -> Unit, chatSend: String,status:  MutableState<String?>
) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            if (chatSend != "") {
                status.value = "sending chat...."

                scope.launch(Dispatchers.IO) {
                    try {
                        val potest = Po_test()
                        println("potest created!")
                        //potest.test_register()
                        //potest.test_account()
                        println("send chat..................")
                        val ret = potest.test_ABChat(chatSend)
                        println("send chat return $ret")
                        status.value = "chat send"
                    }catch(e: Exception){
                        println("Error during chat: ${e.message}")
                        withContext(Dispatchers.Main) {
                            status.value = "Error: ${e.message}"
                        }
                    }

                }
            } else {
                onClick()
            }
        }, modifier = Modifier.padding(2.dp)
    ) {
        Text("S")
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ReceiveButton(onClick: (ImageSrc, Boolean) -> Unit, status:  MutableState<String?>, cropChecked: Boolean) {
    val scope = rememberCoroutineScope()
    // 1. Declare a mutable state to hold the result
//    var chatResult by remember { mutableStateOf<String?>(null) } // Initially null
//    var isLoading by remember { mutableStateOf(false) } // To show loading state
//    var error by remember { mutableStateOf<String?>(null) } // To show errors

    Button(modifier = Modifier.padding(2.dp), onClick = {
        // 1. Declare a mutable state to hold the result
//        isLoading = true // Start loading
//        error = null     // Clear any previous error
//        chatResult = null // Clear previous result

        scope.launch(Dispatchers.IO) { // Run network call in background
            println("receive clicked!....")
            try {
                val potest = Po_test()
                println("potest created!")
                status.value = "doing......"
                val content = potest.test_PhotoRecv() // Call the suspend function
                val command = "#%PROJECT%#"
                val commandOK = content?.indexOf(command)
                val imageByteArray = if (commandOK != -1) {
                    println("receive a projection command")
                    status.value = "photo received"

                    val imageHex = content?.substring(command.length)
                    imageHex?.hexToByteArray()

                } else {
                   status.value = (content)
                    return@launch
                }


                println("test_chat completed with a image, size is : ${imageByteArray?.size}")
                println(imageByteArray?.toHex()?.substring(0, 20))
                //println(imageByteArray.toString().substring(0,20))
                if (cropChecked==false)
                    projectScreen(imageByteArray)
                else {
                    val imageSrc = imageSrcFromPngByteArray(imageByteArray!!)
                    println("imagsrc created")
                    // 2. Update the state on the Main dispatcher (UI thread)
//                withContext(Dispatchers.Main) {
//                    chatResult = "received" // This will trigger recomposition
//                    isLoading = false    // Stop loading
//                }
                    imageSrc?.let { onClick(it, cropChecked) }
                }
            } catch (e: Exception) {
                println("Error during chat: ${e.message}")

                withContext(Dispatchers.Main) {
                    status.value = "Error: ${e.message}"
                }
            }
        }
    }) {
        Text("R")
    }
    // 3. Display the result in the UI
//    if (isLoading) {
//        CircularProgressIndicator() // Show a loading indicator
//    } else if (error != null) {
//        Text("Error: $error", color = MaterialTheme.colors.error)
//    } else if (chatResult != null) {
//        Text("Result: $chatResult")
//    }
}


@Preview
@Composable
fun DemoContent(
    cropState: CropState?,
    loadingStatus: CropperLoading?,
    selectedImage: ImageBitmap?,
    onPick: () -> Unit,
    onBind: () -> Unit,
    onProject: () -> Boolean,
    onPickSend: () -> Unit,
    onReceived: (ImageSrc, Boolean) -> Unit,
    status: MutableState<String?>,
    modifier: Modifier = Modifier,
) {

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    var chatSend by remember { mutableStateOf<String?>("") } // Initially null
    var chatResult = remember { mutableStateOf<String?>("") } // Initially null

    var cropChecked by remember { mutableStateOf(true) }


    //var status = remember { mutableStateOf<String?>("") }// To show loading state
    var error by remember { mutableStateOf<String?>(null) } // To show errors

    if (cropState != null) {
        KropTheme(darkTheme = true) {
            ImageCropperDialog(
                state = cropState,
                style = cropperStyle(
                    shapes = listOf(
                        RectCropShape,
                        // CircleCropShape,
                        //TriangleCropShape,
                        //StarCropShape
                    ),
//                    aspects = listOf(AspectRatio(16, 9), AspectRatio(1, 1)),
                    aspects = listOf(
                        AspectRatio(
                            deviceScreenWidth.toInt(),
                            deviceScreenHeight.toInt()
                        )
                    ),

                    )
            )
        }
    }
    if (cropState == null && loadingStatus != null) {
        LoadingDialog(status = loadingStatus)
    }
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedImage != null) {
            MultiLayeredImage(selectedImage)
        } else Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            Text("No image selected !")
        }
        val po_test = Po_test()
        val chatmap = po_test.chatMap
        Text("Myself: ${chatmap["Myself"]?.userId}")
        Text("Friend: ${chatmap["Friend"]?.userId}")
        Text("Status: ${status.value}")
        Switch(
            checked = cropChecked,
            onCheckedChange = {
                cropChecked = it
            }
        )
        TextField(
            value = chatSend.toString(),
            onValueChange = {
                chatSend = it
            },
            singleLine = true,
            label = { Text("Send") }
        )
        TextField(
            value = chatResult.value.toString(),
            onValueChange = {
                chatResult.value = it
            },
            singleLine = true,
            label = { Text("Recv") }
        )

        Row() {
            Button(onClick = onPick, modifier = Modifier.padding(2.dp)) { Text("选图") }
            Button(onClick = onBind, modifier = Modifier.padding(2.dp)) { Text("绑定") }
            SendButton(onClick = onPickSend, chatSend.toString(), status)
            ReceiveButton(onReceived, status, cropChecked)

            selectedImage?.let {
                Button(
                    onClick = { onProject() },
                    //modifier = Modifier.padding(2.dp)
                            modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) { Text("投屏") }

            }
        }
    }
}


