package com.secuaas.secuops.presentation.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secuaas.secuops.data.model.Application
import com.secuaas.secuops.data.repository.SecuOpsRepository
import com.secuaas.secuops.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _applicationsState = MutableStateFlow<ApplicationsState>(ApplicationsState.Loading)
    val applicationsState: StateFlow<ApplicationsState> = _applicationsState.asStateFlow()

    init {
        loadApplications()
    }

    fun loadApplications() {
        viewModelScope.launch {
            _applicationsState.value = ApplicationsState.Loading
            repository.getApplications().collect { resource ->
                _applicationsState.value = when (resource) {
                    is Resource.Loading -> ApplicationsState.Loading
                    is Resource.Success -> ApplicationsState.Success(resource.data ?: emptyList())
                    is Resource.Error -> ApplicationsState.Error(resource.message ?: "Failed to load applications")
                }
            }
        }
    }

    fun restartApplication(name: String) {
        viewModelScope.launch {
            repository.restartApplication(name).collect { resource ->
                when (resource) {
                    is Resource.Success -> loadApplications()
                    is Resource.Error -> {
                        // TODO: Show error toast
                    }
                    else -> {}
                }
            }
        }
    }
}

sealed class ApplicationsState {
    object Loading : ApplicationsState()
    data class Success(val applications: List<Application>) : ApplicationsState()
    data class Error(val message: String) : ApplicationsState()
}
