package io.xa.sigad.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import coil3.compose.rememberConstraintsSizeResolver
import com.ionspin.kotlin.bignum.BigNumber
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.xa.sigad.screens.wallet.EthIcon
import io.xa.sigad.screens.wallet.MoneyIcon
import io.xa.sigad.screens.wallet.UsdcIcon
import io.xa.sigad.screens.wallet.UsdtIcon
import io.xa.sigad.screens.wallet.WalletIcon
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// --- 通用响应格式 ---
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class Obj2Sign(
    val body: JsonElement? = null,

    val method: String, // 使用 String 来兼容 'GET' | 'POST'
    val query: JsonElement? = null,
    val timestamp: String,

    val url: String,
    val userId: String

)

// --- 广告任务数据 ---
@Serializable
data class Ads3Ad(
    val adBlockId: String,
    val adId: String,
    val campaignId: String,
    val destination: Destination,
    val icon: String,
    val image: String,
    val text: String,
    val clicked: Boolean? = false
)

@Serializable
data class Destination(
    val actionType: String,
    val url: String // 广告目标 URL
)

// --- 3.1 获取广告列表响应数据 ---
@Serializable
data class GetAdsResponse(
    val ads: List<Ads3Ad> // 广告列表
)

// --- 3.2 上报广告点击事件请求数据 ---
@Serializable
data class ClickAdRequest(
    val adId: String,
    val campaignId: String
)

// --- 3.2 上报广告点击事件请求数据 ---
@Serializable
data class AdsEarnings(
    val total: Float // 广告收益总计
)

// 18 是以太坊的标准小数位数 (Wei 到 Ether)
private const val ETHEREUM_UNIT_EXPONENT = 18L

// 预先计算 10^18 的 BigDecimal，以供除法使用
private val WEI_SCALE_FACTOR: BigDecimal = BigDecimal.TEN.pow(ETHEREUM_UNIT_EXPONENT)

// --- 4.1 获取代币资产响应数据（仅获取收益所需的关键字段） ---
@Serializable
data class Token(
    val address: String? = null,//": "0x04fb55008c528a31736db75767f3880cdfac3c9c",
    val network: String? = null, //"eth-mainnet",
    val tokenAddress: String? = null, //"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
    val tokenBalance: String, // 代币余额 (作为收益)
    val tokenMetadata: TokenMetadata? = null,
    val tokenPrices: List<TokenPrice>? = null
) {
    val tokenSymbol: String?
        get() {
            return if (tokenAddress == null) "ETH" else tokenMetadata?.symbol
        }
    val weiAmount: BigInteger
        get() {
            println("weiMaount: " + tokenBalance.removePrefix("0x"))
            return BigInteger.parseString(tokenBalance.removePrefix("0x"), 16)
        }
    val decimalAmount: BigDecimal
        get() {
            val decimalPlaces =
                if (tokenMetadata?.decimals == null) if (tokenAddress == null) ETHEREUM_UNIT_EXPONENT else 4L else tokenMetadata.decimals //最终输出的小数位数（例如，8位）
            val roundingMode: RoundingMode = RoundingMode.FLOOR
            val amountInDecimal = BigDecimal.fromBigInteger(weiAmount)
            // 2. 定义除法所需的精度模式
            // scale 必须设置为 ETHEREUM_UNIT_EXPONENT，以确保在除法前保留足够的精度
            val divisionDecimalMode = DecimalMode(
                scale = ETHEREUM_UNIT_EXPONENT + decimalPlaces, //参数定义了 BigDecimal 结果在内部和运算中必须保持的小数位数
                roundingMode = roundingMode,
                decimalPrecision = decimalPlaces  //decimalPrecision defines how many digits should significand have
                // 最终精度控制在 decimalPlaces
            )

            // 3. 执行除法: Wei / 10^18
            val baseUnitAmount = amountInDecimal.divide(
                BigDecimal.TEN.pow(decimalPlaces), //WEI_SCALE_FACTOR,
                divisionDecimalMode
            )
            println("decimal is $decimalPlaces, ${tokenMetadata?.decimals}, decimalAmount is $baseUnitAmount")

            // 4. 返回精确的字符串表示
            return baseUnitAmount
            //return baseUnitAmount.toPlainString()
        }

    //About DecimalMode
    /**
     * Decimal precision signifies how many digits will significand have. If decimal precision is 0 and RoundingMode is NONE
     * infinite precision is used.
     * @param decimalPrecision max number of digits allowed. Default 0 is unlimited precision.
     * @param roundingMode default RoundingMode.NONE is used with unlimited precision and no specified scale.
     * Otherwise specify mode that is used for rounding when decimalPrecision is exceeded, or when scale is in use.
     * @param scale is number of digits to the right of the decimal point.
     * When this is specified, a RoundingMode that is not RoundingMode.NONE is also required.
     * Scale cannot be greater than precision - 1.
     * If left to default = null, no scale will be used. Rounding and decimalPrecision apply.
     * Negative scale numbers are not supported.
     * Using scale will increase the precision to required number of digits.
     */
    val textAmount: String
        get() {
            val decimalPlaces =
                if (tokenMetadata?.decimals == null) 6 else tokenMetadata.decimals //最终输出的小数位数（例如，8位）
            val roundedD = decimalAmount.roundToDigitPositionAfterDecimalPoint(6,RoundingMode.FLOOR)
//                DecimalMode(
//                    scale = decimalPlaces, //参数定义了 BigDecimal 结果在内部和运算中必须保持的小数位数
//                    roundingMode = RoundingMode.CEILING,
//                    //decimalPrecision = 4  //decimalPrecision defines how many digits should significand have
//                    // 最终精度控制在 decimalPlaces
//                )
//            )
            //val balanceText = roundedD.toPlainString() + " " + tokenMetadata?.symbol
            val rawString = roundedD.toStringExpanded() //roundedD.toPlainString()
            println(" textAmount ${rawString}")
            //val decimalPlaces = 6
            val balanceText = if (!rawString.contains(".")) {
                rawString + "." + "0".repeat(decimalPlaces.toInt())
            } else {
                val parts = rawString.split(".")
                val currentDecimals = parts[1].length
                if (currentDecimals < decimalPlaces) {
                    rawString + "0".repeat((decimalPlaces - currentDecimals).toInt())
                } else {
                    rawString
                }
            }
            return balanceText
        }

    val latestPriceString: String
        get() {
            val s = tokenPrices?.lastOrNull { it.currency == "usd" }?.value ?: "0"
            val d = BigDecimal.parseString(s)
            val roundedD = d.roundSignificand(
                DecimalMode(
                    scale = 4, //参数定义了 BigDecimal 结果在内部和运算中必须保持的小数位数
                    roundingMode = RoundingMode.FLOOR,
                    //decimalPrecision = 4  //decimalPrecision defines how many digits should significand have
                    // 最终精度控制在 decimalPlaces
                )
            )
            return "${tokenMetadata?.symbol}" + roundedD.toPlainString()
        }

}

@Serializable
data class TokenMetadata(
    val symbol: String?,// 代币符号
    val decimals: Long?, //18
    val logo: String?,//"https://static.alchemyapi.io/images/assets/2396.png",
    val name: String?,//"WETH",
)

@Serializable
data class TokenPrice(
    val value: String,// 价格值: "2800.5362031647"
    val currency: String, //"usd",
    val lastUpdatedAt: String //"2025-11-24T00:00:00Z",)
)

@Serializable
data class GetAssetsResponse(
    val tokens: List<Token>
) {

    //precon: only one network is queried
    //if this network has token address defined, then match these addresses
    //if tokenadrress null, it should be main network balance
    fun getEthAndUSDTokens(): List<Token> {
        if (tokens.isEmpty())
            return listOf()
        val network = tokens[0].network;
        val tokenAddressList = tokenaddressMap[network]?.values
        return if (tokenAddressList != null)
            tokens.filter { it.tokenAddress == null || tokenAddressList.contains(it.tokenAddress) }
        else
            listOf();
    }

}

// --- 4.1 获取代币资产请求数据（简化的关键请求体） ---
@Serializable
data class AssetRequestAddress(
    val address: String,
    val networks: List<String>
)

@Serializable
data class AssetRequest(
    val addresses: List<AssetRequestAddress>
)

// --- 接口 1: 健康检查接口响应模型 ---
@Serializable
data class HealthCheckResponse(
    val success: Boolean,
    val message: String,
    val timestamp: String // 假定时间戳是一个字符串
)

// --- 2.1 预注册接口请求数据 ---
@Serializable
data class PreRegisterRequest(
    val masterPK: String, // 主密钥的未压缩公钥 (0x04开头的132位16进制字符串)
    val chatPK: String, // 聊天密钥的未压缩公钥 (0x04开头的132位16进制字符串)
    val nickname: String // 昵称
)

// --- 2.1 预注册接口响应数据 ---
@Serializable
data class PreRegisterResponse(
    val registerPayload: String // 待签名的注册信息 (JSON字符串)
)

// --- 2.2 正式注册接口请求数据 ---
@Serializable
data class RegisterRequest(
    val registerPayload: String, // 预注册接口返回的待签名注册信息
    val signature: String, // 对 registerPayload 的 SHA-256 值签名 (以太坊签名标准格式)
    val devicePK: String, // 硬件设备的公钥
    val deviceChip: String // 硬件设备的芯片 ID
)

// --- 2.2 正式注册接口响应数据 ---
@Serializable
data class RegisterResponse(
    val userId: String // 用户ID (64位的16进制字符串，不含0x前缀)
)


val currencyIconDecimalMaps = mapOf(
    "ETH" to mapOf("icon" to EthIcon, "decimal" to 18),
    "USDC" to mapOf("icon" to UsdcIcon, "decimal" to 6),
    "USDT" to mapOf("icon" to UsdtIcon, "decimal" to 6),
    "base-sepolia" to mapOf("icon" to WalletIcon, "decimal" to 18),
    "polygon-mainnet" to mapOf("icon" to WalletIcon, "decimal" to 18),
    "polygon-mumbai" to mapOf("icon" to WalletIcon, "decimal" to 18),
)

//now support 5 chains/network
//for each netwrok, the usdc and usdt tokenaddress as below (1st: usdc, 2nd: usdt)
val tokenaddressMap = mapOf(
    "eth-mainnet" to mapOf(
        "USDC" to "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48",
        "USDT" to "0xdac17f958d2ee523a2206206994597c13d831ec7"
    ),
    "eth-goerli" to mapOf(),
    "eth-sepolia" to mapOf( "USDC" to "0x1c7d4b196cb0c7b01d743fbc6116a902379c7238"),
    "base-sepolia" to mapOf("USDC" to "0x036cbd53842c5426634e7929541ec2318f3dcf7e"),
    "polygon-mainnet" to mapOf(
        "USDC" to "0x3c499c542cef5e3811e1192ce70d8cc03d5c3359",
        "USDT" to "0xc2132d05d31c914a87c6611c10748aeb04b58e8f"
    ),
    "polygon-mumbai" to mapOf("USDC" to "0x5d6e7e532a32631a5f1246c1c7332c7b1a3b3d4a")
)

/**
 * 代币余额信息
 */
data class TokenBalance(
    val name: String,
    val symbol: String,
    val balance: String,
    val icon: ImageVector,
    val color: Color,
    val price: String = "0"
)


fun mapTokensToTokenBalances(tokenList: List<Token>): List<TokenBalance> {
    println(" ................mapTokensToTokenBalances ${tokenList.size}")
    return tokenList.mapNotNull { token ->
        val metadata = token.tokenMetadata
        if (metadata != null) {
            // 解析tokenBalance为Double
//            val balanceValue = try {
//                println(" ................map tokenBalance ${token.textAmount} ${token.tokenBalance}")
//                token.textAmount.toDouble()
//            } catch (e: NumberFormatException) {
//                println(" execption , ${e.cause} ${e.message}")
//                0.0
//            }
            val balanceValue = token.textAmount
            val symbol = token.tokenSymbol
            // 获取图标
            val icon : ImageVector = symbol.let { networkName ->   //networkName or tokenName
                currencyIconDecimalMaps[networkName]?.get("icon") as ImageVector?
            } ?: WalletIcon // 如果找不到对应的网络图标，则使用默认图标

            // 获取价格
            val price = token.tokenPrices?.firstOrNull()?.value ?: ""

            TokenBalance(
                name = metadata.name ?: "",
                symbol = symbol.toString(),
                balance = balanceValue,
                icon = icon,
                color = Color(0xFF000000), // 颜色不赋予特定值，使用默认黑色
                price = price
            )
        } else {
            println("token has not tokenMetadata!")
            null
        }
    }
}
