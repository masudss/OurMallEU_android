package eu.ourmall.ui.screens.product

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.ourmall.models.product.FilterCriteria
import eu.ourmall.models.product.Product
import eu.ourmall.viewmodels.AppState
import eu.ourmall.ui.components.product.*
import kotlinx.coroutines.delay

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

            AnimatedVisibility(
                visible = showAddedToCartIndicator,
                enter = scaleIn(animationSpec = androidx.compose.animation.core.spring(0.35f, 0.8f)) + fadeIn(),
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
