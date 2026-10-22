package com.ramzi.cashflow.presentation.update

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzi.cashflow.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val apkUrl: String
)

class UpdateViewModel : ViewModel() {

    // GANTI INI DENGAN USERNAME DAN REPO KAMU
    private val githubRepo = "RamziAltF4/cashflow-android"

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    init {
        checkForUpdates()
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val url = URL("https://api.github.com/repos/$githubRepo/releases/latest")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(response)
                        val tagName = json.optString("tag_name", "").replace("v", "")
                        
                        // Ambil link APK pertama yang ada di assets
                        var downloadUrl = ""
                        val assets = json.optJSONArray("assets")
                        if (assets != null && assets.length() > 0) {
                            downloadUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                        }
                        Pair(tagName, downloadUrl)
                    } else {
                        Pair("", "")
                    }
                }

                val (latestVersion, downloadUrl) = result
                val currentVersion = BuildConfig.VERSION_NAME

                if (latestVersion.isNotEmpty() && downloadUrl.isNotEmpty()) {
                    if (isVersionNewer(currentVersion, latestVersion)) {
                        _updateInfo.value = UpdateInfo(latestVersion, downloadUrl)
                        Log.d("UpdateVM", "Update available: $latestVersion")
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateVM", "Failed to check update", e)
            }
        }
    }

    // Fungsi untuk membandingkan versi (misal "1.0" dengan "1.1")
    private fun isVersionNewer(current: String, latest: String): Boolean {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}