package eu.ourmall.models.product

data class ProductPage(
    val items: List<Product>,
    val page: Int,
    val hasMorePages: Boolean
)
