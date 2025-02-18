package com.kosiso.smartcount.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.ui.screen_states.MainOperationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainRepository: MainRepository): ViewModel(){

    val count: StateFlow<Int> = mainRepository.count

    private val _roomOperationResult = MutableStateFlow<MainOperationState<List<Count>>>(MainOperationState.Loading)
    val roomOperationResult: StateFlow<MainOperationState<List<Count>>> = _roomOperationResult

    private val _authOperationResult = MutableStateFlow<MainOperationState<FirebaseUser>>(MainOperationState.Loading)
    val authOperationResult: StateFlow<MainOperationState<FirebaseUser>> = _authOperationResult

    private val _registerOperationResult = MutableStateFlow<MainOperationState<Unit>>(MainOperationState.Loading)
    val registerOperationResult: StateFlow<MainOperationState<Unit>> = _registerOperationResult



    init {// Launches once when the view model comes live
        Log.i("launch count view model", "launched")
        viewModelScope.launch{// Launched once, but collects indefinitely
            _roomOperationResult.value = MainOperationState.Loading
            Log.i("launch count view model 1", "launched")
            try {
                mainRepository.getAllCountList().collect{it->
                    _roomOperationResult.value = MainOperationState.Success(it)
                    Log.i("all count history V.model", "$it")
                }
            }catch (e:Exception){
                _roomOperationResult.value = MainOperationState.Error(e.message ?: "Error fetching count history")
            }

        }
    }

    /**
     * Sign up
     */
    fun signUpNewUser(email: String, password: String){
        viewModelScope.launch{
            _authOperationResult.value = MainOperationState.Loading
            val signUpResult = mainRepository.signUpUser(email, password)
            signUpResult.onSuccess {firebaseUser->
                _authOperationResult.value = MainOperationState.Success(firebaseUser)
            }
            signUpResult.onFailure {
                _authOperationResult.value = MainOperationState.Error(it.message.toString())
            }
        }
    }
     fun signInUser(email: String, password: String){
         viewModelScope.launch{
             _authOperationResult.value = MainOperationState.Loading
             val signInResult = mainRepository.signInUser(email, password)
             signInResult.onSuccess {firebaseUser->
                 _authOperationResult.value = MainOperationState.Success(firebaseUser)
             }
             signInResult.onFailure {
                 _authOperationResult.value = MainOperationState.Error(it.message.toString())
             }
         }
     }
    fun registerNewUserInDB(user: User){
        viewModelScope.launch{
            _registerOperationResult.value = MainOperationState.Loading
            val registerUserInDBResult = mainRepository.registerUserInDB(user)
            registerUserInDBResult.onSuccess {
                _registerOperationResult.value = MainOperationState.Success(Unit)
            }
            registerUserInDBResult.onFailure {
                _registerOperationResult.value = MainOperationState.Error(it.message.toString())
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