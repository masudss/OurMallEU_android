package eu.ourmall.ui.screens.payment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import eu.ourmall.models.cart.CurrencyUtils.toCurrencyText
import eu.ourmall.viewmodels.AppState
import eu.ourmall.ui.components.order.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

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
                    if (!appState.hasCompletedPayment) {
                        IconButton(onClick = { appState.path.removeLastOrNull() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
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
                    "Please make payment to confirm your purchase and alert the vendor to ship your item.",
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
