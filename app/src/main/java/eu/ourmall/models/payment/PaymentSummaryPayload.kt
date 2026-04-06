package eu.ourmall.models.payment

import java.math.BigDecimal

data class PaymentSummaryPayload(
    val subtotal: BigDecimal,
    val discount: BigDecimal,
    val vat: BigDecimal,
    val grandTotal: BigDecimal
)
