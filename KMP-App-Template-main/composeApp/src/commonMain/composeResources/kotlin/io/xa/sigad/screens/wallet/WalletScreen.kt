package io.xa.sigad.wallet

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
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.launch

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
import io.xa.sigad.AppSchemaAndroid
import io.xa.sigad.WalletAPPName
import io.xa.sigad.data.AdsApi
import io.xa.sigad.data.model.CurrentNetworkState
import io.xa.sigad.data.model.TokenBalance
import io.xa.sigad.data.model.currencyIconDecimalMaps
import io.xa.sigad.data.model.getBlockchainChineseName
import io.xa.sigad.data.model.getBlockchainEnglishName
import io.xa.sigad.data.model.mapTokensToTokenBalances
import io.xa.sigad.data.model.tokenaddressMap
import io.xa.sigad.reownProjectId
import io.xa.sigad.screens.wallet.ReceiveArrowIcon
import io.xa.sigad.trustedDomain
import io.xa.sigad.universalAppLink


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


fun ethToWeiHex(amountEth: String): String {
    // 使用字符串避免精度丢失
    println("ethToWeiHex $amountEth")
    val sanitizedAmount = when {
        amountEth.startsWith(".") -> "0$amountEth"
        amountEth.endsWith(".") -> "${amountEth}0"
        else -> amountEth
    }

    val parts = sanitizedAmount.split(".")
    val whole = BigInteger.parseString(parts[0])
    val fractional = if (parts.size > 1) BigInteger.parseString(
        parts[1].padEnd(18, '0').take(18)
    ) else BigInteger.ZERO

    val wei = whole * BigInteger.TEN.pow(18) + fractional
    return "0x" + wei.toString(16)
}

fun tokenToWeiHex(amountEth: String, symbol: String ): String {
    // 使用字符串避免精度丢失
    println("tokenToWeiHex $amountEth")
    //symbol must be in maps, and decimal must be existed
    val decimal = currencyIconDecimalMaps[symbol]?.getValue("decimal") as Int
    val sanitizedAmount = when {
        amountEth.startsWith(".") -> "0$amountEth"
        amountEth.endsWith(".") -> "${amountEth}0"
        else -> amountEth
    }

    val parts = sanitizedAmount.split(".")
    val whole = BigInteger.parseString(parts[0])
    val fractional = if (parts.size > 1) BigInteger.parseString(
        parts[1].padEnd(decimal, '0').take(decimal)
    ) else BigInteger.ZERO

    val wei = whole * BigInteger.TEN.pow(decimal) + fractional
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

/**
 * 构造ERC20 transfer函数的ABI编码数据
 * 函数签名: transfer(address _to, uint256 _value)
 * @param recipientAddress 接收方地址
 * @param amount 代币数量（已转换为最小单位）
 * @return ABI编码的交易数据
 */
private fun constructTransferData(recipientAddress: String, amountWeiHex: String): String {
    // transfer函数的选择器: keccak256("transfer(address,uint256)")的前4字节
    val functionSelector = "a9059cbb"

    // 清理地址格式
    val cleanRecipientAddress = if (recipientAddress.startsWith("0x")) {
        recipientAddress.substring(2)
    } else {
        recipientAddress
    }
    val paddedRecipientAddress = cleanRecipientAddress.padStart(64, '0')

    // 格式化amount为64字符长的十六进制字符串
    val cleanAmountWeiHex = if (amountWeiHex.startsWith("0x")) {
        amountWeiHex.substring(2)
    } else {
        amountWeiHex
    }

    val paddedAmount = cleanAmountWeiHex.padStart(64, '0')

    // 组合数据: 函数选择器 + 接收方地址(64字节) + 金额(64字节)
    return "0x$functionSelector$paddedRecipientAddress$paddedAmount"
}


fun buildPaymentParams(from: String, to: String, weiHex: String): String {
    return """
    [
      {
        "from": "$from",
        "to": "$to",
        "value": "$weiHex"
      }
    ]
    """.trimIndent()
}


//token transaction param is [
//{
//    "from": "0xd8131ed60c407819254163f5ca50c068ee1c5d1d",
//    "to": "0x036cbd53842c5426634e7929541ec2318f3dcf7e",
//    "value": "0x0"
//    "data": "0xa9059cbb000000000000000000000000036cbd53842c5426634e7929541ec2318f3dcf7e00000000000000000000000000000000000000000000000000082bd67afbc000"
//}
//] from,
// //FOR TEST //"$to" replaced with a fixed address
fun buildTokenPaymentParams(from: String, to: String, tokenAddress: String, weiHex: String): String {
    val data = constructTransferData(to, weiHex)
    //// 代币转账时value为0, data is encoded by to(token address) and weiHex
    return """
    [
      {
        "from": "$from",
        "to": "$tokenAddress",
        "value": "0x0",
        "data": "$data"
      }
    ]
    """.trimIndent()
}

/**
 * 计算当前持有的所有代币的 USDT 总价值。
 * toDouble should be wrong....
 *
 */
fun calculateTotalValue(balances: List<TokenBalance>): Double {
    var totalValue = 0.0
    for (balance in balances) {
        // 从模拟汇率表中获取价格，如果找不到则价格为 0
        val rate = MOCK_EXCHANGE_RATES[balance.symbol] ?: 0.0
        totalValue += balance.balance.toDouble() * rate
    }
    return totalValue
}


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
fun WalletPage() {
    val coroutineScope = rememberCoroutineScope()

    // --- 状态管理 ---
    // walletState 现在将通过 LaunchedEffect 派生
    var walletState by remember { mutableStateOf(WalletState(isConnected = false)) }

    // 监听核心状态
    val currentWalletState: WalletConnectionState by walletService.walletState.collectAsState()
    val currentChainId by CurrentNetworkState.currentChainId.collectAsState() // 🌟 Chain ID 状态

    // 其他 UI 状态
    var balances by remember { mutableStateOf<List<TokenBalance>>(emptyList()) }
    var isConnecting by remember { mutableStateOf(false) }
    var isFetchingBalances by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSendDialog by remember { mutableStateOf(false) }
    var showReceiveDialog by remember { mutableStateOf(false) }
    var showTxResultDialog by remember { mutableStateOf<String?>(null) }

//    val fetchBalances: (String) -> Unit = { address ->

//        coroutineScope.launch {
//            // ... 保持不变，但请注意 fetchUserAssets 应该使用 currentChainId 对应的网络名称
//            // val tokensRespond= adsApi.fetchUserAssets(address , listOf("base-sepolia"))
//            // 👆 此处硬编码的 "base-sepolia" 需要改为根据 currentChainId 动态获取
//
//            // 示例：
//            val networkName = getNetworkNameFromChainId(currentChainId) // 假设您有这个函数
//            val tokensRespond = adsApi.fetchUserAssets(address, listOf(networkName))
//            // ... 保持不变 ...
//        }
//    }

    // WalletPage 内部（Lambda 定义）

    val fetchBalances: (String) -> Unit = { address ->
        coroutineScope.launch {
            isFetchingBalances = true
            errorMessage = null
            try {
                //balances = walletService.fetchBalances(address)

                val networkName = getBlockchainEnglishName(reference = currentChainId)
                if (networkName == null) {
                    errorMessage = "没有获取到 ADS3网络(链）名"
                    return@launch
                }

                val adsApi = AdsApi()
                val tokensRespond = adsApi.fetchUserAssets(
                    address,
                    listOf(networkName)
                ) //"eth-sepolia") // 'eth-mainnet'
                println("  =========> get ${address} asserts at ${networkName}, all size is ${tokensRespond.tokens.size}===")

                tokensRespond.tokens.forEach { token ->
                    println("token address " + token.tokenAddress)
                    println(" raw amount " + token.tokenBalance)
                    println(" amount " + token.textAmount)
                    println(" price " + token.latestPriceString)
                    println(" decimal " + token.tokenMetadata?.decimals)
                }
                val tokens2 = tokensRespond.getEthAndUSDTokens()

                println("  ===========>in all tokens, USD TOKEN size ${tokens2.size}===> ")
                tokens2.forEach { token ->
                    println("token address " + token.tokenAddress)
                    println(" raw amount " + token.tokenBalance)
                    println(" amount " + token.textAmount)
                    println(" price " + token.latestPriceString)
                }
//                println("......Enter maptokens.....")
                balances = mapTokensToTokenBalances(tokens2)

            } catch (e: Exception) {
                errorMessage = "获取余额失败: ${e.message}"
            } finally {
                isFetchingBalances = false
            }
        }
    }


    LaunchedEffect(Unit) {
        if (!walletService.isInitialized) {
            println("WalletScreen LaunchedEffect to init walletService")
            walletService.initialize(
                AppkitInitParams(
                    projectId = reownProjectId,
                    connectionType = "auto",
                    metaData = AppMetaData(
                        "SigAD",
                        "SigAd & Wallet Application",
                        "https://${trustedDomain}",
                        emptyList(),
                        //"kotlin-sigad-wc://request",
                        "${AppSchemaAndroid}://request",
                        universalAppLink //"https://111.89-1011.com/sigad"
                    )
                )
            )
        }
    }

    // ----------------------------------------------------
    // 🚀 LaunchedEffect 1: 状态同步 (根据 WalletState 和 Chain ID 更新 UI 状态)
    // 依赖项：currentWalletState 或 currentChainId 变化时，重新运行
    // ----------------------------------------------------
    LaunchedEffect(currentWalletState, currentChainId) {
        when (currentWalletState) {
            is WalletConnectionState.Connected -> {
                val (topic, accounts) = currentWalletState as WalletConnectionState.Connected

                // 1. 查找与当前选中 Chain ID 匹配的账户
                val account = accounts.firstOrNull { it.reference == currentChainId }

                if (account != null) {
                    val chineseName = getBlockchainChineseName(reference = account.reference)
                    // ✅ 安全地更新 walletState
                    walletState = WalletState(account.address, topic, chineseName, true)
                    // 🌟 Chain ID 变化时，walletState.address 会相应更新，这会触发 LaunchedEffect 2
                } else {
                    // 无法找到当前链对应的账户，保持连接Topic，但将地址和中文名置空
                    val chineseName = getBlockchainChineseName(reference = currentChainId)
                    walletState = WalletState(null, topic, chineseName, false)
                }
            }

            is WalletConnectionState.Disconnected -> {
                // ✅ 安全地更新 walletState
                walletState = WalletState(isConnected = false)
            }

            else -> {
                // 保持 walletState 状态不变或按需处理 Connecting/Error
            }
        }
    }

    // ----------------------------------------------------
    // 🚀 LaunchedEffect 2: 数据加载 (监听 walletState.address)
    // 依赖项：只有当 walletState.address 变化时，才触发余额获取
    // ----------------------------------------------------
    LaunchedEffect(walletState.address) {
        val address = walletState.address
        if (address != null && walletState.isConnected) {
            // 确保只在地址有效且连接状态下获取余额
            fetchBalances(address)
        } else {
            balances = emptyList() // 清空余额
        }
    }


    // --- 原有的核心操作函数 (保持不变，或按需调整) ---
    // ... connectWallet, disconnectWallet, fetchBalances, handleSendTransaction 保持不变 ...

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
            println(" wallet disconnectWallet...")
            walletService.disconnect()

            balances = emptyList()
        }
    }


    val handleSendTransaction: (String, String, String) -> Unit = { toAddress, amount, symbol ->

        if (symbol != "ETH") { //token
            val weiHex = tokenToWeiHex(amount, symbol);

            val networkName = getBlockchainEnglishName(reference = currentChainId) // 假设您有这个函数
            if (networkName == null) {
                showTxResultDialog = " 不能获取链ID${currentChainId}的名称"
            } else {
                val tokenAddress = tokenaddressMap[networkName]?.get(symbol)
                if (tokenAddress == null) {
                    showTxResultDialog = " 不能获取${symbol}的代币地址"
                } else {
                    val transactionParam =
                        buildTokenPaymentParams(
                            from = walletState.address.toString(),
                            to = toAddress,
                            tokenAddress = tokenAddress,
                            weiHex = weiHex
                        )
                    println(" token transaction param is ${transactionParam}")
                    coroutineScope.launch {
                        showSendDialog = false
                        showTxResultDialog = "交易发送中..."
                        val success = try {

                            walletService.sendTransaction(transactionParam)
                        } catch (e: Exception) {
                            false
                        }
                    }
                    showTxResultDialog = "交易请求发送，请等待"
                }
            }
        } else { //eth
            val weiHex = ethToWeiHex(amount);

            val transactionParam =
                buildPaymentParams(walletState.address.toString(), toAddress, weiHex);
            println("transactionParam: $transactionParam")
            coroutineScope.launch {
                showSendDialog = false
                showTxResultDialog = "交易发送中..."
                val success = try {

                    walletService.sendTransaction(transactionParam)
                } catch (e: Exception) {
                    false
                }
                showTxResultDialog = "交易请求发送，请等待"
            }
        }
    }
    // --- UI 渲染部分 ---

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
               // .padding(paddingValues),
            //.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 钱包连接/地址显示
            WalletHeader(
                state = walletState, // 🌟 walletState 现在是由 LaunchedEffect 安全更新的
                isConnecting = isConnecting,
                onConnectClick = connectWallet,
                onDisconnectClick = disconnectWallet
            )

            Spacer(Modifier.height(16.dp))

            // 🌟 修正：这里不再进行状态赋值，只做 UI 提示
            when (currentWalletState) {
                is WalletConnectionState.Disconnected -> {
                    Text("状态: 未连接")
                }

                is WalletConnectionState.Connecting -> Text("状态: 正在等待钱包批准...")
                is WalletConnectionState.Connected -> {
                    // 只有在 Connected 且没有找到匹配账户时才显示警告
                    if (walletState.address == null) {
                        val chineseName = getBlockchainChineseName(reference = currentChainId)
                        Text(
                            "警告: 钱包APP连接不包含您当前选择的链 ${chineseName} 的授权账户。请在钱包中授权该链。",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is WalletConnectionState.Error -> {
                    val message = (currentWalletState as WalletConnectionState.Error).message
                    Text("连接错误: ${message}", color = MaterialTheme.colorScheme.error)
                }

                is WalletConnectionState.ResError -> {
                    println(" ....WalletConnectionState.ResError ")
                    val (code, message) = (currentWalletState as WalletConnectionState.ResError)
                    println(" transaction res: $code $message")
                    Text(
                        "转账应答: ${code}, ${message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (walletState.isConnected && walletState.address != null) {
                // ... 操作按钮和余额列表 (保持不变) ...
                ActionButtons(
                    onReceiveClick = { showReceiveDialog = true },
                    onSendClick = { showSendDialog = true }
                )

                Spacer(Modifier.height(24.dp))

                BalanceList(
                    balances = balances,
                    isFetching = isFetchingBalances,
                    // onRefresh 现在应该触发 fetchBalances，它会自动使用当前最新的 Chain ID
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
            network = walletState.chainName!!,
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
                    "钱包已连接${state.chainName}",
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
                        Text("连接到 ${WalletAPPName}", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "请先确保 ${WalletAPPName} 应用已安装。",
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
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
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
            balance.balance,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SendTransactionDialog(
    onDismiss: () -> Unit,
    onSend: (toAddress: String, amount: String, symbol: String) -> Unit,
    availableSymbols: List<String>
) {
    //accaout-1 address in Metamask
    var toAddress by remember { mutableStateOf("0x1c9352b08cd3ff5522b42a3359a3769473101c71") }
    var amountText by remember { mutableStateOf("0.0023") }
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
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
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
fun ReceiveQrCodeDialog(network: String, address: String, qrData: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${network}收款地址") },
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