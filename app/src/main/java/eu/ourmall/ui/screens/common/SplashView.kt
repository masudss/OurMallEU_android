package eu.ourmall.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashView() {
    val brandBlue = Color(red = 0.07f, green = 0.35f, blue = 0.88f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brandBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.16f), CircleShape)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart, // Closest to storefront.fill
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.White
                )
            }

            Text(
                text = "OurMallEU",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Shop trusted vendors in one cart",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp
            )
        }
    }
}

@Preview
@Composable
fun SplashViewPreview() {
    SplashView()
}
