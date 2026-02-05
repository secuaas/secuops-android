package com.secuaas.secuops.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secuaas.secuops.data.model.LoginResponse
import com.secuaas.secuops.data.repository.SecuOpsRepository
import com.secuaas.secuops.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _loginState.value = LoginState.Error("Email and password are required")
            return
        }

        viewModelScope.launch {
            repository.login(_email.value, _password.value).collect { resource ->
                _loginState.value = when (resource) {
                    is Resource.Loading -> LoginState.Loading
                    is Resource.Success -> LoginState.Success(resource.data!!)
                    is Resource.Error -> LoginState.Error(resource.message ?: "Login failed")
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val loginResponse: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}
