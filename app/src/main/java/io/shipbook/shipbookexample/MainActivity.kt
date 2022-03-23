package io.shipbook.shipbookexample

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.shipbook.shipbookexample.databinding.ActivityMainBinding
import io.shipbook.shipbooksdk.Log
import io.shipbook.shipbooksdk.Models.Severity
import io.shipbook.shipbooksdk.ShipBook

/*
 *
 * Created by Elisha Sterngold on 11/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

class MainActivity : FragmentActivity() {
    val log = ShipBook.getLogger("MainActivity")
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
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

        binding.button.setOnClickListener {
            log.v("pressed button")
            val activity = Intent(this, SecondActivity::class.java)
            startActivity(activity)
        }

        binding.buttonCrash.setOnClickListener {
            val temp = 0
            val test = 2
            @Suppress("DIVISION_BY_ZERO")
            log.d("${test/temp} the crash log")
        }
    }
}
