package io.xa.sigad
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient

import android.content.Intent
import android.net.Uri
// androidMain

/////////////////////////////////////////////////////////////////////////
//open external browser
/////////////////////////////////////////////////////////////////////////
//// 实现 common/utils/HostPlatform.kt 中的 expect fun
actual fun hostOpenUrl(url: String) {
    if (!ActivityContextHolder.isInitialized) {
        // 建议使用日志工具输出警告
        println("Warning: ActivityContextHolder not initialized. Cannot open URL: $url")
        return
    }

    println("android hostOpenUrl ${url}")
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        // 必须设置 FLAG_ACTIVITY_NEW_TASK，因为我们是在没有 Activity 的上下文 (ViewModel/common code) 中启动 Activity
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        ActivityContextHolder.appContext.startActivity(intent)
    } catch (e: Exception) {
        // 例如，系统没有找到可以处理 ACTION_VIEW 的 Activity (没有浏览器)
        println("Error opening URL: $url. Reason: ${e.message}")
    }
}


/////////////////////////////////////////////////////////////////////////
//use webview to open url
///////////////////////////////////////////////////////////////////////////s
//@Composable
//actual fun WebViewComponent(url: String, onGoBack: () -> Unit) {
//    AndroidView(factory = { context ->
//        WebView(context).apply {
//            // 设置 WebView 客户端，确保在 App 内加载而不是跳转到浏览器
//            webViewClient = object : WebViewClient() {
//                // ... 可以添加 shouldOverrideUrlLoading 逻辑来处理内部链接 ...
//            }
//            // 启用 JavaScript
//            settings.javaScriptEnabled = true
//
//            // 加载 URL
//            loadUrl(url)
//        }
//    }, update = { webView ->
//        // 如果 URL 变化，重新加载
//        if (webView.url != url) {
//            webView.loadUrl(url)
//        }
//    })
//
//    // **注意：** 真正的返回逻辑 (onGoBack) 需要在 Navigation Composable 中实现，
//    // 例如监听 Android 的返回按钮，并在 WebView 无法返回时调用 onGoBack。
//}



@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebViewPage(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(url)
        }
    })
}
