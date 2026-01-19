package io.xa.sigad.message

import io.xa.sigad.data.ConfigFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object POConfig {
    val poName: String = "mm.301"

    //val tcpIp: String = "192.168.1.3"
    val tcpIp :String
        get(){
            return configManager?.config?.host!!
        }
    val tcpPort : Int
        get(){
            return configManager?.config?.port!!
        }
    var configManager : ConfigFileManager? = null

    init{
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
        //withContext(Dispatchers.IO){
            configManager = ConfigFileManager
            println("configManager is $   ($configManager)\n")

            //configManager.readConfigFromJsonFile()
        }
    }
}

