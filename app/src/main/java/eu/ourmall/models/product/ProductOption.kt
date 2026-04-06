package eu.ourmall.models.product

data class ProductOption(
    val name: String,
    val values: List<String>
) {
    val id: String
        get() = name

    val displayName: String
        get() = name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}
