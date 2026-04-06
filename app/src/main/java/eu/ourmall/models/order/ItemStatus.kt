package eu.ourmall.models.order

enum class ItemStatus(val title: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    val isSettled: Boolean
        get() = this == DELIVERED || this == CANCELLED
}
