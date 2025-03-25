package com.kosiso.smartcount.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.imperiumlabs.geofirestore.GeoQuery

interface MainRepository {
    val count: StateFlow<Int>

    fun increment()
    fun decrement()
    fun resetCount()
//    fun updateCount(callback: (Int) -> Unit)

    fun getAllCountList(): Flow<List<Count>>
    suspend fun insertCount(count: Count)
    suspend fun deleteCount(countId: Int)

    fun getCurrentUser(): FirebaseUser?

    suspend fun signUpUser(email: String, password: String): Result<FirebaseUser>
    suspend fun signInUser(email: String, password: String): Result<FirebaseUser>
    fun signOut()

    suspend fun registerUserInDB(user:User): Result<Unit>

    suspend fun addToAvailableUsersDB(user: User): Result<Unit>
    suspend fun removeFromAvailableUsersDB(): Result<Unit>
    suspend fun removeGeofirestoreLocation(): Result<Unit>


    fun queryAvailableUsers(geoPoint: GeoPoint, radius: Double): GeoQuery

    suspend fun getUserDetails(): Result<User>
    suspend fun setLocationUsingGeoFirestore(userId: String, geoPoint: GeoPoint): Result<Unit>


}