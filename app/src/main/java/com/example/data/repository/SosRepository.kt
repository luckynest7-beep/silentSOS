package com.example.data.repository

import com.example.data.dao.CalcHistoryDao
import com.example.data.dao.ContactDao
import com.example.data.dao.EmergencyEventDao
import com.example.data.dao.SettingDao
import com.example.data.entity.CalcHistory
import com.example.data.entity.Contact
import com.example.data.entity.EmergencyEvent
import com.example.data.entity.Setting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SosRepository(
    private val contactDao: ContactDao,
    private val emergencyEventDao: EmergencyEventDao,
    private val settingDao: SettingDao,
    private val calcHistoryDao: CalcHistoryDao
) {
    // Contacts
    val allContactsFlow: Flow<List<Contact>> = contactDao.getAllContactsFlow()
    
    suspend fun getAllContacts(): List<Contact> = contactDao.getAllContacts()
    
    suspend fun insertContact(contact: Contact) = contactDao.insertContact(contact)
    
    suspend fun updateContact(contact: Contact) = contactDao.updateContact(contact)
    
    suspend fun deleteContact(contact: Contact) = contactDao.deleteContact(contact)
    
    suspend fun deleteContactById(id: Long) = contactDao.deleteContactById(id)

    // Emergency Events
    val allEventsFlow: Flow<List<EmergencyEvent>> = emergencyEventDao.getAllEventsFlow()
    val lastEventFlow: Flow<EmergencyEvent?> = emergencyEventDao.getLastEventFlow()

    suspend fun insertEvent(event: EmergencyEvent) = emergencyEventDao.insertEvent(event)
    
    suspend fun deleteEventById(id: Long) = emergencyEventDao.deleteEventById(id)
    
    suspend fun deleteAllEvents() = emergencyEventDao.deleteAllEvents()

    // Settings helpers
    val allSettingsFlow: Flow<List<Setting>> = settingDao.getAllSettingsFlow()

    suspend fun getSettingValue(key: String, defaultValue: String): String {
        val setting = settingDao.getSettingValue(key)
        if (setting == null) {
            // Save local default
            settingDao.insertSetting(Setting(key, defaultValue))
            return defaultValue
        }
        return setting.value
    }

    suspend fun updateSetting(key: String, value: String) {
        settingDao.insertSetting(Setting(key, value))
    }

    // Specific settings values with default fallback
    suspend fun getSosTriggerCode(): String = getSettingValue("SOS_TRIGGER_CODE", "2026#")
    suspend fun getSettingsCode(): String = getSettingValue("SETTINGS_CODE", "1397#")
    suspend fun getSafeStopPin(): String = getSettingValue("SAFE_STOP_PIN", "0000#")
    suspend fun getIsSafeModeEnabled(): Boolean = getSettingValue("IS_SAFE_MODE_ENABLED", "true").toBoolean()
    suspend fun getIsSelfieEnabled(): Boolean = getSettingValue("IS_SELFIE_ENABLED", "true").toBoolean()
    suspend fun getIsAudioEnabled(): Boolean = getSettingValue("IS_AUDIO_ENABLED", "true").toBoolean()

    // Calculator History
    val allHistoryFlow: Flow<List<CalcHistory>> = calcHistoryDao.getHistoryFlow()

    suspend fun insertHistory(item: CalcHistory) {
        calcHistoryDao.insertHistory(item)
    }

    suspend fun clearHistory() {
        calcHistoryDao.clearHistory()
    }
}
