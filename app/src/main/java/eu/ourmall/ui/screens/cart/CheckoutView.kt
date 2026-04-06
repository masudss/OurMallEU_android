package eu.ourmall.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.ourmall.models.cart.CurrencyUtils.toCurrencyText
import eu.ourmall.viewmodels.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutView(appState: AppState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { appState.path.removeLastOrNull() }) {
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
                    onClick = { appState.goToPayment() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continue to payment")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF2F2F7))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            appState.selectedSections.forEach { section ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(section.vendor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        section.items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(item.product.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(item.selectedOptionsText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text("Qty ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text(item.totalPrice.toCurrencyText(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Price breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    BreakdownRow(title = "Subtotal", value = appState.checkoutTotals.subtotal.toCurrencyText())
                    BreakdownRow(title = "Discounts", value = "-${appState.checkoutTotals.discount.toCurrencyText()}")
                    BreakdownRow(title = "VAT", value = appState.checkoutTotals.vat.toCurrencyText())
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    BreakdownRow(
                        title = "Total",
                        value = appState.checkoutTotals.grandTotal.toCurrencyText(),
                        emphasized = true
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(title: String, value: String, emphasized: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
    }
}
