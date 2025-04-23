package com.kosiso.smartcount.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey()
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val image: String = "",
    val count: Long = 0,
    val countPartners: List<String> = emptyList()
)