package io.xa.sigad.di
import org.koin.core.module.Module

/**
 * 这是一个 expect 属性，用于在平台特定的模块中提供平台实现（如 WalletService）。
 * 必须在每个平台（androidMain, iosMain）中提供 actual 实现。
 */
expect val platformModule: Module