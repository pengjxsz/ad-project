package io.xa.sigad

import android.app.Application
import io.xa.sigad.di.initKoin
import android.app.Activity
import org.koin.android.ext.koin.androidContext // ⚠️ 确保导入 Koin Android 扩展

//class SigADApp : Application() {
//    override fun onCreate() {
//        super.onCreate()
//        // 关键步骤：在应用启动时初始化 Context
//        ActivityContextHolder.appContext = applicationContext
//        initKoin()
//    }
//}


class SigADApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // ❌ 移除旧的 initKoin() 调用

        // ✅ 使用新的 initKoin，并传入配置块
        initKoin {
            // 关键步骤：告诉 Koin 使用当前的 Application 实例作为 Context
            androidContext(this@SigADApp)
        }

        // ActivityContextHolder.appContext = applicationContext // 如果您不再使用这个手动管理 Context 的工具，可以考虑移除
    }
}