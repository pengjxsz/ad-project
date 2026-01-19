package com.composables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val menuEarning: ImageVector
    get() {
        if (_menuEarning != null) return _menuEarning!!
        
        _menuEarning = ImageVector.Builder(
            name = "menuEarning",
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
                moveTo(12f, 2f)
                verticalLineTo(22f)
            }
            path(
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17f, 5f)
                horizontalLineTo(9.5f)
                curveTo(8.57174f, 5f, 7.6815f, 5.36875f, 7.02513f, 6.02513f)
                curveTo(6.36875f, 6.6815f, 6f, 7.57174f, 6f, 8.5f)
                curveTo(6f, 9.42826f, 6.36875f, 10.3185f, 7.02513f, 10.9749f)
                curveTo(7.6815f, 11.6313f, 8.57174f, 12f, 9.5f, 12f)
                horizontalLineTo(14.5f)
                curveTo(15.4283f, 12f, 16.3185f, 12.3687f, 16.9749f, 13.0251f)
                curveTo(17.6313f, 13.6815f, 18f, 14.5717f, 18f, 15.5f)
                curveTo(18f, 16.4283f, 17.6313f, 17.3185f, 16.9749f, 17.9749f)
                curveTo(16.3185f, 18.6313f, 15.4283f, 19f, 14.5f, 19f)
                horizontalLineTo(6f)
            }
        }.build()
        
        return _menuEarning!!
    }

private var _menuEarning: ImageVector? = null

