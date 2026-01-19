package io.xa.sigad.screens.ads3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import  io.xa.sigad.hostOpenUrl
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import io.xa.sigad.data.AdsApi
import io.xa.sigad.data.model.Ads3Ad
import io.xa.sigad.data.model.getBlockchainEnglishName
import io.xa.sigad.data.model.mapTokensToTokenBalances
import io.xa.sigad.screens.WebViewScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


sealed class AppScreens {
    // 基础页面
    data object AdsPageScreen : AppScreens()

    // 新增：WebView 页面，用于在应用内加载 URL
    data class WebViewScreen(val url: String) : AppScreens()
}

// 假设 Voyager 库已配置
// 核心 Compose 页面

class AdsPageScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        println("AdsPageScreen Content")
        val screenModel = rememberScreenModel { AdsPageScreenModel() }

        AdsPageContent(screenModel = screenModel)
    }
}

//fun ReportClick( scope: CoroutineScope, adId: String, campaignId: String){
////    val scope = rememberCoroutineScope()
//    scope.launch {
//        try {
//            val adsApi = AdsApi()
//            val tokensRespond = adsApi.reportAdClick(
//                adId,
//                campaignId
//            )
//
//        } catch (e: Exception) {
//            errorMessage = "获取余额失败: ${e.message}"
//        }
//    }
//}

@Composable
fun AdsPageContent(
    // 假设 ViewModel 注入或通过工厂创建
    screenModel: AdsPageScreenModel,
    // 这是核心回调，用于通知宿主打开 BROWSER
//    openUrlCallback: (String) -> Unit
// 关键点：触发导航到 WebViewScreen
    //  onNavigateTo: (AppScreens) -> Unit
) {
    val state by screenModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("广告任务") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            when (state) {
                is AdsPageScreenModel.State.Init ->
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                is AdsPageScreenModel.State.Loading ->
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                is AdsPageScreenModel.State.Error ->
                    Text(
                        (state as AdsPageScreenModel.State.Error).message,
                        color = MaterialTheme.colors.error, modifier = Modifier.padding(16.dp)
                    )

                is AdsPageScreenModel.State.Result -> {
                    // 2. 收益显示区域
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "当前收益",
                                fontSize = 16.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (state as AdsPageScreenModel.State.Result).earnings.toString(),
                                fontSize = 28.sp,
                                style = MaterialTheme.typography.h4
                            )
                        }
                    }


                    // 3. 广告任务列表
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)
                    ) {
                        item {
                            Text(
                                "广告任务列表",
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items((state as AdsPageScreenModel.State.Result).result) { task ->
                            AdTaskItem(screenModel, task)
//                            screenModel.onAdClicked(task)
//                            // 关键点：触发导航到 WebViewScreen
//                            onNavigateTo(AppScreens.WebViewScreen(task.destination.toString()))
                            Divider()
                        }

//                        urls.forEach { url ->
//                            Text(
//                                text = url,
//                                modifier = Modifier.clickable {
//                                    navigator.push(WebViewScreen(url))
//                                }
//                            )
//                        }

                    }
                }

            }

        }
    }
}

// 广告任务列表项
//fun AdTaskItem(task: Ads3Ad, onGoClick: () -> Unit) {
@Preview
@Composable
fun AdTaskItem(screenModel: AdsPageScreenModel, task: Ads3Ad) {
    val navigator = LocalNavigator.currentOrThrow

    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 广告图标和文本
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon
            //AsyncImage(task.icon) // 实际使用 Coil/Glide 等库加载图片
            AsyncImage(
                model = task.icon,
                contentDescription = "ddd",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .weight(15f),
                // .background(Color.LightGray)
            )

            //Text("🖼️", modifier = Modifier.padding(end = 8.dp).weight(70f))
            println(" ddddd is ${task.destination}, ${task.icon} ${task.image}")
            Column(modifier = Modifier.weight(70f)) {
                Text(
                    task.text, style = MaterialTheme.typography.body1,
                    modifier = Modifier.clickable {
                        navigator.push(WebViewScreen(task.destination.url.toString(), "Ads3"))
                    })
                Text("活动ID: ${task.campaignId}", style = MaterialTheme.typography.caption)
            }

            // 按钮/状态
            if (task.clicked == true) {
                Text(
                    "已完成",
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.subtitle1
                )
            } else {
                task.clicked?.let {
                    Button(
                        onClick = {
                            //ReportClick(coroutineScope, task.adId, task.campaignId)
                            screenModel.onAdClicked(task)
                            navigator.push(WebViewScreen(task.destination.url.toString(), "Ads3")) },
                        //enabled = !it // 禁用已完成的任务
                    ) {
                        Text("GO")
                    }
                }
            }
        }


    }
}

