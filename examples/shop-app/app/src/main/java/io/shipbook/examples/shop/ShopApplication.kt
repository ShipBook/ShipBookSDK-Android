package io.shipbook.examples.shop

import android.app.Application
import io.shipbook.shipbooksdk.ShipBook

class ShopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ShipBook.registerUser("sam.taylor", fullName = "Sam Taylor")
        ShipBook.start(this, BuildConfig.SHIPBOOK_APP_ID, BuildConfig.SHIPBOOK_APP_KEY)
    }
}
