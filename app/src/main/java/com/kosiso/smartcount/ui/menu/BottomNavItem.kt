package com.kosiso.smartcount.ui.menu

import androidx.annotation.DrawableRes

data class BottomNavItem(
    val id: String,
    val name: String,
    val route: String,
    @DrawableRes val icon: Int
)
