package io.xa.sigad.utils

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import network.chaintech.compose.qr_code_kit.models.QrKitOptions
//import network.chaintech.compose.qr_code_kit.rememberQrKitPainter
//
//@Composable
//fun QRCodeView(qrData: String) {
//
//    // 生成二维码的 Painter
//    val painter = rememberQrKitPainter(
//        data = qrData,
//        // 可选：可以自定义尺寸、颜色和添加 logo 等
//        options = QrKitOptions(
//            size = 200 // 像素大小
//        )
//    )
//
//    // 在 Composable 中显示生成的二维码
//    Image(
//        painter = painter,
//        contentDescription = "Generated QR Code",
//        modifier = Modifier.size(200.dp)
//    )
//}