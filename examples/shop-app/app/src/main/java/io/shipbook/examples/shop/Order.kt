package io.shipbook.examples.shop

enum class Fulfillment { DELIVERY, PICKUP }

data class Address(val street: String, val city: String)

data class Order(
    val id: String,
    val items: String,
    val total: String,
    val fulfillment: Fulfillment,
    val address: Address?,
    val store: String? = null,
)

object Orders {
    val all = listOf(
        Order("4810", "Running shoes", "$89.00", Fulfillment.DELIVERY, Address("12 Elm Street", "Austin")),
        Order("4811", "Wireless headphones", "$129.00", Fulfillment.DELIVERY, Address("48 Oak Avenue", "Austin")),
        Order("4812", "Coffee grinder", "$64.00", Fulfillment.PICKUP, null, store = "Downtown store"),
    )

    fun byId(id: String) = all.first { it.id == id }
}
