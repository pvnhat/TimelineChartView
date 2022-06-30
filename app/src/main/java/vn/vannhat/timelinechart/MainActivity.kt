package vn.vannhat.timelinechart

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import vn.vannhat.timelinechart.chart.TimelineChart

class MainActivity : AppCompatActivity() {
    private var timelineChart: TimelineChart? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timelineChart = findViewById(R.id.tlcChart)
        timelineChart?.setEvents(fakedData()) {
            Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fakedData() = listOf(
        EvenModel(
            1,
            "test1",
            "2022-06-30T12:00:00.000Z",
            "2022-06-30T13:00:00.000Z",
            "#03A9F4"
        ),
        EvenModel(
            4,
            "test4",
            "2022-06-30T12:00:00.000Z",
            "2022-06-30T13:30:00.000Z",
            "#03A9F4"
        ),
        EvenModel(
            2,
            "test2",
            "2022-06-30T14:00:00.000Z",
            "2022-06-30T15:00:00.000Z",
            "#03A9F4"
        ),
        EvenModel(
            3,
            "test3",
            "2022-06-30T18:00:00.000Z",
            "2022-06-30T19:00:00.000Z",
            "#03A9F4"
        ),
        EvenModel(
            4,
            "test4",
            "2022-06-30T01:00:00.000Z",
            "2022-06-30T03:40:00.000Z",
            "#03A9F4"
        )
    )
}
