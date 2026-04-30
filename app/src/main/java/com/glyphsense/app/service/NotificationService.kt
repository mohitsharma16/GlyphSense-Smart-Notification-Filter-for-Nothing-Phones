package com.glyphsense.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.glyphsense.app.glyph.GlyphManager
import com.glyphsense.app.data.AppClassifier

class NotificationService : NotificationListenerService() {

    private var glyphManager: GlyphManager? = null
    private var lastPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        glyphManager = GlyphManager(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // Prevent spam duplicates
        if (packageName == lastPackage) return
        lastPackage = packageName

        val priority = AppClassifier.classify(packageName)

        Log.d("GlyphService", "App: $packageName")
        Log.d("GlyphService", "Priority: $priority")

        glyphManager?.playAnimation(priority)
    }

}