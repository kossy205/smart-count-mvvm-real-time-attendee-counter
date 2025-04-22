package com.kosiso.smartcount.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.kosiso.foodshare.repository.LocationRepository
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.ui.screen_states.MainOperationState
import com.kosiso.smartcount.utils.Constants
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
    
    @Inject lateinit var locationRepository: LocationRepository

    // used to make sure available user is fetched once and with the first geoPoint
    // this way it doesnt keep fetching if there is a new location update every 5 sec
    // the list on the screen no longer blinks
    var hasFetchedAvailableUser = false

    val count: StateFlow<Int> = mainRepository.count

    private val userListener = mutableMapOf<String, ListenerRegistration>()
    private val countPartnerListener = mutableMapOf<String, ListenerRegistration>()

    private var listOfCountPartners = mutableListOf<User>()

    private val _roomOperationResult = MutableStateFlow<MainOperationState<List<Count>>>(MainOperationState.Loading)
    val roomOperationResult: StateFlow<MainOperationState<List<Count>>> = _roomOperationResult

    private val _authOperationResult = MutableStateFlow<MainOperationState<FirebaseUser>>(MainOperationState.Idle)
    val authOperationResult: StateFlow<MainOperationState<FirebaseUser>> = _authOperationResult

    private val _registerOperationResult = MutableStateFlow<MainOperationState<Unit>>(MainOperationState.Loading)
    val registerOperationResult: StateFlow<MainOperationState<Unit>> = _registerOperationResult

    private val _onlineStatus = MutableStateFlow<Boolean>(false)
    val onlineStatus: StateFlow<Boolean> = _onlineStatus

    // for the check box in Tap Count Screen
    private val _checkedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedStates: StateFlow<Map<String, Boolean>> = _checkedStates

    // for list of users selected to be part of a counting session
    private val _selectedUserListData = MutableStateFlow<MutableList<User>>(mutableListOf())
    val selectedUserListData: StateFlow<MutableList<User>> = _selectedUserListData

    // this would be true only for users that started a count session
    private val _canFetchAvailableUsers = MutableStateFlow<Boolean>(false)
    val canFetchAvailableUsers: StateFlow<Boolean> = _canFetchAvailableUsers

    private val _uploadToAvailableUsersDBResult = MutableStateFlow<MainOperationState<Unit>>(MainOperationState.Idle)
    val uploadToAvailableUsersDBResult: StateFlow<MainOperationState<Unit>> = _uploadToAvailableUsersDBResult

    private val _availableUsers = MutableStateFlow<MainOperationState<List<User>>>(MainOperationState.Idle)
    val availableUsers: StateFlow<MainOperationState<List<User>>> = _availableUsers

    private val _removeFromAvailableUsersDBResult = MutableStateFlow<MainOperationState<Unit>>(MainOperationState.Loading)
    val removeFromAvailableUsersDBResult: StateFlow<MainOperationState<Unit>> = _removeFromAvailableUsersDBResult

    private val _getUserDetailsResult = MutableStateFlow<MainOperationState<User>>(MainOperationState.Idle)
    val getUserDetailsResult: StateFlow<MainOperationState<User>> = _getUserDetailsResult


    private val _countPartnersList = MutableLiveData<List<String>>(emptyList())
    val countPartnersList: LiveData<List<String>> = _countPartnersList



    init {
        // Launches once when the view model comes live
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





            //
            countPartnersList.asFlow().collect{
                if(it.isNotEmpty()){
                    it.forEach{countPartnerId->
                        addFirebaseUserListener(countPartnerId)
                    }
                }
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
        Log.i("add To Available Users DB VM", "start")
        viewModelScope.launch{
            _uploadToAvailableUsersDBResult.value = MainOperationState.Loading
            val uploadToAvailableUsers = mainRepository.addToAvailableUsersDB(user)
            uploadToAvailableUsers.onSuccess {
                _uploadToAvailableUsersDBResult.value = MainOperationState.Success(Unit)
                getCurrentLocationUpdate()
                Log.i("add To Available Users DB VM", "done")
            }
            uploadToAvailableUsers.onFailure {
                _uploadToAvailableUsersDBResult.value = MainOperationState.Error(it.message.toString())
                Log.i("add To Available Users DB VM", "error ${it.message}")

            }
        }
    }

    fun removeFromAvailableUserDB(){
        viewModelScope.launch{
            _removeFromAvailableUsersDBResult.value = MainOperationState.Loading
            val removeFromAvailableUsers = mainRepository.removeFromAvailableUsersDB()
            removeFromAvailableUsers.onSuccess {
//                mainRepository.removeGeofirestoreLocation()
                _removeFromAvailableUsersDBResult.value = MainOperationState.Success(Unit)
            }
            removeFromAvailableUsers.onFailure {
                _removeFromAvailableUsersDBResult.value = MainOperationState.Error(it.message.toString())

            }
        }
    }

//    fun removeGeofirestoreLocation(){
//        viewModelScope.launch{
//            mainRepository.removeGeofirestoreLocation()
//        }
//    }

    fun setLocationUsingGeoFirestore(docId: String, geoPoint: GeoPoint){
        viewModelScope.launch{
            val setLocationGeofirestore = mainRepository.setLocationUsingGeoFirestore(docId, geoPoint)
            setLocationGeofirestore.onSuccess {
                Log.i("geofirestore location", "Location set successfully $geoPoint")

                /**
                 * Only users that started a count session can fetch available users.
                 * the "" is used to ensure that "fetchAvailableUsers(geoPoint)" is called only once,
                 * and with the first provided geoPoint. This will avoid continually fetching users ...
                 * whenever location is been gotten every second.
                 *
                 */
                 if(_canFetchAvailableUsers.value == true && !hasFetchedAvailableUser){
                    fetchAvailableUsers(geoPoint)
                     hasFetchedAvailableUser = true
                }

            }
            setLocationGeofirestore.onFailure {
                Log.i("geofirestore location", "error: ${it.message}")
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


    private fun getCurrentLocationUpdate(){
        locationRepository.getLocationUpdates(
            {location ->
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                viewModelScope.launch{

                    setLocationUsingGeoFirestore(mainRepository.getCurrentUser()!!.uid, geoPoint)
                }
                Log.i("location gotten","$geoPoint")
            },{exception ->
                Log.i("location exception","$exception")
            }
        )
    }

    fun stopLocationUpdates() {
        Log.i("location stopped", "stopped")
        locationRepository.stopLocationUpdates()
    }

    fun fetchAvailableUsers(geoPoint: GeoPoint){

        _availableUsers.value = MainOperationState.Loading

        Log.i("fetch Available Users", "start")
        val radius = 0.2
        geoQuery = mainRepository.queryAvailableUsers(geoPoint, radius)
        val listOfAvailableUsersDoc = mutableListOf<User>()

        geoQueryEventListener = object : GeoQueryEventListener{
            override fun onGeoQueryError(exception: Exception) {
                Log.i("geoquery error", "$exception")
                _availableUsers.value = MainOperationState.Error(exception.message.toString())
            }

            override fun onGeoQueryReady() {
                Log.i("geoquery Ready 1", "onGeoQueryReady")
            }

            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                Log.i("geoquery qualified 1", "user entered radius is in the radius. $documentID")
                viewModelScope.launch{
                    val docFromDB = mainRepository.getDocFromDB(Constants.AVAILABLE_USERS, documentID)
                    docFromDB.onSuccess {document ->
                        val user = document.toObject(User::class.java)!!
                        listOfAvailableUsersDoc.add(user)
                        _availableUsers.value = MainOperationState.Success(listOfAvailableUsersDoc)
                        Log.i("available user doc", "$document")
                        Log.i("available users doc list", "$listOfAvailableUsersDoc")
                    }
                }
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

            _availableUsers.value = MainOperationState.Idle
            hasFetchedAvailableUser = false

            geoQuery.removeGeoQueryEventListener(geoQueryEventListener)
            isGeoQueryActive = false
            Log.i("geoquery remove listener", "Listener removed")
        } else {
            Log.i("geoquery remove listener", "No active listener to remove")
        }
    }


    fun addFirebaseUserListener(documentID: String){
        Log.i("add User listener VM", "${documentID}")
        removeCountPartnerListener()

        val countPartnerListener = mainRepository.addUserListener(
            documentId = documentID,
            onUpdate = {userResult->
                userResult.apply{

                    onSuccess { updatedUser ->

                        Log.i("add User listener VM", "success $documentID")
                        // Updates the user in the selected users list if present
                        val selectedIndex =
                            _selectedUserListData.value.indexOfFirst { it.id == documentID }

                        Log.i("selectedUserListData 1", "${_selectedUserListData.value}")

                        if (selectedIndex != -1) {
                            val updatedSelectedList = _selectedUserListData.value.toMutableList()
                            updatedSelectedList[selectedIndex] = updatedUser
                            _selectedUserListData.value = updatedSelectedList
                            Log.i("selectedUserListData 2", "${_selectedUserListData.value}")
                        }else{
                            listOfCountPartners.add(updatedUser)
                            setCountPartnerUserList(listOfCountPartners)
                            Log.i("selectedUserListData 3", "${_selectedUserListData.value}")
                        }
                        Log.i("selectedUserListData 4", "${_selectedUserListData.value}")
                        Log.i("listOfCountPartners", "${listOfCountPartners}")

                    }
                    onFailure { exception ->
                        Log.i("add User listener VM", "error: ${exception.message}")
                    }
                }
            }
        )
        this@MainViewModel.countPartnerListener[documentID] = countPartnerListener
    }

    fun updateUserCountInFirebase(countValue: Long){
        viewModelScope.launch{
            mainRepository.updateUserCountInFirebase(countValue).apply {
                onSuccess {
                    Log.i("user count update", "success")
                }
                onFailure {
                    Log.i("user count update", "failed ${it.message}")
                }
            }
        }
    }

    fun removeUserListener(){
        userListener.values.forEach { it.remove() }
        userListener.clear()
    }
    fun removeCountPartnerListener(){
        countPartnerListener.values.forEach { it.remove() }
        countPartnerListener.clear()
    }
    fun removeAllFirebaseListeners(){
        removeUserListener()
        removeCountPartnerListener()
    }

    // listens for changes in current user
    fun addUserListener(){
        removeAllFirebaseListeners()

        val currentUserId = getCurrentUser()?.uid.toString()
        var userListner = mainRepository.addUserListener(
            documentId = currentUserId,
            onUpdate = {updatedUserResult->
                updatedUserResult.apply {

                    onSuccess {updatedUser->
                        val countPartners = updatedUser.countPartners
                        if(countPartners.isEmpty()){
                            Log.i("count partners", "empty $countPartners")
                            _selectedUserListData.value = mutableListOf<User>()
                            listOfCountPartners = mutableListOf()
                            removeCountPartnerListener()
                        }else{
                            Log.i("count partners", "$countPartners")
                            _countPartnersList.value = countPartners
                            countPartners.forEach{countPartnerId->
                                addFirebaseUserListener(countPartnerId)
                            }
                            removeUserListener()
                        }
                    }

                    onFailure {
                        Log.i("count partners", "${it.message}")
                    }
                }
            }
        )
        userListener[currentUserId] = userListner
    }

    fun updateUserCountPartnersInFirebase(countPartners: List<String>){
        viewModelScope.launch{
            mainRepository.updateUserCountPartnersInFirebase(countPartners).apply {
                onSuccess {
                    Log.i("user count partners update", "success")
                }
                onFailure {
                    Log.i("user count partners update", "failed ${it.message}")
                }
            }
        }
    }

    fun finishSessionCount(){
        resetCount()

        _onlineStatus.value = false
        _checkedStates.value = emptyMap()
        _selectedUserListData.value = mutableListOf<User>()
        listOfCountPartners.clear()
        listOfCountPartners = mutableListOf<User>()
        _canFetchAvailableUsers.value = false
        _uploadToAvailableUsersDBResult.value = MainOperationState.Idle
        _availableUsers.value = MainOperationState.Idle

        removeAllFirebaseListeners()
        // the below might cause bug since its being called when theres no geoquery listner available
        removeGeoQueryEventListeners()
    }

    fun getCurrentUser(): FirebaseUser?{
        return mainRepository.getCurrentUser()
    }

    fun increment(){
        mainRepository.increment()
        if(_onlineStatus.value == true){
            val count = count.value
            updateUserCountInFirebase(count.toLong())
        }
        Log.i("count increase 1", "${count}")
    }

    fun decrement(){
        mainRepository.decrement()
        if(_onlineStatus.value == true){
            val count = count.value
            updateUserCountInFirebase(count.toLong())
        }
        Log.i("count decrease 1", "${count}")
    }

    fun resetCount(){
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

    fun setCheckedState(userId: String, isChecked: Boolean){
        val currentStates = _checkedStates.value.toMutableMap()
        currentStates[userId] = isChecked
        _checkedStates.value = currentStates
    }

    fun setSelectedUserList(list: MutableList<User>){
        _selectedUserListData.value = list
        Log.i("add to selected counters list", "$selectedUserListData")
    }
    // the "_selectedUserListData" is used to represent both selected users data and count partners
    // CountPartnerUserList is for users that are not the session count started
    // SelectedUserList is for the users that stared the count session
    fun setCountPartnerUserList(list: MutableList<User>){
        _selectedUserListData.value = list
        Log.i("add to selected counters list", "$selectedUserListData")
    }

    fun canFetchAvailableUsers(canFetchAvailableUsers: Boolean){
        _canFetchAvailableUsers.value = canFetchAvailableUsers
    }

    override fun onCleared() {
        removeAllFirebaseListeners()
        removeGeoQueryEventListeners()
        super.onCleared()
    }

}