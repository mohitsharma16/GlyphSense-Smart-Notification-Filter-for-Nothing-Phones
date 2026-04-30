package com.glyphsense.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object GlyphState {

    private val _priorityFlow = MutableStateFlow<PriorityLevel?>(null)
    val priorityFlow: StateFlow<PriorityLevel?> = _priorityFlow

    fun updatePriority(priority: PriorityLevel?) {
        _priorityFlow.value = priority
    }
}
