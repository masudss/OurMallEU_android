package eu.ourmall.ui.components.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.ourmall.models.cart.CartItem
import eu.ourmall.models.cart.CheckoutTotals
import eu.ourmall.models.cart.CurrencyUtils.toCurrencyText
import eu.ourmall.models.cart.VendorCartSection
import eu.ourmall.viewmodels.AppState

@Composable
fun VendorCartCard(section: VendorCartSection, appState: AppState) {
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
fun CartItemRow(item: CartItem, appState: AppState) {
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
fun CartSummaryBar(totals: CheckoutTotals, selectionCount: Int, onCheckout: () -> Unit) {
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
