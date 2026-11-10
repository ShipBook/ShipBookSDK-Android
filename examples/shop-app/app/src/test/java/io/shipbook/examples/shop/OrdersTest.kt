package io.shipbook.examples.shop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OrdersTest {
    @Test
    fun pickupOrderHasStoreButNoAddress() {
        val order = Orders.byId("4812")
        assertEquals(Fulfillment.PICKUP, order.fulfillment)
        assertNull(order.address)
        assertEquals("Downtown store", order.store)
    }

    @Test
    fun deliveryOrdersHaveAddresses() {
        Orders.all.filter { it.fulfillment == Fulfillment.DELIVERY }.forEach { assertNotNull(it.id, it.address) }
        assertEquals(2, Orders.all.count { it.fulfillment == Fulfillment.DELIVERY })
    }
}
