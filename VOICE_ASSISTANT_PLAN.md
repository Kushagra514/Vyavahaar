# Vyavahaar Voice Assistant & Intent Routing Implementation Plan

**Author**: Senior Solutions Architect & Android/AI Lead  
**Milestone**: [Voice Assistant & Intent Routing (Milestone #1)](https://github.com/Kushagra514/Vyavahaar/milestone/1)  
**Target Codebase**: `Kushagra514/Vyavahaar`  
**Execution Date**: 2026-10-21  

---

## 1. Executive Summary & Improvement Prioritization

Based on our Phase 0 audit and Phase 1 research, we prioritize the **Top 3 High-Impact Improvements** required to convert the voice assistant from a partial prototype into a safe, production-grade, hackathon-winning feature for elderly users:

```
+---------------------------------------------------------------------------------------------------+
| Top 3 High-Impact Improvements                                                                    |
+---------------------------------------------------------------------------------------------------+
| 1. Real Speech Recognition & Runtime Permissions (Issue A & G)                                    |
|    - Remove "Mujhe aaj ke satsang dikhao" hardcoded mock string.                                  |
|    - Dynamic Manifest.permission.RECORD_AUDIO request with accessible failure prompt.             |
|    - Native Android RecognizerIntent (hi-IN, en-IN) with Compose DisposableEffect lifecycle.      |
+---------------------------------------------------------------------------------------------------+
| 2. Safe Action-Confirmation State Machine & Consequential Execution (Issue D & B)                 |
|    - 8-stage VoiceAssistantState (IDLE, LISTENING, TRANSCRIBING, INTERPRETING,                     |
|      AWAITING_CONFIRMATION, EXECUTING, RESPONDING, ERROR).                                        |
|    - Two-step confirmation for sensitive tools (transport_book, sos_trigger).                     |
|    - Spoken confirmation prompt + accessible high-contrast touch actions ("Haan" / "Nahi").       |
+---------------------------------------------------------------------------------------------------+
| 3. Negation-Aware NLU Router & Multi-Locale TTS Fallback (Issue E, B & C)                         |
|    - Pre-filter negation phrases ("mat karo", "nahi", "don't", "cancel") before action match.     |
|    - Purge Kannada references and return polite Hindi/English refusal for unsupported requests.   |
|    - Prevent acoustic feedback echo (stop TTS before mic).                                       |
|    - TTS fallback hierarchy: Locale("hi", "IN") -> Locale("en", "IN") -> Locale.ENGLISH.         |
+---------------------------------------------------------------------------------------------------+
```

---

## 2. Detailed Technical Plan for Top 3 Improvements

### Improvement 1: Real Speech Recognition & Runtime Permissions
- **Affected Files**:
  - `android/app/src/main/java/com/vyavahaar/presentation/home/HomeScreen.kt`
  - `android/app/src/main/java/com/vyavahaar/presentation/home/HomeViewModel.kt`
- **Implementation Strategy**:
  1. Add `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` in `HomeScreen.kt`.
  2. If permission is granted, configure `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` with:
     - `EXTRA_LANGUAGE_MODEL`: `LANGUAGE_MODEL_FREE_FORM`
     - `EXTRA_LANGUAGE`: `"hi-IN"`
     - `EXTRA_ADDITIONAL_LANGUAGES`: `arrayOf("en-IN", "en-US")`
     - `EXTRA_PROMPT`: `"Boliye, main sun raha hoon..."`
  3. In `onResult`, extract `results.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)`:
     - If matches exist and non-empty, send text to `viewModel.processSpokenText(spokenText)`.
     - If user cancelled or silence occurred, transition gracefully to `VoiceAssistantState.IDLE` with a user-friendly visual and audio hint (*"Aapki aawaz nahi sunai di"*).
  4. **Eliminate hardcoded fallback**: Completely remove the `viewModel.stopListeningAndProcess("Mujhe aaj ke satsang dikhao")` fallback.

---

### Improvement 2: Safe Action-Confirmation State Machine
- **Affected Files**:
  - `android/app/src/main/java/com/vyavahaar/presentation/home/HomeViewModel.kt`
  - `android/app/src/main/java/com/vyavahaar/presentation/home/HomeScreen.kt`
- **State Machine Definition**:
  ```kotlin
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

  data class VoiceConfirmationContext(
      val intent: String,
      val spokenActionSummary: String,
      val payload: Map<String, Any?> = emptyMap()
  )
  ```
- **Consequential Tools Requiring Confirmation**:
  - `transport_book`: Elderly users must confirm destination and intent to avoid accidental cab dispatches.
  - `sos_trigger`: High-impact emergency alert sending SMS/call to family.
- **Workflow**:
  1. Intent router flags `requires_confirmation = true`.
  2. UI enters `AWAITING_CONFIRMATION`.
  3. TTS prompts: *"Kya aap sach mein cab book karna chahte hain? Haan ya Na bole."*
  4. User can say "Haan" / "Yes" or tap large green button $\rightarrow$ `EXECUTING`.
  5. User can say "Nahi" / "Cancel" or tap large red button $\rightarrow$ `IDLE` (Cancelled).

---

### Improvement 3: Negation-Aware NLU Router & Multi-Locale TTS Fallback
- **Affected Files**:
  - `backend/app/services/voice_assistant.py`
  - `backend/tests/test_voice_assistant.py`
  - `android/app/src/main/java/com/vyavahaar/presentation/home/HomeViewModel.kt`
- **Backend NLU Engine**:
  1. **Strict Language Scope**: Hindi, English, and Hinglish only.
  2. **Kannada Purge**: Remove all references to Kannada language parsing in `voice_assistant.py`. When Kannada is requested, return:
     - `intent: "unsupported_language"`
     - `response_text: "Kshama karein, awaz sahayata keval Hindi aur English mein uplabdh hai."`
  3. **Negation Evaluation First**:
     - Check for `negation_keywords = ["mat karo", "mat karna", "nahi", "no", "don't", "cancel", "ruk jao", "rehne do"]`.
     - If sentence contains negation + action keyword (e.g. *"Cab book mat karo"*), route to `intent: "cancel_action"` or abort confirmation, returning:
       *"Theek hai, cab book nahi ki gayi hai."*
  4. **TTS Acoustic Echo Prevention**:
     - In `HomeViewModel.startListening()`, immediately call `ttsEngine.stop()` to prevent the microphone from picking up the phone's own speaker.
  5. **TTS Fallback Chain**:
     - Check `tts.setLanguage(Locale("hi", "IN"))`.
     - If `LANG_MISSING_DATA` or `LANG_NOT_SUPPORTED`, fallback to `Locale("en", "IN")`, then `Locale.ENGLISH`.

---

## 3. Implementation Step-by-Step Schedule

1. **Step 1: Backend Refinement (`voice_assistant.py`)**:
   - Clean out Kannada logic.
   - Insert top-level negation parser.
   - Refine Hindi, English, and Hinglish keyword patterns for supported tools.
   - Add unit tests in `backend/tests/test_voice_assistant.py`.
2. **Step 2: Android ViewModel & State Machine (`HomeViewModel.kt`)**:
   - Define `VoiceAssistantState` and confirmation state variables.
   - Add TTS safety controls and language fallback logic.
   - Implement `processSpokenText(query)` with negation & confirmation handling.
3. **Step 3: Android UI & Permissions (`HomeScreen.kt`)**:
   - Wire `ActivityResultContracts.RequestPermission` for `RECORD_AUDIO`.
   - Remove hardcoded `"Mujhe aaj ke satsang dikhao"` string.
   - Render accessible confirmation banner when `AWAITING_CONFIRMATION`.
   - Display real-time state badge and captions for hard-of-hearing users.
4. **Step 4: Verification & Smoke Testing**:
   - Run backend test suite: `./scripts/run_backend_tests.sh`.
   - Run `./scripts/smoke_test.sh`.
   - Compile Android build: `./gradlew assembleDebug`.
5. **Step 5: Delivery & Artifacts**:
   - Update `VOICE_ASSISTANT_TEST_REPORT.md` and `VOICE_ASSISTANT_CHANGELOG.md`.
