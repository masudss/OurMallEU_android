package eu.ourmall.viewmodels.order

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import eu.ourmall.models.order.ItemStatus
import eu.ourmall.models.order.Order
import eu.ourmall.models.order.OrderStatus

class OrderViewModel {
    var currentOrder = mutableStateOf<Order?>(null)
        private set

    var successfulOrders = mutableStateListOf<Order>()
        private set

    val ordersCount: Int
        get() = successfulOrders.size

    val activeOrders: List<Order>
        get() = successfulOrders.filter { order ->
            order.allItems.any { !it.status.isSettled }
        }

    val settledOrders: List<Order>
        get() = successfulOrders.filter { order ->
            order.allItems.isNotEmpty() && order.allItems.all { it.status.isSettled }
        }

    fun addOrder(order: Order) {
        successfulOrders.add(0, order)
        currentOrder.value = order
    }

    fun order(withID: String): Order? {
        return successfulOrders.find { it.id == withID }
    }

    fun cancelOrderItem(orderID: String, orderItemID: String) {
        updateOrder(orderID) { order ->
            val updatedGroups = order.vendorGroups.map { group ->
                val updatedItems = group.items.map { item ->
                    if (item.id == orderItemID) item.copy(status = ItemStatus.CANCELLED) else item
                }
                val allSettled = updatedItems.all { it.status.isSettled }
                group.copy(
                    items = updatedItems,
                    status = if (allSettled) OrderStatus.SETTLED else OrderStatus.IN_PROGRESS
                )
            }
            val allSettled = updatedGroups.all { it.status == OrderStatus.SETTLED }
            order.copy(
                vendorGroups = updatedGroups,
                status = if (allSettled) OrderStatus.SETTLED else OrderStatus.IN_PROGRESS
            )
        }
    }

    fun cancelVendor(orderID: String, vendorID: String) {
        updateOrder(orderID) { order ->
            val updatedGroups = order.vendorGroups.map { group ->
                if (group.vendor.id == vendorID) {
                    group.copy(
                        status = OrderStatus.SETTLED,
                        items = group.items.map { it.copy(status = ItemStatus.CANCELLED) }
                    )
                } else group
            }
            val allSettled = updatedGroups.all { it.status == OrderStatus.SETTLED }
            order.copy(
                vendorGroups = updatedGroups,
                status = if (allSettled) OrderStatus.SETTLED else OrderStatus.IN_PROGRESS
            )
        }
    }

    fun cancelOrder(orderID: String) {
        updateOrder(orderID) { order ->
            order.copy(
                status = OrderStatus.SETTLED,
                vendorGroups = order.vendorGroups.map { group ->
                    group.copy(
                        status = OrderStatus.SETTLED,
                        items = group.items.map { it.copy(status = ItemStatus.CANCELLED) }
                    )
                }
            )
        }
    }

    private fun updateOrder(orderID: String, mutation: (Order) -> Order) {
        currentOrder.value?.let {
            if (it.id == orderID) {
                currentOrder.value = mutation(it)
            }
        }

        val index = successfulOrders.indexOfFirst { it.id == orderID }
        if (index != -1) {
            successfulOrders[index] = mutation(successfulOrders[index])
        }
    }
}
