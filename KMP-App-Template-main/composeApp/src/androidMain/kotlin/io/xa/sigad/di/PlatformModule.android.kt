package io.xa.sigad.di

import org.koin.dsl.module
import io.xa.sigad.wallet.WalletService
import io.xa.sigad.wallet.AndroidAppkitWalletService

actual val platformModule = module {
    // 假设 Android 实现需要 Context
    single<WalletService> (createdAtStart = false){ AndroidAppkitWalletService(context = get()) }
}

// 在 Android Application/Activity 中调用：
//initKoin(platformModules = listOf(PlatformModuleAndroidKt.platformModule))