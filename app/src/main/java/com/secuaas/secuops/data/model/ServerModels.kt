package com.secuaas.secuops.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Server(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val ip: String,
    val cost: Double
)

@Serializable
data class ServerDetail(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val ip: String,
    val cost: Double,
    val cpu: String,
    val ram: String,
    val disk: String,
    val location: String
)
