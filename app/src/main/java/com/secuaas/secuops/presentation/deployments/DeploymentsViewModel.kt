package com.secuaas.secuops.presentation.deployments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secuaas.secuops.data.model.Deployment
import com.secuaas.secuops.data.repository.SecuOpsRepository
import com.secuaas.secuops.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeploymentsViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _deploymentsState = MutableStateFlow<DeploymentsState>(DeploymentsState.Loading)
    val deploymentsState: StateFlow<DeploymentsState> = _deploymentsState.asStateFlow()

    private val _filterStatus = MutableStateFlow<String?>(null)
    val filterStatus: StateFlow<String?> = _filterStatus.asStateFlow()

    init {
        loadDeployments()
    }

    fun setStatusFilter(status: String?) {
        _filterStatus.value = status
        loadDeployments()
    }

    fun loadDeployments() {
        viewModelScope.launch {
            _deploymentsState.value = DeploymentsState.Loading
            repository.getDeployments(
                status = _filterStatus.value
            ).collect { resource ->
                _deploymentsState.value = when (resource) {
                    is Resource.Loading -> DeploymentsState.Loading
                    is Resource.Success -> DeploymentsState.Success(resource.data ?: emptyList())
                    is Resource.Error -> DeploymentsState.Error(resource.message ?: "Failed to load deployments")
                }
            }
        }
    }

    fun retryDeployment(id: String) {
        viewModelScope.launch {
            repository.retryDeployment(id).collect { resource ->
                when (resource) {
                    is Resource.Success -> loadDeployments()
                    else -> {}
                }
            }
        }
    }
}

sealed class DeploymentsState {
    object Loading : DeploymentsState()
    data class Success(val deployments: List<Deployment>) : DeploymentsState()
    data class Error(val message: String) : DeploymentsState()
}
