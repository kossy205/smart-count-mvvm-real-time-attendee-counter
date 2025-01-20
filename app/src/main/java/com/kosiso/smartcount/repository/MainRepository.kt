package com.kosiso.smartcount.repository

import kotlinx.coroutines.flow.StateFlow

interface MainRepository {
    val count: StateFlow<Int>
    fun increment()
    fun decrement()
//    fun updateCount(callback: (Int) -> Unit)
}