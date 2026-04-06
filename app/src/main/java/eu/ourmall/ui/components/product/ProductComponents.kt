package eu.ourmall.ui.components.product

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.ourmall.models.cart.CurrencyUtils.toCurrencyText
import eu.ourmall.models.product.FilterCriteria
import eu.ourmall.models.product.Product
import kotlinx.coroutines.delay
import java.math.BigDecimal

@Composable
fun HeroCarouselView(banners: List<String>) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(Unit) {
        if (banners.size > 1) {
            while (true) {
                delay(3500)
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(28.dp))
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = banners[page],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                            startY = 100f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = when(page) {
                        0 -> "Fresh drops"
                        1 -> "Weekend deals"
                        else -> "Vendor spotlight"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Browse curated picks from independent vendors across fashion, tech, and home.",
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    criteria: FilterCriteria,
    availableCategories: List<String>,
    onApply: (FilterCriteria) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var currentCriteria by remember { mutableStateOf(criteria) }
    var minPriceInput by remember { mutableStateOf(criteria.minPrice?.toString() ?: "") }
    var maxPriceInput by remember { mutableStateOf(criteria.maxPrice?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    currentCriteria = FilterCriteria()
                    minPriceInput = ""
                    maxPriceInput = ""
                }) {
                    Text("Clear All")
                }
            }

            // Category Selection
            if (availableCategories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category", fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableCategories.forEach { category ->
                            FilterChip(
                                selected = currentCriteria.category == category,
                                onClick = {
                                    currentCriteria = currentCriteria.copy(
                                        category = if (currentCriteria.category == category) null else category
                                    )
                                },
                                label = { Text(category) }
                            )
                        }
                    }
                }
            }

            // Price Range
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Price Range", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minPriceInput,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                                minPriceInput = it
                            }
                        },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Text("-", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = maxPriceInput,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                                maxPriceInput = it
                            }
                        },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }

            // In Stock Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = currentCriteria.onlyInStock,
                        onValueChange = { currentCriteria = currentCriteria.copy(onlyInStock = it) },
                        role = Role.Checkbox
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("In Stock Only", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = currentCriteria.onlyInStock,
                    onCheckedChange = null // Handled by toggleable row
                )
            }

            Button(
                onClick = {
                    val finalCriteria = currentCriteria.copy(
                        minPrice = minPriceInput.toBigDecimalOrNull(),
                        maxPrice = maxPriceInput.toBigDecimalOrNull()
                    )
                    onApply(finalCriteria)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Filters", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun AddedToCartOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(86.dp)
                .shadow(18.dp, CircleShape),
            color = Color.Green,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(26.dp).size(34.dp)
            )
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    quantityInCart: Int,
    onTap: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(390.dp)
            .shadow(10.dp, RoundedCornerShape(26.dp))
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                AsyncImage(
                    model = product.imageURL,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (quantityInCart > 0) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                        color = Color.Blue,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            "$quantityInCart",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Text(
                product.vendor.name.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(48.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(28.dp)) {
                Text(
                    product.discountedPrice.toCurrencyText(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (product.discountPercentage > BigDecimal.ZERO) {
                    Text(
                        product.price.toCurrencyText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            Text(
                product.offerEndsText(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                modifier = Modifier.height(32.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.height(20.dp)) {
                Icon(
                    if (product.inStock) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (product.inStock) Color.Green else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    if (product.inStock) "In stock" else "Out of stock",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.inStock) Color.Green else Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth(),
                enabled = product.inStock,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (product.inStock) (if (quantityInCart > 0) "Add more" else "Add to cart") else "Unavailable")
            }
        }
    }
}
