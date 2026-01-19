package io.xa.sigad

// composeApp/src/iosMain/kotlin/io/xa/sigad/Platform.ios.kt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
//import kotlin.native.concurrent.isMainThread

// ----------------------------------------------------
// 定义 IOS 平台的协程作用域
// ----------------------------------------------------

/**
 * 在 iOS 平台上用于启动 coroutines 的 Scope。
 * 默认使用主调度器 (Main Dispatcher)，并使用 SupervisorJob 确保子协程失败不影响父协程。
 */
val platformCoroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

// 您也可以在这里定义一个实际的 Dispatchers.Main 替代方案 (如果需要)
// 确保在 iOS 上使用主线程的 Dispatcher。