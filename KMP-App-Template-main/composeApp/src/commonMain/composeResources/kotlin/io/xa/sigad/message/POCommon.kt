package io.xa.sigad.message
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.math.round
import kotlin.random.Random
import kotlinx.serialization.json.*

@Serializable
data class AirDropData(
    val id: String , //accountId, id allocated by post office server
    val chatcpkh: String, //post office's public key
    val gas: Int
)


@Serializable
data class MMAccountData(
    val seed:Int,
    var airDrop: AirDropData?,
    val po: String
) {
    val userId: String
        get() = "${this.po}.${this.airDrop?.id}"

    val chatKey:String
        get() {
            val a = sha256Hash("test-chat-${seed}")
            println("test-chat-${seed}   ${a.toHex()}")

            return sha256Hash("test-chat-${seed}").toHex()
        }
    val masterKey:String
        get(){
            return sha256Hash("test-master-${seed}").toHex()
        }
    val nickname: String
        get() = "RandNick${seed}"

    val  ethAddress :String = "0xf2ec4a773ef90c58d98ea734c0ebdb538519b988" // 用户要绑定的以太坊钱包地址

    val airDroped: Boolean
        get() = (airDrop != null)
}

@Serializable
data class MMAccountBalance (
    val createtime: Long,/* 账号注册/创建时间 */
    val gas: Double,/* 账号邮票数量 */
    val master: String, /* 账号主密钥公钥 */
    val money: Double/* 账号余额, 未使用 */
)

/**
 * 账号资料数据格式
 */
@Serializable
data class MMAccountProfile (
    val createtime: Long,/* 账号注册/创建时间 */
    val master: String, /* 账号主密钥公钥 */
    val name: String, /* 账号昵称 */
    val props: Map<String, JsonElement> /* 账号属性 */
)



@Serializable
data class MMSMessage(
    val sn: Int,
    val speaker: String,
    val receiver: String,
    val content: String,
    val time: Long,
    val prevhash: String? = null,
    val pending: Boolean? = null,
    val sign0: String? = null,
    val sign1: String? = null,
    val sign2: String? = null
)


/**
 * Sorts a JSONElement according to the specified rules.
 *
 * - JSONArray elements are left as is (no sorting).
 * - JSONObject elements are sorted by their keys.
 * - Recursively processes all elements within the JSON structure.
 *
 * @param element The JSONElement to sort.
 * @return A new JSONElement with the sorting applied.
 */
//fun sortJsonElement(element: JsonElement): JsonElement {
//    return when (element) {
//        is JsonObject -> {
//            JsonObject(element.jsonObject.toMap(). .mapValues { sortJsonElement(it.value) })
//        }
//        is JsonArray -> {
//            JsonArray(element.map { sortJsonElement(it) })
//        }
//        else -> element // JsonPrimitive or JsonNull - no sorting needed
//    }
//}

/**
 * Recursively sorts the keys of JsonObjects within a JsonElement.
 * JsonArrays are mapped with their elements also sorted.
 * JsonPrimitives and JsonNull are returned as is.
 *
 * This implementation is pure Kotlin and suitable for Kotlin Multiplatform (KMP) commonMain.
 */
fun sortJsonElement(element: JsonElement): JsonElement {
    return when (element) {
        is JsonObject -> {
            // 1. Get the entries of the JsonObject (which is a Set<Map.Entry<String, JsonElement>>)
            // 2. Convert it to a List of entries
            // 3. Sort the list of entries by key
            // 4. Convert the sorted list back to a Map
            // 5. Recursively sort the values of this new map
            val sortedEntries = element.entries
                .sortedBy { it.key } // Sort by key
                .associate { (key, value) ->
                    key to sortJsonElement(value) // Recursively sort the value
                }
            JsonObject(sortedEntries) // Create a new JsonObject from the sorted map
        }
        is JsonArray -> {
            // Map each element in the array, recursively sorting it
            JsonArray(element.map { sortJsonElement(it) })
        }
        else -> element // JsonPrimitive or JsonNull - no sorting needed
    }
}
//
//fun main() {
//    val jsonString = """
//        {
//          "b": 2,
//          "a": 1,
//          "c": {
//            "f": 6,
//            "e": 5,
//            "d": [9, 8, 7],
//            "g": { "z": 26, "y": 25 }
//          },
//          "arr": [4, 3, 2],
//          "str": "hello"
//        }
//    """.trimIndent()
//
//    val json = Json.Default
//    val jsonElement = json.parseToJsonElement(jsonString)
//
//    val sortedElement = sortJsonElement(jsonElement)
//    val sortedJsonString = sortedElement.toString()
//
//    println("Original JSON:\n$jsonString\n")
//    println("Sorted JSON:\n$sortedJsonString")
//}
////
//@Serializable
//data class UnitMessage(
//    val callee: String,
//    val caller: String,
//    val callid: Int,
//    val proc: MutableList<Any> = mutableListOf(),
//    val result: String? = null,
//    val reply: Int? = null,
//)
//
//
//@Serializable
//data class MessageRequest(
//    val type: String = "unit_message",
//    var message: UnitMessage
//
//    )
//


fun generateUnRegisteredAcount() : MMAccountData {
    val seedDouble = Random.nextDouble()
    val seed = round(seedDouble * (9999 - 1000 + 1)) + 1000
    return MMAccountData(seed.toInt(), null , POConfig.poName);
}

fun reverseByteArrayAndFlipEndian(byteArray: ByteArray): ByteArray {
    val reversedArray = byteArray.reversed().toByteArray()
    val result = ByteArray(reversedArray.size)
    for (i in reversedArray.indices) {
        result[i] = reversedArray[i].reverseBits()
    }
    return result
}

// Extension function to reverse the bits of a Byte
fun Byte.reverseBits(): Byte {
    var b = this.toInt() and 0xFF
    b = (b shr 4) or (b shl 4) and 0xFF
    b = ((b and 0xCC) shr 2) or ((b and 0x33) shl 2) and 0xFF
    b = ((b and 0xAA) shr 1) or ((b and 0x55) shl 1) and 0xFF
    return b.toByte()
}
