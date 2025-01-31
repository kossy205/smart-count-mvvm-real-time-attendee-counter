package com.kosiso.smartcount.database

import androidx.room.TypeConverter
import java.sql.Timestamp

class Converters {

    // Convert Timestamp to Long (for storing in database)
    @TypeConverter
    fun fromTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it) }
    }

    // Convert Long (timestamp) to Timestamp
    @TypeConverter
    fun toTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.time
    }
}
