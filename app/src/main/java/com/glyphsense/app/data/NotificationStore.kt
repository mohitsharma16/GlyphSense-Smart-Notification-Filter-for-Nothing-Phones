package com.glyphsense.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StoredNotification(
    val key: String,
    val packageName: String,
    val postedAt: Long
)

object NotificationStore {

    private val _notifications = MutableStateFlow<List<StoredNotification>>(emptyList())
    val notifications: StateFlow<List<StoredNotification>> = _notifications.asStateFlow()

    fun upsert(notification: StoredNotification) {
        _notifications.update { current ->
            val replaced = current.filterNot { it.key == notification.key }
            replaced + notification
        }
    }

    fun remove(key: String) {
        _notifications.update { current -> current.filterNot { it.key == key } }
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}