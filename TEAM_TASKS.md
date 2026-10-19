# Vyavahaar (व्यवहार) — Team Workstream Allocations & Task Contracts

> **Document Type:** 5-Engineer Team Assignment, Interface Contracts & Merge Policy  
> **Sprint Goal:** Live Hackathon Judging Demonstration & Prototype Defense  
> **Integrator / Lead Architect:** Kushagra Tewari

---

## 1. Shared Files & Merge Conflict Mitigation Policy

The following files are touched across multiple features and represent potential merge conflict hot-spots:
* `android/app/src/main/java/org/vyavahaar/app/data/remote/VyavahaarApiService.kt` (API Interface)
* `android/app/src/main/java/org/vyavahaar/app/data/remote/ApiModels.kt` (Shared DTOs)
* `android/app/src/main/java/org/vyavahaar/app/navigation/NavGraph.kt` (Navigation routes)
* `backend/app/main.py` (FastAPI router mounts)
* `backend/tests/test_api.py` (Test suite)

### Merge Rules:
1. **Branching:** Work must proceed in feature branches named `feature/<feature-name>`.
2. **API Contract First:** DTO additions in `ApiModels.kt` and endpoint signatures in `VyavahaarApiService.kt` must be coordinated with the Lead Architect prior to merging.
3. **Automated Gate:** Every pull request must pass `./scripts/run_backend_tests.sh` and the GitHub Actions CI workflow before merging to `main`.

---

## 2. Workstream Breakdown

---

### Owner 1: Voice AI & Multilingual Intent Engineer
* **Feature Scope:** Real voice modality, audio speech-to-text, multilingual NLU parsing, spoken TTS feedback, and allowlisted tool dispatching.
* **Explicit Exclusions:** Screen layout design, payment gateway integration, direct hardware telephony dialing.
* **Key Files:**
  * `android/.../screens/home/HomeScreen.kt` (Microphone button & STT launcher)
  * `android/.../screens/home/HomeViewModel.kt` (Speech state & TTS synthesis)
  * `backend/app/services/voice_assistant.py` (NLU keyword/regex intent classifier)
  * `backend/app/api/v1/endpoints/assistant.py` (Voice command endpoint)
* **Dependencies & Shared Interfaces:**
  * Consumes `POST /api/v1/assistant/process-command` (`VoiceCommandRequestDto` $\rightarrow$ `VoiceCommandResponseDto`).
  * Emits tool calls: `search_events`, `request_ride`, `schedule_appointment`, `scan_prescription`, `start_call`, `change_accessibility_settings`, `send_sos_alert`.
* **Specific Implementation Tasks:**
  1. Maintain `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` with Hindi (`hi-IN`), Indian English (`en-IN`), and US English (`en-US`) language support.
  2. Ensure graceful fallback on emulators where Google Speech Services are unavailable.
  3. Ensure consequential actions (cab booking) require explicit two-step user confirmation before dispatching.
  4. Ensure spoken TTS audio plays in Hindi using `Locale("hi", "IN")`.
* **Acceptance Criteria & Test Demonstration:**
  * Tapping mic and speaking *"Mujhe satsang dikhao"* transcribes Hindi, triggers `search_events`, and navigates to the Events screen.
  * Speaking *"Cab book karo"* asks for verbal and visual confirmation before executing.
  * Verified in automated test: `test_voice_assistant_commands()`.

---

### Owner 2: UI/UX, Authentication & Reliability Engineer
* **Feature Scope:** Senior-first accessibility (typography, touch targets $\ge 56\,\text{dp}$, high contrast, font scaling), OTP login and session lifecycle, theme switching, and network error resilience.
* **Explicit Exclusions:** Medical algorithm design, taxi routing optimization.
* **Key Files:**
  * `android/.../screens/auth/LoginScreen.kt` & `OtpVerifyScreen.kt` (Phone + OTP authentication)
  * `android/.../screens/auth/OnboardingScreen.kt` (Language & font size selection)
  * `android/.../screens/settings/SettingsScreen.kt` (Preferences & logout)
  * `android/.../MainActivity.kt` (Startup lifecycle & auth state resolution)
  * `android/.../data/local/PreferencesDataStore.kt` (Token & profile storage)
  * `android/.../theme/Color.kt`, `Theme.kt`, `Type.kt` (Accessible palettes & styles)
* **Dependencies & Shared Interfaces:**
  * Consumes `POST /api/v1/auth/request-otp` and `POST /api/v1/auth/verify-otp`.
  * Manages `KEY_AUTH_TOKEN`, `KEY_IS_ONBOARDED`, `KEY_FONT_SCALE` in DataStore.
* **Specific Implementation Tasks:**
  1. Verify sandbox OTP flow (+919876543210 / 123456) with developer hints and validation.
  2. Guard against auth startup race conditions in `MainActivity.kt`.
  3. Ensure logout safely clears tokens and redirects to Login with zero backstack leaks.
  4. Provide clear user-facing toast/dialog feedback when backend is unreachable.
* **Acceptance Criteria & Test Demonstration:**
  * Fresh app install boots to Login $\rightarrow$ verifies OTP $\rightarrow$ completes Onboarding $\rightarrow$ lands on Home.
  * Reopening app while logged in lands straight on Home without login screen flicker.
  * Font slider dynamically scales UI typography up to $1.6\times$ without clipping.

---

### Owner 3: Emergency Assistance & SOS Systems Engineer
* **Feature Scope:** Highly visible SOS button, accidental-trigger protection, caregiver emergency alert dispatch, GPS coordinate sharing, and safe demonstration test mode.
* **Explicit Exclusions:** Real-world SMS billing gateways, physical ambulance vehicle dispatch.
* **Key Files:**
  * `android/.../screens/home/HomeScreen.kt` (SOS button & dialog)
  * `android/.../screens/home/HomeViewModel.kt` (SOS dispatch handler)
  * `backend/app/api/v1/endpoints/profile.py` (`POST /sos-alert` endpoint)
  * `backend/app/schemas/profile.py` (`SosAlertRequest`, `SosAlertResponse`)
  * `backend/app/models/emergency_contact.py` (Caregiver database entity)
* **Dependencies & Shared Interfaces:**
  * Interface: `POST /api/v1/profile/sos-alert` (`SosAlertRequestDto` $\rightarrow$ `SosAlertResponseDto`).
  * Telephony intent: `Intent.ACTION_DIAL` targeting `tel:112`.
* **Specific Implementation Tasks:**
  1. Implement accidental-trigger protection modal in `HomeScreen.kt` with distinct options: Direct 112 Call, Notify Contacts, and Safe Demo Mode.
  2. Implement backend `/sos-alert` endpoint retrieving registered emergency contacts and formatting emergency alert payloads with coordinates.
  3. Support `is_test_mode=True` to enable risk-free hackathon demonstrations without triggering live services or spamming contacts.
* **Acceptance Criteria & Test Demonstration:**
  * Tapping SOS opens confirmation modal.
  * Selecting "Safe Demo / Test Mode" returns `status="SIMULATED_TEST_DISPATCHED"`, displays location and contact summaries, and does not fire real calls.
  * Selecting "112 Direct Call" safely opens the Android phone dialer with `112` prefilled.
  * Verified in automated test: `test_sos_emergency_alert_safe_test_mode()`.

---

### Owner 4: Community Events & Hyperlocal Discovery Engineer
* **Feature Scope:** Hyperlocal community events (Satsang, Yoga, Morning Walk, Health Camps), verified organizer badges, idempotent RSVP registration, and RSVP cancellation.
* **Explicit Exclusions:** Ticketing payment gateways, complex seat allocation.
* **Key Files:**
  * `android/.../screens/events/EventsScreen.kt` (Events UI, chips, join/leave buttons)
  * `android/.../data/repository/EventsRepository.kt` (Repository layer)
  * `android/.../data/remote/VyavahaarApiService.kt` (Retrofit calls)
  * `backend/app/api/v1/endpoints/events.py` (Event listing, register, cancel endpoints)
  * `backend/app/models/event.py` (`Event`, `EventRegistration` models)
* **Dependencies & Shared Interfaces:**
  * `GET /api/v1/events/` (Optional `category` query param).
  * `POST /api/v1/events/{id}/register` (Idempotent registration).
  * `DELETE /api/v1/events/{id}/register` (RSVP cancellation).
* **Specific Implementation Tasks:**
  1. Display category filter chips with responsive loading and empty states.
  2. Implement bidirectional RSVP: "Shamil Ho (Join Event)" and "Karyakram Chhodein (Cancel RSVP)".
  3. Ensure RSVP status persists across screen navigations and app restarts.
  4. Connect voice discovery intent `search_events` to auto-filter by category (e.g. Satsang).
* **Acceptance Criteria & Test Demonstration:**
  * User can filter by "Satsang & Bhajan" $\rightarrow$ click "Shamil Ho" $\rightarrow$ status updates to "Registered".
  * User can click "Karyakram Chhodein" $\rightarrow$ registration is cancelled $\rightarrow$ event reverts to "Shamil Ho".
  * Verified in automated test: `test_events_discovery_and_registration()` and `test_event_rsvp_leave_cancellation()`.

---

### Owner 5: Prescription OCR & Janaushadhi Medicine Engineer
* **Feature Scope:** Camera-based prescription capture, image preview, entity extraction (drug names, strengths, formulations, instructions), confidence scoring, Janaushadhi generic alternative lookup, and medical safety disclaimers.
* **Explicit Exclusions:** Autonomous diagnosis, direct prescription dispensation.
* **Key Files:**
  * `android/.../screens/prescriptions/PrescriptionsScreen.kt` (Camera capture, OCR cards, alternatives)
  * `android/.../data/repository/PrescriptionRepository.kt` (Prescription network calls)
  * `backend/app/api/v1/endpoints/prescriptions.py` (`/scan` and `/medicines/search`)
  * `backend/app/services/medicine_catalog.py` (Indian essential medicines catalog)
  * `backend/app/models/prescription.py` (`PrescriptionRecord` model)
* **Dependencies & Shared Interfaces:**
  * `POST /api/v1/prescriptions/scan` (`PrescriptionScanDto` $\rightarrow$ `PrescriptionResponseDto`).
  * `GET /api/v1/prescriptions/medicines/search?q={query}` (`MedicineSearchDto`).
* **Specific Implementation Tasks:**
  1. Support both camera capture via `ActivityResultContracts.TakePicturePreview()` and 1-click sample demo button for emulator evaluation.
  2. Implement robust text and dosage entity extraction mapping to `MEDICINE_CATALOG`.
  3. Render extracted medicine items with confidence badges and a one-tap "Saste Vikalp Khojein" action.
  4. Ensure all responses include prominent medical safety disclaimers forbidding autonomous brand substitution without doctor approval.
* **Acceptance Criteria & Test Demonstration:**
  * User scans or taps "Sample Demo" $\rightarrow$ extracts Metformin, Amlodipine, Pan 40 with $\ge 85\%$ confidence.
  * Tapping "Saste Vikalp Khojein" looks up Janaushadhi generic alternatives (e.g. Janaushadhi Metformin 500 at ₹6.50 vs branded Glycomet at ₹28.50).
  * Prominent red medical warning card is clearly visible.
  * Verified in automated test: `test_prescription_ocr_and_persistent_history()`.
