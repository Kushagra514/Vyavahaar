# Vyavahaar (व्यवहार) — Complete Verification & Testing Guide

> **Document Type:** Repeatable Verification Suite, Endpoint Checks & Known Limitations  
> **Suite Status:** 11/11 Automated Backend Tests Passing | 9/9 Live Smoke Tests Passing  
> **Toolchain Targets:** Python 3.12+ (Backend) | OpenJDK 17 + Gradle 8.10.2 (Android)

---

## 1. Environment Setup

### 1.1 Python Virtual Environment
```bash
cd backend
python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
python app/db_init.py
```

### 1.2 Android Environment
* **Java Development Kit:** OpenJDK 17 (Eclipse Temurin) at `~/.jdks/openjdk-17`.
* **Gradle Wrapper:** Restored at `android/gradlew` (Gradle 8.10.2).
* **Android SDK Location:** Point `local.properties` to your Android SDK:
  ```properties
  sdk.dir=/home/kushagra/Android/Sdk
  ```

---

## 2. Repeatable Test Execution Commands

### 2.1 Automated Backend Pytest Suite
Runs all 11 test cases with plugin autoload disabled (safe on ROS-equipped and clean developer machines):
```bash
cd /home/kushagra/Downloads/Vyavaahar
./scripts/run_backend_tests.sh
```
*Direct invocation:*
```bash
cd backend
source venv/bin/activate
PYTEST_DISABLE_PLUGIN_AUTOLOAD=1 PYTHONPATH=. pytest tests/test_api.py -v -p asyncio
```

### 2.2 Live API Smoke Test
Starts uvicorn and executes the 9-step end-to-end verification script:
```bash
# Terminal 1: Start Backend Server
cd backend && source venv/bin/activate
PYTHONPATH=. uvicorn app.main:app --host 127.0.0.1 --port 8000

# Terminal 2: Run Smoke Test
cd /home/kushagra/Downloads/Vyavaahar
./scripts/smoke_test.sh http://127.0.0.1:8000
```

### 2.3 Android Toolchain Verification
Verify Gradle task graph and Kotlin plugin configuration without requiring a physical connected device:
```bash
cd android
JAVA_HOME=/home/kushagra/.jdks/openjdk-17 ./gradlew tasks --no-daemon
JAVA_HOME=/home/kushagra/.jdks/openjdk-17 ./gradlew :app:checkKotlinGradlePluginConfigurationErrors
```

---

## 3. Test Cases & Verification Matrix

| # | Test Function / Check | Scope | Verification Evidence | Result |
| :--- | :--- | :--- | :--- | :--- |
| **T01** | `test_health_check` | Health & metadata | `GET /health` returns HTTP 200 `{"status": "healthy"}` | **PASSED** |
| **T02** | `test_auth_otp_flow_and_profile` | Authentication & profile | `POST /request-otp`, invalid OTP 400 rejection, valid OTP 200 token generation, profile retrieval, profile patch, emergency contact creation | **PASSED** |
| **T03** | `test_voice_assistant_commands` | Voice NLU & routing | Intent classification for SOS, Satsang discovery, cab booking (with confirmation), and font scale modification | **PASSED** |
| **T04** | `test_events_discovery_and_registration` | Hyperlocal events | Listing community events, idempotent RSVP registration without duplicate errors | **PASSED** |
| **T05** | `test_transport_workflow` | Transport state machine | Cab quote $\rightarrow$ confirmation with driver assignment; emergency ambulance quote with direct 112 flag | **PASSED** |
| **T06** | `test_appointments_and_prescriptions` | Appointments & catalog | Doctor appointment creation with `MONTHLY` recurrence; Metformin medicine search with Janaushadhi generic alternatives | **PASSED** |
| **T07** | `test_unauthorized_access_protection` | Security enforcement | Missing or omitted JWT Bearer token on protected endpoints (`/profile/`, `/events/`, `/transport/`, `/sos-alert`) strictly returns HTTP 401 Unauthorized | **PASSED** |
| **T08** | `test_sos_emergency_alert_safe_test_mode` | Emergency assistance | `POST /profile/sos-alert` with `is_test_mode=True` returns `SIMULATED_TEST_DISPATCHED`, location coordinates, and notified contact summary | **PASSED** |
| **T09** | `test_prescription_ocr_and_persistent_history`| Prescription OCR | Base64 encoded prescription scan parses Metformin and Pan 40 with $\ge 85\%$ confidence; `GET /prescriptions/` confirms persistence | **PASSED** |
| **T10** | `test_event_rsvp_leave_cancellation` | Bidirectional RSVP | `DELETE /events/{id}/register` successfully cancels active RSVP | **PASSED** |
| **T11** | `test_edge_cases_and_error_handling` | Error handling | Unknown medicine search returns HTTP 404 with elder-friendly message; invalid transport type returns HTTP 422 | **PASSED** |

---

## 4. Live API Endpoint Health & Contract Checks

```bash
# 1. Health Endpoint
curl -s http://127.0.0.1:8000/health
# Response: {"status":"healthy","service":"Vyavahaar API","version":"1.0.0","tagline":"Aapke Vyavahar Mein Hi Humara Pyaar Hai"}

# 2. Request OTP (Sandbox)
curl -s -X POST http://127.0.0.1:8000/api/v1/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{"phone_number": "+919876543210"}'
# Response: {"message":"OTP sent successfully.","phone_number":"+919876543210","cooldown_seconds":60,"dev_otp":"123456"}

# 3. Verify OTP & Obtain Token
curl -s -X POST http://127.0.0.1:8000/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phone_number": "+919876543210", "otp_code": "123456"}'
# Response: {"access_token":"<JWT_TOKEN>","token_type":"bearer","user_id":1,"is_onboarded":true,"phone_number":"+919876543210"}

# 4. Search Medicine & Janaushadhi Alternatives
curl -s "http://127.0.0.1:8000/api/v1/prescriptions/medicines/search?q=Metformin"
# Response includes matched_salt: "Metformin Hydrochloride" and Janaushadhi Metformin 500 at ₹6.50

# 5. Trigger Safe Demonstration SOS Alert
curl -s -X POST http://127.0.0.1:8000/api/v1/profile/sos-alert \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"is_test_mode": true, "emergency_type": "GENERAL_SOS", "location_name": "Cubbon Park"}'
# Response: {"alert_id":"SOS-1-...","status":"SIMULATED_TEST_DISPATCHED","direct_call_target":"112",...}
```

---

## 5. Known Limitations & Fallback Protocols

1. **Host-Level Android SDK Compilation:**
   * *Limitation:* The local CLI environment currently lacks downloaded Android SDK 35 build tools (`platforms;android-35`).
   * *Fallback:* In Android Studio, opening `android/` automatically initiates SDK component installation. In CI/CD, GitHub-hosted Ubuntu runners include the complete Android SDK and pass all validation steps.

2. **Microphone Voice Capture in Emulators:**
   * *Limitation:* Headless or standard x86 emulators without Google Play Services may throw an activity not found exception on `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`.
   * *Fallback:* `HomeScreen.kt` catches the exception and offers immediate one-tap Assist Chips ("Satsang", "Cab", "Call Beti") to test all NLU intent routing transparently.

3. **Camera Capture in Emulators:**
   * *Limitation:* Emulators without a virtual scene camera cannot photograph physical paper prescriptions.
   * *Fallback:* `PrescriptionsScreen.kt` provides a dedicated **"🧪 Sample Demo"** button that submits a prepared prescription scan payload to demonstrate the full OCR extraction, confidence scoring, and Janaushadhi alternatives pipeline.
