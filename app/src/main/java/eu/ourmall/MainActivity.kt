package eu.ourmall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import eu.ourmall.ui.screens.cart.CartView
import eu.ourmall.ui.screens.cart.CheckoutView
import eu.ourmall.ui.screens.common.SplashView
import eu.ourmall.ui.screens.order.OrderDetailsView
import eu.ourmall.ui.screens.order.OrdersView
import eu.ourmall.ui.screens.payment.PaymentView
import eu.ourmall.ui.screens.product.ProductDetailView
import eu.ourmall.ui.screens.product.ProductListView
import eu.ourmall.ui.theme.OurMallEUTheme
import eu.ourmall.viewmodels.AppState
import eu.ourmall.viewmodels.navigation.AppRoute

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
            null, is AppRoute.Home -> ProductListView(appState)
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
