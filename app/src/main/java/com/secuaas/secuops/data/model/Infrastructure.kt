package com.secuaas.secuops.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Application(
    val id: String,
    val name: String,
    @SerialName("display_name") val displayName: String? = null,
    val environment: String,
    val namespace: String,
    val status: String,
    val domains: List<String> = emptyList(),
    @SerialName("deployed_at") val deployedAt: String,
    @SerialName("deployed_by") val deployedBy: String
)

@Serializable
data class Deployment(
    val id: String,
    @SerialName("application_id") val applicationId: String,
    @SerialName("project_id") val projectId: String,
    val operation: String,
    @SerialName("commit_sha") val commitSha: String? = null,
    @SerialName("commit_message") val commitMessage: String? = null,
    val status: String,
    val phase: String? = null,
    val progress: Int = 0,
    @SerialName("started_at") val startedAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
    val duration: Int? = null,
    @SerialName("auto_corrected") val autoCorrected: Boolean = false,
    val errors: List<DeploymentError> = emptyList()
)

@Serializable
data class DeploymentError(
    val phase: String,
    val type: String,
    val message: String,
    val details: String? = null,
    val correctable: Boolean = false,
    val corrected: Boolean = false
)

@Serializable
data class Project(
    val id: String,
    val name: String,
    @SerialName("display_name") val displayName: String,
    val description: String? = null,
    val category: String? = null,
    val repositories: List<ProjectRepository> = emptyList(),
    val scalable: Boolean = true,
    val environments: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ProjectRepository(
    val url: String,
    val branch: String = "main",
    val component: String,
    @SerialName("dockerfile_path") val dockerfilePath: String = "Dockerfile"
)

@Serializable
data class PodInfo(
    val name: String,
    val status: String,
    val restarts: Int,
    val age: String,
    val ready: String
)

@Serializable
data class ServiceInfo(
    val name: String,
    val type: String,
    @SerialName("cluster_ip") val clusterIp: String,
    val ports: String
)

@Serializable
data class IngressInfo(
    val name: String,
    val hosts: List<String>,
    val address: String? = null
)

@Serializable
data class CertificateInfo(
    val name: String,
    val ready: Boolean,
    val secret: String,
    val issuer: String? = null,
    @SerialName("valid_until") val validUntil: String? = null
)
