package eu.ourmall.viewmodels.payment

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PaymentViewModelTest {

    private lateinit var viewModel: PaymentViewModel

    @Before
    fun setup() {
        viewModel = PaymentViewModel()
    }

    @Test
    fun `test initial state`() {
        assertFalse(viewModel.isSubmittingPayment)
        assertNull(viewModel.paymentErrorMessage)
        assertNull(viewModel.paymentReference)
        assertFalse(viewModel.hasCompletedPayment)
    }

    @Test
    fun `test start payment`() {
        viewModel.startPayment()
        assertTrue(viewModel.isSubmittingPayment)
        assertNull(viewModel.paymentErrorMessage)
    }

    @Test
    fun `test complete payment`() {
        viewModel.startPayment()
        viewModel.completePayment("REF-123")
        
        assertFalse(viewModel.isSubmittingPayment)
        assertEquals("REF-123", viewModel.paymentReference)
        assertTrue(viewModel.hasCompletedPayment)
    }

    @Test
    fun `test fail payment`() {
        viewModel.startPayment()
        viewModel.failPayment("Error message")
        
        assertFalse(viewModel.isSubmittingPayment)
        assertEquals("Error message", viewModel.paymentErrorMessage)
        assertFalse(viewModel.hasCompletedPayment)
    }

    @Test
    fun `test reset state`() {
        viewModel.completePayment("REF-123")
        viewModel.resetState()
        
        assertNull(viewModel.paymentReference)
        assertFalse(viewModel.hasCompletedPayment)
    }
}
