package io.xa.sigad

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import com.reown.appkit.client.AppKit

//20251204 USE KOIN INSTEAD
//import io.xa.sigad.wallet.initAndroidWalletService

class MainActivity : ComponentActivity() {

    // ----------------------------------------------------
    // 🌟 WalletConnect Deep Link 处理 🌟
    // ----------------------------------------------------

    /**
     * 当 Activity 已经存在（singleTop/singleTask）时，接收新的 Intent。
     * 钱包 App 回调时会调用此方法。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 必须设置新的 Intent，以便后续如需再次处理时，Intent.data 是最新的
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * 实际处理 Intent 中的 URI 数据，并将其转发给 KMP/AppKit。
     */
    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        println("deeplink handleIntent.............................")
        // 检查 Intent 是否包含 URI 数据，并且匹配我们配置的 Scheme
        //if (uri != null && (uri.scheme == "android-sigad-wc" ||
        if (uri != null && (uri.scheme == AppSchemaAndroid ||(
                uri.scheme == "https" && uri.host== trustedDomain
                ))) {

            AppKit.handleDeepLink (uri.toString()){ err ->
                println(err.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initSystemChecker(applicationContext)
        registerMainActivityProvider { this }

        // 🎯 关键步骤：在 setContent() 调用前初始化服务
       // 传入 this (MainActivity) 作为 Context，因为它是一个 Activity，

        // 具有启动 Deep Links (Intent) 的权限。
        //20251204 use koin import instead to be consistent with IOS
        // initAndroidWalletService(this)

// 🌟 首次启动时，处理 Intent (包含首次 Deep Link 或常规启动)
        handleIntent(intent)
        setContent {
            // Remove when https://issuetracker.google.com/issues/364713509 is fixed
            LaunchedEffect(isSystemInDarkTheme()) {
                enableEdgeToEdge()
            }
            App()
        }
    }
}

