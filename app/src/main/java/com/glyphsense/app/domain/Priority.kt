package com.glyphsense.app.domain

enum class PriorityLevel {
    IMPORTANT,
    NORMAL,
    SILENT;

    companion object {
        val DEFAULT: PriorityLevel = NORMAL

        fun fromName(name: String?): PriorityLevel = when (name) {
            IMPORTANT.name -> IMPORTANT
            NORMAL.name -> NORMAL
            SILENT.name -> SILENT
            else -> DEFAULT
        }
    }
}