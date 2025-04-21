package com.kosiso.smartcount.utils

import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

enum class CountType(val type: String) {
    INDIVIDUAL("Individual"),
    SESSION_COUNT("Session Count")
}