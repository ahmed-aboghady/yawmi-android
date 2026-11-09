package com.yawmi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    // WEEKLY tasks belong to the Friday-starting week; DAILY tasks belong to one date.
    val type: String,
    val weekStart: String,
    val dayKey: String = "",
    val isDone: Boolean = false
)
