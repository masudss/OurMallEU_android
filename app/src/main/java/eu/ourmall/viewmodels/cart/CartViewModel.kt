package eu.ourmall.viewmodels.cart

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import eu.ourmall.models.cart.CartItem
import eu.ourmall.models.cart.CheckoutTotals
import eu.ourmall.models.cart.CurrencyUtils.rounded
import eu.ourmall.models.cart.CurrencyUtils.vatRate
import eu.ourmall.models.cart.VendorCartSection
import eu.ourmall.models.product.Product
import eu.ourmall.models.product.ProductSelection
import java.math.BigDecimal

class CartViewModel {
    var cartItems = mutableStateMapOf<String, CartItem>()
        private set

    var selectedVendorIDs = mutableStateOf<Set<String>>(emptySet())
        private set

    val cartCount: Int
        get() = cartItems.values.sumOf { it.quantity }

    val vendorSections: List<VendorCartSection>
        get() {
            val grouped = cartItems.values.groupBy { it.product.vendor }
            return grouped.map { (vendor, items) ->
                VendorCartSection(
                    vendor = vendor,
                    items = items.sortedWith(compareBy({ it.product.name }, { it.selectedOptionsText })),
                    isSelected = selectedVendorIDs.value.contains(vendor.id)
                )
            }.sortedBy { it.vendor.name }
        }

    val selectedSections: List<VendorCartSection>
        get() = vendorSections.filter { it.isSelected }

    val checkoutTotals: CheckoutTotals
        get() {
            if (selectedSections.isEmpty()) return CheckoutTotals.empty
            val subtotal = selectedSections.fold(BigDecimal.ZERO) { acc, section -> acc.add(section.subtotal) }
            val discount = selectedSections.fold(BigDecimal.ZERO) { acc, section -> acc.add(section.discountTotal) }
            val vat = subtotal.multiply(vatRate).rounded()
            val grandTotal = subtotal.add(vat).rounded()
            return CheckoutTotals(subtotal.rounded(), discount.rounded(), vat, grandTotal)
        }

    val cartTotals: CheckoutTotals
        get() {
            if (selectedSections.isEmpty()) return CheckoutTotals.empty
            val subtotal = selectedSections.fold(BigDecimal.ZERO) { acc, section -> acc.add(section.subtotal) }
            val discount = selectedSections.fold(BigDecimal.ZERO) { acc, section -> acc.add(section.discountTotal) }
            return CheckoutTotals(subtotal.rounded(), discount.rounded(), BigDecimal.ZERO, subtotal.rounded())
        }

    fun addToCart(product: Product, selection: ProductSelection, onCartChanged: () -> Unit) {
        if (!product.inStock) return
        onCartChanged()

        val normalized = normalizedSelection(product, selection)
        val candidateKey = cartKey(product.id, normalized.selectedOptions)

        val existingQuantity = cartItems[candidateKey]?.quantity ?: 0
        val newQuantity = minOf(product.quantityRemaining, existingQuantity + maxOf(1, normalized.quantity))
        if (newQuantity <= 0) return

        cartItems[candidateKey] = CartItem(
            product = product,
            selectedOptions = normalized.selectedOptions,
            quantity = newQuantity
        )
        selectedVendorIDs.value = selectedVendorIDs.value + product.vendor.id
    }

    fun quantityInCart(forProduct: Product): Int {
        return cartItems.values
            .filter { it.product.id == forProduct.id }
            .sumOf { it.quantity }
    }

    fun updateQuantity(itemID: String, quantity: Int, onCartChanged: () -> Unit) {
        val existing = cartItems[itemID] ?: return
        onCartChanged()

        if (quantity <= 0) {
            cartItems.remove(itemID)
        } else {
            cartItems[itemID] = existing.copy(
                quantity = minOf(quantity, existing.product.quantityRemaining)
            )
        }
        pruneSelectedVendors()
    }

    fun toggleVendorSelection(vendorID: String) {
        val current = selectedVendorIDs.value.toMutableSet()
        if (current.contains(vendorID)) {
            current.remove(vendorID)
        } else {
            current.add(vendorID)
        }
        selectedVendorIDs.value = current
    }

    fun removePurchasedItems() {
        val purchasedItemKeys = selectedSections.flatMap { it.items.map { item -> item.cartKey } }.toSet()
        purchasedItemKeys.forEach { cartItems.remove(it) }
        selectedVendorIDs.value = emptySet()
    }

    private fun pruneSelectedVendors() {
        val currentVendorIDs = cartItems.values.map { it.product.vendor.id }.toSet()
        selectedVendorIDs.value = selectedVendorIDs.value.intersect(currentVendorIDs)
    }

    private fun normalizedSelection(product: Product, selection: ProductSelection): ProductSelection {
        val resolvedOptions = mutableMapOf<String, String>()
        for (option in product.options) {
            val chosen = selection.selectedOptions[option.name]
            if (chosen != null && option.values.contains(chosen)) {
                resolvedOptions[option.name] = chosen
            } else {
                resolvedOptions[option.name] = option.values.firstOrNull() ?: ""
            }
        }
        return ProductSelection(
            selectedOptions = resolvedOptions,
            quantity = minOf(maxOf(1, selection.quantity), product.quantityRemaining)
        )
    }

    private fun cartKey(productId: String, selectedOptions: Map<String, String>): String {
        val optionsKey = selectedOptions.keys.sorted().joinToString("|") {
            "$it=${selectedOptions[it] ?: ""}"
        }
        return "$productId::$optionsKey"
    }
}
