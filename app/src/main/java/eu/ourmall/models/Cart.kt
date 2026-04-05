package eu.ourmall.models

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

data class CartItem(
    val product: Product,
    val selectedOptions: Map<String, String>,
    var quantity: Int
) {
    val id: String
        get() = cartKey

    val cartKey: String
        get() {
            val optionsKey = selectedOptions.keys.sorted().joinToString("|") { 
                "$it=${selectedOptions[it] ?: ""}" 
            }
            return "${product.id}::$optionsKey"
        }

    val totalPrice: BigDecimal
        get() = product.discountedPrice.multiply(BigDecimal(quantity))

    val totalListPrice: BigDecimal
        get() = product.price.multiply(BigDecimal(quantity))

    val selectedOptionsText: String
        get() {
            if (selectedOptions.isEmpty()) return "Default options"
            return selectedOptions.keys.sorted().joinToString(" • ") { key ->
                "${key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}: ${selectedOptions[key] ?: ""}"
            }
        }
}

data class VendorCartSection(
    val vendor: Vendor,
    val items: List<CartItem>,
    val isSelected: Boolean
) {
    val id: String
        get() = vendor.id

    val subtotal: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalPrice) }

    val listTotal: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalListPrice) }

    val discountTotal: BigDecimal
        get() = listTotal.subtract(subtotal)
}

data class CheckoutTotals(
    val subtotal: BigDecimal,
    val discount: BigDecimal,
    val vat: BigDecimal,
    val grandTotal: BigDecimal
) {
    companion object {
        val empty = CheckoutTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
    }
}

object CurrencyUtils {
    val vatRate = BigDecimal("0.075")

    fun BigDecimal.rounded(scale: Int = 2): BigDecimal {
        return this.setScale(scale, RoundingMode.HALF_EVEN)
    }

    fun BigDecimal.toCurrencyText(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(this.rounded())
    }
}
