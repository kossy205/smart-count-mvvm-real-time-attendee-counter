package com.kosiso.smartcount.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "counts_table")
data class Count(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val count: Int = 0,
    val countName: String = "",
    val countType: String = "",
    val date: Timestamp = Timestamp(System.currentTimeMillis())
)