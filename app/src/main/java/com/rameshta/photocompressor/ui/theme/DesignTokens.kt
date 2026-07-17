package com.rameshta.photocompressor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object AppShapes {
    val small = RoundedCornerShape(10.dp)
    val medium = RoundedCornerShape(16.dp)
    val large = RoundedCornerShape(22.dp)
    val pill = RoundedCornerShape(percent = 50)
}

object AppElevation {
    val none = 0.dp
    val subtle = 1.dp
    val raised = 3.dp
}

object AppMotion {
    const val fast = 120
    const val standard = 180
    const val screen = 240
}

object AppIconSizes {
    val sm = 18.dp
    val md = 22.dp
    val lg = 28.dp
}

object AppTouchTargets {
    val min = 48.dp
    val button = 52.dp
}

object AppSemanticColors {
    val success = Color(0xFF168A55)
    val successDark = Color(0xFF5CE0A0)
    val warning = Color(0xFF9A6400)
    val warningDark = Color(0xFFFFC15A)
}
