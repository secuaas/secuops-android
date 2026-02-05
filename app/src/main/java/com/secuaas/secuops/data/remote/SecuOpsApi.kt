package com.secuaas.secuops.data.remote

import com.secuaas.secuops.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SecuOpsApi {

    // ===================== AUTHENTICATION =====================

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/change-password")
    suspend fun changePassword(@Body request: PasswordChangeRequest): Response<Unit>

    @GET("/api/auth/me")
    suspend fun getCurrentUser(): Response<User>

    // ===================== APPLICATIONS =====================

    @GET("/api/applications")
    suspend fun getApplications(): Response<List<Application>>

    @GET("/api/applications/{name}")
    suspend fun getApplication(@Path("name") name: String): Response<Application>

    @POST("/api/applications/{name}/restart")
    suspend fun restartApplication(@Path("name") name: String): Response<Unit>

    @POST("/api/applications/{name}/scale")
    suspend fun scaleApplication(
        @Path("name") name: String,
        @Body request: ScaleRequest
    ): Response<Unit>

    @DELETE("/api/applications/{name}")
    suspend fun deleteApplication(@Path("name") name: String): Response<Unit>

    // ===================== DEPLOYMENTS =====================

    @GET("/api/deployments")
    suspend fun getDeployments(
        @Query("application_id") applicationId: String? = null,
        @Query("project_id") projectId: String? = null,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50
    ): Response<List<Deployment>>

    @GET("/api/deployments/{id}")
    suspend fun getDeployment(@Path("id") id: String): Response<Deployment>

    @POST("/api/deployments/new")
    suspend fun createDeployment(@Body request: DeploymentRequest): Response<Deployment>

    @POST("/api/deployments/{id}/retry")
    suspend fun retryDeployment(@Path("id") id: String): Response<Deployment>

    // ===================== PROJECTS =====================

    @GET("/api/projects")
    suspend fun getProjects(): Response<List<Project>>

    @GET("/api/projects/{id}")
    suspend fun getProject(@Path("id") id: String): Response<Project>

    @POST("/api/projects")
    suspend fun createProject(@Body project: ProjectCreateRequest): Response<Project>

    @PUT("/api/projects/{id}")
    suspend fun updateProject(
        @Path("id") id: String,
        @Body project: ProjectUpdateRequest
    ): Response<Project>

    @DELETE("/api/projects/{id}")
    suspend fun deleteProject(@Path("id") id: String): Response<Unit>

    // ===================== INFRASTRUCTURE =====================

    @GET("/api/infrastructure/pods")
    suspend fun getPods(
        @Query("namespace") namespace: String? = null,
        @Query("environment") environment: String = "dev"
    ): Response<List<PodInfo>>

    @GET("/api/infrastructure/services")
    suspend fun getServices(
        @Query("namespace") namespace: String? = null,
        @Query("environment") environment: String = "dev"
    ): Response<List<ServiceInfo>>

    @GET("/api/infrastructure/ingresses")
    suspend fun getIngresses(
        @Query("namespace") namespace: String? = null,
        @Query("environment") environment: String = "dev"
    ): Response<List<IngressInfo>>

    @GET("/api/infrastructure/certificates")
    suspend fun getCertificates(
        @Query("namespace") namespace: String? = null,
        @Query("environment") environment: String = "dev"
    ): Response<List<CertificateInfo>>

    // ===================== DOMAINS =====================

    @GET("/api/domains")
    suspend fun getDomains(@Query("zone") zone: String? = null): Response<List<DomainRecord>>

    @POST("/api/domains")
    suspend fun createDomain(@Body request: DomainCreateRequest): Response<DomainRecord>

    @DELETE("/api/domains/{id}")
    suspend fun deleteDomain(@Path("id") id: String): Response<Unit>

    // ===================== VPS / SERVERS =====================

    @GET("/api/servers")
    suspend fun getServers(): Response<List<Server>>

    @GET("/api/servers/{id}")
    suspend fun getServer(@Path("id") id: String): Response<ServerDetail>

    @POST("/api/servers/{id}/reboot")
    suspend fun rebootServer(@Path("id") id: String): Response<Unit>

    // ===================== BILLING =====================

    @GET("/api/billing/invoices")
    suspend fun getInvoices(
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null
    ): Response<List<Invoice>>

    @GET("/api/billing/summary")
    suspend fun getBillingSummary(): Response<BillingSummary>
}

// ===================== REQUEST MODELS =====================

@kotlinx.serialization.Serializable
data class ScaleRequest(val replicas: Int)

@kotlinx.serialization.Serializable
data class DeploymentRequest(
    @kotlinx.serialization.SerialName("project_id") val projectId: String,
    @kotlinx.serialization.SerialName("application_name") val applicationName: String,
    val environment: String,
    @kotlinx.serialization.SerialName("config_overrides") val configOverrides: Map<String, String> = emptyMap()
)

@kotlinx.serialization.Serializable
data class ProjectCreateRequest(
    val name: String,
    @kotlinx.serialization.SerialName("display_name") val displayName: String,
    val description: String? = null,
    val category: String? = null,
    val repositories: List<ProjectRepository>,
    val scalable: Boolean = true,
    val environments: List<String> = listOf("k8s-dev")
)

@kotlinx.serialization.Serializable
data class ProjectUpdateRequest(
    @kotlinx.serialization.SerialName("display_name") val displayName: String? = null,
    val description: String? = null,
    val category: String? = null
)

@kotlinx.serialization.Serializable
data class DomainRecord(
    val id: String,
    val zone: String,
    val subdomain: String,
    val type: String,
    val target: String,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String
)

@kotlinx.serialization.Serializable
data class DomainCreateRequest(
    val zone: String,
    val subdomain: String,
    val type: String = "A",
    val target: String
)

@kotlinx.serialization.Serializable
data class Server(
    val id: String,
    val name: String,
    val type: String, // "vps", "dedicated", "cloud"
    val provider: String, // "ovh", "other"
    val status: String,
    val ip: String,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String
)

@kotlinx.serialization.Serializable
data class ServerDetail(
    val id: String,
    val name: String,
    val type: String,
    val provider: String,
    val status: String,
    val ip: String,
    val cpu: Int,
    val ram: Int, // GB
    val disk: Int, // GB
    val os: String,
    @kotlinx.serialization.SerialName("monthly_cost") val monthlyCost: Double,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String
)

@kotlinx.serialization.Serializable
data class Invoice(
    val id: String,
    @kotlinx.serialization.SerialName("invoice_number") val invoiceNumber: String,
    val date: String,
    val amount: Double,
    val currency: String = "EUR",
    val status: String,
    @kotlinx.serialization.SerialName("pdf_url") val pdfUrl: String? = null
)

@kotlinx.serialization.Serializable
data class BillingSummary(
    @kotlinx.serialization.SerialName("current_month") val currentMonth: Double,
    @kotlinx.serialization.SerialName("last_month") val lastMonth: Double,
    @kotlinx.serialization.SerialName("year_to_date") val yearToDate: Double,
    val currency: String = "EUR",
    val breakdown: Map<String, Double> = emptyMap()
)
