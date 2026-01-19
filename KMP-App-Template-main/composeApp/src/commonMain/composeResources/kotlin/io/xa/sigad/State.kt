package io.xa.sigad

sealed class State {
    object Init : State()
    object Loading : State()
    data class Result(val data: Any) : State()
}


expect fun exitApp()

enum class SystemCheck {
    BluetoothPermission,
    BluetoothEnabled,
    //WifiEnabled,
    NfcEnabled
}
fun SystemCheck.readableName(): String = when (this) {
    SystemCheck.BluetoothPermission -> "Bluetooth Permission"
    SystemCheck.BluetoothEnabled -> "Bluetooth Not Enabled"
   // SystemCheck.WifiEnabled -> "Wi-Fi Not Enabled"  //check called WifiService which needd ACCESS_WIFI_STATE
    SystemCheck.NfcEnabled -> "NFC Not Enabled"
}

expect fun checkPermission(permission: SystemCheck): Boolean

fun checkPermissions() : List<SystemCheck> {
    val checks = listOf(
        SystemCheck.BluetoothPermission,
        SystemCheck.BluetoothEnabled,
        //SystemCheck.WifiEnabled,
        SystemCheck.NfcEnabled
    )
    val failed = checks.filterNot { checkPermission(it) }

    return failed;

}