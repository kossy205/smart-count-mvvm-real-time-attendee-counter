package com.kosiso.smartcount.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.kosiso.smartcount.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.compareTo
import kotlin.dec

@HiltViewModel
class MainViewModel @Inject constructor(val mainRepository: MainRepository): ViewModel(){


//    private val _count = MutableStateFlow(0)
//    val count : StateFlow<Int> = _count
    val count: StateFlow<Int> = mainRepository.count

    fun increment(){
        mainRepository.increment()
        Log.i("count increase 1", "${count}")
    }

    fun decrement(){
        mainRepository.decrement()
        Log.i("count decrease 1", "${count}")
    }





}