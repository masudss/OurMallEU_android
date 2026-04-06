package eu.ourmall.services

import eu.ourmall.models.order.ItemStatus
import eu.ourmall.models.payment.PaymentResponse
import eu.ourmall.models.product.Product
import eu.ourmall.models.product.ProductPage
import eu.ourmall.models.product.ProductSamples
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.UUID

sealed class APIError(message: String) : Exception(message) {
    object InvalidResponse : APIError("The server returned an invalid response.")
    data class BadStatusCode(val code: Int) : APIError("The server request failed with status code $code.")
    object DecodingFailed : APIError("The response could not be decoded.")
    object EmptyCheckout : APIError("Select at least one vendor before continuing to payment.")
    data class Transport(val msg: String) : APIError(msg)
}

interface CommerceAPI {
    @GET("data")
    suspend fun getProducts(): ProductListResponse
}

data class ProductListResponse(
    val products: List<Product>
)

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
            paymentReference = "PAY-${(1000..9999).random()}",
            success = true
        )
    }
}

class CommerceAPIClient : CommerceServicing {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mp160a575ce3a6471b72.free.beeceptor.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(CommerceAPI::class.java)

    override suspend fun fetchProducts(page: Int, pageSize: Int): ProductPage {
        try {
            val response = api.getProducts()
            val allProducts = response.products
            
            // Client-side pagination since the new endpoint doesn't seem to support it
            val startIndex = (page - 1) * pageSize
            if (startIndex >= allProducts.size) {
                return ProductPage(items = emptyList(), page = page, hasMorePages = false)
            }
            val endIndex = minOf(allProducts.size, startIndex + pageSize)
            val pageItems = allProducts.subList(startIndex, endIndex)
            
            return ProductPage(
                items = pageItems,
                page = page,
                hasMorePages = endIndex < allProducts.size
            )
        } catch (e: Exception) {
            throw APIError.Transport(e.message ?: "Unknown error")
        }
    }

    override suspend fun submitPayment(payload: Map<String, Any?>): PaymentResponse {
        // Mock payment submission as endpoint for payment wasn't provided
        kotlinx.coroutines.delay(600)
        return PaymentResponse(
            paymentReference = "PAY-${(1000..9999).random()}",
            success = true
        )
    }
}
