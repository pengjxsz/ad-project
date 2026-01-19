package io.xa.sigad.wallet
// composeApp/src/iosMain/kotlin/io/xa/sigad/IosAppkitBridge.kt

//package io.xa.sigad

import io.xa.sigad.wallet.AppkitInitParams
import io.xa.sigad.wallet.WalletConnectionState
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSString
import platform.darwin.NSObject

// -------------------------------------------------------------------
// 1. AppkitManagerBridge: 供 Swift/Obj-C 实现的接口
//    它负责封装纯 Swift 库（ReownAppkit）的所有直接操作。
//    注意：StateFlow 不能直接在接口中桥接，因此我们使用回调或一个通用的 State 属性。
// -------------------------------------------------------------------

interface AppkitManagerBridge {

    // 初始化方法
    fun initialize(params: AppkitInitParams): Boolean

    // 连接钱包 (WalletConnect 相关的逻辑)
    fun connectToTrustWallet()

    // 断开连接
    fun disconnect()

    // 状态回调：由 Swift 端在连接状态变化时调用
    // (newState: 状态名, address: 钱包地址, topic: WC Topic, error: 错误信息)
    fun setWalletStateCallback(callback: (WalletConnectionState) -> Unit)
    // 交易和余额方法
    fun fetchBalances(address: String): List<String> // 简化为 List<String>，TokenBalance 涉及复杂对象桥接
    fun sendTransaction(transactionParam: String): Boolean
    fun generateReceiveQRCode(address: String): String
}

// -------------------------------------------------------------------
// 2. 运行时注入容器 (Injection Container)
// -------------------------------------------------------------------

// 这是一个全局变量，在 iOS App 启动时，由 Swift 代码设置其实例。
lateinit var AppkitBridgeInstance: AppkitManagerBridge