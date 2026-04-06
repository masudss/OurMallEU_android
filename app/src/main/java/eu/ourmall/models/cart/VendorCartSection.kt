package eu.ourmall.models.cart

import eu.ourmall.models.product.Vendor
import java.math.BigDecimal

data class VendorCartSection(
    val vendor: Vendor,
    val items: List<CartItem>,
    val isSelected: Boolean
) {
    val subtotal: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.product.discountedPrice.multiply(BigDecimal(item.quantity)))
        }

    val discountTotal: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, item ->
            val original = item.product.price.multiply(BigDecimal(item.quantity))
            val discounted = item.product.discountedPrice.multiply(BigDecimal(item.quantity))
            acc.add(original.subtract(discounted))
        }
}
