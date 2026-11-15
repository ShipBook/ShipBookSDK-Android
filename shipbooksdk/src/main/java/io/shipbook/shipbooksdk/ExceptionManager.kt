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
    private val frameworkPrefixes = listOf("android.", "java.", "com.android.", "androidx.")
    fun start(hasException: Boolean = true) {
        if (!hasException)
            return

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            InnerLog.e(TAG,"catch uncaught exception")
            // ActivityThread wraps lifecycle crashes in a throwable with no app frames, so report the first one in the chain that has them.
            val seen = HashSet<Throwable>()
            val chain = generateSequence(throwable) { it.cause }.takeWhile { seen.add(it) }.toList()
            val reported = chain.firstOrNull { t -> t.stackTrace.any { frame -> frameworkPrefixes.none { frame.className.startsWith(it) } } } ?: chain.last()
            val exception = Exception(reported.javaClass.name, reported.message ?: throwable.message, reported.stackTrace.toInternal())
            LogManager.push(exception)

            if (oldHandler != null) oldHandler.uncaughtException(thread, throwable)
            else System.exit(1)
        }
    }


}