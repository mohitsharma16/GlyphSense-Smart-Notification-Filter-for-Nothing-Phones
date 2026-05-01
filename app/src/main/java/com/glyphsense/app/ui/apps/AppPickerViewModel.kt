package com.glyphsense.app.ui.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.glyphsense.app.data.InstalledApp
import com.glyphsense.app.data.InstalledAppsRepository
import com.glyphsense.app.data.PrioritySettingsRepository
import com.glyphsense.app.domain.PriorityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppRow(
    val app: InstalledApp,
    val priority: PriorityLevel
)

data class AppPickerUiState(
    val loading: Boolean = true,
    val query: String = "",
    val rows: List<AppRow> = emptyList(),
    val importantCount: Int = 0,
    val normalCount: Int = 0,
    val silentCount: Int = 0
)

class AppPickerViewModel(app: Application) : AndroidViewModel(app) {

    private val priorityRepo = PrioritySettingsRepository(app)
    private val appsRepo = InstalledAppsRepository(app)

    private val installed = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val query = MutableStateFlow("")
    private val loading = MutableStateFlow(true)

    val state: StateFlow<AppPickerUiState> =
        combine(installed, priorityRepo.priorities, query, loading) { apps, priorities, q, isLoading ->
            val rows = apps.map { app ->
                AppRow(app, priorities[app.packageName] ?: PriorityLevel.DEFAULT)
            }
            val filtered = if (q.isBlank()) rows
            else rows.filter { it.app.label.contains(q, ignoreCase = true) }

            AppPickerUiState(
                loading = isLoading,
                query = q,
                rows = filtered,
                importantCount = rows.count { it.priority == PriorityLevel.IMPORTANT },
                normalCount = rows.count { it.priority == PriorityLevel.NORMAL },
                silentCount = rows.count { it.priority == PriorityLevel.SILENT }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppPickerUiState()
        )

    init {
        viewModelScope.launch {
            installed.value = appsRepo.loadAll()
            loading.value = false
        }
    }

    fun setQuery(q: String) { query.value = q }

    fun setPriority(packageName: String, level: PriorityLevel) {
        viewModelScope.launch {
            priorityRepo.setPriority(packageName, level)
        }
    }
}
