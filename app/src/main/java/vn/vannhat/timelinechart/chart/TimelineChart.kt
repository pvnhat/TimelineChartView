package vn.vannhat.timelinechart.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import vn.vannhat.timelinechart.EvenModel
import vn.vannhat.timelinechart.R
import java.util.*
import kotlin.math.max
import kotlin.math.min

class TimelineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val hourDividerRects: List<Rect>
    private val halfHourDividerRects: List<Rect>
    private val verticalDividerRects: List<Rect>

    private val startHour: Int
    private val endHour: Int
    private val startMinute: Int
    private val endMinute: Int
    private val minuteCount: Int
    private val hourLabelsCount: Int
    private val hourDividersCount: Int
    private val halfHourDividersCount: Int
    private val hourLineHeight: Int
    private val haftHourHeight: Int
    private val hourLabelWidth: Int
    private val eventMargin: Int
    private val verticalColumnCount: Int
    private val verticalColumnWidth: Int

    private val masterLinePaint: Paint
    private val haftHourLinePaint: Paint


    var chartHours = listOf<ChartTime>()
    private var chartEvents = listOf<ChartEvent>()
    private var eventColumnSpansHelper: EventColumnSpansHelper? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimelineChart)
        startHour = max(
            typedArray.getInt(R.styleable.TimelineChart_tlcStartHour, MIN_START_HOUR),
            MIN_START_HOUR
        )
        endHour = min(
            typedArray.getInt(R.styleable.TimelineChart_tlcEndHour, MAX_END_HOUR),
            MAX_END_HOUR
        )
        hourLineHeight =
            typedArray.getDimensionPixelSize(R.styleable.TimelineChart_tlcHourLineHeight, 0)
        haftHourHeight =
            typedArray.getDimensionPixelSize(R.styleable.TimelineChart_tlcHaftHourHeight, 0)
        val hourLineColor =
            typedArray.getColor(R.styleable.TimelineChart_tlcHourLineColor, 0)
        val haftHourLineColor =
            typedArray.getColor(R.styleable.TimelineChart_tlcHaftHourLineColor, 0)
        hourLabelWidth =
            typedArray.getDimensionPixelSize(R.styleable.TimelineChart_tlcHourLabelWidth, 0)
        eventMargin =
            typedArray.getDimensionPixelSize(R.styleable.TimelineChart_tlcEventMargin, 0)
        verticalColumnCount =
            max(typedArray.getInteger(R.styleable.TimelineChart_tlcVerticalColumnCount, 0), 0)
        verticalColumnWidth =
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineChart_tlcVerticalColumnWidth,
                DEFAULT_COLUMN_WIDTH
            )

        startMinute = startHour * MINUTES_PER_HOUR
        endMinute = endHour * MINUTES_PER_HOUR

        val hourCount = endHour - startHour
        minuteCount = hourCount * MINUTES_PER_HOUR
        hourLabelsCount = hourCount + 1
        hourDividersCount = hourCount + 1
        halfHourDividersCount = hourCount

        hourDividerRects = List(hourDividersCount) { Rect() }
        halfHourDividerRects = List(halfHourDividersCount) { Rect() }
        verticalDividerRects = List(verticalColumnCount + 1) { Rect() }

        masterLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = hourLineColor
        }
        haftHourLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = haftHourLineColor
        }
        setHourLabels()
        setWillNotDraw(false)
        typedArray.recycle()
    }

    private fun setHourLabels() {
        if (startHour >= endHour) return
        val calendar = Calendar.getInstance()
        chartHours = List(hourLabelsCount) { h ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, 0)
            val hour = calendar.toStringFormat()
            val tv = LayoutInflater.from(context)
                .inflate(R.layout.textview_chart_time, this, false) as AppCompatTextView
            tv.text = hour
            addView(tv)
            ChartTime(hour, tv, Rect())
        }
    }

    fun setEvents(events: List<EvenModel>, eventClick: (event: EvenModel) -> Unit) {
        val oldEvents = chartEvents.toList()
        var oldEventRemaining = oldEvents.size
        chartEvents = List(events.size) { i ->
            val e = events[i]

            // Reuse old inflated view if it exists
            val eventView = if (oldEventRemaining > 0) {
                oldEvents[--oldEventRemaining].view
            } else {
                LayoutInflater.from(this.context).inflate(R.layout.layout_chart_event, this, false)
            }
            val tvTitle = eventView.findViewById<TextView>(R.id.tvTitle)
            tvTitle.text = e.title
            eventView.setOnClickListener {
                eventClick(e)
            }
            val evenColor = Color.parseColor(e.color)
            eventView.setBackgroundColor(evenColor)
            addView(eventView)
            ChartEvent(
                title = e.title,
                startMinusOfDay = getMinusOfDayFromStr(e.startTime) ?: 0,
                endMinusOfDay = getMinusOfDayFromStr(e.endTime) ?: 0,
                view = eventView,
                rect = Rect(),
                color = evenColor
            )
        }
        eventColumnSpansHelper = EventColumnSpansHelper(chartEvents.map {
            EventTimeRange(it.startMinusOfDay, it.endMinusOfDay)
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        validateChildViews()

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var firstDividerTop = 0
        var lastDividerMarginBottom = 0

        chartHours.forEachIndexed { index, chartTime ->
            val v = chartTime.view
            measureChild(v, widthMeasureSpec, heightMeasureSpec)
            if (index == 0) firstDividerTop = v.measuredHeight / 2
            if (index == chartHours.size - 1) lastDividerMarginBottom = v.measuredHeight / 2
        }

        val usableHeight = (hourDividerRects.size + halfHourDividerRects.size - 1) * haftHourHeight
        val usableWidth = (verticalDividerRects.size - 1) * verticalColumnWidth * 2
        val minuteHeight: Float = usableHeight.toFloat() / minuteCount
        firstDividerTop += paddingTop
        val verticalPadding =
            firstDividerTop + lastDividerMarginBottom + paddingBottom + hourLineHeight
        val horizontalPadding = paddingStart + paddingEnd + hourLineHeight
        val measuredHeight = usableHeight + verticalPadding
        val measuredWidth = usableWidth + horizontalPadding + hourLabelWidth

        // Calculate the horizontal positions of the dividers
        val hourLabelStart = paddingLeft
        val hourLabelEnd = hourLabelStart + hourLabelWidth
        val dividerEnd = max(measuredWidth, this.measuredWidth) - paddingRight
        val labelHeight = chartHours.firstOrNull()?.view?.measuredHeight ?: 0
        val dividerBottom = measuredHeight - paddingBottom - labelHeight / 2

        // Set the rects for hour labels, dividers, and events
        setHourLabelRects(hourLabelStart, hourLabelEnd, firstDividerTop)
        setDividerRects(firstDividerTop, hourLabelEnd, dividerEnd, dividerBottom)
        setEventRects(firstDividerTop, minuteHeight, hourLabelEnd, dividerEnd)

        // Measure the views
        measureHourLabels()
        measureEvents()

        setMeasuredDimension(max(measuredWidth, widthMeasureSpec), measuredHeight)
    }

    private fun measureEvents() {
        chartEvents.forEach {
            measureExactly(it.view, it.rect)
        }
    }

    private fun measureHourLabels() {
        chartHours.forEach {
            measureExactly(it.view, it.rect)
        }
    }

    private fun measureExactly(view: View, rect: Rect) {
        view.measure(
            MeasureSpec.makeMeasureSpec(rect.right - rect.left, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(rect.bottom - rect.top, MeasureSpec.EXACTLY)
        )
    }

    private fun setEventRects(
        firstDividerTop: Int,
        minuteHeight: Float,
        dividerStart: Int,
        dividerEnd: Int
    ) {
        if (chartEvents.isEmpty() || eventColumnSpansHelper == null) return
        chartEvents.forEachIndexed { index, e ->
            val eventColumnSpan = eventColumnSpansHelper?.columnSpans?.getOrNull(index) ?: return
            var eventStartMinute = max(startMinute, e.startMinusOfDay)
            var duration = min(endMinute, e.endMinusOfDay) - eventStartMinute
            if (duration < MIN_DURATION_MINUTES) {
                duration = MIN_DURATION_MINUTES
                eventStartMinute = endMinute - duration
            }

            val start = eventColumnSpan.startColumn * e.columnWidth + dividerStart + eventMargin
            val end = start + e.columnWidth - eventMargin * 2
            val topOffset = (eventStartMinute - startMinute) * minuteHeight
            val top = firstDividerTop + topOffset + hourLineHeight + eventMargin
            val bottom = top + (duration * minuteHeight) - eventMargin * 2 - hourLineHeight
            e.rect.set(start.toInt(), top.toInt(), end.toInt(), bottom.toInt())
        }
    }

    private fun setDividerRects(
        firstDividerTop: Int,
        dividerStart: Int,
        dividerEnd: Int,
        dividerBottom: Int
    ) {
        hourDividerRects.forEachIndexed { index, rect ->
            val top = firstDividerTop + index * 2 * haftHourHeight
            val bottom = top + hourLineHeight
            rect.set(dividerStart, top, dividerEnd, bottom)
        }
        halfHourDividerRects.forEachIndexed { index, rect ->
            val top = firstDividerTop + (index * 2 + 1) * haftHourHeight
            val bottom = top + hourLineHeight
            rect.set(dividerStart, top, dividerEnd, bottom)
        }
        verticalDividerRects.forEachIndexed { index, rect ->
            val start = dividerStart + index * 2 * verticalColumnWidth
            val end = start + hourLineHeight
            rect.set(start, firstDividerTop, end, dividerBottom)
        }
    }

    private fun setHourLabelRects(hourLabelStart: Int, hourLabelEnd: Int, firstDividerTop: Int) {
        chartHours.forEachIndexed { index, chartTime ->
            val height = chartTime.view.measuredHeight
            val top = firstDividerTop + haftHourHeight * index * 2 - height / 2
            val bottom = top + height
            chartTime.rect.set(hourLabelStart, top, hourLabelEnd, bottom)
        }
    }

    private fun validateChildViews() {
        when {
            chartHours.isEmpty() -> {
                throw IllegalStateException("Please set valid value for tlcStartHour and tlcEndHour")
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        hourDividerRects.forEach {
            canvas?.drawRect(it, masterLinePaint)
        }
        halfHourDividerRects.forEach {
            canvas?.drawRect(it, haftHourLinePaint)
        }
        verticalDividerRects.forEach {
            canvas?.drawRect(it, masterLinePaint)
        }
    }

    override fun shouldDelayChildPressedState(): Boolean = false

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        chartHours.forEach {
            it.view.layout(it.rect.left, it.rect.top, it.rect.right, it.rect.bottom)
        }
        chartEvents.forEach {
            it.view.layout(it.rect.left, it.rect.top, it.rect.right, it.rect.bottom)
        }
    }

    companion object {
        private const val MIN_START_HOUR = 0
        private const val MAX_END_HOUR = 24
        private const val MINUTES_PER_HOUR = 60
        private const val MIN_DURATION_MINUTES = 15
        private const val DEFAULT_COLUMN_WIDTH = 300
    }
}
