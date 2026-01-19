package io.xa.sigad.screens.wallet
/*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.xa.sigad.data.TokenBalances
import io.xa.sigad.data.rememberWalletState
//import io.xa.sigad.utils.QRCodeView

@Composable
fun WalletScreen() {
    val walletState = rememberWalletState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Wallet address
        Text(
            text = "Wallet Address: ${walletState.address}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(12.dp))

        // Action buttons
        Row {
            Button(onClick = { walletState.showReceiveQR() }) {
                Text("Receive")
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = { walletState.showPayDialog() }) {
                Text("Pay")
            }
        }

        Spacer(Modifier.height(20.dp))

        // Balances section
        Text(
            text = "Balances",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        BalanceView(walletState.balances)

        // Conditional UI for QR or Pay dialog
//        if (walletState.showQR) {
//            QRCodeView(walletState.address)
//        }

//        if (walletState.showPayDialog) {
//            PayDialog(walletState.address) { (amount, address) ->
//                walletState.sendPayment(amount, address)
//            }
//        }
    }
}

@Composable
fun BalanceView(balances: TokenBalances) {
    Column {
        BalanceRow("ETH", balances.eth)
        BalanceRow("USDC", balances.usdc)
        BalanceRow("USDT", balances.usdt)
    }
}

@Composable
fun BalanceRow(symbol: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = symbol, style = MaterialTheme.typography.bodyMedium)
        Text(text = amount, style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
fun PayDialog(
    fromAddress: String,
    onConfirm: (Int,String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var toAddress by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Send Payment")
            Spacer(Modifier.height(8.dp))
            TextField(value = toAddress, onValueChange = { toAddress = it }, label = { Text("Recipient Address") })
            Spacer(Modifier.height(8.dp))
            TextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (ETH)") })
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onConfirm(amount.toInt(), toAddress) }) {
                Text("Confirm")
            }
        }
    }
}


suspend fun sendTransaction(to: String, amount: String) {
//    val tx = WalletTransaction(
//        from = walletAddress,
//        to = to,
//        value = amount.toBigInteger()
//    )
//    connector.sendTransaction(tx)
}
*/

//back for reference

