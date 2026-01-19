package io.xa.sigad.wallet
//
//class WalletConnectClient {
//    suspend fun connect(): WalletSession {
//        // Use TrustWallet WalletConnect Kotlin SDK
//        // Generate QR code for manual connection
//        return TODO("Provide the return value")
//    }
//
//    suspend fun sendTransaction(to: String, amount: Int) {
//        // Build and send transaction via WalletConnect
//    }
//
//    suspend fun connectWallet(): WalletSession {
////        val connector = WalletConnectClient(...) // configure bridge, metadata
////        val session = connector.connect()
////        if (session.accounts.isEmpty()) {
////            // Prompt user to manually open Trust Wallet and scan QR
////        }
////        return session
//        return TODO("Provide the return value")
//    }
//
//}

//
//class WalletConnectClient(
//    private val connector: WalletConnect // from TrustWallet WalletConnect Kotlin SDK
//) {
//
//    suspend fun connect(): WalletSession {
//        val session = connector.connect()
//        return session
//    }
//
//    suspend fun sendTransaction(
//        from: String,
//        to: String,
//        amountWei: BigInteger,
//        chainId: Int = 1 // Ethereum mainnet
//    ) {
//        val tx = WalletConnectTransaction(
//            from = from,
//            to = to,
//            value = amountWei.toString(16), // hex string
//            gas = "0x5208", // 21000 gas
//            gasPrice = "0x3B9ACA00", // 1 gwei
//            data = "0x"
//        )
//        connector.ethSendTransaction(session, tx)
//    }
//}
//
//package com.mykmpapp // 替换为您的应用包名
//
//import android.app.Application
//// 导入新的 Reown Core/AppKit 包
//import com.reown.android.core.Core
//import com.reown.appkit.AppKit
//import com.reown.appkit.client.Modal
//import com.reown.appkit.client.AppKit as AppKitClient // 重命名 AppKit 客户端以避免冲突
//
//// 实际对象实现通用期望
//actual object WalletConnectSetup {
//
//    // ⚠️ 必须在调用 initialize() 之前设置 Application Context
//    private var applicationInstance: Application? = null
//
//    fun setApplication(application: Application) {
//        this.applicationInstance = application
//    }
//
//    actual fun initialize(
//        projectId: String,
//        metadata: AppMetadata,
//        deepLinkUri: String
//    ) {
//        val application = applicationInstance
//        if (application == null) {
//            println("WalletConnect initialization failed: Application context not set.")
//            return
//        }
//
//        // 1. 映射 commonMain 的数据模型到 WC SDK 的模型 (Core.Model 保持不变)
//        val wcMetadata = Core.Model.AppMetaData(
//            name = metadata.name,
//            description = metadata.description,
//            url = metadata.url,
//            icons = metadata.icons,
//            // Deep Link 用于从外部钱包返回
//            redirect = deepLinkUri
//        )
//
//        // 2. 初始化 Core Client
//        Core.initialize(
//            projectId = projectId,
//            application = application,
//            metaData = wcMetadata,
//        ) { error ->
//            println("Android Core Init Error: ${error.throwable.localizedMessage}")
//        }
//
//        // 3. 初始化 AppKit Client (dApp 端)
//        // 注意：AppKitClient 是 com.reown.appkit.AppKit 的别名
//        val initParams = Modal.Params.Init(core = Core)
//        AppKitClient.initialize(initParams) { error ->
//            println("Android AppKit Init Error: ${error.throwable.localizedMessage}")
//        }
//
//        // 4. 设置 AppKit 代理来接收事件（会话连接、断开等）
//        AppKitClient.setDelegate(AppKitDelegateImpl()) // 修正了 setDelegate 的调用
//
//        println("✅ Android AppKit Initialized.")
//    }
//}
//
//// 简单的代理实现 (用于监听会话状态), 使用新的 AppKit Delegate
//class AppKitDelegateImpl : AppKitClient.Delegate {
//    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
//        println("Session Approved: ${approvedSession.topic}")
//        // 在此处更新您的 UI/状态
//    }
//
//    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
//        println("Session Rejected.")
//    }
//
//    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
//        println("Session Deleted: ${deletedSession.topic}")
//    }
//
//    // ... 实现所有必需的方法
//}