package com.secuaas.secuops.data.repository

import com.secuaas.secuops.data.local.TokenManager
import com.secuaas.secuops.data.model.*
import com.secuaas.secuops.data.remote.*
import com.secuaas.secuops.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecuOpsRepository @Inject constructor(
    private val api: SecuOpsApi,
    private val tokenManager: TokenManager
) {

    // ===================== AUTHENTICATION =====================

    suspend fun login(email: String, password: String): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                tokenManager.saveToken(loginResponse.token)
                tokenManager.saveUserInfo(loginResponse.user.email, loginResponse.user.role)
                emit(Resource.Success(loginResponse))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun logout() {
        tokenManager.clearAll()
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    suspend fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.changePassword(PasswordChangeRequest(currentPassword, newPassword))
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.message() ?: "Password change failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ===================== APPLICATIONS =====================

    suspend fun getApplications(): Flow<Resource<List<Application>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getApplications()
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getApplication(name: String): Flow<Resource<Application>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getApplication(name)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun restartApplication(name: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.restartApplication(name)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun scaleApplication(name: String, replicas: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.scaleApplication(name, ScaleRequest(replicas))
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ===================== DEPLOYMENTS =====================

    suspend fun getDeployments(
        applicationId: String? = null,
        projectId: String? = null,
        status: String? = null
    ): Flow<Resource<List<Deployment>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getDeployments(applicationId, projectId, status)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getDeployment(id: String): Flow<Resource<Deployment>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getDeployment(id)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun createDeployment(request: DeploymentRequest): Flow<Resource<Deployment>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.createDeployment(request)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun retryDeployment(id: String): Flow<Resource<Deployment>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.retryDeployment(id)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ===================== PROJECTS =====================

    suspend fun getProjects(): Flow<Resource<List<Project>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getProjects()
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getProject(id: String): Flow<Resource<Project>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getProject(id)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ===================== INFRASTRUCTURE =====================

    suspend fun getPods(namespace: String? = null, environment: String = "dev"): Flow<Resource<List<PodInfo>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getPods(namespace, environment)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getServices(namespace: String? = null, environment: String = "dev"): Flow<Resource<List<ServiceInfo>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getServices(namespace, environment)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getCertificates(namespace: String? = null, environment: String = "dev"): Flow<Resource<List<CertificateInfo>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getCertificates(namespace, environment)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getInfrastructure(type: String, namespace: String? = null, environment: String = "dev"): Flow<Resource<List<Any>>> = flow {
        emit(Resource.Loading())
        try {
            val response = when (type) {
                "pods" -> {
                    val r = api.getPods(namespace, environment)
                    if (r.isSuccessful && r.body() != null) {
                        emit(Resource.Success(r.body() as List<Any>))
                    } else {
                        emit(Resource.Error(r.message() ?: "Request failed"))
                    }
                    return@flow
                }
                "services" -> {
                    val r = api.getServices(namespace, environment)
                    if (r.isSuccessful && r.body() != null) {
                        emit(Resource.Success(r.body() as List<Any>))
                    } else {
                        emit(Resource.Error(r.message() ?: "Request failed"))
                    }
                    return@flow
                }
                "ingresses" -> {
                    val r = api.getIngresses(namespace, environment)
                    if (r.isSuccessful && r.body() != null) {
                        emit(Resource.Success(r.body() as List<Any>))
                    } else {
                        emit(Resource.Error(r.message() ?: "Request failed"))
                    }
                    return@flow
                }
                "certificates" -> {
                    val r = api.getCertificates(namespace, environment)
                    if (r.isSuccessful && r.body() != null) {
                        emit(Resource.Success(r.body() as List<Any>))
                    } else {
                        emit(Resource.Error(r.message() ?: "Request failed"))
                    }
                    return@flow
                }
                else -> emit(Resource.Error("Invalid infrastructure type"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ===================== DOMAINS =====================

    suspend fun getDomains(zone: String? = null): Flow<Resource<List<DomainRecord>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getDomains(zone)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun createDomain(request: DomainCreateRequest): Flow<Resource<DomainRecord>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.createDomain(request)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getDomainRecords(zone: String? = null): Flow<Resource<List<DomainRecord>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getDomains(zone)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun deleteDomainRecord(zone: String, id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.deleteDomain(id)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ===================== SERVERS =====================

    suspend fun getServers(): Flow<Resource<List<Server>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getServers()
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getServer(id: String): Flow<Resource<ServerDetail>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getServer(id)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getServerDetail(id: String): Flow<Resource<ServerDetail>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getServer(id)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun rebootServer(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.rebootServer(id)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ===================== BILLING =====================

    suspend fun getInvoices(year: Int? = null, month: Int? = null): Flow<Resource<List<Invoice>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getInvoices(year, month)
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getBillingSummary(): Flow<Resource<BillingSummary>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getBillingSummary()
            handleResponse(response, this)
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ===================== HELPER =====================

    private suspend fun <T> handleResponse(response: Response<T>, flow: kotlinx.coroutines.flow.FlowCollector<Resource<T>>) {
        if (response.isSuccessful && response.body() != null) {
            flow.emit(Resource.Success(response.body()!!))
        } else {
            flow.emit(Resource.Error(response.message() ?: "Request failed"))
        }
    }
}
