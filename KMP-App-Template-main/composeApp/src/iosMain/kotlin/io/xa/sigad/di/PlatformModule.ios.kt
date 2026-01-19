package io.xa.sigad.di

import org.koin.dsl.module
import io.xa.sigad.wallet.WalletService
import io.xa.sigad.wallet.IOSAppkitWalletService

// iosMain/io/xa/sigad/di/PlatformModule.ios.kt
actual val platformModule = module {
    single<WalletService> (createdAtStart = false){ IOSAppkitWalletService() }
}

// 在 iOSApp.swift 中调用：
//KoinKt.initKoin(platformModules: [PlatformModuleIosKt.platformModule])