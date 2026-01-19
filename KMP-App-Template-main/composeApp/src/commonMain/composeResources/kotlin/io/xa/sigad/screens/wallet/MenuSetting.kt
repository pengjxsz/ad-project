package com.composables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val menuSetting: ImageVector
    get() {
        if (_menuSetting != null) return _menuSetting!!
        
        _menuSetting = ImageVector.Builder(
            name = "menuSetting",
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
                moveTo(9.671f, 4.13603f)
                curveTo(9.7261f, 3.55637f, 9.99533f, 3.01807f, 10.4261f, 2.62631f)
                curveTo(10.8569f, 2.23454f, 11.4182f, 2.01746f, 12.0005f, 2.01746f)
                curveTo(12.5828f, 2.01746f, 13.1441f, 2.23454f, 13.5749f, 2.62631f)
                curveTo(14.0057f, 3.01807f, 14.2749f, 3.55637f, 14.33f, 4.13603f)
                curveTo(14.3631f, 4.51048f, 14.486f, 4.87145f, 14.6881f, 5.18837f)
                curveTo(14.8903f, 5.50529f, 15.1659f, 5.76884f, 15.4915f, 5.95671f)
                curveTo(15.8171f, 6.14457f, 16.1831f, 6.25123f, 16.5587f, 6.26765f)
                curveTo(16.9343f, 6.28407f, 17.3082f, 6.20977f, 17.649f, 6.05103f)
                curveTo(18.1781f, 5.81081f, 18.7777f, 5.77605f, 19.331f, 5.95352f)
                curveTo(19.8843f, 6.13098f, 20.3518f, 6.50798f, 20.6425f, 7.01113f)
                curveTo(20.9332f, 7.51429f, 21.0263f, 8.1076f, 20.9036f, 8.6756f)
                curveTo(20.781f, 9.2436f, 20.4514f, 9.74565f, 19.979f, 10.084f)
                curveTo(19.6714f, 10.2999f, 19.4203f, 10.5866f, 19.2469f, 10.9201f)
                curveTo(19.0736f, 11.2535f, 18.983f, 11.6237f, 18.983f, 11.9995f)
                curveTo(18.983f, 12.3753f, 19.0736f, 12.7456f, 19.2469f, 13.079f)
                curveTo(19.4203f, 13.4124f, 19.6714f, 13.6992f, 19.979f, 13.915f)
                curveTo(20.4514f, 14.2534f, 20.781f, 14.7555f, 20.9036f, 15.3235f)
                curveTo(21.0263f, 15.8915f, 20.9332f, 16.4848f, 20.6425f, 16.9879f)
                curveTo(20.3518f, 17.4911f, 19.8843f, 17.8681f, 19.331f, 18.0455f)
                curveTo(18.7777f, 18.223f, 18.1781f, 18.1883f, 17.649f, 17.948f)
                curveTo(17.3082f, 17.7893f, 16.9343f, 17.715f, 16.5587f, 17.7314f)
                curveTo(16.1831f, 17.7478f, 15.8171f, 17.8545f, 15.4915f, 18.0424f)
                curveTo(15.1659f, 18.2302f, 14.8903f, 18.4938f, 14.6881f, 18.8107f)
                curveTo(14.486f, 19.1276f, 14.3631f, 19.4886f, 14.33f, 19.863f)
                curveTo(14.2749f, 20.4427f, 14.0057f, 20.981f, 13.5749f, 21.3727f)
                curveTo(13.1441f, 21.7645f, 12.5828f, 21.9816f, 12.0005f, 21.9816f)
                curveTo(11.4182f, 21.9816f, 10.8569f, 21.7645f, 10.4261f, 21.3727f)
                curveTo(9.99533f, 20.981f, 9.7261f, 20.4427f, 9.671f, 19.863f)
                curveTo(9.63794f, 19.4884f, 9.5151f, 19.1273f, 9.31286f, 18.8103f)
                curveTo(9.11063f, 18.4933f, 8.83497f, 18.2296f, 8.50923f, 18.0418f)
                curveTo(8.18349f, 17.8539f, 7.81727f, 17.7472f, 7.44158f, 17.7309f)
                curveTo(7.06589f, 17.7146f, 6.6918f, 17.7891f, 6.351f, 17.948f)
                curveTo(5.82189f, 18.1883f, 5.22233f, 18.223f, 4.669f, 18.0455f)
                curveTo(4.11567f, 17.8681f, 3.64817f, 17.4911f, 3.35748f, 16.9879f)
                curveTo(3.06679f, 16.4848f, 2.97371f, 15.8915f, 3.09636f, 15.3235f)
                curveTo(3.219f, 14.7555f, 3.5486f, 14.2534f, 4.021f, 13.915f)
                curveTo(4.32862f, 13.6992f, 4.57973f, 13.4124f, 4.75309f, 13.079f)
                curveTo(4.92645f, 12.7456f, 5.01695f, 12.3753f, 5.01695f, 11.9995f)
                curveTo(5.01695f, 11.6237f, 4.92645f, 11.2535f, 4.75309f, 10.9201f)
                curveTo(4.57973f, 10.5866f, 4.32862f, 10.2999f, 4.021f, 10.084f)
                curveTo(3.54926f, 9.74547f, 3.22025f, 9.24362f, 3.0979f, 8.67601f)
                curveTo(2.97555f, 8.1084f, 3.06861f, 7.51557f, 3.35898f, 7.01274f)
                curveTo(3.64936f, 6.50991f, 4.11631f, 6.13301f, 4.66909f, 5.95527f)
                curveTo(5.22187f, 5.77753f, 5.82098f, 5.81166f, 6.35f, 6.05103f)
                curveTo(6.69076f, 6.20977f, 7.06474f, 6.28407f, 7.4403f, 6.26765f)
                curveTo(7.81586f, 6.25123f, 8.18193f, 6.14457f, 8.50754f, 5.95671f)
                curveTo(8.83314f, 5.76884f, 9.10869f, 5.50529f, 9.31086f, 5.18837f)
                curveTo(9.51304f, 4.87145f, 9.63588f, 4.51048f, 9.669f, 4.13603f)
            }
            path(
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 15f)
                curveTo(13.6569f, 15f, 15f, 13.6569f, 15f, 12f)
                curveTo(15f, 10.3431f, 13.6569f, 9f, 12f, 9f)
                curveTo(10.3431f, 9f, 9f, 10.3431f, 9f, 12f)
                curveTo(9f, 13.6569f, 10.3431f, 15f, 12f, 15f)
                close()
            }
        }.build()
        
        return _menuSetting!!
    }

private var _menuSetting: ImageVector? = null

