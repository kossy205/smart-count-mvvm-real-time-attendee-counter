package com.kosiso.smartcount.repository

import androidx.lifecycle.LiveData
import com.kosiso.smartcount.database.models.Count
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MainRepository {
//    val count: LiveData<Int>
    val count: StateFlow<Int>
//    val allCountList: Flow<List<Count>>
    fun increment()
    fun decrement()
    fun resetCount()
//    fun updateCount(callback: (Int) -> Unit)

    fun getAllCountList(): Flow<List<Count>>
    suspend fun insertCount(count: Count)
    suspend fun deleteCount(countId: Int)

}