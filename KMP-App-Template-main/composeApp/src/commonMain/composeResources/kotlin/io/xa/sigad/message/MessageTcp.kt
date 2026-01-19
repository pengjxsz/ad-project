package io.xa.sigad.message

import io.ktor.network.sockets.aSocket
// In commonMain
import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.Dispatchers // Or another suitable CoroutineContext
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds
import io.ktor.utils.io.readBuffer
import io.ktor.utils.io.readFully
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.readText
import io.ktor.utils.io.writeBuffer
import io.ktor.utils.io.writeFully
//The reason io.ktor.utils.io (specifically ByteReadChannel extensions)
// no longer has readIntLittleEndian in Ktor 3.x is because Ktor 3.x has migrated
// its low-level I/O primitives to use kotlinx-io
// instead of its own internal ktor-io implementation.
// implementation("org.jetbrains.kotlinx:kotlinx-io:0.3.0")
//  kotlinx-io has moved to kotlinx-io-core
//import io.ktor.utils.io.readIntLittleEndian
//import io.ktor.utils.io.writeIntLittleEndian
import kotlinx.io.Buffer
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.IO
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.timeout
import kotlinx.io.readIntLe
import kotlinx.io.writeIntLe
import kotlinx.serialization.json.JsonElement
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.milliseconds

val selectorManager = SelectorManager(Dispatchers.IO) // Or Dispatchers.IO, depending on your needs

@OptIn(ExperimentalUnsignedTypes::class)
fun stringToUByteArray(input: String): UByteArray {
    return input.encodeToByteArray().toUByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun createPacketBuffer(msg: String): ByteArray {
    val length = msg.length
    val a = stringToUByteArray(msg)
    val packetBuffer = UByteArray(4 + 4) // Allocate buffer
    packetBuffer.writeUInt32LE(length, 0) // Write length at position 0
    packetBuffer.writeUInt32LE(2, 4) // Write 2 at position 4

    val last = packetBuffer.plus(a)

    return last.toByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.writeUInt32LE(value: Int, offset: Int) {
    this[offset] = (value and 0xFF).toUByte()
    this[offset + 1] = ((value shr 8) and 0xFF).toUByte()
    this[offset + 2] = ((value shr 16) and 0xFF).toUByte()
    this[offset + 3] = ((value shr 24) and 0xFF).toUByte()
}


fun test1() = runBlocking {
    val selector = SelectorManager()
    val socket = aSocket(selector).tcp().connect("127.0.0.1", 4000)// Replace YOUR_PORT

    try {
        println("Connected successfully!")
        // Perform some communication if needed
    } catch (e: Exception) {
        println("Connection failed: ${e.message}")
    } finally {
        socket.close()
        selector.close()
    }
}

fun testConnect() {
    val a = MessageTcp.getInstance()
    runBlocking {
        val aJsonObject = a.sendRequest("msg")
        //val aAirDropData = aJsonString?.let { Json.decodeFromString<AirDropData>(it) };
        println("get network reply.....")

        return@runBlocking aJsonObject
    }
    println("can't get network reply.....")
}

fun testRegisterMessage() {
    val aAirDropData = AirDropData(
        "93115568",
        "2c4d5f94777da362bb5d58915c0d1caa7261b8c4474a89f6230875763acf244d",
        1000
    );
    // test-chat-3467 test-master-3467 93115568 2c4d5f94777da362bb5d58915c0d1caa7261b8c4474a89f6230875763acf244d
    val accountA = MMAccountData(3647, aAirDropData, POConfig.poName)
    println("accountA=====>")
    println(accountA.toString())
    println("accountA=====>chatkey, masterkey, chatcpkh")

    println(accountA.chatKey)
    println(accountA.masterKey)
    println(accountA.airDrop?.chatcpkh)

    val msgR = messageRegister(accountA)
    println("register message is====> ")
    println(msgR)
}
//fun main() {
//    //testConnect()
//    //testRegisterMessage()
//    val a="AB0F"
//    print(a.reverseHex())
//}


/**
 * Converts an Int to a 4-byte ByteArray in little-endian order.
 */
fun Int.toByteArrayLittleEndian(): ByteArray {
    return byteArrayOf(
        (this and 0xFF).toByte(),
        ((this ushr 8) and 0xFF).toByte(),
        ((this ushr 16) and 0xFF).toByte(),
        ((this ushr 24) and 0xFF).toByte()
    )
}


/**
 * Converts a 4-byte ByteArray segment (from offset) to an Int in little-endian order.
 * Assumes the byteArray has at least 4 bytes from the given offset.
 */
fun ByteArray.toIntLittleEndian(offset: Int = 0): Int {
    require(this.size >= offset + 4) { "ByteArray does not have enough bytes for an Int at offset $offset" }
    return (this[offset].toInt() and 0xFF) or
            ((this[offset + 1].toInt() and 0xFF) shl 8) or
            ((this[offset + 2].toInt() and 0xFF) shl 16) or
            ((this[offset + 3].toInt() and 0xFF) shl 24)
}


//class MessageTcp(private val serverAddress: String, private val serverPort: Int) {
class MessageTcp() {
    private val serverAddress: String
        get() = POConfig.tcpIp

    private val serverPort: Int
        get() = POConfig.tcpPort

    companion object {
        @Volatile
        private var instance: MessageTcp? = null

        @OptIn(InternalCoroutinesApi::class)
        //fun getInstance(serverAddress: String, serverPort: Int): MessageTcp {
        fun getInstance(): MessageTcp {
            if (instance == null) {
                instance = MessageTcp()
            }
            return instance!!
        }
    }


    suspend fun sendRequest(jsonRequest: String): JsonElement? {
        try {
            //withContext(Dispatchers.IO) {

            println(
                "[DEBUG] Sending UnitMessage: ${
                    jsonRequest.substring(
                        0,
                        if (jsonRequest.length > 1024) 1023 else jsonRequest.length
                    )
                }"
            ) // Log request

            //val response = sendTcpMessage(jsonRequest)
            val response = sendTcpMessageWithTimeoutRetry(jsonRequest)

            println(
                "[DEBUG] Received response: ${
                    response.substring(
                        0,
                        if (response.length > 1024) 1023 else response.length
                    )
                }}"
            ) // Log response
            //return@withContext response
            //response
            val aReplyObject = Json.parseToJsonElement(response) as JsonObject
            val aMessageObject = aReplyObject["message"] as JsonObject
            val aResult = aMessageObject["result"] as JsonElement
            //println("[DEBUG] Received Result: ${Json.encodeToString(aResult)}") // Log response
            println("[DEBUG] Received Result}") // Log response
            return aResult
            //}
        } catch (e: Exception) {
            println("[ERROR] Request failed: ${e.message}")
            val coroutineName = coroutineContext[CoroutineName]?.name ?: "Unnamed"
            val jobId = coroutineContext[Job]?.toString()?.substringAfter("Job@") ?: "N/A"
            println("[Coroutine:$coroutineName@$jobId]")
            throw Exception(e.message)
        }
    }


    /**
     * fun <T> Flow<T>.retry(
     *     retries: Long = Long.MAX_VALUE,
     *     predicate: suspend (cause: Throwable) -> Boolean = { true }
     * ): Flow<T>
     *
     * fun <T> Flow<T>.retry(
     *     retries: Long = Long.MAX_VALUE,
     *     onEachRetry: (cause: Throwable, attempt: Long) -> Unit = { _, _ -> },
     *     predicate: suspend (cause: Throwable) -> Boolean = { true }
     * ): Flow<T>
     */
    @OptIn(FlowPreview::class)
    suspend fun sendTcpMessageWithTimeoutRetry(
        data: String,
        timeoutMillis: Long = 5.seconds.inWholeMilliseconds,
        maxRetries: Int = 3,
        retryDelayMillis: Long = 1.seconds.inWholeMilliseconds
    ): String {
        return flow {
            println("connect $serverAddress $serverPort")
            val socket = aSocket(selectorManager)
                .tcp()
                .connect(serverAddress, serverPort)
            println("connected.....")
            val output = socket.openWriteChannel(autoFlush = true)
            val input = socket.openReadChannel()

            try {
                println("[DEBUG] Connected to server: $serverAddress:$serverPort")

                val bytesToWrite = data.encodeToByteArray()
                println("data length is ${data.length}, bytearray size is ${bytesToWrite.size}")


                //original: using kotin-io.utils
                //output.writewriteIntLittleEndian(bytesToWrite.size)
//                output.writeIntLittleEndian(0x2)

                //Now:
//                val buffer = Buffer()
//                buffer.writeIntLe(bytesToWrite.size);
//                buffer.writeIntLe(0x02);

//                val packet = buildPacket {
//                    //println("[Sender LE] Writing length ${bytesToWrite.size} (Little Endian) to Packet")
//                    this.writeIntLe(bytesToWrite.size) // writeIntLe is an extension on ByteWritePacket.Builder
//
//                    //println("[Sender LE] Writing fixed value 0x2 (Little Endian) to Packet")
//                    this.writeIntLe(0x2) // writeIntLe is an extension on ByteWritePacket.Builder
//
//                    // Write the actual message body directly into the same packet
//                    //println("[Sender LE] Writing ${bytesToWrite.size} bytes of message body to Packet")
//                    this.writeFully(bytesToWrite) // writeFully is also an extension on ByteWritePacket.Builder
//                }
//
//                println(packet.encodeBase64())
//                // Write the entire packet to the channel
//                println("[Sender LE] Writing packet content (${packet.remaining} bytes) to channel")
//
//                output.writePacket(packet)

                val lengthBytes = bytesToWrite.size.toByteArrayLittleEndian()
                val fixedValueBytes = (0x2).toByteArrayLittleEndian()

                println("[Sender V2] Writing length ${bytesToWrite.size} (Little Endian)")
                output.writeFully(lengthBytes, 0, lengthBytes.size)

                println("[Sender V2] Writing fixed value 0x2 (Little Endian)")
                output.writeFully(fixedValueBytes, 0, fixedValueBytes.size)
                output.writeFully(bytesToWrite, 0, bytesToWrite.size)


                //output.flush()
                //output.writeBuffer(buffer);
                //output.writeStringUtf8(data)
                //output.writeFully(bytesToWrite, 0, bytesToWrite.size)
                println("wait for response .....")

//                val length = input.readIntLittleEndian()
//                input.readIntLittleEndian()


//                val numBytesToRead = 8 // For an Int
//                //val rBuffer = input.readBuffer(numBytesToRead) // R
//                val headerPacket = input.readPacket(numBytesToRead) // R
//                val length = headerPacket.readIntLe()

                val intSizeBytes = 4 // An Int is 4 bytes

                println("[Receiver V2] Reading length (Little Endian)")
                val lengthByteArray = ByteArray(intSizeBytes)
                input.readFully(lengthByteArray)
                val length = lengthByteArray.toIntLittleEndian()
                println("[Receiver V2] Read length: $length")

                println("[Receiver V2] Reading second fixed value (Little Endian)")
                val fixedValueByteArray = ByteArray(intSizeBytes)
                input.readFully(fixedValueByteArray)
                val fixedValue = fixedValueByteArray.toIntLittleEndian()
                println("[Receiver V2] Read fixed value: $fixedValue")

                val messageBody = ByteArray(length)

                input.readFully(messageBody)

                //val response = input.readRemaining(length.toLong()).readText()
                val response = messageBody.decodeToString()
                println("response is ${response.length} $response")
                emit(response)
            } finally {
                println("[DEBUG] Closing socket")
                socket.close()
            }
        }
            .timeout(timeoutMillis.milliseconds)
            .retry(maxRetries.toLong()) { e ->
                delay(retryDelayMillis)
                true // Indicate retry
            }
            .onEach { attempt -> // This will be executed on each successful emission (not retry)
                // You might not need anything here for retries logging
            }
            .catch { e ->
                val errMsg = ("[ERROR] Network failure after $maxRetries retries: ${e.message}")
                throw Exception(errMsg)
            }
            .single()
    }


    @OptIn(ExperimentalUnsignedTypes::class)
    suspend fun sendTcpMessage(data: String): String {
        return try {
            println("connect ${serverAddress} ${serverPort}")
            val socket = aSocket(selectorManager)
                .tcp()
                .connect(serverAddress, serverPort)
            println("connected.....")
            val output = socket.openWriteChannel(autoFlush = true)
            val input = socket.openReadChannel()
            //println("[DEBUG] Sending UnitMessage: ${data}") // Log request

            println("[DEBUG] Connected to server: $serverAddress:$serverPort") // Log connection
//            output.writeIntLittleEndian(data.length)
//            output.writeIntLittleEndian(0x2)
//            output.writeStringUtf8(data)

            val buffer = Buffer()
            buffer.writeIntLe(data.length);
            buffer.writeIntLe(0x02);
            output.writeBuffer(buffer);
            //output.writeStringUtf8(data)
            val bytesToWrite = data.encodeToByteArray()

            output.writeFully(bytesToWrite, 0, bytesToWrite.size)
            println("wait for response .....")


//            val toSend = createPacketBuffer(data)
//            output.writeFully(toSend, 0, toSend.size)
            println("wait for response .....")

//            val length = input.readIntLittleEndian();
//            input.readIntLittleEndian()
            val numBytesToRead = 8 // For an Int
            val rBuffer = input.readBuffer(numBytesToRead) // R
            val length = rBuffer.readIntLe()
            val response = input.readRemaining(length.toLong()).readText()
            //val response = input.readRemaining().readText()
            println("response is ${response.length} ${response}")
            println("[DEBUG] Closing socket") // Log socket closing
            socket.close()

            response
        } catch (e: Exception) {
            val errMsg = ("[ERROR] Network failure: ${e.message}")
            throw Exception(errMsg)

        }
    }
}


/*

class message_tcp(private val serverAddress: String, private val serverPort: Int) {

    private val timeout: Duration = 5.seconds // Set timeout for socket communication

    suspend fun sendRequest(proc: String, payload: Map<String, Any>? = null): JsonObject? {
        return try {
            withContext(Dispatchers.IO) {
                val requestMessage = mapOf(
                    "message" to mapOf(
                        "callee" to "mm.301.register",
                        "caller" to "guestof_${System.currentTimeMillis()}_1",
                        "callid" to 1,
                        "proc" to listOf(proc),
                        "reply" to false,
                        "result" to payload
                    ),
                    "type" to "unit_message"
                )

                val jsonRequest = Json.encodeToJsonElement(requestMessage).toString()

                println("[DEBUG] Sending request: $jsonRequest") // Logging request

                val response = sendTcpMessage(jsonRequest)

                println("[DEBUG] Received response: $response") // Logging response

                Json.parseToJsonElement(response) as JsonObject
            }
        } catch (e: Exception) {
            println("[ERROR] Request failed: ${e.message}")
            null
        }
    }

    private suspend fun sendTcpMessage(data: String): String {
        return try {
            val socket = aSocket(ActorSelectorManager(Dispatchers.IO))
                .tcp()
                .connect(serverAddress, serverPort)

            val output = socket.openWriteChannel(autoFlush = true)
            val input = socket.openReadChannel()

            println("[DEBUG] Connected to server: $serverAddress:$serverPort") // Connection log

            output.writeStringUtf8(data)

            val response = input.readRemaining().readText()

            println("[DEBUG] Closing socket") // Log socket closing
            withContext(Dispatchers.IO) {
                socket.close()
            }

            response
        } catch (e: Exception) {
            println("[ERROR] Network failure: ${e.message}")
            ""
        }
    }
}
*/