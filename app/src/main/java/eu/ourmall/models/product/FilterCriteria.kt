package eu.ourmall.models.product

import java.math.BigDecimal

data class FilterCriteria(
    val category: String? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val onlyInStock: Boolean = false
)
