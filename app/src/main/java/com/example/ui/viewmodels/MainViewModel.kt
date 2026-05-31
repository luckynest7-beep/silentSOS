package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.CalcHistory
import com.example.data.entity.Contact
import com.example.data.entity.EmergencyEvent
import com.example.data.repository.SosRepository
import com.example.services.AudioService
import com.example.services.BatteryService
import com.example.services.CameraService
import com.example.services.ContactService
import com.example.services.LocationService
import com.example.services.SosService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Stack

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = SosRepository(
        contactDao = db.contactDao(),
        emergencyEventDao = db.emergencyEventDao(),
        settingDao = db.settingDao(),
        calcHistoryDao = db.calcHistoryDao()
    )

    // Services
    private val locationService = LocationService(application)
    private val batteryService = BatteryService(application)
    private val contactService = ContactService(application)
    private val cameraService = CameraService(application)
    private val audioService = AudioService(application)
    private val sosService = SosService(
        context = application,
        repository = repository,
        locationService = locationService,
        batteryService = batteryService,
        contactService = contactService,
        cameraService = cameraService,
        audioService = audioService
    )

    // Calculator State
    val calcInput = MutableStateFlow("")
    val calcResult = MutableStateFlow("0")
    val isDegreeMode = MutableStateFlow(true)
    val animateResultTrigger = MutableStateFlow(0)
    private var isOperatorJustAdded = false

    // App state
    val isSosActive = MutableStateFlow(false)
    val shouldNavigateToSettings = MutableStateFlow(false)

    // Reactive lists from Database
    val contactsList: StateFlow<List<Contact>> = repository.allContactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastEvent: StateFlow<EmergencyEvent?> = repository.lastEventFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allEventsList: StateFlow<List<EmergencyEvent>> = repository.allEventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calcHistoryList: StateFlow<List<CalcHistory>> = repository.allHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings memory cache for rapid execution
    val sosTriggerCode = MutableStateFlow("2026#")
    val settingsCode = MutableStateFlow("1397#")
    val safeStopPin = MutableStateFlow("0000#")
    val isSafeModeEnabled = MutableStateFlow(true)
    val isSelfieEnabled = MutableStateFlow(true)
    val isAudioEnabled = MutableStateFlow(true)

    init {
        loadSettingsFromDatabase()
    }

    private fun loadSettingsFromDatabase() {
        viewModelScope.launch {
            sosTriggerCode.value = repository.getSosTriggerCode()
            settingsCode.value = repository.getSettingsCode()
            safeStopPin.value = repository.getSafeStopPin()
            isSafeModeEnabled.value = repository.getIsSafeModeEnabled()
            isSelfieEnabled.value = repository.getIsSelfieEnabled()
            isAudioEnabled.value = repository.getIsAudioEnabled()
        }
    }

    // Refresh memory cache when database changes
    fun persistSetting(key: String, value: String) {
        val cleanValue = if (key in listOf("SOS_TRIGGER_CODE", "SETTINGS_CODE", "SAFE_STOP_PIN")) {
            val trimmed = value.trim()
            if (trimmed.endsWith("#")) trimmed else "$trimmed#"
        } else {
            value
        }
        viewModelScope.launch {
            repository.updateSetting(key, cleanValue)
            when (key) {
                "SOS_TRIGGER_CODE" -> sosTriggerCode.value = cleanValue
                "SETTINGS_CODE" -> settingsCode.value = cleanValue
                "SAFE_STOP_PIN" -> safeStopPin.value = cleanValue
                "IS_SAFE_MODE_ENABLED" -> isSafeModeEnabled.value = value.toBoolean()
                "IS_SELFIE_ENABLED" -> isSelfieEnabled.value = value.toBoolean()
                "IS_AUDIO_ENABLED" -> isAudioEnabled.value = value.toBoolean()
            }
        }
    }

    // Calculator operations logic
    fun setDegreeMode(isDeg: Boolean) {
        isDegreeMode.value = isDeg
        evaluateCalculator(isFromUserTap = false)
    }

    fun onCalcPress(char: String, lifecycleOwner: LifecycleOwner) {
        val currentStr = calcInput.value
        
        when (char) {
            "C" -> {
                calcInput.value = ""
                calcResult.value = "0"
                isOperatorJustAdded = false
            }
            "DEL" -> {
                if (currentStr.isNotEmpty()) {
                    calcInput.value = currentStr.dropLast(1)
                }
            }
            "=" -> {
                // Intercept secret codes!
                val cleanCode = currentStr.trim()
                
                if (cleanCode == sosTriggerCode.value) {
                    // SILENT SOS TRIGGERED!
                    triggerSilentSos(lifecycleOwner)
                    calcInput.value = ""
                    calcResult.value = ""
                    return
                }
                
                if (cleanCode == settingsCode.value) {
                    // NAVIGATE TO HIDDEN SETTINGS!
                    vibrateBriefly(100)
                    shouldNavigateToSettings.value = true
                    calcInput.value = ""
                    calcResult.value = "0"
                    return
                }

                if (isSosActive.value && cleanCode == safeStopPin.value) {
                    // STOP SOS/SAFE TRIGGERS!
                    stopSilentSos()
                    calcInput.value = ""
                    calcResult.value = "Safe"
                    return
                }

                // Normal math evaluation
                evaluateCalculator(isFromUserTap = true)
            }
            "+", "-", "×", "÷" -> {
                if (currentStr.isNotEmpty()) {
                    val lastChar = currentStr.last().toString()
                    if (lastChar == "+" || lastChar == "-" || lastChar == "×" || lastChar == "÷") {
                        calcInput.value = currentStr.dropLast(1) + char
                    } else {
                        calcInput.value = currentStr + char
                    }
                    isOperatorJustAdded = true
                }
            }
            else -> {
                calcInput.value = currentStr + char
                isOperatorJustAdded = false
            }
        }
    }

    private fun evaluateCalculator(isFromUserTap: Boolean = false) {
        val expression = calcInput.value
        if (expression.isEmpty()) return
        
        try {
            val sanitized = expression.replace("×", "*").replace("÷", "/").replace("#", "")
            val resultValue = evaluateMathExpression(sanitized)
            
            // Format result output elegantly
            val resultStr = if (resultValue.isNaN()) {
                "Error"
            } else if (resultValue.isInfinite()) {
                "Infinity"
            } else if (resultValue % 1.0 == 0.0) {
                if (resultValue >= Long.MAX_VALUE || resultValue <= Long.MIN_VALUE) {
                    String.format(java.util.Locale.US, "%.4e", resultValue)
                } else {
                    resultValue.toLong().toString()
                }
            } else {
                String.format(java.util.Locale.US, "%.8f", resultValue).trimEnd('0').trimEnd('.')
            }
            
            calcResult.value = resultStr
            
            if (isFromUserTap) {
                animateResultTrigger.value = animateResultTrigger.value + 1
                // Save to database local history
                viewModelScope.launch {
                    repository.insertHistory(
                        CalcHistory(expression = expression, result = resultStr)
                    )
                }
            }
        } catch (e: Exception) {
            calcResult.value = "Error"
        }
    }

    // A robust, highly general recursive-descent mathematical expression parser
    private fun evaluateMathExpression(expr: String): Double {
        return try {
            val cleaned = expr.lowercase()
                .replace("π", "3.141592653589793")
                .replace("e", "2.718281828459045")
                .replace("√", "sqrt")
                .replace("∛", "cbrt")
            
            val parser = object {
                var pos = -1
                var ch = ' '

                fun nextChar() {
                    ch = if (++pos < cleaned.length) cleaned[pos] else '\u0000'
                }

                fun eat(charToEat: Char): Boolean {
                    while (ch == ' ') nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < cleaned.length) return Double.NaN
                    return x
                }

                // expression = term | expression `+` term | expression `-` term
                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        if (eat('+')) x += parseTerm()
                        else if (eat('-')) x -= parseTerm()
                        else return x
                    }
                }

                // term = factor | term `*` factor | term `/` factor
                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        if (eat('*')) x *= parseFactor()
                        else if (eat('/')) {
                            val divisor = parseFactor()
                            x = if (divisor == 0.0) Double.NaN else x / divisor
                        } else return x
                    }
                }

                // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor
                fun parseFactor(): Double {
                    if (eat('+')) return parseFactor()
                    if (eat('-')) return -parseFactor()

                    var x: Double
                    val startPos = this.pos
                    if (eat('(')) {
                        x = parseExpression()
                        eat(')')
                    } else if (ch in '0'..'9' || ch == '.') {
                        while (ch in '0'..'9' || ch == '.') nextChar()
                        val numStr = cleaned.substring(startPos, this.pos)
                        x = numStr.toDoubleOrNull() ?: 0.0
                    } else if (ch in 'a'..'z') {
                        while (ch in 'a'..'z') nextChar()
                        val func = cleaned.substring(startPos, this.pos)
                        val argument = parseFactor()
                        val isDeg = isDegreeMode.value
                        x = when (func) {
                            "sqrt" -> java.lang.Math.sqrt(argument)
                            "cbrt" -> java.lang.Math.cbrt(argument)
                            "sin" -> if (isDeg) java.lang.Math.sin(java.lang.Math.toRadians(argument)) else java.lang.Math.sin(argument)
                            "cos" -> if (isDeg) java.lang.Math.cos(java.lang.Math.toRadians(argument)) else java.lang.Math.cos(argument)
                            "tan" -> if (isDeg) java.lang.Math.tan(java.lang.Math.toRadians(argument)) else java.lang.Math.tan(argument)
                            "asin" -> {
                                val rad = java.lang.Math.asin(argument)
                                if (isDeg) java.lang.Math.toDegrees(rad) else rad
                            }
                            "acos" -> {
                                val rad = java.lang.Math.acos(argument)
                                if (isDeg) java.lang.Math.toDegrees(rad) else rad
                            }
                            "atan" -> {
                                val rad = java.lang.Math.atan(argument)
                                if (isDeg) java.lang.Math.toDegrees(rad) else rad
                            }
                            "log" -> java.lang.Math.log10(argument)
                            "ln" -> java.lang.Math.log(argument)
                            else -> 0.0
                        }
                    } else {
                        return Double.NaN
                    }

                    // Handle postfix operations: power '^', factorial '!', percent '%'
                    while (true) {
                        if (eat('^')) {
                            x = java.lang.Math.pow(x, parseFactor())
                        } else if (eat('%')) {
                            x /= 100.0
                        } else if (eat('!')) {
                            x = factorial(x)
                        } else {
                            break
                        }
                    }

                    return x
                }

                fun factorial(n: Double): Double {
                    val intVal = n.toInt()
                    if (intVal < 0 || intVal > 170) return Double.NaN // Prevent overflow
                    var result = 1.0
                    for (i in 1..intVal) {
                        result *= i
                    }
                    return result
                }
            }
            parser.parse()
        } catch (e: Exception) {
            Double.NaN
        }
    }

    // Emergency control methods
    private fun triggerSilentSos(lifecycleOwner: LifecycleOwner) {
        vibrateBriefly(150)
        isSosActive.value = true
        Log.d("MainViewModel", "SILENT SOS ACTIVE! Triggering background services...")
        sosService.triggerSos(lifecycleOwner, viewModelScope)
    }

    private fun stopSilentSos() {
        vibrateBriefly(300)
        isSosActive.value = false
        Log.d("MainViewModel", "SILENT SOS DEACTIVATED! Triggering Safe Dispatch...")
        sosService.stopSos(viewModelScope) { sent ->
            Log.d("MainViewModel", "Stop SOS Dispatch finish status: $sent")
        }
    }

    private fun vibrateBriefly(durationMs: Long) {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            // Ignore on devices without vibrators or standard emulator limitations
        }
    }

    // Contacts management helpers
    fun addContact(name: String, number: String) {
        viewModelScope.launch {
            repository.insertContact(Contact(name = name, phoneNumber = number))
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    // Evidence clear helpers
    fun clearEvidenceLogs() {
        viewModelScope.launch {
            // Delete actual files to preserve device memory!
            allEventsList.value.forEach { event ->
                event.selfieFilePath?.let { File(it).delete() }
                event.backCameraFilePath?.let { File(it).delete() }
                event.audioFilePath?.let { File(it).delete() }
            }
            repository.deleteAllEvents()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun restoreHistoryEntry(expression: String, result: String) {
        calcInput.value = expression
        calcResult.value = result
    }

    override fun onCleared() {
        super.onCleared()
        cameraService.shutdown()
    }
}
