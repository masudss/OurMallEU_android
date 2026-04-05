package eu.ourmall.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.ourmall.models.CurrencyUtils.toCurrencyText
import eu.ourmall.models.Product
import eu.ourmall.models.ProductSelection
import eu.ourmall.viewmodels.AppState
import kotlinx.coroutines.delay
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailView(product: Product, appState: AppState, onDismiss: () -> Unit) {
    var selection by remember { mutableStateOf(product.defaultSelection) }
    var showAddConfirmation by remember { mutableStateOf(false) }
    var showAddedToCartIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(showAddedToCartIndicator) {
        if (showAddedToCartIndicator) {
            delay(900)
            showAddedToCartIndicator = false
            onDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.shadow(8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Button(
                    onClick = { showAddConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = product.inStock,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (product.inStock) "Add ${selection.quantity} to cart" else "Unavailable")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(30.dp))
                ) {
                    AsyncImage(
                        model = product.imageURL,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Fallback gradient if image fails (simplified)
                    if (product.imageURL == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color.Blue.copy(alpha = 0.8f), Color.Cyan.copy(alpha = 0.7f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Photo,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            product.vendor.name.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Text(
                            product.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            product.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                product.discountedPrice.toCurrencyText(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (product.discountPercentage > BigDecimal.ZERO) {
                                Text(
                                    product.price.toCurrencyText(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(product.offerEndsText(), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    if (product.inStock) Icons.Default.Inventory2 else Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (product.inStock) Color.Green else Color.Red
                                )
                                Text(
                                    if (product.inStock) "In stock" else "Out of stock",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (product.inStock) Color.Green else Color.Red
                                )
                            }
                        }
                    }

                    if (product.options.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            product.options.forEach { option ->
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(option.displayName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expanded = true },
                                            color = Color(0xFFF2F2F7),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(selection.selectedOptions[option.name] ?: option.values.firstOrNull() ?: "Select")
                                                Spacer(Modifier.weight(1f))
                                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            option.values.forEach { value ->
                                                DropdownMenuItem(
                                                    text = { Text(value) },
                                                    onClick = {
                                                        val newOptions = selection.selectedOptions.toMutableMap()
                                                        newOptions[option.name] = value
                                                        selection = selection.copy(selectedOptions = newOptions)
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF2F2F7), RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("${selection.quantity}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            
                            Spacer(Modifier.weight(1f))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (selection.quantity > 1) selection = selection.copy(quantity = selection.quantity - 1) },
                                    enabled = product.inStock && selection.quantity > 1
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                IconButton(
                                    onClick = { if (selection.quantity < product.quantityRemaining) selection = selection.copy(quantity = selection.quantity + 1) },
                                    enabled = product.inStock && selection.quantity < product.quantityRemaining
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showAddedToCartIndicator,
                enter = scaleIn(animationSpec = spring(0.35f, 0.8f)) + fadeIn(),
                exit = fadeOut()
            ) {
                AddedToCartFeedbackOverlay()
            }
        }
    }

    if (showAddConfirmation) {
        AlertDialog(
            onDismissRequest = { showAddConfirmation = false },
            title = { Text("Add item to cart?") },
            text = { Text("Add ${product.name} to your cart?") },
            confirmButton = {
                Button(onClick = {
                    appState.addToCart(product, selection)
                    showAddConfirmation = false
                    showAddedToCartIndicator = true
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddConfirmation = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
private fun AddedToCartFeedbackOverlay() {
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
