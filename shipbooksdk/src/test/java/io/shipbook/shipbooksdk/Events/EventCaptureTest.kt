package io.shipbook.shipbooksdk.Events

import android.app.Activity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import io.shipbook.shipbooksdk.Appenders.BaseAppender
import io.shipbook.shipbooksdk.Appenders.Config
import io.shipbook.shipbooksdk.Appenders.SBCloudAppender
import io.shipbook.shipbooksdk.LogManager
import io.shipbook.shipbooksdk.Models.ActionEvent
import io.shipbook.shipbooksdk.Models.ActivityEvent
import io.shipbook.shipbooksdk.Models.BaseLog
import io.shipbook.shipbooksdk.Networking.SessionManager
import io.shipbook.shipbooksdk.Util.onSeekBarChangeListener
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config as RobolectricConfig
import java.io.File

@RunWith(RobolectricTestRunner::class)
@RobolectricConfig(sdk = [34])
class EventCaptureTest {
    private val pushed = mutableListOf<BaseLog>()
    private val actions get() = pushed.filterIsInstance<ActionEvent>()
    private lateinit var layout: LinearLayout

    @Before
    fun setUp() {
        LogManager.appenders = mutableMapOf("test" to object : BaseAppender("test", null) {
            override fun update(config: Config?) {}
            override fun push(log: BaseLog) { pushed.add(log) }
        })
        layout = LinearLayout(RuntimeEnvironment.getApplication())
    }

    @After
    fun tearDown() {
        LogManager.appenders = mutableMapOf()
    }

    @Test
    fun aClickIsRecordedOnceHoweverOftenTheActivityStarts() {
        var clicks = 0
        val button = Button(layout.context).apply { text = "Start"; setOnClickListener { clicks++ } }
        layout.addView(button)

        repeat(3) { ActionEventManager.registerViews(layout) }
        button.performClick()

        assertEquals(listOf("onClick" to "Start"), actions.map { it.action to it.senderTitle })
        assertEquals("Button", actions[0].sender)
        assertEquals(1, clicks)
    }

    @Test
    fun aListenerTheAppSetsLaterIsWrappedOnTheNextStart() {
        var first = 0
        var second = 0
        val button = Button(layout.context).apply { text = "Buy"; setOnClickListener { first++ } }
        layout.addView(button)

        ActionEventManager.registerViews(layout)
        button.setOnClickListener { second++ }
        ActionEventManager.registerViews(layout)
        button.performClick()

        assertEquals(1, actions.size)
        assertEquals(0 to 1, first to second)
    }

    @Test
    fun whatTheUserTypesIsNeverSent() {
        val field = EditText(layout.context)
        layout.addView(field)

        repeat(2) { ActionEventManager.registerViews(layout) }
        field.setText("4111 1111 1111 1111")

        assertEquals(listOf("textChanged" to ""), actions.map { it.action to it.senderTitle })
        assertFalse(actions[0].toJson().toString().contains("4111"))
    }

    @Test
    fun aSliderIsRecordedWhenTheUserMovesItNotWhenTheAppDoes() {
        val heard = mutableListOf<Boolean>()
        val slider = SeekBar(layout.context)
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) { heard.add(fromUser) }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        layout.addView(slider)

        repeat(2) { ActionEventManager.registerViews(layout) }
        slider.progress = 40
        assertEquals(emptyList<ActionEvent>(), actions)

        slider.onSeekBarChangeListener!!.onProgressChanged(slider, 60, true)
        assertEquals(listOf("progressChanged"), actions.map { it.action })
        assertEquals(listOf(false, true), heard)
    }

    @Test
    fun aCheckBoxIsRecordedOnce() {
        val heard = mutableListOf<Boolean>()
        val box = CheckBox(layout.context).apply { text = "Remember me"; setOnCheckedChangeListener { _, checked -> heard.add(checked) } }
        layout.addView(box)

        repeat(3) { ActionEventManager.registerViews(layout) }
        box.isChecked = true

        assertEquals(listOf("onCheckedChange" to "Remember me"), actions.map { it.action to it.senderTitle })
        assertEquals(listOf(true), heard)
    }

    @Test
    fun activityEventsAreNotDoubledWhenEventCaptureStartsAgain() {
        SessionManager.application = RuntimeEnvironment.getApplication()

        repeat(2) { EventManager.start() }
        Robolectric.buildActivity(Activity::class.java).setup()

        val events = pushed.filterIsInstance<ActivityEvent>().map { it.event }
        assertEquals(listOf("onActivityCreated", "onActivityStarted", "onActivityResumed"), events)
    }

    @Test
    fun aSavedFileFromAnOlderSdkLoadsWithoutItsFragmentEvents() {
        val baseLog = BaseLog::class.java.name
        val thread = """"threadInfo":{"threadName":"main","threadId":2,"isMain":true}"""
        val file = File.createTempFile("saved", ".log").apply {
            writeText(
                "token: abc\n" +
                """$baseLog: {"type":"fragmentEvent","orderId":1,"time":"2026-11-22T10:00:00.000+0000",$thread,"name":"a.b.HomeFragment","event":"onFragmentResumed"}""" + "\n" +
                """$baseLog: {"type":"screenEvent","orderId":2,"time":"2026-11-22T10:00:01.000+0000",$thread,"name":"Home"}""" + "\n")
        }

        val appender = SBCloudAppender("cloud", null)
        val sessions = appender.loadFromFile(file)
        appender.terminate()

        assertEquals(listOf("screenEvent"), sessions.single().logs.map { it.type })
    }
}
