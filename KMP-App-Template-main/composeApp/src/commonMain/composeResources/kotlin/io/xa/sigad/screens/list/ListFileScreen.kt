package io.xa.sigad.screens.list
import androidx.compose.ui.Alignment
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import io.xa.sigad.crop.picker.rememberImagePicker

import io.xa.sigad.data.FileItem
import io.xa.sigad.data.resourcePictures
import io.xa.sigad.screens.EmptyScreenContent
import io.xa.sigad.screens.camera.CameraKScreen
import io.xa.sigad.screens.camera.PickPhone
import io.xa.sigad.screens.detail.ImageSrcScreen
import sigad.composeapp.generated.resources.Res


@Composable
fun ListFileScreen(
    navigateToDetails: (objectId: String) -> Unit
) {
    val objects = resourcePictures;
    val navigator = LocalNavigator.currentOrThrow

    val imagePicker = rememberImagePicker(onImage = {
        //val imgSrc = ImageBitmapSrc(it)
        navigator.push(ImageSrcScreen(it))
    })
    // 🏆 步骤 1: 使用 Column 作为主布局
    Column(modifier = Modifier.fillMaxSize()) {

        // 🚀 步骤 2: 按钮行（Row）现在位于 Column 内部，AnimatedContent 外部
        Row(
            modifier = Modifier
                //.weight(1f)
                //.heightIn(min = 2.dp), // 例如，限制最小高度为 36.dp (默认值可能更高).fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 拍照按钮 (左边)
            Button(
                onClick = {
                    println("--- Navigator: Pushing CameraKScreen ---")
                    navigator.push(CameraKScreen())
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("📷 拍 照")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. 相册按钮 (右边)
            Button(
                onClick =  { imagePicker.pick() },

                modifier = Modifier.weight(1f)
            ) {
                Text("📂 相 册")
            }
        }

        // 🏆 步骤 3: AnimatedContent 占据 Column 的剩余空间
        AnimatedContent(
            targetState = objects.isNotEmpty(),
            modifier = Modifier.weight(1f) // 让 AnimatedContent 填充剩余空间
        ) { objectsAvailable ->
            if (objectsAvailable) {
                // 列表内容
                ObjectGrid(
                    objects = objects,
                    onObjectClick = navigateToDetails,
                )
            } else {
                // 空白内容
                EmptyScreenContent(Modifier.fillMaxSize())
            }
        }
    }
}
//
//@Composable
//fun ListFileScreen(
//    navigateToDetails: (objectId: String) -> Unit
//) {
//
//    val objects = resourcePictures;
//    val navigator = LocalNavigator.currentOrThrow
//
//    AnimatedContent(objects.isNotEmpty()) { objectsAvailable ->
//        // =========================================================
//        // 🚀 新增的按钮行 (替代了您原有的 ObejctGrid 上方的一行)
//        // =========================================================
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // 1. 拍照按钮 (左边)
//            Button(
//                onClick = {
//                    // 导航到相机屏幕
//                    println("--- Navigator: Pushing CameraKScreen ---")
//                    navigator.push(CameraKScreen())
//                },
//                modifier = Modifier.weight(1f)
//            ) {
//                Text("📷 拍 照")
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            // 2. 相册按钮 (右边)
//            Button(
//                onClick = {
//                    // TODO: 实现导航到相册屏幕或调用相册选择器
//                    println("TODO: 打开相册/文件选择器")
//                    // 示例: navigator.push(GalleryScreen())
//                },
//                modifier = Modifier.weight(1f)
//            ) {
//                Text("📂 相 册")
//            }
//        }
//
//        if (objectsAvailable) {
//            ObjectGrid(
//                objects = objects,
//                onObjectClick = navigateToDetails,
//            )
//        } else {
//            EmptyScreenContent(Modifier.fillMaxSize())
//        }
//    }
//}

@Composable
private fun ObjectGrid(
    objects: List<FileItem>,
    onObjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(180.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
    ) {
        items(objects, key = { it.resource }) { obj ->
            ObjectFrame(
                obj = obj,
                onClick = { onObjectClick(obj.resource) },
            )
        }
    }
}

@Composable
private fun ObjectFrame(
    obj: FileItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current


    //val painter = painterResource("drawable/${obj.thumbnailResource}")

    Column(
        modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model =  ImageRequest.Builder(context)
                .data(Res.getUri("drawable/${obj.thumbnailResource}")) // <--- Access via nested properties!
                .build(),

            contentDescription = obj.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.LightGray),
        )

        Spacer(Modifier.height(2.dp))

        Text(obj.name, style = MaterialTheme.typography.titleMedium)
        //Text(obj.artistDisplayName, style = MaterialTheme.typography.bodyMedium)
        //Text(obj.objectDate, style = MaterialTheme.typography.bodySmall)
    }
}
