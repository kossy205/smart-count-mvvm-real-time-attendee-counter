package com.kosiso.smartcount.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kosiso.smartcount.database.models.Count


@Database(
    entities = [Count::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class RoomDatabase: RoomDatabase() {

    abstract fun countDao(): CountDao
}