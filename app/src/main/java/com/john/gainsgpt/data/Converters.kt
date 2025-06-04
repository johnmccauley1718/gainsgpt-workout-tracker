package com.john.gainsgpt.data.local

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Converters {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @TypeConverter
    fun fromDateString(value: String?): Date? {
        return value?.let {
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun toDateString(date: Date?): String? {
        return date?.let {
            try {
                dateFormat.format(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}
