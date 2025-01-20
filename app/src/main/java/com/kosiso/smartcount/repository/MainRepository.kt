package com.kosiso.smartcount.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.StateFlow

interface MainRepository {
//    val count: LiveData<Int>
    val count: StateFlow<Int>
    fun increment()
    fun decrement()
//    fun updateCount(callback: (Int) -> Unit)
}