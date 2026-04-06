package eu.ourmall.viewmodels.cart

import eu.ourmall.models.product.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CartViewModelTest {

    private lateinit var viewModel: CartViewModel

    @Before
    fun setup() {
        viewModel = CartViewModel()
    }

    @Test
    fun `test initial state`() {
        assertEquals(0, viewModel.cartCount)
        assertTrue(viewModel.cartItems.isEmpty())
        assertTrue(viewModel.vendorSections.isEmpty())
    }

    @Test
    fun `test add to cart`() {
        val product = ProductSamples.sampleProducts.first { it.inStock }
        viewModel.addToCart(product, product.defaultSelection) {}
        
        assertEquals(1, viewModel.cartCount)
        assertEquals(1, viewModel.vendorSections.size)
        assertTrue(viewModel.selectedVendorIDs.value.contains(product.vendor.id))
    }

    @Test
    fun `test update quantity`() {
        val product = ProductSamples.sampleProducts.first { it.inStock }
        viewModel.addToCart(product, product.defaultSelection) {}
        val cartKey = viewModel.vendorSections.first().items.first().cartKey
        
        viewModel.updateQuantity(cartKey, 2) {}
        assertEquals(2, viewModel.cartCount)
        
        viewModel.updateQuantity(cartKey, 0) {}
        assertEquals(0, viewModel.cartCount)
        assertEquals(0, viewModel.vendorSections.size)
    }

    @Test
    fun `test checkout totals`() {
        val product = ProductSamples.sampleProducts.first { it.id == "shoe-1" } // Price 120, Disc 15% -> 102
        viewModel.addToCart(product, product.defaultSelection) {}
        
        val totals = viewModel.checkoutTotals
        // Subtotal = 102
        // VAT = 102 * 0.075 = 7.65
        // Total = 109.65
        assertEquals(BigDecimal("102.00"), totals.subtotal)
        assertEquals(BigDecimal("7.65"), totals.vat)
        assertEquals(BigDecimal("109.65"), totals.grandTotal)
    }
}
