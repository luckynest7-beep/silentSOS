package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CalcHistoryDao
import com.example.data.dao.ContactDao
import com.example.data.dao.EmergencyEventDao
import com.example.data.dao.SettingDao
import com.example.data.entity.CalcHistory
import com.example.data.entity.Contact
import com.example.data.entity.EmergencyEvent
import com.example.data.entity.Setting

@Database(
    entities = [Contact::class, EmergencyEvent::class, Setting::class, CalcHistory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun emergencyEventDao(): EmergencyEventDao
    abstract fun settingDao(): SettingDao
    abstract fun calcHistoryDao(): CalcHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "silent_sos_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
