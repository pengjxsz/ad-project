package io.xa.sigad.data


import io.xa.sigad.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.StringValues
import io.xa.sigad.message.masterSignCompactB64
import io.xa.sigad.message.sortJsonElement
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.*
// 假设的基础 URL
private const val BASE_URL = "http://119.23.248.75:7000"
//private const val BASE_URL = "http://legal.adpal.xyz:7000"

// 示例用户 ID (需要实际实现签名机制来获取)
private  val CURRENT_USER_ID: String //= "73746d3a257f0a30b70cf70ddcd5444df29c745ff68e73cd15d7e36547bd9ee5"
    get(){
       return  ConfigFileManager.config.user_id
    }
private  val CURRENT_USER_MASTERKEY :String // = "65b0b9314d6c622a077464ea38c03d6e8ed46af64bff9ecff1ed4630f502f028"
    get(){
        return ConfigFileManager.config.masterKey
    }

//private const val FAKE_REQUEST_SIGN = "0x..." // 假设签名已实现

// 使用宽松的 Json 配置，允许 null 值并输出默认值（防止排序丢失字段）
private val JsonForSigning = Json {
    prettyPrint = false // 必须禁用漂亮的打印，以确保精确的 JSON 字符串匹配
    encodeDefaults = false // 确保所有字段（包括默认的 null/空）都输出，以便排序准确
}

/////////////////////////////////////////////////////////////////////////////
//签名
/////////////////////////////////////////////////////////////////////////////
/**
 * 步骤 1: 对签名对象按 key 升序排列后转为 JSON 字符串。
 *
 * kotlinx.serialization 默认不保证顺序。因此，我们必须手动处理 JsonElement
 * 以确保键是升序排列的。
 *
 * @param obj 待签名的对象
 * @return 规范化的 JSON 字符串
 */
fun normalizeAndSerialize(obj: Obj2Sign): String {
    // 1. 将 Obj2Sign 转换为 JsonElement
    val jsonElement = JsonForSigning.encodeToJsonElement(obj)

    // 2. 确保 JsonElement 是一个 JsonObject
    require(jsonElement is JsonObject) { "Expected JsonObject for signing." }

    // 3. 对 JsonObject 的键进行升序排序
    //val sortedMap = jsonElement.toMap().toSortedMap()
    val sortedJsonElement = sortJsonElement(jsonElement)

//    val sortedJsonElement = JsonObject(sortedMap)
    val stringToBeSigned =  JsonForSigning.encodeToString(sortedJsonElement)
    println("stringToBeSigned: " + stringToBeSigned)
    return stringToBeSigned;
    //return sortedJsonElement.toString()
}

/**
 * 步骤 2 & 3: 计算 SHA-256 哈希并签名。
 *
 * @param normalizedJson 规范化的 JSON 字符串
 * @return 以太坊签名 (0x开头的132位16进制字符串)
 */
fun signNormalizedJson(normalizedJson: String, privateKey: String): String {
    val signature = masterSignCompactB64(normalizedJson, privateKey, false)
    println("signNormalizedJson: " + signature)
    return signature
}


// 自定义业务逻辑异常
class ApiException(
    val apiMessage: String? = null,
    val apiErrorType: String? = null
) : Exception("API request failed: $apiErrorType ($apiMessage)")


class AdsApi(private val userId: String = CURRENT_USER_ID) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        // 可以添加拦截器处理通用请求头和错误
    }

    /**
     * 通用 API 响应处理器，检查成功标志并解包数据。
     * @throws ApiException 如果 success 为 false
     */
    private fun <T> handleResponse(apiResponse: ApiResponse<T>): T {
        if (apiResponse.success) {
            // 如果成功，返回 data 字段 (T!)
            return apiResponse.data ?: throw ApiException("API returned success: true but data field is null.")
        } else {
            // 如果失败，抛出包含错误信息的异常
            throw ApiException(apiResponse.message, apiResponse.error)
        }
    }

    // 假设签名实现函数，目前仅返回一个假的签名和时间戳
    private fun getAuthHeaders(method: String,
                               urlPath: String,
                               body: JsonElement? = null,
                               query: JsonElement? = null): Map<String, String> {

        if (CURRENT_USER_MASTERKEY == "")
            throw Exception("Account not initialized!")
        // 1. 获取请求时间戳 (单位为毫秒)
        val requestTime = Clock.System.now().toEpochMilliseconds().toString()

        // 2. 构造待签名对象 Obj2Sign
        val objToSign = Obj2Sign(
            userId = userId,
            timestamp = requestTime,
            url = urlPath,
            method = method,
            // body/query 必须是非空或 JsonNull
            body = body, //?: JsonNull,
            query = query //?: JsonNull
        )

        // 3. 规范化、排序并序列化为 JSON 字符串
        val normalizedJson = normalizeAndSerialize(objToSign)

        // 4. 计算 SHA-256 哈希并签名
        // ⚠️ 注意：这里传入的 privateKey 应该用于实际签名
        val requestSign = signNormalizedJson(normalizedJson, CURRENT_USER_MASTERKEY)

        return mapOf(
            "user-id" to userId,
            "request-time" to requestTime,
            "request-sign" to "0x${requestSign}"
        )

    }

    // --- 接口 1: 健康检查接口 (无需鉴权) ---
    suspend fun healthCheck(): HealthCheckResponse{
        // 由于健康检查的响应示例结构不同，我们将其特殊处理
        return try {
            val response = client.get("$BASE_URL/api/health")

            if (response.status.isSuccess()) {
                // 直接解析为 ApiResponse<HealthCheckResponse>
                val response1: ApiResponse<HealthCheckResponse> = response.body()

                return handleResponse(response1)

            } else {
                // 如果状态码不是 2xx，手动构造一个失败响应
                HealthCheckResponse(
                    success = false,
                    message = "Health check failed with status: ${response.status.value}" ,
                    timestamp = ""
                )
            }
        } catch (e: Exception) {
            HealthCheckResponse(success = false, message = "Network error: ${e.message}", timestamp = "")
        }
    }

    // --- 接口 2.1: 预注册接口 ---
    suspend fun preRegister(
        masterPK: String,
        chatPK: String,
        nickname: String
    ): PreRegisterResponse {
        val requestBody = PreRegisterRequest(
            masterPK = masterPK,
            chatPK = chatPK,
            nickname = nickname
        )

        val response:ApiResponse<PreRegisterResponse> =  client.post("$BASE_URL/api/user/pre-register") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
            // 无需鉴权头
        }.body()

        return handleResponse(response)

    }

    // --- 接口 2.2: 正式注册接口 ---
    suspend fun register(
        registerPayload: String,
        signature: String,
        devicePK: String,
        deviceChip: String
    ): RegisterResponse {
        val requestBody = RegisterRequest(
            registerPayload = registerPayload,
            signature = signature,
            devicePK = devicePK,
            deviceChip = deviceChip
        )

        val response: ApiResponse<RegisterResponse> = client.post("$BASE_URL/api/user/register") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
            // 无需鉴权头
        }.body()

        return handleResponse(response)

    }

    // --- 接口 3.1: 获取广告列表 ---
    suspend fun fetchAdsList(): GetAdsResponse {
//        val response : ApiResponse<GetAdsResponse>  =  client.get("$BASE_URL/api/ads3/ads") {
//            headers.appendAll(getAuthHeaders() as StringValues)
//        }.body()
//
//        return handleResponse(response)
        val urlPath = "/api/ads3/ads"

        val response :ApiResponse<GetAdsResponse> = client.get("${BASE_URL}${urlPath}") {
            // GET 请求没有 body， query 字段如果不需要则为 null
            getAuthHeaders("GET", urlPath).forEach { (key, value) ->
                headers.append(key, value)
            }
        }.body()
        return handleResponse(response)
    }

    // --- 接口 3.2: 上报广告点击事件 ---
    suspend fun reportAdClick(adId: String, campaignId: String): Map<String, Boolean> {
        val requestBody = ClickAdRequest(adId, campaignId)
        val jsonBody = JsonForSigning.encodeToJsonElement(requestBody);

        val urlPath = "/api/ads3/click"

        val response :ApiResponse<Map<String, Boolean>> =  client.post("${BASE_URL}${urlPath}") {
            contentType(ContentType.Application.Json)
            getAuthHeaders("POST", urlPath, jsonBody).forEach { (key, value) ->
                headers.append(key, value)
            }
            setBody(requestBody)
        }.body()

        return handleResponse(response)

    }

    //3.3 earning
    suspend fun getAdsEarnings(): AdsEarnings {

        val urlPath = "/api/ads3/earnings"

        val response :ApiResponse<AdsEarnings> = client.get("${BASE_URL}${urlPath}") {
            // GET 请求没有 body， query 字段如果不需要则为 null
            getAuthHeaders("GET", urlPath).forEach { (key, value) ->
                headers.append(key, value)
            }
        }.body()
        return handleResponse(response)
    }

    //0xd8131eD60c407819254163f5ca50C068Ee1C5D1D trustwallet eth-wallet
    //{"addresses": [{"address": "0x04fb55008c528a31736db75767f3880cdfac3c9c", "networks": ["eth-mainnet"]}]}
    // --- 接口 4.1: 获取代币资产 (用于显示收益) ---
    suspend fun fetchUserAssets(address: String, networks: List<String>): GetAssetsResponse {
        val requestBody = AssetRequest(
            addresses = listOf(AssetRequestAddress(address, networks))
        )
        val jsonBody = JsonForSigning.encodeToJsonElement(requestBody);

        val urlPath = "/api/assets/tokens"
        println(" fetchUserAssets, address:  ${address}, network: ${networks.first()}")
        val response: ApiResponse<GetAssetsResponse> =  client.post("${BASE_URL}${urlPath}") {
            contentType(ContentType.Application.Json)
            getAuthHeaders("POST", urlPath, jsonBody).forEach { (key, value) ->
                headers.append(key, value)
            }
            println(requestBody.toString())
            setBody(requestBody)
        }.body()
        // 调用解包函数，直接返回 GetAssetsResponse
        return handleResponse(response)
    }

}