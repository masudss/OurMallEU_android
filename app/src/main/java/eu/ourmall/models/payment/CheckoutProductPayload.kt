package eu.ourmall.models.payment

import java.math.BigDecimal

data class CheckoutProductPayload(
    val productId: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val selectedOptions: Map<String, String>
)
