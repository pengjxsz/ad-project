package io.xa.sigad

import android.content.Context

/**
 * 用于在应用生命周期内持有 Application Context 的单例。
 * 必须在 Application.onCreate() 中初始化。
 */
object ActivityContextHolder {
    // 使用 lateinit var 来确保在调用前初始化
    lateinit var appContext: Context

    // 修正：使用 Kotlin 反射属性来检查是否已初始化
    val isInitialized: Boolean
        get() = ::appContext.isInitialized
}
