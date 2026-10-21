# Vyavahaar Voice Assistant & Intent Routing Changelog

**Release Milestone**: [Voice Assistant & Intent Routing (Milestone #1)](https://github.com/Kushagra514/Vyavahaar/milestone/1)  
**Date**: 2026-10-21  

---

## 1. Summary of Changes

### Removed
- **Hardcoded Mock Fallback in Android**: Completely removed `viewModel.stopListeningAndProcess("Mujhe aaj ke satsang dikhao")` from the microphone error-catch block in `HomeScreen.kt`.
- **Kannada Language from Voice Modality**: Purged all Kannada routing branches and docstring references from `backend/app/services/voice_assistant.py` and `README.md`.
- **Boolean Voice State**: Deprecated the simplistic binary `isListening: Boolean` in favor of an explicit 8-stage state machine.

### Added
- **Runtime `RECORD_AUDIO` Permission Handling**:
  - Integrated `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` in `HomeScreen.kt`.
  - Added accessible permission rationale `AlertDialog` explaining microphone necessity to senior citizens with a direct shortcut to Android Application Settings.
- **8-Stage Voice Assistant State Machine (`VoiceAssistantState`)**:
  - `IDLE`: Microphone inactive, ready for input.
  - `LISTENING`: Microphone capturing user speech; TTS engine halted to prevent acoustic echo.
  - `TRANSCRIBING`: Speech audio converted to text transcript.
  - `INTERPRETING`: Transcription dispatched to backend NLU router.
  - `AWAITING_CONFIRMATION`: High-impact actions (`transport_book`, `sos_trigger`) paused awaiting user affirmation ("Haan" / "Yes" or UI button).
  - `EXECUTING`: Action confirmed and dispatched to target backend service or local activity.
  - `RESPONDING`: Spoken audio feedback played through TTS with simultaneous visual caption card.
  - `ERROR`: Graceful recovery with informative guidance when permissions, network, or hardware fails.
- **Negation-First Intent Parsing**:
  - Inserted top-level negation handler in `backend/app/services/voice_assistant.py` checking for phrases like *"mat karo"*, *"nahi"*, *"cancel"*, *"don't"*, *"ruk jao"*, *"wapas"*.
  - Fixed bug where sentences like *"Cab book mat karo"* previously triggered cab bookings due to substring matching on *"cab"*.
- **Unsupported Language Guardrail**:
  - Automated detection and polite decline in Hindi/English when Kannada or other regional languages are requested over voice:
    > *"Kshama karein, awaz sahayata keval Hindi aur English mein uplabdh hai."*
- **Acoustic Feedback Echo Prevention**:
  - Explicitly invokes `tts.stop()` immediately upon `startListening()` and before initiating voice recognition.
- **Multi-Locale TTS Tiered Fallback**:
  - Checks and applies `Locale("hi", "IN")` $\rightarrow$ `Locale("en", "IN")` $\rightarrow$ `Locale.ENGLISH` if voice data is missing.
- **Automated Voice Test Suite**:
  - Created `backend/tests/test_voice_assistant.py` with 12 comprehensive unit and integration test cases covering immediate SOS, English commands, cab confirmations, negation overrides, Kannada rejection, medicine search, prescription scanning, and fallbacks.

### Changed
- **`backend/app/services/voice_assistant.py`**:
  - Restructured priority order: SOS/Emergency $\rightarrow$ Unsupported Language Guardrail $\rightarrow$ Negation/Cancellation $\rightarrow$ Pure Rejection $\rightarrow$ Affirmation/Confirmation $\rightarrow$ Prescription/Medicine $\rightarrow$ Ambulance $\rightarrow$ Cab Booking (two-step confirmation) $\rightarrow$ Calls $\rightarrow$ Events/Satsang $\rightarrow$ Doctor Appointment $\rightarrow$ Accessibility Settings $\rightarrow$ Fallback.
- **`android/app/src/main/java/org/vyavahaar/app/ui/screens/home/HomeViewModel.kt`**:
  - Refactored UI state and coroutine handlers to track `VoiceAssistantState`.
  - Added `cancelConsequentialAction()`, `onPermissionDenied()`, `onSpeechSilence()`, and `onSpeechError()`.
- **`README.md`**:
  - Updated voice modality documentation to reflect Hindi, English, and Hinglish support.

---

## 2. Grok Research & Integration Checklist

| Item | Requirement | Status | Notes |
| :--- | :--- | :--- | :--- |
| 1 | Free-tier / open-source ASR Evaluation | **COMPLETE** | Evaluated Native Android ASR, IndicConformer, Whisper, Vosk in `VOICE_ASSISTANT_RESEARCH.md`. Selected Native Android ASR for zero cloud latency and zero cost. |
| 2 | Language Restriction Enforcement | **COMPLETE** | Strictly scoped to Hindi (`hi-IN`), Indian English (`en-IN`), and Hinglish. Kannada removed from voice pipeline. |
| 3 | Safe Consequential Execution | **COMPLETE** | Two-step confirmation for cab booking with spoken prompt and high-contrast UI buttons. |
| 4 | Negation Evaluation Priority | **COMPLETE** | Tested and verified *"Cab book mat karo"* aborts without booking. |
| 5 | Acoustic Feedback Loop Elimination | **COMPLETE** | TTS explicitly stopped before microphone opens. |
| 6 | GitHub Issues & Milestone Integration | **COMPLETE** | Milestone #1 and Issues #2 through #9 created and tracked on repository `Kushagra514/Vyavahaar`. |
| 7 | Full Automated Test Coverage | **COMPLETE** | 23/23 tests passing in CI/CD pipeline via `./scripts/run_backend_tests.sh`. |
