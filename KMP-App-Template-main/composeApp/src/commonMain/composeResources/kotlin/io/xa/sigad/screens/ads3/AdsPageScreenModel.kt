package io.xa.sigad.screens.ads3


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.ktor.utils.io.core.String
import io.xa.sigad.data.AdsApi
import io.xa.sigad.data.model.Ads3Ad
import io.xa.sigad.data.model.AdsEarnings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

//// --- UI 模型 ---
//data class AdTask(
//    val ad: Ads3Ad,
//    val isCompleted: Boolean = false // 模拟任务完成状态
//)

// --- ViewModel (简化版) ---
// if not using ScreenModel, then
//    define  ContrutinScope
//    define mutableStateOf
//    proragate state
//class AdsPageScreenModel(
//    private val adsApi: AdsApi,
//    private val userWalletAddress: String // 假设用户钱包地址已获取
//) :StateScreenModel<AdsPageScreenModel.State>(State.Init){
//    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//    private val _state = mutableStateOf(AdsScreenState(isLoading = true))
//    val state: State<AdsScreenState> = _state
class AdsPageScreenModel(
) : StateScreenModel<AdsPageScreenModel.State>(State.Init) {

    sealed class State {
        object Init : State()
        object Loading : State()
        data class Error(val message: String) : State()
        data class Result(val earnings: Float, val result: List<Ads3Ad> = emptyList()) : State()
    }

    private val adsApi = AdsApi()

    var ad3AdListCache: List<Ads3Ad> = emptyList()

    init {
        println("AdsPageScreeModel init....")
        loadData()
    }

    private fun loadData() = screenModelScope.launch {
        //_state.value = _state.value.copy(isLoading = true, errorMessage = null)
        mutableState.value = State.Loading
        try {
            // 1. 获取收益 (假设只关心一个代币作为收益)
            val earningResponse = adsApi.getAdsEarnings();

            // 2. 获取广告任务列表
            val adsResponse = adsApi.fetchAdsList()
            ad3AdListCache = adsResponse.ads
            println("earning is ${earningResponse.total}, task size is ${adsResponse.ads.size}")

            adsResponse.ads.forEach { ele -> println("${ele.adId} ${ele.clicked}") }

            mutableState.value = State.Result(earningResponse.total, adsResponse.ads)

        } catch (e: Exception) {
            mutableState.value = State.Error(e.message.toString())
//            _state.value = _state.value.copy(
//                isLoading = false,
//                errorMessage = "数据加载失败: ${e.message}"
//            )
        }
    }

    // 广告点击逻辑
//    fun onAdClicked(task: Ads3Ad, openUrlCallback: (String) -> Unit) = screenModelScope.launch {
    fun onAdClicked(task: Ads3Ad) = screenModelScope.launch {
        println("onAdClicked ${task.destination}")
        // 实际逻辑：
        // 1. 打开 WebView
        //openUrlCallback(task.destination.url)

        // 2. (此处仅为模拟，实际点击事件上报应在 WebView 关闭后进行)
        // 假设点击 GO 立即上报，并标记任务完成
        try {
            val reportResponse = adsApi.reportAdClick(task.adId, task.campaignId)

            val result = mutableState.value as State.Result
            ad3AdListCache = ad3AdListCache.map {
                if (it.adId == task.adId) it.copy(clicked = true) else it
            }
            mutableState.value = State.Result(result.earnings, ad3AdListCache);
        } catch (e: Exception) {
            println(" report clicked event err: ${e.message}")
        }
    }

}