package vn.vannhat.timelinechart.chart

import android.graphics.Color
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorRes
import vn.vannhat.timelinechart.R

data class ChartEvent(
    val title: String?,
    val startMinusOfDay: Int,
    val endMinusOfDay: Int,
    val view: View,
    val rect: Rect,
    val color: Int = Color.parseColor(DEFAULT_COLOR_CODE),
    val columnWidth: Double = DEFAULT_WIDTH,
) {
    companion object {
        const val DEFAULT_WIDTH = 200.0
        const val DEFAULT_COLOR_CODE = "#03A9F4"
    }
}
