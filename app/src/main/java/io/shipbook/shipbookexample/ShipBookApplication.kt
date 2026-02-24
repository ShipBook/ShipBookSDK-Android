package io.shipbook.shipbookexample

import android.app.Application
import io.shipbook.shipbooksdk.ShipBook
import io.shipbook.shipbookexample.BuildConfig

/*
 *
 * Created by Elisha Sterngold on 22/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

class ShipBookApplication : Application() {
    @Override
    override fun onCreate() {
        super.onCreate()
        ShipBook.enableInnerLog(true)
        ShipBook.ignoreViews(R.id.ignore, R.id.password)
        if (BuildConfig.SHIPBOOK_URL.isNotEmpty()) ShipBook.setConnectionUrl(BuildConfig.SHIPBOOK_URL)
        ShipBook.start(this, BuildConfig.SHIPBOOK_APP_ID, BuildConfig.SHIPBOOK_APP_KEY)
        ShipBook.addWrapperClass(LogWrapper::class.java.name)
        ShipBook.registerUser("1")
    }
}