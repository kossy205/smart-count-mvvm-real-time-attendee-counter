package com.kosiso.smartcount.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.ui.screen_states.MainOperationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainRepository: MainRepository): ViewModel(){

    private lateinit var geoQueryEventListener: GeoQueryEventListener
    private lateinit var geoQuery: GeoQuery
    private var isGeoQueryActive: Boolean = false


    val count: StateFlow<Int> = mainRepository.count

    private val _roomOperationResult = MutableStateFlow<MainOperationState<List<Count>>>(MainOperationState.Loading)
    val roomOperationResult: StateFlow<MainOperationState<List<Count>>> = _roomOperationResult

    private val _authOperationResult = MutableStateFlow<MainOperationState<FirebaseUser>>(MainOperationState.Idle)
    val authOperationResult: StateFlow<MainOperationState<FirebaseUser>> = _authOperationResult

    private val _registerOperationResult = MutableStateFlow<MainOperationState<Unit>>(MainOperationState.Loading)
    val registerOperationResult: StateFlow<MainOperationState<Unit>> = _registerOperationResult

    private val _onlineStatus = MutableStateFlow<Boolean>(false)
    val onlineStatus: StateFlow<Boolean> = _onlineStatus

    private val _uploadToAvailableUsersDBResult = MutableStateFlow<MainOperationState<Unit>>(MainOperationState.Idle)
    val uploadToAvailableUsersDBResult: StateFlow<MainOperationState<Unit>> = _uploadToAvailableUsersDBResult

    private val _removeFromAvailableUsersDBResult = MutableStateFlow<MainOperationState<Unit>>(MainOperationState.Loading)
    val removeFromAvailableUsersDBResult: StateFlow<MainOperationState<Unit>> = _removeFromAvailableUsersDBResult

    private val _getUserDetailsResult = MutableStateFlow<MainOperationState<User>>(MainOperationState.Loading)
    val getUserDetailsResult: StateFlow<MainOperationState<User>> = _getUserDetailsResult

    private val _setLocationWithGeoFireResult = MutableStateFlow<MainOperationState<Unit>>(MainOperationState.Loading)
    val setLocationWithGeoFireResult: StateFlow<MainOperationState<Unit>> = _setLocationWithGeoFireResult



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
    fun resetAuthState() {
        _authOperationResult.value = MainOperationState.Idle
    }
    fun resetRegisterState() {
        _registerOperationResult.value = MainOperationState.Idle
    }

    fun signUpNewUser(email: String, password: String){
        viewModelScope.launch{
            _authOperationResult.value = MainOperationState.Loading
            val signUpResult = mainRepository.signUpUser(email, password)
            signUpResult.onSuccess {firebaseUser->
                _authOperationResult.value = MainOperationState.Success(firebaseUser)
                Log.i("signing up user", "success")
            }
            signUpResult.onFailure {
                _authOperationResult.value = MainOperationState.Error(it.message.toString())
                Log.i("signing up user", "errorMessage: ${it}")
            }
        }
    }
     fun signInUser(email: String, password: String){
         viewModelScope.launch{
             _authOperationResult.value = MainOperationState.Loading
             val signInResult = mainRepository.signInUser(email, password)
             signInResult.onSuccess {firebaseUser->
                 _authOperationResult.value = MainOperationState.Success(firebaseUser)
                 Log.i("logging in user", "success")
             }
             signInResult.onFailure {
                 _authOperationResult.value = MainOperationState.Error(it.message.toString())
                 Log.i("logging in user", "errorMessage: ${it}")
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

    fun addToAvailableUsersDB(user: User){
        viewModelScope.launch{
            _uploadToAvailableUsersDBResult.value = MainOperationState.Loading
            val uploadToAvailableUsers = mainRepository.addToAvailableUsersDB(user)
            uploadToAvailableUsers.onSuccess {
                _uploadToAvailableUsersDBResult.value = MainOperationState.Success(Unit)
//                mainRepository.setLocationUsingGeoFirestore(getCurrentUser()!!.uid, geoPoint)
            }
            uploadToAvailableUsers.onFailure {
                _uploadToAvailableUsersDBResult.value = MainOperationState.Error(it.message.toString())

            }
        }
    }

    fun removeFromAvailableUserDB(){
        viewModelScope.launch{
            _removeFromAvailableUsersDBResult.value = MainOperationState.Loading
            val removeFromAvailableUsers = mainRepository.removeFromAvailableUsersDB()
            removeFromAvailableUsers.onSuccess {
                mainRepository.removeGeofirestoreLocation()
                _removeFromAvailableUsersDBResult.value = MainOperationState.Success(Unit)

            }
            removeFromAvailableUsers.onFailure {
                _removeFromAvailableUsersDBResult.value = MainOperationState.Error(it.message.toString())

            }
        }
    }

    fun getUserDetails(){
        viewModelScope.launch{
            _getUserDetailsResult.value = MainOperationState.Loading
            val getUserDetails = mainRepository.getUserDetails()
            getUserDetails.onSuccess {user ->
                _getUserDetailsResult.value = MainOperationState.Success(user)

            }
            getUserDetails.onFailure {
                _getUserDetailsResult.value = MainOperationState.Error(it.message.toString())

            }
        }
    }

    fun fetchAvailableUsers(geoPoint: GeoPoint){
        val radius = 0.2
        geoQuery = mainRepository.queryAvailableUsers(geoPoint, radius)

        geoQueryEventListener = object : GeoQueryEventListener{
            override fun onGeoQueryError(exception: Exception) {
                Log.i("geoquery error", "$exception")
            }

            override fun onGeoQueryReady() {
                Log.i("geoquery Ready 1", "onGeoQueryReady")

            }

            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                Log.i("geoquery qualified 1", " user entered radius is in the radius.")


            }

            override fun onKeyExited(documentID: String) {
                Log.i("geoquery onKeyExit", "location no longer in the radius")
            }

            override fun onKeyMoved(documentID: String, location: GeoPoint) {
                Log.i("geoquery onKeyMoved", "moved but still in radius location is $location")
            }
        }

        geoQuery.addGeoQueryEventListener(geoQueryEventListener)
        isGeoQueryActive = true
    }

    fun removeGeoQueryEventListeners(){
        if (isGeoQueryActive) {
            geoQuery.removeGeoQueryEventListener(geoQueryEventListener)
            isGeoQueryActive = false
            Log.i("geoquery remove listener", "Listener removed")
        } else {
            Log.i("geoquery remove listener", "No active listener to remove")
        }
    }



    fun getCurrentUser(): FirebaseUser?{
        return mainRepository.getCurrentUser()
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

    fun onlineStatus(isOnline: Boolean){
        _onlineStatus.value = isOnline
    }


}