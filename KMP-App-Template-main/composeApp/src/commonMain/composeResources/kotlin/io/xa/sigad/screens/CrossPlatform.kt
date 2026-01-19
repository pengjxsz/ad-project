package io.xa.sigad

import androidx.compose.runtime.Composable

val reownProjectId :String = "3295917dc4c50eaf2208e6ebb3dcc32f"

//val baseWalletDeepLinkURL :String = "https://link.trustwallet.com/wc"
val baseWalletDeepLinkURL :String = "https://metamask.app.link/wc"


val trustedDomain :String = "legal.adpal.xyz" //""111.89-1011.com"
val universalAppLink :String  = "https://${trustedDomain}/sigad"  //https://111.89-1011.com/sigad
val AppSchemaAndroid :String = "android-sigad-wc"
val AppSchemaIOS :String  = "kotlin-sigad-wc"
val WalletAPPName : String = "Metamask" //Trust Wallet
/**
 * 宿主平台接口：用于在系统浏览器中打开 URL。
 * 这是一个 expect 接口，要求所有目标平台实现其 actual 逻辑。
 *
 * @param url 要打开的完整 URL 字符串 (例如: "https://google.com")
 */
expect fun hostOpenUrl(url: String)
//
//{
//    // 在 Android 宿主中：
//    // context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//
//    // 在 iOS 宿主中：
//    // NavController.present(SFSafariViewController(url))
//
//    // 关键是：在 WebView 关闭后，你可能需要调用 viewModel.checkTaskCompletion(taskId)
//    // 来进行最终的广告点击上报和验证。
//}

///**
// * Common 期望：定义平台必须提供的 WebView 渲染组件
// *
// * @param url 要加载的 URL
// * @param onGoBack 通知宿主平台 WebView 内部操作要求返回
// */
//@Composable
//expect fun WebViewComponent(url: String, onGoBack: () -> Unit)


// expect declaration
@Composable
expect fun WebViewPage(url: String)