package eu.ourmall.viewmodels

import eu.ourmall.models.cart.*
import eu.ourmall.models.order.*
import eu.ourmall.models.product.*
import eu.ourmall.models.payment.*
import eu.ourmall.services.*
import eu.ourmall.viewmodels.navigation.AppRoute
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
        override suspend fun fetchProducts(page: Int, pageSize: Int): ProductPage {
            return ProductPage(ProductSamples.sampleProducts.take(6), page, true)
        }

        override suspend fun submitPayment(payload: Map<String, Any?>): PaymentResponse {
            return PaymentResponse("PAY-REF", true)
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
        assertTrue(appState.path.isEmpty())
    }

    @Test
    fun `test start sequence`() = runTest {
        appState.start()
        
        // Wait for splash delay (2000ms)
        advanceTimeBy(2001)
        
        assertFalse(appState.isShowingSplash)
        assertEquals(6, appState.products.size)
    }

    @Test
    fun `test navigation flow`() {
        appState.goToCart()
        assertEquals(AppRoute.Cart, appState.path.last())
        
        val product = ProductSamples.sampleProducts.first()
        appState.goToProduct(product)
        assertTrue(appState.path.last() is AppRoute.ProductDetail)
        
        appState.goHome()
        assertTrue(appState.path.isEmpty())
    }

    @Test
    fun `test orchestration - submit payment clears cart and adds order`() = runTest {
        val product = ProductSamples.sampleProducts.first { it.inStock }
        appState.addToCart(product)
        
        appState.submitPayment()
        advanceTimeBy(2001) // Orchestration delay
        
        assertTrue(appState.hasCompletedPayment)
        assertEquals(1, appState.successfulOrders.size)
        assertEquals(0, appState.cartCount)
    }
}
