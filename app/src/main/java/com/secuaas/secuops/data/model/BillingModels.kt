package com.secuaas.secuops.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BillingSummary(
    val total: Double,
    val breakdown: Map<String, Double>
)

@Serializable
data class Invoice(
    val id: String,
    val date: String,
    val amount: Double,
    val status: String
)
