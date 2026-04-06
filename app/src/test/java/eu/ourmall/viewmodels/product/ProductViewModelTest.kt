package eu.ourmall.viewmodels.product

import eu.ourmall.models.product.*
import eu.ourmall.services.CommerceServicing
import eu.ourmall.models.payment.PaymentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

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
            return PaymentResponse("PAY-REF", true)
        }
    }

    private lateinit var service: MockService
    private lateinit var viewModel: ProductViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        service = MockService()
        viewModel = ProductViewModel(service)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state`() {
        assertTrue(viewModel.products.isEmpty())
        assertFalse(viewModel.isLoadingProducts)
        assertNull(viewModel.productErrorMessage)
    }

    @Test
    fun `test refresh products loads products`() = runTest {
        viewModel.refreshProducts()
        advanceUntilIdle()
        
        assertEquals(6, viewModel.products.size)
        assertEquals(1, service.fetchCount)
        assertFalse(viewModel.isLoadingProducts)
    }

    @Test
    fun `test search functionality`() = runTest {
        val product1 = ProductSamples.sampleProducts.first { it.id == "shoe-1" } // "Runner Pro"
        val product2 = ProductSamples.sampleProducts.first { it.id == "phone-1" } // "Galaxy S23"
        
        viewModel.products.addAll(listOf(product1, product2))
        
        viewModel.searchQuery = "Runner"
        assertEquals(1, viewModel.filteredProducts.size)
        assertEquals(product1.id, viewModel.filteredProducts.first().id)
        
        viewModel.searchQuery = "Galaxy"
        assertEquals(1, viewModel.filteredProducts.size)
        assertEquals(product2.id, viewModel.filteredProducts.first().id)
        
        viewModel.searchQuery = "Unknown"
        assertEquals(0, viewModel.filteredProducts.size)
        
        viewModel.clearFilters()
        assertEquals(2, viewModel.filteredProducts.size)
    }

    @Test
    fun `test category filter`() = runTest {
        val fashionProduct = ProductSamples.sampleProducts.first { it.id == "shoe-1" }
        val techProduct = ProductSamples.sampleProducts.first { it.id == "phone-1" }
        
        viewModel.products.addAll(listOf(fashionProduct, techProduct))
        
        viewModel.updateFilter(FilterCriteria(category = "Fashion"))
        assertEquals(1, viewModel.filteredProducts.size)
        assertEquals(fashionProduct.id, viewModel.filteredProducts.first().id)
        
        viewModel.updateFilter(FilterCriteria(category = "Tech"))
        assertEquals(1, viewModel.filteredProducts.size)
        assertEquals(techProduct.id, viewModel.filteredProducts.first().id)
    }

    @Test
    fun `test price range filter`() = runTest {
        val product1 = ProductSamples.sampleProducts.first { it.id == "shoe-1" } // Price 120, Disc 15% -> 102
        val product2 = ProductSamples.sampleProducts.first { it.id == "phone-1" } // Price 850, Disc 5% -> 807.50
        
        viewModel.products.addAll(listOf(product1, product2))
        
        // Min 50, Max 150 -> only product1
        viewModel.updateFilter(FilterCriteria(minPrice = BigDecimal("50"), maxPrice = BigDecimal("150")))
        assertEquals(1, viewModel.filteredProducts.size)
        assertEquals(product1.id, viewModel.filteredProducts.first().id)
        
        // Min 500 -> only product2
        viewModel.updateFilter(FilterCriteria(minPrice = BigDecimal("500")))
        assertEquals(1, viewModel.filteredProducts.size)
        assertEquals(product2.id, viewModel.filteredProducts.first().id)
    }

    @Test
    fun `test in stock filter`() = runTest {
        val inStockProduct = ProductSamples.sampleProducts.first { it.inStock }
        val outOfStockProduct = ProductSamples.sampleProducts.first { !it.inStock }
        
        viewModel.products.addAll(listOf(inStockProduct, outOfStockProduct))
        
        viewModel.updateFilter(FilterCriteria(onlyInStock = true))
        assertEquals(1, viewModel.filteredProducts.size)
        assertEquals(inStockProduct.id, viewModel.filteredProducts.first().id)
        
        viewModel.updateFilter(FilterCriteria(onlyInStock = false))
        assertEquals(2, viewModel.filteredProducts.size)
    }
}
