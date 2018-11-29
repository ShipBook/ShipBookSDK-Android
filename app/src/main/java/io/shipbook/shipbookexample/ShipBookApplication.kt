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
        ShipBook.start(this,"YOUR_APPID", "YOUR_APPKEY")
    }
}