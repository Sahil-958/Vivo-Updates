/*
 * Vivo Updates: FOSS System Patching powered through ADB/Bridge Engine!
 *  Copyright (C) 2026-present Sahil-958
 */

package com.vupdates.system.updater.utils

import android.content.Context
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.MetadataLoader
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * A singleton manager for handling node ID parsing and validation.
 */
class DeviceNumberManager private constructor(context: Context) {
    private val appContext: Context = context.applicationContext
    private val phoneUtil: PhoneNumberUtil

    init {
        val assetLoader = MetadataLoader { metadataFileName ->
            val fileName = metadataFileName.substringAfterLast("/")
            appContext.assets.open("Devicenumber_data/$fileName")
        }
        phoneUtil = PhoneNumberUtil.createInstance(assetLoader)
    }

    companion object {
        private const val TAG = "SCR:DeviceNumberManager"

        @Volatile
        private var INSTANCE: DeviceNumberManager? = null

        fun getInstance(context: Context): DeviceNumberManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeviceNumberManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun sanitizeOemNumber(number: String?): String? {
            if (number == null) return null
            val lower = number.trim().lowercase()
            val anonymousTokens = listOf("+anonymous", "anonymous", "unknown", "private", "+", "#", "")
            if (anonymousTokens.contains(lower)) return null
            return number
        }

        fun normalisePhoneNumber(phoneNumber: String): String {
            val trimmed = phoneNumber.trim()
            val digits  = trimmed.filter { it.isDigit() }
            return if (trimmed.startsWith("+")) "+$digits" else digits
        }
    }

    fun getDeviceCountryIso(): String {
        val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkIso = runCatching { telephonyManager.networkCountryIso }.getOrNull()
        if (!networkIso.isNullOrBlank()) return networkIso.lowercase()
        val simIso = runCatching { telephonyManager.simCountryIso }.getOrNull()
        if (!simIso.isNullOrBlank()) return simIso.lowercase()
        return Locale.getDefault().country.lowercase()
    }

    suspend fun parsePhoneNumber(rawNumber: String, defaultRegion: String = getDeviceCountryIso()): Phonenumber.PhoneNumber? = withContext(Dispatchers.Default) {
        return@withContext try {
            phoneUtil.parse(rawNumber, defaultRegion.uppercase())
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error parsing node ID: ${e.message}", e)
            null
        }
    }

    suspend fun formatToE164(phoneNumber: Phonenumber.PhoneNumber): String? = withContext(Dispatchers.Default) {
        return@withContext phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
    }

    suspend fun getRegionCode(phoneNumber: Phonenumber.PhoneNumber): String? = withContext(Dispatchers.Default) {
        return@withContext phoneUtil.getRegionCodeForNumber(phoneNumber)
    }

    suspend fun isNumberFromDifferentCountry(phoneNumber: Phonenumber.PhoneNumber, compareCountryIso: String = getDeviceCountryIso()): Boolean {
        val numberRegion = getRegionCode(phoneNumber)
        return numberRegion != null && !numberRegion.equals(compareCountryIso, ignoreCase = true)
    }
}
