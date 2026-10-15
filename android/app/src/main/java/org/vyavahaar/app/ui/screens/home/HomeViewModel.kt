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
import kotlinx.coroutines.launch
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.AllowlistedToolCallDto
import org.vyavahaar.app.data.remote.ApiClient
import org.vyavahaar.app.data.remote.VoiceCommandResponseDto
import org.vyavahaar.app.data.repository.AssistantRepository
import java.util.Locale

data class HomeUiState(
    val userName: String = "Sadasya",
    val preferredLanguage: String = "hi",
    val fontScale: Float = 1.2f,
    val isListening: Boolean = false,
    val recognizedTranscript: String = "",
    val assistantFeedback: String = "",
    val activeToolCall: AllowlistedToolCallDto? = null,
    val showConfirmationDialog: Boolean = false,
    val confirmationPrompt: String = "",
    val statusMessage: String = "Sabhi sevaayein taiyaar hain (All services ready)"
)

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
                tts?.language = Locale("hi", "IN")
            }
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VyavahaarTts")
    }

    fun startListening() {
        _uiState.value = _uiState.value.copy(
            isListening = true,
            recognizedTranscript = "",
            assistantFeedback = "Suniye, main sun raha hoon... (Listening...)",
            statusMessage = "Listening..."
        )
        speak("Ji kahiye, main sun raha hoon.")
    }

    fun stopListeningAndProcess(spokenText: String) {
        _uiState.value = _uiState.value.copy(
            isListening = false,
            recognizedTranscript = spokenText,
            statusMessage = "Anurodh samajh rahe hain... (Interpreting request...)"
        )

        viewModelScope.launch {
            val result = assistantRepo.processVoiceCommand(spokenText)
            result.onSuccess { response ->
                handleAssistantResponse(response)
            }.onFailure { error ->
                val fallbackMsg = "Kshama karein, network error aayi. Kripya punah prayas karein."
                _uiState.value = _uiState.value.copy(
                    assistantFeedback = fallbackMsg,
                    statusMessage = "Sampark nahi ho saka"
                )
                speak(fallbackMsg)
            }
        }
    }

    private fun handleAssistantResponse(resp: VoiceCommandResponseDto) {
        speak(resp.spokenFeedback)
        if (resp.toolCall != null && resp.toolCall.requiresConfirmation) {
            _uiState.value = _uiState.value.copy(
                assistantFeedback = resp.spokenFeedback,
                activeToolCall = resp.toolCall,
                showConfirmationDialog = true,
                confirmationPrompt = resp.toolCall.confirmationPrompt ?: resp.spokenFeedback
            )
        } else {
            _uiState.value = _uiState.value.copy(
                assistantFeedback = resp.spokenFeedback,
                activeToolCall = resp.toolCall,
                showConfirmationDialog = false
            )
        }
    }

    fun confirmConsequentialAction() {
        _uiState.value = _uiState.value.copy(showConfirmationDialog = false)
        val feedback = "Karya safaltapoorvak aage badha diya gaya hai."
        _uiState.value = _uiState.value.copy(assistantFeedback = feedback)
        speak(feedback)
    }

    fun dismissAssistant() {
        _uiState.value = _uiState.value.copy(
            isListening = false,
            activeToolCall = null,
            showConfirmationDialog = false,
            recognizedTranscript = "",
            assistantFeedback = ""
        )
    }

    fun triggerSos(onDirectCall: (Intent) -> Unit) {
        speak("112 emergency dialing shuru kar rahe hain.")
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:112")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        onDirectCall(callIntent)
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
