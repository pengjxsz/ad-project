package io.xa.sigad


import kotlinx.cinterop.*
import platform.CoreNFC.NFCNDEFReaderSession
import platform.Foundation.NSClassFromString
import platform.CoreBluetooth.*
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.exit

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun checkPermission(permission: SystemCheck): Boolean {
    return when (permission) {
        SystemCheck.BluetoothPermission -> {
            val manager = CBCentralManager(null, null)
            manager.authorization == CBManagerAuthorizationAllowedAlways || manager.authorization == CBManagerAuthorizationNotDetermined
        }
        SystemCheck.BluetoothEnabled -> {
            val manager = CBCentralManager(null, null)
            manager.state == CBManagerStatePoweredOn
        }
        //SystemCheck.WifiEnabled -> isWifiEnabled()
        SystemCheck.NfcEnabled -> {
            NSClassFromString("NFCNDEFReaderSession") != null &&
                    NFCNDEFReaderSession.readingAvailable
        }

    }
}



//@OptIn(ExperimentalForeignApi::class)
//fun isWifiEnabled(): Boolean = memScoped {
//    val flags = alloc<SCNetworkReachabilityFlagsVar>()
//    val reachability = SCNetworkReachabilityCreateWithName(null, "apple.com")
//    if (reachability != null && SCNetworkReachabilityGetFlags(reachability, flags.ptr)) {
//        val flagValue = flags.value.toInt()
//        val reachable = (flagValue and kSCNetworkReachabilityFlagsReachable) != 0
//        val isOnWifi = (flagValue and kSCNetworkReachabilityFlagsIsWWAN) == 0
//        reachable && isOnWifi
//    } else {
//        false
//    }
//}

//However, sometimes, due to the way the macro is defined or interpreted by cinterop,
// it might be seen as a function-like macro or a constant that doesn't explicitly resolve to an Int in the Kotlin binding.
// If it's a macro that performs no operation or is used in a context where cinterop can't infer its type as an integer,
// it might default to Unit (Kotlin's equivalent of void in C, meaning "no meaningful value").
@OptIn(ExperimentalForeignApi::class)
fun isWifiEnabled(): Boolean = memScoped {
    val flags = alloc<SCNetworkReachabilityFlagsVar>()
    val reachability = SCNetworkReachabilityCreateWithName(null, "apple.com")

    // Explicit integer values for the flags if cinterop is misinterpreting them
    val KSCNETWORKREACHABILITYFLAGSREACHABLE_INT = 1 // kSCNetworkReachabilityFlagsReachable
    val KSCNETWORKREACHABILITYFLAGSISWWAN_INT = 2   // kSCNetworkReachabilityFlagsIsWWAN

    if (reachability != null && SCNetworkReachabilityGetFlags(reachability, flags.ptr)) {
        val flagValue = flags.value.toInt()

        // Use the explicit integer value
        val reachable = (flagValue and KSCNETWORKREACHABILITYFLAGSREACHABLE_INT) != 0
        val isOnWifi = (flagValue and KSCNETWORKREACHABILITYFLAGSISWWAN_INT) == 0

        reachable && isOnWifi
    } else {
        false
    }
}


actual fun exitApp() {
    exit(0)
}
