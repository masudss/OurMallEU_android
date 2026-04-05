package eu.ourmall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import eu.ourmall.ui.theme.OurMallEUTheme

import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import eu.ourmall.ui.screens.*
import eu.ourmall.viewmodels.AppRoute
import eu.ourmall.viewmodels.AppState

class MainActivity : ComponentActivity() {
    private val appState: AppState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OurMallEUTheme {
                MainContent(appState)
            }
        }
    }
}

@Composable
fun MainContent(appState: AppState) {
    LaunchedEffect(Unit) {
        appState.start()
    }

    Crossfade(targetState = appState.isShowingSplash, label = "SplashTransition") { isShowingSplash ->
        if (isShowingSplash) {
            SplashView()
        } else {
            NavigationWrapper(appState)
        }
    }
}

@Composable
fun NavigationWrapper(appState: AppState) {
    // Basic stack-based navigation mirroring SwiftUI's NavigationStack(path:)
    val currentRoute = appState.path.lastOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentRoute) {
            null -> ProductListView(appState)
            is AppRoute.Cart -> CartView(appState)
            is AppRoute.ProductDetail -> ProductDetailView(
                product = currentRoute.product,
                appState = appState,
                onDismiss = { appState.goHome() }
            )
            is AppRoute.Orders -> OrdersView(appState)
            is AppRoute.OrderDetails -> OrderDetailsView(appState, currentRoute.orderId)
            is AppRoute.Checkout -> CheckoutView(appState)
            is AppRoute.Payment -> PaymentView(appState)
        }
    }
}
