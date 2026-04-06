package eu.ourmall.models.payment

data class PaymentResponse(
    val paymentReference: String,
    val success: Boolean
)
