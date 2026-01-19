package io.xa.sigad.screens.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
// 🚀 确保此导入存在，它是 items(List<T>) 的核心
import androidx.compose.foundation.lazy.items

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // 委托所需的 getValue

import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.xa.sigad.data.model.BlockchainNetwork
import io.xa.sigad.data.model.CurrentNetworkState
import io.xa.sigad.data.model.MAIN_NETWORKS
import io.xa.sigad.data.model.TEST_NETWORKS



data class NetworkSelectionScreen(val isTestnet: Boolean) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
// 🚀 获取当前选中的 Chain ID
     //   val currentChainId by CurrentNetworkState.currentChainId // 替换为您的实际状态
        val currentChainId by CurrentNetworkState.currentChainId.collectAsState() // 假设您使用了 StateFlow

        val networks = if (isTestnet) TEST_NETWORKS else MAIN_NETWORKS
        val title = if (isTestnet) "选择测试网络" else "选择主网络"

        // 假设当前选中的网络状态保存在一个全局 ViewModel 或 State 中
        // val currentNetwork by remember { GlobalNetworkState.currentNetwork }.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = { BackButton(onBack = { navigator.pop() }) }
                )
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(networks) { network ->
                    NetworkItem(
                        network = network,
                        isSelected = network.chainId == currentChainId,
                        onClick = {
                            // TODO: 1. 更新全局网络状态
                            // TODO: 2. 导航返回 (可选: navigator.pop())
                            println("Switched to Network: ${network.chineseName}")
                            // 1. 更新全局网络状态
                            CurrentNetworkState.setNetwork(network.chainId) // 替换为您的实际更新逻辑

                            // 2. 导航返回
                            navigator.pop()
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

//@Composable
//fun NetworkItem(network: BlockchainNetwork, isSelected: Boolean = false, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column(modifier = Modifier.weight(1f)) {
//            Text(network.chineseName, style = MaterialTheme.typography.titleMedium)
//            Text("Chain ID: ${network.chainId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//        }
//
//        if (isSelected) {
//            Icon(Icons.Filled.Check, contentDescription = "已选择", tint = MaterialTheme.colorScheme.primary)
//        }
//    }
//}

// commonMain/screens/NetworkSelectionScreen.kt

@Composable
fun NetworkItem(network: BlockchainNetwork, isSelected: Boolean = false, onClick: () -> Unit) {
    // 决定背景颜色
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer // 使用主题中的强调背景色
    } else {
        Color.Transparent
    }

    // 决定文字颜色
    val titleColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer // 使用强调背景色上的文字颜色
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor) // 🚀 应用背景色
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // 🚀 应用文字颜色和粗体样式
            Text(
                network.chineseName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = titleColor
                )
            )
            Text(
                "Chain ID: ${network.chainId}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) titleColor.copy(alpha = 0.8f) else Color.Gray
            )
        }

        // 🚀 添加复选标记图标
        if (isSelected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "已选择",
                tint = titleColor // 使用与文字相配的颜色
            )
        }
    }
}