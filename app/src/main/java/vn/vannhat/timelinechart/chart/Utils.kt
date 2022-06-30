package vn.vannhat.timelinechart.chart

import java.text.SimpleDateFormat
import java.util.*

fun Calendar?.toStringFormat(
    outputPattern: String = FORMAT_HH_MM,
    locale: Locale = Locale.getDefault()
): String {
    if (this == null) return ""
    val outputFormat = SimpleDateFormat(outputPattern, locale)
    return try {
        outputFormat.format(this.time)
    } catch (ignored: Exception) {
        ""
    }
}

fun getMinusOfDayFromStr(
    time: String?,
    format: String = FORMAT_SERVER_RESPONSE_WITHOUT_TIME_ZONE,
    locale: Locale = Locale.getDefault()
): Int? {
    if (time.isNullOrEmpty()) return null
    val inputFormat = SimpleDateFormat(format, locale)
    return try {
        val date = inputFormat.parse(time) ?: return null
        val cal = Calendar.getInstance().apply {
            setTime(date)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
        }
        cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    } catch (ignored: Exception) {
        null
    }
}

const val FORMAT_HH_MM = "HH:mm aa"
const val FORMAT_SERVER_RESPONSE_WITHOUT_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS"
