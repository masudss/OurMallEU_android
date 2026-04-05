package eu.ourmall.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import eu.ourmall.models.*
import eu.ourmall.viewmodels.AppState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsView(appState: AppState, orderId: String) {
    val order = appState.order(orderId)
    var pendingCancellation by remember { mutableStateOf<CancellationTarget?>(null) }
    var isProcessingCancellation by remember { mutableStateOf(false) }
    var refundAmount by remember { mutableStateOf<BigDecimal?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order details") },
                navigationIcon = {
                    IconButton(onClick = { appState.path.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (order != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2F2F7))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Order details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Track and manage your paid order while it is in transit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    OrderStatusSection(
                        order = order,
                        onCancelItem = { pendingCancellation = CancellationTarget.Item(it) },
                        onCancelVendor = { pendingCancellation = CancellationTarget.Vendor(it) },
                        onCancelOrder = { pendingCancellation = CancellationTarget.FullOrder }
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Text("Order unavailable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("This order could not be found.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            if (isProcessingCancellation) {
                PaymentProcessingOverlay(message = "Processing refund...")
            }

            refundAmount?.let { amount ->
                RefundSuccessDialog(refundAmount = amount, onDismiss = { refundAmount = null })
            }
        }
    }

    pendingCancellation?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingCancellation = null },
            title = { Text(target.title) },
            text = { Text(target.message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        order?.let { currentOrder ->
                            scope.launch {
                                isProcessingCancellation = true
                                val amountToRefund = target.refundAmount(currentOrder)
                                delay(2000)
                                when (target) {
                                    is CancellationTarget.Item -> appState.cancelOrderItem(currentOrder.id, target.item.id)
                                    is CancellationTarget.Vendor -> appState.cancelVendor(currentOrder.id, target.group.vendor.id)
                                    CancellationTarget.FullOrder -> appState.cancelOrder(currentOrder.id)
                                }
                                isProcessingCancellation = false
                                refundAmount = amountToRefund
                                pendingCancellation = null
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCancellation = null }) {
                    Text("No")
                }
            }
        )
    }
}
