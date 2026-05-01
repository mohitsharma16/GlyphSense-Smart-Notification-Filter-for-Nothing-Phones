package com.glyphsense.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.glyphsense.app.ui.permissions.NotificationListenerAccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val notificationAccessGranted: Boolean = false
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(
        HomeUiState(notificationAccessGranted = NotificationListenerAccess.isGranted(app))
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeUiState(
                notificationAccessGranted = NotificationListenerAccess.isGranted(getApplication())
            )
        }
    }
}