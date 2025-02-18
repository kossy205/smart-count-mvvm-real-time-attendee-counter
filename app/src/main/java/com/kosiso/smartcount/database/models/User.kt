package com.kosiso.smartcount.database.models

data class User(
    val id: String = "",
    val name: String = "",
    val phone: Long = 0,
    val email: String = "",
    val image: String = ""
)