package com.kosiso.smartcount.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User


@Database(
    entities = [Count::class, User::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract class RoomDatabase: RoomDatabase() {

    abstract fun countDao(): CountDao
    abstract fun userDao(): UserDao
}