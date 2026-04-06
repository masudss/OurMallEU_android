package eu.ourmall.viewmodels.payment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PaymentViewModel {
    var isSubmittingPayment by mutableStateOf(false)
        private set

    var paymentErrorMessage by mutableStateOf<String?>(null)
        private set

    var paymentReference by mutableStateOf<String?>(null)
        private set

    var hasCompletedPayment by mutableStateOf(false)
        private set

    fun startPayment() {
        isSubmittingPayment = true
        paymentErrorMessage = null
        paymentReference = null
        hasCompletedPayment = false
    }

    fun completePayment(reference: String) {
        paymentReference = reference
        hasCompletedPayment = true
        isSubmittingPayment = false
    }

    fun failPayment(message: String?) {
        paymentErrorMessage = message
        hasCompletedPayment = false
        isSubmittingPayment = false
    }

    fun resetState() {
        hasCompletedPayment = false
        paymentErrorMessage = null
        paymentReference = null
    }
}
