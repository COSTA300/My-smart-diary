package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.ai.CompanionEngine
import com.example.ai.CompanionPersonality
import com.example.data.*
import com.example.security.CryptoManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class PasscodeState {
    NOT_SET,
    LOCKED,
    UNLOCKED,
    DECOY_UNLOCKED
}

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("diary_security_prefs", Context.MODE_PRIVATE)

    // DB & Repository init
    private val database: DiaryDatabase by lazy {
        Room.databaseBuilder(
            application.applicationContext,
            DiaryDatabase::class.java,
            "dear_diary_database"
        )
        .fallbackToDestructiveMigration() // ensures safety across edits
        .build()
    }

    val repository: DiaryRepository by lazy {
        DiaryRepository(database.diaryDao())
    }

    // Security States
    private val _locksState = MutableStateFlow(PasscodeState.LOCKED)
    val locksState: StateFlow<PasscodeState> = _locksState.asStateFlow()

    private val _isDecoySession = MutableStateFlow(false)
    val isDecoySession: StateFlow<Boolean> = _isDecoySession.asStateFlow()

    private val _pinSetupError = MutableStateFlow<String?>(null)
    val pinSetupError: StateFlow<String?> = _pinSetupError.asStateFlow()

    private val _unlockError = MutableStateFlow<Boolean>(false)
    val unlockError: StateFlow<Boolean> = _unlockError.asStateFlow()

    // Form inputs & selected options
    val inputMood = MutableStateFlow("Peaceful")
    val inputThoughts = MutableStateFlow("")
    val selectedCompanion = MutableStateFlow(CompanionPersonality.MAYA)
    val activeTheme = MutableStateFlow("SAGE_CALM") // Themes: SAGE_CALM, MIDNIGHT_VELVET, EMBER_GLOW, AMBER_ROSE

    val aiConversationEnabled = MutableStateFlow(sharedPrefs.getBoolean("ai_conversation_enabled", true))

    fun setAiConversationEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("ai_conversation_enabled", enabled).apply()
        aiConversationEnabled.value = enabled
    }

    val customApiKey = MutableStateFlow(sharedPrefs.getString("custom_gemini_api_key", "") ?: "")

    val isGeminiConfigured: StateFlow<Boolean> = customApiKey.map { customKey ->
        (customKey.isNotBlank() && customKey != "placeholder" && customKey != "MY_GEMINI_API_KEY") || 
        (com.example.BuildConfig.GEMINI_API_KEY.isNotEmpty() && com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" && com.example.BuildConfig.GEMINI_API_KEY != "placeholder")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setCustomApiKey(key: String) {
        sharedPrefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
        customApiKey.value = key.trim()
    }

    fun getEffectiveApiKey(): String {
        val customKey = customApiKey.value
        if (customKey.isNotBlank() && customKey != "placeholder" && customKey != "MY_GEMINI_API_KEY") {
            return customKey
        }
        return com.example.BuildConfig.GEMINI_API_KEY
    }

    // Database Reactive Flows (entries depend on unlocked or decoy status)
    private val _allEntries = MutableStateFlow<List<DecryptedDiaryEntry>>(emptyList())
    val allEntries: StateFlow<List<DecryptedDiaryEntry>> = _allEntries.asStateFlow()

    private val _selectedEntry = MutableStateFlow<DecryptedDiaryEntry?>(null)
    val selectedEntry: StateFlow<DecryptedDiaryEntry?> = _selectedEntry.asStateFlow()

    private val _activeChatMessages = MutableStateFlow<List<DecryptedChatMessage>>(emptyList())
    val activeChatMessages: StateFlow<List<DecryptedChatMessage>> = _activeChatMessages.asStateFlow()

    private var observeEntriesJob: kotlinx.coroutines.Job? = null
    private var activeChatMessagesJob: kotlinx.coroutines.Job? = null

    init {
        // Evaluate initial locking state
        val isSetup = sharedPrefs.getBoolean("primary_pin_installed", false)
        if (!isSetup) {
            _locksState.value = PasscodeState.NOT_SET
        } else {
            _locksState.value = PasscodeState.LOCKED
        }
    }

    fun selectTheme(themeId: String) {
        activeTheme.value = themeId
    }

    /**
     * Set up primary PIN and optional decoy PIN.
     */
    fun setupPasscodes(primaryPin: String, decoyPin: String?, onComplete: () -> Unit) {
        if (primaryPin.length < 4) {
            _pinSetupError.value = "Passcode must be at least 4 digits"
            return
        }
        if (decoyPin != null && decoyPin == primaryPin) {
            _pinSetupError.value = "Decoy passcode cannot be identical to the primary"
            return
        }

        try {
            // 1. Derive & encrypt validation message for primary PIN
            val primaryKey = CryptoManager.deriveKey(primaryPin)
            val primaryValidation = CryptoManager.encrypt("VALID_PRIMARY", primaryKey)

            val editor = sharedPrefs.edit()
            editor.putBoolean("primary_pin_installed", true)
            editor.putString("primary_validation", primaryValidation)

            // 2. Setup decoy if provided
            if (!decoyPin.isNullOrBlank()) {
                val decoyKey = CryptoManager.deriveKey(decoyPin)
                val decoyValidation = CryptoManager.encrypt("VALID_DECOY", decoyKey)
                editor.putBoolean("decoy_pin_installed", true)
                editor.putString("decoy_validation", decoyValidation)
            } else {
                editor.putBoolean("decoy_pin_installed", false)
                editor.remove("decoy_validation")
            }
            editor.apply()

            // 3. Set the active key to primary and unlock directly
            repository.setKey(primaryKey)
            _isDecoySession.value = false
            _locksState.value = PasscodeState.UNLOCKED
            _pinSetupError.value = null
            
            // Sync database entries
            observeEntries()
            onComplete()
        } catch (e: Exception) {
            _pinSetupError.value = "Encryption Setup Error: ${e.localizedMessage}"
        }
    }

    /**
     * Unlock the vault with the PIN.
     */
    fun unlockWithPin(pin: String): Boolean {
        _unlockError.value = false
        if (pin.length < 4) return false

        try {
            val testKey = CryptoManager.deriveKey(pin)

            // Web search / Local decryption verification of the primary PIN
            val primaryValidationStr = sharedPrefs.getString("primary_validation", "") ?: ""
            if (primaryValidationStr.isNotEmpty()) {
                val decrypted = CryptoManager.decrypt(primaryValidationStr, testKey)
                if (decrypted == "VALID_PRIMARY") {
                    repository.setKey(testKey)
                    _isDecoySession.value = false
                    _locksState.value = PasscodeState.UNLOCKED
                    observeEntries()
                    return true
                }
            }

            // If not primary, check decoy PIN if installed
            val isDecoySetup = sharedPrefs.getBoolean("decoy_pin_installed", false)
            if (isDecoySetup) {
                val decoyValidationStr = sharedPrefs.getString("decoy_validation", "") ?: ""
                if (decoyValidationStr.isNotEmpty()) {
                    val decryptedDecoy = CryptoManager.decrypt(decoyValidationStr, testKey)
                    if (decryptedDecoy == "VALID_DECOY") {
                        repository.setKey(testKey)
                        _isDecoySession.value = true
                        _locksState.value = PasscodeState.DECOY_UNLOCKED
                        observeEntries()
                        return true
                    }
                }
            }

            _unlockError.value = true
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            _unlockError.value = true
            return false
        }
    }

    fun lockDiary() {
        repository.clearKey()
        observeEntriesJob?.cancel()
        observeEntriesJob = null
        activeChatMessagesJob?.cancel()
        activeChatMessagesJob = null
        
        _selectedEntry.value = null
        _allEntries.value = emptyList()
        _activeChatMessages.value = emptyList()
        _locksState.value = PasscodeState.LOCKED
    }

    private fun observeEntries() {
        observeEntriesJob?.cancel()
        // Collect reactive entries flow based on decoy status
        observeEntriesJob = viewModelScope.launch {
            repository.getDecryptedEntries(isDecoy = _isDecoySession.value).collect { list ->
                _allEntries.value = list
                // Update selected entry object if it's currently selected
                val currentSelected = _selectedEntry.value
                if (currentSelected != null) {
                    val updated = list.firstOrNull { it.id == currentSelected.id }
                    _selectedEntry.value = updated
                }
            }
        }
    }

    fun selectEntry(entry: DecryptedDiaryEntry?) {
        _selectedEntry.value = entry
        _activeChatMessages.value = emptyList()
        activeChatMessagesJob?.cancel()
        if (entry != null) {
            activeChatMessagesJob = viewModelScope.launch {
                repository.getDecryptedMessagesForEntry(entry.id).collect { messages ->
                    _activeChatMessages.value = messages
                }
            }
        }
    }

    /**
     * Save the initial diary flow state and trigger AI response immediately.
     */
    fun saveEntryAndRespond() {
        val thoughtsText = inputThoughts.value
        if (thoughtsText.isBlank()) return

        val dateStr = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date())
        val moodText = inputMood.value
        val companion = selectedCompanion.value
        val isDecoy = _isDecoySession.value

        viewModelScope.launch {
            // 1. Create entry
            val newEntry = DecryptedDiaryEntry(
                dateStr = dateStr,
                timestamp = System.currentTimeMillis(),
                mood = moodText,
                moodColor = getMoodHexIndex(moodText),
                companionName = companion.displayName,
                title = "Reflection on $moodText",
                mainText = thoughtsText,
                isDecoy = isDecoy
            )
            val entryId = repository.insertDecryptedEntry(newEntry)

            // Clear inputs
            inputThoughts.value = ""

            // 2. Generate and insert AI companion response
            if (aiConversationEnabled.value) {
                val apiKeyToUse = getEffectiveApiKey()
                val companionMsgResult = com.example.ai.GeminiClient.getAIResponse(
                    userInput = thoughtsText,
                    mood = moodText,
                    personality = companion,
                    history = emptyList(),
                    apiKeyOverride = apiKeyToUse
                )

                val companionMsg = when (companionMsgResult) {
                    is com.example.ai.GeminiResult.Success -> companionMsgResult.responseText
                    is com.example.ai.GeminiResult.Error -> {
                        "[Connection Notice: Live companion offline (${companionMsgResult.exceptionMessage}). Fallback sequence loaded]\n\n" +
                        CompanionEngine.generateResponse(thoughtsText, moodText, companion)
                    }
                    is com.example.ai.GeminiResult.KeyNotConfigured -> {
                        CompanionEngine.generateResponse(thoughtsText, moodText, companion)
                    }
                }
                
                val companionChatMsg = DecryptedChatMessage(
                    entryId = entryId,
                    sender = "companion",
                    text = companionMsg,
                    timestamp = System.currentTimeMillis() + 100 // slight delay for logs sorting
                )
                repository.insertDecryptedMessage(companionChatMsg)
            }

            // Automatically select today's entry to view the interactive chat board directly
            val insertedEntry = newEntry.copy(id = entryId)
            selectEntry(insertedEntry)
        }
    }

    /**
     * Insert a user chat reply and trigger companion reply within the active thread.
     */
    fun sendChatMessage(replyText: String) {
        val entry = _selectedEntry.value ?: return
        if (replyText.isBlank()) return

        viewModelScope.launch {
            // 1. Save User Message
            val userMsg = DecryptedChatMessage(
                entryId = entry.id,
                sender = "user",
                text = replyText,
                timestamp = System.currentTimeMillis()
            )
            repository.insertDecryptedMessage(userMsg)

            // 2. Generate Companion Response (takes active user input in companion personality context)
            val personality = CompanionPersonality.values().firstOrNull { it.displayName == entry.companionName } 
                ?: CompanionPersonality.MAYA
                
            val apiKeyToUse = getEffectiveApiKey()
            val replyMsgResult = com.example.ai.GeminiClient.getAIResponse(
                userInput = replyText,
                mood = entry.mood,
                personality = personality,
                history = _activeChatMessages.value,
                entryText = entry.mainText,
                apiKeyOverride = apiKeyToUse
            )

            val replyMsg = when (replyMsgResult) {
                is com.example.ai.GeminiResult.Success -> replyMsgResult.responseText
                is com.example.ai.GeminiResult.Error -> {
                    "[Connection Notice: Live companion offline (${replyMsgResult.exceptionMessage}). Fallback sequence loaded]\n\n" +
                    CompanionEngine.generateResponse(replyText, entry.mood, personality)
                }
                is com.example.ai.GeminiResult.KeyNotConfigured -> {
                    CompanionEngine.generateResponse(replyText, entry.mood, personality)
                }
            }

            val companionMsg = DecryptedChatMessage(
                entryId = entry.id,
                sender = "companion",
                text = replyMsg,
                timestamp = System.currentTimeMillis() + 150
            )
            repository.insertDecryptedMessage(companionMsg)
        }
    }

    fun startAiConversationForActiveEntry() {
        val entry = _selectedEntry.value ?: return
        viewModelScope.launch {
            val apiKeyToUse = getEffectiveApiKey()
            val companion = CompanionPersonality.values().firstOrNull { it.displayName == entry.companionName } 
                ?: CompanionPersonality.MAYA
                
            val companionMsgResult = com.example.ai.GeminiClient.getAIResponse(
                userInput = entry.mainText,
                mood = entry.mood,
                personality = companion,
                history = emptyList(),
                apiKeyOverride = apiKeyToUse
            )

            val companionMsg = when (companionMsgResult) {
                is com.example.ai.GeminiResult.Success -> companionMsgResult.responseText
                is com.example.ai.GeminiResult.Error -> {
                    "[Connection Notice: Live companion offline (${companionMsgResult.exceptionMessage}). Fallback sequence loaded]\n\n" +
                    CompanionEngine.generateResponse(entry.mainText, entry.mood, companion)
                }
                is com.example.ai.GeminiResult.KeyNotConfigured -> {
                    CompanionEngine.generateResponse(entry.mainText, entry.mood, companion)
                }
            }
            
            val companionChatMsg = DecryptedChatMessage(
                entryId = entry.id,
                sender = "companion",
                text = companionMsg,
                timestamp = System.currentTimeMillis() + 100
            )
            repository.insertDecryptedMessage(companionChatMsg)
        }
    }

    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
            if (_selectedEntry.value?.id == entryId) {
                _selectedEntry.value = null
                _activeChatMessages.value = emptyList()
            }
        }
    }

    private fun getMoodHexIndex(mood: String): Int {
        return when (mood) {
            "Peaceful" -> 0
            "Heavy" -> 1
            "Anxious" -> 2
            "Lonely" -> 3
            "Hopeful" -> 4
            "Secretive" -> 5
            else -> 0
        }
    }
}
