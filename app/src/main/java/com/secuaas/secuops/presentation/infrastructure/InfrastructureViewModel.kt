package com.secuaas.secuops.presentation.infrastructure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secuaas.secuops.data.model.CertificateInfo
import com.secuaas.secuops.data.model.IngressInfo
import com.secuaas.secuops.data.model.PodInfo
import com.secuaas.secuops.data.model.ServiceInfo
import com.secuaas.secuops.data.repository.SecuOpsRepository
import com.secuaas.secuops.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfrastructureViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _podsState = MutableStateFlow<InfrastructureState<List<PodInfo>>>(InfrastructureState.Loading)
    val podsState: StateFlow<InfrastructureState<List<PodInfo>>> = _podsState.asStateFlow()

    private val _servicesState = MutableStateFlow<InfrastructureState<List<ServiceInfo>>>(InfrastructureState.Loading)
    val servicesState: StateFlow<InfrastructureState<List<ServiceInfo>>> = _servicesState.asStateFlow()

    private val _ingressesState = MutableStateFlow<InfrastructureState<List<IngressInfo>>>(InfrastructureState.Loading)
    val ingressesState: StateFlow<InfrastructureState<List<IngressInfo>>> = _ingressesState.asStateFlow()

    private val _certificatesState = MutableStateFlow<InfrastructureState<List<CertificateInfo>>>(InfrastructureState.Loading)
    val certificatesState: StateFlow<InfrastructureState<List<CertificateInfo>>> = _certificatesState.asStateFlow()

    private val _selectedEnvironment = MutableStateFlow("dev")
    val selectedEnvironment: StateFlow<String> = _selectedEnvironment.asStateFlow()

    private val _selectedNamespace = MutableStateFlow<String?>(null)
    val selectedNamespace: StateFlow<String?> = _selectedNamespace.asStateFlow()

    init {
        loadAll()
    }

    fun setEnvironment(environment: String) {
        _selectedEnvironment.value = environment
        loadAll()
    }

    fun setNamespace(namespace: String?) {
        _selectedNamespace.value = namespace
        loadAll()
    }

    fun loadAll() {
        loadPods()
        loadServices()
        loadIngresses()
        loadCertificates()
    }

    fun loadPods() {
        viewModelScope.launch {
            _podsState.value = InfrastructureState.Loading
            repository.getPods(_selectedNamespace.value, _selectedEnvironment.value).collect { resource ->
                _podsState.value = when (resource) {
                    is Resource.Loading -> InfrastructureState.Loading
                    is Resource.Success -> InfrastructureState.Success(resource.data ?: emptyList())
                    is Resource.Error -> InfrastructureState.Error(resource.message ?: "Failed to load pods")
                }
            }
        }
    }

    fun loadServices() {
        viewModelScope.launch {
            _servicesState.value = InfrastructureState.Loading
            repository.getServices(_selectedNamespace.value, _selectedEnvironment.value).collect { resource ->
                _servicesState.value = when (resource) {
                    is Resource.Loading -> InfrastructureState.Loading
                    is Resource.Success -> InfrastructureState.Success(resource.data ?: emptyList())
                    is Resource.Error -> InfrastructureState.Error(resource.message ?: "Failed to load services")
                }
            }
        }
    }

    fun loadIngresses() {
        viewModelScope.launch {
            _ingressesState.value = InfrastructureState.Loading
            repository.getInfrastructure().collect { resource ->
                _ingressesState.value = when (resource) {
                    is Resource.Loading -> InfrastructureState.Loading
                    is Resource.Success -> InfrastructureState.Success(resource.data ?: emptyList())
                    is Resource.Error -> InfrastructureState.Error(resource.message ?: "Failed to load ingresses")
                }
            }
        }
    }

    fun loadCertificates() {
        viewModelScope.launch {
            _certificatesState.value = InfrastructureState.Loading
            repository.getCertificates(_selectedNamespace.value, _selectedEnvironment.value).collect { resource ->
                _certificatesState.value = when (resource) {
                    is Resource.Loading -> InfrastructureState.Loading
                    is Resource.Success -> InfrastructureState.Success(resource.data ?: emptyList())
                    is Resource.Error -> InfrastructureState.Error(resource.message ?: "Failed to load certificates")
                }
            }
        }
    }
}

sealed class InfrastructureState<out T> {
    object Loading : InfrastructureState<Nothing>()
    data class Success<T>(val data: T) : InfrastructureState<T>()
    data class Error(val message: String) : InfrastructureState<Nothing>()
}
