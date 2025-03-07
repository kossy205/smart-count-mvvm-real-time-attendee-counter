package com.kosiso.smartcount.repository

import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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



}