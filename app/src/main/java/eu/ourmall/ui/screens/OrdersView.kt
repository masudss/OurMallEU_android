package eu.ourmall.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.ourmall.models.*
import eu.ourmall.models.CurrencyUtils.toCurrencyText
import eu.ourmall.viewmodels.AppState
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

enum class OrdersFilter(val title: String) {
    IN_PROGRESS("In progress"),
    SETTLED("Settled");

    val emptyTitle: String get() = when(this) {
        IN_PROGRESS -> "No in-progress orders"
        SETTLED -> "No settled orders"
    }

    val emptyMessage: String get() = when(this) {
        IN_PROGRESS -> "Paid orders that are still moving through fulfillment will appear here."
        SETTLED -> "Orders where every item is delivered or cancelled will appear here."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersView(appState: AppState) {
    var selectedFilter by remember { mutableStateOf(OrdersFilter.IN_PROGRESS) }
    val expandedOrderIDs = remember { mutableStateListOf<String>() }

    val filteredOrders = remember(selectedFilter, appState.successfulOrders.size) {
        when (selectedFilter) {
            OrdersFilter.IN_PROGRESS -> appState.activeOrders
            OrdersFilter.SETTLED -> appState.settledOrders
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders") },
                navigationIcon = {
                    IconButton(onClick = { appState.path.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF2F2F7))
        ) {
            OrderFilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            if (selectedFilter == OrdersFilter.IN_PROGRESS) Icons.Default.Inventory else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Text(selectedFilter.emptyTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(selectedFilter.emptyMessage, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(filteredOrders) { order ->
                        OrderHistoryCard(
                            order = order,
                            showsTracking = selectedFilter == OrdersFilter.IN_PROGRESS,
                            isExpanded = expandedOrderIDs.contains(order.id),
                            onToggleExpansion = {
                                if (expandedOrderIDs.contains(order.id)) expandedOrderIDs.remove(order.id)
                                else expandedOrderIDs.add(order.id)
                            },
                            onOpenDetails = { appState.goToOrderDetails(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderFilterChips(selectedFilter: OrdersFilter, onFilterSelected: (OrdersFilter) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OrdersFilter.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                modifier = Modifier.clickable { onFilterSelected(filter) },
                color = if (isSelected) Color.Blue else Color.White,
                shape = CircleShape,
                border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(
                    text = filter.title,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else Color.Black,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(
    order: Order,
    showsTracking: Boolean,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.clickable { onToggleExpansion() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Order ${order.id.take(8)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(dateFormatter.format(order.createdAt), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Surface(
                    color = (if (order.isCancelled) Color.Red else (if (order.isSettled) Color.Green else Color.Blue)).copy(alpha = 0.12f),
                    shape = CircleShape
                ) {
                    Text(
                        order.displayStatusTitle,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = if (order.isCancelled) Color.Red else (if (order.isSettled) Color.Green else Color.Blue),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp),
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    order.vendorGroups.forEach { group ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF2F2F7), RoundedCornerShape(18.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(group.vendor.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

                            group.items.forEach { item ->
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            item.selectedOptions.entries.joinToString(" • ") { "${it.key}: ${it.value}" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        Text("Qty ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }

                                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            (item.unitPrice.multiply(BigDecimal(item.quantity))).toCurrencyText(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            item.status.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (item.status == ItemStatus.CANCELLED) Color.Red else Color.Gray
                                        )
                                    }
                                }

                                if (showsTracking) {
                                    ItemTrackingProgressView(status = item.status)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onOpenDetails,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View order details")
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemTrackingProgressView(status: ItemStatus) {
    val steps = listOf(ItemStatus.PENDING, ItemStatus.CONFIRMED, ItemStatus.SHIPPED, ItemStatus.DELIVERED)
    
    fun stepIndex(s: ItemStatus): Int = when(s) {
        ItemStatus.PENDING -> 0
        ItemStatus.CONFIRMED -> 1
        ItemStatus.SHIPPED -> 2
        ItemStatus.DELIVERED -> 3
        ItemStatus.CANCELLED -> 3
    }

    val currentIdx = stepIndex(status)

    Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            steps.forEachIndexed { index, step ->
                val isCompleted = status != ItemStatus.CANCELLED && index <= currentIdx
                val isCurrent = step == status
                
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (status == ItemStatus.CANCELLED) Color.Gray.copy(alpha = 0.35f)
                            else if (isCompleted) (if (step == ItemStatus.DELIVERED) Color.Green else Color.Blue)
                            else Color.Gray.copy(alpha = 0.25f),
                            CircleShape
                        )
                )

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(
                                if (status == ItemStatus.CANCELLED) Color.Gray.copy(alpha = 0.35f)
                                else if (index < currentIdx) Color.Blue
                                else Color.Gray.copy(alpha = 0.22f)
                            )
                    )
                }
            }
        }

        Row {
            steps.forEach { step ->
                Text(
                    step.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = if (status == ItemStatus.CANCELLED) Color.Gray else (if (step == status) Color.Black else Color.Gray),
                    fontWeight = if (step == status) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
