package io.xa.sigad.wallet

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.xa.sigad.data.model.TokenBalance
import kotlinx.coroutines.flow.StateFlow


// --- 1. 数据模型定义 (Data Models) ---

data class WalletAccount(
    val namespace: String = "", //ex: eip155

    /// A reference string that identifies a blockchain within a given namespace.
    val  reference: String = "", //ex: 85432

    /// The account's address specific to the blockchain.
    val  address: String = "",

    //val topic : String = ""
)

/**
 * 钱包连接状态, connectstate update WalletStat
 * @param address 连接成功后的钱包地址
 * @param isConnected 是否已连接
 */
data class WalletState(
    val address: String? = null,
    val topic: String?=null,
    val chainName : String?=null,
    val isConnected: Boolean = false
)
sealed class WalletConnectionState {
    object Disconnected : WalletConnectionState()
    object Connecting : WalletConnectionState()

    // ... 其他状态
    //data class Connected(val address: String, val topic: String) : WalletConnectionState()
    data class Connected(val topic: String, val accounts: List<WalletAccount>) : WalletConnectionState()
    data class Error(val message:String) : WalletConnectionState()
    data class ResError(val code: Long, val message :String): WalletConnectionState()
}

/**
 * Appkit 初始化所需元数据，对应 Reown Appkit Core.Model.AppMetaData
 */
data class AppMetaData(
    val name: String,
    val description: String,
    val url: String,
    val icons: List<String>,
    val redirect: String,
    val appLink: String
)

/**
 * Appkit 初始化参数
 */
data class AppkitInitParams(
    val projectId: String,
    val connectionType: String, // 简化为字符串表示连接类型
    val metaData: AppMetaData
)

// --- 2. 钱包服务接口 (在 commonMain 中定义) ---

/**
 * 钱包服务接口 (EXPECTED Interface)
 */
interface WalletService {
    var isInitialized: Boolean

    // 暴露状态流。StateFlow 本身是多平台兼容的。
    val walletState: StateFlow<WalletConnectionState>
    /**
     * 初始化 Appkit SDK，必须在其他操作前调用。
     */
    suspend fun initialize(params: AppkitInitParams): Boolean

    suspend fun connectToTrustWallet()
    suspend fun disconnect()
    suspend fun fetchBalances(address: String): List<TokenBalance>
    //suspend fun sendTransaction(address: String, symbol: String, amount: Double): Boolean
    suspend fun sendTransaction(transactionParam: String): Boolean
    fun generateReceiveQRCode(address: String): String
}

// ----------------------------------------------------
// 🎯 KMP 依赖注入：声明期望依赖 (EXPECTED Dependency)
// ----------------------------------------------------
expect val walletService: WalletService
// ----------------------------------------------------

// --- 6. UI 核心组件 ---

