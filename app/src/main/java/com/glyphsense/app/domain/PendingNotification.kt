package com.glyphsense.app.domain

data class PendingNotification(
    val key: String,
    val packageName: String,
    val priority: PriorityLevel,
    val postedAt: Long
)