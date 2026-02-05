package com.secuaas.secuops.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainRecord(
    val id: String,
    val zone: String,
    val type: String,
    val name: String,
    val value: String,
    val ttl: Int
)
