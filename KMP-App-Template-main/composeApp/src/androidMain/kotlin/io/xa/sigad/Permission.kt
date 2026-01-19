package io.xa.sigad

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.content.pm.PackageManager
import android.Manifest
import android.nfc.NfcAdapter
import android.app.Activity
import androidx.core.content.ContextCompat

lateinit var applicationContext: Context

fun initSystemChecker(context: Context) {
    applicationContext = context.applicationContext
}

actual fun checkPermission(permission: SystemCheck): Boolean {
    return when (permission) {
        SystemCheck.BluetoothPermission -> {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.BLUETOOTH_CONNECT  //BLUETOOTH not checked!
            ) == PackageManager.PERMISSION_GRANTED
        }

        SystemCheck.BluetoothEnabled -> {
            BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
        }

//        SystemCheck.WifiEnabled -> {
//            try {
//                val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
//                wifiManager.isWifiEnabled
//            }catch(e: Exception){
//                println(e.message)
//                false
//            }
//        }
        ////////////////////////////////////////////////////////////////////////////////
        //<uses-permission android:name="android.permission.NFC" />
        //<uses-feature android:name="android.hardware.nfc" android:required="true" />
        //<uses-feature android:name="android.hardware.nfc" android:required="true" />
        //
        // android.permission.NFC is a normal permission,
        // so it’s automatically granted at install time—no runtime request needed.
        ////////////////////////////////////////////////////////////////////////////////
        //        SystemCheck.NfcPermission -> {
        //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //                ContextCompat.checkSelfPermission(
        //                    applicationContext,
        //                    Manifest.permission.NEARBY_DEVICES
        //                ) == PackageManager.PERMISSION_GRANTED
        //            } else true // No explicit permission required pre-Android 13
        //        }


        SystemCheck.NfcEnabled -> {
            val adapter = NfcAdapter.getDefaultAdapter(applicationContext)
            adapter?.isEnabled == true
        }
    }
}


lateinit var mainActivityProvider: () -> Activity

fun registerMainActivityProvider(provider: () -> Activity) {
    mainActivityProvider = provider
}

actual fun exitApp() {
    mainActivityProvider().finishAffinity()
}
