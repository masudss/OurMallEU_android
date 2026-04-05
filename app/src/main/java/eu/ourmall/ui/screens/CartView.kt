package eu.ourmall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.ourmall.models.CartItem
import eu.ourmall.models.CheckoutTotals
import eu.ourmall.models.CurrencyUtils.toCurrencyText
import eu.ourmall.models.VendorCartSection
import eu.ourmall.viewmodels.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartView(appState: AppState) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cart") })
        },
        bottomBar = {
            if (appState.vendorSections.isNotEmpty()) {
                Surface(
                    modifier = Modifier.shadow(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    CartSummaryBar(
                        totals = appState.cartTotals,
                        selectionCount = appState.selectedSections.size,
                        onCheckout = { appState.goToCheckout() }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF2F2F7))) {
            if (appState.vendorSections.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Text("Your cart is empty", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(appState.vendorSections) { section ->
                        VendorCartCard(section = section, appState = appState)
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorCartCard(section: VendorCartSection, appState: AppState) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = { appState.toggleVendorSelection(section.vendor.id) }) {
                    Icon(
                        if (section.isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Toggle Selection",
                        tint = if (section.isSelected) Color.Blue else Color.Gray
                    )
                }

                Column {
                    Text(section.vendor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Vendor total: ${section.subtotal.toCurrencyText()}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            section.items.forEachIndexed { index, item ->
                CartItemRow(item = item, appState = appState)
                if (index < section.items.size - 1) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, appState: AppState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(item.selectedOptionsText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("${item.product.discountedPrice.toCurrencyText()} each", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Qty ${item.quantity}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                
                // Simplified Stepper replacement for Android
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { appState.updateQuantity(item.id, item.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    IconButton(
                        onClick = { appState.updateQuantity(item.id, item.quantity + 1) },
                        enabled = item.quantity < item.product.quantityRemaining,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            Text(item.totalPrice.toCurrencyText(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CartSummaryBar(totals: CheckoutTotals, selectionCount: Int, onCheckout: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Selected vendors", color = Color.Gray)
            Spacer(Modifier.weight(1f))
            Text("$selectionCount", fontWeight = FontWeight.SemiBold)
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Grand total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(totals.grandTotal.toCurrencyText(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectionCount > 0,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Checkout")
        }
    }
}
