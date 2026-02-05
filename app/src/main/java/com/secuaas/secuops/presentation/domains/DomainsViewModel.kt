package com.secuaas.secuops.presentation.domains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secuaas.secuops.data.model.DomainRecord
import com.secuaas.secuops.data.repository.SecuOpsRepository
import com.secuaas.secuops.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DomainsState {
    object Loading : DomainsState()
    data class Success(val records: List<DomainRecord>) : DomainsState()
    data class Error(val message: String) : DomainsState()
}

@HiltViewModel
class DomainsViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _domainsState = MutableStateFlow<DomainsState>(DomainsState.Loading)
    val domainsState: StateFlow<DomainsState> = _domainsState.asStateFlow()

    private val _zoneFilter = MutableStateFlow<String?>(null)
    val zoneFilter: StateFlow<String?> = _zoneFilter.asStateFlow()

    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter.asStateFlow()

    init {
        loadDomains()
    }

    fun loadDomains() {
        viewModelScope.launch {
            _domainsState.value = DomainsState.Loading
            repository.getDomainRecords(
                zone = _zoneFilter.value,
                recordType = _typeFilter.value
            ).collect { resource ->
                _domainsState.value = when (resource) {
                    is Resource.Loading -> DomainsState.Loading
                    is Resource.Success -> DomainsState.Success(resource.data ?: emptyList())
                    is Resource.Error -> DomainsState.Error(resource.message ?: "Unknown error")
                }
            }
        }
    }

    fun setZoneFilter(zone: String?) {
        _zoneFilter.value = zone
        loadDomains()
    }

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
        loadDomains()
    }

    fun deleteDomain(zone: String, recordId: String) {
        viewModelScope.launch {
            repository.deleteDomainRecord(zone, recordId).collect { resource ->
                when (resource) {
                    is Resource.Success -> loadDomains()
                    is Resource.Error -> {
                        // Show error (could be handled with a separate state)
                    }
                    else -> {}
                }
            }
        }
    }
}
