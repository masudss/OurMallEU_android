package eu.ourmall.models.cart

import java.math.BigDecimal

data class CheckoutTotals(
    val subtotal: BigDecimal,
    val discount: BigDecimal,
    val vat: BigDecimal,
    val grandTotal: BigDecimal
) {
    companion object {
        val empty = CheckoutTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
    }
}
