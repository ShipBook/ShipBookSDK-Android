package io.shipbook.shipbookexample

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.shipbook.shipbooksdk.Log
import io.shipbook.shipbooksdk.Models.Severity
import io.shipbook.shipbooksdk.ShipBook
import kotlinx.android.synthetic.main.activity_main.*

/*
 *
 * Created by Elisha Sterngold on 11/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

class MainActivity : FragmentActivity() {
    val log = ShipBook.getLogger("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        log.e("error message")
        log.w("warning message")
        log.i("info message")
        log.d("debug message")
        log.v("verbose message")

        LogWrapper.d("wrapper", "wrapper debug message")
        Log.message("fullMessage", "debug message", Severity.Debug, null, "on", "main", 31, "MAIN")
        Log.w(null , "no tag")
        Log.message("message severity number", "debug message", Severity.fromInt(3), null, "on", "main", 31, "MAIN")
        ShipBook.screen("main screen")

        button.setOnClickListener {
            log.v("pressed button")
            val activity = Intent(this, SecondActivity::class.java)
            startActivity(activity)
        }

        buttonCrash.setOnClickListener {
            val temp = 0
            val test = 2
            @Suppress("DIVISION_BY_ZERO")
            log.d("${test/temp} the crash log")
        }
    }
}
