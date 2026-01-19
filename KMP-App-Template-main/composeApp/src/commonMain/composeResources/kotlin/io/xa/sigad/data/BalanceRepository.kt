package io.xa.sigad.data

//discarded!!!

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.pow

class BalanceRepository(
    private val httpClient: HttpClient = HttpClient(),
    private val apiKey: String = "YOUR_API_KEY" // Covalent/Alchemy/Etherscan
) {

    suspend fun fetchBalances(address: String): TokenBalances {
        // Example using Covalent API
        val url = "https://api.covalenthq.com/v1/eth-mainnet/address/$address/balances_v2/?key=$apiKey"

        val response: HttpResponse = httpClient.get(url)
        val body: String = response.body()
        println("=====fetchBalances:")
        println(body)

        val parsed = Json.decodeFromString(CovalentResponse.serializer(), body)

        val ethBalance = parsed.data.items.find { it.contract_ticker_symbol == "ETH" }?.balanceReadable() ?: "0"
        val usdcBalance = parsed.data.items.find { it.contract_ticker_symbol == "USDC" }?.balanceReadable() ?: "0"
        val usdtBalance = parsed.data.items.find { it.contract_ticker_symbol == "USDT" }?.balanceReadable() ?: "0"
        val btcBalance = parsed.data.items.find { it.contract_ticker_symbol == "WBTC" }?.balanceReadable() ?: "0"

        return TokenBalances(
            eth = ethBalance,
            usdc = usdcBalance,
            usdt = usdtBalance,
            btc = btcBalance
        )
    }
}

@Serializable
data class CovalentResponse(val data: CovalentData)

@Serializable
data class CovalentData(val items: List<TokenItem>)

@Serializable
data class TokenItem(
    val contract_ticker_symbol: String,
    val balance: String,
    val contract_decimals: Int
) {
    fun balanceReadable(): String {
        return (balance.toBigDecimal() / (10.0.pow(contract_decimals))).toPlainString()
    }
}

private fun Double.toPlainString(): String {
    return "";
}

private fun String.toBigDecimal() : Float{
    TODO("Not yet implemented")
    return 0f;
}
//
//✅ Usage in WalletState
//LaunchedEffect(walletState.address) {
//    if (walletState.address.isNotEmpty()) {
//        walletState.loadBalances(walletState.address, BalanceRepository())
//    }
//}
