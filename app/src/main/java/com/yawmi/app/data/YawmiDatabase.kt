package com.yawmi.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class YawmiDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    companion object {
        @Volatile private var INSTANCE: YawmiDatabase? = null
        fun get(context: Context): YawmiDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, YawmiDatabase::class.java, "yawmi.db").build().also { INSTANCE = it }
        }
    }
}
