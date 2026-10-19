# Vyavahaar (व्यवहार) — Live Hackathon Demonstration Script

> **Document Type:** Live Judging Walkthrough, Exact Operator Commands & Demonstration Protocol  
> **Target Audience:** Hackathon Judges, Technical Evaluators, Live Demonstrators  
> **Demonstration Duration:** 5 to 7 Minutes  
> **Prerequisites:** Backend running on `http://127.0.0.1:8000/` (or Docker Compose)

---

## 1. Pre-Demo Verification Checklist (2 Minutes Before Judging)

Run the fast pre-flight check to verify service health and database readiness:
```bash
cd /home/kushagra/Downloads/Vyavaahar
# 1. Run automated backend test suite (11/11 tests)
./scripts/run_backend_tests.sh

# 2. Check live backend health
curl -s http://127.0.0.1:8000/health
# Expected: {"status":"healthy","service":"Vyavahaar API",...}
```

---

## 2. Step-by-Step Live Walkthrough

### Act 1 — The Elder's Onboarding & Accessibility Experience (60 Seconds)
* **Narrative:** *"Indian senior citizens struggle with small fonts, complicated menus, and English-only interfaces. Vyavahaar opens with a welcoming, high-contrast, Indian-language interface with touch targets exceeding 56 dp."*
* **Demonstration Steps:**
  1. Open app to **Login Screen** (`Vyavahaar Pravesh`).
  2. Point out phone input prefilled with dev phone `+91 9876543210`.
  3. Tap **"OTP Bhejein"** $\rightarrow$ Observe Sandbox OTP hint `123456`.
  4. On **OTP Screen**, enter `123456` and tap **"Satyaapan Karein"**.
  5. On **Onboarding Screen**, show the language selector (`Hindi`, `English`, `Kannada`) and the real-time **Font Size Slider** ($1.0\times$ to $1.6\times$).
  6. Tap **"Aage Badhein"** $\rightarrow$ Lands on **Home Dashboard**.
* **Expected Output:**
  * Warm cream background (`#FFFDF7`), high-contrast typography, large touch targets.
  * Spoken Hindi TTS greeting: *"Namaste, main aapki sahayata ke liye taiyaar hoon."*
* **Fallback Protocol:** If Android Studio / emulator is unavailable, demonstrate the auth lifecycle via CLI:
  ```bash
  curl -s -X POST http://127.0.0.1:8000/api/v1/auth/verify-otp \
    -H "Content-Type: application/json" \
    -d '{"phone_number": "+919876543210", "otp_code": "123456"}'
  ```

---

### Act 2 — Real Voice Modality & Intent Routing (90 Seconds)
* **Narrative:** *"Instead of navigating multi-level menus, seniors press a single, prominent microphone button and speak naturally in Hindi or Hinglish."*
* **Demonstration Steps:**
  1. On Home Dashboard, tap the pulsing orange **Microphone Button**.
  2. Speak: *"Mujhe aaj ke satsang dikhao"* (or tap the **Satsang** quick-command chip).
  3. Observe real-time assistant feedback card:
     * Transcribed text: *"Mujhe aaj ke satsang dikhao"*
     * Spoken Hindi TTS response: *"Satsang aur kirtan karyakram khoje ja rahe hain."*
     * Action button: **"Aage badhein (Open)"** $\rightarrow$ Tapping navigates directly to the filtered Events screen!
  4. Return to Home and demonstrate consequential action safety:
     * Speak: *"Mere liye mandir ke liye cab book karo"* (or tap **Cab** chip).
     * Observe two-step confirmation modal: *"Kya aap Mandir ke liye cab book karna chahte hain?"*
     * Demonstrate that consequential actions prevent accidental elder bookings.
* **Expected Output:**
  * Spoken audio synthesis via native TTS (`Locale("hi", "IN")`).
  * Structured tool execution via `voice_assistant.py` with zero cloud latency.

---

### Act 3 — Emergency Assistance & Safe SOS Demonstration (60 Seconds)
* **Narrative:** *"In an emergency, an elder cannot afford confusing menus. Vyavahaar features a prominent Red SOS button with accidental-trigger protection and caregiver notifications."*
* **Demonstration Steps:**
  1. On Home Dashboard, tap the red **SOS Button**.
  2. Observe the **Emergency Protection Dialog** showing:
     * `🚨 112 Direct Call Lagayein` (Real emergency dialer)
     * `📢 Contacts Ko Alert Bhejein` (Live caregiver dispatch)
     * `🧪 Safe Demo / Test Mode` (Demonstration mode)
  3. Tap **"🧪 Safe Demo / Test Mode"**.
  4. Observe the **SOS Alert Summary Dialog**:
     * Alert ID: `SOS-1-...`
     * Status: `SIMULATED_TEST_DISPATCHED`
     * Target: `112 Emergency Services`
     * GPS Coordinates: `(12.9716, 77.5946) near Cubbon Park, Bengaluru`
     * Formatted Caregiver SMS Payload displayed for judges.
  5. Demonstrate that **112 Direct Call** separately launches the Android phone dialer with `tel:112` prefilled.
* **Expected Output:**
  * Accidental-trigger protection prevents panic mis-clicks.
  * Verified backend dispatch to registered emergency contacts (`POST /api/v1/profile/sos-alert`).

---

### Act 4 — Local Events Discovery & Persistent RSVP (60 Seconds)
* **Narrative:** *"Social isolation is a major crisis for elderly citizens. Vyavahaar connects them to verified hyperlocal Satsangs, laughter yoga sessions, and free health checkup camps."*
* **Demonstration Steps:**
  1. From bottom navigation, tap the **Calendar Icon** (or navigate via voice).
  2. Show category filter chips: `Sabhi`, `Satsang & Bhajan`, `Morning Walk`, `Yoga & Pranayama`, `Health Camp`.
  3. Point out verified organizer badges (`Indiranagar Senior Citizen Satsang Samiti`, `Rotary Club`).
  4. On an event card, tap **"Shamil Ho (Join Event)"**.
  5. Observe instant RSVP confirmation badge: **"Aap registered hain (Registered)"**.
  6. Tap **"Karyakram Chhodein (Cancel RSVP)"** to demonstrate full bidirectional RSVP persistence.
* **Expected Output:**
  * Idempotent registration on SQLite database.
  * State persists across refreshes and screen changes.

---

### Act 5 — Prescription OCR & Janaushadhi Medicine Savings (90 Seconds)
* **Narrative:** *"Indian seniors frequently struggle with complex doctor prescriptions and spend excessive money on branded medicines. Vyavahaar provides camera OCR and maps active salts to government-verified Janaushadhi generic equivalents."*
* **Demonstration Steps:**
  1. From Services or Voice, open **"Dawai & Parchi Jankaari"**.
  2. Tap **"🧪 Sample Demo"** (or use camera if on physical hardware).
  3. Observe OCR extraction results:
     * Doctor: `Dr. A. K. Sharma (MD Internal Med)`
     * Extracted medicines with confidence badges:
       - `Metformin 500mg SR` (92% Confidence)
       - `Amlodipine 5mg` (88% Confidence)
       - `Pan 40` (90% Confidence)
     * Prominent medical safety disclaimer: *"CRITICAL DISCLAIMER: Informational only. Never modify dosages without consulting your physician."*
  4. On the `Metformin 500mg SR` card, tap **"Saste Vikalp Khojein"**.
  5. Observe the verified Indian essential medicines catalog response:
     * Active Salt: `Metformin Hydrochloride 500mg`
     * Branded Price: Glycomet 500 SR at **₹28.50**
     * Pradhan Mantri Bhartiya Janaushadhi Generic Price: **₹6.50** ($\sim 77\%$ cost savings for the elder!)
* **Expected Output:**
  * Accurate entity parsing, confidence scoring, clear distinction between provisional OCR and verified medical database facts.

---

## 3. Judge Q&A & Technical Defense Cheat-Sheet

* **Q: Why deterministic NLU instead of a cloud LLM for voice?**  
  *A: Elderly emergency and utility commands require zero network latency, guaranteed sub-100ms response times, and deterministic safety. Cloud LLMs hallucinate tool calls; our allowlisted regex/keyword parser in `voice_assistant.py` is safe, offline-capable, and cost-free.*

* **Q: How is security handled if there is a dev OTP?**  
  *A: The sandbox OTP (`123456`) is restricted to the designated development phone number (`+919876543210`) in `config.py`. All protected endpoints strictly validate cryptographic HS256 JWT tokens. We explicitly tested and eliminated silent auth bypasses in `security.py`.*

* **Q: What happens if an elder accidentally taps the SOS button?**  
  *A: Accidental-trigger protection opens a clear confirmation modal distinguishing between live 112 dialing, caregiver notification, and safe testing. Emergency calls always require explicit confirmation.*

* **Q: Is the medicine substitution automatic?**  
  *A: Absolutely not. In accordance with medical ethics and Indian CDSCO regulations, Vyavahaar displays verified chemical salt equivalences and prices for informational transparency, accompanied by explicit disclaimers directing elders to consult their physician before changing brands.*
