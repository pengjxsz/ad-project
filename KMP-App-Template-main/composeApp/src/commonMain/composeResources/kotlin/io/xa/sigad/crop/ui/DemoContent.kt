package io.xa.sigad.crop.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
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
import io.xa.sigad.crop.picker.deviceScreenHeight
import io.xa.sigad.crop.picker.deviceScreenWidth
import io.xa.sigad.crop.picker.phoneScreenWidth
import io.xa.sigad.crop.picker.projectScreen
import io.xa.sigad.crop.picker.widthHeightRatio
import io.xa.sigad.crop.ui.theme.KropTheme
import com.attafitamim.krop.ui.ImageCropperDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview


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


@Preview
@Composable
fun DemoContent(
    cropState: CropState?,
    loadingStatus: CropperLoading?,
    selectedImage: ImageBitmap?,
    onProject: () -> Boolean,
    status: MutableState<String?>,
    modifier: Modifier = Modifier,
) {
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

        Row(// 确保 Row 占据可用宽度
            modifier = Modifier.fillMaxWidth(),
            // 🚀 关键修改：让 Row 内部的内容（Button）水平居中
            horizontalArrangement = Arrangement.Center
        ) {
            selectedImage?.let {
                Button(
                    onClick = { onProject() },
                    //modifier = Modifier.padding(2.dp)
                    modifier = Modifier.size(72.dp).padding(top=2.dp),
                    shape = CircleShape,
//                    colors = ButtonDefaults.buttonColors(
//                            containerColor = Color.White, // 或者 MaterialTheme.colorScheme.surface
//                    contentColor = MaterialTheme.colorScheme.primary) // 设置文本/图标颜色
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "投屏", modifier = Modifier.size(24.dp))
                }

            }
        }
    }
}


