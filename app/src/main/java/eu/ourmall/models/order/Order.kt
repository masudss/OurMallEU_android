package eu.ourmall.models.order

import java.util.*

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val status: OrderStatus,
    val vendorGroups: List<VendorOrderGroup>,
    val createdAt: Date
) {
    val allItems: List<OrderItem>
        get() = vendorGroups.flatMap { it.items }

    val isSettled: Boolean
        get() = vendorGroups.all { it.isSettled }

    val isCancelled: Boolean
        get() = vendorGroups.all { it.isCancelled }

    val displayStatusTitle: String
        get() = if (isCancelled) "Cancelled" else if (isSettled) "Settled" else "In Progress"
}
