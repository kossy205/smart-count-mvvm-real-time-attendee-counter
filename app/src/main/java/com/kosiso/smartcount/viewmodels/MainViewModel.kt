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
import com.kosiso.smartcount.repository.LocationRepository
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.ui.screen_states.MainOperationState
import com.kosiso.smartcount.utils.Constants
import com.kosiso.smartcount.utils.CrashlyticsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository,
    private val crashlyticsUtils: CrashlyticsUtils): ViewModel(){

    private lateinit var geoQueryEventListener: GeoQueryEventListener
    private lateinit var geoQuery: GeoQuery
    private var isGeoQueryActive: Boolean = false
    
    @Inject lateinit var locationRepository: LocationRepository

    // used to make sure available user is fetched once and with the first geoPoint
    // this way it doesn't keep fetching if there is a new location update every 5 sec
    // the list on the screen no longer blinks
    var hasFetchedAvailableUser = false

    val count: StateFlow<Int> = mainRepository.count


    // part of count down logic
    private val _countdown = MutableStateFlow<Int>(0)
    val countdown: StateFlow<Int> = _countdown
    private val _isFinished = MutableLiveData<Boolean>()
    val isFinished: LiveData<Boolean> get() = _isFinished

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

    private val _updateUserDetailsResult = MutableStateFlow<String?>(null)
    val updateUserDetailsResult: StateFlow<String?> = _updateUserDetailsResult

    private val _getUserDetailsFromRoomDBResult = MutableStateFlow<MainOperationState<User>>(MainOperationState.Idle)
    val getUserDetailsFromRoomDBResult: StateFlow<MainOperationState<User>> = _getUserDetailsFromRoomDBResult

    private val _countPartnersList = MutableLiveData<List<String>>(emptyList())
    val countPartnersList: LiveData<List<String>> = _countPartnersList

    private val _isUserCountStarter = MutableStateFlow<MainOperationState<Boolean>>(
        MainOperationState.Idle)
    val isUserCountStarter: StateFlow<MainOperationState<Boolean>> = _isUserCountStarter

//    private val _userUpdatedSuccess = MutableLiveData<List<String>>(emptyList())
//    val userUpdatedSuccess: LiveData<List<String>> = _userUpdatedSuccess



    init {
        // Launches once when the view model comes live
        Log.i("launch count view model", "launched")
        getAllCountHistoryList()

        viewModelScope.launch{
            countPartnersList.asFlow().collect{
                Log.i("count partners 2 xxl", "$it")
                if(it.isNotEmpty()){
                    addFirebaseListenerToListOfCountPartner(it)
                }
            }
        }
    }

    /**
     * Auth/Registration
     */
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
                crashlyticsUtils.recordException(it)
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

                getUserToInsertIntoRoomDB()
            }
            signInResult.onFailure {
                _authOperationResult.value = MainOperationState.Error(it.message.toString())
                Log.i("logging in user", "errorMessage: ${it}")
                crashlyticsUtils.recordException(it)
            }
        }
    }

    fun resetAuthState() {
        _authOperationResult.value = MainOperationState.Idle
    }
    fun resetRegisterState() {
        _registerOperationResult.value = MainOperationState.Idle
    }

    fun getUserToInsertIntoRoomDB(){
        viewModelScope.launch{
            mainRepository.getUserDetails().apply {
                onSuccess { user->
                    insertUserIntoRoomDB(user)
                }
                onFailure {
                    Log.i("get user details for roomDB", "error getting user details to insert into room: ${it.message}")
                    crashlyticsUtils.recordException(it)
                }
            }
        }
    }

    fun getCurrentUser(): FirebaseUser?{
        return mainRepository.getCurrentUser()
    }

    fun signOut(){
        Log.i("user signed out", "signed out")
        return mainRepository.signOut()
    }


    //----------------------------------------------------------------------------------------------


    /**
     * GeoFirestore operations
     */
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
                crashlyticsUtils.recordException(it)
            }
        }
    }
    fun fetchAvailableUsers(geoPoint: GeoPoint){

        _availableUsers.value = MainOperationState.Loading

        Log.i("fetch Available Users", "start")
        val radius = 1.0
        geoQuery = mainRepository.queryAvailableUsers(geoPoint, radius)
        val listOfAvailableUsersDoc = mutableListOf<User>()

        viewModelScope.launch {
            for (i in 30 downTo 0) {
                _countdown.value = i
                delay(1000L) // 1-second delay
            }
            _isFinished.postValue(true) // Trigger action when done
            _availableUsers.value = MainOperationState.Success(listOfAvailableUsersDoc)
            Log.i("count time done 0","done $listOfAvailableUsersDoc")
            if(isFinished.value == true){
                _availableUsers.value = MainOperationState.Success(listOfAvailableUsersDoc)
                Log.i("count time done 1","done $listOfAvailableUsersDoc")
            }
        }

        geoQueryEventListener = object : GeoQueryEventListener{
            override fun onGeoQueryError(exception: Exception) {
                Log.i("geoquery error", "$exception")
                _availableUsers.value = MainOperationState.Error(exception.message.toString())
                crashlyticsUtils.recordException(exception)
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
//                        _availableUsers.value = MainOperationState.Success(listOfAvailableUsersDoc)
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
    // GeoFire Listeners
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


    //----------------------------------------------------------------------------------------------


    /**
     * Location Operations
     */
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


    //----------------------------------------------------------------------------------------------


    /**
     * User Collection
     */
    fun registerNewUserInDB(user: User){
        viewModelScope.launch{
            _registerOperationResult.value = MainOperationState.Loading
            val registerUserInDBResult = mainRepository.registerUserInDB(user)
            registerUserInDBResult.onSuccess {
                _registerOperationResult.value = MainOperationState.Success(Unit)
                insertUserIntoRoomDB(user)
            }
            registerUserInDBResult.onFailure {
                _registerOperationResult.value = MainOperationState.Error(it.message.toString())
                crashlyticsUtils.recordException(it)
            }

        }
    }
    fun updateUserDetails(newName: String){
        viewModelScope.launch{
            val userId = getCurrentUser()!!.uid
            mainRepository.updateUserDetails(userId, newName).apply {
                onSuccess {
                    //always triggerring fix the bug
                    updateUserInToRoomDB(newName)
                }
                onFailure {
                    _updateUserDetailsResult.value = it.message.toString()
                    crashlyticsUtils.recordException(it)
                }
            }
        }
    }
    fun getUserDetails(){
        viewModelScope.launch{
            val getUserDetails = mainRepository.getUserDetails()
            getUserDetails.onSuccess {user ->
                _getUserDetailsResult.value = MainOperationState.Success(user)

            }
            getUserDetails.onFailure {
                _getUserDetailsResult.value = MainOperationState.Error(it.message.toString())
                crashlyticsUtils.recordException(it)

            }
        }
    }


    //----------------------------------------------------------------------------------------------


    /**
     * Available User Collections
     */
    // upload
    fun addToAvailableUsersDB(user: User){
        Log.i("add To Available Users DB VM", "start")
        viewModelScope.launch{
            _uploadToAvailableUsersDBResult.value = MainOperationState.Loading
            val uploadToAvailableUsers = mainRepository.addToAvailableUsersDB(user)
            uploadToAvailableUsers.onSuccess {
                _uploadToAvailableUsersDBResult.value = MainOperationState.Success(Unit)
                getCurrentLocationUpdate()
                Log.i("add To Available Users DB VM", "done $user")
            }
            uploadToAvailableUsers.onFailure {
                _uploadToAvailableUsersDBResult.value = MainOperationState.Error(it.message.toString())
                Log.i("add To Available Users DB VM", "error ${it.message}")
                crashlyticsUtils.recordException(it)
            }
        }
    }
    fun checkIfUserIsCountStarter(){
        viewModelScope.launch{
            val getAvailableUser = mainRepository.getAvailableUserDetails()
            getAvailableUser.onSuccess {user ->
                val isStarter = user.isStarter
                _isUserCountStarter.value = MainOperationState.Success(isStarter)
                Log.i("is user count starter vm", "${user.isStarter}, $user")
            }
            getAvailableUser.onFailure {
                Log.i("check If User IsCount Starter", it.message.toString())
                _isUserCountStarter.value = MainOperationState.Error(it.message.toString())
                crashlyticsUtils.recordException(it)

            }
        }
    }

    // delete
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
                crashlyticsUtils.recordException(it)

            }
        }
    }

    //Update
    fun updateUserCountPartnersInFirebase(countPartnerId: String, countPartners: List<String>){
        viewModelScope.launch{
            mainRepository.updateAvailableUser(
                countPartnerId,
                Constants.COUNT_PARTNERS,
                countPartners).apply {
                onSuccess {
                    Log.i("user count partners update", "success: ${countPartners}")
                }
                onFailure {
                    Log.i("user count partners update", "failed ${it.message}")
                    crashlyticsUtils.recordException(it)
                }
            }
        }
    }
    fun updateUserCountInFirebase(countValue: Long){
        viewModelScope.launch{
            mainRepository.updateAvailableUser(
                getCurrentUser()!!.uid,
                Constants.COUNT,
                countValue).apply {
                onSuccess {
                    Log.i("user count update", "success")
                }
                onFailure {
                    Log.i("user count update", "failed ${it.message}")
                    crashlyticsUtils.recordException(it)
                }
            }
        }
    }
    fun updateUserStarterCounterStatus(){
        viewModelScope.launch{
            mainRepository.updateAvailableUser(
                userId = getCurrentUser()!!.uid,
                fieldName = Constants.IS_STARTER,
                fieldValue = true
            )
            Log.i("update Starter Status", "done")
        }
    }


    //----------------------------------------------------------------------------------------------


    /**
     * Room Operations
     */
    fun updateUserInToRoomDB(newName: String){
        viewModelScope.launch{
            mainRepository.updateUserInRoom(newName).apply {
                onSuccess {
                    _updateUserDetailsResult.value = "User updated successfully"
                    getUserDetailsFromRoomDB()
                }
                onFailure {
                    _updateUserDetailsResult.value = it.message.toString()
                    crashlyticsUtils.recordException(it)
                }
            }
        }
    }

    fun getUserDetailsFromRoomDB(){
        viewModelScope.launch{
            mainRepository.getUserDetailsFromRoomDB().apply {
                onSuccess { user->
                    _getUserDetailsFromRoomDBResult.value = MainOperationState.Success(user)
                }
                onFailure {
                    _getUserDetailsFromRoomDBResult.value = MainOperationState.Error(it.message.toString())
                    crashlyticsUtils.recordException(it)
                }
            }
        }
    }
    fun getAllCountHistoryList(){
        Log.i("launch count view model", "launched")
        viewModelScope.launch {// Launched once, but collects indefinitely
            _roomOperationResult.value = MainOperationState.Loading
            Log.i("launch count view model 1", "launched")
            try {
                mainRepository.getAllCountList().collect { it ->
                    _roomOperationResult.value = MainOperationState.Success(it)
                    Log.i("all count history V.model", "$it")
                }
            } catch (e: Exception) {
                _roomOperationResult.value =
                    MainOperationState.Error(e.message ?: "Error fetching count history")
                crashlyticsUtils.recordException(e)
            }
        }
    }

    fun insertUserIntoRoomDB(user: User){
        val user = User(
            id = user.id,
            name = user.name,
            phone = user.phone,
            email = user.email,
            password = user.password,
            image = user.image,
            count = user.count,
            countPartners = user.countPartners
//            isStarter = false
        )
        viewModelScope.launch{
            mainRepository.insertUserInToRoom(user).apply {
                onSuccess {
                    Log.i("insert User Into RoomDB", "success")
                }
                onFailure {
                    Log.i("insert User Into RoomDB", "failed: ${it.message}")
                    crashlyticsUtils.recordException(it)
                }
            }
        }
    }
    // add a success result or failure result
    fun insertCount(count: Count){
        viewModelScope.launch{
            mainRepository.insertCount(count)
        }
    }

    // room delete
    fun deleteCount(countId: Int){
        viewModelScope.launch{
            mainRepository.deleteCount(countId)
        }
    }


    //----------------------------------------------------------------------------------------------


    /**
     * Firebase Listeners
     */
    // listens for changes in current user
    fun addUserListener(){
        val currentUserId = getCurrentUser()?.uid.toString()

        mainRepository.addUserListener(
            documentId = currentUserId,
            onUpdate = {updatedUserResult->
                updatedUserResult.apply {
                    onSuccess {updatedUser->
                        val countPartners = updatedUser.countPartners
                        if(countPartners.isEmpty()){
                            Log.i("count partners xxx", "empty $countPartners")
                            _selectedUserListData.value = mutableListOf<User>()
                            listOfCountPartners = mutableListOf()
                            removeCountPartnerListener()
                        }else{
                            Log.i("count partners xxx", "$countPartners")
                            _countPartnersList.value = countPartners
                            removeUserListener()
                        }
                    }
                    onFailure {
                        crashlyticsUtils.recordException(it)
                    }
                }
            }
        )
    }

    fun addFirebaseListenerToListOfCountPartner(documentIDList: List<String>){
        //  removeCountPartnerListener()
        Log.i("count partners 3 xxl", "$documentIDList")
        documentIDList.forEach{documentID->
            addCountPartnerListener(documentID)
        }
    }

    // count partner listener
    fun addCountPartnerListener(documentID: String){
        mainRepository.addCountPartnerListener(
            documentId = documentID,
            onUpdate = {countPartnerResult->
                countPartnerResult.apply{

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
                        crashlyticsUtils.recordException(exception)
                    }
                }
            }
        )
    }




    fun removeUserListener(){
        mainRepository.stopUserListener()
    }
    fun removeCountPartnerListener(){
        mainRepository.stopAllCountPartnerListeners()
    }
    fun removeAllFirebaseListeners(){
        mainRepository.stopAllFirebaseListeners()
    }


    //----------------------------------------------------------------------------------------------


    /**
     * finish count
      */
    fun finishSessionCount(){
        // adding resetCount() below would reset counts when a user is counting offline.
        // If they navigate away from the tapcount tab and then come back again, this function
        // would be called because they're always offline.
        // that means, if set, counts would be reset if client is in offline
//        resetCount()

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
        stopLocationUpdates()
    }


    //----------------------------------------------------------------------------------------------


    /**
     * Count Operations
     */
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
        Log.i("add to selected counters list", "${selectedUserListData.value}")
    }
    // the "_selectedUserListData" is used to represent both selected users data and count partners
    // CountPartnerUserList is for users that are not the session count starters
    // SelectedUserList is for the users that stared the count session
    fun setCountPartnerUserList(list: MutableList<User>){
        _selectedUserListData.value = list
        Log.i("add to selected counters list", "${list}")
    }
    fun canFetchAvailableUsers(canFetchAvailableUsers: Boolean){
        _canFetchAvailableUsers.value = canFetchAvailableUsers
    }


    override fun onCleared() {
        Log.d("MainViewModel", "ViewModel cleared")
        removeAllFirebaseListeners()
        removeGeoQueryEventListeners()
        super.onCleared()
    }

}