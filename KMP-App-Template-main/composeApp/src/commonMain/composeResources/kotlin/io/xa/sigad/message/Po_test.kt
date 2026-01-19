package io.xa.sigad.message

import io.xa.sigad.crop.picker.getPlatform
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.Job
import kotlin.random.Random

class Po_test() {
    //66666======seed, aidrop:  test-chat-7149 test-master-7149 16777816 78821e127bf5efdead9698e364b92dff65a76570663a5b50edcdfb00bee81b87
    var accountRegisteredA = MMAccountData(
        7149,
        AirDropData(
            "16777816",
            "78821e127bf5efdead9698e364b92dff65a76570663a5b50edcdfb00bee81b87",
            1000
        ), POConfig.poName
    );

    //66666======seed, aidrop:  test-chat-6188 test-master-6188 37107148 ba8e7a0af8f7471d2ae217f7086e368445d066f74f183eed2830cfca64c5eaaa
    var accountRegisteredB = MMAccountData(
        6188,
        AirDropData(
            "37107148",
            "ba8e7a0af8f7471d2ae217f7086e368445d066f74f183eed2830cfca64c5eaaa",
            1000
        ), POConfig.poName
    );
    var accountA = generateUnRegisteredAcount()

    val chatMap : Map<String, MMAccountData>
        get(){
            val platform = getPlatform()
            if (platform.name.indexOf("Android") != -1){
                return mapOf("Myself" to accountRegisteredA, "Friend" to accountRegisteredB)
            }else{
                return mapOf("Myself" to accountRegisteredB, "Friend" to accountRegisteredA)
            }
    }

    val chatA : MMAccountData
        get(){
            return chatMap["Myself"]!!
        }
    val chatB : MMAccountData
        get(){
            return chatMap["Friend"]!!
        }
    //var accountB = generateUnRegisteredAcount()
//    val module = SerializersModule {
//        polymorphic(Any::class) {
//            subclass(String::class)
//            subclass(Int::class)
//            subclass(Boolean::class)
//            subclass(List::class) // Corrected: List<*> to List<Any?>
//            subclass(Map::class)  // Corrected: Map<*, *> to Map<String, Any?>
//        }
//    }
//
//    // Correct way to configure Json to use the SerializersModule:
//    val myJson = Json { serializersModule = module }

    suspend fun test_register44() {

        //9615 34536000 0b237e8d7a45410596697afa4b4399ee14f0ea70335c6b21fa57239a00e010d8
        val aAirDropData = AirDropData(
            "50669543",
            "6295401c6894fcce1321059ade789572b7a1e3373e5752dea998f3469dc78bd0",
            1000
        );
        // test-chat-3467 test-master-3467 93115568 2c4d5f94777da362bb5d58915c0d1caa7261b8c4474a89f6230875763acf244d
        //66666======seed, aidrop:  test-chat-7281 test-master-7281 85182184 fd34978019bd36a619b9fde0661b7934da3bf6b20d4ba1cf8fcb9827f5482928

        val accountA = MMAccountData(7384, aAirDropData, POConfig.poName)
        println("accountA=====>")
        println(accountA.toString())
        println("accountA=====>chatkey, masterkey, chatcpkh")

        println(accountA.chatKey)
        println(accountA.masterKey)
        println(accountA.airDrop?.chatcpkh)

        val msgR = messageRegister(accountA, 1747533512938)
        println("register message is====> ")
        println(msgR)

    }

    suspend fun test_register22() {
        val masterKey = "885ea29eeb1ea63800a977be0c9ce7d1d498a67c6ed2682ccf9094f0103d7b10"
        val str =
            "{\"actions\":[{\"act\":\"register\",\"domain\":\"mm.301\",\"param\":[\"50669543\",\"306abd685cfed25e52dc70f24e990e58c315f06a240429dc3e4965ed4a74a60f\"]},{\"act\":\"givegas\",\"domain\":\"mm.301\",\"param\":[\"mm.301.50669543\",1000]},{\"act\":\"setprop\",\"domain\":\"mm.301.50669543\",\"param\":[\"alias\",\"RandNick7384\"]},{\"act\":\"setactor\",\"domain\":\"mm.301.50669543\",\"param\":[\"chat\",\"a6f3942ddb588b0ba9589cbb66cb84d4510172a38fe28147aad6f47efb6a75fe\"]},{\"act\":\"setactor\",\"domain\":\"mm.301.50669543\",\"param\":[\"chat_office\",\"6295401c6894fcce1321059ade789572b7a1e3373e5752dea998f3469dc78bd0\"]},{\"act\":\"setprop\",\"domain\":\"mm.301.50669543\",\"param\":[\"ethaddr\",\"F2ec4a773ef90c58d98ea734c0eBDB538519b988\"]}],\"time\":1747533512938}"
        val msgSingature = masterSignCompactB64(
            str,
            masterKey
        )
        println(msgSingature)
    }

    suspend fun test_register() {
        val poclient = POClient.Companion.getInstance()
        println(accountA.toString())
        val msgA = messageGetFreeId()
        println("=======>1")
//        println(msgObjA.toString())
//        val msgA = Json.encodeToString(msgObjA).toString()
//        println("=======>2")

        val airDropDataA = poclient.getFreeId(msgA)
        println("=======>3")

        accountA.airDrop = airDropDataA;
        val msgR = messageRegister(accountA)
//        println("=======>4")

//        val msgR = Json.encodeToString(msgObjR).toString()
        println(msgR)

        val registered = poclient.register(msgR)
        println("=======>6")

    }

    suspend fun test_account() {

        val aAirDropData = AirDropData(
            "50669543",
            "6295401c6894fcce1321059ade789572b7a1e3373e5752dea998f3469dc78bd0",
            1000
        );
        val accountA = MMAccountData(7384, aAirDropData, POConfig.poName)

        val poclient = POClient.Companion.getInstance()

        val r = poclient.queryAccountBalance(accountA.userId)
        println(r)

        val r1 = poclient.queryAccountProfile(accountA.userId)
        println(r1)

        val r2 = poclient.queryAccountProps(accountA.userId, listOf("alias"))
        println(r2)
    }

    suspend fun test_chat() {
        //storeUserCredentials(accountRegisteredA.userId, "123456")
        //val p=  retrieveUserPassword(accountRegisteredA.userId)
        //println("stored p: $p")
        val aAirDropData = AirDropData(
            "",//""37107148",
            "6295401c6894fcce1321059ade789572b7a1e3373e5752dea998f3469dc78bd0",
            1000
        );
        val accountA = MMAccountData(7384, aAirDropData, POConfig.poName)

        val poclient = POClient.Companion.getInstance()

        val r = poclient.syncContactsOfId(accountA.userId)
        var r2 = poclient.getContactsOfId(accountA.userId)


        poclient.getChatRecordsBetween("mm.301.37107148", "mm.301.16777816", -1, 0)

        poclient.syncChatRecordsBetween(accountRegisteredB.userId, accountRegisteredA.userId)
        poclient.syncContactsOfId(accountRegisteredB.userId)
        val msgList = poclient.getChatRecordsBetween(
            accountRegisteredB.userId,
            accountRegisteredA.userId,
            -1,
            0
        )
        val lastMsg = msgList?.get(0)
        println(msgList?.get(0))
        //poclient.chat(accountRegisteredB.userId, accountRegisteredA.userId, accountRegisteredB.chatKey, "Hello world!", lastMsg)
        val commandBody = ByteArray(1024 * 10)
        Random.nextBytes(commandBody)

        poclient.chatCommmand(
            accountRegisteredB.userId,
            accountRegisteredA.userId,
            accountRegisteredB.chatKey,
            "PROJECT",
            commandBody,
            lastMsg
        )
        val aMsg = poclient.getLastestChatRecordsBetween(
            accountRegisteredA.userId,
            accountRegisteredB.userId
        )
        println(aMsg)
    }

    suspend fun test_PhotoRecv(): String? {

        println("receive.....")
        val poclient = POClient.Companion.getInstance()
        val A = chatA
        val B = chatB
        val aMsg = poclient.getLastestChatRecordsBetween(
            A.userId,
            B.userId,
            )
        println("received.....")
        return aMsg?.content
    }
    suspend fun test_PhotoRecv1(): ByteArray? {

        println("receive.....")
        val poclient = POClient.Companion.getInstance()
        val A = chatA
        val B = chatB
        val aMsg = poclient.getLastestChatRecordsBetween(
            A.userId,
            B.userId,
        )
        println("received.....")
        val command = aMsg?.content?.substring(0,11)
        if (command == null)
            return null;
        else if (command == "#%PROJECT%#"){
            println("receive a projection command")

            val imageHex = aMsg?.content?.substring(command.length)
            val imageByteArray = imageHex?.hexToByteArray()
            return imageByteArray
        }else{
            return null
        }
    }


    suspend fun test_PhotoSend(content: ByteArray): Boolean {

        val coroutineName = coroutineContext[CoroutineName]?.name ?: "Unnamed"
        val jobId = coroutineContext[Job]?.toString()?.substringAfter("Job@") ?: "N/A"
        println("1[Coroutine:$coroutineName@$jobId]")

        val poclient = POClient.Companion.getInstance()
        println("preparing to send command project")
        val A = chatA
        val B = chatB
        val ret = poclient.chatCommmand(
            A.userId,
            B.userId,
            A.chatKey,
            "PROJECT",
            content,
            null
        )
        return ret;
    }

    suspend fun test_ABChat(content: String): Boolean {
        val poclient = POClient.Companion.getInstance()
        println("preparing to send command project")
        val A = chatA
        val B = chatB
        val ret = poclient.chatAB(
            A.userId,
            B.userId,
            A.chatKey,
            content,
            null
        )
        return ret;
    }

}

