package io.xa.sigad

// shared/src/commonMain/kotlin/ConfigurationManager.kt

import androidx.compose.runtime.mutableStateOf
import io.xa.sigad.data.AdsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.xa.sigad.data.ConfigFileManager
import io.xa.sigad.message.WebAccount
import io.xa.sigad.message.createWebAaccount
import io.xa.sigad.message.masterSignCompactB64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import io.xa.sigad.crop.picker.bindNFC
import io.xa.sigad.crop.picker.deviceColors
import io.xa.sigad.crop.picker.deviceScreenHeight
import io.xa.sigad.crop.picker.deviceScreenWidth

import io.xa.sigad.crop.picker.isBound
import io.xa.sigad.crop.picker.saveRegisterInfo
import io.xa.sigad.crop.picker.webAccount

// 定义初始化状态
sealed class AppSetupState {
    data object Loading : AppSetupState() // 正在检查状态
    data object Required : AppSetupState() // 需要初始化
    data object RegisterRequired : AppSetupState() // 需要初始化
    data object Completed : AppSetupState() // 初始化已完成
    data class AppSetupError(val message: String) : AppSetupState()
}

object ConfigurationManager {
    // 使用 StateFlow 允许 Composable 订阅状态变化
    private val _setupState = MutableStateFlow<AppSetupState>(AppSetupState.Loading)
    val setupState: StateFlow<AppSetupState> = _setupState.asStateFlow()

    init {
        // 在 App 启动时立即检查持久化状态
        checkInitialSetup()
    }

    private fun checkInitialSetup() {
        // TODO: 从 KMP Settings 或其他持久化存储中读取 'isInitialized'
        val isInitialized = getPersistedInitializationStatus() // 替换为实际的读取逻辑

        _setupState.value = if (isInitialized) {
            AppSetupState.Completed
        } else {
            AppSetupState.Required
        }
    }

    /**
     * 初始化逻辑比较复杂，核心规则描述如下：
     * 初始化环节：
     * 1 APP密钥初始化
     * 2 APP密钥和设备公钥进行交换
     * 3 投屏配置文件初始化
     * 5 邮局密钥初始化
     * 6 用户注册
     * 是否初始化的判断标准：
     * 1 APP需要的配置文件是否存在
     * 2 投屏需要的设备配置文件是否存在
     * 3 投屏需要的APPKEY是否存在
     * 4 投屏需要的DEVICEPK是否存在
     * 5 是否已经注册，用户ID是否存在
     * 原则：
     * 1 如果APP配置文件存在，里面的用户ID存在，则已经初始化；否则需要初始化；
     * 2 邮局相关密钥的产生: APP-KOTLIN，保存到配置文件和 KEYCHAIN(IOS，安卓目前不管）
     * 3 APP密钥的产生：APP-OBJECTC，保存到KEYCHAIN
     * 4 设备ID和设备公钥：APP-OBJECTC-读取，保存到到KEYCHAIN
     * 5 用户ID的产生：APP-KOTLIN，保存到配置文件和KEYCHAIN
     * 处理过程：
     * 1 如果APP根据配置文件判断没有初始化，则进入初始化
     * 2 用户点初始化按钮，启动手机壳层面的初始化
     * 3   PHONESELL在初始化自身的时候，读取APP KEY对，邮局密钥对，用户ID等。
     * 4	PHONESHEL判断是否已经和手机壳绑定
     * 5     如果没有绑定，则绑定
     * 6       如果APP KEY对 不存在，则产生新的并保存到KEYCHAIN
     * 7       如果APP KEY已经存在，则使用
     * 8       绑定APP PUBLIC KEY和DEVICE PUBLIC KEYCHAIN
     * 9       获取到手机壳配置文件和CHIPID
     * 10 由于底层的异步，APP的初始化主控 进行循环非阻塞等待
     * 11   如果超时，初始化失败。
     * 12   如果手机壳已经有USERID，则认为已经注册，认为邮局密钥对有效。创建和保存APP配置文件
     * 13   如果没有USERID，则进行注册
     * 14       APP层面产生邮局需要的密钥对
     * 15       从手机壳获取DEVCIEPK和CHIPID
     * 16       调用RESISTER API进行注册，得到USERID
     * 17       保存USERID到手机壳层面（IOS KEYCHAIN)
     * 18       创建和保存APP配置文件
     *
     * 场景：
     * 1 第一次使用
     * 2 已经注册，第二次使用
     * 3 卸载后重新安装
     * 4 后台注册失败，重新注册
     * 5 清除APP投屏密钥对
     */
    // 在用户点击初始化按钮后调用
    fun runInitialization() {
        _setupState.value = AppSetupState.Loading

        try {
//            PhoneShell *phoneShell = [[PhoneShell sharedInstance] init];
//            if (![phoneShell isEverBound]){
//                [phoneShell BindNFCDevice];
//            }

            if (!isBound()) {
                println("enter binding.....")
                bindNFC()
            }

            println("bound.......")
//
//            val initAccount = createWebAaccount()
//
//            val adsApi = AdsApi()
//            val preRegisterRet = adsApi.preRegister(initAccount.masterPK, initAccount.chatPK, "nick_name")
//            //preRegisterRet as PreRegisterResponse
//            println("  ===register: preregister...")
//            println(preRegisterRet.registerPayload)
//
//            // 模拟耗时的初始化操作，例如网络请求、数据库设置
//            //kotlinx.coroutines.delay(2000)
//            //getDeviceInfo()
//            val deviceChip = "5658d6476432a7e221b7032cdfdfe32a"
//            val devicePK =
//                "0x049ef4269ba41b614ec402f3f656d62d1fc84e79124ad49320781b5d1ccbd47fb88d2e81c404b2ce85ddc8ae1b62240ebae2d37a3aeae913774290c6a2a48ce884"
//            val signedString =
//                masterSignCompactB64(preRegisterRet.registerPayload, initAccount.masterKey, false)
//            println("  ===register: signedString...")
//            println(signedString)
//            val registerRet = adsApi.register(
//                preRegisterRet.registerPayload,
//                "0x${signedString}",
//                devicePK,
//                deviceChip //"5658d6476432a0e220b7032ccfdfe31a"
//            )
//            println(" ===register: successfully: " + registerRet.userId)
//            ConfigFileManager.updateAndSave(WebAccount(user_id = registerRet.userId,
//                masterPK=initAccount.masterPK,
//                masterKey = initAccount.masterKey,
//                chatKey = initAccount.chatKey,
//                chatPK = initAccount.chatPK,
//                deviceChip = deviceChip,
//                devicePK = devicePK))
//            // TODO: 持久化存储 'true'
//            setPersistedInitializationStatus(true)
//            println(" ===register: account saved: ")
            //_setupState.value = AppSetupState.Completed

        } catch (e: Exception) {
            println("RegisterRet: " + e.message)
            _setupState.value = AppSetupState.AppSetupError(e.message.toString())
        }

    }


    suspend fun runRegister() {
        _setupState.value = AppSetupState.RegisterRequired

        try {
            //When registering, must create new key pairs
            val initAccount = createWebAaccount()

            val adsApi = AdsApi()
            val nickName = "nick_name"
            val preRegisterRet =
                adsApi.preRegister(initAccount.masterPK, initAccount.chatPK, nickName)
            //preRegisterRet as PreRegisterResponse
            println("  ===register: preregister...")
            println(preRegisterRet.registerPayload)

            // 模拟耗时的初始化操作，例如网络请求、数据库设置
            //kotlinx.coroutines.delay(2000)
            //getDeviceInfo()
            val deviceChip = webAccount.deviceChip //"5658d6476432a7e221b7032cdfdfe32a"
            val devicePK = "0x${webAccount.devicePK}"
                //"0x049ef4269ba41b614ec402f3f656d62d1fc84e79124ad49320781b5d1ccbd47fb88d2e81c404b2ce85ddc8ae1b62240ebae2d37a3aeae913774290c6a2a48ce884"
            val signedString =
                masterSignCompactB64(preRegisterRet.registerPayload, initAccount.masterKey, false)
            println("  ===register: signedString...(${deviceChip})(${devicePK})")
            println(signedString)
            val registerRet = adsApi.register(
                preRegisterRet.registerPayload,
                "0x${signedString}",
                devicePK,
                deviceChip //"5658d6476432a0e220b7032ccfdfe31a"
            )
            println(" ===register: successfully: " + registerRet.userId)
            webAccount = WebAccount(
                user_id = registerRet.userId,
                masterPK = initAccount.masterPK,
                masterKey = initAccount.masterKey,
                chatKey = initAccount.chatKey,
                chatPK = initAccount.chatPK,
                deviceChip = deviceChip,
                devicePK = devicePK,
                user_nick = nickName,
                deviceWith = deviceScreenWidth,
                deviceHeight = deviceScreenHeight,
                device_colors = deviceColors
            )
            ConfigFileManager.updateAndSave(webAccount)
            saveRegisterInfo()

            // TODO: 持久化存储 'true'
            setPersistedInitializationStatus(true)
            println(" ===register: account saved: ")
            _setupState.value = AppSetupState.Completed

        } catch (e: Exception) {
            println("RegisterRet: " + e.message)
            _setupState.value = AppSetupState.AppSetupError(e.message.toString())
        }

    }

    suspend fun restoreRegister() {
        println(" user id already existed. restore it...")
        //webAccount.user_id = "efb51d4d17c82643081725b090006be8f76ee0adef93f888a9411a5bcd508ec9"
        ConfigFileManager.updateAndSave(
            webAccount
        )
        // TODO: 持久化存储 'true'
        setPersistedInitializationStatus(true)
        setCompleteStatus();
    }

    //called after initializaion of bingding phoneshell
    fun setRegisterStatus() {
        _setupState.value = AppSetupState.RegisterRequired
    }

    fun setCompleteStatus() {
        _setupState.value = AppSetupState.Completed
    }

    fun setErrorStatus(message:String = "") {
        _setupState.value = AppSetupState.AppSetupError( if (message=="") "手机壳初始化绑定 PUBKEY 失败," else message)
    }

    fun cleanupMasterKey(){

        webAccount =  WebAccount(masterKey = "", //webAccount.masterKey,
            masterPK= "", //webAccount.masterKey,
            chatKey= webAccount.chatKey,
            chatPK = webAccount.chatPK,
            user_id = webAccount.user_id,
            deviceChip = "",
            devicePK = "")

        saveRegisterInfo();
        println(".... cleanupMasterKey")

    }

    fun cleanupRegisterInfo(){
        val tmpAccount = mapOf(
            "user_id" to "",
            "masterPK" to  "",
            "masterKey" to  "",
            "chatKey" to  "",
            "chatPK" to  "",
            // "deviceChip": "42503152343538050064C23656036F78",
            //"devicePK": "0x04FB0143A440AE61A4A231A9AA364FB6A3CB201292BCD4602809A0041FF9FB9EDA3A1F12AE37476950A21AE019D60598B12FDCC14770EF409030F04FF45C7B7FAE",
        )
        webAccount =  WebAccount(masterKey = tmpAccount.getValue("masterKey"),
            masterPK= tmpAccount.getValue("masterPK"),
            chatKey= tmpAccount.getValue("chatKey"),
            chatPK = tmpAccount.getValue("chatPK"),
            user_id = tmpAccount.getValue("user_id"),
            deviceChip = "",
            devicePK = "")

        saveRegisterInfo();
        println(".... cleanupRegisterInfo")
    }
    fun setRegisterInfo(){
        val tmpAccount = mapOf(
            "user_id" to "90719dc59553a950412b81d550f6d0fd60255bd32f37fe71516ca8cadfea37b3",
            "masterPK" to  "0x049eaa8cadd686c23ec1bb0b4a309c465204acefd2f837c481dae8d3da61ad64e960b14bd5f66b9115d3beb580928e8374cafbf84c6ac3348e42c66a85067f3053",
            "masterKey" to  "51f05f60b268225e1ca977bc330a9c1419ab923c0c188fb51e1ab5786790798b",
            "chatKey" to  "3b789c7d222f16709419ae2dd4e28ee5c640d68c789ca94779f1f7ce328e0900",
            "chatPK" to  "0x048ad166f88a2b6548e266697baf97a602b7eeb19d3c74d9fc8a0e45c6ee6dcb88fabb8dc27b2e035ef3eef194b944b44476e92e6d35010e4042bdd968635a55d7",
            // "deviceChip": "42503152343538050064C23656036F78",
            //"devicePK": "0x04FB0143A440AE61A4A231A9AA364FB6A3CB201292BCD4602809A0041FF9FB9EDA3A1F12AE37476950A21AE019D60598B12FDCC14770EF409030F04FF45C7B7FAE",
        )
        webAccount =  WebAccount(masterKey = tmpAccount.getValue("masterKey"),
            masterPK= tmpAccount.getValue("masterPK"),
            chatKey= tmpAccount.getValue("chatKey"),
            chatPK = tmpAccount.getValue("chatPK"),
            user_id = tmpAccount.getValue("user_id"),
            deviceChip = "",
            devicePK = "")

        saveRegisterInfo();
        println(".... setRegisterInfo")

    }
    // 假设的持久化函数
    private fun getPersistedInitializationStatus(): Boolean {
        // 实际实现：读取 KMP Settings
        val loadedConfig = runBlocking {
            ConfigFileManager.readConfigFromJsonFile()
        }

        //For test
        //cleanupRegisterInfo();

        println("configscreen load config..78..")
        return if (loadedConfig != null)
            ConfigFileManager.isInitialized() // 假设默认为 false，需要初始化
        else
            false
    }

    private fun setPersistedInitializationStatus(status: Boolean) {
        // 实际实现：写入 KMP Settings
    }
}