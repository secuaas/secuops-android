package com.secuaas.secuops.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.secuaas.secuops.data.model.AndroidVersionInfo
import com.secuaas.secuops.data.model.UpdateState
import com.secuaas.secuops.data.model.VersionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UpdateManager for SecuOps Android
 *
 * Manages automatic updates following SecuAAS standard:
 * - Checks /api/version endpoint for new versions
 * - Downloads APK from /binaries/ endpoint
 * - Manages download progress and installation
 * - Follows same pattern as CCL Manager
 *
 * Standard SecuAAS Update Flow:
 * 1. User clicks "Check for Updates" in Settings
 * 2. GET /api/version → compare version_code
 * 3. If available, show "Update Available" with changelog
 * 4. User clicks "Download"
 * 5. Download APK via DownloadManager
 * 6. Show progress 0-100%
 * 7. When complete, show "Install Now"
 * 8. User clicks → Android package installer opens
 */
@Singleton
class UpdateManager @Inject constructor(
    private val context: Context
) {
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var currentDownloadId: Long = -1
    private var apiBaseUrl: String = ""
    private var downloadReceiver: BroadcastReceiver? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val prefs = context.getSharedPreferences("update_manager_prefs", Context.MODE_PRIVATE)

    init {
        // Load saved API URL for update checks even when not connected
        apiBaseUrl = prefs.getString(PREF_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
        if (apiBaseUrl.isNotEmpty()) {
            Log.d(TAG, "Loaded saved API URL: $apiBaseUrl")
        }
    }

    /**
     * Set the API base URL for checking updates
     * Should be called when user configures or connects to SecuOps API
     *
     * @param url Base URL like "https://api.secuops.secuaas.dev" (without trailing slash)
     */
    fun setApiUrl(url: String) {
        apiBaseUrl = url.trimEnd('/')
        // Save URL for future update checks even when not connected
        prefs.edit().putString(PREF_API_URL, apiBaseUrl).apply()
        Log.d(TAG, "Saved API URL: $apiBaseUrl")
    }

    /**
     * Get the current app version name (e.g., "0.2.3")
     */
    fun getCurrentVersionName(): String {
        return try {
            val packageInfo = getPackageInfo()
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Get the current app version code (e.g., 3)
     * Used for numeric comparison with server version
     */
    fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = getPackageInfo()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun getPackageInfo(): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }

    /**
     * Check for available updates
     *
     * Makes HTTP call to /api/version endpoint (public, no auth required)
     * Compares version_code from server with current app version_code
     *
     * Updates _updateState with result:
     * - UpdateState.Available if new version exists
     * - UpdateState.UpToDate if current version is latest
     * - UpdateState.Error if check failed
     */
    suspend fun checkForUpdate() {
        if (apiBaseUrl.isEmpty()) {
            _updateState.value = UpdateState.Error("API URL not configured. Please configure in Settings.")
            return
        }

        _updateState.value = UpdateState.Checking

        try {
            withContext(Dispatchers.IO) {
                val url = "$apiBaseUrl/api/version"
                Log.d(TAG, "Checking for updates at: $url")

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = httpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    response.close()

                    if (body != null) {
                        val versionInfo = gson.fromJson(body, VersionInfo::class.java)
                        withContext(Dispatchers.Main) {
                            handleVersionInfo(versionInfo)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _updateState.value = UpdateState.Error("Empty response from server")
                        }
                    }
                } else {
                    val code = response.code
                    response.close()
                    withContext(Dispatchers.Main) {
                        _updateState.value = UpdateState.Error("Server error: $code")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            _updateState.value = UpdateState.Error(e.message ?: "Failed to check for updates")
        }
    }

    private fun handleVersionInfo(versionInfo: VersionInfo) {
        val androidInfo = versionInfo.android
        if (androidInfo == null) {
            _updateState.value = UpdateState.UpToDate(getCurrentVersionName())
            return
        }

        val currentVersionCode = getCurrentVersionCode()

        if (androidInfo.versionCode > currentVersionCode) {
            _updateState.value = UpdateState.Available(
                versionInfo = androidInfo,
                currentVersion = getCurrentVersionName(),
                currentVersionCode = currentVersionCode
            )
        } else {
            _updateState.value = UpdateState.UpToDate(getCurrentVersionName())
        }
    }

    /**
     * Download the update APK
     *
     * Uses Android DownloadManager to download APK in background
     * Shows notification with progress
     * When complete, triggers ReadyToInstall state
     *
     * @param versionInfo Version to download (from Available state)
     */
    fun downloadUpdate(versionInfo: AndroidVersionInfo) {
        if (apiBaseUrl.isEmpty()) {
            _updateState.value = UpdateState.Error("API URL not configured")
            return
        }

        _updateState.value = UpdateState.Downloading(versionInfo, 0)

        val downloadUrl = apiBaseUrl + versionInfo.downloadUrl
        val fileName = "secuops-android-v${versionInfo.version}.apk"

        // Delete old APK files
        cleanupOldApks()

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("SecuOps Update")
            setDescription("Downloading version ${versionInfo.version}")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        currentDownloadId = downloadManager.enqueue(request)

        // Register receiver for download completion
        registerDownloadReceiver(versionInfo)

        // Start progress monitoring
        monitorDownloadProgress(versionInfo)
    }

    private fun registerDownloadReceiver(versionInfo: AndroidVersionInfo) {
        unregisterDownloadReceiver()

        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == currentDownloadId) {
                    handleDownloadComplete(versionInfo)
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            downloadReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterDownloadReceiver() {
        downloadReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                // Receiver might not be registered
            }
        }
        downloadReceiver = null
    }

    private fun handleDownloadComplete(versionInfo: AndroidVersionInfo) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(currentDownloadId)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

            val status = cursor.getInt(statusIndex)
            val localUri = cursor.getString(localUriIndex)

            cursor.close()

            if (status == DownloadManager.STATUS_SUCCESSFUL && localUri != null) {
                val filePath = Uri.parse(localUri).path ?: ""
                _updateState.value = UpdateState.ReadyToInstall(
                    versionInfo = versionInfo,
                    downloadId = currentDownloadId,
                    filePath = filePath
                )
            } else {
                _updateState.value = UpdateState.Error("Download failed")
            }
        } else {
            cursor.close()
            _updateState.value = UpdateState.Error("Download not found")
        }

        unregisterDownloadReceiver()
    }

    private fun monitorDownloadProgress(versionInfo: AndroidVersionInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            while (_updateState.value is UpdateState.Downloading) {
                val query = DownloadManager.Query().setFilterById(currentDownloadId)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                    val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                    val bytesTotal = cursor.getLong(bytesTotalIndex)

                    if (bytesTotal > 0) {
                        val progress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                        withContext(Dispatchers.Main) {
                            if (_updateState.value is UpdateState.Downloading) {
                                _updateState.value = UpdateState.Downloading(versionInfo, progress)
                            }
                        }
                    }
                }
                cursor.close()
                delay(500)
            }
        }
    }

    /**
     * Install the downloaded APK
     *
     * Opens Android package installer with the downloaded APK
     * Requires REQUEST_INSTALL_PACKAGES permission
     * Uses FileProvider for Android 7+ compatibility
     *
     * @param filePath Path to downloaded APK file
     */
    fun installUpdate(filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                _updateState.value = UpdateState.Error("APK file not found")
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Failed to install: ${e.message}")
        }
    }

    /**
     * Cancel any ongoing download
     */
    fun cancelDownload() {
        if (currentDownloadId != -1L) {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.remove(currentDownloadId)
            currentDownloadId = -1
        }
        unregisterDownloadReceiver()
        _updateState.value = UpdateState.Idle
    }

    /**
     * Reset update state to idle
     */
    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    /**
     * Clean up old downloaded APK files
     */
    private fun cleanupOldApks() {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            downloadDir?.listFiles()?.filter { it.extension == "apk" }?.forEach { it.delete() }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    /**
     * Format file size for display
     *
     * @param bytes File size in bytes
     * @return Formatted string like "18.4 MB"
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    companion object {
        private const val TAG = "UpdateManager"
        private const val PREF_API_URL = "api_url"
        private const val DEFAULT_API_URL = "https://api.secuops.secuaas.dev"
    }
}
