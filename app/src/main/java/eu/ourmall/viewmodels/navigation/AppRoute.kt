package eu.ourmall.viewmodels.navigation

import eu.ourmall.models.product.Product

sealed class AppRoute {
    object Home : AppRoute()
    object Cart : AppRoute()
    object Orders : AppRoute()
    data class OrderDetails(val orderId: String) : AppRoute()
    data class ProductDetail(val product: Product) : AppRoute()
    object Checkout : AppRoute()
    object Payment : AppRoute()
}
