package io.xa.sigad.wallet
/*
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.flow.StateFlow

// 导入 Compose Graphics API 用于自定义图标
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.painterResource
import qrgenerator.qrkitpainter.rememberQrKitPainter
import sigad.composeapp.generated.resources.Res

import kotlin.math.pow
import com.ionspin.kotlin.bignum.integer.BigInteger

// --- 0. 自定义 SVG 路径图标定义 (Custom Inline SVG Icons) ---

val WalletIcon: ImageVector
    get() = Builder(
        name = "Wallet",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(21.0f, 18.0f)
            horizontalLineTo(3.0f)
            curveTo(1.9f, 18.0f, 1.0f, 17.1f, 1.0f, 16.0f)
            verticalLineTo(8.0f)
            curveTo(1.0f, 6.9f, 1.9f, 6.0f, 3.0f, 6.0f)
            horizontalLineTo(21.0f)
            curveTo(22.1f, 6.0f, 23.0f, 6.9f, 23.0f, 8.0f)
            verticalLineTo(16.0f)
            curveTo(23.0f, 17.1f, 22.1f, 18.0f, 21.0f, 18.0f)
            close()
            moveTo(11.0f, 8.0f)
            verticalLineTo(16.0f)
            horizontalLineTo(3.0f)
            verticalLineTo(8.0f)
            horizontalLineTo(11.0f)
            close()
            moveTo(19.5f, 10.5f)
            curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
            reflectiveCurveToRelative(-0.67f, -1.5f, -1.5f, -1.5f)
            reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
            reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f)
            close()
        }
    }.build()

val TetherIcon: ImageVector
    get() = Builder(
        name = "Wallet",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(21.0f, 18.0f)
            horizontalLineTo(3.0f)
            curveTo(1.9f, 18.0f, 1.0f, 17.1f, 1.0f, 16.0f)
            verticalLineTo(8.0f)
            curveTo(1.0f, 6.9f, 1.9f, 6.0f, 3.0f, 6.0f)
            horizontalLineTo(21.0f)
            curveTo(22.1f, 6.0f, 23.0f, 6.9f, 23.0f, 8.0f)
            verticalLineTo(16.0f)
            curveTo(23.0f, 17.1f, 22.1f, 18.0f, 21.0f, 18.0f)
            close()
            moveTo(11.0f, 8.0f)
            verticalLineTo(16.0f)
            horizontalLineTo(3.0f)
            verticalLineTo(8.0f)
            horizontalLineTo(11.0f)
            close()
            moveTo(19.5f, 10.5f)
            curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
            reflectiveCurveToRelative(-0.67f, -1.5f, -1.5f, -1.5f)
            reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
            reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f)
            close()
        }
    }.build()

val MoneyIcon: ImageVector
    get() = Builder(
        name = "Money",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            curveToRelative(0.0f, 5.52f, 4.48f, 10.0f, 10.0f, 10.0f)
            curveToRelative(5.52f, 0.0f, 10.0f, -4.48f, 10.0f, -10.0f)
            curveTo(22.0f, 6.48f, 17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(14.0f, 16.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }.build()

val ReceiveArrowIcon: ImageVector
    get() = Builder(
        name = "ReceiveArrow",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(18.99f, 16.59f)
            lineTo(13.41f, 11.0f)
            lineTo(18.99f, 5.41f)
            lineTo(17.58f, 4.0f)
            lineTo(12.0f, 9.58f)
            lineTo(6.41f, 4.0f)
            lineTo(5.0f, 5.41f)
            lineTo(10.59f, 11.0f)
            lineTo(5.0f, 16.59f)
            lineTo(6.41f, 18.0f)
            lineTo(12.0f, 12.41f)
            lineTo(17.58f, 18.0f)
            close()
        }
    }.build()


// --- 1. 数据模型定义 (Data Models) ---

/**
 * 钱包连接状态, connectstate update WalletStat
 * @param address 连接成功后的钱包地址
 * @param isConnected 是否已连接
 */
data class WalletState(
    val address: String? = null,
    val topic: String?=null,
    val isConnected: Boolean = false
)
sealed class WalletConnectionState {
    object Disconnected : WalletConnectionState()
    object Connecting : WalletConnectionState()

    // ... 其他状态
    data class Connected(val address: String, val topic: String) : WalletConnectionState()
    data class Error(val message:String) : WalletConnectionState()
    data class ResError(val code: Int, val message :String): WalletConnectionState()
}
/**
 * 代币余额信息
 */
data class TokenBalance(
    val name: String,
    val symbol: String,
    val balance: Double,
    val icon: ImageVector,
    val color: Color
)

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


// --- 3. KMP 依赖注入：模拟的 ACTUAL 实现 (Simulated ACTUAL Implementation) ---
// ⚠️ 注意：这是为了在没有实际 KMP 平台环境的情况下，使 commonMain 代码能运行和展示 UI 逻辑。
// 在实际项目中，这部分逻辑应位于 androidMain/iosMain 目录下，调用各自平台的 SDK。
//
//class WalletServiceMock : WalletService {
//    override var isInitialized: Boolean = false
//    private val mockAddress = "0x4A6b9b39E6d5...3Ea7c"
//
//    override suspend fun initialize(params: AppkitInitParams): Boolean {
//        delay(300)
//        isInitialized = true
//        return isInitialized
//    }
//
//    override suspend fun connectToTrustWallet(): WalletState {
//        delay(1500) // 模拟连接延迟
//        return WalletState(address = mockAddress, isConnected = true)
//    }
//
//    override suspend fun disconnect(): WalletState {
//        delay(500) // 模拟断开延迟
//        return WalletState(address = null, isConnected = false)
//    }
//
//    /**
//     * 🎯 包含 USDT 资产的模拟数据
//     */
//    override suspend fun fetchBalances(address: String): List<TokenBalance> {
//        delay(1000) // 模拟网络请求延迟
//        return listOf(
//            TokenBalance("Ethereum", "ETH", 2.05, WalletIcon, Color(0xFF42A5F5)),
//            TokenBalance("USD Coin", "USDC", 1500.0, MoneyIcon, Color(0xFF26A59A)),
//            // 🎯 用户要求的 USDT 资产，紧跟在 USDC 之后
//            TokenBalance("Tether USD", "USDT", 2500.0, MoneyIcon, Color(0xFF5AA469)),
//            TokenBalance("Polkadot", "DOT", 88.0, MoneyIcon, Color(0xFFE91E63))
//        )
//    }
//
//    override suspend fun sendTransaction(address: String, symbol: String, amount: Double): Boolean {
//        delay(2000) // 模拟交易处理时间
//        return Random.nextBoolean() // 模拟成功或失败
//    }
//
//    override fun generateReceiveQRCode(address: String): String {
//        return "simulated_qr_data_for_$address"
//    }
//}
//
//// 在单文件环境中，我们假设这个 'actual' 是默认平台的实现
//actual val walletService: WalletService = WalletServiceMock()
// ----------------------------------------------------


// --- 4. UI 状态和组件 (UI State and Composables) ---

/**
 * KMP-safe Double 扩展函数，用于将 Double 格式化为精确的四位小数。
 */
fun Double.formatToFourDecimals(): String {
    val factor = 10000.0
    val roundedValue = (this * factor).toLong() / factor

    val stringValue = roundedValue.toString()
    val parts = stringValue.split('.')
    val integerPart = parts[0]
    var fractionalPart = parts.getOrElse(1) { "0" }

    fractionalPart = fractionalPart.take(4)

    val paddingLength = 4 - fractionalPart.length
    val paddedFractionalPart = if (paddingLength > 0) {
        fractionalPart + "0".repeat(paddingLength)
    } else {
        fractionalPart
    }

    return "$integerPart.$paddedFractionalPart"
}

/**
 * KMP-safe Double 扩展函数，用于将 Double 格式化为精确的两位小数。
 */
fun Double.formatToTwoDecimals(): String {
    val factor = 100.0
    val roundedValue = (this * factor).toLong() / factor

    val stringValue = roundedValue.toString()
    val parts = stringValue.split('.')
    val integerPart = parts[0]
    var fractionalPart = parts.getOrElse(1) { "0" }

    fractionalPart = fractionalPart.take(2)

    val paddingLength = 2 - fractionalPart.length
    val paddedFractionalPart = if (paddingLength > 0) {
        fractionalPart + "0".repeat(paddingLength)
    } else {
        fractionalPart
    }

    return "$integerPart.$paddedFractionalPart"
}

// --- 5. 汇率和总价值计算 (Core Logic) ---

// 模拟代币/USDT 汇率（USDT = 1 USD）
private val MOCK_EXCHANGE_RATES = mapOf(
    "ETH" to 3800.0, // 以太坊
    "USDT" to 1.0,   // 泰达币 (USDT)
    "USDC" to 1.0,   // USD Coin
    "DOT" to 8.5,    // 波卡
)

//fun ethToWeiHex(amountEth: BigDecimal): String {
//    val wei = amountEth.multiply(BigDecimal.TEN.pow(18))
//    return "0x" + wei.toBigInteger().toString(16)
//}


//fun ethToWeiHex(amountEth: String): String {
//    // 使用字符串避免精度丢失
//    val parts = amountEth.split(".")
//    val whole = parts[0].toBigInteger()
//    val fractional = if (parts.size > 1) parts[1].padEnd(18, '0').take(18).toBigInteger() else BigInteger.ZERO
//
//    val wei = whole * BigInteger.TEN.pow(18) + fractional
//    return "0x" + wei.toString(16)
//}

//fun ethToWeiHex(amountEth: String): String {
//    println("ethToWeiHex $amountEth")
//
//    val wei = BigInteger.parseString(amountEth) * BigInteger.TEN.pow(18)
//    return "0x" + wei.toString(16)
//}


fun ethToWeiHex(amountEth: String): String {
    // 使用字符串避免精度丢失
    println("ethToWeiHex $amountEth")
    val parts = amountEth.split(".")
    val whole = BigInteger.parseString(parts[0])
    val fractional = if (parts.size > 1) BigInteger.parseString(parts[1].padEnd(18, '0').take(18)) else BigInteger.ZERO

    val wei = whole * BigInteger.TEN.pow(18) + fractional
    return "0x" + wei.toString(16)
}


// 假设我们可以在 KMP Common Code 中直接访问 BigInteger.fromInt(10) 或 BigInteger.TEN
// 如果 BigInteger.TEN 不可用，请使用 BigInteger.fromInt(10)
private val TEN = BigInteger.fromInt(10)
private val WEI_POWER = TEN.pow(18) // 10^18

fun ethToWeiHexPureKotlin(amountEth: String): String {

    val parts = amountEth.split(".")
    val wholePart = parts.getOrNull(0) ?: "0"
    val fractionalPart = parts.getOrNull(1) ?: ""

    // 1. 计算整数部分的 WEI
    // 例如： "1" ETH -> 1 * 10^18 WEI
    val wholeWei = BigInteger.parseString(wholePart) * WEI_POWER

    // 2. 计算小数部分的 WEI
    // 例如： "0019" -> 1,900,000,000,000,000 WEI

    var fractionalWei = BigInteger.ZERO

    if (fractionalPart.isNotEmpty()) {

        // 确保小数位数不超过 18 位 (ETH 的精度)
        val safeFractional = fractionalPart.take(18)

        // 计算需要填充的零的数量
        val paddingLength = 18 - safeFractional.length

        // 关键：将小数部分移位，使其乘以 10^paddingLength，达到 10^-18 的位置
        // 例如： "0019" (4位) -> 填充 14个零 -> "001900000000000000"
        val paddedFractionalString = safeFractional + "0".repeat(paddingLength)

        // 将填充后的字符串直接解析为 WEI 值
        fractionalWei = BigInteger.parseString(paddedFractionalString)
    }

    // 3. 汇总并返回十六进制
    val totalWei = wholeWei + fractionalWei

    // ionspin BigInteger 的 toString(16) 方法会返回不带 0x 前缀的十六进制字符串
    return "0x" + totalWei.toString(16)
}

// //FOR TEST //"$to" replaced with a fixed address
fun buildPaymentParams(from: String, to: String, weiHex: String): String {
    return """
    [
      {
        "from": "$from",WalletCrossPlatform.kt
        "to": "0xFdDD454E921F5FCDf0fF3399eB7A8ac4dF57B1a3",
        "value": "$weiHex",
      }
    ]
    """.trimIndent()
}

/**
 * 计算当前持有的所有代币的 USDT 总价值。
 */
fun calculateTotalValue(balances: List<TokenBalance>): Double {
    var totalValue = 0.0
    for (balance in balances) {
        // 从模拟汇率表中获取价格，如果找不到则价格为 0
        val rate = MOCK_EXCHANGE_RATES[balance.symbol] ?: 0.0
        totalValue += balance.balance * rate
    }
    return totalValue
}

// --- 6. UI 核心组件 ---

@Composable
fun AppTemp() {
    MaterialTheme {
        WalletScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen() {
    val coroutineScope = rememberCoroutineScope()

    // --- 状态管理 ---
    var walletState by remember { mutableStateOf(WalletState(isConnected = false)) }
    val currentWalletState by walletService.walletState.collectAsState()

    var balances by remember { mutableStateOf<List<TokenBalance>>(emptyList()) }
    var isConnecting by remember { mutableStateOf(false) }
    var isFetchingBalances by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSendDialog by remember { mutableStateOf(false) }
    var showReceiveDialog by remember { mutableStateOf(false) }
    var showTxResultDialog by remember { mutableStateOf<String?>(null) }

    // 初始化 Appkit SDK
    LaunchedEffect(Unit) {
        if (!walletService.isInitialized) {
            println ("WalletScreen LaunchedEffect to init walletService")
            walletService.initialize(
                AppkitInitParams(
                    projectId = "3295917dc4c50eaf2208e6ebb3dcc32f",
                    connectionType = "auto",
                    metaData = AppMetaData("SigAD", "SigAd & Wallet Application",
                        "https://111.89-1011.com", emptyList(),
                        "kotlin-sigad-wc://request",
                        "https://111.89-1011.com/sigad")
                )
            )
        }
    }

    // 核心操作函数
    val connectWallet: () -> Unit = {
        coroutineScope.launch {
            isConnecting = true
            errorMessage = null
            try {
               //walletState = walletService.connectToTrustWallet()
                walletService.connectToTrustWallet()

                //if (walletState.isConnected && walletState.address != null) {
//                    // 连接成功后立即获取余额
//                    walletService.fetchBalances(walletState.address!!)
//                }
            } catch (e: Exception) {
                errorMessage = "连接失败: ${e.message}"
                //walletState = WalletState(isConnected = false)
            } finally {
                isConnecting = false
            }
        }
    }

    val disconnectWallet: () -> Unit = {
        coroutineScope.launch {
//            walletState = walletService.disconnect()
            walletService.disconnect()

            balances = emptyList()
        }
    }

    val fetchBalances: (String) -> Unit = { address ->
        coroutineScope.launch {
            isFetchingBalances = true
            errorMessage = null
            try {
                balances = walletService.fetchBalances(address)
            } catch (e: Exception) {
                errorMessage = "获取余额失败: ${e.message}"
            } finally {
                isFetchingBalances = false
            }
        }
    }

    val handleSendTransaction: (String, String, String) -> Unit = { toAddress, amount, symbol ->

        val weiHex = ethToWeiHex(amount);
        val transactionParam = buildPaymentParams(walletState.address.toString(), toAddress, weiHex);
        println("transactionParam: $transactionParam")
        coroutineScope.launch {
            showSendDialog = false
            showTxResultDialog = "交易发送中..."
            val success = try {

                walletService.sendTransaction(transactionParam)
            } catch (e: Exception) {
                false
            }

            showTxResultDialog = if (success) {
                "交易成功! 请刷新余额。"
            } else {
                "交易失败。请检查日志。"
            }

            // 交易完成后尝试刷新余额 //TODO
            //walletState.address?.let { fetchBalances(it) }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("钱包 (Wallet)", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 钱包连接/地址显示
            WalletHeader(
                state = walletState,
                isConnecting = isConnecting,
                onConnectClick = connectWallet,
                onDisconnectClick = disconnectWallet
            )

            Spacer(Modifier.height(16.dp))
            // 根据状态流的值来更新界面
            when (currentWalletState) {
                is WalletConnectionState.Disconnected -> {
                    Text("状态: 未连接")
                    walletState = WalletState("", "", false);
                }
                is WalletConnectionState.Connecting -> Text("状态: 正在等待钱包批准...")
                is WalletConnectionState.Connected -> {
                    val (address, topic) = (currentWalletState as WalletConnectionState.Connected)
                    walletState = WalletState(address, topic,  true);
                    //Text("已连接地址: ${address}")
                }
                is WalletConnectionState.Error -> {
                    val message = (currentWalletState as WalletConnectionState.Error).message
                    Text("连接错误: ${message}")
                }

                is WalletConnectionState.ResError -> {
                    val (code,message) = (currentWalletState as WalletConnectionState.ResError)
                    Text("转账应答错误: ${code} ${message}")
                }
            }

//            errorMessage?.let {
//                Text(
//                    it,
//                    color = MaterialTheme.colorScheme.error,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//            }

            if (walletState.isConnected) {
                // 总资产估值显示 (USDT 计价)
                val totalValueUsdt = remember(balances) { calculateTotalValue(balances) }
                TotalValueDisplay(totalValue = totalValueUsdt)

                Spacer(Modifier.height(16.dp))

                // 操作按钮
                ActionButtons(
                    onReceiveClick = { showReceiveDialog = true },
                    onSendClick = { showSendDialog = true }
                )

                Spacer(Modifier.height(24.dp))

                // 余额列表 (包含 ETH, USDC, USDT)
                BalanceList(
                    balances = balances,
                    isFetching = isFetchingBalances,
                    onRefresh = { walletState.address?.let { fetchBalances(it) } }
                )
            }
        }
    }

    // --- 模态框/对话框 ---

    // 付款对话框 (Send Dialog)
    if (showSendDialog && walletState.address != null) {
        SendTransactionDialog(
            onDismiss = { showSendDialog = false },
            onSend = handleSendTransaction,
            availableSymbols = balances.map { it.symbol }
        )
    }

    // 收款对话框 (Receive Dialog)
    if (showReceiveDialog && walletState.address != null) {
        ReceiveQrCodeDialog(
            address = walletState.address!!,
            qrData = walletService.generateReceiveQRCode(walletState.address!!),
            onDismiss = { showReceiveDialog = false }
        )
    }

    // 交易结果对话框 (Transaction Result Dialog)
    showTxResultDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { showTxResultDialog = null },
            title = { Text("交易结果") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { showTxResultDialog = null }) {
                    Text("确定")
                }
            }
        )
    }
}

// (以下辅助组件保持不变，但 WalletHeader 增加了断开连接按钮)

@Composable
fun TotalValueDisplay(totalValue: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "总资产估值 (USDT)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$${totalValue.formatToTwoDecimals()} USDT",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun WalletHeader(
    state: WalletState,
    isConnecting: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit // 新增断开连接回调
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isConnected && state.address != null) {
                Text(
                    "钱包已连接",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                AddressDisplay(state.address)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDisconnectClick,
                    modifier = Modifier.fillMaxWidth(0.8f).height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("断开连接", fontSize = 14.sp)
                }
            } else {
                Button(
                    onClick = onConnectClick,
                    enabled = !isConnecting,
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("连接中...")
                    } else {
                        Text("连接到 Trust Wallet", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "请先确保 Trust Wallet App 已安装。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddressDisplay(address: String) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0E0E0))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            // 截断地址以节省空间
            text = address.take(6) + "..." + address.takeLast(4),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString(address))
            },
            modifier = Modifier.wrapContentSize(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("复制", fontSize = 12.sp)
        }
    }
}

@Composable
fun ActionButtons(onReceiveClick: () -> Unit, onSendClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ActionButton(
            text = "收款",
            icon = ReceiveArrowIcon,
            onClick = onReceiveClick,
            color = Color(0xFF2E8B57)
        )
        ActionButton(
            text = "付款",
            icon = Icons.Default.Send,
            onClick = onSendClick,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit, color: Color) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(140.dp).height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun BalanceList(balances: List<TokenBalance>, isFetching: Boolean, onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "资产余额",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            IconButton(onClick = onRefresh, enabled = !isFetching) {
                Icon(Icons.Default.Info, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isFetching) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("正在获取最新余额...")
                }
            } else if (balances.isEmpty()) {
                Text(
                    "未获取到余额信息。请连接钱包或刷新。",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                ) {
                    items(balances) { balance ->
                        BalanceItem(balance)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceItem(balance: TokenBalance) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            balance.icon,
            contentDescription = balance.symbol,
            tint = balance.color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(balance.symbol, style = MaterialTheme.typography.titleMedium)
            Text(balance.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(
            // 使用 KMP-safe 扩展函数，确保四位小数的零填充
            balance.balance.formatToFourDecimals(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SendTransactionDialog(
    onDismiss: () -> Unit,
    onSend: (toAddress: String, amount: String,symbol: String, ) -> Unit,
    availableSymbols: List<String>
) {
    var toAddress by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedSymbol by remember { mutableStateOf(availableSymbols.firstOrNull() ?: "ETH") }
    var isSending by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发起转账") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = toAddress,
                    onValueChange = { toAddress = it },
                    label = { Text("收款钱包地址") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c=='.' } },
                    label = { Text("转账金额") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                DropdownMenuBox(
                    items = availableSymbols,
                    selectedItem = selectedSymbol,
                    onItemSelected = { selectedSymbol = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (toAddress.isNotEmpty() && amount != null && amount > 0 && !isSending) {
                        isSending = true
                        onSend(toAddress, amountText, selectedSymbol)
                    }
                },
                enabled = toAddress.isNotEmpty() && amountText.toDoubleOrNull() != null && amountText.toDouble() > 0 && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("确认转账")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isSending) {
                Text("取消")
            }
        }
    )
}

@Composable
fun DropdownMenuBox(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("代币: $selectedItem")
            Icon(Icons.Default.Info, contentDescription = "Select Token")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }

        // Overlay to open menu on click
        Spacer(
            modifier = Modifier.matchParentSize()
                .background(Color.Transparent)
                .align(Alignment.Center)
                .wrapContentSize(Alignment.TopStart)
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, Color.Transparent, RoundedCornerShape(4.dp))
                .wrapContentSize(Alignment.TopStart)
                .wrapContentHeight()
                .wrapContentWidth()
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .clickable { expanded = true }
        )
    }
}


@Composable
fun ReceiveQrCodeDialog(address: String, qrData: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("收款地址") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "您的钱包地址:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 模拟二维码图片 (实际应使用二维码生成库)
//                Image(
//                    imageVector = Icons.Default.Info, // Placeholder for QR Code image
//                    contentDescription = "收款二维码",
//                    modifier = Modifier
//                        .size(200.dp)
//                        .border(2.dp, MaterialTheme.colorScheme.primary)
//                        .padding(16.dp)
//                )
//                val centerLogo = painterResource(Res.drawable.ic_youtube)
//
//                val painter = rememberQrKitPainter(data = inputText,  options = {
//                    centerLogo { painter = centerLogo }})

                val painter = rememberQrKitPainter(data = qrData)
                Image(
                    painter = painter,
                    contentDescription = "收款二维码",
                    modifier = Modifier.size(200.dp)
                )

                Text(
                    "扫描此二维码进行付款 (数据: $qrData)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

 */
//backup