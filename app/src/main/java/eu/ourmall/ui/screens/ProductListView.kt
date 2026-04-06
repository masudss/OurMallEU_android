package eu.ourmall.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.ourmall.models.CurrencyUtils.toCurrencyText
import eu.ourmall.models.FilterCriteria
import eu.ourmall.models.Product
import eu.ourmall.viewmodels.AppState
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListView(appState: AppState) {
    var productPendingConfirmation by remember { mutableStateOf<Product?>(null) }
    var showAddedToCartIndicator by remember { mutableStateOf(false) }
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    val horizontalPadding = 16.dp
    val gridSpacing = 14.dp

    LaunchedEffect(showAddedToCartIndicator) {
        if (showAddedToCartIndicator) {
            delay(900)
            showAddedToCartIndicator = false
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Products") },
                    navigationIcon = {
                        TextButton(onClick = { appState.goToOrders() }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text("Orders", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                if (appState.ordersCount > 0) {
                                    Surface(
                                        color = Color.Blue,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            "${appState.ordersCount}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        Box(modifier = Modifier
                            .padding(end = 10.dp)
                            .clickable { appState.goToCart() }) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(24.dp)
                            )
                            if (appState.cartCount > 0) {
                                Surface(
                                    color = Color.Red,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
                                ) {
                                    Text(
                                        "${appState.cartCount}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                )

                // Search Bar and Filter Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = appState.searchQuery,
                        onValueChange = { appState.searchQuery = it },
                        placeholder = { Text("Search products...") },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (appState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { appState.searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true
                    )

                    IconButton(
                        onClick = { isFilterSheetOpen = true },
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = if (appState.filterCriteria != FilterCriteria()) MaterialTheme.colorScheme.primaryContainer else Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        BadgedBox(
                            badge = {
                                if (appState.filterCriteria != FilterCriteria()) {
                                    Badge()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (appState.filterCriteria != FilterCriteria()) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))) {
            if (appState.isLoadingProducts && appState.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Loading products...")
                    }
                }
            } else if (appState.productErrorMessage != null && appState.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("Products unavailable", fontWeight = FontWeight.Bold)
                        Text(appState.productErrorMessage ?: "")
                        Button(onClick = { appState.retryLoadingProducts() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                val products = appState.filteredProducts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (appState.searchQuery.isEmpty() && appState.filterCriteria == FilterCriteria()) {
                        item {
                            HeroCarouselView(banners = appState.heroBanners)
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                if (appState.searchQuery.isNotEmpty() || appState.filterCriteria != FilterCriteria()) "Search results" else "Featured products",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (products.isEmpty()) {
                                Text(
                                    "No products found matching your criteria.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    "Showing ${products.size} products",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    val chunkedProducts = products.chunked(2)
                    itemsIndexed(chunkedProducts) { index, row ->
                        Row(
                            modifier = Modifier
                                .padding(horizontal = horizontalPadding)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gridSpacing)
                        ) {
                            row.forEach { product ->
                                ProductGridCard(
                                    product = product,
                                    quantityInCart = appState.quantityInCart(product),
                                    onTap = { appState.goToProduct(product) },
                                    onAddToCart = { productPendingConfirmation = product },
                                    modifier = Modifier.weight(1f)
                                )
                                // Trigger loading next page only if not filtering
                                if (product == products.last() && appState.searchQuery.isEmpty() && appState.filterCriteria == FilterCriteria()) {
                                    LaunchedEffect(product) {
                                        appState.loadNextPageIfNeeded(product)
                                    }
                                }
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(gridSpacing))
                    }

                    if (appState.isLoadingNextPage && appState.searchQuery.isEmpty() && appState.filterCriteria == FilterCriteria()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    if (appState.productErrorMessage != null && appState.products.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = horizontalPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(appState.productErrorMessage ?: "", color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Added to Cart Overlay
            AnimatedVisibility(
                visible = showAddedToCartIndicator,
                enter = scaleIn(animationSpec = spring(0.35f, 0.8f)) + fadeIn(),
                exit = fadeOut()
            ) {
                AddedToCartOverlay()
            }
        }
    }

    if (isFilterSheetOpen) {
        FilterBottomSheet(
            criteria = appState.filterCriteria,
            availableCategories = appState.allCategories,
            onApply = { 
                appState.updateFilter(it)
                isFilterSheetOpen = false
            },
            onDismiss = { isFilterSheetOpen = false }
        )
    }

    productPendingConfirmation?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingConfirmation = null },
            title = { Text("Add item to cart?") },
            text = { Text("Add ${product.name} to your cart?") },
            confirmButton = {
                Button(onClick = {
                    appState.addToCart(product)
                    productPendingConfirmation = null
                    showAddedToCartIndicator = true
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { productPendingConfirmation = null }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
private fun HeroCarouselView(banners: List<String>) {
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
private fun AddedToCartOverlay() {
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
private fun ProductGridCard(
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
