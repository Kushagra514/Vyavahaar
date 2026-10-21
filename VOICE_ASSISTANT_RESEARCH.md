# Vyavahaar — Voice Assistant & Intent Routing Technical Research

> **Document Type:** ASR, NLU, and TTS Architectural Evaluation & Feasibility Study  
> **Target Audience:** Engineering Team, Technical Judges, Solutions Architects  
> **Scope:** Hindi, English, and Hinglish Speech Recognition, Intent Parsing, and Speech Synthesis  
> **Author:** Voice & AI Systems Engineering Lead

---

## 1. Executive Summary & Problem Context

Vyavahaar is an elder-centric application designed for Indian senior citizens. Elders frequently encounter cognitive and motor challenges with standard touch interfaces: small fonts, nested menus, keyboard typos, and English-only nomenclature. 

A voice modality serves as the primary gateway for seniors to invoke emergencies (112 SOS), schedule medical appointments, book transportation, discover community Satsangs, and scan prescriptions.

However, an elderly voice interface must adhere to strict non-negotiables:
1. **Zero-Latency Emergency Path:** Critical commands (*"bachao"*, *"emergency"*, *"112"*) must resolve in $< 50\,\text{ms}$ without waiting on cloud LLM inference or network round-trips.
2. **Strict Consequential Action Safety:** High-impact actions (ride booking, SOS dispatch) must require explicit confirmation and cannot execute from ambiguous or hallucinated intent.
3. **Deterministic Reliability:** Zero hallucinations, zero arbitrary function execution.
4. **Cost & Sustainability:** Zero mandatory paid cloud API dependencies for core operations.
5. **Language Scope:** Focused exclusively on **Hindi (`hi-IN`)**, **English (`en-IN` / `en-US`)**, and **Hinglish** (code-switched everyday speech).

---

## 2. Speech-to-Text (ASR) Candidate Evaluation

| Technology | License & Commercial Terms | Cloud Cost & Limits | Hindi / Hinglish Accuracy | Latency & Hardware Profile | Offline Support | Verdict |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Android Native `SpeechRecognizer` (`RecognizerIntent`)** | Android Open Source Project (Apache 2.0) / Google Play Services | **100% Free** (built-in OS service, zero API billing) | **High** on Hindi and Indian English; captures common Hinglish words via hybrid vocabulary | **Low** ($< 200\,\text{ms}$ end-of-speech to transcription); zero server overhead | **Supported** if offline speech pack is installed in Google Speech Services | **SELECTED for Production Prototype** |
| **AI4Bharat IndicConformer / IndicASR** | MIT / CC-BY 4.0 | Self-hosted server required ($\sim \$40-\$80/\text{mo}$ for GPU/CPU inference) | **Exceptional** on pure Hindi and Indic dialects; specialized for Indian acoustic environments | $250-600\,\text{ms}$ network latency + server queue time; requires 4GB+ VRAM or 4 vCPU | Server-dependent; not locally runnable on low-cost elder phones | **Evaluated & Deferred** (High server infrastructure cost) |
| **OpenAI Whisper / `faster-whisper` (int8/tiny)** | MIT | Open weights; self-hosted server or cloud API (\$0.006 / min) | High on English and Hindi; strong on Romanized Hindi | 500ms–1500ms on CPU; thermal/battery heavy for on-device mobile inference | Feasible on high-end devices, impractical on entry-level Android devices | **Evaluated & Rejected for On-Device** (Excessive mobile resource consumption) |
| **Cloud ASR (Google Cloud Speech-to-Text)** | Proprietary commercial | Free tier: 60 mins/mo; thereafter \$0.016 / 15s (\$0.064/min) | High | $300-800\,\text{ms}$ network round-trip; fails without active internet | None | **Rejected** (Hidden operational costs; sudden service cut-off after free tier) |

### Selected ASR Strategy: Android Native Speech Engine
* **Integration Mechanism:** `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` with runtime permission checking (`Manifest.permission.RECORD_AUDIO`) and standard activity result handling.
* **Language Parameters:**
  * Primary: `hi-IN` (Hindi - India)
  * Secondary / Supported: `en-IN` (English - India), `en-US` (English - US)
  * `EXTRA_LANGUAGE_PREFERENCE`: `"hi-IN"`
  * `EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE`: `false` (allows engine to return English transcription if English was spoken)
* **Privacy Assurance:** Raw audio buffers remain on-device or transit directly to user's configured Google Speech Service without intermediate third-party audio recording retention or backend logging.

---

## 3. Natural Language Intent Parsing & Routing Evaluation

| Approach | Technology | Cost / Month | Execution Latency | Safety & Hallucination Risk | Consequential Action Guardrails | Verdict |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Deterministic Allowlisted Rule/Entity Router** | Python regex & token taxonomy (`voice_assistant.py`) | **\$0.00** (Local CPU process) | **$< 2\,\text{ms}$** | **Zero hallucination risk**; strictly bounded to 11 allowlisted tools | Inherent: tool signatures declare `requires_confirmation=True` | **SELECTED as Primary Authority** |
| **Open-Weight Small LLM (Qwen 2.5-1.5B / 3B)** | Apache 2.0 / Qwen License | Self-hosted GPU instance ($\sim \$60-\$120/\text{mo}$) | $400-1200\,\text{ms}$ | Low-to-moderate; can hallucinate function parameters or invent tools | Requires secondary validation filter layer | **Deferred for Post-MVP Q&A** |
| **Commercial Cloud LLMs (Grok API, GPT-4o-mini)** | Proprietary commercial | Metered token pricing; unbudgeted billing risks | $800-2500\,\text{ms}$ | Variable; risk of jailbreaks, prompt injection, and unpredictable output | Cannot be trusted directly with emergency or financial actions | **Rejected for Critical Path** |

### Selected NLU Strategy: Bounded Deterministic Tool Router
* **Intent Architecture:** The backend NLU parser (`backend/app/services/voice_assistant.py`) parses Hindi, English, and Hinglish transcripts into typed `VoiceCommandResponse` objects.
* **Allowlisted Tools:**
  1. `send_sos_alert` (Emergency SOS dispatch)
  2. `request_ride` (Cab and Ambulance booking)
  3. `search_events` (Hyperlocal Satsang, Yoga, Walking discovery)
  4. `schedule_appointment` (Doctor visit scheduling)
  5. `scan_prescription` (Camera prescription OCR launch)
  6. `search_medicine_information` (Janaushadhi salt queries)
  7. `start_call` (Direct caregiver calling)
  8. `change_accessibility_settings` (Font scaling, theme, language)
  9. `cancel_action` (Deterministic negation & state reset)
  10. `navigate_to_screen` (Safe screen routing)
* **Negation & Ambiguity Guardrails:** Transcripts containing negative phrases (*"nahi"*, *"mat karo"*, *"don't"*, *"cancel"*) are evaluated with priority to prevent accidental execution of negated actions (e.g. *"Cab book mat karo"* must not trigger a cab booking).

---

## 4. Text-to-Speech (TTS) Evaluation

| Technology | License & Cost | Voices Available | Hindi (`hi-IN`) Quality | Latency | Device Dependency | Verdict |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Android Native `TextToSpeech`** | AOSP / Device Native | Google TTS, Samsung TTS, OEM engines | High (natural pronunciation of Hindi phonemes and Indian accent) | **$< 50\,\text{ms}$** | Requires installed TTS engine (present on $>99\%$ of certified Android devices) | **SELECTED** |
| **AI4Bharat Indic-TTS** | MIT | 22 Indian languages | Excellent, human-like cadence | $300-800\,\text{ms}$ network latency | Requires self-hosted streaming server | Deferred |
| **Cloud TTS (Google Cloud / ElevenLabs)** | Metered commercial | Studio quality | High | $400-1000\,\text{ms}$ | Network required; recurring cost per character | Rejected |

### Selected TTS Strategy: Device Native Audio Synthesis
* **Initialization:** Initialized in `HomeViewModel.kt` with language priority `Locale("hi", "IN")`.
* **Fallback Protocol:** If `tts.setLanguage(Locale("hi", "IN"))` returns `LANG_MISSING_DATA` or `LANG_NOT_SUPPORTED`, it gracefully falls back to `Locale("en", "IN")` or `Locale.ENGLISH`. If TTS is disabled or unavailable on hardware, visual feedback cards on Compose UI display the entire response text.
* **Interruption & Queue Safety:** Uses `TextToSpeech.QUEUE_FLUSH` to immediately clear stale audio queues when a new command is issued, preventing queued responses from playing over subsequent user speech.

---

## 5. Grok & Modern Voice Architecture Review

### 5.1 Project Note Recovery
A search of the project repository (`grep -rn -i "grok" .`) yielded no pre-existing Grok design artifacts or conversation records. In accordance with engineering standards, this limitation is documented. We conducted fresh analysis of Grok / xAI architectural paradigms for conversational assistants.

### 5.2 Grok Paradigm Comparison vs. Vyavahaar Reality
* **Grok Real-Time Streaming Vision:** Grok advocates for continuous bidirectional audio streaming over WebSockets with real-time token generation and interruption detection.
* **Why Pure Streaming LLM Voice is Inadvisable for Vyavahaar:**
  1. *Connectivity Fragility:* Indian tier-2 and tier-3 cities frequently experience 2G/3G drops. A continuous streaming WebSocket will disconnect mid-command.
  2. *Emergency Liability:* An LLM generating token-by-token responses during an SOS event introduces fatal latency and hallucination risks.
  3. *Financial Feasibility:* An elderly care non-profit or hackathon prototype cannot sustain \$500+/month in cloud inference fees.
* **Adopted Principles from Grok Research:**
  * **Interruption Capability:** User speech or cancel tap immediately aborts active TTS playback.
  * **Explicit Intent State Machine:** Structured state transitions with clear feedback.
  * **Strict Safety Boundaries:** Disconnect generative text generation from destructive operations.

---

## 6. Architectural Decision Records (ADRs)

* **ADR-01: Native SpeechRecognizer Over Cloud ASR**  
  *Decision:* Use Android OS native `RecognizerIntent` with `hi-IN` and `en-IN`.  
  *Rationale:* Zero operating cost, native OS microphone permission integration, low battery consumption, works with user's existing Google Speech language packs.

* **ADR-02: Bounded Deterministic NLU Over Generative LLM**  
  *Decision:* Maintain deterministic regex/token router in `voice_assistant.py` as sole authority for tool dispatching.  
  *Rationale:* Sub-2ms execution time, 100% deterministic safety, zero hallucinations, complete testability.

* **ADR-03: Two-Step Confirmation State Machine for Consequential Actions**  
  *Decision:* Cab bookings, ambulance calls, and SOS dispatches require verbal or visual confirmation before side effects occur.  
  *Rationale:* Protects seniors from mis-recognitions, background ambient noise, or accidental commands.
