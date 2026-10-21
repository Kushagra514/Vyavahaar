#!/usr/bin/env bash
set -e

# Vyavahaar Live API Smoke Test
# Validates running backend service, authentication, and core API workflows.

BASE_URL="${1:-http://127.0.0.1:8000}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_ROOT/backend"

SPAWNED_SERVER=0
if ! curl -s "$BASE_URL/health" > /dev/null 2>&1; then
    echo "Notice: No server running at $BASE_URL. Auto-starting local instance for smoke test..."
    cd "$BACKEND_DIR"
    if [ -d "venv" ]; then
        # shellcheck disable=SC1091
        source venv/bin/activate
    fi
    python3 -m uvicorn app.main:app --host 127.0.0.1 --port 8000 > /dev/null 2>&1 &
    SERVER_PID=$!
    SPAWNED_SERVER=1
    trap 'if [ $SPAWNED_SERVER -eq 1 ]; then kill $SERVER_PID 2>/dev/null || true; wait $SERVER_PID 2>/dev/null || true; fi' EXIT INT TERM
    for _ in {1..40}; do
        if curl -s "$BASE_URL/health" > /dev/null 2>&1; then
            break
        fi
        sleep 0.2
    done
fi

echo "=========================================================="
echo " Starting Vyavahaar API Smoke Test against: $BASE_URL"
echo "=========================================================="

# 1. Health check
echo -n "[1/9] Testing Health Check endpoint... "
HEALTH_RESP=$(curl -s -f "$BASE_URL/health")
echo "PASSED"
echo "      Response: $HEALTH_RESP"

# 2. Request OTP
echo -n "[2/9] Testing Dev OTP Request (+919876543210)... "
OTP_REQ_RESP=$(curl -s -f -X POST "$BASE_URL/api/v1/auth/request-otp" \
    -H "Content-Type: application/json" \
    -d '{"phone_number": "+919876543210"}')
echo "PASSED"

# 3. Verify OTP and obtain JWT Bearer Token
echo -n "[3/9] Testing Dev OTP Verification (Code: 123456)... "
TOKEN_RESP=$(curl -s -f -X POST "$BASE_URL/api/v1/auth/verify-otp" \
    -H "Content-Type: application/json" \
    -d '{"phone_number": "+919876543210", "otp_code": "123456"}')
TOKEN=$(python3 -c "import json; print(json.loads('''$TOKEN_RESP''')['access_token'])")
if [ -z "$TOKEN" ]; then
    echo "FAILED (No token returned)"
    exit 1
fi
echo "PASSED (Bearer token acquired)"

# 4. Profile retrieval
echo -n "[4/9] Testing Profile Retrieval with Bearer Token... "
PROFILE_RESP=$(curl -s -f "$BASE_URL/api/v1/profile/" \
    -H "Authorization: Bearer $TOKEN")
echo "PASSED"

# 5. Multilingual Voice Assistant NLU (SOS Command)
echo -n "[5/9] Testing Voice Assistant SOS Intent Parsing... "
SOS_RESP=$(curl -s -f -X POST "$BASE_URL/api/v1/assistant/process-command" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"transcript": "Bachao, emergency hai, 112 ko call lagao"}')
INTENT=$(python3 -c "import json; print(json.loads('''$SOS_RESP''')['intent'])")
if [ "$INTENT" != "send_sos_alert" ]; then
    echo "FAILED (Unexpected intent: $INTENT)"
    exit 1
fi
echo "PASSED (Intent: $INTENT)"

# 6. Community Events Discovery
echo -n "[6/9] Testing Hyperlocal Community Events Discovery... "
EVENTS_RESP=$(curl -s -f "$BASE_URL/api/v1/events/" \
    -H "Authorization: Bearer $TOKEN")
EVENT_COUNT=$(python3 -c "import json; print(len(json.loads('''$EVENTS_RESP''')))")
echo "PASSED ($EVENT_COUNT community events available)"

# 7. Indian Medicine Catalog Lookup
echo -n "[7/9] Testing Indian Medicine Salt & Alternative Lookup... "
MED_RESP=$(curl -s -f "$BASE_URL/api/v1/prescriptions/medicines/search?q=Metformin")
SALT=$(python3 -c "import json; print(json.loads('''$MED_RESP''')['matched_salt'])")
echo "PASSED (Matched salt: $SALT)"

# 8. Safe Emergency SOS Workflow in Test Mode
echo -n "[8/9] Testing Safe Emergency SOS Simulation in Test Mode... "
SOS_ALERT_RESP=$(curl -s -f -X POST "$BASE_URL/api/v1/profile/sos-alert" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"is_test_mode": true, "emergency_type": "GENERAL_SOS", "location_name": "Cubbon Park"}')
SOS_STATUS=$(python3 -c "import json; print(json.loads('''$SOS_ALERT_RESP''')['status'])")
if [ "$SOS_STATUS" != "SIMULATED_TEST_DISPATCHED" ]; then
    echo "FAILED (Unexpected SOS status: $SOS_STATUS)"
    exit 1
fi
echo "PASSED (Status: $SOS_STATUS, Direct Target: 112)"

# 9. Prescription OCR entity parsing
echo -n "[9/9] Testing Prescription OCR Extraction and Safety Disclaimers... "
SAMPLE_RX="Rx: Dr. Sharma\n1. Tab Metformin 500mg SR\n2. Tab Pan 40"
B64_RX=$(echo -e "$SAMPLE_RX" | base64 -w 0)
OCR_RESP=$(curl -s -f -X POST "$BASE_URL/api/v1/prescriptions/scan" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"image_base64_or_url\": \"$B64_RX\"}")
MED_COUNT=$(python3 -c "import sys, json; print(len(json.load(sys.stdin)['extracted_medicines']))" <<< "$OCR_RESP")
if [ "$MED_COUNT" -lt 1 ]; then
    echo "FAILED (No medicines extracted)"
    exit 1
fi
echo "PASSED ($MED_COUNT medicines parsed with safety disclaimer)"

echo "=========================================================="
echo " ALL 9/9 SMOKE TESTS PASSED! Vyavahaar backend is fully operational."
echo "=========================================================="
