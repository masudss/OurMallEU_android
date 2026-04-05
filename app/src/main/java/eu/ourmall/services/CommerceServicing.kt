package eu.ourmall.services

import eu.ourmall.models.*
import java.util.UUID

sealed class APIError(message: String) : Exception(message) {
    object InvalidResponse : APIError("The server returned an invalid response.")
    data class BadStatusCode(val code: Int) : APIError("The server request failed with status code $code.")
    object DecodingFailed : APIError("The response could not be decoded.")
    object EmptyCheckout : APIError("Select at least one vendor before continuing to payment.")
    data class Transport(val msg: String) : APIError(msg)
}

interface CommerceServicing {
    suspend fun fetchProducts(page: Int, pageSize: Int): ProductPage
    suspend fun submitPayment(payload: Map<String, Any?>): PaymentResponse
}

class PreviewCommerceService : CommerceServicing {
    override suspend fun fetchProducts(page: Int, pageSize: Int): ProductPage {
        kotlinx.coroutines.delay(250)
        val allProducts = ProductSamples.sampleProducts
        val startIndex = (page - 1) * pageSize
        if (startIndex >= allProducts.size) {
            return ProductPage(items = emptyList(), page = page, hasMorePages = false)
        }

        val endIndex = minOf(allProducts.size, startIndex + pageSize)
        val pageItems = allProducts.subList(startIndex, endIndex)
        return ProductPage(items = pageItems, page = page, hasMorePages = endIndex < allProducts.size)
    }

    override suspend fun submitPayment(payload: Map<String, Any?>): PaymentResponse {
        kotlinx.coroutines.delay(600)
        return PaymentResponse(
            orderId = UUID.randomUUID().toString(),
            paymentReference = "PAY-${(1000..9999).random()}",
            status = ItemStatus.PENDING.name.lowercase()
        )
    }
}

// CommerceAPIClient would normally use Retrofit or Ktor. 
// For "line by line" logic replication, we will focus on the state management first.
class CommerceAPIClient : CommerceServicing {
    // Placeholder for actual implementation if needed, but AppState uses this or Preview
    override suspend fun fetchProducts(page: Int, pageSize: Int): ProductPage {
        throw APIError.Transport("Not implemented")
    }

    override suspend fun submitPayment(payload: Map<String, Any?>): PaymentResponse {
        throw APIError.Transport("Not implemented")
    }
}
