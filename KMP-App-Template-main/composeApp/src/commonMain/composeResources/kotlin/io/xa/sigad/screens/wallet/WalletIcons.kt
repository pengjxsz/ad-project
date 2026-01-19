package io.xa.sigad.screens.wallet


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor

import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.PathParser
// --- 0. 自定义 SVG 路径图标定义 (Custom Inline SVG Icons) ---

val EthIcon: ImageVector = ImageVector.Builder(
    name = "EthIcon",
    defaultWidth = 500.dp,
    defaultHeight = 500.dp,
    viewportWidth = 500f,
    viewportHeight = 500f
).apply {
    addPath(
        pathData = PathParser().parsePathString("M249.982,6.554 L397.98,251.112 L250.53,188.092 Z").toNodes(),
        fill = SolidColor(Color(0xFF2F3030))
    )
    addPath(
        pathData = PathParser().parsePathString("M102.39,251.112 L249.982,6.554 L250.53,188.092 Z").toNodes(),
        fill = SolidColor(Color(0xFF828384))
    )
    addPath(
        pathData = PathParser().parsePathString("M249.982,341.285 L102.39,251.112 L250.53,188.092 Z").toNodes(),
        fill = SolidColor(Color(0xFF343535))
    )
    addPath(
        pathData = PathParser().parsePathString("M397.98,251.112 L250.53,188.092 L249.982,341.285 Z").toNodes(),
        fill = SolidColor(Color(0xFF131313))
    )
    addPath(
        pathData = PathParser().parsePathString("M249.982,372.329 L397.98,284.597 L249.982,493.13 Z").toNodes(),
        fill = SolidColor(Color(0xFF2F3030))
    )
    addPath(
        pathData = PathParser().parsePathString("M249.982,372.329 L102.39,284.597 L249.982,493.13 Z").toNodes(),
        fill = SolidColor(Color(0xFF828384))
    )
}.build()

val UsdcIcon: ImageVector = ImageVector.Builder(
    name = "UsdcIcon",
    defaultWidth = 500.dp,
    defaultHeight = 500.dp,
    viewportWidth = 500f,
    viewportHeight = 500f
).apply {
    addPath(
        pathData = PathParser().parsePathString("M250,250 m-248.3,0 a248.3,248.3 0 1,0 496.6,0 a248.3,248.3 0 1,0 -496.6,0").toNodes(),
        fill = SolidColor(Color(0xFF2775CA))
    )
    addPath(
        pathData = PathParser().parsePathString("M203.4,422.7c0,5.8-4.7,9.1-10.1,7.4C118.1,406,63.8,335.7,63.8,252.5c0-83,54.3-153.4,129.6-177.5 ...").toNodes(),
        fill = SolidColor(Color.White)
    )
    addPath(
        pathData = PathParser().parsePathString("M265.5,368.9c0,4.3-3.5,7.8-7.8,7.8h-15.5c-4.3,0-7.8-3.5-7.8-7.8v-24.4 ...").toNodes(),
        fill = SolidColor(Color.White)
    )
    addPath(
        pathData = PathParser().parsePathString("M306.6,430c-5.6,1.7-10.1-1.6-10.1-7.4v-14.5c0-4.3,2.5-8.3,6.6-9.7 ...").toNodes(),
        fill = SolidColor(Color.White)
    )
}.build()

val UsdtIcon: ImageVector = ImageVector.Builder(
    name = "UsdtIcon",
    defaultWidth = 500.dp,
    defaultHeight = 500.dp,
    viewportWidth = 500f,
    viewportHeight = 500f
).apply {
    addPath(
        pathData = PathParser().parsePathString("M91.5,34.7L0.4,226.2c-0.7,1.5-0.4,3.2,0.8,4.3l246.2,235.9c1.5,1.4,3.7,1.4,5.2,0l246.2-235.9 ...").toNodes(),
        fill = SolidColor(Color(0xFF50AF95)),
        pathFillType = PathFillType.EvenOdd
    )
    addPath(
        pathData = PathParser().parsePathString("M281.6,245.8L281.6,245.8c-1.8,0.1-10.9,0.7-31.3,0.7c-16.2,0-27.7-0.5-31.7-0.7 ...").toNodes(),
        fill = SolidColor(Color.White),
        pathFillType = PathFillType.EvenOdd
    )
}.build()

val WalletIcon: ImageVector
    get() = Builder(
        name = "Wallet",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(21.0f, 18.0f)
            horizontalLineTo(3.0f)
            curveTo(1.9f, 18.0f, 1.0f, 17.1f, 1.0f, 16.0f)
            verticalLineTo(8.0f)
            curveTo(1.0f, 6.9f, 1.9f, 6.0f, 3.0f, 6.0f)
            horizontalLineTo(21.0f)
            curveTo(22.1f, 6.0f, 23.0f, 6.9f, 23.0f, 8.0f)
            verticalLineTo(16.0f)
            curveTo(23.0f, 17.1f, 22.1f, 18.0f, 21.0f, 18.0f)
            close()
            moveTo(11.0f, 8.0f)
            verticalLineTo(16.0f)
            horizontalLineTo(3.0f)
            verticalLineTo(8.0f)
            horizontalLineTo(11.0f)
            close()
            moveTo(19.5f, 10.5f)
            curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
            reflectiveCurveToRelative(-0.67f, -1.5f, -1.5f, -1.5f)
            reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
            reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f)
            close()
        }
    }.build()


val MoneyIcon: ImageVector
    get() = Builder(
        name = "Money",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            curveToRelative(0.0f, 5.52f, 4.48f, 10.0f, 10.0f, 10.0f)
            curveToRelative(5.52f, 0.0f, 10.0f, -4.48f, 10.0f, -10.0f)
            curveTo(22.0f, 6.48f, 17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(14.0f, 16.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }.build()

val ReceiveArrowIcon: ImageVector
    get() = Builder(
        name = "ReceiveArrow",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(18.99f, 16.59f)
            lineTo(13.41f, 11.0f)
            lineTo(18.99f, 5.41f)
            lineTo(17.58f, 4.0f)
            lineTo(12.0f, 9.58f)
            lineTo(6.41f, 4.0f)
            lineTo(5.0f, 5.41f)
            lineTo(10.59f, 11.0f)
            lineTo(5.0f, 16.59f)
            lineTo(6.41f, 18.0f)
            lineTo(12.0f, 12.41f)
            lineTo(17.58f, 18.0f)
            close()
        }
    }.build()

