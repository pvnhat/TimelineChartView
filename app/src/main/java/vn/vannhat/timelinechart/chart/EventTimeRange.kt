package vn.vannhat.timelinechart.chart

import kotlin.math.max

data class EventTimeRange(val startMinute: Int, val endMinute: Int) {

    fun isConflict(range: EventTimeRange): Boolean {
        return startMinute >= range.startMinute && startMinute < range.endMinute ||
            endMinute > range.startMinute && endMinute <= range.endMinute ||
            range.startMinute in startMinute until endMinute ||
            range.endMinute in (startMinute + 1)..endMinute
    }
}

data class EventColumnSpan(var startColumn: Int, var endColumn: Int)

data class EventColumnSpansHelper(private val timeRanges: List<EventTimeRange>) {
    val columnSpans = mutableListOf<EventColumnSpan>()
    var columnCount = 0

    init {
        timeRanges.forEachIndexed { index, _ ->
            findStartColumn(index)
        }
        timeRanges.forEachIndexed { index, _ ->
            findEndColumn(index)
        }
    }

    private fun findStartColumn(position: Int) {
        timeRanges.forEachIndexed { index, _ ->
            if (isColumnEmpty(index, position)) {
                columnSpans.add(EventColumnSpan(index, index + 1))
                columnCount = max(columnCount, index + 1)
                return
            }
        }
    }

    private fun findEndColumn(position: Int) {
        val columnSpan = columnSpans[position]
        for (i in columnSpan.endColumn until columnCount) {
            if (isColumnEmpty(i, position)) return
        }
        columnSpan.endColumn++
    }

    private fun isColumnEmpty(column: Int, position: Int): Boolean {
        val timeRange = timeRanges[position]
        columnSpans.forEachIndexed each@{ index, eventColumnSpan ->
            if (position == index) return@each
            if (eventColumnSpan.startColumn == column && timeRanges[index].isConflict(timeRange)) {
                return false
            }
        }
        return true
    }
}
