package eu.ourmall.models

import java.math.BigDecimal
import java.util.Date
import java.util.UUID

enum class OrderStatus(val title: String) {
    IN_PROGRESS("In progress"),
    SETTLED("Settled");

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.find { it.name.lowercase() == value.lowercase() || it.title.lowercase() == value.lowercase() } ?: IN_PROGRESS
        }
    }
}

enum class ItemStatus(val title: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    val isSettled: Boolean
        get() = this == DELIVERED || this == CANCELLED
}

data class OrderItem(
    val id: String = UUID.randomUUID().toString(),
    val productID: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val selectedOptions: Map<String, String>,
    val status: ItemStatus
)

data class VendorOrderGroup(
    val vendor: Vendor,
    val items: List<OrderItem>,
    val status: OrderStatus
) {
    val id: String
        get() = vendor.id

    val isSettled: Boolean
        get() = status == OrderStatus.SETTLED

    val isCancelled: Boolean
        get() = items.isNotEmpty() && items.all { it.status == ItemStatus.CANCELLED }

    val isDelivered: Boolean
        get() = items.isNotEmpty() && items.all { it.status == ItemStatus.DELIVERED }

    val displayStatusTitle: String
        get() = when {
            isCancelled -> "Cancelled"
            isDelivered -> "Delivered"
            else -> status.title
        }
}

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val status: OrderStatus,
    val vendorGroups: List<VendorOrderGroup>,
    val createdAt: Date = Date()
) {
    val isSettled: Boolean
        get() = status == OrderStatus.SETTLED

    val allItems: List<OrderItem>
        get() = vendorGroups.flatMap { it.items }

    val isCancelled: Boolean
        get() = allItems.isNotEmpty() && allItems.all { it.status == ItemStatus.CANCELLED }

    val isDelivered: Boolean
        get() = allItems.isNotEmpty() && allItems.all { it.status == ItemStatus.DELIVERED }

    val displayStatusTitle: String
        get() = when {
            isCancelled -> "Cancelled"
            isDelivered -> "Delivered"
            else -> status.title
        }
}

data class PaymentRequest(
    val vendors: Map<String, List<CheckoutProductPayload>>,
    val summary: PaymentSummaryPayload
)

data class CheckoutProductPayload(
    val productId: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val selectedOptions: Map<String, String>
)

data class PaymentSummaryPayload(
    val subtotal: BigDecimal,
    val discount: BigDecimal,
    val vat: BigDecimal,
    val grandTotal: BigDecimal
)

data class PaymentResponse(
    val orderId: String,
    val paymentReference: String,
    val status: String
)
