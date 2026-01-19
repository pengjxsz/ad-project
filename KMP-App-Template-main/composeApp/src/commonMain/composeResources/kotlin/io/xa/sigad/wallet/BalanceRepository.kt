package io.xa.sigad.wallet

import io.xa.sigad.data.TokenBalances

class BalanceRepository {
    suspend fun fetchBalances(address: String): TokenBalances {
        // Use Covalent or Alchemy API
        // Parse ETH, USDC, USDT, BTC balances
        return TODO("Provide the return value")
    }
}
