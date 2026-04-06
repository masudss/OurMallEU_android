package eu.ourmall.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.ourmall.models.*
import eu.ourmall.models.CurrencyUtils.rounded
import eu.ourmall.models.CurrencyUtils.vatRate
import eu.ourmall.services.CommerceAPIClient
import eu.ourmall.services.CommerceServicing
import eu.ourmall.services.PreviewCommerceService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

sealed class AppRoute {
    object Cart : AppRoute()
    object Orders : AppRoute()
    data class OrderDetails(val orderId: String) : AppRoute()
    data class ProductDetail(val product: Product) : AppRoute()
    object Checkout : AppRoute()
    object Payment : AppRoute()
}

class AppState(private val service: CommerceServicing = CommerceAPIClient()) : ViewModel() {

    var path = mutableStateListOf<AppRoute>()
        private set

    var isShowingSplash by mutableStateOf(true)
        private set

    var products = mutableStateListOf<Product>()
        private set

    var isLoadingProducts by mutableStateOf(false)
        private set

    var productErrorMessage by mutableStateOf<String?>(null)
        private set

    var isLoadingNextPage by mutableStateOf(false)
        private set

    var cartItems = mutableStateMapOf<String, CartItem>()
        private set

    var selectedVendorIDs = mutableStateOf<Set<String>>(emptySet())
        private set

    var isSubmittingPayment by mutableStateOf(false)
        private set

    var paymentErrorMessage by mutableStateOf<String?>(null)
        private set

    var paymentReference by mutableStateOf<String?>(null)
        private set

    var currentOrder by mutableStateOf<Order?>(null)
        private set

    var successfulOrders = mutableStateListOf<Order>()
        private set

    var hasCompletedPayment by mutableStateOf(false)
        private set

    var searchQuery by mutableStateOf("")
    
    var filterCriteria by mutableStateOf(FilterCriteria())
        private set

    val heroBanners = listOf(
        "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=1400&q=80",
        "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=960&q=60",
        "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=960&q=60"
    )

    private val pageSize = 6
    private var hasStarted = false
    var currentPage = 0
        private set
    var hasMoreProducts = true
        private set

    val cartCount: Int
        get() = cartItems.values.sumOf { it.quantity }

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

    val filteredProducts: List<Product>
        get() {
            return products.filter { product ->
                val matchesSearch = searchQuery.isEmpty() || 
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.vendor.name.contains(searchQuery, ignoreCase = true)
                
                val matchesCategory = filterCriteria.category == null || 
                    product.category.any { it.equals(filterCriteria.category!!, ignoreCase = true) }
                
                val matchesPrice = (filterCriteria.minPrice == null || product.discountedPrice >= filterCriteria.minPrice!!) &&
                                  (filterCriteria.maxPrice == null || product.discountedPrice <= filterCriteria.maxPrice!!)
                
                val matchesStock = !filterCriteria.onlyInStock || product.inStock
                
                matchesSearch && matchesCategory && matchesPrice && matchesStock
            }
        }

    val allCategories: List<String>
        get() = products.flatMap { it.category }.distinct().sorted()

    fun updateFilter(criteria: FilterCriteria) {
        filterCriteria = criteria
    }

    fun clearFilters() {
        filterCriteria = FilterCriteria()
        searchQuery = ""
    }

    fun start() {
        if (hasStarted) return
        hasStarted = true

        viewModelScope.launch {
            delay(2000)
            isShowingSplash = false
            refreshProducts()
        }
    }

    suspend fun refreshProducts() {
        if (isLoadingProducts) return
        isLoadingProducts = true
        productErrorMessage = null
        currentPage = 0
        hasMoreProducts = true
        products.clear()

        try {
            val firstPage = service.fetchProducts(1, pageSize)
            currentPage = firstPage.page
            hasMoreProducts = firstPage.hasMorePages
            products.addAll(firstPage.items)
        } catch (e: Exception) {
            if (service is CommerceAPIClient) {
                try {
                    val fallback = PreviewCommerceService().fetchProducts(1, pageSize)
                    currentPage = fallback.page
                    hasMoreProducts = fallback.hasMorePages
                    products.addAll(fallback.items)
                } catch (fallbackE: Exception) {
                    productErrorMessage = e.message
                }
            } else {
                productErrorMessage = e.message
            }
        } finally {
            isLoadingProducts = false
        }
    }

    fun retryLoadingProducts() {
        viewModelScope.launch {
            refreshProducts()
        }
    }

    fun loadNextPageIfNeeded(currentProduct: Product) {
        if (!hasMoreProducts || isLoadingProducts || isLoadingNextPage) return
        val lastItems = products.takeLast(4)
        if (lastItems.any { it.id == currentProduct.id }) {
            viewModelScope.launch {
                loadNextPage()
            }
        }
    }

    fun addToCart(product: Product) {
        addToCart(product, product.defaultSelection)
    }

    fun addToCart(product: Product, selection: ProductSelection) {
        if (!product.inStock) return
        resetPaymentStateForCartChanges()

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

    fun updateQuantity(itemID: String, quantity: Int) {
        val existing = cartItems[itemID] ?: return
        resetPaymentStateForCartChanges()

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

    fun goToCart() { path.add(AppRoute.Cart) }
    fun goToOrders() { path.add(AppRoute.Orders) }
    fun goToOrderDetails(orderId: String) { path.add(AppRoute.OrderDetails(orderId)) }
    fun goToProduct(product: Product) { path.add(AppRoute.ProductDetail(product)) }
    fun goToCheckout() { if (selectedSections.isNotEmpty()) path.add(AppRoute.Checkout) }
    fun goToPayment() { if (selectedSections.isNotEmpty()) path.add(AppRoute.Payment) }
    fun goHome() { path.clear() }

    fun submitPayment() {
        viewModelScope.launch {
            isSubmittingPayment = true
            paymentErrorMessage = null

            try {
                delay(2000)
                val request = buildPaymentRequest()
                val pendingOrder = buildPendingOrder()
                currentOrder = pendingOrder

                val response = service.submitPayment(emptyMap())
                paymentReference = response.paymentReference
                hasCompletedPayment = true
                successfulOrders.add(0, pendingOrder)
                removePurchasedItems()
            } catch (e: Exception) {
                paymentErrorMessage = e.message
                hasCompletedPayment = false
            } finally {
                isSubmittingPayment = false
            }
        }
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

    fun order(withID: String): Order? {
        return successfulOrders.find { it.id == withID }
    }

    private suspend fun loadNextPage() {
        if (!hasMoreProducts) return
        isLoadingNextPage = true

        try {
            val nextPage = currentPage + 1
            val response = service.fetchProducts(nextPage, pageSize)
            currentPage = response.page
            hasMoreProducts = response.hasMorePages
            products.addAll(response.items)
        } catch (e: Exception) {
            productErrorMessage = e.message
        } finally {
            isLoadingNextPage = false
        }
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

    private fun buildPaymentRequest(): PaymentRequest {
        if (selectedSections.isEmpty()) throw Exception("Empty checkout")

        val vendors = selectedSections.associate { section ->
            section.vendor.id to section.items.map {
                CheckoutProductPayload(
                    productId = it.product.id,
                    quantity = it.quantity,
                    unitPrice = it.product.discountedPrice.rounded(),
                    selectedOptions = it.selectedOptions
                )
            }
        }

        val totals = checkoutTotals
        val summary = PaymentSummaryPayload(
            subtotal = totals.subtotal,
            discount = totals.discount,
            vat = totals.vat,
            grandTotal = totals.grandTotal
        )

        return PaymentRequest(vendors, summary)
    }

    private fun buildPendingOrder(): Order {
        val groups = selectedSections.map { section ->
            VendorOrderGroup(
                vendor = section.vendor,
                items = section.items.map {
                    OrderItem(
                        productID = it.product.id,
                        productName = it.product.name,
                        quantity = it.quantity,
                        unitPrice = it.product.discountedPrice.rounded(),
                        selectedOptions = it.selectedOptions,
                        status = ItemStatus.PENDING
                    )
                },
                status = OrderStatus.IN_PROGRESS
            )
        }
        return Order(
            status = OrderStatus.IN_PROGRESS,
            vendorGroups = groups,
            createdAt = Date()
        )
    }

    private fun removePurchasedItems() {
        val purchasedItemKeys = selectedSections.flatMap { it.items.map { item -> item.cartKey } }.toSet()
        purchasedItemKeys.forEach { cartItems.remove(it) }
        selectedVendorIDs.value = emptySet()
    }

    private fun pruneSelectedVendors() {
        val currentVendorIDs = cartItems.values.map { it.product.vendor.id }.toSet()
        selectedVendorIDs.value = selectedVendorIDs.value.intersect(currentVendorIDs)
    }

    private fun resetPaymentStateForCartChanges() {
        hasCompletedPayment = false
        paymentErrorMessage = null
        paymentReference = null
    }

    private fun updateOrder(orderID: String, mutation: (Order) -> Order) {
        currentOrder?.let {
            if (it.id == orderID) {
                currentOrder = mutation(it)
            }
        }

        val index = successfulOrders.indexOfFirst { it.id == orderID }
        if (index != -1) {
            successfulOrders[index] = mutation(successfulOrders[index])
        }
    }

    companion object {
        val Preview = AppState(PreviewCommerceService())
    }
}
