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
    val log = ShipBook.getLogger("ShipBookApplication")
    @Override
    override fun onCreate() {
        super.onCreate()
        ShipBook.start(this,"YOUR_APPID", "YOUR_APPKEY")
        log.d("started log")
    }
}