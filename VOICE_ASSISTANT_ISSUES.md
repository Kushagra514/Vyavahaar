# Vyavahaar Voice Assistant GitHub Issues Inventory

**Milestone**: [Voice Assistant & Intent Routing (Milestone #1)](https://github.com/Kushagra514/Vyavahaar/milestone/1)  
**Repository**: [Kushagra514/Vyavahaar](https://github.com/Kushagra514/Vyavahaar)  
**Creation Date**: 2026-10-21  
**Scope**: Production-grade Hindi, English, and Hinglish speech recognition, intent routing, and safe action execution.

---

## Issue Inventory Summary Table

| Issue Letter | Issue ID | Issue Title | Priority | Labels | URL |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **A** | #2 | Replace mock microphone input with real speech recognition & permission handling | P0 (Critical) | `voice`, `ai`, `accessibility` | [View #2](https://github.com/Kushagra514/Vyavahaar/issues/2) |
| **B** | #3 | Connect transcription to backend intent router (Hindi/English/Hinglish) | P0 (Critical) | `voice`, `ai` | [View #3](https://github.com/Kushagra514/Vyavahaar/issues/3) |
| **C** | #4 | Implement reliable spoken responses (TTS lifecycle, hi-IN/en-IN fallback) | P1 (High) | `voice`, `accessibility` | [View #4](https://github.com/Kushagra514/Vyavahaar/issues/4) |
| **D** | #5 | Introduce safe action-confirmation state machine for consequential tools | P0 (Critical) | `voice`, `ai`, `accessibility` | [View #5](https://github.com/Kushagra514/Vyavahaar/issues/5) |
| **E** | #6 | Make voice intent routing robust against ambiguity, negation, and misinterpretation | P1 (High) | `voice`, `ai` | [View #6](https://github.com/Kushagra514/Vyavahaar/issues/6) |
| **F** | #7 | Add automated voice pipeline and regression tests | P1 (High) | `voice`, `ai` | [View #7](https://github.com/Kushagra514/Vyavahaar/issues/7) |
| **G** | #8 | Privacy, permissions, and resource lifecycle hardening | P2 (Medium) | `voice`, `accessibility` | [View #8](https://github.com/Kushagra514/Vyavahaar/issues/8) |
| **H** | #9 | Document setup, limitations, and reproducible voice demo | P2 (Medium) | `voice`, `ai`, `accessibility` | [View #9](https://github.com/Kushagra514/Vyavahaar/issues/9) |

---

## Detailed Issue Specifications

### Issue A (#2): Replace mock microphone input with real speech recognition & permission handling
- **URL**: https://github.com/Kushagra514/Vyavahaar/issues/2
- **Milestone**: Voice Assistant & Intent Routing
- **Problem**: Microphone button previously caught exceptions and fell back to hardcoded string `"Mujhe aaj ke satsang dikhao"`. Lack of runtime `RECORD_AUDIO` permission request caused immediate failures on Android 6.0+ devices.
- **Key Deliverables**:
  1. Dynamic `RECORD_AUDIO` permission launcher using `ActivityResultContracts.RequestPermission`.
  2. Native Android `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` with `LANGUAGE_MODEL_FREE_FORM` and language extras `hi-IN` and `en-IN`.
  3. Total removal of mock fallback strings.
  4. Accessible rationale dialog for permission denials.

### Issue B (#3): Connect transcription to backend intent router (Hindi/English/Hinglish)
- **URL**: https://github.com/Kushagra514/Vyavahaar/issues/3
- **Milestone**: Voice Assistant & Intent Routing
- **Problem**: Spoken transcripts must post to `/api/v1/voice/process-command` under authentic JWT tokens. The backend voice assistant previously contained Kannada docstrings and routing branches that violate the Hindi/English/Hinglish-only project constraint.
- **Key Deliverables**:
  1. Purge all Kannada handling and docstrings from `backend/app/services/voice_assistant.py`.
  2. Return polite refusal in Hindi/English if Kannada or unsupported language is requested.
  3. Support Hindi Devanagari, Indian English, and Hinglish transliterations.
  4. Restrict executed tools to the allowlist: `events_list`, `events_rsvp`, `transport_book`, `transport_status`, `medicine_search`, `call_caregiver`, `sos_trigger`.

### Issue C (#4): Implement reliable spoken responses (TTS lifecycle, hi-IN/en-IN fallback)
- **URL**: https://github.com/Kushagra514/Vyavahaar/issues/4
- **Milestone**: Voice Assistant & Intent Routing
- **Problem**: Unchecked TTS playback risks acoustic feedback into the microphone during speech recognition. Devices without Hindi TTS voice data fail silently.
- **Key Deliverables**:
  1. Stop TTS engine immediately before initiating speech listening.
  2. Implement fallback chain: `Locale("hi", "IN")` -> `Locale("en", "IN")` -> `Locale.ENGLISH`.
  3. Display simultaneous visual captions for hard-of-hearing elderly users.
  4. Unbind TTS on Composable disposal.

### Issue D (#5): Introduce safe action-confirmation state machine for consequential tools
- **URL**: https://github.com/Kushagra514/Vyavahaar/issues/5
- **Milestone**: Voice Assistant & Intent Routing
- **Problem**: Consequential actions like booking a cab (`transport_book`) or triggering emergency SOS (`sos_trigger`) must not fire immediately on speech recognition without user verification.
- **Key Deliverables**:
  1. Multi-state `VoiceAssistantState`: `IDLE`, `LISTENING`, `TRANSCRIBING`, `INTERPRETING`, `AWAITING_CONFIRMATION`, `EXECUTING`, `RESPONDING`, `ERROR`.
  2. Two-step confirmation dialog with spoken prompt and high-contrast touch buttons.
  3. Affirmation ("Haan", "Yes", "Confirm") vs Rejection ("Nahi", "No", "Cancel", "Ruko").

### Issue E (#6): Make voice intent routing robust against ambiguity, negation, and misinterpretation
- **URL**: https://github.com/Kushagra514/Vyavahaar/issues/6
- **Milestone**: Voice Assistant & Intent Routing
- **Problem**: Negated sentences like *"Cab book mat karo"* were incorrectly matched by action substrings ("cab", "book").
- **Key Deliverables**:
  1. Prioritize negation patterns before keyword and tool matching.
  2. Graceful fallback for ambiguous inputs with clarifying elderly-friendly guidance.
  3. Intent confidence thresholds to prevent false positives.

### Issue F (#7): Add automated voice pipeline and regression tests
- **URL**: https://github.com/Kushagra514/Vyavahaar/issues/7
- **Milestone**: Voice Assistant & Intent Routing
- **Problem**: Absence of dedicated regression tests for voice intent parsing across languages, negation, and token verification.
- **Key Deliverables**:
  1. Dedicated pytest suite in `backend/tests/test_voice_assistant.py` with $\ge 10$ test cases.
  2. Verify integration with `./scripts/run_backend_tests.sh`.
  3. Test report artifact documenting passing results.

### Issue G (#8): Privacy, permissions, and resource lifecycle hardening
- **URL**: https://github.com/Kushagra514/Vyavahaar/issues/8
- **Milestone**: Voice Assistant & Intent Routing
- **Problem**: Elderly privacy must be preserved; audio recordings must not be cached on disk or sent to unverified endpoints.
- **Key Deliverables**:
  1. Immediate resource release in Compose `DisposableEffect`.
  2. No disk storage or audio telemetry leaks.
  3. Token-authenticated API requests only.

### Issue H (#9): Document setup, limitations, and reproducible voice demo
- **URL**: https://github.com/Kushagra514/Vyavahaar/issues/9
- **Milestone**: Voice Assistant & Intent Routing
- **Problem**: Judges and contributors need verified instructions for testing speech input on physical devices, emulators, and cURL commands.
- **Key Deliverables**:
  1. Guide on configuring Android Emulator virtual microphone.
  2. Set of working cURL requests for test transcripts.
  3. Document offline vs online ASR limitations.
