package eu.ourmall.models.product

import java.math.BigDecimal
import java.util.*

object ProductSamples {
    private val calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 2)
    }
    private val futureDate = calendar.time

    val sampleProducts = listOf(
        Product(
            id = "shoe-1",
            name = "Runner Pro",
            summary = "High-performance running shoes for all terrains.",
            price = BigDecimal("120.00"),
            discountPercentage = BigDecimal("15"),
            category = listOf("Fashion", "Men", "Sports"),
            vendor = Vendor(id = "v-1", name = "SportGear"),
            imageURL = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
            quantityRemaining = 10,
            offerEndsAt = futureDate,
            options = listOf(
                ProductOption(name = "Size", values = listOf("40", "41", "42", "43", "44")),
                ProductOption(name = "Color", values = listOf("Red", "Blue", "Black"))
            )
        ),
        Product(
            id = "phone-1",
            name = "Galaxy S23",
            summary = "Latest generation smartphone with pro-grade camera.",
            price = BigDecimal("850.00"),
            discountPercentage = BigDecimal("5"),
            category = listOf("Tech", "Electronics", "Mobile"),
            vendor = Vendor(id = "v-2", name = "TechWorld"),
            imageURL = "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=600&q=80",
            quantityRemaining = 5,
            offerEndsAt = futureDate,
            options = listOf(
                ProductOption(name = "Storage", values = listOf("128GB", "256GB", "512GB")),
                ProductOption(name = "Color", values = listOf("Phantom Black", "Cream", "Green"))
            )
        ),
        Product(
            id = "coffee-1",
            name = "Arabica Beans",
            summary = "Premium roasted coffee beans from Ethiopia.",
            price = BigDecimal("24.50"),
            discountPercentage = BigDecimal.ZERO,
            category = listOf("Grocery", "Beverages"),
            vendor = Vendor(id = "v-3", name = "DailyGrind"),
            imageURL = "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?auto=format&fit=crop&w=600&q=80",
            quantityRemaining = 50,
            offerEndsAt = null,
            options = listOf(
                ProductOption(name = "Grind", values = listOf("Whole Bean", "Filter", "Espresso"))
            )
        ),
        Product(
            id = "watch-1",
            name = "Classic Chrono",
            summary = "Elegant analog watch with leather strap.",
            price = BigDecimal("199.00"),
            discountPercentage = BigDecimal("20"),
            category = listOf("Fashion", "Accessories"),
            vendor = Vendor(id = "v-4", name = "Timeless"),
            imageURL = "https://images.unsplash.com/photo-1524592094714-0f0654e20314?auto=format&fit=crop&w=600&q=80",
            quantityRemaining = 15,
            offerEndsAt = futureDate,
            options = emptyList()
        ),
        Product(
            id = "mat-1",
            name = "Yoga Mat",
            summary = "Eco-friendly non-slip yoga mat.",
            price = BigDecimal("45.00"),
            discountPercentage = BigDecimal("10"),
            category = listOf("Sports", "Wellness"),
            vendor = Vendor(id = "v-1", name = "SportGear"),
            imageURL = "https://images.unsplash.com/photo-1592432676556-2845654056bc?auto=format&fit=crop&w=600&q=80",
            quantityRemaining = 25,
            offerEndsAt = futureDate,
            options = listOf(
                ProductOption(name = "Thickness", values = listOf("4mm", "6mm"))
            )
        ),
        Product(
            id = "lamp-1",
            name = "Desk Lamp",
            summary = "Modern LED desk lamp with adjustable brightness.",
            price = BigDecimal("35.00"),
            discountPercentage = BigDecimal.ZERO,
            category = listOf("Home", "Office"),
            vendor = Vendor(id = "v-5", name = "HomeStyle"),
            imageURL = "https://images.unsplash.com/photo-1534073828943-f801091bb18c?auto=format&fit=crop&w=600&q=80",
            quantityRemaining = 0,
            offerEndsAt = null,
            options = emptyList()
        ),
        Product(
            id = "bag-1",
            name = "Leather Tote",
            summary = "Spacious tote bag made of genuine leather.",
            price = BigDecimal("150.00"),
            discountPercentage = BigDecimal.ZERO,
            category = listOf("Fashion", "Accessories"),
            vendor = Vendor(id = "v-4", name = "Timeless"),
            imageURL = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=600&q=80",
            quantityRemaining = 8,
            offerEndsAt = null,
            options = listOf(
                ProductOption(name = "Color", values = listOf("Brown", "Black", "Tan"))
            )
        ),
        Product(
            id = "headphones-1",
            name = "Noise Cancel Pro",
            summary = "Wireless headphones with active noise cancellation.",
            price = BigDecimal("299.00"),
            discountPercentage = BigDecimal("15"),
            category = listOf("Tech", "Electronics", "Audio"),
            vendor = Vendor(id = "v-2", name = "TechWorld"),
            imageURL = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80",
            quantityRemaining = 12,
            offerEndsAt = futureDate,
            options = listOf(
                ProductOption(name = "Color", values = listOf("Silver", "Black"))
            )
        )
    )
}
