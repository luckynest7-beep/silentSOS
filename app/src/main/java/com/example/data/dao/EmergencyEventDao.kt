package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.EmergencyEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyEventDao {
    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
    fun getAllEventsFlow(): Flow<List<EmergencyEvent>>

    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC LIMIT 1")
    fun getLastEventFlow(): Flow<EmergencyEvent?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EmergencyEvent): Long

    @Query("DELETE FROM emergency_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM emergency_events")
    suspend fun deleteAllEvents()
}
