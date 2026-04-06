package eu.ourmall.models.product

import eu.ourmall.models.order.ItemStatus
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

data class Product(
    val id: String,
    val name: String,
    val imageURL: String?,
    val vendor: Vendor,
    val price: BigDecimal,
    val discountPercentage: BigDecimal,
    val category: List<String> = emptyList(),
    val offerEndsAt: Date?,
    val quantityRemaining: Int,
    val summary: String,
    val options: List<ProductOption>,
    var status: ItemStatus = ItemStatus.PENDING
) {
    val inStock: Boolean
        get() = quantityRemaining > 0

    val discountMultiplier: BigDecimal
        get() = discountPercentage.divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
            .coerceIn(BigDecimal.ZERO, BigDecimal.ONE)

    val discountedPrice: BigDecimal
        get() = price.subtract(price.multiply(discountMultiplier))

    fun offerEndsText(): String {
        val now = Date()
        val endsAt = offerEndsAt ?: return "No active offer"

        if (endsAt.before(now)) return "Offer ended"

        val diff = endsAt.time - now.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days >= 2 -> {
                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                "Offer ends ${sdf.format(endsAt)}"
            }
            days >= 1 -> "Offer ends tomorrow"
            hours >= 1 -> "Offer ends in $hours ${if (hours == 1L) "hr" else "hrs"}"
            minutes >= 1 -> "Offer ends in $minutes min"
            else -> "Offer ends now"
        }
    }

    val defaultSelection: ProductSelection
        get() = ProductSelection(
            selectedOptions = options.associate { it.name to (it.values.firstOrNull() ?: "") },
            quantity = 1
        )
}
