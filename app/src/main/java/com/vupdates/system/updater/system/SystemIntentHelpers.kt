/*
 * Vivo Updates: FOSS System Patching powered through ADB/Bridge Engine!
 *  Copyright (C) 2026-present Sahil-958
 */

package com.vupdates.system.updater.system

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.vupdates.system.updater.AppUrls
import com.vupdates.system.updater.BuildConfig
import com.vupdates.system.updater.R
import com.vupdates.system.updater.integrations.shizuku.ShizukuConnectionManager
import com.vupdates.system.updater.utils.AppLogger

/**
 * SystemIntentHelpers.kt contains shortcuts for opening system screens and doing
 * other [Context] related tasks.
 */

private const val TAG = "SCR:SystemIntentHelpers"

/**
 * A folder-picker that asks for long-term read and write access to the chosen folder.
 */
class PersistentFolderPickerContract : ActivityResultContracts.OpenDocumentTree() {
    override fun createIntent(context: Context, input: Uri?): Intent {
        return super.createIntent(context, input).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        }
    }
}

/**
 * Locks in long-term read and write access to [uri] so it remains valid after a reboot.
 */
fun Context.takePersistableFolderPermission(uri: Uri) {
    contentResolver.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )
}

/**
 * Opens the App Info page for this app.
 */
fun Context.openAppSettings() {
    launchSmartIntent(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$packageName".toUri()
        }
    )
}

/**
 * Opens the Bridge Engine app or website.
 */
fun Context.openShizukuManager() {
    val packageName = ShizukuConnectionManager.getPackageName(this) ?: ""
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        launchSmartIntent(launchIntent)
    } else {
        launchSmartIntent(Intent(Intent.ACTION_VIEW).apply { data = AppUrls.ENGINE_WEBSITE.toUri() })
    }
}

/** Opens the project GitHub page. */
fun Context.openGithub() {
    launchSmartIntent(Intent(Intent.ACTION_VIEW).apply { data = AppUrls.GITHUB_REPOSITORY.toUri() })
}

/** Opens the GitHub report issue page. */
fun Context.openGithubReportIssue() {
    launchSmartIntent(Intent(Intent.ACTION_VIEW).apply { data = AppUrls.GITHUB_NEW_ISSUE.toUri() })
}

/**
 * Copies [text] to the clipboard.
 */
fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(this, getString(R.string.general_copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }
}

private fun Context.launchSmartIntent(intent: Intent) {
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
