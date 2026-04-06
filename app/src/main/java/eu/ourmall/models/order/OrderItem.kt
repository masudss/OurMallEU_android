package eu.ourmall.models.order

import java.math.BigDecimal

data class OrderItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val productID: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val selectedOptions: Map<String, String>,
    val status: ItemStatus
) {
    val selectedOptionsText: String
        get() = selectedOptions.values.joinToString(", ")

    val subtotal: BigDecimal
        get() = unitPrice.multiply(BigDecimal(quantity))
}
