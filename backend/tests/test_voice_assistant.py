import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.db_init import init_db
from app.services.voice_assistant import voice_assistant_service

client = TestClient(app)

@pytest.fixture(scope="session", autouse=True)
def setup_database():
    init_db()

@pytest.fixture
def auth_header():
    resp = client.post("/api/v1/auth/verify-otp", json={"phone_number": "+919876543210", "otp_code": "123456"})
    assert resp.status_code == 200
    token = resp.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}

def test_voice_sos_immediate_dispatch(auth_header):
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Bachao, emergency hai, madad karo"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "send_sos_alert"
    assert data["confidence"] > 0.95
    assert data["tool_call"]["requires_confirmation"] is False
    assert "SOS" in data["spoken_feedback"]

def test_voice_english_sos(auth_header):
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Help me, this is an emergency, call police"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "send_sos_alert"
    assert data["confidence"] > 0.95

def test_voice_cab_booking_requires_confirmation(auth_header):
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Mandir jane ke liye ek cab book karo"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "request_ride"
    assert data["tool_call"]["requires_confirmation"] is True
    assert "Mandir" in data["tool_call"]["parameters"]["suggested_destination"]
    assert data["tool_call"]["confirmation_prompt"] is not None

def test_voice_negation_overrides_cab_action(auth_header):
    # User says "cab book mat karo" - previously would book cab because of "cab" keyword
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Cab book mat karo"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "cancel_action"
    assert "radd" in data["spoken_feedback"]
    assert data["tool_call"]["requires_confirmation"] is False

def test_voice_negation_english_dont_book(auth_header):
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Don't book the cab, cancel it"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "cancel_action"

def test_voice_affirmative_confirmation(auth_header):
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Haan, confirm kar do"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "confirm_action"
    assert "pushti" in data["spoken_feedback"]

def test_voice_rejection_cancellation(auth_header):
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Nahi, ruk jao"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "cancel_action"

@pytest.mark.parametrize("unsupported_text", [
    "Kannada bhasha mein bolo",
    "Bengali te bolun",
    "Tamil bhashaiyil pesavum"
])
def test_voice_unsupported_language_rejection(auth_header, unsupported_text):
    # Constraint: Only Hindi, English, and Hinglish supported; other regional languages rejected politely
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": unsupported_text}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "unsupported_language"
    assert "Kshama karein, awaz sahayata keval Hindi aur English mein uplabdh hai." in data["spoken_feedback"]
    assert data["needs_clarification"] is True

def test_voice_satsang_events_hindi_and_english(auth_header):
    # Hindi
    resp_hi = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Mujhe aaj ke satsang aur kirtan dikhao"}
    )
    assert resp_hi.status_code == 200
    assert resp_hi.json()["intent"] == "search_events"
    assert resp_hi.json()["tool_call"]["parameters"]["category"] == "Satsang"

    # English
    resp_en = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Show me yoga and community events"}
    )
    assert resp_en.status_code == 200
    assert resp_en.json()["intent"] == "search_events"

def test_voice_medicine_and_prescription(auth_header):
    # Prescription scan
    resp_rx = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Doctor ki prescription parchi scan karo"}
    )
    assert resp_rx.status_code == 200
    assert resp_rx.json()["intent"] == "scan_prescription"

    # Medicine search
    resp_med = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Paracetamol dawai ki jankaari do"}
    )
    assert resp_med.status_code == 200
    assert resp_med.json()["intent"] == "search_medicine_information"

def test_voice_call_family(auth_header):
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Bete ko video call lagao"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "start_call"
    assert data["tool_call"]["parameters"]["target_name"] == "Beta"
    assert data["tool_call"]["parameters"]["is_video"] is True

def test_voice_unrecognized_fallback(auth_header):
    resp = client.post(
        "/api/v1/assistant/process-command",
        headers=auth_header,
        json={"transcript": "Random gibberish blablabla qwerty"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["intent"] == "unknown"
    assert data["needs_clarification"] is True
    assert len(data["clarification_options"]) > 0
