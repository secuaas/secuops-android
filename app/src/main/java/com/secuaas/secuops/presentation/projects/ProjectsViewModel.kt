package com.secuaas.secuops.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secuaas.secuops.data.model.Project
import com.secuaas.secuops.data.repository.SecuOpsRepository
import com.secuaas.secuops.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _projectsState = MutableStateFlow<ProjectsState>(ProjectsState.Loading)
    val projectsState: StateFlow<ProjectsState> = _projectsState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _projectsState.value = ProjectsState.Loading
            repository.getProjects().collect { resource ->
                _projectsState.value = when (resource) {
                    is Resource.Loading -> ProjectsState.Loading
                    is Resource.Success -> ProjectsState.Success(resource.data ?: emptyList())
                    is Resource.Error -> ProjectsState.Error(resource.message ?: "Failed to load projects")
                }
            }
        }
    }
}

sealed class ProjectsState {
    object Loading : ProjectsState()
    data class Success(val projects: List<Project>) : ProjectsState()
    data class Error(val message: String) : ProjectsState()
}
