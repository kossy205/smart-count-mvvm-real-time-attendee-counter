package com.kosiso.smartcount.repository

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.kosiso.smartcount.database.CountDao
import com.kosiso.smartcount.database.RoomDatabase
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import javax.inject.Inject

class MainRepoImpl @Inject constructor(
    val countDao: CountDao,
    val firebaseAuth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    val geoFirestore: GeoFirestore
    ): MainRepository {

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

    override suspend fun signUpUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()
            Result.success(authResult.user!!)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun signInUser(email: String, password: String): Result<FirebaseUser> {
        return try{
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
            Result.success(authResult.user!!)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun registerUserInDB(user: User): Result<Unit> {
        return try{
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

    override suspend fun addToAvailableUsersDB(user: User): Result<Unit> {
        return try{
            firestore
                .collection(Constants.AVAILABLE_USERS)
                .document(getCurrentUser()!!.uid)
                .set(user, SetOptions.merge())
                .await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun removeFromAvailableUsersDB(): Result<Unit> {
        return try {
            firestore.collection(Constants.AVAILABLE_USERS)
                .document(getCurrentUser()?.uid.toString())
                .delete()
                .await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getUserDetails(): Result<User> {
        return try {
            val document = firestore.collection(Constants.USERS)
                .document(getCurrentUser()?.uid.toString())
                .get()
                .await()

            val user = document.toObject(User::class.java)
                ?: return Result.failure(Exception("User not found"))

            Result.success(user)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun setLocationUsingGeoFirestore(userId: String, geoPoint: GeoPoint): Result<Unit> {
        return try{
            geoFirestore.setLocation(userId, geoPoint)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

}