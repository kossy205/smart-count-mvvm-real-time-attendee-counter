package com.kosiso.smartcount.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.ui.screen_states.RoomOperationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainRepository: MainRepository): ViewModel(){

    val count: StateFlow<Int> = mainRepository.count

//    private val _countList = MutableStateFlow<List<Count>>(emptyList())
//    val countList: StateFlow<List<Count>> = _countList

    private val _roomOperationResult = MutableStateFlow<RoomOperationState<List<Count>>>(RoomOperationState.Loading)
    val roomOperationResult: StateFlow<RoomOperationState<List<Count>>> = _roomOperationResult


    init {// Launches once when the view model comes live
        Log.i("launch count view model", "launched")
        viewModelScope.launch{// Launched once, but collects indefinitely
            _roomOperationResult.value = RoomOperationState.Loading
            Log.i("launch count view model 1", "launched")
            try {
                mainRepository.getAllCountList().collect{it->
                    _roomOperationResult.value = RoomOperationState.Success(it)
                    Log.i("all count history V.model", "$it")
                }
            }catch (e:Exception){
                _roomOperationResult.value = RoomOperationState.Error(e.message ?: "Error fetching count history")
            }

        }
    }

    fun increment(){
        mainRepository.increment()
        Log.i("count increase 1", "${count}")
    }

    fun decrement(){
        mainRepository.decrement()
        Log.i("count decrease 1", "${count}")
    }

    fun reset(){
        mainRepository.resetCount()
        Log.i("count reset 1", "${count}")
    }

    // add a success result or failure result
    fun insertCount(count: Count){
        viewModelScope.launch{
            mainRepository.insertCount(count)
        }
    }

    fun deleteCount(countId: Int){
        viewModelScope.launch{
            mainRepository.deleteCount(countId)
        }
    }
}