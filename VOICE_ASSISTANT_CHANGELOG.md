# Vyavahaar Voice Assistant & Intent Routing Changelog

**Release Milestone**: [Voice Assistant & Intent Routing (Milestone #1)](https://github.com/Kushagra514/Vyavahaar/milestone/1)  
**Date**: 2026-10-21  

---

## 1. Summary of Changes

### Removed
- **Hardcoded Mock Fallback in Android**: Completely removed `viewModel.stopListeningAndProcess("Mujhe aaj ke satsang dikhao")` from the microphone error-catch block in `HomeScreen.kt`.
- **Kannada Language from Voice Modality**: Purged all Kannada routing branches and docstring references from `backend/app/services/voice_assistant.py`, `README.md`, and `DEMO_SCRIPT.md`.
- **Boolean Voice State**: Deprecated the simplistic binary `isListening: Boolean` in favor of an explicit 8-stage state machine.

### Added
- **Runtime `RECORD_AUDIO` Permission Handling**:
  - Integrated `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` in `HomeScreen.kt`.
  - Added accessible permission rationale `AlertDialog` explaining microphone necessity to senior citizens with a direct shortcut to Android Application Settings.
- **8-Stage Voice Assistant State Machine (`VoiceAssistantState`)**:
  - `IDLE`, `LISTENING`, `TRANSCRIBING`, `INTERPRETING`, `AWAITING_CONFIRMATION`, `EXECUTING`, `RESPONDING`, `ERROR`.
- **Negation-First Intent Parsing**:
  - Inserted top-level negation handler in `backend/app/services/voice_assistant.py` checking for phrases like *"mat karo"*, *"nahi"*, *"cancel"*, *"don't"*, *"ruk jao"*, *"wapas"*.
  - Fixed bug where sentences like *"Cab book mat karo"* previously triggered cab bookings due to substring matching on *"cab"*.
- **Generic Unsupported Language Guardrail**:
  - Automated detection and polite decline in Hindi/English when Kannada, Bengali, Tamil, etc., are requested over voice:
    > *"Kshama karein, awaz sahayata keval Hindi aur English mein uplabdh hai."*
- **Acoustic Feedback Echo Prevention**:
  - Explicitly invokes `tts.stop()` immediately upon `startListening()` and before initiating voice recognition.
- **Multi-Locale TTS Tiered Fallback**:
  - Checks and applies `Locale("hi", "IN")` $\rightarrow$ `Locale("en", "IN")` $\rightarrow$ `Locale.ENGLISH` if voice data is missing.
- **Android Launcher Icon Vector Assets**:
  - Added `ic_launcher_background.xml`, `ic_launcher_foreground.xml`, `ic_launcher.xml`, and `ic_launcher_round.xml` to resolve AAPT resource linking errors.
- **Automated Smoke Test Lifecycle**:
  - Updated `scripts/smoke_test.sh` to auto-detect and launch a local FastAPI server instance when none is running, resolving exit code 7.

### Fixed
- **Android Compilation Defects**:
  - Resolved missing Android SDK path by provisioning command-line tools into `/home/kushagra/Android/Sdk` and configuring `android/local.properties`.
  - Added missing `import androidx.compose.ui.unit.sp` in `OnboardingScreen.kt` and `OtpVerifyScreen.kt`.
  - Corrected `doctorName` reference in `PrescriptionsScreen.kt`.
  - Verified `./gradlew compileDebugKotlin` builds cleanly (`BUILD SUCCESSFUL in 12s`).
- **SOS Voice-to-Action Wiring**:
  - Bound `send_sos_alert` tool call to open the SOS protection dialog (`showSosDialog = true`) with prominent emergency button styling (`SosCrimson`) and label `"🚨 Aapatkaal SOS Kholein"`.
- **Microphone Tap Debouncing**:
  - Prevented concurrent or duplicate speech recognition sessions by ignoring microphone taps when already interpreting or awaiting confirmation.

---

## 2. Grok Research & Integration Checklist

| Item | Requirement | Status | Notes |
| :--- | :--- | :--- | :--- |
| 1 | Free-tier / open-source ASR Evaluation | **COMPLETE** | Evaluated Native Android ASR, IndicConformer, Whisper, Vosk in `VOICE_ASSISTANT_RESEARCH.md`. Selected Native Android ASR for zero cloud latency and zero cost. |
| 2 | Language Restriction Enforcement | **COMPLETE** | Strictly scoped to Hindi (`hi-IN`), Indian English (`en-IN`), and Hinglish. Kannada removed from voice pipeline; generic rejection tested for Kannada, Tamil, Bengali. |
| 3 | Safe Consequential Execution | **COMPLETE** | Two-step confirmation for cab booking with spoken prompt and high-contrast UI buttons. |
| 4 | Negation Evaluation Priority | **COMPLETE** | Tested and verified *"Cab book mat karo"* aborts without booking. |
| 5 | Acoustic Feedback Loop Elimination | **COMPLETE** | TTS explicitly stopped before microphone opens. |
| 6 | Android Build & Smoke Test Verification | **COMPLETE** | Android SDK configured, Kotlin compile passed, smoke test passed with auto-lifecycle. |
| 7 | Full Automated Test Coverage | **COMPLETE** | 25/25 tests passing in pytest suite; 9/9 passing in smoke test suite. |
