package com.composables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val menuPixart: ImageVector
    get() {
        if (_menuPixart != null) return _menuPixart!!
        
        _menuPixart = ImageVector.Builder(
            name = "menuPixart",
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
                moveTo(12f, 22f)
                curveTo(9.34784f, 22f, 6.8043f, 20.9464f, 4.92893f, 19.0711f)
                curveTo(3.05357f, 17.1957f, 2f, 14.6522f, 2f, 12f)
                curveTo(2f, 9.34784f, 3.05357f, 6.8043f, 4.92893f, 4.92893f)
                curveTo(6.8043f, 3.05357f, 9.34784f, 2f, 12f, 2f)
                curveTo(14.6522f, 2f, 17.1957f, 2.94821f, 19.0711f, 4.63604f)
                curveTo(20.9464f, 6.32387f, 22f, 8.61305f, 22f, 11f)
                curveTo(22f, 12.3261f, 21.4732f, 13.5979f, 20.5355f, 14.5355f)
                curveTo(19.5979f, 15.4732f, 18.3261f, 16f, 17f, 16f)
                horizontalLineTo(14.75f)
                curveTo(14.425f, 16f, 14.1064f, 16.0905f, 13.83f, 16.2614f)
                curveTo(13.5535f, 16.4322f, 13.3301f, 16.6767f, 13.1848f, 16.9674f)
                curveTo(13.0394f, 17.2581f, 12.9779f, 17.5835f, 13.0071f, 17.9072f)
                curveTo(13.0363f, 18.2308f, 13.155f, 18.54f, 13.35f, 18.8f)
                lineTo(13.65f, 19.2f)
                curveTo(13.845f, 19.46f, 13.9637f, 19.7692f, 13.9929f, 20.0928f)
                curveTo(14.0221f, 20.4165f, 13.9606f, 20.7419f, 13.8152f, 21.0326f)
                curveTo(13.6699f, 21.3233f, 13.4465f, 21.5678f, 13.17f, 21.7386f)
                curveTo(12.8936f, 21.9095f, 12.575f, 22f, 12.25f, 22f)
                horizontalLineTo(12f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF6B7280)),
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(13.5f, 7f)
                curveTo(13.7761f, 7f, 14f, 6.77614f, 14f, 6.5f)
                curveTo(14f, 6.22386f, 13.7761f, 6f, 13.5f, 6f)
                curveTo(13.2239f, 6f, 13f, 6.22386f, 13f, 6.5f)
                curveTo(13f, 6.77614f, 13.2239f, 7f, 13.5f, 7f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF6B7280)),
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.5f, 11f)
                curveTo(17.7761f, 11f, 18f, 10.7761f, 18f, 10.5f)
                curveTo(18f, 10.2239f, 17.7761f, 10f, 17.5f, 10f)
                curveTo(17.2239f, 10f, 17f, 10.2239f, 17f, 10.5f)
                curveTo(17f, 10.7761f, 17.2239f, 11f, 17.5f, 11f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF6B7280)),
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.5f, 13f)
                curveTo(6.77614f, 13f, 7f, 12.7761f, 7f, 12.5f)
                curveTo(7f, 12.2239f, 6.77614f, 12f, 6.5f, 12f)
                curveTo(6.22386f, 12f, 6f, 12.2239f, 6f, 12.5f)
                curveTo(6f, 12.7761f, 6.22386f, 13f, 6.5f, 13f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF6B7280)),
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8.5f, 8f)
                curveTo(8.77614f, 8f, 9f, 7.77614f, 9f, 7.5f)
                curveTo(9f, 7.22386f, 8.77614f, 7f, 8.5f, 7f)
                curveTo(8.22386f, 7f, 8f, 7.22386f, 8f, 7.5f)
                curveTo(8f, 7.77614f, 8.22386f, 8f, 8.5f, 8f)
                close()
            }
        }.build()
        
        return _menuPixart!!
    }

private var _menuPixart: ImageVector? = null

