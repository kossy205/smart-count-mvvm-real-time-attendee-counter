package com.kosiso.smartcount.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.kosiso.smartcount.database.CountDao
import com.kosiso.smartcount.database.UserDao
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery
import javax.inject.Inject

class MainRepoImpl @Inject constructor(
    val countDao: CountDao,
    val userDao: UserDao,
    val firebaseAuth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    val geoFirestore: GeoFirestore
    ): MainRepository {

    private val _count = MutableStateFlow(0)
    override val count : StateFlow<Int> = _count


    private var userListenerRegistration: ListenerRegistration? = null
    private var isUserListening = false
    private var currentUserDocRef: DocumentReference? = null

    // count Partner listeners - a map to store multiple count partner listeners
    private data class CountPartnerItemListener(
        val registration: ListenerRegistration,
        val countPartnerId: String,
        val docRef: DocumentReference
    )
    // Map of listener ID to listener data
    private val countPartnerListeners = mutableMapOf<String, CountPartnerItemListener>()


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

    override suspend fun insertUserInToRoom(user: User): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userDao.insertUser(user)
                Result.success(Unit)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUserInRoom(newName: String): Result<Unit> {
        return withContext(Dispatchers.IO){
            try {
                userDao.updateUser(newName)
                Result.success(Unit)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }




    override suspend fun signUpUser(email: String, password: String): Result<FirebaseUser> {
        return withContext(Dispatchers.IO){
            try {
                val authResult = firebaseAuth
                    .createUserWithEmailAndPassword(email, password)
                    .await()
                Result.success(authResult.user!!)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override suspend fun signInUser(email: String, password: String): Result<FirebaseUser> {
        return withContext(Dispatchers.IO){
            try{
                Log.i("add To Available Users DB VM", "start")
                val authResult = firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .await()
                Log.i("add To Available Users DB VM", "done")
                Result.success(authResult.user!!)
            }catch (e: Exception){
                Log.i("add To Available Users DB VM", "error: ${e.message}")
                Result.failure(e)
            }
        }
    }

    override suspend fun registerUserInDB(user: User): Result<Unit> {
        return withContext(Dispatchers.IO){
            try{
                firestore
                    .collection(Constants.USERS)
                    .document(getCurrentUser()!!.uid)
                    .set(user, SetOptions.merge())
                    .await()
                Result.success(Unit)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override suspend fun addToAvailableUsersDB(user: User): Result<Unit> {
        return withContext(Dispatchers.IO){
            try{
                firestore
                    .collection(Constants.AVAILABLE_USERS)
                    .document(getCurrentUser()!!.uid)
                    .set(user, SetOptions.merge())
                    .await()

                Log.i("add To Available Users DB repo", "done $user")
                Result.success(Unit)
            }catch (e: Exception){
                Log.i("add To Available Users DB repo", "fail ${e.message}")
                Result.failure(e)
            }
        }
    }

    override suspend fun removeFromAvailableUsersDB(): Result<Unit> {
        return withContext(Dispatchers.IO){
            try {
                Log.i("remove From Available User DB impl", "start")
                firestore.collection(Constants.AVAILABLE_USERS)
                    .document(getCurrentUser()?.uid.toString())
                    .delete()
                    .await()
                Log.i("remove From Available User DB impl", "done")
                Result.success(Unit)
            }catch (e: Exception){
                Log.i("remove From Available User DB impl", e.message.toString())
                Result.failure(e)
            }
        }
    }

    // no need for this, the "removeFromAvailableUsersDB()" already does this.
    // no need for this.
    // calling the below function after "removeFromAvailableUsersDB()" has been called ...
    // would lead to an empty doc being created in firebase.
    // This is because you're asking it to remove geofirelocation from a doc that has been removed already.
    override suspend fun removeGeofirestoreLocation(): Result<Unit> {
        return withContext(Dispatchers.IO){
            try {
                Log.i("remove Geofirestore Location", "start")
                geoFirestore.removeLocation(getCurrentUser()?.uid.toString())
                Log.i("remove Geofirestore Location", "done")
                Result.success(Unit)
            }catch (e: Exception){
                Log.i("remove Geofirestore Location", "error")
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserDetails(): Result<User> {
        return withContext(Dispatchers.IO){
            try {
                val document = firestore.collection(Constants.USERS)
                    .document(getCurrentUser()?.uid.toString())
                    .get()
                    .await()

                val user = document.toObject(User::class.java)
                    ?: return@withContext Result.failure(Exception("User not found"))

                Result.success(user)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }
    override suspend fun getAvailableUserDetails(): Result<User> {
        return withContext(Dispatchers.IO){
            try {
                val document = firestore.collection(Constants.AVAILABLE_USERS)
                    .document(getCurrentUser()?.uid.toString())
                    .get()
                    .await()

                val user = document.toObject(User::class.java)
                    ?: return@withContext Result.failure(Exception("User not found"))

                Log.i("get available user detail ", "${document.data}")
                Result.success(user)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override suspend fun setLocationUsingGeoFirestore(userId: String, geoPoint: GeoPoint): Result<Unit> {
        return withContext(Dispatchers.IO){
            try{
                geoFirestore.setLocation(userId, geoPoint)
                Result.success(Unit)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override fun queryAvailableUsers(geoPoint: GeoPoint, radius: Double): GeoQuery {
        return geoFirestore.queryAtLocation(geoPoint, radius)
    }

    override suspend fun getDocFromDB(
        collection: String,
        documentId: String
    ): Result<DocumentSnapshot> {
        return withContext(Dispatchers.IO){
            try {
                val document = firestore.collection(collection)
                    .document(documentId)
                    .get()
                    .await()

                Result.success(document)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override fun addUserListener(
        documentID: String,
        onUpdate: (Result<User>) -> Unit) {

        if (isUserListening) {
            Log.i("user listener", "Already listening to a user, stopping previous listener")
            stopUserListener()
        }
        try {
            val userDocRef = firestore
                .collection(Constants.AVAILABLE_USERS)
                .document(documentID)
            val userListenerRegistration = userDocRef
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.i("user listener", "User listen failed: ${error.message}")
                        onUpdate(Result.failure(error))
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.i("user listener", "Current user data updated: ${snapshot.id}")
                        try {
                            // Convert document to user data
                            val userData = snapshot.toObject(User::class.java)
                            onUpdate(Result.success(userData!!))
                        } catch (e: Exception) {
                            Log.i("user listener", "Error parsing user data: ${e.message}")
                            onUpdate(Result.failure(e))
                        }
                    } else {
                        Log.i("user listener", "Current user document doesn't exist")
                        onUpdate(Result.failure(Exception("User document not found")))
                    }
                }
        }catch (e:Exception){
            Log.i("user listener", "Error setting up user listener: ${e.message}")
            onUpdate(Result.failure(e))
            isUserListening = false
        }

        isUserListening = true
        Log.i("user listener", "Listener registered for user: $documentID")
    }
    override fun stopUserListener() {
        Log.i("user listener", "Stopping user listener, isUserListening=$isUserListening")
        userListenerRegistration?.let {
            it.remove()
            Log.i("user listener", "User listener removed")
        }
        userListenerRegistration = null
        isUserListening = false
    }

    override fun addCountPartnerListener(
        documentId: String,
        onUpdate: (Result<User>) -> Unit): String {
        // since this would be used to listen to several count partners,
        // a unique identifier for each listener has to be created which is "listenerId".
        // I just want it to be same with the documentID.
        val listenerId = documentId

        // If already listening to this specific count partner with this ID, stop first
        if (countPartnerListeners.containsKey(listenerId)) {
            Log.i("count partner listener", "Already listening to food item with ID $listenerId, stopping previous listener")
            stopCountPartnerListener(listenerId)
        }

        try {
            Log.i("count partner listener", "Starting listener for food item: $documentId with listener ID: $listenerId")

            // Get reference to the count partner document
            val countPartnerDocRef = firestore
                .collection(Constants.AVAILABLE_USERS)
                .document(documentId)

            // Set up the listener
            val registration = countPartnerDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.i("count partner listener", "Food item listen failed for $listenerId: ${error.message}")
                    onUpdate(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.i("count partner listener", "Food item data updated for $listenerId: ${snapshot.id}")
                    try {
                        // Convert document to count partner data
                        val countPartner = snapshot.toObject(User::class.java)
                        onUpdate(Result.success(countPartner!!))
                    } catch (e: Exception) {
                        Log.i("count partner listener", "Error parsing food item data for $listenerId: ${e.message}")
                        onUpdate(Result.failure(e))
                    }
                } else {
                    Log.i("count partner listener", "Food item document doesn't exist for $listenerId")
                    onUpdate(Result.failure(Exception("Food item document not found")))
                }
            }

            // Store the listener information
            countPartnerListeners[listenerId] = CountPartnerItemListener(
                registration = registration,
                countPartnerId = documentId,
                docRef = countPartnerDocRef
            )

            Log.i("count partner listener", "Listener registered for food item: $documentId with ID: $listenerId")
            return listenerId

        } catch (e: Exception) {
            Log.i("count partner listener", "Error setting up food item listener for $listenerId: ${e.message}")
            onUpdate(Result.failure(e))
            return listenerId
        }
    }
    override fun stopCountPartnerListener(listenerId: String) {
        Log.i("count partner listener", "Stopping food item listener with ID: $listenerId")

        countPartnerListeners[listenerId]?.let { listener ->
            listener.registration.remove()
            countPartnerListeners.remove(listenerId)
            Log.i("count partner listener", "Food item listener removed: $listenerId")
        } ?: run {
            Log.i("count partner listener", "No food item listener found with ID: $listenerId")
        }
    }
    override fun stopAllCountPartnerListeners() {
        Log.i("all count partner listener", "Stopping all food item listeners, count: ${countPartnerListeners.size}")

        // Create a copy of the keys to avoid concurrent modification
        val listenerIds = countPartnerListeners.keys.toList()

        for (listenerId in listenerIds) {
            stopCountPartnerListener(listenerId)
        }

        countPartnerListeners.clear()
        Log.i("all count partner listener", "All food item listeners stopped")
    }

    override fun stopAllFirebaseListeners() {
        stopUserListener()
        stopAllCountPartnerListeners()
        Log.i("all firebase listener", "All listeners stopped")
    }


    override suspend fun updateAvailableUser(
        userId: String,
        fieldName: String,
        fieldValue: Any): Result<Unit> {
        return withContext(Dispatchers.IO){

            val update = mapOf(fieldName to fieldValue)
            try {
                firestore
                    .collection(Constants.AVAILABLE_USERS)
                    .document(userId)
                    .update(update)
                    .await()
                Log.i("update available user", "done $fieldName = $fieldValue")
                Result.success(Unit)
            }catch (e:Exception){
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUserDetails(userId: String, newName: String): Result<Unit> {
        return withContext(Dispatchers.IO){
            try {
                firestore
                    .collection(Constants.USERS)
                    .document(userId)
                    .update(Constants.NAME, newName)
                    .await()
                Result.success(Unit)
            }catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserDetailsFromRoomDB(): Result<User> {
        return withContext(Dispatchers.IO){
            try {
                val user = userDao.getUserById()
                Result.success(user)
            }catch (e: Exception){

                Result.failure(e)
            }
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }










//    override suspend fun updateUserCountInFirebase(countValue: Long): Result<Unit> {
//        return withContext(Dispatchers.IO){
//            try {
//                firestore
//                    .collection(Constants.AVAILABLE_USERS)
//                    .document(getCurrentUser()?.uid!!)
//                    .update(Constants.COUNT, countValue)
//                    .await()
//                Result.success(Unit)
//            }catch (e: Exception){
//                Result.failure(e)
//            }
//        }
//    }
//
//    override suspend fun updateUserCountPartnersInFirebase(countPartnerId: String,countPartners: List<String>): Result<Unit> {
//        return withContext(Dispatchers.IO){
//            try {
//                firestore
//                    .collection(Constants.AVAILABLE_USERS)
//                    .document(countPartnerId)
//                    .update(Constants.COUNT_PARTNERS, countPartners)
//                Result.success(Unit)
//            }catch (e: Exception){
//                Result.failure(e)
//            }
//        }
//    }

}