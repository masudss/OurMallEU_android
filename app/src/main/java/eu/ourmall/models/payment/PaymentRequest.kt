package eu.ourmall.models.payment

data class PaymentRequest(
    val vendors: Map<String, List<CheckoutProductPayload>>,
    val summary: PaymentSummaryPayload
)
