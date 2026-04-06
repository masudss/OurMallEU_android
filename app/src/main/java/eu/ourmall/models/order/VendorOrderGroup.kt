package eu.ourmall.models.order

import eu.ourmall.models.product.Vendor
import java.math.BigDecimal

data class VendorOrderGroup(
    val vendor: Vendor,
    val items: List<OrderItem>,
    val status: OrderStatus
) {
    val subtotal: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.subtotal) }

    val isSettled: Boolean
        get() = items.all { it.status.isSettled }

    val isCancelled: Boolean
        get() = items.all { it.status == ItemStatus.CANCELLED }

    val displayStatusTitle: String
        get() = if (isCancelled) "Cancelled" else if (isSettled) "Settled" else "In Progress"
}
