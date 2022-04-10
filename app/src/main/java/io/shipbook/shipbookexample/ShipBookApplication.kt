package io.shipbook.shipbookexample

import android.app.Application
import io.shipbook.shipbooksdk.ShipBook

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
        ShipBook.start(this,"YOUR_APPID", "YOUR_APPKEY")
        ShipBook.addWrapperClass(LogWrapper::class.java.name)
        ShipBook.registerUser("1")
    }
}