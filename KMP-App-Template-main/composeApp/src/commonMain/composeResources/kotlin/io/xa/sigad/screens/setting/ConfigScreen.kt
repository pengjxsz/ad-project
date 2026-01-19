package io.xa.sigad.screens.setting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.xa.sigad.data.AdsApi
import io.xa.sigad.data.ConfigFileManager
import io.xa.sigad.message.WebAccount
import io.xa.sigad.message.createWebAaccount
import io.xa.sigad.message.masterSignCompactB64
import kotlinx.coroutines.launch

@Composable
fun ConfigScreen() {
    // Access the singleton object directly
    val configManager = ConfigFileManager
    println("configManager screen is $   (${configManager})\n")
    val coroutineScope = rememberCoroutineScope()

    var host by remember { mutableStateOf("127.0.0.1") }
    var portNumber by remember { mutableStateOf(400) }
    var message by remember { mutableStateOf("") }

    // Load config on initial composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val loadedConfig = configManager.readConfigFromJsonFile()
            println("configscreen load config..78..")
            if (loadedConfig != null) {
                println("configscreen load config..781..")

                host = loadedConfig.host //loadedConfig["ip"]?.jsonPrimitive?.content ?: "N/A"
                portNumber = loadedConfig.port //loadedConfig["port"]?.jsonPrimitive?.int ?: 0
                message = "Config loaded successfully!"
//                if (!configManager.isInitialized()){
//                    val a = createWebAaccount();
//                    println("===>4 ${a.user_id} ${a.chatKey} ${a.masterKey} ${a.masterPK} ${a.chatPK}")
//                    try {
////                        val adsApi = AdsApi()
//                        val preRegisterRet = adsApi.preRegister(a.masterPK, a.chatPK, "nick_name")
//                        //preRegisterRet as PreRegisterResponse
//                        println(preRegisterRet.registerPayload)
//
//                        //val compA = "{\"actions\":[{\"act\":\"register\",\"domain\":\"mm.301\",\"param\":[\"35991673\",\"b93adf9a8e7c118ca32925da6b1f9b5fc7b1016449290942c1c8d8b45c2e45ac\"]},{\"act\":\"givegas\",\"domain\":\"mm.301\",\"param\":[\"mm.301.35991673\",1000]},{\"act\":\"setprop\",\"domain\":\"mm.301.35991673\",\"param\":[\"alias\",\"abcde0123456789\"]},{\"act\":\"setactor\",\"domain\":\"mm.301.35991673\",\"param\":[\"chat\",\"26ade27aece3e809dbbdc69d06c9b29e345d3de462725677a5b9fc831f653e8b\"]},{\"act\":\"setactor\",\"domain\":\"mm.301.35991673\",\"param\":[\"chat_office\",\"1d4f99ed08ac05e385f9eb4181064d10f44625b75497f05404b0a67052f781f1\"]},{\"act\":\"setprop\",\"domain\":\"mm.301.35991673\",\"param\":[\"ethaddr\",\"4AB666742818843Fa016BbA4d433190f7e880985\"]}],\"time\":1762610939338}"
//                        val  compA = "{\"actions\":[{\"act\":\"register\",\"domain\":\"mm.301\",\"param\":[\"35991673\",\"b93adf9a8e7c118ca32925da6b1f9b5fc7b1016449290942c1c8d8b45c2e45ac\"]},{\"act\":\"givegas\",\"domain\":\"mm.301\",\"param\":[\"mm.301.35991673\",1000]},{\"act\":\"setprop\",\"domain\":\"mm.301.35991673\",\"param\":[\"alias\",\"abcde0123456789\"]},{\"act\":\"setactor\",\"domain\":\"mm.301.35991673\",\"param\":[\"chat\",\"26ade27aece3e809dbbdc69d06c9b29e345d3de462725677a5b9fc831f653e8b\"]},{\"act\":\"setactor\",\"domain\":\"mm.301.35991673\",\"param\":[\"chat_office\",\"1d4f99ed08ac05e385f9eb4181064d10f44625b75497f05404b0a67052f781f1\"]},{\"act\":\"setprop\",\"domain\":\"mm.301.35991673\",\"param\":[\"ethaddr\",\"4AB666742818843Fa016BbA4d433190f7e880985\"]}],\"time\":1762610939338}";
//
//                        //val compARet = masterSignCompactB64(compA, "47ac269ce71aa58dd210f79f2baa75a93cf0d5bd59a93f35ff82deb963c9cebc", false)
//                        //println("compared: ====> "  + compARet);
//
//                        val deviceChip = "5658d6476432a0e220b7032cdfdfe31a"
//                        val devicePK = "0x044851b519903a4b09879dd3b94f7a56b9daba1c5f03867dd2deaf6978308a08353d54e68b805be2b3c2a341ea2543514a64e41f8c33fff5699f92b22bbe1927e0"
//                        val signedString =  masterSignCompactB64(preRegisterRet.registerPayload, a.masterKey, false)
//                        println("signed:")
//                        println(signedString)
//                        val registerRet = adsApi.register(preRegisterRet.registerPayload,
//                            "0x${signedString}",
//                            devicePK,
//                            deviceChip //"5658d6476432a0e220b7032ccfdfe31a"
//                            )
//                        println("Registered successfully: " + registerRet.userId)
//                        configManager.updateAndSave(WebAccount(user_id = registerRet.userId,
//                            masterPK=a.masterPK,
//                            masterKey = a.masterKey,
//                            chatKey = a.chatKey,
//                            chatPK = a.chatPK,
//                            deviceChip = deviceChip,
//                            devicePK = devicePK))
//                        }catch(e: Exception){
//                        println("RegisterRet: " + e.message)
//                    }
//                }else{
//                    println("configscreen load config..782..")
//
//                    val adsApi = AdsApi()
//                    val adList  = adsApi.fetchAdsList()
//                    //not like javacript, param should be enclosed by ()
//                    val earning = adsApi.getAdsEarnings();
//                    println("earning is " + earning.total )
//                    println("111 get adlist =====> ")
//                    adList.ads.forEach{ele ->  println("${ele.adId} ${ele.clicked}")}
//                    if (adList.ads.size > 0) {
//                        val ad0 = adList.ads[0]
//                        val adReport = adsApi.reportAdClick(ad0.adId, ad0.campaignId);
//                        println(adReport)
//                    }
////                    val add = "0x04fb55008c528a31736db75767f3880cdfac3c9c"
//                    //val add = "0x0000000000675d852c8638df2f227949052b1208"
//                     val add ="0xd3561dA2bFCAC843494854f7de1AF98a3962925f" //ustc trade history
//                    val tokens = adsApi.fetchUserAssets(add , listOf("eth-mainnet"))
//                    val tokens2 = tokens.getEthAndUSDTokens()
//                    println("222 get tokens, all size is ${tokens.tokens.size}===USD size ${tokens2.size}===> ")
//                    tokens2.forEach { token ->
//                            println("token address "+token.tokenAddress)
//                        println(" raw amount " + token.tokenBalance)
//                        println(" amount " + token.textAmount)
//                            println(" price " + token.latestPriceString)
//                    }
//                }
            } else {
                message = "Failed to load config or file does not exist. Using default values."
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Configuration Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        TextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("IP Address") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true
        )

        TextField(
            value = portNumber.toString(), // Display Int as String
            onValueChange = { newValue ->
                // Only allow numeric input for port
                // Update portNumber as Int, default to 0 if input is not a valid number or empty
                portNumber = newValue.toIntOrNull() ?: 0
            },
            label = { Text("Port") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    configManager.updateAndSave(host, portNumber)
                    message = "Config saved successfully!"
                }
            }) {
                Text("Save Config")
            }

            Button(onClick = {
                coroutineScope.launch {
                    val loadedConfig = configManager.readConfigFromJsonFile()
                    if (loadedConfig != null) {
                        host = loadedConfig.host //loadedConfig["ip"]?.jsonPrimitive?.content ?: "N/A"
                        portNumber = loadedConfig.port //loadedConfig["port"]?.jsonPrimitive?.int ?: 0
                        message = "Config loaded successfully!"
                    } else {
                        message = "Failed to load config or file does not exist."
                    }
                }
            }) {
                Text("Load Config")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(message, style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(16.dp))

    }
}
