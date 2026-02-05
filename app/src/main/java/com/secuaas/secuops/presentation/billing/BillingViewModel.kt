package com.secuaas.secuops.presentation.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secuaas.secuops.data.model.BillingSummary
import com.secuaas.secuops.data.model.Invoice
import com.secuaas.secuops.data.repository.SecuOpsRepository
import com.secuaas.secuops.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BillingState {
    object Loading : BillingState()
    data class Success(
        val summary: BillingSummary,
        val invoices: List<Invoice>
    ) : BillingState()
    data class Error(val message: String) : BillingState()
}

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Loading)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    init {
        loadBilling()
    }

    fun loadBilling() {
        viewModelScope.launch {
            _billingState.value = BillingState.Loading

            // Load both summary and invoices
            var summary: BillingSummary? = null
            var invoices: List<Invoice>? = null
            var error: String? = null

            // Get summary
            repository.getBillingSummary().collect { resource ->
                when (resource) {
                    is Resource.Success -> summary = resource.data
                    is Resource.Error -> error = resource.message
                    else -> {}
                }
            }

            // Get invoices
            repository.getInvoices().collect { resource ->
                when (resource) {
                    is Resource.Success -> invoices = resource.data
                    is Resource.Error -> if (error == null) error = resource.message
                    else -> {}
                }
            }

            _billingState.value = if (summary != null && invoices != null) {
                BillingState.Success(summary!!, invoices!!)
            } else {
                BillingState.Error(error ?: "Failed to load billing data")
            }
        }
    }
}
