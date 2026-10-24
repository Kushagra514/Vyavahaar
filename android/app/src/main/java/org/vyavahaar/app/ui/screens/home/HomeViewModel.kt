package org.vyavahaar.app.ui.screens.home

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.AllowlistedToolCallDto
import org.vyavahaar.app.data.remote.ApiClient
import org.vyavahaar.app.data.remote.SosAlertRequestDto
import org.vyavahaar.app.data.remote.VoiceCommandResponseDto
import org.vyavahaar.app.data.repository.AssistantRepository
import java.util.Locale

enum class VoiceAssistantState {
    IDLE,
    LISTENING,
    TRANSCRIBING,
    INTERPRETING,
    AWAITING_CONFIRMATION,
    EXECUTING,
    RESPONDING,
    ERROR
}

data class HomeUiState(
    val userName: String = "Sadasya",
    val preferredLanguage: String = "hi",
    val fontScale: Float = 1.2f,
    val voiceState: VoiceAssistantState = VoiceAssistantState.IDLE,
    val recognizedTranscript: String = "",
    val assistantFeedback: String = "",
    val activeToolCall: AllowlistedToolCallDto? = null,
    val showConfirmationDialog: Boolean = false,
    val confirmationPrompt: String = "",
    val statusMessage: String = "Sabhi sevaayein taiyaar hain (All services ready)"
) {
    val isListening: Boolean get() = voiceState == VoiceAssistantState.LISTENING
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PreferencesDataStore(application)
    private val assistantRepo = AssistantRepository(ApiClient.apiService, preferences)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        initTts()
        viewModelScope.launch {
            preferences.userName.collect { name ->
                _uiState.value = _uiState.value.copy(userName = name)
            }
        }
        viewModelScope.launch {
            preferences.preferredLanguage.collect { lang ->
                _uiState.value = _uiState.value.copy(preferredLanguage = lang)
            }
        }
        viewModelScope.launch {
            preferences.fontScale.collect { scale ->
                _uiState.value = _uiState.value.copy(fontScale = scale)
            }
        }
    }

    private fun initTts() {
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Tiered Fallback: hi-IN -> en-IN -> en-US/ENGLISH
                val hiResult = tts?.setLanguage(Locale("hi", "IN"))
                if (hiResult == TextToSpeech.LANG_MISSING_DATA || hiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val enInResult = tts?.setLanguage(Locale("en", "IN"))
                    if (enInResult == TextToSpeech.LANG_MISSING_DATA || enInResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.language = Locale.ENGLISH
                    }
                }
            }
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VyavahaarTts")
    }

    fun startListening() {
        // Prevent acoustic feedback echo: stop TTS engine before opening mic
        tts?.stop()

        _uiState.value = _uiState.value.copy(
            voiceState = VoiceAssistantState.LISTENING,
            recognizedTranscript = "",
            assistantFeedback = "Suniye, main sun raha hoon... (Listening...)",
            statusMessage = "Listening..."
        )
    }

    fun stopListeningAndProcess(spokenText: String) {
        // Stop any active TTS
        tts?.stop()

        val trimmed = spokenText.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // If awaiting confirmation, process affirmative / negation responses directly
        if (_uiState.value.voiceState == VoiceAssistantState.AWAITING_CONFIRMATION || _uiState.value.showConfirmationDialog) {
            val affirmativeWords = listOf("haan", "ha", "yes", "theek hai", "confirm", "kar do", "aage badho", "ok")
            val negativeWords = listOf("nahi", "na", "no", "cancel", "mat karo", "rok do", "band karo")

            if (affirmativeWords.any { lower.contains(it) }) {
                confirmConsequentialAction()
                return
            } else if (negativeWords.any { lower.contains(it) }) {
                cancelConsequentialAction()
                return
            }
        }

        _uiState.value = _uiState.value.copy(
            voiceState = VoiceAssistantState.INTERPRETING,
            recognizedTranscript = trimmed,
            statusMessage = "Anurodh samajh rahe hain... (Interpreting request...)"
        )

        viewModelScope.launch {
            val result = assistantRepo.processVoiceCommand(trimmed)
            result.onSuccess { response ->
                handleAssistantResponse(response)
            }.onFailure { error ->
                val msg = error.message ?: ""
                if (msg.startsWith("NOT_AUTHENTICATED")) {
                    _uiState.value = _uiState.value.copy(
                        voiceState = VoiceAssistantState.ERROR,
                        assistantFeedback = "Please log in first to use voice commands. Open Login screen and use OTP 123456.",
                        statusMessage = "Login required"
                    )
                    speak("Please log in first to use voice commands.")
                } else {
                    val fallbackMsg = "Kshama karein, network error aayi. Kripya punah prayas karein."
                    _uiState.value = _uiState.value.copy(
                        voiceState = VoiceAssistantState.ERROR,
                        assistantFeedback = fallbackMsg,
                        statusMessage = "Sampark nahi ho saka"
                    )
                    speak(fallbackMsg)
                }
            }
        }
    }

    private fun handleAssistantResponse(resp: VoiceCommandResponseDto) {
        speak(resp.spokenFeedback)
        if (resp.toolCall != null && resp.toolCall.requiresConfirmation) {
            _uiState.value = _uiState.value.copy(
                voiceState = VoiceAssistantState.AWAITING_CONFIRMATION,
                assistantFeedback = resp.spokenFeedback,
                activeToolCall = resp.toolCall,
                showConfirmationDialog = true,
                confirmationPrompt = resp.toolCall.confirmationPrompt ?: resp.spokenFeedback,
                statusMessage = "Pushti ka intezaar hai (Confirmation required)"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                voiceState = VoiceAssistantState.RESPONDING,
                assistantFeedback = resp.spokenFeedback,
                activeToolCall = resp.toolCall,
                showConfirmationDialog = false,
                statusMessage = "Anurodh poora hua (Request completed)"
            )
        }
    }

    fun confirmConsequentialAction() {
        val feedback = "Karya safaltapoorvak aage badha diya gaya hai."
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceAssistantState.RESPONDING,
            showConfirmationDialog = false,
            assistantFeedback = feedback,
            statusMessage = "Pushti safal"
        )
        speak(feedback)
    }

    fun cancelConsequentialAction() {
        val feedback = "Theek hai, karya radd kar diya gaya hai."
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceAssistantState.IDLE,
            showConfirmationDialog = false,
            activeToolCall = null,
            assistantFeedback = feedback,
            statusMessage = "Radd kiya gaya"
        )
        speak(feedback)
    }

    fun onPermissionDenied() {
        val msg = "Aawaz suvidha ke liye microphone ki anumati avashyak hai."
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceAssistantState.ERROR,
            assistantFeedback = msg,
            statusMessage = "Microphone anumati asweekar"
        )
    }

    fun onSpeechSilence() {
        val msg = "Aapki aawaz nahi sunai di. Kripya punah prayas karein."
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceAssistantState.IDLE,
            assistantFeedback = msg,
            statusMessage = "Taiyaar"
        )
        speak(msg)
    }

    fun onSpeechError(error: String) {
        val msg = "Aawaz input mein samasya: $error"
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceAssistantState.ERROR,
            assistantFeedback = msg,
            statusMessage = "Aawaz asafal"
        )
    }

    fun dismissAssistant() {
        tts?.stop()
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceAssistantState.IDLE,
            activeToolCall = null,
            showConfirmationDialog = false,
            recognizedTranscript = "",
            assistantFeedback = "",
            statusMessage = "Sabhi sevaayein taiyaar hain"
        )
    }

    fun dismissToolCall() {
        _uiState.value = _uiState.value.copy(
            activeToolCall = null,
            showConfirmationDialog = false
        )
    }

    fun triggerSos(onDirectCall: (Intent) -> Unit) {
        tts?.stop()
        speak("112 emergency dialing shuru kar rahe hain.")
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:112")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        onDirectCall(callIntent)
    }

    fun triggerSosAlertBackend(isTestMode: Boolean, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = preferences.authToken.firstOrNull() ?: ""
                val resp = ApiClient.apiService.triggerSosAlert(
                    token = "Bearer $token",
                    req = SosAlertRequestDto(
                        isTestMode = isTestMode,
                        emergencyType = if (isTestMode) "DEMO_TEST_SOS" else "GENERAL_SOS",
                        locationName = "Bengaluru (Elder Residence)"
                    )
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val alert = resp.body()!!
                    val msg = if (alert.isTestMode) {
                        "Test Mode SOS Safal: Caregiver alert payload generated (${alert.notifiedContactsCount} contacts)."
                    } else {
                        "Emergency SOS Alert bheja gaya: 112 aur parivar ko soochit kiya gaya."
                    }
                    _uiState.value = _uiState.value.copy(assistantFeedback = msg, statusMessage = alert.status)
                    speak(msg)
                    onComplete(alert.alertMessagePayload)
                } else {
                    val errMsg = "SOS server sampark vifal raha. Direct 112 dial karein."
                    _uiState.value = _uiState.value.copy(assistantFeedback = errMsg)
                    speak(errMsg)
                    onComplete(errMsg)
                }
            } catch (e: Exception) {
                val errMsg = "SOS alert error: ${e.localizedMessage}. Direct 112 dial karein."
                _uiState.value = _uiState.value.copy(assistantFeedback = errMsg)
                speak(errMsg)
                onComplete(errMsg)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
