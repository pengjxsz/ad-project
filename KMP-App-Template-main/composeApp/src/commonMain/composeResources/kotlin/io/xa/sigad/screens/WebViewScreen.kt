package io.xa.sigad.screens
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import io.xa.sigad.WebViewComponent
//
//// commonMain/kotlin/ui/WebViewScreen.kt
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun WebViewScreen(
//    url: String,
//    // 宿主导航回调，用于处理 "返回" 操作
//    onBack: () -> Unit
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Web View") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        // 调用平台实现的 WebView 组件
//        WebViewComponent(url = url) {
//            // 当 WebView 内部逻辑需要返回时，调用宿主的 onBack
//            onBack()
//        }
//    }
//}

// commonMain
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import cafe.adriel.voyager.core.screen.Screen
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Modifier
import io.xa.sigad.WebViewPage

//class WebViewScreen( val url: String, val title: String) : Screen {
//    @Composable
//    override fun Content() {
//        WebViewPage(url)
//    }
//}


// commonMain/screens/WebviewScreen.kt



data class WebViewScreen(val url: String, val title: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    // 🚀 关键：使用 navigationIcon 添加返回按钮
                    navigationIcon = {
                        BackButton(onBack = { navigator.pop() }) // 调用 voyager 的 pop
                    }
                )
            }
        ) { padding ->
            // TODO: 在这里放置实际的平台 WebView 实现
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                // ActualWebView(url = url, modifier = Modifier.fillMaxSize())
                WebViewPage(url)

                // 占位符
                //Text("加载网页: $url", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

// 辅助回退按钮 Composable
// 如果这个函数已在其他文件定义，请确保已正确导入
@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
    }
}
