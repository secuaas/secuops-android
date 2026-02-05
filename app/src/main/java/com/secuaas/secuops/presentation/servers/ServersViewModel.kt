package com.secuaas.secuops.presentation.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secuaas.secuops.data.remote.Server
import com.secuaas.secuops.data.remote.ServerDetail
import com.secuaas.secuops.data.repository.SecuOpsRepository
import com.secuaas.secuops.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ServersState {
    object Loading : ServersState()
    data class Success(val servers: List<Server>) : ServersState()
    data class Error(val message: String) : ServersState()
}

sealed class ServerDetailState {
    object Idle : ServerDetailState()
    object Loading : ServerDetailState()
    data class Success(val detail: ServerDetail) : ServerDetailState()
    data class Error(val message: String) : ServerDetailState()
}

@HiltViewModel
class ServersViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _serversState = MutableStateFlow<ServersState>(ServersState.Loading)
    val serversState: StateFlow<ServersState> = _serversState.asStateFlow()

    private val _serverDetailState = MutableStateFlow<ServerDetailState>(ServerDetailState.Idle)
    val serverDetailState: StateFlow<ServerDetailState> = _serverDetailState.asStateFlow()

    init {
        loadServers()
    }

    fun loadServers() {
        viewModelScope.launch {
            _serversState.value = ServersState.Loading
            repository.getServers().collect { resource ->
                _serversState.value = when (resource) {
                    is Resource.Loading -> ServersState.Loading
                    is Resource.Success -> ServersState.Success(resource.data ?: emptyList())
                    is Resource.Error -> ServersState.Error(resource.message ?: "Unknown error")
                }
            }
        }
    }

    fun loadServerDetail(serverId: String) {
        viewModelScope.launch {
            _serverDetailState.value = ServerDetailState.Loading
            repository.getServerDetail(serverId).collect { resource ->
                _serverDetailState.value = when (resource) {
                    is Resource.Loading -> ServerDetailState.Loading
                    is Resource.Success -> resource.data?.let { ServerDetailState.Success(it) }
                        ?: ServerDetailState.Error("Server not found")
                    is Resource.Error -> ServerDetailState.Error(resource.message ?: "Unknown error")
                }
            }
        }
    }

    fun rebootServer(serverId: String) {
        viewModelScope.launch {
            repository.rebootServer(serverId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        // Reload server detail to get updated status
                        loadServerDetail(serverId)
                    }
                    is Resource.Error -> {
                        // Show error (could be handled with a separate state)
                    }
                }
            }
        }
    }
}
