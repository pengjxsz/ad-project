package io.xa.sigad.screens.camera

// commonMain/CameraKScreen.kt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kashif.cameraK.controller.CameraController// 请注意：CameraK 库的包名可能依赖于你导入的具体版本

import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.attafitamim.krop.core.images.ImageBitmapSrc
import com.attafitamim.krop.core.images.ImageSrc
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.enums.FlashMode
import com.kashif.cameraK.enums.ImageFormat
import com.kashif.cameraK.enums.QualityPrioritization
import com.kashif.cameraK.enums.TorchMode
import com.kashif.cameraK.permissions.Permissions
import com.kashif.cameraK.permissions.providePermissions
import com.kashif.cameraK.result.ImageCaptureResult
import com.kashif.cameraK.ui.CameraPreview
import io.xa.sigad.screens.detail.CropFileScreen
import io.xa.sigad.screens.detail.ImageSrcScreen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

//import com.kashif.imagesaverplugin.rememberImageSaverPlugin
// === 1. Voyager Screen 容器 ===
class CameraKScreen : Screen {
    @Composable
    override fun Content() {
        println("--- CameraKScreen: Content Composable Entered ---")
        val navigator = LocalNavigator.currentOrThrow
        val permissions: Permissions = providePermissions()

        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
        ) {
            val cameraPermissionState =
                remember { mutableStateOf(permissions.hasCameraPermission()) }
            val storagePermissionState =
                remember { mutableStateOf(permissions.hasStoragePermission()) }

            // CameraK Controller 和 ImageSaverPlugin 的状态
            val cameraController = remember { mutableStateOf<CameraController?>(null) }
//            val imageSaverPlugin = rememberImageSaverPlugin(
//                config = ImageSaverConfig(
//                    isAutoSave = false,
//                    prefix = "MyApp",
//                    directory = Directory.PICTURES,
//                    customFolderName = "CustomFolder"
//                )
//            )

            // 权限处理 (使用你原有的 PermissionsHandler)
            PermissionsHandler(
                permissions = permissions,
                cameraPermissionState = cameraPermissionState,
                storagePermissionState = storagePermissionState
            )

            // 当权限全部通过后，显示 Camera 内容
            if (cameraPermissionState.value && storagePermissionState.value) {
                CameraContent(
                    cameraController = cameraController,
                    //imageSaverPlugin = null,
                    // 增加返回按钮功能
                    onBack = { navigator.pop() }
                )
            } else {
                // 如果权限未授予，显示一个占位符或提示
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("需要相机和存储权限才能使用。", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun PermissionsHandler(
    permissions: Permissions,
    cameraPermissionState: MutableState<Boolean>,
    storagePermissionState: MutableState<Boolean>
) {
    if (!cameraPermissionState.value) {
        permissions.RequestCameraPermission(
            onGranted = { cameraPermissionState.value = true },
            onDenied = { println("Camera Permission Denied") }
        )
    }

    if (!storagePermissionState.value) {
        permissions.RequestStoragePermission(
            onGranted = { storagePermissionState.value = true },
            onDenied = { println("Storage Permission Denied") }
        )
    }
}

private suspend fun handleImageCapture(
    cameraController: CameraController,
    //imageSaverPlugin: ImageSaverPlugin,
    onImageCaptured: (ImageBitmap) -> Unit
) {
    when (val result = cameraController.takePicture()) {
        is ImageCaptureResult.Success -> {
            println(" handleImageCaputer ok....")
            val bitmap = result.byteArray.decodeToImageBitmap()
            onImageCaptured(bitmap)


//            if (!imageSaverPlugin.config.isAutoSave) {
//                val customName = "Manual_${Uuid.random().toHexString()}"
//                imageSaverPlugin.saveImage(
//                    byteArray = result.byteArray,
//                    imageName = customName
//                )?.let { path ->
//                    println("Image saved at: $path")
//                }
//            }
        }

        is ImageCaptureResult.Error -> {
            println("Image Capture Error: ${result.exception.message}")
        }
    }
}




// === 2. 修改 CameraContent 以接受 onBack 回调 ===
//@Composable
//private fun CameraContent(
//    cameraController: MutableState<CameraController?>,
//    //imageSaverPlugin: ImageSaverPlugin?,
//    onBack: () -> Unit // 新增的返回回调
//) {
//    val coroutineScope = rememberCoroutineScope()
//
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        CameraPreview(
//            modifier = Modifier.fillMaxSize(),
//            cameraConfiguration = {
//                setCameraLens(CameraLens.BACK)
//                setFlashMode(FlashMode.OFF)
//                setImageFormat(ImageFormat.JPEG)
//                setDirectory(Directory.PICTURES)
//                setTorchMode(TorchMode.OFF)
//                setQualityPrioritization(QualityPrioritization.QUALITY)
//               // addPlugin(imageSaverPlugin)
//            },
//            onCameraControllerReady = {
//                print("==> Camera Controller Ready")
//                cameraController.value = it
//
//            }
//        )
//
//        cameraController.value?.let { controller ->
//            coroutineScope.launch {
//                handleImageCapture(
//                    cameraController = controller,
//                    onImageCaptured = { /* ... */ }
//                )
//            }
//        }
//    }

@Composable
private fun CameraContent(
    cameraController: MutableState<CameraController?>,
    onBack: () -> Unit // 新增的返回回调
) {
    val coroutineScope = rememberCoroutineScope()
    // 获取当前的 CameraController 实例，如果已准备好
    val controller = cameraController.value
    val navigator = LocalNavigator.currentOrThrow

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. 相机预览 (CameraPreview)
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            cameraConfiguration = {
                setCameraLens(CameraLens.BACK)
                setFlashMode(FlashMode.OFF)
                setImageFormat(ImageFormat.JPEG)
                setDirectory(Directory.PICTURES)
                setTorchMode(TorchMode.OFF)
                setQualityPrioritization(QualityPrioritization.QUALITY)
                // addPlugin(imageSaverPlugin)
            },
            onCameraControllerReady = {
                print("==> Camera Controller Ready")
                cameraController.value = it // 设置控制器实例
            }
        )

        // 2. 顶部控件 (回退按钮)
        // 这一部分总是可见的，因为它不依赖于 controller 是否准备好
        TopControls(onBack = onBack, modifier = Modifier.align(Alignment.TopCenter))

        // 3. 底部控件 (拍照按钮)
        // 只有当 controller 准备好时，拍照按钮才应该被激活/显示
        if (controller != null) {
            BottomControls(
                // 按钮被点击时，在协程中执行拍照操作
                onCapture = {
                    coroutineScope.launch {
                        handleImageCapture(
                            cameraController = controller,
                            onImageCaptured = {
                                val imgSrc = ImageBitmapSrc(it)
                                navigator.push(ImageSrcScreen(imgSrc))
                            }
                        )
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            // 可以在这里显示加载提示
            Text("正在启动相机...", modifier = Modifier.align(Alignment.Center))
        }
    }
}

// 回退按钮 (通常位于顶部)
@Composable
fun TopControls(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // 回退按钮
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White // 确保在深色相机预览上有足够的对比度
            )
        }
    }
}

// 拍照按钮 (通常位于底部)
@Composable
fun BottomControls(onCapture: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        // 拍照按钮
        Button(
            onClick = onCapture,
            // 可选：让按钮更大更圆
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            // 这里可以放一个图标或只留空，让按钮像一个圆圈
        }
    }
}

