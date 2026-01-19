package io.xa.sigad.message
//
//import androidx.lifecycle.LifecycleOwner
//import kotlin.math.floor
//import kotlin.math.round
//import kotlin.random.Random


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.concurrent.Volatile


interface PostOfficeInterface {
    suspend fun getFreeId(msg: String): AirDropData?
    suspend fun register(msg: String): Boolean
    suspend fun queryAccountBalance(mmId: String): MMAccountBalance?
    suspend fun queryAccountProfile(mmId: String): MMAccountProfile?
    suspend fun queryAccountProps(mmId: String, props: List<String>): JsonObject?
    suspend fun syncContactsOfId(mmId: String): JsonElement?
    suspend fun getContactsOfId(mmId: String): JsonElement?
    suspend fun syncChatRecordsBetween(mmIdA: String, mmIdB: String): JsonElement?
    suspend fun getChatRecordsBetween(
        mmIdA: String,
        mmIdB: String,
        from: Int,
        to: Int
    ): List<MMSMessage>?

    suspend fun getLastestChatRecordsBetween(
        mmIdA: String,
        mmIdB: String,
    ): MMSMessage?

    suspend fun chat(
        mmIdA: String,
        mmIdB: String,
        AChatKey: String,
        content: String,
        lastMsg: MMSMessage?
    ): Boolean

    suspend fun chatCommmand(
        mmIdA: String,
        mmIdB: String,
        AChatKey: String,
        command: String,
        commandBody: ByteArray,
        lastMsg: MMSMessage?
    ): Boolean

    suspend fun chatAB(
        mmIdA: String,
        mmIdB: String,
        AChatKey: String,
        content: String,
        lastMsg: MMSMessage?
    ): Boolean

}
/*
   /**
     * 用户 id
     */
    private userId: String
    /**
     * 用户昵称
     */
    private nickname: string
    /**
     * 用户主密钥(私钥)
     */
    private masterKey: string
    /**
     * 用户聊天密钥(私钥)
     */
    private chatKey: string
    /**
     * 所在邮局, `mm.301`
     */
    private po: string
    /**
     * 所在邮局的压缩公钥哈希
     */
    private poCPKH: string
 */
//
//export interface MMAirDrop {
//    id: string; /* 账号 id, 与传统账号不同, id 仅为标识, 对 id 的所有权限控制通过用户的主密钥私钥完成 */
//    gas: number; /* 邮票数量 */
//    chatcpkh: string; /* 用户的聊天公钥 */
//}
//const chatSeed = `test-chat-${seed}`
//const masterSeed = `test-master-${seed}`
//const nick = `RandNick${seed}`

//object declaration is a concise way of creating a singleton class without the need to define a class and a companion object.
//object DataProviderManager {
//    private val providers = mutableListOf<DataProvider>()
//
//    // Registers a new data provider
//    fun registerDataProvider(provider: DataProvider) {
//        providers.add(provider)
//    }
//
//    // Retrieves all registered data providers
//    val allDataProviders: Collection<DataProvider>
//        get() = providers
//}
//class Singleton private constructor() {
//    companion object {
//        @Volatile
//        private var instance: Singleton? = null
//
//        fun getInstance() =
//            instance ?: synchronized(this) {
//                instance ?: Singleton().also { instance = it }
//            }
//    }
//
//    fun doSomething() = "Doing something"
//}

class POClient private constructor() : PostOfficeInterface {
    companion object {
        @Volatile
        private var instance: POClient? = null

        private var messageTcpIns: MessageTcp? = null
        fun getInstance(): POClient {
            if (instance == null) {
                //val poName = POConfig.poName
//                        val tcpIp = POConfig.tcpIp
//                        val tcpPort = POConfig.tcpPort
                messageTcpIns = MessageTcp.getInstance()
                instance = POClient()

            }
            return instance!!
        }
    }

    override suspend fun getFreeId(msg: String): AirDropData? {

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            val a = Json.decodeFromJsonElement<AirDropData>(aResult)
            println("get network reply Ok.....")
            return a
        }
        println("get network reply error, without airdrop....")
        return null

    }

    override suspend fun register(msg: String): Boolean {
        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            val a = aResult as JsonPrimitive
            if (a.isString && a.content == "committed") {
                println("get network reply, committed....")
                return true
            } else {
                println("get network reply error, not committed....${a.content}")
                return false
            }

        }
        println("get network reply error, not committed....")

        return false
    }

    /**
     *   "message": {
     *         "callee": "mm.chain",
     *         "caller": "guestof_1747576145165_1",
     *         "callid": 1,
     *         "proc": ["query_account", "mm.301.50669543"],
     *         "reply": true,
     *         "result": {
     *             "createtime": 1747533514447,
     *             "gas": 996,
     *             "master": "306abd685cfed25e52dc70f24e990e58c315f06a240429dc3e4965ed4a74a60f",
     *             "money": 0
     *         }
     *     },
     *     "type": "unit_message"
     * }
     */
    override suspend fun queryAccountBalance(mmId: String): MMAccountBalance? {
        val msg = messageQueryAccountBalance(mmId)

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            val a = Json.decodeFromJsonElement<MMAccountBalance>(aResult)
            println("get network reply Ok.....${a}")
            return a
        }
        println("get network reply error, without airdrop....")
        return null
    }

    override suspend fun queryAccountProfile(mmId: String): MMAccountProfile? {
        val msg = messageQueryAccountProfile(mmId)

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            val a = Json.decodeFromJsonElement<MMAccountProfile>(aResult)
            println("get network reply Ok.....")
            return a
        }
        println("get network reply error, without airdrop....")
        return null
    }

    override suspend fun queryAccountProps(mmId: String, props: List<String>): JsonObject? {
        val msg = messageQueryAccountProperty(mmId, props)

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            return aResult as JsonObject

        }
        println("get network reply error, not committed....")

        return null
    }

    /**
     *   "message": {
     *         "callee": "mm.301.chat",
     *         "caller": "guestof_1747578255294_1",
     *         "callid": 1,
     *         "proc": ["syncode", ["friends", "mm.301.37107148"]],
     *         "reply": true,
     *         "result": 1747578256936
     *     },
     *     "type": "unit_message"
     * }
     */
    override suspend fun syncContactsOfId(mmId: String): JsonElement? {
        val msg = messageSyncContactsOfId(mmId)

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            return aResult
        }
        println("get network reply error, not committed....")
        return null
    }

    /**
     * "result":[]
     */
    override suspend fun getContactsOfId(mmId: String): JsonElement? {
        val msg = messageGetContactsOfId(mmId)

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            return aResult
        }
        println("get network reply error, not committed....")

        return null
    }

    /**
     * "result":1747618635721
     */
    override suspend fun syncChatRecordsBetween(mmIdA: String, mmIdB: String): JsonElement? {
        val msg = messageSyncChatRecordsBetween(mmIdA, mmIdB)

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            return aResult
        }
        println("get network reply error, not committed....")

        return null
    }

    /**
     * [{
     *         "content": "hi bob",
     *         "receiver": "mm.301.37107148",
     *         "sign0": "AHaxZUteiEnho1TNGYBS7XAX8JFeOwYxPZ9CczHz9qLESryjQzdAu+ViH4RCWVklUIn3NTd3CqJGk8q+fTsIVzs=",
     *         "sign1": "ANSKJO35jp5R6rpijoRa9T5J2XTXqgeQeIc3zJ10gW2TPDEgXT27xmZ94qp2lmyRQrLEDQ0vcYiFu5VELwHxYBQ=",
     *         "sign2": "AG3ezp5zfRd5oXsJnOef9lxqrhshiNJlksmBgP10TH+rAFaeOK4WpgNAxYEQjRPqaVgH8vg8IY71kvq+c+RVuDA=",
     *         "sn": 0,
     *         "speaker": "mm.301.16777816",
     *         "time": 1747570587642
     *     }, {
     *         "content": "hi alice",
     *         "prevhash": "f98228c0a9123f1e2ef531077ce83fbc5e99990879c00b85de903194ab4e3162",
     *         "receiver": "mm.301.16777816",
     *         "sign0": "AZ9ltraTMgwjvCD7S4HOEF7qWI6F21qgSBKr8bRotnkrJJBACIu/kD3ltzxXj1eWcjkAaAOhr2DHu5RDjB0uHsc=",
     *         "sign1": "AWaDKWoB9I1ySLWiXKpeW8gbRstk5d87t2fMJpb+UeWpYmpHJLIORvs1/y8zrkNkkciI1gwYYFpBTaOhC8o6s4Y=",
     *         "sign2": "AdGgC8qJAzBpy0DK3jYNIWXOSwIqk+gAR7k36fuqkK25QwzlE2N1EygVPsncC6RRy6LV9nSBCWWU8PG7qqvT47g=",
     *         "sn": 1,
     *         "speaker": "mm.301.37107148",
     *         "time": 1747570588191
     *     }
     */
    override suspend fun getChatRecordsBetween(
        mmIdA: String,
        mmIdB: String,
        from: Int,
        to: Int
    ): List<MMSMessage>? {
        val msg = messageGetChatRecordsBetween(mmIdA, mmIdB, from, to)

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            val ret = when (aResult) {
                is JsonObject -> listOf(Json.decodeFromJsonElement<MMSMessage>(aResult))// treat this as JsonObject}
                is JsonArray -> Json.decodeFromJsonElement<List<MMSMessage>>(aResult)//treat this as JsonArray}
                else -> null //
            }
            return ret;
        }
        println("get network reply error, not committed....")

        return null
    }

    override suspend fun getLastestChatRecordsBetween(
        mmIdA: String,
        mmIdB: String,
    ): MMSMessage? {
        val msg = messageGetChatRecordsBetween(mmIdA, mmIdB, -1, 0)

        val aResult = messageTcpIns?.sendRequest(msg)
        if (aResult != null) {
            val ret = when (aResult) {
                is JsonObject -> Json.decodeFromJsonElement<MMSMessage>(aResult)// treat this as JsonObject}
                is JsonArray -> null//treat this as JsonArray}
                else -> null //
            }
            return ret;
        }
        println("get network reply error, not committed....")

        return null
    }


    override suspend fun chat(
        mmIdA: String,
        mmIdB: String,
        chatKeyA: String,
        content: String,
        lastMsg: MMSMessage?
    ): Boolean {
        println("chatKeyA is $chatKeyA")

        val msg = messageChat(mmIdA, chatKeyA, mmIdB, content, lastMsg)

        val aResult = messageTcpIns?.sendRequest(msg)
        println("chat send......receive......")
        if (aResult != null) {
            if (aResult is JsonPrimitive) {
                val b = Json.decodeFromJsonElement<Boolean>(aResult)
                return b
            }
            throw Exception("can't get the right result from POSTServer")

            return false
        }
        println("get network reply error, not committed....")
        throw Exception("can't get result from POSTServer")

        return false
    }

    override suspend fun chatCommmand(
        mmIdA: String,
        mmIdB: String,
        chatKeyA: String,
        command: String,
        commandBody: ByteArray,
        lastMsg: MMSMessage?
    ): Boolean {
        val lastMsgReal = if (lastMsg == null) {
            val aMsgLast = getLastestChatRecordsBetween(
                mmIdA,
                mmIdB
            )
            //println(aMsgLast)
             aMsgLast
        }else{
            lastMsg
        }
        println("get last msg; chatKeyA is ${chatKeyA}")
        val content = "#%${command}%#${commandBody.toHex()}"
        println("content length is ${content.length} , ${commandBody.size}")
        val ret = chat(mmIdA, mmIdB, chatKeyA, content, lastMsgReal)
        return ret
    }

    override suspend fun chatAB(
        mmIdA: String,
        mmIdB: String,
        chatKeyA: String,
        content: String,
        lastMsg: MMSMessage?
    ): Boolean {
        val lastMsgReal = if (lastMsg == null) {
            val aMsgLast = getLastestChatRecordsBetween(
                mmIdA,
                mmIdB
            )
            //println(aMsgLast)
            aMsgLast
        }else{
            lastMsg
        }
        println("get last msg; chatKeyA is ${chatKeyA}")
        println("content length is ${content.length}")
        val ret = chat(mmIdA, mmIdB, chatKeyA, content, lastMsgReal)
        return ret
    }

}
