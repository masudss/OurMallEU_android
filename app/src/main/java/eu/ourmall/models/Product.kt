package eu.ourmall.models

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

data class Vendor(
    val id: String,
    val name: String
)

data class Product(
    val id: String,
    val name: String,
    val imageURL: String?,
    val vendor: Vendor,
    val price: BigDecimal,
    val discountPercentage: BigDecimal,
    val offerEndsAt: Date?,
    val quantityRemaining: Int,
    val summary: String,
    val options: List<ProductOption>,
    var status: ItemStatus = ItemStatus.PENDING
) {
    val inStock: Boolean
        get() = quantityRemaining > 0

    val discountMultiplier: BigDecimal
        get() = discountPercentage.divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
            .coerceIn(BigDecimal.ZERO, BigDecimal.ONE)

    val discountedPrice: BigDecimal
        get() = price.subtract(price.multiply(discountMultiplier))

    fun offerEndsText(): String {
        val now = Date()
        val endsAt = offerEndsAt ?: return "No active offer"

        return if (endsAt.before(now)) {
            "Offer ended"
        } else {
            // Simple string for now, we can use a library or custom logic for "relative" time later if needed to match RelativeDateTimeFormatter
            "Offer ends ${endsAt}" 
        }
    }

    val defaultSelection: ProductSelection
        get() = ProductSelection(
            selectedOptions = options.associate { it.name to (it.values.firstOrNull() ?: "") },
            quantity = 1
        )
}

data class ProductOption(
    val name: String,
    val values: List<String>
) {
    val id: String
        get() = name

    val displayName: String
        get() = name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}

data class ProductSelection(
    var selectedOptions: Map<String, String>,
    var quantity: Int
)

data class ProductPage(
    val items: List<Product>,
    val page: Int,
    val hasMorePages: Boolean
)

// Extension for Sample Data
object ProductSamples {
    val sampleProducts = listOf(
        Product(
            id = "shoe-1",
            name = "Aero Runner",
            imageURL = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-a", name = "BluePeak Sports"),
            price = BigDecimal(120),
            discountPercentage = BigDecimal(15),
            offerEndsAt = Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 8,
            summary = "Lightweight running shoes built for long city miles and daily training.",
            options = listOf(
                ProductOption(name = "size", values = listOf("S", "M", "L", "XL", "XXL", "XXXL")),
                ProductOption(name = "color", values = listOf("Red", "Black", "White"))
            )
        ),
        Product(
            id = "watch-1",
            name = "Nordic Smart Watch",
            imageURL = "https://images.unsplash.com/photo-1434056886845-dac89ffe9b56?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-b", name = "NorthHub Electronics"),
            price = BigDecimal(240),
            discountPercentage = BigDecimal(10),
            offerEndsAt = Date(System.currentTimeMillis() + 10 * 60 * 60 * 1000L),
            quantityRemaining = 0,
            summary = "An everyday smart watch with health tracking and strong battery life.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Black", "Silver", "Blue"))
            )
        ),
        Product(
            id = "bag-1",
            name = "Metro Carry Bag",
            imageURL = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-a", name = "BluePeak Sports"),
            price = BigDecimal(85),
            discountPercentage = BigDecimal(5),
            offerEndsAt = Date(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 4,
            summary = "Structured carry bag with padded compartments and weather-ready fabric.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Black", "Green", "Navy"))
            )
        ),
        Product(
            id = "hoodie-1",
            name = "Core Street Hoodie",
            imageURL = "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-c", name = "ThreadHaus"),
            price = BigDecimal(72),
            discountPercentage = BigDecimal(20),
            offerEndsAt = Date(System.currentTimeMillis() + 28 * 60 * 60 * 1000L),
            quantityRemaining = 13,
            summary = "Heavyweight hoodie with a relaxed fit and brushed inner lining.",
            options = listOf(
                ProductOption(name = "size", values = listOf("S", "M", "L", "XL", "XXL")),
                ProductOption(name = "color", values = listOf("Gray", "Black", "Cream"))
            )
        ),
        Product(
            id = "speaker-1",
            name = "Pulse Mini Speaker",
            imageURL = "https://images.unsplash.com/photo-1589003077984-894e133dabab?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-b", name = "NorthHub Electronics"),
            price = BigDecimal(96),
            discountPercentage = BigDecimal(12),
            offerEndsAt = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 16,
            summary = "Portable Bluetooth speaker with crisp sound and all-day playback.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Black", "Red", "White"))
            )
        ),
        Product(
            id = "lamp-1",
            name = "Halo Desk Lamp",
            imageURL = "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-d", name = "Casa Nova"),
            price = BigDecimal(58),
            discountPercentage = BigDecimal(8),
            offerEndsAt = Date(System.currentTimeMillis() + 4 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 7,
            summary = "Minimal desk lamp with dimmable warmth and a stable metal base.",
            options = listOf(
                ProductOption(name = "color", values = listOf("White", "Black", "Brass"))
            )
        ),
        Product(
            id = "chair-1",
            name = "Contour Lounge Chair",
            imageURL = "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-d", name = "Casa Nova"),
            price = BigDecimal(180),
            discountPercentage = BigDecimal(18),
            offerEndsAt = Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 3,
            summary = "Soft upholstered lounge chair shaped for comfort and small spaces.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Sand", "Olive", "Charcoal"))
            )
        ),
        Product(
            id = "tee-1",
            name = "Essential Tee",
            imageURL = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-c", name = "ThreadHaus"),
            price = BigDecimal(28),
            discountPercentage = BigDecimal(0),
            offerEndsAt = null,
            quantityRemaining = 42,
            summary = "Soft jersey tee made for daily wear and easy layering.",
            options = listOf(
                ProductOption(name = "size", values = listOf("S", "M", "L", "XL", "XXL", "XXXL")),
                ProductOption(name = "color", values = listOf("White", "Black", "Green"))
            )
        ),
        Product(
            id = "kettle-1",
            name = "Arc Electric Kettle",
            imageURL = "https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-e", name = "Kitchen Fold"),
            price = BigDecimal(64),
            discountPercentage = BigDecimal(14),
            offerEndsAt = Date(System.currentTimeMillis() + 36 * 60 * 60 * 1000L),
            quantityRemaining = 11,
            summary = "Fast-boil kettle with a clean spout and compact countertop footprint.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Silver", "Matte Black"))
            )
        ),
        Product(
            id = "blender-1",
            name = "Vivid Blend Pro",
            imageURL = "https://images.unsplash.com/photo-1570222094114-d054a817e56b?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-e", name = "Kitchen Fold"),
            price = BigDecimal(135),
            discountPercentage = BigDecimal(9),
            offerEndsAt = Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 6,
            summary = "Countertop blender designed for smoothies, soups, and frozen mixes.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Black", "White"))
            )
        ),
        Product(
            id = "camera-1",
            name = "Vista Pocket Camera",
            imageURL = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-b", name = "NorthHub Electronics"),
            price = BigDecimal(320),
            discountPercentage = BigDecimal(11),
            offerEndsAt = Date(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 5,
            summary = "Compact travel camera with sharp optics and simple manual controls.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Black", "Silver"))
            )
        ),
        Product(
            id = "sofa-1",
            name = "Cloud Corner Sofa",
            imageURL = "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-d", name = "Casa Nova"),
            price = BigDecimal(720),
            discountPercentage = BigDecimal(16),
            offerEndsAt = Date(System.currentTimeMillis() + 6 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 2,
            summary = "Large modular sofa with deep cushions and neutral upholstery.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Beige", "Slate", "Stone"))
            )
        ),
        Product(
            id = "headphone-1",
            name = "QuietPulse Headphones",
            imageURL = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-b", name = "NorthHub Electronics"),
            price = BigDecimal(210),
            discountPercentage = BigDecimal(13),
            offerEndsAt = Date(System.currentTimeMillis() + 20 * 60 * 60 * 1000L),
            quantityRemaining = 14,
            summary = "Noise-cancelling headphones tuned for commuting and focused work.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Black", "White", "Blue"))
            )
        ),
        Product(
            id = "boot-1",
            name = "Trailmark Boots",
            imageURL = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-a", name = "BluePeak Sports"),
            price = BigDecimal(145),
            discountPercentage = BigDecimal(7),
            offerEndsAt = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 10,
            summary = "Durable outdoor boots with strong grip and weather-resistant materials.",
            options = listOf(
                ProductOption(name = "size", values = listOf("S", "M", "L", "XL", "XXL")),
                ProductOption(name = "color", values = listOf("Brown", "Black"))
            )
        ),
        Product(
            id = "bottle-1",
            name = "Thermo Steel Bottle",
            imageURL = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-a", name = "BluePeak Sports"),
            price = BigDecimal(32),
            discountPercentage = BigDecimal(6),
            offerEndsAt = Date(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 30,
            summary = "Insulated bottle that keeps drinks cold for long training days.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Blue", "Black", "White"))
            )
        ),
        Product(
            id = "mat-1",
            name = "Yoga Pro Mat",
            imageURL = "https://images.unsplash.com/photo-1592432676556-2815347329d7?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-a", name = "BluePeak Sports"),
            price = BigDecimal(55),
            discountPercentage = BigDecimal(0),
            offerEndsAt = null,
            quantityRemaining = 25,
            summary = "Extra thick non-slip yoga mat for all levels of practice.",
            options = listOf(
                ProductOption(name = "color", values = listOf("Purple", "Blue", "Gray"))
            )
        ),
        Product(
            id = "tablet-1",
            name = "NotePad Pro 11",
            imageURL = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-b", name = "NorthHub Electronics"),
            price = BigDecimal(450),
            discountPercentage = BigDecimal(5),
            offerEndsAt = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 12,
            summary = "Powerful tablet for creators and professionals on the go.",
            options = listOf(
                ProductOption(name = "storage", values = listOf("128GB", "256GB", "512GB"))
            )
        ),
        Product(
            id = "lamp-2",
            name = "Articulated Desk Light",
            imageURL = "https://images.unsplash.com/photo-1534073828943-f801091bb18c?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-d", name = "Casa Nova"),
            price = BigDecimal(42),
            discountPercentage = BigDecimal(10),
            offerEndsAt = Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 15,
            summary = "Adjustable desk lamp with energy-efficient LED bulb.",
            options = listOf(
                ProductOption(name = "color", values = listOf("White", "Black"))
            )
        ),
        Product(
            id = "pan-1",
            name = "Cast Iron Skillet",
            imageURL = "https://images.unsplash.com/photo-1590159443580-c13f615f7956?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-e", name = "Kitchen Fold"),
            price = BigDecimal(48),
            discountPercentage = BigDecimal(0),
            offerEndsAt = null,
            quantityRemaining = 20,
            summary = "Pre-seasoned cast iron skillet for perfect searing and baking.",
            options = emptyList()
        ),
        Product(
            id = "shirt-1",
            name = "Oxford Button Down",
            imageURL = "https://images.unsplash.com/photo-1598033129183-c4f50c7176c8?auto=format&fit=crop&w=900&q=80",
            vendor = Vendor(id = "vendor-c", name = "ThreadHaus"),
            price = BigDecimal(65),
            discountPercentage = BigDecimal(12),
            offerEndsAt = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L),
            quantityRemaining = 18,
            summary = "Classic oxford shirt made from premium cotton.",
            options = listOf(
                ProductOption(name = "size", values = listOf("S", "M", "L", "XL")),
                ProductOption(name = "color", values = listOf("White", "Light Blue"))
            )
        )
        // ... more can be added to match exactly
    )
}
