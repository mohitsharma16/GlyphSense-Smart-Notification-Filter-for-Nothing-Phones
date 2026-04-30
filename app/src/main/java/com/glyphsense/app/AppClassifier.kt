package com.glyphsense.app

object AppClassifier {

    private val defaultMap = mapOf(
        // IMPORTANT
        "com.whatsapp" to PriorityLevel.IMPORTANT,
        "com.google.android.dialer" to PriorityLevel.IMPORTANT,
        "com.google.android.apps.messaging" to PriorityLevel.IMPORTANT,

        // NORMAL
        "com.instagram.android" to PriorityLevel.NORMAL,
        "com.facebook.katana" to PriorityLevel.NORMAL,

        // SILENT
        "com.amazon.mShop.android.shopping" to PriorityLevel.SILENT,
        "com.flipkart.android" to PriorityLevel.SILENT
    )

    fun classify(packageName: String): PriorityLevel {
        return defaultMap[packageName] ?: PriorityLevel.NORMAL
    }

}
