package io.xa.sigad.message

import kotlinx.atomicfu.AtomicInt
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.atomicfu.atomic // Import the atomicfu functions


@Serializable
data class WrappedItem(val type: String, val data: JsonElement)

//class AtomicCounter {
//    private val _counter = atomic(0)
//
//    fun increment() {
//        _counter.incrementAndGet() // Atomically increments and returns the new value
//    }
//
//    fun get(): Int = _counter.value
//}

class PostOfficeMessage(
    val serviceName: String,
    val methodName: String,
    val bPOPrefix: Boolean = true
) {

    val callee: String
        get() = if (this.bPOPrefix) {
            "${POConfig.poName}.${this.serviceName}"
        } else {
            this.serviceName
        }
    val caller: String
        get() {
            val currentMoment: Instant = Clock.System.now()

            return "guestof_${currentMoment.toEpochMilliseconds()}_1"
        }
    val callid: Int
        get() {
            return requestId.incrementAndGet()
        }
    val message_type: String = "unit_message"

    //var proc: MutableList<WrappedItem> = mutableListOf(methodName);
    //val proc: MutableList<WrappedItem> = mutableListOf(
    //val proc: MutableList<JsonElement> = mutableListOf(JsonPrimitive(methodName))
    //       JsonPrimitive(methodName)
//        WrappedItem("Array", Json.encodeToJsonElement(listOf("item1", "item2"))),
//        WrappedItem("Object", Json.encodeToJsonElement(mapOf("key" to "value")))
    //   )

    var proc: JsonArray = buildJsonArray { add(JsonPrimitive(methodName)) }

    companion object {
        var requestId: AtomicInt = atomic(0);
        fun create(
            serviceName: String,
            methodName: String,
            bPOPrefix: Boolean = true
        ): PostOfficeMessage {
            return PostOfficeMessage(serviceName, methodName, bPOPrefix)
        }
    }

    fun addProcContent(param: JsonElement) {
        val updatedArray = JsonArray(proc + param)
        proc = updatedArray
    }

    fun getMessageString(): String {
        val aMessageContent = buildJsonObject {
            put("callee", JsonPrimitive(callee)) // "callee" to callee,
            put("caller", JsonPrimitive(caller)) //"caller" to caller,
            put("callid", JsonPrimitive(callid))//"callid" to callid,
            put("proc", proc)// "proc" to proc
        }
        val aMessage = buildJsonObject {
            put("message", aMessageContent) //"message" to aMessageContent,
            put("type", JsonPrimitive(message_type)) //"type" to message_type
        }
        return Json.encodeToString(aMessage);
    }

}

@OptIn(ExperimentalSerializationApi::class)
fun messageGetFreeId(): String {
    val factoryMsg = PostOfficeMessage.create(serviceName = "register", methodName = "get_freeid");
    factoryMsg.addProcContent(JsonPrimitive(null))
    return factoryMsg.getMessageString();
}

//
//prepareRegisterPayload(
//chatSeed: string, masterSeed: string, nickname: string, po: string, airDrop: MMAirDrop,
//ethAddress: string
//): TxPayload {
//    const vault = new KeyVault({ chatSeed, masterSeed, po, nickname, airDrop })
//    this.vault = vault
//
//    const { id, chatcpkh } = airDrop
//    const param: TxPayload = {
//        actions: [
//        {
//            act: 'register',
//            domain: po,
//            param: [id, vault.masterCPKHash()],
//        },
//        {
//            act: 'givegas',
//            domain: po,
//            param: [vault.id(), 1000],
//        },
//        {
//            act: 'setprop',
//            domain: vault.id(),
//            param: ['alias', nickname],
//        },
//        {
//            act: 'setactor',
//            domain: vault.id(),
//            param: ['chat', vault.chatCPKHash()],
//        },
//        {
//            act: 'setactor',
//            domain: vault.id(),
//            param: ['chat_office', chatcpkh],
//        },
//        {
//            act: "setprop",
//            domain: vault.id(),
//            param: ["ethaddr", checksumAddress(ethAddress)],
//        }
//        ],
//        time: Date.now(),
//    }
//
//    param.signature = vault.masterSignCompactB64(param)
//    console.log(JSON.stringify(param, null, 4))
//    return param
//}

fun messageRegister(account: MMAccountData, timeStamp: Long = 0): String {
    if (!account.airDroped)
        throw IllegalArgumentException("account without airdroped id")

    val factoryMsg = PostOfficeMessage.create(serviceName = "register", methodName = "register");

    val currentMoment: Instant = Clock.System.now()
    val currentTs = if (timeStamp == 0L) {
        currentMoment.toEpochMilliseconds()
    } else {
        timeStamp
    }

//    {
//        actions: [
//        {
//            act: 'register',
//            domain: po,
//            param: [id, vault.masterCPKHash()],
//        },
//        {
//            act: 'givegas',
//            domain: po,
//            param: [vault.id(), 1000],
//        },
//        {
//            act: 'setprop',
//            domain: vault.id(),
//            param: ['alias', nickname],
//        },
//        {
//            act: 'setactor',
//            domain: vault.id(),
//            param: ['chat', vault.chatCPKHash()],
//        },
//        {
//            act: 'setactor',
//            domain: vault.id(),
//            param: ['chat_office', chatcpkh],
//        },
//        {
//            act: "setprop",
//            domain: vault.id(),
//            param: ["ethaddr", checksumAddress(ethAddress)],
//        }
//        ],
//        time: Date.now(),
//    }
//
    val masterPubKeyHash = secp256k1GetPublicKeyFromPrivate(account.masterKey)
    val chatPubKeyHash = secp256k1GetPublicKeyFromPrivate(account.chatKey)
    val tranformedEthaddr = checksumAddress(account.ethAddress)
//    val actRegisterMap = mapOf(
//        "act" to "register",
//        "domain" to account.po,
//        "param" to listOf(account.airDrop.id, masterPubKeyHash))


//    val acttionList1 = listOf(
//        buildMap {
//            put("act" , "register")
//            put("domain" , account.po)
//            put("param" ,
//                listOf(account.airDrop?.id, masterPubKeyHash)
//        },
//
//        mapOf(
//            "act" to "givagas",
//            "domain" to account.po,
//            "param" to listOf(account.airDrop?.id, 1000)
//        ),
//
//        mapOf(
//            "act" to "setprop",
//            "domain" to account.userId,
//            "param" to listOf("alias", account.nickname)
//        ),
//
//        mapOf(
//            "act" to "setactor",
//            "domain" to account.userId,
//            "param" to listOf("chat", chatPubKeyHash)
//        ),
//
//        mapOf(
//            "act" to "ethaddr",
//            "domain" to account.userId,
//            "param" to listOf(account.airDrop?.id, tranformedEthaddr)
//
//        )
//    )

    val actionList = buildJsonArray {
        add(buildJsonObject {
            put("act", JsonPrimitive("register"))
            put("domain", JsonPrimitive(account.po))
            put(
                "param",
                buildJsonArray {
                    add(JsonPrimitive(account.airDrop?.id))
                    add(JsonPrimitive(masterPubKeyHash))
                })
        })
        add(
            buildJsonObject {
                put("act", JsonPrimitive("givegas"))
                put("domain", JsonPrimitive(account.po))
                put("param", buildJsonArray {
                    add(JsonPrimitive(account.userId))
                    add(JsonPrimitive(1000))
                })
            }
        )
        add(
            buildJsonObject {
                put("act", JsonPrimitive("setprop"))
                put("domain", JsonPrimitive(account.userId))
                put("param", buildJsonArray {
                    add(JsonPrimitive("alias"))
                    add(JsonPrimitive(account.nickname))
                })
            }
        )


        add(
            buildJsonObject {
                put("act", JsonPrimitive("setactor"))
                put("domain", JsonPrimitive(account.userId))
                put("param", buildJsonArray {
                    add(JsonPrimitive("chat"))
                    add(JsonPrimitive(chatPubKeyHash))
                })
            }
        )

        add(
            buildJsonObject {
                put("act", JsonPrimitive("setactor"))
                put("domain", JsonPrimitive(account.userId))
                put("param", buildJsonArray {
                    add(JsonPrimitive("chat_office"))
                    add(JsonPrimitive(account.airDrop?.chatcpkh))
                })
            }
        )

        add(
            buildJsonObject {
                put("act", JsonPrimitive("setprop"))
                put("domain", JsonPrimitive(account.userId))
                put("param", buildJsonArray {
                    add(JsonPrimitive("ethaddr"))
                    add(JsonPrimitive(tranformedEthaddr))
                })
            }
        )
    }
//
//    var param = mutableMapOf<String, Any>(
//        "actions" to acttionList,
//        "time" to currentTs
//    )

    var paramsToSign = buildJsonObject {
        put("actions", actionList)
        put("time", JsonPrimitive(currentTs))
        //put("time", JsonPrimitive(1747533512919))

    }
    val paramstoSignJson = Json.encodeToString(paramsToSign)
    val msgSingature = masterSignCompactB64(
        paramstoSignJson,
        account.masterKey
    )


    //param.signature = vault.masterSignCompactB64(param)

    val lastParams = buildJsonObject {
        put("actions", actionList)
        put("signature", JsonPrimitive(msgSingature))
        put("time", JsonPrimitive(currentTs))
        //put("time", JsonPrimitive(1747533512919))
    }
    //val sortedLastParams= sortJsonElement(lastParams)
    factoryMsg.addProcContent(lastParams)

    val msg = factoryMsg.getMessageString();
    println(msg)
    return msg;
}


fun messageQueryAccountBalance(accountId: String): String {
    val factoryMsg =
        PostOfficeMessage.create(serviceName = "mm.chain", methodName = "query_account", false);
    factoryMsg.addProcContent(JsonPrimitive(accountId))
    return factoryMsg.getMessageString();
}


fun messageQueryAccountProfile(accountId: String): String {
    val factoryMsg =
        PostOfficeMessage.create(
            serviceName = "mm.chain",
            methodName = "query_account_info",
            false
        );
    factoryMsg.addProcContent(JsonPrimitive(accountId))
    return factoryMsg.getMessageString();
}

fun messageQueryAccountProperty(accountId: String, props: List<String>): String {
    val factoryMsg =
        PostOfficeMessage.create(
            serviceName = "mm.chain",
            methodName = "query_account_props",
            false
        );
    val aArray = buildJsonArray {
        add(JsonPrimitive(accountId))
        for (prop in props) {
            add(JsonPrimitive(prop))
        }
    }
    factoryMsg.addProcContent(aArray)
    return factoryMsg.getMessageString();
}

/**
 * 获取用户联系人最新的状态码
 */
fun messageSyncContactsOfId(accountId: String): String {
    val factoryMsg = PostOfficeMessage.create(serviceName = "chat", methodName = "syncode");
    //val listParam = listOf("friends", accountId)
    val listJsonElement = buildJsonArray {
        add(JsonPrimitive("friends"))
        add(JsonPrimitive(accountId))
    }
    //factoryMsg.addProcContent(Json.encodeToJsonElement(ListSerializer(listOf("item1", "item2"))))
    factoryMsg.addProcContent(listJsonElement)
    return factoryMsg.getMessageString();
}


/**
 * 获取用户所有联系人
 */
fun messageGetContactsOfId(accountId: String): String {
    val factoryMsg = PostOfficeMessage.create(serviceName = "chat", methodName = "get_friends");
    //val listParam = listOf(accountId)
    val listJsonElement = buildJsonArray {
        add(JsonPrimitive(accountId))
    }
    factoryMsg.addProcContent(listJsonElement)
    return factoryMsg.getMessageString();
}

/**
 * 获取两个用户间最新的状态码
 */
fun messageSyncChatRecordsBetween(accountIdA: String, accountIdB: String): String {
    val factoryMsg = PostOfficeMessage.create(serviceName = "chat", methodName = "syncode");
    //val listParam = listOf("chat", accountIdA, accountIdB)
    val listJsonElement = buildJsonArray {
        add(JsonPrimitive("chat"))
        add(JsonPrimitive(accountIdA))
        add(JsonPrimitive(accountIdB))
    }
    factoryMsg.addProcContent(listJsonElement)
    return factoryMsg.getMessageString();
}

/**
 * 获取两个用户间的消息
 */
fun messageGetChatRecordsBetween(
    accountIdA: String,
    accountIdB: String,
    from: Int,
    to: Int
): String {
    val factoryMsg = PostOfficeMessage.create(serviceName = "chat", methodName = "chat_record");
    //var listParam = mutableListOf("chat", accountIdA, accountIdB, from)
    var toNew = 0
    if (from > -1) {
        if (to > from) toNew = to
        else toNew = (from + 1)
    }

    val listJsonElement = buildJsonArray {
        add(JsonPrimitive(accountIdA))
        add(JsonPrimitive(accountIdB))
        add(JsonPrimitive(from))
        if (toNew > 0)
            add(JsonPrimitive(toNew))
    }

    factoryMsg.addProcContent(listJsonElement)
    return factoryMsg.getMessageString();
}

private fun buildChatJsonObject(
    speaker: String,
    receiver: String,
    content: String,
    prevhash: String?,
    sn: Int,
    time: Long,
    sign0: String?
): JsonObject {
    val aJsonObject = buildJsonObject {
        put("content", JsonPrimitive(content))

        if (prevhash != null)
            put("prevhash", JsonPrimitive(prevhash))
        put("receiver", JsonPrimitive(receiver))
        if (sign0 != null)
            put("sign0", JsonPrimitive(sign0))

        put("sn", JsonPrimitive(sn))
        put("speaker", JsonPrimitive(speaker))
        put("time", JsonPrimitive(time))
    }
    return aJsonObject
}

fun messageChat(
    speaker: String, speakChatKey: String,
    receiver: String, content: String, lastMsg: MMSMessage?
): String {
    val prevhash = if (lastMsg == null)
        null
    else {
//        val preMsgJsonObject = buildJsonObject {
//            put("content", JsonPrimitive(lastMsg.content))
//            put("prevhash", JsonPrimitive(lastMsg.prevhash))
//            put("receiver", JsonPrimitive(lastMsg.receiver))
//            put("sn", JsonPrimitive(lastMsg.sn))
//            put("speaker", JsonPrimitive(lastMsg.speaker))
//            put("time", JsonPrimitive(lastMsg.time))
//        }
        val preMsgJsonObject = buildChatJsonObject(
            lastMsg.speaker,
            lastMsg.receiver,
            lastMsg.content,
            lastMsg.prevhash,
            lastMsg.sn,
            lastMsg.time,
            null
        )
        val preMsg = Json.encodeToString(preMsgJsonObject)
        val hash = POHash(preMsg)
        hash
    }

    val nexsn = if (lastMsg == null) 0 else (lastMsg.sn + 1)
    val currentMonment = Clock.System.now().toEpochMilliseconds()


//    val  contentToSignJSON = buildJsonObject {
//         put("content", JsonPrimitive(content))
//         put("prevhash", JsonPrimitive(prevhash))
//         put("receiver", JsonPrimitive(receiver))
//         put("sn", JsonPrimitive(nexsn))
//         put("speaker", JsonPrimitive(speaker))
//         put("time", JsonPrimitive(currentMonment))
//     }
    val contentToSignJSON = buildChatJsonObject(
        speaker,
        receiver,
        content,
        prevhash,
        nexsn,
        currentMonment,
        null
    )
    val contentToSign = Json.encodeToString(contentToSignJSON)
    val sign0 = masterSignCompactB64(contentToSign, speakChatKey)


//        "sign0": "AZ9ltraTMgwjvCD7S4HOEF7qWI6F21qgSBKr8bRotnkrJJBACIu/kD3ltzxXj1eWcjkAaAOhr2DHu5RDjB0uHsc=",
//    val testJson = buildJsonObject {
//        put("content", JsonPrimitive("hi alice"))
//        put("prevhash",JsonPrimitive("f98228c0a9123f1e2ef531077ce83fbc5e99990879c00b85de903194ab4e3162"))
//        put( "receiver",JsonPrimitive("mm.301.16777816"))
//        put("sn",JsonPrimitive(1))
//        put("speaker",JsonPrimitive("mm.301.37107148"))
//        put("time",JsonPrimitive(1747570588191))
//    }
//        val sign1 = masterSignCompactB64(Json.encodeToString(testJson), speakChatKey)
//        println("sign1 $sign1")

        val listJsonElement = buildChatJsonObject(
        speaker,
        receiver,
        content,
        prevhash,
        nexsn,
        currentMonment,
        sign0
    )

    val factoryMsg = PostOfficeMessage.create(serviceName = "chat", methodName = "chat1");

    factoryMsg.addProcContent(listJsonElement)
    return factoryMsg.getMessageString();


}


/*
  async sendMsg(receiver: string, content: string, lastMsg?: MMMsg): Promise<{
    result: boolean; sn: number; time: number
  }> {

    const nextSn = lastMsg ? lastMsg.sn + 1 : 0
    const prevhash = lastMsg
      ? reverseHex(
        bytesToHex(
          sha256(
            JSON.stringify(
              sortJSON({
                sn: lastMsg.sn,
                speaker: lastMsg.speaker,
                receiver: lastMsg.receiver,
                content: lastMsg.content,
                time: lastMsg.time,
                prevhash: lastMsg.prevhash,
              })
            )
          )
        )
      )
      : undefined

    const params2sign = {
      sn: nextSn,
      time: Date.now(),
      content,
      prevhash,
      speaker: this.vault!.id(),
      receiver,
    }

    const chatMsg: MMChat1Msg = {
      ...params2sign,
      sign0: mmChatSignCompact(sha256(JSON.stringify(sortJSON(params2sign))), this.vault!.getChatKey(), 'base64'),
    }
    const result = (await this.getChatClient().call('chat1', chatMsg)) as boolean
    return { result, sn: nextSn, time: params2sign.time }
  }
}
*/
