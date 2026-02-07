package com.secuaas.secuops.data.model

import com.google.gson.annotations.SerializedName

/**
 * Version information from /api/version endpoint
 * Standard SecuAAS format for Android app updates
 */
data class VersionInfo(
    @SerializedName("android")
    val android: AndroidVersionInfo?
)

data class AndroidVersionInfo(
    @SerializedName("version")
    val version: String,

    @SerializedName("version_code")
    val versionCode: Int,

    @SerializedName("download_url")
    val downloadUrl: String,

    @SerializedName("changelog")
    val changelog: String,

    @SerializedName("file_size")
    val fileSize: Long,

    @SerializedName("min_version")
    val minVersion: Int
)
