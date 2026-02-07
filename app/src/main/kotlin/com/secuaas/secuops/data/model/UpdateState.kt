package com.secuaas.secuops.data.model

/**
 * Update state machine for SecuOps Android app
 * Follows SecuAAS standard update flow
 */
sealed class UpdateState {
    /**
     * No update check in progress
     */
    object Idle : UpdateState()

    /**
     * Checking for updates from API
     */
    object Checking : UpdateState()

    /**
     * Update is available
     */
    data class Available(
        val versionInfo: AndroidVersionInfo,
        val currentVersion: String,
        val currentVersionCode: Int
    ) : UpdateState()

    /**
     * App is up to date
     */
    data class UpToDate(
        val currentVersion: String
    ) : UpdateState()

    /**
     * Downloading update APK
     */
    data class Downloading(
        val versionInfo: AndroidVersionInfo,
        val progress: Int  // 0-100
    ) : UpdateState()

    /**
     * Download complete, ready to install
     */
    data class ReadyToInstall(
        val versionInfo: AndroidVersionInfo,
        val downloadId: Long,
        val filePath: String
    ) : UpdateState()

    /**
     * Error occurred during update process
     */
    data class Error(
        val message: String
    ) : UpdateState()
}
