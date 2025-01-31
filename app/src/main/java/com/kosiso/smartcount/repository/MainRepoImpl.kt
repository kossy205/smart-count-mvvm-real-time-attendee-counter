package com.kosiso.smartcount.repository

import android.util.Log
import com.kosiso.smartcount.database.CountDao
import com.kosiso.smartcount.database.RoomDatabase
import com.kosiso.smartcount.database.models.Count
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class MainRepoImpl @Inject constructor(val countDao: CountDao): MainRepository {

    private val _count = MutableStateFlow(0)
    override val count : StateFlow<Int> = _count

//    override val allCountList: Flow<List<Count>> = countDao.getAllCounts()

    override fun increment(){
        _count.value = _count.value + 1
        Log.i("count increase", "${count}")
    }

    override fun decrement(){
        when{
            (_count.value <= 0) ->{
                _count.value = 0
            }
            (_count.value > 0) ->{
                _count.value = _count.value - 1
            }
        }
        Log.i("count decrease", "${count}")
    }

    override fun resetCount() {
        _count.value = 0
        Log.i("count reset", "${count}")
    }

    override fun getAllCountList(): Flow<List<Count>> {
        Log.i("all count history", "${countDao.getAllCounts()}")
        return countDao.getAllCounts()
    }

    override suspend fun insertCount(count: Count) {
        countDao.insertCount(count)
        Log.i("insert count", "inserted")
    }

    override suspend fun deleteCount(countId: Int) {
        countDao.deleteCountById(countId)
        Log.i("delete count", "deleted")
    }


}