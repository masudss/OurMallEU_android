package eu.ourmall.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.ourmall.viewmodels.AppState
import eu.ourmall.ui.components.cart.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartView(appState: AppState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = {
                    IconButton(onClick = { appState.path.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
