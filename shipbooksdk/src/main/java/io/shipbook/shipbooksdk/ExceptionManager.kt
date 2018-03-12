package io.shipbook.shipbooksdk

import io.shipbook.shipbooksdk.Models.Exception
import io.shipbook.shipbooksdk.Util.toInternal

/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

object ExceptionManager {
    private val TAG = ExceptionManager::class.java.simpleName
    fun start(hasException: Boolean = true) {
        if (!hasException)
            return

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            InnerLog.e(TAG,"catch uncaught exception")
            val stackTrace = throwable.stackTrace.toInternal()
            val exception = Exception(throwable.javaClass.name, throwable.message, stackTrace)
            LogManager.push(exception)

            if (oldHandler != null) oldHandler.uncaughtException(thread, throwable)
            else System.exit(1)
        }
    }


}