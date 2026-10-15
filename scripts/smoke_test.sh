#!/usr/bin/env bash
set -e

# Vyavahaar Live API Smoke Test
# Validates running backend service, authentication, and core API workflows.

BASE_URL="${1:-http://127.0.0.1:8000}"

echo "=========================================================="
echo " Starting Vyavahaar API Smoke Test against: $BASE_URL"
echo "=========================================================="

# 1. Health check
echo -n "[1/7] Testing Health Check endpoint... "
HEALTH_RESP=$(curl -s -f "$BASE_URL/health")
echo "PASSED"
echo "      Response: $HEALTH_RESP"

# 2. Request OTP
echo -n "[2/7] Testing Dev OTP Request (+919876543210)... "
OTP_REQ_RESP=$(curl -s -f -X POST "$BASE_URL/api/v1/auth/request-otp" \
    -H "Content-Type: application/json" \
    -d '{"phone_number": "+919876543210"}')
echo "PASSED"

# 3. Verify OTP and obtain JWT Bearer Token
echo -n "[3/7] Testing Dev OTP Verification (Code: 123456)... "
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
echo -n "[4/7] Testing Profile Retrieval with Bearer Token... "
PROFILE_RESP=$(curl -s -f "$BASE_URL/api/v1/profile/" \
    -H "Authorization: Bearer $TOKEN")
echo "PASSED"

# 5. Multilingual Voice Assistant NLU (SOS Command)
echo -n "[5/7] Testing Voice Assistant SOS Intent Parsing... "
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
echo -n "[6/7] Testing Hyperlocal Community Events Discovery... "
EVENTS_RESP=$(curl -s -f "$BASE_URL/api/v1/events/" \
    -H "Authorization: Bearer $TOKEN")
EVENT_COUNT=$(python3 -c "import json; print(len(json.loads('''$EVENTS_RESP''')))")
echo "PASSED ($EVENT_COUNT community events available)"

# 7. Indian Medicine Catalog Lookup
echo -n "[7/7] Testing Indian Medicine Salt & Alternative Lookup... "
MED_RESP=$(curl -s -f "$BASE_URL/api/v1/prescriptions/medicines/search?q=Metformin")
SALT=$(python3 -c "import json; print(json.loads('''$MED_RESP''')['matched_salt'])")
echo "PASSED (Matched salt: $SALT)"

echo "=========================================================="
echo " ALL SMOKE TESTS PASSED! Vyavahaar backend is fully operational."
echo "=========================================================="
