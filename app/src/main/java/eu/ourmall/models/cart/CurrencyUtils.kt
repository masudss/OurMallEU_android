package eu.ourmall.models.cart

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    val vatRate = BigDecimal("0.075")

    fun BigDecimal.rounded(scale: Int = 2): BigDecimal {
        return this.setScale(scale, RoundingMode.HALF_EVEN)
    }

    fun BigDecimal.toCurrencyText(): String {
        val nairaLocale = Locale("en", "NG")
        val formatter = NumberFormat.getCurrencyInstance(nairaLocale)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(this.rounded())
    }
}
