package com.kosiso.smartcount.repository

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainRepoImpl(): MainRepository {

    private val _count = MutableStateFlow(0)
    override val count : StateFlow<Int> = _count

    override fun increment(){
        _count.value = _count.value + 1
        Log.i("count increase", "${count}")
//        return _count.value
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
//        return _count.value
    }

//    override fun updateCount(callback: (Int) -> Unit) {
//        callback(count.value)
//    }
}