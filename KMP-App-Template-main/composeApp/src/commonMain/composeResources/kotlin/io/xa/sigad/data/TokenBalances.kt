package io.xa.sigad.data
import androidx.compose.runtime.*
//discarded!!!

data class TokenBalances(
    val eth: String = "0",
    val usdc: String = "0",
    val usdt: String = "0",
    val btc: String = "0"
)

class WalletState {
    var address by mutableStateOf("")
    var balances by mutableStateOf(TokenBalances())
    var showQR by mutableStateOf(false)
    var showPayDialog by mutableStateOf(false)

    fun showReceiveQR() {
        showQR = true
        showPayDialog = false
    }

    fun showPayDialog() {
        showPayDialog = true
        showQR = false
    }

    fun hideDialogs() {
        showQR = false
        showPayDialog = false
    }

    suspend fun loadBalances(address: String, repository: BalanceRepository) {
        this.address = address
        balances = repository.fetchBalances(address)
    }

    suspend fun sendPayment(amount: String) {
        // Call WalletConnect client to send transaction
        // Example: connector.sendTransaction(to = ..., amount = ...)
        hideDialogs()
    }
}
//
//class WalletState(
//    private val wcClient: WalletConnectClient
//) {
//    var address by mutableStateOf("")
//    var balances by mutableStateOf(TokenBalances())
//    var showQR by mutableStateOf(false)
//    var showPayDialog by mutableStateOf(false)
//
//    fun showReceiveQR() { showQR = true; showPayDialog = false }
//    fun showPayDialog() { showPayDialog = true; showQR = false }
//    fun hideDialogs() { showQR = false; showPayDialog = false }
//
//    suspend fun sendPayment(amountEth: String, to: String) {
//        val amountWei = amountEth.toBigDecimal()
//            .multiply(BigDecimal("1000000000000000000")) // convert ETH → wei
//            .toBigInteger()
//
//        wcClient.sendTransaction(
//            from = address,
//            to = to,
//            amountWei = amountWei
//        )
//        hideDialogs()
//    }
//}

@Composable
fun rememberWalletState(): WalletState {
    return remember { WalletState() }
}

//Now your WalletScreen can call:
//LaunchedEffect(Unit) {
//    walletState.loadBalances(walletState.address, BalanceRepository())
//}
