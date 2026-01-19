package io.xa.sigad.data.model


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// commonMain/data/Network.kt

data class BlockchainNetwork(
    val englishName: String,
    val chineseName: String,
    val chainType: String, // "eip155"
    val chainId: String,
    val isTestnet: Boolean = false // 默认不是测试网
)

// 扩展函数，用于处理您数据中缺失 "test" 字段的情况
fun Map<String, String>.toBlockchainNetwork(): BlockchainNetwork? {
    // 检查必需字段是否存在
    val en = this["en"] ?: return null
    val zh = this["zh"] ?: return null
    val chain = this["chain"] ?: return null
    val chainid = this["chainid"] ?: return null

    // 检查 "test" 字段是否存在并转换
    // 对于您数据中 "eth-sepolia" 缺失 "test" 字段，我们假设它不是测试网 (或者根据实际情况判断)
    val isTest = this["test"]?.toBoolean() ?: false

    return BlockchainNetwork(
        englishName = en,
        chineseName = zh,
        chainType = chain,
        chainId = chainid,
        isTestnet = isTest
    )
}

val ALL_NETWORKS: List<BlockchainNetwork> = listOf(
    mapOf("en" to "eth-mainnet", "zh" to "以太坊主网", "chain" to "eip155", "chainid" to "1", "test" to "false"),
    mapOf("en" to "eth-goerli", "zh" to "以太坊测试旧", "chain" to "eip155", "chainid" to "5", "test" to "true"),
    mapOf("en" to "eth-sepolia", "zh" to "以太坊测试新", "chain" to "eip155", "chainid" to "11155111", "test" to "true"), // 假设缺失 "test" 字段的 Sepolia 是测试网
    mapOf("en" to "polygon-mainnet", "zh" to "Polygon主网", "chain" to "eip155", "chainid" to "137", "test" to "false"),
    mapOf("en" to "polygon-mumbai", "zh" to "Polygon测试", "chain" to "eip155", "chainid" to "80001", "test" to "true"),
    mapOf("en" to "base-sepolia", "zh" to "以太坊测试L2", "chain" to "eip155", "chainid" to "84532", "test" to "true")
).mapNotNull { it.toBlockchainNetwork() }

val MAIN_NETWORKS = ALL_NETWORKS.filter { !it.isTestnet }
val TEST_NETWORKS = ALL_NETWORKS.filter { it.isTestnet }



fun getBlockchainFullId(networkName: String): String? {
    val network = ALL_NETWORKS.find { it.englishName == networkName }
    return if (network != null) {
        "${network.chainType}:${network.chainId}"
    } else {
        null
    }
}

/**
 * if chinese name found, return it; else return namesapce:reference
 */
fun getBlockchainChineseName(namespace : String = "eip155", reference: String ) : String?{
    val network = ALL_NETWORKS.find { it.chainType == namespace && it.chainId == reference }
    return if (network != null)  network.chineseName else "${namespace}:${reference}"
}

fun getBlockchainEnglishName(namespace : String = "eip155", reference: String ) : String?{
    val network = ALL_NETWORKS.find { it.chainType == namespace && it.chainId == reference }
    return network?.englishName
}

// commonMain/state/CurrentNetworkState.kt (占位符)

// 假设这是一个全局可观察的状态
object CurrentNetworkState {
    // 初始值可以设置为您的默认网络，例如以太坊主网 Chain ID: "1"
    //使用 androidx.compose.runtime.State
    //import androidx.compose.runtime.getValue // 🚀 确保有此导入
//    private val _currentChainId = mutableStateOf("1")
//    val currentChainId: State<String> = _currentChainId

    private val _currentChainId = MutableStateFlow("1")
    // 这里的类型是 kotlinx.coroutines.flow.StateFlow<String>
    val currentChainId: StateFlow<String> = _currentChainId.asStateFlow()

    fun setNetwork(chainId: String) {
        _currentChainId.value = chainId
    }
}