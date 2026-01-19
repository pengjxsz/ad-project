package io.xa.sigad.data

// Import necessary libraries
import io.github.vinceglb.filekit.FileKit // Corrected import
import io.github.vinceglb.filekit.PlatformFile // Corrected import
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.resolve
import io.github.vinceglb.filekit.writeString
import io.xa.sigad.message.WebAccount
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.concurrent.Volatile


/**
 * Demonstrates saving a JSON string to a file and reading it back using FileKit,
 * with a fixed file path instead of a file dialog.
 */
object ConfigFileManager {

    private val json = Json { prettyPrint = true;encodeDefaults = true} // Configure JSON serializer for pretty printing
    private val configFileName = "sigad_config.json" // The fixed name of the configuration file

//    val masterPrivK=""
//    val masterPK=""
//    val chatPrivK=""
//    val chatPK=""
//    val user_id=""
//    val deviceChip=""
//    val devicePK=""

//    "设备ID" to "ABCD-1234-EFGH",
//    "设备公钥" to "0xAbCdE...FfGg",
//    "用户ID" to "User-998877",
//    "昵称" to "KMP Dev",
//    "屏幕大小" to "1080 * 2400",
//    "色彩数" to "16777216" /
    // 当前配置（可能未初始化）
    @Volatile
    var config: WebAccount = WebAccount()
        private set

    //Reserved for local post test/usage
//    var ip = "127.0.0.1"
//    var port = 400
//
    // Default JSON content for initialization
//    private val defaultJsonContent = buildJsonObject {
//        put("ip", ip) //"127.0.0.1")
//        put("port", port) //400)
//    }.toString()

    init {
        // This block runs when the singleton object is first accessed.
        // It's important to use runBlocking here if this initialization needs to be synchronous
        // before other parts of the app use the ConfigFileManager.
        // In a real Composable app, you might trigger this from a suitable CoroutineScope.
        runBlocking {
            try {
                val file = getConfigFile()
                if (!file.exists()) {
                    println("Configuration file '$configFileName' does not exist. Creating with default content.")
                    //file.writeString(defaultJsonContent)
                    //println("Default configuration successfully created at: ${file.path}")
                    //config = WebAccount()
                } else {
                    println("Configuration file '$configFileName' already exists. Get Config.")
//                    println("====>1 default WebAccount ")
//                    println(json.encodeToString(WebAccount(user_id = "rrr")))
//                    println("====>2")

                    //reset config
//                    file.writeString(json.encodeToString<WebAccount>(WebAccount()));
//                    return@runBlocking

                    val fileContent = file.readString()
                    try {
                        config = parsJsonObject(fileContent)
                    } catch (ee: Exception) {
                        //historical file. write empty config to this file
                        //updateAndSave(WebAccount());
                        file.writeString(json.encodeToString<WebAccount>(WebAccount()))
                    }
                }
            } catch (e: Exception) {

                  println("Error during ConfigFileManager initialization: ${e.cause} ${e.message}")
                    e.printStackTrace()
                }
            }
        }


    /**
     * Retrieves the File object for the fixed configuration file.
     * This will create a File object pointing to 'config.json' in a default
     * application-specific directory or the current working directory, depending
     * on the platform's FileKit implementation.
     */
    private  fun getConfigFile(): PlatformFile {
        // FileKit.newFile creates a File object for the given path.
        // On most platforms, this will resolve to a file in the application's
        // working directory or a suitable default location.
        println("getfile11")
        val file = FileKit.filesDir.resolve(configFileName)
        println("getfile12")
        return file
    }

    /**
     * Saves a given JSON string to the fixed 'config.json' file.
     *
     * @param jsonString The JSON string to save.
     */
    //        suspend fun saveConfigToJsonFile(ipAddress: String, portNumber: Int) {
    suspend fun saveConfigToJsonFile() {

        withContext(Dispatchers.IO) {
            try {
                // Get the File object for the fixed path
                val file = getConfigFile()

//                ip = ipAddress
//                port = portNumber
//                println("ip is $ip, port is $port")
//                val jsonString = buildJsonObject {
//                    put("ip", JsonPrimitive(ipAddress))
//                    put("port", JsonPrimitive(portNumber)) // Convert port to Int
//                }.toString()
                val jsonString = json.encodeToString<WebAccount>(config)
                // Write the JSON string to the file
                file.writeString(jsonString)
                println("Configuration successfully saved to: ${file.path}")
            } catch (e: Exception) {
                println("Error saving configuration file to '$configFileName': ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun parsJsonObject(fileContent: String): WebAccount{
        // Parse the content into a JsonObject
//        val jsonObject = json.parseToJsonElement(fileContent).jsonObject
//        println("Configuration successfully read and parsed from '$configFileName'.")
//
//       val a =    json.decodeFromString<WebAccount>(fileContent)
//        json.encodeToString<WebAccount>(a)
//        ip = jsonObject["ip"]?.jsonPrimitive?.content ?: "N/A"
//        port = jsonObject["port"]?.jsonPrimitive?.int ?: 0
//        return jsonObject;
        return json.decodeFromString<WebAccount>(fileContent)
    }
    /**
     * Reads the content from the fixed 'config.json' file and parses it into a JSON object.
     *
     * @return The parsed JsonObject, or null if the file cannot be read or parsed.
     */
    suspend fun readConfigFromJsonFile(): WebAccount? {
        return withContext(Dispatchers.IO) {
            try {
                // Get the File object for the fixed path
                //println("read1")
                val file = getConfigFile()
                //println("read11")

                // Check if the file exists before attempting to read
                if (!file.exists()) {
                    println("Configuration file '$configFileName' does not exist. No configuration to load.")
                    return@withContext null
                }
                println("read2")

                // Read the content of the file
                val fileContent = file.readString()
                println("Content read from file '$configFileName': \n$fileContent")

                val jsonObject = parsJsonObject(fileContent)

                return@withContext jsonObject
            } catch (e: Exception) {
                println("Error reading or parsing configuration file '$configFileName': ${e.message}")
                e.printStackTrace()
                return@withContext null
            }
        }
    }


    /**
     * 【关键】由外部模块调用：设置新配置并保存
     */
    suspend fun updateAndSave(newConfig: WebAccount) {
        config = newConfig
        saveConfigToJsonFile()
    }

    suspend fun updateAndSave(ipAddress: String, portNumber: Int) {
        config.host = ipAddress
        config.port = portNumber
        saveConfigToJsonFile()
    }

    /**
     * 判断是否已初始化（即 user_id 非空）
     */
    fun isInitialized(): Boolean {
        println("isInitialized: ${config.user_id} ${config.user_id.isNotBlank()}");
        return config.user_id.isNotBlank()
    }
}

// Example usage in a Composable function (or main function for testing)
/*
// Assuming you are in a Composable context or a suspend main function
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.encodeToString

@Composable
fun ConfigScreen() {
    val configManager = remember { ConfigFileManager() }
    var savedConfigText by remember { mutableStateOf("") }
    var loadedConfigObject by remember { mutableStateOf<JsonObject?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            // Example JSON string to save
            val sampleJson = buildJsonObject {
                put("appName", "My Fixed Path App")
                put("version", "1.1.0")
                put("settings", buildJsonObject {
                    put("fixedPathMode", true)
                    put("logLevel", "INFO")
                })
            }.toString()

            savedConfigText = sampleJson
            LaunchedEffect(Unit) {
                configManager.saveConfigToJsonFile(sampleJson)
            }
        }) {
            Text("Save Config (Fixed Path)")
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            LaunchedEffect(Unit) {
                loadedConfigObject = configManager.readConfigFromJsonFile()
            }
        }) {
            Text("Load Config (Fixed Path)")
        }

        Spacer(Modifier.height(16.dp))

        if (savedConfigText.isNotEmpty()) {
            Text("Last Saved JSON:")
            SelectionContainer {
                Text(savedConfigText, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))

        loadedConfigObject?.let {
            Text("Loaded Config JSON:")
            SelectionContainer {
                Text(Json.encodeToString(JsonObject.serializer(), it), style = MaterialTheme.typography.bodySmall)
            }
        } ?: Text("No config loaded yet.")
    }
}

// For a simple console application test:
suspend fun main() {
    val configManager = ConfigFileManager()

    // Example JSON string to save
    val sampleJson = buildJsonObject {
        put("appName", "My Fixed Path App")
        put("version", "1.1.0")
        put("settings", buildJsonObject {
            put("fixedPathMode", true)
            put("logLevel", "INFO")
        })
    }.toString()

    println("Attempting to save JSON to fixed path...")
    configManager.saveConfigToJsonFile(sampleJson)

    println("\nAttempting to read JSON from fixed path...")
    val loadedConfig = configManager.readConfigFromJsonFile()

    loadedConfig?.let {
        println("\nSuccessfully loaded and parsed JSON object from fixed path:")
        println(Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), it))
        println("App Name: ${it["appName"]?.jsonPrimitive?.content}")
    } ?: println("\nFailed to load or parse configuration from fixed path.")
}
*/
