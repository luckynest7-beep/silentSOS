package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calc_history")
data class CalcHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
