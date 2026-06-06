/*
 * Vivo Updates: FOSS System Patching powered through ADB/Bridge Engine!
 *  Copyright (C) 2026-present Sahil-958
 */

package com.vupdates.system.updater.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.StringRes
import com.vupdates.system.updater.R
import com.vupdates.system.updater.data.recordings.RecordingDirection
import com.vupdates.system.updater.data.recordings.RecordingMetadata
import com.vupdates.system.updater.data.AppPreferences
import com.vupdates.system.updater.integrations.scrcpy.ScrcpyAudioCodec
import com.vupdates.system.updater.system.permissions.PermissionChecks
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PatchingFileNameFormatter {
    const val TAG = "SCR:PatchingFileNameFormatter"

    enum class FileNamePlaceholder(val tag: String, @param:StringRes val descriptionResId: Int) {
        DATE("{date}", R.string.placeholder_date_desc),
        DIRECTION("{direction}", R.string.placeholder_direction_desc),
        PHONE_NUMBER("{phone_number}", R.string.placeholder_phone_number_desc),
        CONTACT_NAME("{contact_name}", R.string.placeholder_contact_name_desc),
        CROSS_COUNTRY("{cross_country}", R.string.placeholder_cross_country_desc)
    }

    fun formatFileName(
        context: Context,
        metadata: RecordingMetadata,
        codec: ScrcpyAudioCodec,
        customFormat: String? = null
    ): String {
        val template = customFormat ?: AppPreferences(context).getFileNameTemplate()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss.SSSZ", Locale.CANADA).format(Date())
        val directionStr = when (metadata.direction) {
            RecordingDirection.INCOMING -> "in"
            RecordingDirection.OUTGOING -> "out"
        }
        val phoneStr = metadata.getBestNumber() ?: ""
        var contactStr = ""
        if (template.contains(FileNamePlaceholder.CONTACT_NAME.tag) && phoneStr.isNotEmpty()) {
            contactStr = getContactName(context, phoneStr) ?: ""
        }
        val crossCountryStr = metadata.isCrossCountry.toString()
        val baseName = template
            .replace(FileNamePlaceholder.DATE.tag, dateStr)
            .replace(FileNamePlaceholder.DIRECTION.tag, directionStr)
            .replace(FileNamePlaceholder.PHONE_NUMBER.tag, phoneStr)
            .replace(FileNamePlaceholder.CONTACT_NAME.tag, contactStr)
            .replace(FileNamePlaceholder.CROSS_COUNTRY.tag, crossCountryStr)

        AppLogger.v(TAG, "Formatted base filename: '$baseName' with template '$template'")
        return "$baseName${codec.containerExtension}"
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        if (!PermissionChecks.hasContactsPermission(context)) return null
        val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        return context.contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (nameIndex != -1) cursor.getString(nameIndex) else null
            } else null
        }
    }
}
