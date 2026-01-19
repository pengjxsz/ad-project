package io.xa.sigad.wallet // 确保包名与 commonMain 一致

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.Modal.Model.JsonRpcResponse
import com.reown.appkit.client.models.request.SentRequestResult
import com.reown.appkit.presets.AppKitChainsPresets
import com.reown.sign.client.Sign
//mport com.reown.sign.client.Sign.Model
import com.reown.sign.client.SignClient
import io.ktor.client.request.request
import io.xa.sigad.baseWalletDeepLinkURL
// 导入 Kotlin 协程相关的库，例如 kotlinx.coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//import com.walletconnect.sign.client.SignClient
import io.xa.sigad.wallet.WalletService // 导入 commonMain 中的接口
import io.xa.sigad.wallet.AppkitInitParams
import io.xa.sigad.wallet.WalletState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.mp.KoinPlatform.getKoin
import io.xa.sigad.screens.wallet.WalletIcon
import io.xa.sigad.screens.wallet.MoneyIcon
import io.xa.sigad.data.model.TokenBalance
import io.xa.sigad.message.WebAccount
import io.xa.sigad.screens.wallet.UsdtIcon

// ----------------------------------------------------
// 🎯 KMP 依赖注入：声明实际依赖 (actual)
// 1. 定义一个可变的 WalletService 实例，用于存储 Android 平台的服务实现。
//20251204 use KOIN import instead
///////////////////////////////////////////////////////////////////
// private lateinit var androidWalletServiceInstance: WalletService

// 2. 实现 actual val walletService，它将返回上述实例。
//actual val walletService: WalletService
//    get() = if (::androidWalletServiceInstance.isInitialized) {
//        androidWalletServiceInstance
//    } else {
//        // 实际应用中不应发生这种情况，但在 Compose Preview 或测试中可能需要一个默认值
//        // 推荐的做法是在应用启动时保证初始化。
//        // 为了避免运行时崩溃，我们抛出错误，或在非生产环境返回一个 Mock。
//        // 这里选择抛出错误，强制在启动时初始化。
//        throw IllegalStateException("WalletService must be initialized via initAndroidWalletService() before use.")
//    }
// actual val walletService 现在直接从 Koin 容器中获取实例
actual val walletService: WalletService
    get() = getKoin().get<WalletService>() // Koin 会在第一次调用时提供已创建的单例


// ----------------------------------------------------

/**
 * Android 平台的 WalletService 实际实现。
 * 它需要一个 Android Context 来启动 Trust Wallet 的 Intent。
 */
class AndroidAppkitWalletService(
    private val context: Context // 接收 Activity Context
) : WalletService {

    // 内部实现，与 Common 中的接口匹配
    private val _walletState =
        MutableStateFlow<WalletConnectionState>(WalletConnectionState.Disconnected)
    override val walletState: StateFlow<WalletConnectionState> = _walletState.asStateFlow()

    override var isInitialized: Boolean = false
    private var initParams: AppkitInitParams? = null

    // 实现了 commonMain 中的所有方法... (实现与之前相同，仅省略以保持简洁)

    override suspend fun initialize(params: AppkitInitParams): Boolean {

        return withContext(Dispatchers.IO) {
            initParams = params

            // 实际操作:
            // 1. 调用 Reown Appkit Core SDK 的初始化方法。
            // ReownCoreClient.initialize(context, params.projectId, params.metaData, params.connectionType)
            val connectionType =
                if (params.connectionType == "auto") ConnectionType.AUTOMATIC else ConnectionType.MANUAL
            val projectId =
                params.projectId; //"3295917dc4c50eaf2208e6ebb3dcc32f" // Get Project ID at https://dashboard.reown.com/
            val appMetaData = Core.Model.AppMetaData(
                name = params.metaData.name, //"Kotlin.AppKit",
                description = params.metaData.description, //"Kotlin AppKit Implementation",
                url = params.metaData.url, //"kotlin.reown.com",
                icons = params.metaData.icons, //listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
                redirect = params.metaData.redirect, //"kotlin-modal-wc://request"
                        appLink = params.metaData.appLink //"https://111.89-1011.com/sigad"
            )
            println("CoreClient.initialize")
            var bError = false
            //  修改点：从 context 获取 Application 实例
            // context 是 Activity，applicationContext 通常就是 Application 实例
            val application = context.applicationContext as Application
            CoreClient.initialize(
                projectId = projectId,
                connectionType = connectionType,
                application = application,
                metaData = appMetaData,
                onError = { error ->
                    // Error will be thrown if there's an issue during initialization
                    println("CoreClient.initialize failed");
                    println(error.toString())
                    bError = true;
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "钱包服务初始化失败: ${error.throwable.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            println("AppKit.initialize")
            if (bError)
                return@withContext false;
            AppKit.initialize(
                init = Modal.Params.Init(
                    CoreClient,
                    includeWalletIds = listOf("trustwallet")
                ),
                onSuccess = {
                    // Callback will be called if initialization is successful
                    isInitialized = true
                },
                onError = { error ->
                    bError = true;
                    // Error will be thrown if there's an issue during initialization
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "钱包服务初始化失败: ${error.throwable.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )

            println("Android: Initializing Reown Appkit SDK for project ${params.projectId}")
            true
        }
        if (isInitialized) {
            AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())
//            Chain(chainName=Ethereum, chainNamespace=eip155, chainReference=1, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=Ether, symbol=ETH, decimal=18), chainImage=null,
//                rpcUrl=https://cloudflare-eth.com, blockExplorerUrl=https://etherscan.io),
//             Chain(chainName=Arbitrum One, chainNamespace=eip155, chainReference=42161, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=Ether, symbol=ETH, decimal=18),
//                chainImage=null, rpcUrl=https://arb1.arbitrum.io/rpc, blockExplorerUrl=https://arbiscan.io),
//             Chain(chainName=Polygon, chainNamespace=eip155, chainReference=137, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=MATIC, symbol=MATIC, decimal=18),
//                chainImage=null, rpcUrl=https://polygon-rpc.com, blockExplorerUrl=https://polygonscan.com),
//             Chain(chainName=Avalanche, chainNamespace=eip155, chainReference=43114, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=Avalanche, symbol=AVAX, decimal=18),
//                chainImage=null, rpcUrl=https://api.avax.network/ext/bc/C/rpc, blockExplorerUrl=https://snowtrace.io),
//             Chain(chainName=BNB Smart Chain, chainNamespace=eip155, chainReference=56, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=BNB, symbol=BNB, decimal=18),
//                chainImage=null, rpcUrl=https://rpc.ankr.com/bsc, blockExplorerUrl=https://bscscan.com),
//             Chain(chainName=OP Mainnet, chainNamespace=eip155, chainReference=10, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=Ether, symbol=ETH, decimal=18),
//                chainImage=null, rpcUrl=https://mainnet.optimism.io, blockExplorerUrl=https://explorer.optimism.io),
//             Chain(chainName=Gnosis, chainNamespace=eip155, chainReference=100, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=Gnosis, symbol=xDAI, decimal=18),
//                chainImage=null, rpcUrl=https://rpc.gnosischain.com, blockExplorerUrl=https://blockscout.com/xdai/mainnet),
//             Chain(chainName=zkSync Era, chainNamespace=eip155, chainReference=324, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=Ether, symbol=ETH, decimal=18),
//                chainImage=null, rpcUrl=https://mainnet.era.zksync.io, blockExplorerUrl=https://explorer.zksync.io),
//             Chain(chainName=Zora, chainNamespace=eip155, chainReference=7777777, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=Ether, symbol=ETH, decimal=18),
//                chainImage=null, rpcUrl=https://rpc.zora.energy, blockExplorerUrl=https://explorer.zora.energy),
//             Chain(chainName=Base, chainNamespace=eip155, chainReference=8453, requiredMethods=[personal_sign, eth_signTypedData, eth_sendTransaction],
//                optionalMethods=[wallet_switchEthereumChain, wallet_addEthereumChain], events=[chainChanged, accountsChanged], token=Token(name=Ether, symbol=ETH, decimal=18),
//                chainImage=null, rpcUrl=https://mainnet.base.org, blockExplorerUrl=https://basescan.org), Chain(chainName=Celo, chainNamespace=eip155, chainR
//            println(AppKitChainsPresets.ethChains.values.toList())
            setupAppKitDelegate();
            return true
        } else return false
    }


    // ✅ 核心方法：设置监听器
    private fun setupAppKitDelegate() {

        val appKitModalDelegate = object : AppKit.ModalDelegate {

            // 🎯 连接成功：这是你获取账号和地址的地方
            override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
                // approvedSession 对象包含了连接的所有信息

                // 1. 获取账户列表 (通常是 eip155:1:0xAddress 格式)
                // eip155 代表以太坊兼容链
                approvedSession as Modal.Model.ApprovedSession.WalletConnectSession
                val firstAccount = approvedSession.namespaces["eip155"]?.accounts?.firstOrNull()

                // dApp 发起 session_proposal 请求
//                        钱包同意后，SDK 生成一个新的 session topic（不同于 pairing topic）
//                这个 session topic 成为此后 所有区块链请求（签名、发送交易等）的通信通道 ID
                val topic = approvedSession.topic;

                //20251216 get the acctout list instead of getting the first accout
                //approvedSession.accounts.map {  }

                // 2. 解析出纯地址 (去掉 "eip155:1:" 前缀)
                //// 连接到 Sepolia 测试网
                //val chains = listOf("eip155:11155111")
                //eip155:11155111:0xAddress...
                //eip155:Chain ID:地址
                val address = firstAccount?.split(":")?.lastOrNull()

                if (address != null) {
                    Log.d("AppKit", "连接成功，钱包地址: $address,主题: $topic ")
                    val anAccounts = listOf<WalletAccount>(WalletAccount(namespace = "",  reference = "", address=address))
                    _walletState.value = WalletConnectionState.Connected(topic, anAccounts)
                    // 3. 更新状态 (UI 会自动收到通知)
                    //_currentAddress.value = address
                }else{
                    _walletState.value = WalletConnectionState.Error("钱包APP没有返回效连接");
                }
            }

            // ❌ 用户拒绝或连接失败
            override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
                Log.e("AppKit", "用户拒绝了连接")
                _walletState.value = WalletConnectionState.Error("用户拒绝了连接");
            }

            // 🔌 用户断开连接
            override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
                Log.d("AppKit", "钱包已断开")
                _walletState.value = WalletConnectionState.Error("钱包已断开");

                // 清空地址状态
                //_currentAddress.value = null
            }

            // 其他回调按需实现，暂时可以留空
            override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {}
            override fun onSessionExtend(session: Modal.Model.Session) {}
            override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {}
            override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
                println("onSessionRequestResponse")
                // 检查是否有错误
                when (response.result) {

//                    if (response.result.error != null) {
//                        println("Transaction rejected or failed: ${response.error.message}")
//                        // 处理错误状态
//                    } else {
//                        // 成功！结果即为交易哈希
//                        // 注意：result 通常是一个 Any/JsonElement，需要转换为 String
//                        val transactionHash = response.result.toString()
//                        println("Transaction successful! Hash: $transactionHash")
//
//                        // 更新 UI 或跳转到交易状态查询页面
//                    }
                    is JsonRpcResponse.JsonRpcError -> {
                        val (id, code, message) = (response.result as JsonRpcResponse.JsonRpcError)
                        println("Transaction rejected or failed: ${id} ${code} ${message}")
                        _walletState.value = WalletConnectionState.ResError(code.toLong(), message)
                    }

                    is JsonRpcResponse.JsonRpcResult -> {
                        val (id, result) = (response.result as JsonRpcResponse.JsonRpcResult)
                        println("Transaction OK: ${id}  ${result}")
                    }
                }
            }

            override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {}
            override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {}
            override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
                Log.d("AppKit", "连接状态改变: ${state}")
                _walletState.value = WalletConnectionState.Disconnected
            }

            override fun onError(error: Modal.Model.Error) {
                Log.e("AppKit", "SDK 错误: ${error.throwable.message}")
                _walletState.value =
                    WalletConnectionState.Error("SDK 错误: ${error.throwable.message}")
            }
//            APPKIT SESSION监听到的ERROR：2025-11-21 14:37:07.423 11707-11816
//            SDK 错误: Batch subscribe error: src.length=10 srcPos=7 dst.length=10 dstPos=6 length=-6
//            Batch subscribe error (批量订阅错误)：
//                这发生在 AppKit 尝试订阅或处理来自 WalletConnect 中继服务器 (Relay Server) 的一批消息或主题更新时。
//                这是 WalletConnect 协议用于实时监听连接状态和交易请求的机制。
//            length=-6： 这是典型的 数组操作错误（类似于 Java/Kotlin 中的 System.arraycopy() ）。
//                length（要复制的长度）被计算成了一个负数（-6），这在进行内存或数组操作时是非法的，立即导致了程序崩溃或异常。
//            根本原因： 这是 AppKit/CoreClient SDK 内部处理消息的 订阅或数据批量处理逻辑中存在一个 Bug，导致它在计算数据块大小时出错。
        }

        // 注册代理
        AppKit.setDelegate(appKitModalDelegate)
    }


    // ✅ 提供给 UI 调用的连接方法
    //fun openWalletConnection() {
    override suspend fun connectToTrustWallet() {
        // 这会打开底部弹窗，让用户选择 MetaMask 等钱包
        //AppKit.connect {  }
//        AppKit.connect()
        _walletState.value = WalletConnectionState.Connecting
        // Step 1: Create or retrieve a pairing
        //    data class Pairing(
        //            val topic: String,
        //            val expiry: Long,
        //            val peerAppMetaData: AppMetaData? = null,
        //            val relayProtocol: String,
        //            val relayData: String?,
        //            val uri: String,
        //            @Deprecated("isActive has been deprecated. It will be removed soon.")
        //            val isActive: Boolean,
        //            val registeredMethods: String
        //        ) : Model()

        //dApp 调用 Pairing.create() → SDK 生成一个 pairing topic (token)
        //dApp 将此 topic 编码到 WalletConnect URI 中(after 'wc：', 64bytes)
        //💡 此时的 topic 称为 pairing topic，用于协商正式 session。
        //Pairing URI (Paring serialization )
        // wc:14f836400441f986ecddf34a9db26f043a4bc7010381eea7c8dcacc88cea70c7@2?relay-protocol=irn&expiryTimestamp=1764765666&symKey=9eb045a79904172de3221c11a2ffe057e9ac83e0830c25ea3f43c4bd71036866

        val pairing = CoreClient.Pairing.create() // generates a new WalletConnect URI
        println("Pairing URI: ${pairing?.uri}")

//2025/12/08/ appkit interal bug:
//        val chain = AppKit.chains.getSelectedChain(AppKit.selectedChain?.id)
//        mabye due to config changed for ProjectID in reown.com, session can not be established?
//        just android version issue, leave it now
// Step 2: Build namespaces
        //eth_sendTransaction → Used to send payments or execute smart contract calls.
        //personal_sign → Used to sign arbitrary messages for authentication or proof of ownership.
        //eth_signTypedData (EIP‑712) → Used to sign structured data (like orders, votes, or permits)
        //   in a human‑readable and verifiable way.
        val ethNamespace = Modal.Model.Namespace.Proposal(
//        eip155,11155111,Sepolia (以太坊新测试网),eip155:11155111:0xAddress...
//        eip155,80001,Polygon Mumbai (Polygon 测试网),eip155:80001:0xAddress...
//        eip155,5,Goerli (以太坊旧测试网，已弃用),eip155:5:0xAddress...

            chains = listOf("eip155:1"),
            methods = listOf("eth_sendTransaction", "personal_sign"),
            events = listOf("chainChanged", "accountsChanged")
        )

        val connectParams = pairing?.let {
            Modal.Params.Connect(
                namespaces = mapOf("eip155" to ethNamespace),
                optionalNamespaces = null,
                properties = null,
                pairing = it // Core.Model.Pairing
            )
        }

        //var mockAddress = "";
// Step 3: Connect
        connectParams?.let {
            AppKit.connect(
                connect = it,
                onSuccess = { session ->
                    println("AppKit.Connected(channel set): ${session}")
                    //mockAddress = session;
                    pairing?.uri?.let { openTrustWallet(it) }

                },
                onError = { error ->
                    println("Connection failed: $error")
                    _walletState.value =
                        WalletConnectionState.Error(error.throwable.message.toString());
                }
            )
        }
//        return WalletState(address = mockAddress, isConnected = true)

        // → You need to send this URI to TrustWallet (via deep link or QR)
    }


    // ... 在你的服务类中 ...
    /*
        suspend fun createPairingSynchronously(): Core.Model.Pairing? = withContext(Dispatchers.IO) {
            // ⚠️ 关键点：在 IO 线程中调用阻塞函数
            try {
                // 使用不带 methods 参数的 create 方法
                val pairing = CoreClient.Pairing.create(
                    onError = { error ->
                        Log.e("Reown", "CoreClient Create Error: ${error.throwable.message}")
                    }
                )
                // pairing 对象中就包含了 URI
                return@withContext pairing
            } catch (e: Exception) {
                Log.e("Reown", "创建配对时发生异常", e)
                return@withContext null
            }
        }
        // 1. 定义一个 Job，用于控制协程的生命周期
        private val job = SupervisorJob()

        // 2. 定义一个 CoroutineScope，使用 IO 调度器进行后台网络/IO操作
    // 并在 Scope 中包含 Job
        private val scope = CoroutineScope(Dispatchers.IO + job)

        fun connectTrustWalletDirectly() {
            // 启动一个协程来处理后台操作
            scope.launch { // 假设你有一个 CoroutineScope

                val pairing = createPairingSynchronously()

                if (pairing == null) {
                    Log.e("Reown", "配对对象为空，无法跳转。")
                    // 提示用户错误，例如 Toast
                    return@launch
                }

                // 关键点：配对对象 (Pairing) 中就包含了 URI 属性
                val wcUri = pairing.uri

                Log.d("Reown", "最终生成的 URI: $wcUri")

                // 4. 跳转 Trust Wallet (需要在主线程执行 UI 操作)
                withContext(Dispatchers.Main) {
                    openTrustWallet(wcUri)
                }
            }
        }

        fun connectTrustWalletDirectly2() {
            // 定义你要连接的配置
            val chains = listOf("eip155:1") // Ethereum Mainnet
            //val chains = listOf("eip155:1", "eip155:137") // 例如: Ethereum Mainnet (1), Polygon (137)

            val methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTypedData")
            val events = listOf("chainChanged", "accountsChanged")

            // 构建 Proposal 对象
            val proposalNamespace = Sign.Model.Namespace.Proposal(
                chains = chains,
                methods = methods,
                events = events
            )

            // 🛠️ 推荐做法：
            // 1. 将 namespaces (必选) 设为 null 或空 map (除非你的 App 离了某条链完全无法运行)
            // 2. 将配置放入 optionalNamespaces (可选)
            val connectParams = Sign.Params.Connect(
                namespaces = null,
                optionalNamespaces = mapOf("eip155" to proposalNamespace),
                pairing = null // ✅ 显式传入 null 创建新连接
            )

            SignClient.connect(
                connectParams,
                onSuccess = { success ->
                    val wcUri = success
                    Log.d("Reown", "直连 URI: $wcUri")
                    openTrustWallet(wcUri)
                },
                onError = { error ->
                    Log.e("Reown", "错误: ${error.throwable.message}")
                }
            )
        }

    */
    private fun openTrustWallet(wcUri: String) {
        // 方式二：使用 Universal Link (推荐)
        // 它可以自动检测 APP，如果没有安装可能会跳到下载页或官网
        //"https://link.trustwallet.com/wc
        val deepLink = "${baseWalletDeepLinkURL}?uri=${Uri.encode(wcUri)}"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(deepLink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("Wallet", "无法打开 Trust Wallet", e)
        }
    }

    /*
    override suspend fun connectToTrustWallet(): WalletState {
        if (!isInitialized) {
            throw IllegalStateException("Android WalletService must be initialized first.")
        }

        val connectUrl = buildTrustWalletConnectUrl(initParams!!.metaData.redirect)

        // 确保 context 是 Activity 类型才能启动外部 App
        if (context is Activity) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(connectUrl))
            context.startActivity(intent)
            println("Android: Launching Trust Wallet for connection via URL: $connectUrl")
        } else {
            // 如果传入的是 Application Context，外部 Intent 可能需要 FLAG_ACTIVITY_NEW_TASK
            println("Android: WARNING: Only Application Context available. Intent launch may fail. Simulating connection...")
        }

        // 模拟: 等待 3 秒后，假设连接成功并返回一个地址。
        kotlinx.coroutines.delay(3000)

        val mockAddress = "0x" + List(40) {
            "0123456789abcdef"[kotlin.random.Random.nextInt(16)]
        }.joinToString("")

        return WalletState(address = mockAddress, isConnected = true)
    }
*/
    override suspend fun disconnect() {
        println("Android: Disconnecting wallet via Appkit SDK...")
        var bError = true;
        AppKit.disconnect(
            onSuccess = {
                bError = false
                _walletState.value = WalletConnectionState.Disconnected;

            },
            onError = {
                Log.e("AppKit", "断开失败")
                _walletState.value = WalletConnectionState.Error("断开失败");

            }
        )
//        if (bError)
//            return WalletState(address = null, isConnected = true)
//        else
//            return WalletState(address = null, isConnected = false)
    }

    override suspend fun fetchBalances(address: String): List<TokenBalance> {
        // 实际操作: 调用 Appkit SDK 接口获取余额
        println("Android: Fetching real balances from Appkit for $address...")

        // 模拟返回数据
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

    /**
     *  chain is declared in the session namespace, not inside the transaction object.
     */
    //override suspend fun sendTransaction(addressFrom: String, addressTo: String, topic:String, amount: Double): Boolean {
    override suspend fun sendTransaction(transactionParam: String): Boolean {
        println("Android: Sending transaction via Appkit SDK...")
        //kotlinx.coroutines.delay(2500)
//        return kotlin.random.Random.nextBoolean()

        // Assume you already have an approved session
        //val session = approvedSession
        //val topic = session.topic

// Send transaction request
        AppKit.request(
            //request = Modal.Params.Request(
            request = com.reown.appkit.client.models.request.Request(
                method = "eth_sendTransaction",
                params = transactionParam
            ),
            onSuccess = { result: SentRequestResult ->
                println("Transaction result: ${result}")
            },
            onError = { error: Throwable ->
                println("Transaction failed: $error")
                _walletState.value = WalletConnectionState.Error("Transaction failed: $error");
            }
        )
        return true;
    }

    //    EIP-681 格式（Ethereum URI Scheme），它能让钱包自动识别链和金额：
//    QR 码内容=ethereum:[地址]@[ChainID]?value=[金额]
    override fun generateReceiveQRCode(address: String): String {
        return "ethereum:$address"
    }

    private fun buildTrustWalletConnectUrl(redirectUri: String): String {
        return "trust://wc?uri=WAKU_PROTOCOL_PAYLOAD&redirect=$redirectUri"
    }

}

/**
 *  20251204 USE KONIN INSTEAD
 * 外部调用函数，用于在 Android 主入口点初始化 WalletService 实例。
 * @param context 通常是 MainActivity 或 Application 实例。
 */
//fun initAndroidWalletService(context: Context) {
//    androidWalletServiceInstance = AndroidAppkitWalletService(context)
//    println("Android Wallet Service Initialized and assigned to walletService.")
//}