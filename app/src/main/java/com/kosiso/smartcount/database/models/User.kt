package com.kosiso.smartcount.database.models

data class User(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val image: String = "",
    val count: Long = 0,
    val countPartners: List<String> = emptyList()
)