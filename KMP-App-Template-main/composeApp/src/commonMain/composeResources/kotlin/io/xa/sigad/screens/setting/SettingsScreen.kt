package io.xa.sigad.screens.setting

// commonMain/screens/SettingsScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.xa.sigad.screens.WebViewScreen
// ... 其他 Compose 导入

object SettingsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            //topBar = { TopAppBar(modifier = Modifier.height(48.dp),title = { Text("设置") }) }
        ) { padding ->
            Column(modifier = Modifier.padding(1.dp).fillMaxSize()) {

                Text(
                    "设置",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                // --- 1 & 2. 网络切换 ---
                SettingItem(
                    title = "网络切换",
                    onClick = { navigator.push(NetworkSelectionScreen(isTestnet = false)) }
                )
                Divider()

                SettingItem(
                    title = "测试网络切换",
                    onClick = { navigator.push(NetworkSelectionScreen(isTestnet = true)) }
                )
                Divider()

                // --- 3 & 4. 服务条款 & 隐私策略 ---
                val termsUrl = "https://legal.adpal.xyz/tos"
                val privacyUrl = "https://legal.adpal.xyz/privacy"

                SettingItem(
                    title = "服务条款",
                    onClick = { navigator.push(WebViewScreen(url = termsUrl, title = "服务条款")) }
                )
                Divider()

                SettingItem(
                    title = "隐私策略",
                    onClick = { navigator.push(WebViewScreen(url = privacyUrl, title = "隐私策略")) }
                )
                Divider()

                // --- 5. 设备信息 ---
                SettingItem(
                    title = "设备信息",
                    onClick = { navigator.push(DeviceInfoScreen) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun SettingItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "进入")
    }
}