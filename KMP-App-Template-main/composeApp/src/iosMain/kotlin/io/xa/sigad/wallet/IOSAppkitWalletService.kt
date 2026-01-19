package io.xa.sigad.wallet

// 导入 CommonMain 中的图标
// iosMain/kotlin/AppkitWalletService.kt

// 假设这些是 commonMain 中的类
// 导入您自己的 Framework 模块名称
// 假设您的 Framework baseName 是 "ComposeApp"

import androidx.compose.ui.graphics.Color
import io.xa.sigad.data.model.TokenBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

import io.xa.sigad.wallet.AppkitBridgeInstance
import io.xa.sigad.wallet.AppkitManagerBridge
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import io.xa.sigad.platformCoroutineScope
import io.xa.sigad.screens.wallet.MoneyIcon
import io.xa.sigad.screens.wallet.WalletIcon
import io.xa.sigad.screens.wallet.UsdtIcon

import org.koin.mp.KoinPlatform.getKoin

///////////////////////////////////////////////////////////////////
// Backing field for the iOS WalletService
//20251204 USING KNIO INSTEAD
///////////////////////////////////////////////////////////////////
//private var iosWalletServiceInstance: WalletService? = null
//
//// Public accessor, throws if not initialized
//actual val walletService: WalletService
//    get() = iosWalletServiceInstance
//        ?: throw IllegalStateException("WalletService must be initialized via initIosWalletService() before use.")
//
//// Initialization function to be called from Swift/iOS app startup
//fun initIosWalletService(service: WalletService) {
//    iosWalletServiceInstance = service
//}

actual val walletService: WalletService
    get() = getKoin().get<WalletService>() // Koin 会在第一次调用时提供已创建的单例

// ----------------------------------------------------
// 实际依赖声明 --- back for reference
// ----------------------------------------------------
//actual val walletService: WalletService = IOSAppkitWalletService()
//// Initialization function to be called from Swift/iOS app startup
//fun initIosWalletService(service: WalletService) {
//    walletService = service
//}

// ----------------------------------------------------
// 🎯 KMP 依赖注入：声明实际依赖 (actual)
// ----------------------------------------------------

// 实现 WalletService 接口
class IOSAppkitWalletService : WalletService {

    // 使用注入的桥接实例
    private val appkitBridge: AppkitManagerBridge
        get() = AppkitBridgeInstance

    // 状态流的内部可变版本
    private val _walletState :MutableStateFlow<WalletConnectionState> = MutableStateFlow(WalletConnectionState.Disconnected)
    override val walletState: StateFlow<WalletConnectionState> = _walletState.asStateFlow()

    override var isInitialized: Boolean = false
        set

    init {
        // 在初始化时，设置 Swift 桥接层的回调函数
        // 当 Swift 侧状态变化时，它会通知这个 Kotlin 闭包
        appkitBridge.setWalletStateCallback { state, ->
            // 在 Kotlin 协程中更新状态流，确保线程安全
            platformCoroutineScope.launch {
                //_walletState.value = newState as WalletConnectionState.Disconnected
                _walletState.value = state
                // 可以在这里处理地址和错误信息
            }
        }

    }

    override suspend fun initialize(params: AppkitInitParams): Boolean {
        // 调用桥接层的方法
        val success = appkitBridge.initialize(params)
        isInitialized = success
        return success
    }

    override suspend fun connectToTrustWallet() {
        // 挂起函数不能直接调用非挂起桥接函数，但我们可以简单封装
        appkitBridge.connectToTrustWallet()
    }

    override suspend fun disconnect() {
        println(" wallet disconnectWallet ios implement...")

        appkitBridge.disconnect()
    }

    override suspend fun fetchBalances(address: String): List<TokenBalance> {
        // 假设桥接层返回 List<String>，这里需要进行转换
        return listOf(
            TokenBalance(
                "Ethereum",
                "ETH",
                "2.3456",
                WalletIcon,
                Color(0xFF627EEA)
            ),
            TokenBalance(
                "USD Coin",
                "USDC",
                "3500.78",
                MoneyIcon,
                Color(0xFF2775CA)
            ),
            TokenBalance(
                "UST Coin",
                "Tether",
                "1200.78",
                UsdtIcon,
                Color(0xFF2775CA)
            ),

            )
    }

    override suspend fun sendTransaction(transactionParam: String): Boolean {
        return appkitBridge.sendTransaction(transactionParam)
    }

    override fun generateReceiveQRCode(address: String): String {
        return appkitBridge.generateReceiveQRCode(address)
    }
}


/**
 * minimum implement to test compiling
// ----------------------------------------------------
// 🎯 KMP 依赖注入：声明实际依赖 (actual)
// ----------------------------------------------------

// Backing field for the iOS WalletService
private var iosWalletServiceInstance: WalletService? = null

// Public accessor, throws if not initialized
actual val walletService: WalletService
get() = iosWalletServiceInstance
?: throw IllegalStateException("WalletService must be initialized via initIosWalletService() before use.")

// Initialization function to be called from Swift/iOS app startup
fun initIosWalletService(service: WalletService) {
iosWalletServiceInstance = service
}
//val  iosm : IOSAppkitManager = IOSAppkitManager()
// 确保导入路径是您的 Xcode Product Module Name.SwiftClassName

class IOSAppkitWalletService(
override var isInitialized: Boolean,
override val walletState: StateFlow<WalletConnectionState>
) : WalletService{
// ...
private val appkitManager: IOSAppkitManager = IOSAppkitManager()
override suspend fun initialize(params: AppkitInitParams): Boolean {
TODO("Not yet implemented")
}

override suspend fun connectToTrustWallet() {
TODO("Not yet implemented")
}

override suspend fun disconnect() {
TODO("Not yet implemented")
}

override suspend fun fetchBalances(address: String): List<TokenBalance> {
TODO("Not yet implemented")
}

override suspend fun sendTransaction(transactionParam: String): Boolean {
TODO("Not yet implemented")
}

override fun generateReceiveQRCode(address: String): String {
TODO("Not yet implemented")
}
// ...
}

 */


/** 2025/12/03 WALLETSERVICE -> IOS ACTRUAL IMPLEMENT -> OBJECTC-> SWIFT
 *
 * iOS 平台的 WalletService 实际实现。
 * 在实际项目中，它会依赖于一个 Swift/Objective-C 模块来处理 Trust Wallet SDK 的调用。
 */
//class IosAppkitWalletService : WalletService {
//
//    override var isInitialized: Boolean = false
//    private var initParams: AppkitInitParams? = null
//
//    override suspend fun initialize(params: AppkitInitParams): Boolean {
//        // 实际操作:
//        // 1. 调用 Swift 中的包装器方法，该方法将调用 Reown Appkit Core SDK 的初始化方法。
//        //    ReownCoreClient.shared.initialize(params.projectId, params.metaData, params.connectionType)
//
//        println("iOS: Initializing Reown Appkit SDK for project ${params.projectId}")
//
//        // 模拟初始化成功
//        initParams = params
//        isInitialized = true
//        return true
//    }
//
//    override suspend fun connectToTrustWallet(): WalletState {
//        if (!isInitialized) {
//            throw IllegalStateException("iOS WalletService must be initialized first.")
//        }
//
//        // 实际操作:
//        // 1. 构建 Trust Wallet Deep Link / Universal Link URL。
//        // 2. 调用 Swift 函数来打开 URL（UIApplication.shared.open）。
//        val connectUrl = buildTrustWalletConnectUrl(initParams!!.metaData.redirect)
//
//        println("iOS: Launching Trust Wallet for connection via URL: $connectUrl")
//
//        // ⚠️ 关键点: 真实连接是异步的，需要等待 Trust Wallet App 返回结果 (通过 deep link)。
//        // 模拟: 等待 3 秒后，假设连接成功并返回一个地址。
//        kotlinx.coroutines.delay(3000)
//
//        // 假设 Trust Wallet 连接成功
//        val mockAddress = "0x" + List(40) {
//            "0123456789abcdef"[kotlin.random.Random.nextInt(16)]
//        }.joinToString("")
//        // 确保 iOS 地址和 Android 地址不同，以证明平台差异
//        val iosAddress = mockAddress.replace('a', 'b').replace('c', 'd')
//
//        return WalletState(address = iosAddress, isConnected = true)
//    }
//
//    override suspend fun disconnect(): WalletState {
//        // 实际操作: 调用 Swift/Appkit SDK 的断开连接方法
//        println("iOS: Disconnecting wallet via Appkit SDK...")
//        // AppkitClient.shared.disconnect()
//        return WalletState(address = null, isConnected = false)
//    }
//
//    override suspend fun fetchBalances(address: String): List<TokenBalance> {
//        // 实际操作: 调用 Swift/Appkit SDK 接口获取余额
//        println("iOS: Fetching real balances from Appkit for $address...")
//
//        // 返回模拟数据
//        return listOf(
//            // 稍作修改以体现这是 iOS 平台的模拟数据
//            TokenBalance("Ethereum", "ETH", 0.5123, WalletIcon, Color(0xFF627EEA)),
//            TokenBalance("Tether USD", "USDT", 980.11, MoneyIcon, Color(0xFF26A17B)),
//            TokenBalance("Polkadot", "DOT", 55.0, WalletIcon, Color(0xFFE6007A)),
//        )
//    }
//
//    override suspend fun sendTransaction(address: String, symbol: String, amount: Double): Boolean {
//        // 实际操作: 调用 Swift/Appkit SDK 签名和发送交易
//        println("iOS: Sending transaction via Appkit SDK...")
//        // AppkitClient.shared.sendTransaction(...)
//
//        // 模拟 Trust Wallet App 启动和返回签名结果
//        kotlinx.coroutines.delay(2500)
//        // iOS 平台有 80% 的概率成功
//        return kotlin.random.Random.nextInt(10) < 8
//    }
//
//    override fun generateReceiveQRCode(address: String): String {
//        // 实际操作: Trust Wallet 的 QR 码通常是标准的 deep link 格式
//        return "ios-wallet-receive:$address"
//    }
//
//    // 辅助函数: 构建 Trust Wallet 连接 URL (模拟 Deep Link 构造)
//    private fun buildTrustWalletConnectUrl(redirectUri: String): String {
//        return "trust://wc?uri=WAKU_PROTOCOL_PAYLOAD&redirect=$redirectUri"
//    }
//}

/**
 * 实际的 expect/actual 实现，返回 iOS 平台的 WalletService 实例。
 */
//actual val walletService: WalletService = IosAppkitWalletService()


// iOS 不需要像 Android 那样传入 Context，但可能需要传入一个 UIApplicationDelegate
// 这里我们先简化，假设初始化所需的参数可以直接获取或不依赖外部传入。
// 如果 AppKit.initialize 需要依赖 UIApplication，则需要在实际项目中考虑如何传入。
/**
 * 钱包服务接口 (ACTUAL Implementation for iOS)
 * 这是一个桥接类，它调用实际的 Swift 实现。
 *
 * 注意：由于 AppKit iOS SDK 是纯 Swift，不带 @objc，
 * 且 AppKit 的连接和回调机制基于 Delegate/Closure，
 * 我们需要一个 Swift 类 (例如: IOSAppkitManager) 来封装 Reown AppKit 的 Swift API，
 * 并提供一个简洁的 @objc 桥接接口供 Kotlin 调用。
 * * ⚠️ 警告: AppKit 的 Swift API 不能直接在 Kotlin/Native 中使用，
 * 必须通过一个 Objective-C/Swift 桥接层。
 * 我在这里假设您已经创建了一个 `IOSAppkitManager` Swift 类，
 * 它暴露了 @objc 兼容的方法供 Kotlin 调用。
 */
/*
class IOSAppkitWalletService : WalletService {
    private val _walletState = MutableStateFlow<WalletConnectionState>(WalletConnectionState.Disconnected)
    override val walletState: StateFlow<WalletConnectionState> = _walletState.asStateFlow()

    override var isInitialized: Boolean = false
    private var initParams: AppkitInitParams? = null

    // 假设这是一个 Swift 类，它封装了所有 AppKit 的 Swift API，
    // 并将回调通过 KMP 兼容的方式（例如，简单回调）暴露给 Kotlin。
    // 这个类需要是 @objcMembers 或继承自 NSObject 才能被 Kotlin/Native 看到。
//    private val appkitManager: IOSAppkitManager = IOSAppkitManager(
//        // 将状态更新的 Lambda 传入 Swift Manager
//        onStateChange = { newState, address, topic, error ->
//            MainScope().launch {
//                when (newState) {
//                    "Connected" -> _walletState.value = WalletConnectionState.Connected(address!!, topic!!)
//                    "Disconnected" -> _walletState.value = WalletConnectionState.Disconnected
//                    "Connecting" -> _walletState.value = WalletConnectionState.Connecting
//                    "Error" -> _walletState.value = WalletConnectionState.Error(error ?: "Unknown error")
//                    "ResError" -> {
//                        // 假设 error 格式为 "code:message"
//                        val parts = error?.split(":", limit = 2)
//                        val code = parts?.getOrNull(0)?.toIntOrNull() ?: -1
//                        val message = parts?.getOrNull(1) ?: "Transaction error"
//                        _walletState.value = WalletConnectionState.ResError(code, message)
//                    }
//                }
//            }
//        }
//    )

    override suspend fun initialize(params: AppkitInitParams): Boolean {
        initParams = params

        // 调用 Swift Manager 的初始化方法
//        val success = appkitManager.initialize(
//            projectId = params.projectId,
//            name = params.metaData.name,
//            description = params.metaData.description,
//            url = params.metaData.url,
//            icons = params.metaData.icons,
//            redirect = params.metaData.redirect
//        )
        val success = true
        isInitialized = success
        println("iOS: Initializing Reown Appkit SDK for project ${params.projectId}")
        return success
    }

    override suspend fun connectToTrustWallet() {
        if (!isInitialized) {
            _walletState.value = WalletConnectionState.Error("AppKit not initialized")
            return
        }

        _walletState.value = WalletConnectionState.Connecting

        // AppKit.connect 的逻辑被封装在 Swift Manager 中
        //val wcUri = appkitManager.connect()
        val wcUri = ""
        if (wcUri != null) {
            // 在 iOS 中，通过 Universal Link 打开 Trust Wallet
            openTrustWallet(wcUri)
        } else {
            _walletState.value = WalletConnectionState.Error("Failed to create WalletConnect URI")
        }
    }

    override suspend fun disconnect() {
       // appkitManager.disconnect()
        _walletState.value = WalletConnectionState.Disconnected
    }

    override suspend fun fetchBalances(address: String): List<TokenBalance> {
        // 实际调用 appkitManager 的实现
        // 由于这是一个耗时操作，通常也需要通过 Swift 的异步/回调机制来桥接
        //val jsonBalances = appkitManager.fetchBalances(address)
        // ⚠️ 需要将返回的 JSON 字符串反序列化为 List<TokenBalance>
        // 为了简洁，这里返回一个空列表
//        println("iOS: Fetching balances for $address. Result: $jsonBalances")
        return emptyList()
    }

    override suspend fun sendTransaction(transactionParam: String): Boolean {
        // transactionParam 应该是 JSON 格式的 TransactionRequest
//        appkitManager.sendTransaction(transactionParam)
        // 交易结果会通过 onStateChange -> WalletConnectionState.ResError/Connected 来更新
        // 这里返回 true 表示请求已发送
        return true
    }

    override fun generateReceiveQRCode(address: String): String {
        // 简单返回一个示例 URI
        return "ethereum:$address"
    }

    // 辅助函数：在 iOS 中打开 Trust Wallet APP
    private fun openTrustWallet(wcUri: String) {
        val encodedUri = wcUri.urlEncoded()
        val deepLink = "https://link.trustwallet.com/wc?uri=$encodedUri"

        // 使用 UIApplication.sharedApplication() 打开 URL
        val url = NSURL(string = deepLink)
        UIApplication.sharedApplication().openURL(url)
    }

    // URL 编码的扩展函数
    private fun String.urlEncoded(): String {
//        return this.stringByAddingPercentEncodingWithAllowedCharacters(
//            NSCharacterSet.URLQueryAllowedCharacterSet
//        ) ?: this
        return ""
    }
}

*/
