package io.xa.sigad.screens.setting

// commonMain/screens/DeviceInfoScreen.kt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.xa.sigad.ConfigurationManager
import io.xa.sigad.data.ConfigFileManager

object DeviceInfoScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // TODO: 这些信息需要通过 expect/actual 或平台 API 获取
        val config = ConfigFileManager.config
        val infoList = listOf(
            "设备ID" to config.deviceChip,
            "设备公钥" to config.devicePK,
            "用户ID" to config.user_id,
            "昵称" to config.user_nick,
            "屏幕大小" to "${config.deviceWith} * ${config.deviceHeight}",
            "色彩数" to "${config.device_colors}" // 24位真彩色，即 2^24
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("设备信息") },
                    navigationIcon = { BackButton(onBack = { navigator.pop() }) }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

                infoList.forEach { (key, value) ->
                    DeviceInfoItem(key = key, value = value)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun DeviceInfoItem(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(key, modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

// 辅助回退按钮 Composable
@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
    }
}