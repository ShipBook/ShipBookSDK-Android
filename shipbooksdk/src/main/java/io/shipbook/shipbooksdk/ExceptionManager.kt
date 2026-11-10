package io.shipbook.shipbooksdk

import io.shipbook.shipbooksdk.Models.Exception
import io.shipbook.shipbooksdk.Util.toInternal

/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal object ExceptionManager {
    private val TAG = ExceptionManager::class.java.simpleName
    fun start(hasException: Boolean = true) {
        if (!hasException)
            return

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            InnerLog.e(TAG,"catch uncaught exception")
            // ActivityThread wraps lifecycle crashes, so only the root cause carries the app's frames.
            val seen = HashSet<Throwable>()
            val rootCause = generateSequence(throwable) { it.cause }.takeWhile { seen.add(it) }.last()
            val stackTrace = rootCause.stackTrace.toInternal()
            val exception = Exception(rootCause.javaClass.name, rootCause.message ?: throwable.message, stackTrace)
            LogManager.push(exception)

            if (oldHandler != null) oldHandler.uncaughtException(thread, throwable)
            else System.exit(1)
        }
    }


}