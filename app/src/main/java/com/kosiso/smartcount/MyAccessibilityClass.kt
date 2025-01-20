package com.kosiso.smartcount

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.kosiso.smartcount.repository.MainRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VolumeButtonAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var volumeButtonProcessor: VolumeButtonProcessor

    private var lastEventTime = 0L
    private val debounceTime = 300L // Prevent multiple rapid triggers

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to handle accessibility events
    }

    override fun onInterrupt() {
        // Handle interruption if needed
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        event?.let {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEventTime < debounceTime) {
                return true
            }

            when (it.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    Log.i("accessibility up", "done")
                    if (it.action == KeyEvent.ACTION_DOWN) {
                        Log.i("accessibility up 1", "done")
                        volumeButtonProcessor.incrementCount()
                        lastEventTime = currentTime
                        return true
                    }
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    Log.i("accessibility up", "done")
                    if (it.action == KeyEvent.ACTION_DOWN) {
                        Log.i("accessibility down 1", "done")
                        volumeButtonProcessor.decrementCount()
                        lastEventTime = currentTime
                        return true
                    }
                }
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.i("accessibility", "done")

        // Configure accessibility service
        serviceInfo = AccessibilityServiceInfo().apply {
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        }
    }
}



// VolumeButtonProcessor.kt
class VolumeButtonProcessor @Inject constructor(
    private val mainRepository: MainRepository
)
{
    private val scope = CoroutineScope(Dispatchers.IO + Job())


    fun incrementCount() {
        scope.launch {
            mainRepository.increment()
        }
    }

    fun decrementCount() {
        scope.launch {
            mainRepository.decrement()
        }
    }
}