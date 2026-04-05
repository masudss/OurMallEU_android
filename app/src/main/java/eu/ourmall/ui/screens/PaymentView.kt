package eu.ourmall.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.ourmall.models.*
import eu.ourmall.models.CurrencyUtils.toCurrencyText
import eu.ourmall.viewmodels.AppState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed class CancellationTarget {
    data class Item(val item: OrderItem) : CancellationTarget()
    data class Vendor(val group: VendorOrderGroup) : CancellationTarget()
    object FullOrder : CancellationTarget()

    val title: String get() = when(this) {
        is Item -> "Cancel item?"
        is Vendor -> "Cancel vendor order?"
        FullOrder -> "Cancel entire order?"
    }

    val message: String get() = when(this) {
        is Item -> "This will cancel ${item.productName} only."
        is Vendor -> "This will cancel all items from ${group.vendor.name}."
        FullOrder -> "This will cancel every vendor and item in this order."
    }

    fun refundAmount(order: Order): BigDecimal {
        return when(this) {
            is Item -> if (item.status != ItemStatus.CANCELLED) (item.unitPrice.multiply(BigDecimal(item.quantity))) else BigDecimal.ZERO
            is Vendor -> group.items.filter { it.status != ItemStatus.CANCELLED }
                .fold(BigDecimal.ZERO) { acc, item -> acc.add(item.unitPrice.multiply(BigDecimal(item.quantity))) }
            FullOrder -> order.vendorGroups.flatMap { it.items }.filter { it.status != ItemStatus.CANCELLED }
                .fold(BigDecimal.ZERO) { acc, item -> acc.add(item.unitPrice.multiply(BigDecimal(item.quantity))) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentView(appState: AppState) {
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }
    var pendingCancellation by remember { mutableStateOf<CancellationTarget?>(null) }
    var isProcessingCancellation by remember { mutableStateOf(false) }
    var refundAmount by remember { mutableStateOf<BigDecimal?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(appState.hasCompletedPayment) {
        if (appState.hasCompletedPayment) {
            showPaymentSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    appState.paymentReference?.let {
                        Text(
                            "Payment reference: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (!appState.hasCompletedPayment) {
                        Button(
                            onClick = { appState.submitPayment() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !appState.isSubmittingPayment,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (appState.isSubmittingPayment) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Pay now")
                            }
                        }
                    } else {
                        Button(
                            onClick = { appState.goHome() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Go home")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF2F2F7))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Your payment request groups products by vendor and sends that dictionary object to the payment API.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            appState.checkoutTotals.grandTotal.toCurrencyText(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                appState.paymentErrorMessage?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Text(it, color = Color.Red)
                    }
                }

                appState.currentOrder?.let { order ->
                    OrderStatusSection(
                        order = order,
                        onCancelItem = { pendingCancellation = CancellationTarget.Item(it) },
                        onCancelVendor = { pendingCancellation = CancellationTarget.Vendor(it) },
                        onCancelOrder = { pendingCancellation = CancellationTarget.FullOrder }
                    )
                }
            }

            if (appState.isSubmittingPayment) {
                PaymentProcessingOverlay()
            }

            if (isProcessingCancellation) {
                PaymentProcessingOverlay(message = "Processing refund...")
            }

            if (showPaymentSuccessDialog) {
                PaymentSuccessDialog(onDismiss = { showPaymentSuccessDialog = false })
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
                        appState.currentOrder?.let { currentOrder ->
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

@Composable
fun OrderStatusSection(
    order: Order,
    onCancelItem: (OrderItem) -> Unit,
    onCancelVendor: (VendorOrderGroup) -> Unit,
    onCancelOrder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Order status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(
                order.displayStatusTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (order.isCancelled) Color.Red else (if (order.isSettled) Color.Green else Color.Blue)
            )
        }

        order.vendorGroups.forEach { group ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.vendor.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            group.displayStatusTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (group.isCancelled) Color.Red else (if (group.isSettled) Color.Green else Color.Gray)
                        )
                    }

                    Button(
                        onClick = { onCancelVendor(group) },
                        enabled = !group.isSettled,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Cancel vendor", fontSize = 12.sp)
                    }
                }

                group.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                item.selectedOptions.entries.joinToString(" • ") { "${it.key}: ${it.value}" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text("Qty ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Text(
                            item.status.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (item.status == ItemStatus.CANCELLED) Color.Red else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        TextButton(
                            onClick = { onCancelItem(item) },
                            enabled = !item.status.isSettled
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onCancelOrder,
            modifier = Modifier.fillMaxWidth(),
            enabled = !order.isSettled,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cancel entire order")
        }
    }
}

@Composable
fun PaymentProcessingOverlay(message: String = "Processing payment...") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            }
            Text(message, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentSuccessDialog(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.Blue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsBike, // Closest to bicycle
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = Color.Blue
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Items are on the way", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Your payment was successful and your order is now being prepared for delivery.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Okay")
                }
            }
        }
    }
}

@Composable
fun RefundSuccessDialog(refundAmount: BigDecimal, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.Green.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SettingsBackupRestore, // Closest to arrow.uturn.backward
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = Color.Green
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Refund successful", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "You have been refunded ${refundAmount.toCurrencyText()} for the cancelled items.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Okay")
                }
            }
        }
    }
}
