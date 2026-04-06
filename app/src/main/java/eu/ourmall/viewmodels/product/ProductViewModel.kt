package eu.ourmall.viewmodels.product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eu.ourmall.models.product.FilterCriteria
import eu.ourmall.models.product.Product
import eu.ourmall.services.CommerceAPIClient
import eu.ourmall.services.CommerceServicing
import eu.ourmall.services.PreviewCommerceService

class ProductViewModel(private val service: CommerceServicing) {
    var products = mutableStateListOf<Product>()
        private set

    var isLoadingProducts by mutableStateOf(false)
        private set

    var productErrorMessage by mutableStateOf<String?>(null)
        private set

    var isLoadingNextPage by mutableStateOf(false)
        private set

    var searchQuery by mutableStateOf("")
    
    var filterCriteria by mutableStateOf(FilterCriteria())
        private set

    private val pageSize = 6
    var currentPage = 0
        private set
    var hasMoreProducts = true
        private set

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

    suspend fun loadNextPage() {
        if (!hasMoreProducts || isLoadingNextPage) return
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

    fun loadNextPageIfNeeded(currentProduct: Product, onNeedLoad: suspend () -> Unit) {
        if (!hasMoreProducts || isLoadingProducts || isLoadingNextPage) return
        val lastItems = products.takeLast(4)
        if (lastItems.any { it.id == currentProduct.id }) {
            // The caller (AppState) will launch this in its scope
        }
    }
}
