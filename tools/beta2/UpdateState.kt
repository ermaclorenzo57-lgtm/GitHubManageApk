package com.lomipe.devhub.ui

import android.content.Context
import androidx.compose.runtime.*
import com.lomipe.devhub.core.ReleaseVersion
import com.lomipe.devhub.core.newerRelease
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

// Current public repository; replace with the dedicated release repo before the official launch.
private const val RELEASE_REPO = "ermaclorenzo57-lgtm/GitHubManageApk"
private const val CHECK_URL = "https://api.github.com/repos/$RELEASE_REPO/releases?per_page=30"
private const val CHECK_INTERVAL = 24L * 60 * 60 * 1000

class UpdateState {
    var available by mutableStateOf<String?>(null)
        private set
    var status by mutableStateOf("Checks once daily when the app opens.")
        private set
    var checking by mutableStateOf(false)
        private set

    @Suppress("DEPRECATION")
    fun installed(ctx: Context): String =
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "unknown"

    fun open(ctx: Context) {
        val tag = available ?: return
        if (ReleaseVersion.parse(tag) != null)
            Intents.openGitHubUrl(ctx, "https://github.com/$RELEASE_REPO/releases/tag/$tag")
    }

    suspend fun check(ctx: Context, force: Boolean) {
        if (checking) return
        val prefs = ctx.getSharedPreferences("release_check", Context.MODE_PRIVATE)
        val installed = installed(ctx)
        val current = ReleaseVersion.parse(installed)
        val saved = prefs.getString("tag", null)
        available = saved?.takeIf { tag -> ReleaseVersion.parse(tag)?.let { v -> current != null && v > current && (current.stage != 3 || v.stage == 3) } == true }
        val last = prefs.getLong("checked", 0)
        if (!force && System.currentTimeMillis() - last in 0L until CHECK_INTERVAL) {
            status = if (available != null) "An update is available." else "No newer published release found at last check."
            return
        }
        checking = true
        status = "Checking published releases…"
        try {
            val tag = withContext(Dispatchers.IO) {
                val c = URL(CHECK_URL).openConnection() as HttpsURLConnection
                c.instanceFollowRedirects = false
                c.connectTimeout = 8000
                c.readTimeout = 8000
                c.setRequestProperty("Accept", "application/vnd.github+json")
                c.setRequestProperty("User-Agent", "LomileDevHub-release-check")
                try {
                    check(c.responseCode == 200) { "Release service unavailable" }
                    val bytes = c.inputStream.use { input ->
                        val out = java.io.ByteArrayOutputStream()
                        val buf = ByteArray(8192)
                        while (true) {
                            val n = input.read(buf)
                            if (n < 0) break
                            require(out.size() + n <= 2 * 1024 * 1024) { "Release response too large" }
                            out.write(buf, 0, n)
                        }
                        out.toByteArray()
                    }
                    newerRelease(installed, bytes.toString(Charsets.UTF_8))
                } finally { c.disconnect() }
            }
            available = tag
            prefs.edit().putString("tag", tag).putLong("checked", System.currentTimeMillis()).apply()
            status = if (tag == null) "No newer published release found." else "Update $tag available. Review the release before installing."
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            status = "Could not check for updates. Try again when connected."
        } finally { checking = false }
    }
}
