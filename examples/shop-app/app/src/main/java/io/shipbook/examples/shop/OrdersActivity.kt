package io.shipbook.examples.shop

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.shipbook.shipbooksdk.Log

class OrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)
        val list = findViewById<LinearLayout>(R.id.orders)
        Orders.all.forEach { order ->
            val row = layoutInflater.inflate(R.layout.item_order, list, false)
            row.findViewById<TextView>(R.id.title).text = getString(R.string.order_title, order.id)
            row.findViewById<TextView>(R.id.items).text = order.items
            row.findViewById<TextView>(R.id.total).text = order.total
            row.setOnClickListener {
                Log.i(TAG, "Opening order ${order.id}")
                startActivity(Intent(this, OrderActivity::class.java).putExtra(OrderActivity.EXTRA_ORDER_ID, order.id))
            }
            list.addView(row)
        }
        Log.i(TAG, "Loaded ${Orders.all.size} orders")
    }

    companion object {
        private const val TAG = "Orders"
    }
}
