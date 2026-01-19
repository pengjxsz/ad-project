package io.xa.sigad

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKWebView
import platform.Foundation.NSURLRequest
import platform.WebKit.*
import platform.Foundation.*
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGFloat

actual fun hostOpenUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return

    // 访问主线程并使用 UIApplication.sharedApplication() 打开 URL
    if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
        UIApplication.sharedApplication.openURL(nsUrl)
        // 注意：Swift/Objective-C API 有时需要一个 completion handler，但对于简单的 openURL，这个 KMP 包装通常足够。
    } else {
        println("Error: Cannot open URL on iOS: $url")
    }
}


// iosMain/kotlin/ui/WebViewComponentActual.kt


//@Composable
//@OptIn(ExperimentalForeignApi::class)
//actual fun WebViewComponent(url: String, onGoBack: () -> Unit) {
//    UIKitView(
//        factory = {
//            WKWebView().apply {
//                val nsUrl = NSURL.URLWithString(url)
//                if (nsUrl != null) {
//                    val request = NSURLRequest.requestWithURL(nsUrl)
//                    this.loadRequest(request)
//                }
//            }
//        },
//        update = { webView ->
//            // 如果 URL 变化，重新加载
//        },
//        modifier = // ... 适当的 Modifier ...
//    )
//    // **注意：** iOS 的返回逻辑也依赖于宿主的 Navigation Stack。
//}


//@OptIn(ExperimentalForeignApi::class)
//val zeroRect = CGRect(
//    origin = platform.CoreGraphics.CGPointMake(0.0, 0.0),
//    size   = platform.CoreGraphics.CGSizeMake(0.0, 0.0)
//)

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WebViewPage(url: String) {
    UIKitView(
        factory = {
            val config = WKWebViewConfiguration().apply {
                preferences.javaScriptEnabled = true
                allowsInlineMediaPlayback = true
            }

            val webView = WKWebView()
            //val webView = WKWebView(frame = CGRectZero.readValue(), configuration = config)
            //            webView.navigationDelegate = object : NSObject(), WKNavigationDelegateProtocol {
            //                override fun webView(
            //                    webView: WKWebView,
            //                    didFinishNavigation: WKNavigation?
            //                ) {
            //                    println("Finished loading: ${webView.URL?.absoluteString}")
            //                }
            //            }

            val nsUrl = NSURL(string = url)
            if (true) {
                webView.loadRequest(NSURLRequest(nsUrl))
            }

            webView
        },
        modifier = Modifier
            .fillMaxSize()
    )
    // 应用 padding，确保内容不被 TopBar 或系统栏遮挡
}

