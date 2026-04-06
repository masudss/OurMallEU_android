package eu.ourmall.models.order

enum class OrderStatus(val title: String) {
    IN_PROGRESS("In progress"),
    SETTLED("Settled");

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.find { it.name.lowercase() == value.lowercase() || it.title.lowercase() == value.lowercase() } ?: IN_PROGRESS
        }
    }
}
