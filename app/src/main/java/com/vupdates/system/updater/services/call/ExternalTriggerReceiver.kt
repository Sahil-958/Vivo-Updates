/*
 * Vivo Updates: FOSS System Patching powered through ADB/Bridge Engine!
 *  Copyright (C) 2026-present Sahil-958
 */

package com.vupdates.system.updater.services.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vupdates.system.updater.data.recordings.RecordingDirection
import com.vupdates.system.updater.data.recordings.RecordingMetadata
import com.vupdates.system.updater.services.recording.RecordingForegroundService
import com.vupdates.system.updater.utils.AppLogger

/**
 * An exported BroadcastReceiver designed to allow third-party automation tools (like vFlow, Tasker)
 * to trigger the patching (recording) process via intents.
 *
 * Supported Actions:
 * - START: com.vupdates.system.updater.ACTION_START_PATCH
 *   Optional Extra: "node_id" (String) - Identifier for the session
 * - STOP: com.vupdates.system.updater.ACTION_STOP_PATCH
 */
class ExternalTriggerReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SCR:ExternalTriggerReceiver"
        const val ACTION_START_PATCH = "com.vupdates.system.updater.ACTION_START_PATCH"
        const val ACTION_STOP_PATCH = "com.vupdates.system.updater.ACTION_STOP_PATCH"
        const val EXTRA_NODE_ID = "node_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        AppLogger.i(TAG, "Received external trigger intent: $action")

        when (action) {
            ACTION_START_PATCH -> {
                val nodeId = intent.getStringExtra(EXTRA_NODE_ID) ?: "External Trigger"
                val metadata = RecordingMetadata(
                    rawPhoneNumber = nodeId,
                    direction = RecordingDirection.INCOMING // Default to incoming for metadata purposes
                )
                
                val serviceIntent = Intent(context, RecordingForegroundService::class.java).apply {
                    this.action = RecordingForegroundService.ACTION_START_RECORDING
                    putExtra(RecordingForegroundService.EXTRA_RECORDING_METADATA, metadata)
                }
                context.startForegroundService(serviceIntent)
            }
            ACTION_STOP_PATCH -> {
                val serviceIntent = Intent(context, RecordingForegroundService::class.java).apply {
                    this.action = RecordingForegroundService.ACTION_STOP_RECORDING
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
