package eu.ourmall.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.ourmall.models.cart.*
import eu.ourmall.models.order.*
import eu.ourmall.models.payment.*
import eu.ourmall.models.product.*
import eu.ourmall.models.cart.CurrencyUtils.rounded
import eu.ourmall.services.CommerceAPIClient
import eu.ourmall.services.CommerceServicing
import eu.ourmall.services.PreviewCommerceService
import eu.ourmall.viewmodels.cart.CartViewModel
import eu.ourmall.viewmodels.navigation.AppRoute
import eu.ourmall.viewmodels.order.OrderViewModel
import eu.ourmall.viewmodels.payment.PaymentViewModel
import eu.ourmall.viewmodels.product.ProductViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class AppState(private val service: CommerceServicing = CommerceAPIClient()) : ViewModel() {

    private val productViewModel = ProductViewModel(service)
    private val cartViewModel = CartViewModel()
    private val orderViewModel = OrderViewModel()
    private val paymentViewModel = PaymentViewModel()

    var path = mutableStateListOf<AppRoute>()
        private set

    var isShowingSplash by mutableStateOf(true)
        private set

    // Product Delegation
    val products get() = productViewModel.products
    val isLoadingProducts get() = productViewModel.isLoadingProducts
    val productErrorMessage get() = productViewModel.productErrorMessage
    val isLoadingNextPage get() = productViewModel.isLoadingNextPage
    var searchQuery by productViewModel::searchQuery
    val filterCriteria get() = productViewModel.filterCriteria
    val filteredProducts get() = productViewModel.filteredProducts
    val allCategories get() = productViewModel.allCategories
    val currentPage get() = productViewModel.currentPage
    val hasMoreProducts get() = productViewModel.hasMoreProducts

    fun updateFilter(criteria: FilterCriteria) = productViewModel.updateFilter(criteria)
    fun clearFilters() = productViewModel.clearFilters()
    
    fun retryLoadingProducts() {
        viewModelScope.launch {
            productViewModel.refreshProducts()
        }
    }

    fun loadNextPageIfNeeded(currentProduct: Product) {
        if (!hasMoreProducts || isLoadingProducts || isLoadingNextPage) return
        val lastItems = products.takeLast(4)
        if (lastItems.any { it.id == currentProduct.id }) {
            viewModelScope.launch {
                productViewModel.loadNextPage()
            }
        }
    }

    // Cart Delegation
    val cartItems get() = cartViewModel.cartItems
    val selectedVendorIDs get() = cartViewModel.selectedVendorIDs
    val cartCount get() = cartViewModel.cartCount
    val vendorSections get() = cartViewModel.vendorSections
    val selectedSections get() = cartViewModel.selectedSections
    val checkoutTotals get() = cartViewModel.checkoutTotals
    val cartTotals get() = cartViewModel.cartTotals

    fun addToCart(product: Product) = addToCart(product, product.defaultSelection)
    fun addToCart(product: Product, selection: ProductSelection) {
        cartViewModel.addToCart(product, selection) {
            paymentViewModel.resetState()
        }
    }
    fun quantityInCart(forProduct: Product) = cartViewModel.quantityInCart(forProduct)
    fun updateQuantity(itemID: String, quantity: Int) {
        cartViewModel.updateQuantity(itemID, quantity) {
            paymentViewModel.resetState()
        }
    }
    fun toggleVendorSelection(vendorID: String) = cartViewModel.toggleVendorSelection(vendorID)

    // Order Delegation
    val currentOrder get() = orderViewModel.currentOrder.value
    val successfulOrders get() = orderViewModel.successfulOrders
    val ordersCount get() = orderViewModel.ordersCount
    val activeOrders get() = orderViewModel.activeOrders
    val settledOrders get() = orderViewModel.settledOrders

    fun order(withID: String) = orderViewModel.order(withID)
    fun cancelOrderItem(orderID: String, orderItemID: String) = orderViewModel.cancelOrderItem(orderID, orderItemID)
    fun cancelVendor(orderID: String, vendorID: String) = orderViewModel.cancelVendor(orderID, vendorID)
    fun cancelOrder(orderID: String) = orderViewModel.cancelOrder(orderID)

    // Payment Delegation
    val isSubmittingPayment get() = paymentViewModel.isSubmittingPayment
    val paymentErrorMessage get() = paymentViewModel.paymentErrorMessage
    val paymentReference get() = paymentViewModel.paymentReference
    val hasCompletedPayment get() = paymentViewModel.hasCompletedPayment

    fun submitPayment() {
        viewModelScope.launch {
            paymentViewModel.startPayment()

            try {
                delay(2000)
                val request = buildPaymentRequest()
                val pendingOrder = buildPendingOrder()
                
                val response = service.submitPayment(emptyMap())
                paymentViewModel.completePayment(response.paymentReference)
                orderViewModel.addOrder(pendingOrder)
                cartViewModel.removePurchasedItems()
            } catch (e: Exception) {
                paymentViewModel.failPayment(e.message)
            }
        }
    }

    // Navigation
    fun goToCart() { path.add(AppRoute.Cart) }
    fun goToOrders() { path.add(AppRoute.Orders) }
    fun goToOrderDetails(orderId: String) { path.add(AppRoute.OrderDetails(orderId)) }
    fun goToProduct(product: Product) { path.add(AppRoute.ProductDetail(product)) }
    fun goToCheckout() { if (selectedSections.isNotEmpty()) path.add(AppRoute.Checkout) }
    fun goToPayment() { if (selectedSections.isNotEmpty()) path.add(AppRoute.Payment) }
    fun goHome() { path.clear() }

    // Misc
    val heroBanners = listOf(
        "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=1400&q=80",
        "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=960&q=60",
        "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=960&q=60"
    )

    fun start() {
        if (isShowingSplash && products.isEmpty() && !isLoadingProducts) {
            viewModelScope.launch {
                delay(2000)
                isShowingSplash = false
                productViewModel.refreshProducts()
            }
        }
    }

    // Private Helpers (retained in AppState for orchestration)
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

    companion object {
        val Preview = AppState(PreviewCommerceService())
    }
}
