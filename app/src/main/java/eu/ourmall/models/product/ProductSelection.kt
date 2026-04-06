package eu.ourmall.models.product

data class ProductSelection(
    val selectedOptions: Map<String, String>,
    val quantity: Int
)
