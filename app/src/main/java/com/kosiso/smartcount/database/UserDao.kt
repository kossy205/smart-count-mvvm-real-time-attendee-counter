package com.kosiso.smartcount.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE user_table SET name = :newName")
    suspend fun updateUser(newName: String)

    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun getUserById(): User
}