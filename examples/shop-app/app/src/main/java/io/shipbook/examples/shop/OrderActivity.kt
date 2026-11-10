package io.shipbook.examples.shop

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.shipbook.shipbooksdk.Log

class OrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        val order = Orders.byId(intent.getStringExtra(EXTRA_ORDER_ID)!!)
        Log.i(TAG, "Order ${order.id}: fulfillment=${order.fulfillment}, store=${order.store}, address=${order.address}")

        title = getString(R.string.order_title, order.id)
        findViewById<TextView>(R.id.items).text = order.items
        findViewById<TextView>(R.id.total).text = order.total
        val where = findViewById<TextView>(R.id.where)
        if (order.fulfillment == Fulfillment.PICKUP) {
            findViewById<TextView>(R.id.whereLabel).setText(R.string.pick_up_at)
            where.text = order.store
        } else {
            where.text = getString(R.string.address, order.address?.street, order.address?.city)
        }

        findViewById<Button>(R.id.track).setOnClickListener {
            Log.i(TAG, "Tracking order ${order.id}")
            val destination = order.address!!
            findViewById<TextView>(R.id.tracking).text = getString(R.string.arriving, destination.street)
        }
    }

    companion object {
        const val EXTRA_ORDER_ID = "orderId"
        private const val TAG = "OrderDetails"
    }
}
