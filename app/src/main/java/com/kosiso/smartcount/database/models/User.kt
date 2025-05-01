package com.kosiso.smartcount.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import com.kosiso.smartcount.utils.Constants

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
    val countPartners: List<String> = emptyList(),
    @get:PropertyName(Constants.IS_STARTER) // Explicitly set Firestore field name
    val isStarter: Boolean = false
)

/**
 * Had to use the "@get:PropertyName(Constants.IS_STARTER)" to force firestore to use "isStarter,
 * because it keeps changing the name to starter which was causing issues.
 */