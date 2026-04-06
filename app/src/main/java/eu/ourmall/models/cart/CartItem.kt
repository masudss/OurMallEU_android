package eu.ourmall.models.cart

import eu.ourmall.models.product.Product

data class CartItem(
    val product: Product,
    val selectedOptions: Map<String, String>,
    val quantity: Int
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

    val selectedOptionsText: String
        get() = selectedOptions.values.joinToString(", ")

    val totalPrice: java.math.BigDecimal
        get() = product.discountedPrice.multiply(java.math.BigDecimal(quantity))
}
