package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.CalcHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CalcHistoryDao {
    @Query("SELECT * FROM calc_history ORDER BY timestamp DESC LIMIT 100")
    fun getHistoryFlow(): Flow<List<CalcHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CalcHistory)

    @Query("DELETE FROM calc_history")
    suspend fun clearHistory()
}
