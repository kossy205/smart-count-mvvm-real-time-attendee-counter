package com.kosiso.smartcount.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class CrashlyticsUtils @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) {
    fun log(message: String) {
        crashlytics.log(message)
    }

    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}