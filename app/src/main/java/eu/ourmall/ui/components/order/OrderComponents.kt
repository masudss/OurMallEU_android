package eu.ourmall.ui.components.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.ourmall.models.order.*
import eu.ourmall.models.cart.CurrencyUtils.toCurrencyText
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
fun PaymentSuccessDialog(onDismiss: () -> Unit) {
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
