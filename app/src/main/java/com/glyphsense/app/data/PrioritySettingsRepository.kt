package com.glyphsense.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.glyphsense.app.domain.PriorityLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prioritiesDataStore by preferencesDataStore(name = "glyphsense_priorities")

class PrioritySettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.prioritiesDataStore

    val priorities: Flow<Map<String, PriorityLevel>> =
        dataStore.data.map { prefs ->
            val overrides = prefs.asMap()
                .mapNotNull { (key, value) ->
                    val level = (value as? String)?.let(PriorityLevel::fromName) ?: return@mapNotNull null
                    key.name to level
                }
                .toMap()
            DefaultPriorities + overrides
        }

    suspend fun priorityFor(packageName: String): PriorityLevel =
        priorities.first()[packageName] ?: PriorityLevel.DEFAULT

    suspend fun setPriority(packageName: String, level: PriorityLevel) {
        val key = stringPreferencesKey(packageName)
        dataStore.edit { prefs ->
            prefs[key] = level.name
        }
    }

    suspend fun clearOverride(packageName: String) {
        val key = stringPreferencesKey(packageName)
        dataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    companion object {
        val DefaultPriorities: Map<String, PriorityLevel> = mapOf(
            "com.whatsapp" to PriorityLevel.IMPORTANT,
            "com.whatsapp.w4b" to PriorityLevel.IMPORTANT,
            "com.google.android.dialer" to PriorityLevel.IMPORTANT,
            "com.google.android.apps.messaging" to PriorityLevel.IMPORTANT,
            "com.google.android.gm" to PriorityLevel.IMPORTANT,
            "org.telegram.messenger" to PriorityLevel.IMPORTANT,
            "com.microsoft.teams" to PriorityLevel.IMPORTANT,
            "com.Slack" to PriorityLevel.IMPORTANT,

            "com.instagram.android" to PriorityLevel.NORMAL,
            "com.facebook.katana" to PriorityLevel.NORMAL,
            "com.twitter.android" to PriorityLevel.NORMAL,
            "com.snapchat.android" to PriorityLevel.NORMAL,
            "com.spotify.music" to PriorityLevel.NORMAL,

            "com.amazon.mShop.android.shopping" to PriorityLevel.SILENT,
            "com.flipkart.android" to PriorityLevel.SILENT,
            "com.zomato" to PriorityLevel.SILENT,
            "in.swiggy.android" to PriorityLevel.SILENT
        )
    }
}
