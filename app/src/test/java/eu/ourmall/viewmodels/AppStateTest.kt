package eu.ourmall.viewmodels

import eu.ourmall.models.*
import eu.ourmall.services.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private class MockService : CommerceServicing {
        var productsToReturn = ProductSamples.sampleProducts.take(6)
        var hasMore = true
        var fetchCount = 0

        override suspend fun fetchProducts(page: Int, pageSize: Int): ProductPage {
            fetchCount++
            return ProductPage(productsToReturn, page, hasMore)
        }

        override suspend fun submitPayment(payload: Map<String, Any?>): PaymentResponse {
            return PaymentResponse("order-123", "PAY-REF", "pending")
        }
    }

    private lateinit var service: MockService
    private lateinit var appState: AppState

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        service = MockService()
        appState = AppState(service)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state`() {
        assertTrue(appState.isShowingSplash)
        assertTrue(appState.products.isEmpty())
        assertEquals(0, appState.cartCount)
        assertEquals(0, appState.ordersCount)
    }

    @Test
    fun `test start loads products`() = runTest {
        appState.start()
        
        // Wait for splash delay (2000ms)
        advanceTimeBy(2001)
        
        assertFalse(appState.isShowingSplash)
        assertEquals(6, appState.products.size)
        assertEquals(1, service.fetchCount)
    }

    @Test
    fun `test navigation`() {
        appState.goToCart()
        assertEquals(AppRoute.Cart, appState.path.last())
        
        appState.goToOrders()
        assertEquals(AppRoute.Orders, appState.path.last())
        
        appState.goHome()
        assertTrue(appState.path.isEmpty())
    }

    @Test
    fun `test add to cart`() {
        val product = ProductSamples.sampleProducts.first { it.inStock }
        appState.addToCart(product)
        
        assertEquals(1, appState.cartCount)
        assertEquals(1, appState.vendorSections.size)
        assertTrue(appState.selectedVendorIDs.value.contains(product.vendor.id))
    }

    @Test
    fun `test update quantity`() {
        val product = ProductSamples.sampleProducts.first { it.inStock }
        appState.addToCart(product)
        val cartKey = appState.vendorSections.first().items.first().cartKey
        
        appState.updateQuantity(cartKey, 2)
        assertEquals(2, appState.cartCount)
        
        appState.updateQuantity(cartKey, 0)
        assertEquals(0, appState.cartCount)
        assertEquals(0, appState.vendorSections.size)
    }

    @Test
    fun `test checkout totals`() {
        val product = ProductSamples.sampleProducts.first { it.id == "shoe-1" } // Price 120, Disc 15% -> 102
        appState.addToCart(product)
        
        val totals = appState.checkoutTotals
        // Subtotal = 102
        // VAT = 102 * 0.075 = 7.65
        // Total = 109.65
        assertEquals(BigDecimal("102.00"), totals.subtotal)
        assertEquals(BigDecimal("7.65"), totals.vat)
        assertEquals(BigDecimal("109.65"), totals.grandTotal)
    }

    @Test
    fun `test submit payment`() = runTest {
        val product = ProductSamples.sampleProducts.first { it.inStock }
        appState.addToCart(product)
        
        appState.submitPayment()
        advanceTimeBy(2001) // Delay in submitPayment
        
        assertTrue(appState.hasCompletedPayment)
        assertEquals(1, appState.successfulOrders.size)
        assertEquals(0, appState.cartCount)
    }

    @Test
    fun `test cancel order item`() = runTest {
        val product = ProductSamples.sampleProducts.first { it.inStock }
        appState.addToCart(product)
        appState.submitPayment()
        advanceTimeBy(2001)
        
        val order = appState.successfulOrders.first()
        val itemID = order.vendorGroups.first().items.first().id
        
        appState.cancelOrderItem(order.id, itemID)
        
        val updatedOrder = appState.order(order.id)!!
        assertEquals(ItemStatus.CANCELLED, updatedOrder.vendorGroups.first().items.first().status)
    }
}
