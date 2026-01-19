package com.composables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val menuWallet: ImageVector
    get() {
        if (_menuWallet != null) return _menuWallet!!
        
        _menuWallet = ImageVector.Builder(
            name = "menuWallet",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 7f)
                verticalLineTo(4f)
                curveTo(19f, 3.73478f, 18.8946f, 3.48043f, 18.7071f, 3.29289f)
                curveTo(18.5196f, 3.10536f, 18.2652f, 3f, 18f, 3f)
                horizontalLineTo(5f)
                curveTo(4.46957f, 3f, 3.96086f, 3.21071f, 3.58579f, 3.58579f)
                curveTo(3.21071f, 3.96086f, 3f, 4.46957f, 3f, 5f)
                curveTo(3f, 5.53043f, 3.21071f, 6.03914f, 3.58579f, 6.41421f)
                curveTo(3.96086f, 6.78929f, 4.46957f, 7f, 5f, 7f)
                horizontalLineTo(20f)
                curveTo(20.2652f, 7f, 20.5196f, 7.10536f, 20.7071f, 7.29289f)
                curveTo(20.8946f, 7.48043f, 21f, 7.73478f, 21f, 8f)
                verticalLineTo(12f)
                moveTo(21f, 12f)
                horizontalLineTo(18f)
                curveTo(17.4696f, 12f, 16.9609f, 12.2107f, 16.5858f, 12.5858f)
                curveTo(16.2107f, 12.9609f, 16f, 13.4696f, 16f, 14f)
                curveTo(16f, 14.5304f, 16.2107f, 15.0391f, 16.5858f, 15.4142f)
                curveTo(16.9609f, 15.7893f, 17.4696f, 16f, 18f, 16f)
                horizontalLineTo(21f)
                curveTo(21.2652f, 16f, 21.5196f, 15.8946f, 21.7071f, 15.7071f)
                curveTo(21.8946f, 15.5196f, 22f, 15.2652f, 22f, 15f)
                verticalLineTo(13f)
                curveTo(22f, 12.7348f, 21.8946f, 12.4804f, 21.7071f, 12.2929f)
                curveTo(21.5196f, 12.1054f, 21.2652f, 12f, 21f, 12f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 19.5304f, 3.21071f, 20.0391f, 3.58579f, 20.4142f)
                curveTo(3.96086f, 20.7893f, 4.46957f, 21f, 5f, 21f)
                horizontalLineTo(20f)
                curveTo(20.2652f, 21f, 20.5196f, 20.8946f, 20.7071f, 20.7071f)
                curveTo(20.8946f, 20.5196f, 21f, 20.2652f, 21f, 20f)
                verticalLineTo(16f)
            }
        }.build()
        
        return _menuWallet!!
    }

private var _menuWallet: ImageVector? = null

