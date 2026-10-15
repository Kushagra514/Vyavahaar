# Vyavahaar (व्यवहार) — Project Status & Progress Tracker

> **Source of Truth for Team Development, Verification State, and Technical Debt.**  
> *Last Updated: October 15, 2026*  
> *Repository Branch: Main Workspace*

---

## 1. Overall Project Health & Executive Status

| Dimension | Status | Verified Date | Summary & Evidence |
| :--- | :--- | :--- | :--- |
| **Development Stage** | `ALPHA / PROTOTYPE` | 2026-10-15 | Functional FastAPI backend with passing test suite; native Compose UI structured but unverified on hardware. |
| **Backend API Startup** | `VERIFIED` | 2026-10-15 | Verified via Uvicorn on `127.0.0.1:8000`. Health endpoint returns `{"status":"healthy"}` (HTTP 200). |
| **Backend Automated Tests** | `VERIFIED` | 2026-10-15 | 6/6 test cases passing in `test_api.py` via `scripts/run_backend_tests.sh` in 0.93s. |
| **Android CLI Build** | `BLOCKED` | 2026-10-15 | Host runs JDK 25 (`25.0.4.1`). Gradle 8.10.2 Kotlin DSL compiler fails (`IllegalArgumentException: 25.0.4.1`). Requires JDK 17. |
| **Android App Launch** | `IMPLEMENTED - UNVERIFIED` | 2026-10-15 | Code structure and Compose screens complete, but runtime emulator/device execution pending JDK 17 and Android SDK 35 setup. |
| **Android-to-Backend Network** | `IMPLEMENTED - UNVERIFIED` | 2026-10-15 | `ApiClient.kt` configured for `http://10.0.2.2:8000/`; verified via backend mock tests, unverified on active emulator. |
| **Authentication Flow** | `VERIFIED` (Backend) | 2026-10-15 | Dev sandbox OTP (`123456`) and phone verification verified on live API via `scripts/smoke_test.sh`. |
| **Voice AI Intent Pipeline** | `VERIFIED` (Backend NLU) | 2026-10-15 | Multilingual regex/keyword intent classification verified for SOS, Satsang, Cab, and Font scaling. |
| **Voice Audio Capture (STT)** | `NOT STARTED` (Android) | 2026-10-15 | Android microphone button emits mock string (`"Mujhe aaj ke satsang dikhao"`). Native `SpeechRecognizer` is not wired. |
| **Onboarding Readiness** | `IN PROGRESS` | 2026-10-15 | Missing Gradle wrapper files restored; `.env.example`, `pytest.ini`, test scripts, and documentation established. |

---

## 2. Status Label Definitions

* **`NOT STARTED`**: Architectural design or backlog item; no active code exists in the repository.
* **`IN PROGRESS`**: Code is actively being written or refactored; feature is incomplete.
* **`BLOCKED`**: Progress cannot continue until an external dependency, toolchain issue, or hardware prerequisite is resolved.
* **`IMPLEMENTED - UNVERIFIED`**: Code is fully written and committed, but has not yet been executed or proven on live runtime target (device, emulator, or hardware).
* **`VERIFIED`**: Proven operational through automated test execution, live HTTP assertions, or reproducible manual validation.
* **`DEFERRED`**: Intentionally postponed for post-MVP release.

---

## 3. Feature-Level Progress Tracker

### A. Android UI & Presentation Layer

| Feature | Status | Implementation Location | Verification Evidence | Blockers / Dependencies | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Phone Login & OTP Screens** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/auth/LoginScreen.kt`, `OtpVerifyScreen.kt` | UI code structured with high-contrast text and 6-digit PIN boxes. | Requires JDK 17 & Android SDK 35 build. | Android Team |
| **Senior Onboarding Flow** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/auth/OnboardingScreen.kt` | Language selection (Hindi/English/Kannada) and font slider. | Requires runtime test. | Android Team |
| **Home Dashboard & Big Buttons** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/home/HomeScreen.kt` | Matches high-contrast layout with large touch targets ($\ge 56\text{ dp}$). | Needs verification on physical display. | Android Team |
| **112 Direct SOS Call** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/home/HomeViewModel.kt#L141-L148` | Fires `Intent.ACTION_DIAL` with `tel:112`. Does not block on network. | Requires device with SIM/Telephony. | Android Team |
| **Accessible Typography & Scaling** | `IMPLEMENTED - UNVERIFIED` | `android/.../theme/Type.kt`, `PreferencesDataStore.kt` | Font scale values ($1.0\times$ to $1.6\times$) wired to DataStore. | Visual verification across density buckets. | Android Team |
| **Light & Dark Themes** | `IMPLEMENTED - UNVERIFIED` | `android/.../theme/Theme.kt`, `Color.kt` | Warm cream light theme and high-contrast dark theme implemented. | Visual check on AMOLED screen. | Android Team |
| **Hyperlocal Events UI** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/events/EventsScreen.kt` | Satsang, Yoga, and Walk listings with filter chips and RSVP buttons. | API hooked to `EventsRepository.kt`. | Android Team |
| **Transport & Ambulance UI** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/transport/TransportScreen.kt` | Cab quotation cards and 112 ambulance dispatch banners. | API hooked to `TransportRepository.kt`. | Android Team |
| **Doctor Appointment UI** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/appointments/AppointmentsScreen.kt` | Recurrence selector (One-time, Weekly, Monthly) and doctor list. | API hooked to `AppointmentsRepository.kt`. | Android Team |
| **Prescription & Medicine UI** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/prescriptions/PrescriptionsScreen.kt` | Salt information card, Janaushadhi alternative viewer, and search bar. | Real camera capture not wired (uses stub). | Android Team |
| **Contacts & Messaging UI** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/contacts/ContactsScreen.kt` | Big avatar cards with direct voice call and message buttons. | Telephony/Dialer integration unverified. | Android Team |
| **Settings & Preferences UI** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/settings/SettingsScreen.kt` | Language switcher, font size slider, and logout confirmation. | Wired to Jetpack DataStore. | Android Team |

### B. Backend Services & Data Layer

| Feature | Status | Implementation Location | Verification Evidence | Blockers / Dependencies | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Backend Startup & Health Check** | `VERIFIED` | `backend/app/main.py#L50-L57` | `GET /health` returns HTTP 200 with service metadata (`scripts/smoke_test.sh`). | None. | Backend Team |
| **Database Initialization & Seeding**| `VERIFIED` | `backend/app/db_init.py` | Automatically creates 10 tables and seeds 4 community events on SQLite. | None. | Backend Team |
| **Development Sandbox OTP Auth** | `VERIFIED` | `backend/app/api/v1/endpoints/auth.py` | `POST /request-otp` and `POST /verify-otp` verified in `test_api.py`. | None (Production requires SMS gateway). | Backend Team |
| **JWT Session Security** | `VERIFIED` | `backend/app/core/security.py` | 30-day elder access tokens generated and validated on protected endpoints. | None. | Backend Team |
| **User Profile & Emergency Contacts**| `VERIFIED` | `backend/app/api/v1/endpoints/profile.py` | Profile update and emergency contact CRUD verified in test suite. | None. | Backend Team |
| **Community Events & RSVP** | `VERIFIED` | `backend/app/api/v1/endpoints/events.py` | Listing, category filtering, and idempotent event registration verified. | None. | Backend Team |
| **Transport State Machine** | `VERIFIED` | `backend/app/api/v1/endpoints/transport.py` | Cab quote, confirmation, and ambulance dispatch verified in test suite. | Real taxi fleet partner API deferred. | Backend Team |
| **Doctor Appointment Scheduling** | `VERIFIED` | `backend/app/api/v1/endpoints/appointments.py` | Appointment creation with `MONTHLY` recurrence verified in test suite. | Calendar sync deferred. | Backend Team |
| **Indian Medicine Salt Catalog** | `VERIFIED` | `backend/app/services/medicine_catalog.py` | Authentic catalog query (Metformin, Paracetamol, etc.) returns Janaushadhi generic alternatives with medical disclaimers. | None. | Backend Team |
| **Prescription OCR Parsing** | `IMPLEMENTED - UNVERIFIED` | `backend/app/api/v1/endpoints/prescriptions.py` | Mock OCR pipeline extracts medicines; Google Cloud Vision / ML Kit OCR pending. | External Cloud Vision API key. | Backend Team |
| **Contacts & Welcome Message** | `VERIFIED` | `backend/app/api/v1/endpoints/contacts.py` | Auto-seeds default trusted contacts (Beta, Beti, Doctor) and welcome message. | None. | Backend Team |

### C. Voice & AI Systems

| Feature | Status | Implementation Location | Verification Evidence | Blockers / Dependencies | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Multilingual Intent Parsing (NLU)** | `VERIFIED` | `backend/app/services/voice_assistant.py` | Verified with Hindi/Hinglish commands (`test_api.py` & `smoke_test.sh`). | None. Zero cloud latency fallback. | AI Team |
| **Allowlisted Tool Dispatcher** | `VERIFIED` | `backend/app/services/voice_assistant.py#L11-L23` | Restricts voice execution to 11 safe functions; blocks arbitrary execution. | None. | AI Team |
| **Consequential Action Confirmation** | `VERIFIED` | `backend/app/services/voice_assistant.py#L75-L77` | Cab booking and sensitive tools return `requires_confirmation=True`. | None. | AI Team |
| **Android Text-to-Speech (TTS)** | `IMPLEMENTED - UNVERIFIED` | `android/.../screens/home/HomeViewModel.kt#L62-L72` | Configured with `Locale("hi", "IN")` via Android native `TextToSpeech`. | Device audio output unverified. | AI Team |
| **Android Speech-to-Text (STT)** | `NOT STARTED` | `android/.../screens/home/HomeScreen.kt#L231` | Microphone click emits fixed test string. Native `SpeechRecognizer` missing. | Integration required. | Android / AI |
| **Cloud LLM (Gemini 1.5)** | `DEFERRED` | `backend/app/core/config.py#L23` | Placeholder setting present; local deterministic rule parser handles MVP. | API key required for free-form queries. | AI Team |

---

## 4. Known Bugs, Toolchain Blockers, and Technical Debt

| Issue | Severity | Affected Component | Reproduction Steps | Current Workaround | Assigned Owner | Next Action |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Gradle Kotlin DSL JDK 25 Crash** | `HIGH` | Android Build | Run `./gradlew tasks` on Linux host with OpenJDK 25 installed. | Install OpenJDK 17 (`sudo apt install openjdk-17-jdk`) and point `JAVA_HOME`. | DevOps | Configure Gradle Toolchain or install JDK 17. |
| **Missing Android SDK in CI/CLI** | `HIGH` | Android Build | Run `./gradlew assembleDebug` in CLI environment without Android SDK. | Use Android Studio IDE to download SDK 35 automatically. | Android Team | Document SDK installation steps in README. |
| **ROS launch_testing Pytest Crash** | `MEDIUM` (Resolved) | Backend Tests | Run `pytest` on Ubuntu systems with ROS installed. Pytest auto-loads broken ROS plugins. | Run via `scripts/run_backend_tests.sh` with `PYTEST_DISABLE_PLUGIN_AUTOLOAD=1`. | DevOps | Add isolated test runner and document workaround. |
| **SpeechRecognizer Unwired in UI** | `HIGH` | Android Voice Assistant | Tap microphone button on Home screen. | Currently supplies hardcoded mock query for verification. | Android Team | Wire Android `SpeechRecognizer` or Speech-to-Text Intent. |
| **Prescription Camera Capture Stubbed** | `MEDIUM` | Android Prescriptions | Tap "Parchi Photo Scan Karein". | Uses fixed base64 placeholder string. | Android Team | Implement CameraX photo capture intent. |

---

## 5. Prioritized Next Actions

1. **Resolve Local Android JDK 17 Environment**: Install JDK 17 and configure Android SDK 35 so `./gradlew assembleDebug` succeeds from the CLI.
2. **Wire Native Android `SpeechRecognizer`**: Replace the stub in `HomeScreen.kt` with Android's built-in `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` to capture live Hindi and English speech from the device microphone.
3. **Verify Android App in Emulator**: Start Android Studio, boot an API 34/35 emulator, execute the debug build, and assert communication with `http://10.0.2.2:8000/`.
4. **Implement CameraX Prescription Capture**: Integrate CameraX capture to provide actual prescription image uploads to `/api/v1/prescriptions/scan`.
5. **Add GitHub Actions CI**: Set up automated workflow to run `scripts/run_backend_tests.sh` and Gradle lint checks on pull requests.

---

## 6. Audit & Progress Changelog

### 2026-10-15 — Comprehensive Repository Audit & Reproducibility Setup
* **Audit Execution**: Audited entire Android and backend codebase, verifying dependencies, package structure, and endpoints.
* **Wrapper Restoration**: Restored missing `gradlew`, `gradlew.bat`, and `gradle-wrapper.jar` (Gradle 8.10.2).
* **Backend Verification**: Confirmed active FastAPI service on port 8000. All 6 automated test cases verified in `test_api.py`.
* **Reproducibility Automation**: Created `scripts/run_backend_tests.sh`, `scripts/smoke_test.sh`, `backend/Dockerfile`, and `docker-compose.yml`.
* **Security & Isolation**: Created root `.gitignore` protecting virtual environments, databases, and secrets.
* **Status Established**: Authored `PROJECT_STATUS.md` as the definitive progress tracking system.
