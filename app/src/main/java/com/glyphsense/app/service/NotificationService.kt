package com.glyphsense.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.glyphsense.app.data.NotificationStore
import com.glyphsense.app.data.StoredNotification

class NotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        rebuildFromActive()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationStore.clear()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (shouldIgnore(sbn)) return
        NotificationStore.upsert(sbn.toStored())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationStore.remove(sbn.key)
    }

    private fun rebuildFromActive() {
        val active = try {
            activeNotifications ?: return
        } catch (e: SecurityException) {
            Log.w(TAG, "No notification access yet", e)
            return
        }
        NotificationStore.clear()
        active.forEach { sbn ->
            if (!shouldIgnore(sbn)) NotificationStore.upsert(sbn.toStored())
        }
    }

    private fun shouldIgnore(sbn: StatusBarNotification): Boolean {
        if (sbn.packageName == packageName) return true
        val flags = sbn.notification?.flags ?: return true
        if (flags and Notification.FLAG_GROUP_SUMMARY != 0) return true
        if (flags and Notification.FLAG_ONGOING_EVENT != 0) return true
        if (flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return true
        return false
    }

    private fun StatusBarNotification.toStored() = StoredNotification(
        key = key,
        packageName = packageName,
        postedAt = postTime
    )

    private companion object {
        const val TAG = "GlyphSense.Listener"
    }
}