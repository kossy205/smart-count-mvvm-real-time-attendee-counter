package com.kosiso.smartcount.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kosiso.smartcount.database.models.Count
import kotlinx.coroutines.flow.Flow

@Dao
interface CountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCount(count: Count)

    @Query("SELECT * FROM counts_table WHERE id = :countId")
    suspend fun getCountById(countId: Int): Count

    @Query("SELECT * FROM counts_table ORDER BY id DESC")
    fun getAllCounts(): Flow<List<Count>>

    @Query("DELETE FROM counts_table WHERE id = :countId")
    suspend fun deleteCountById(countId: Int)
}