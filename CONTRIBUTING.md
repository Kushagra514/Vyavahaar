# Vyavahaar (व्यवहार) — Team Contributing Guide

Namaste! Welcome to the Vyavahaar development team. This document sets out practical conventions and workflows so teammates can collaborate smoothly without stepping on each other's toes or breaking working features.

---

## 1. Repository Structure

```
Vyavahaar/
├── android/                    # Native Android application (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── src/main/java/org/vyavahaar/app/
│   │   │   ├── data/          # Retrofit API clients, repositories, DataStore preferences
│   │   │   ├── navigation/    # Compose NavHost and destination definitions
│   │   │   ├── theme/         # High-contrast color palette, accessible typography
│   │   │   └── ui/            # Compose screens (Home, Auth, Events, Transport, etc.)
│   │   └── build.gradle.kts   # App-level dependencies and Android config
│   ├── gradle/                # Gradle version catalog (libs.versions.toml) and wrapper
│   └── gradlew                # Gradle executable wrapper
├── backend/                   # REST API backend (FastAPI + Python 3 + SQLAlchemy)
│   ├── app/
│   │   ├── api/v1/endpoints/  # Route controllers (auth, events, transport, assistant, etc.)
│   │   ├── core/              # Config (Pydantic BaseSettings), database engine, security
│   │   ├── models/            # SQLAlchemy ORM models
│   │   ├── schemas/           # Pydantic request/response schemas
│   │   ├── services/          # NLU voice assistant parser & Indian medicine catalog
│   │   ├── db_init.py         # Table creation and sample community events seeder
│   │   └── main.py            # FastAPI entry point & lifespan handler
│   ├── tests/                 # Automated pytest suite
│   ├── requirements.txt       # Python dependencies
│   └── Dockerfile             # Container definition for reproducible deployment
├── scripts/                   # Reproducible helper scripts
│   ├── run_backend_tests.sh   # Isolated backend test runner
│   └── smoke_test.sh          # Live HTTP API smoke tester
├── docker-compose.yml         # Containerized backend & database orchestration
├── PROJECT_STATUS.md          # Team source of truth for progress and verification
└── README.md                  # Project overview and setup instructions
```

---

## 2. Branching & Git Conventions

### Branch Naming
Use descriptive branch prefixes based on what you are working on:
* `feature/<short-description>`: New screens, endpoints, or features (e.g. `feature/android-speech-recognizer`, `feature/prescription-camerax`)
* `fix/<bug-description>`: Bug fixes (e.g. `fix/otp-cooldown-timer`, `fix/appointment-sort-order`)
* `docs/<topic>`: Documentation updates (e.g. `docs/api-guide`, `docs/setup-faq`)
* `refactor/<module>`: Code cleanup without functional changes (e.g. `refactor/transport-viewmodel`)

### Commit Messages
Write clear, concise commit messages in the imperative mood:
```
feat(android): wire Android SpeechRecognizer intent to mic button
fix(backend): correct event registration idempotency check
docs: update setup prerequisites for OpenJDK 17
```

---

## 3. Local Development Setup

### Backend (Python 3.10+)
1. Create and activate a Python virtual environment:
   ```bash
   cd backend
   python3 -m venv venv
   source venv/bin/activate
   ```
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Copy environment variables:
   ```bash
   cp .env.example .env
   ```
4. Start the development server:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```

### Android (Android Studio & JDK 17)
1. Open the `android/` directory in Android Studio (Koala, Ladybug, or later).
2. Ensure Android Studio uses **JDK 17** (File $\to$ Settings $\to$ Build, Execution, Deployment $\to$ Build Tools $\to$ Gradle $\to$ Gradle JDK $\to$ Select JDK 17).
3. Let Gradle sync dependencies.
4. Launch an emulator (API 34 or 35) or connect a physical device.
5. In the Android emulator, the app automatically communicates with the host machine at `http://10.0.2.2:8000/`.

---

## 4. Running Tests Before Submitting Changes

Always run tests before opening a pull request:

### Backend Tests
Run the isolated test script from the project root:
```bash
./scripts/run_backend_tests.sh
```
Or run the live HTTP smoke test if the backend server is running:
```bash
./scripts/smoke_test.sh
```

### Android Code Verification
Ensure the project builds cleanly without errors:
```bash
cd android
./gradlew testDebugUnitTest
```

---

## 5. Pull Request Guidelines

Every pull request must include a description using this checklist:

```markdown
### Summary of Changes
- What was added, changed, or removed?

### How Was This Tested?
- [ ] Ran `./scripts/run_backend_tests.sh`
- [ ] Verified manually on Android Emulator / Physical Device
- [ ] Verified live endpoints with `./scripts/smoke_test.sh`

### Known Limitations or Outstanding Work
- Mention any edge cases, mock services used, or follow-up tasks required.
```

---

## 6. Secrets & Environment Configuration

* **Never commit secrets**: Never hardcode API keys, tokens, passwords, or personal credentials into git.
* Keep all sensitive configuration in local `.env` files (which are ignored in `.gitignore`).
* If you introduce a new environment variable:
  1. Add it to `backend/app/core/config.py` with appropriate default or optional typing.
  2. Add it to `backend/.env.example` with a dummy placeholder value.
  3. Document its purpose in `README.md`.

---

## 7. Keeping Documentation Up-to-Date

When your changes are merged:
* If you implemented, tested, or verified a feature, update its entry in `PROJECT_STATUS.md`.
* Move features from `IMPLEMENTED - UNVERIFIED` to `VERIFIED` **only** if you have evidence (automated tests or reproducible device execution).
* If you added or changed an API endpoint, update the API table in `README.md`.

---

## 8. Senior Accessibility Design Principles (Do Not Break!)

Vyavahaar is specifically engineered for Indian elders. Whenever modifying the Android UI, preserve these rules:
1. **Touch Targets**: Minimum interactive touch target must be **$48\times 48\text{ dp}$** (preferably $56\text{ dp}$ or larger).
2. **High Contrast**: Ensure contrast ratios $\ge 4.5:1$ for normal text and $\ge 3:1$ for large text against backgrounds.
3. **Deterministic Emergency Fallback**: Never hide or disable the 112 SOS button behind menus or network calls.
4. **Action Confirmation**: Never perform consequential actions (e.g. paying, booking a cab, deleting records) without an explicit verbal or single-tap confirmation dialog.
5. **Clear Language**: UI copy should use respectful, simple words (Hindi, English, or Kannada) without technical jargon.
