package eu.ourmall.viewmodels.order

import eu.ourmall.models.order.*
import eu.ourmall.models.product.ProductSamples
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class OrderViewModelTest {

    private lateinit var viewModel: OrderViewModel

    @Before
    fun setup() {
        viewModel = OrderViewModel()
    }

    @Test
    fun `test initial state`() {
        assertNull(viewModel.currentOrder.value)
        assertEquals(0, viewModel.ordersCount)
        assertTrue(viewModel.successfulOrders.isEmpty())
    }

    @Test
    fun `test add order`() {
        val order = createSampleOrder()
        viewModel.addOrder(order)
        
        assertEquals(1, viewModel.ordersCount)
        assertNotNull(viewModel.currentOrder.value)
        assertEquals(order.id, viewModel.currentOrder.value?.id)
    }

    @Test
    fun `test cancel order item`() {
        val order = createSampleOrder()
        viewModel.addOrder(order)
        val itemID = order.vendorGroups.first().items.first().id
        
        viewModel.cancelOrderItem(order.id, itemID)
        
        val updatedOrder = viewModel.order(order.id)!!
        assertEquals(ItemStatus.CANCELLED, updatedOrder.vendorGroups.first().items.first().status)
    }

    private fun createSampleOrder(): Order {
        val vendor = ProductSamples.sampleProducts.first().vendor
        val item = OrderItem(
            productID = "p1",
            productName = "Product 1",
            quantity = 1,
            unitPrice = java.math.BigDecimal("100"),
            selectedOptions = emptyMap(),
            status = ItemStatus.PENDING
        )
        val group = VendorOrderGroup(vendor, listOf(item), OrderStatus.IN_PROGRESS)
        return Order(status = OrderStatus.IN_PROGRESS, vendorGroups = listOf(group), createdAt = Date())
    }
}
