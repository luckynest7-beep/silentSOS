package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings")
    fun getAllSettingsFlow(): Flow<List<Setting>>

    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): Setting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}
