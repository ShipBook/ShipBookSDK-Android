package io.shipbook.shipbookexample

import io.shipbook.shipbooksdk.Log

/*
 *
 * Created by Elisha Sterngold on 2019-06-17.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

class LogWrapper {
    companion object {
        fun e(tag:String, msg: String, throwable: Throwable? = null) {
            Log.e(tag, msg, throwable)
        }

        fun w(tag:String, msg: String, throwable: Throwable? = null) {
            Log.w(tag, msg, throwable)
        }

        fun i(tag:String, msg: String, throwable: Throwable? = null) {
            Log.i(tag, msg, throwable)
        }

        fun d(tag:String, msg: String, throwable: Throwable? = null) {
            Log.d(tag, msg, throwable)
        }
    }
}