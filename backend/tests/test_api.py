import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.core.database import Base, engine
from app.db_init import init_db

client = TestClient(app)

@pytest.fixture(scope="session", autouse=True)
def setup_database():
    init_db()

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

def test_auth_otp_flow_and_profile():
    # 1. Request OTP
    phone = "+919876543210"
    req_resp = client.post("/api/v1/auth/request-otp", json={"phone_number": phone})
    assert req_resp.status_code == 200
    assert req_resp.json()["phone_number"] == phone
    
    # 2. Verify OTP with wrong code
    fail_resp = client.post("/api/v1/auth/verify-otp", json={"phone_number": phone, "otp_code": "000000"})
    assert fail_resp.status_code == 400
    
    # 3. Verify OTP with correct code
    verify_resp = client.post("/api/v1/auth/verify-otp", json={"phone_number": phone, "otp_code": "123456"})
    assert verify_resp.status_code == 200
    token_data = verify_resp.json()
    assert "access_token" in token_data
    token = token_data["access_token"]
    headers = {"Authorization": f"Bearer {token}"}
    
    # 4. Get profile
    profile_resp = client.get("/api/v1/profile/", headers=headers)
    assert profile_resp.status_code == 200
    profile_data = profile_resp.json()
    assert profile_data["phone_number"] == phone
    
    # 5. Update profile
    patch_resp = client.patch(
        "/api/v1/profile/",
        headers=headers,
        json={"name": "Aadarniya Dadaji", "preferred_language": "hi", "font_scale": 1.4}
    )
    assert patch_resp.status_code == 200
    assert patch_resp.json()["name"] == "Aadarniya Dadaji"
    assert patch_resp.json()["font_scale"] == 1.4

    # 6. Add emergency contact
    em_resp = client.post(
        "/api/v1/profile/emergency-contacts",
        headers=headers,
        json={
            "name": "Beti Sunita",
            "phone_number": "+919876543299",
            "relationship_type": "Daughter",
            "is_primary": True,
            "can_view_location": True
        }
    )
    assert em_resp.status_code == 200
    assert em_resp.json()["name"] == "Beti Sunita"

def test_voice_assistant_commands():
    # Authenticate first
    verify_resp = client.post("/api/v1/auth/verify-otp", json={"phone_number": "+919876543210", "otp_code": "123456"})
    headers = {"Authorization": f"Bearer {verify_resp.json()['access_token']}"}

    # Test SOS command
    sos_resp = client.post(
        "/api/v1/assistant/process-command",
        headers=headers,
        json={"transcript": "Bachao, emergency hai, 112 ko call lagao"}
    )
    assert sos_resp.status_code == 200
    assert sos_resp.json()["intent"] == "send_sos_alert"
    assert sos_resp.json()["tool_call"]["tool_name"] == "send_sos_alert"

    # Test Satsang command
    satsang_resp = client.post(
        "/api/v1/assistant/process-command",
        headers=headers,
        json={"transcript": "Mujhe aaj ke satsang dikhao"}
    )
    assert satsang_resp.status_code == 200
    assert satsang_resp.json()["intent"] == "search_events"
    assert satsang_resp.json()["tool_call"]["parameters"]["category"] == "Satsang"

    # Test Cab booking command
    cab_resp = client.post(
        "/api/v1/assistant/process-command",
        headers=headers,
        json={"transcript": "Mere liye mandir ke liye ek cab book karo"}
    )
    assert cab_resp.status_code == 200
    assert cab_resp.json()["intent"] == "request_ride"
    assert cab_resp.json()["tool_call"]["requires_confirmation"] is True

    # Test Text size accessibility command
    font_resp = client.post(
        "/api/v1/assistant/process-command",
        headers=headers,
        json={"transcript": "Text size bada kar do"}
    )
    assert font_resp.status_code == 200
    assert font_resp.json()["intent"] == "change_accessibility_settings"
    assert font_resp.json()["tool_call"]["parameters"]["font_scale"] == 1.4

def test_events_discovery_and_registration():
    verify_resp = client.post("/api/v1/auth/verify-otp", json={"phone_number": "+919876543210", "otp_code": "123456"})
    headers = {"Authorization": f"Bearer {verify_resp.json()['access_token']}"}

    # List events
    events_resp = client.get("/api/v1/events/", headers=headers)
    assert events_resp.status_code == 200
    events = events_resp.json()
    assert len(events) >= 1
    event_id = events[0]["id"]

    # Register for event
    reg_resp = client.post(f"/api/v1/events/{event_id}/register", headers=headers)
    assert reg_resp.status_code == 200
    assert reg_resp.json()["status"] == "REGISTERED"

    # Test idempotency (registering again should succeed without duplicate error)
    reg_again = client.post(f"/api/v1/events/{event_id}/register", headers=headers)
    assert reg_again.status_code == 200
    assert reg_again.json()["status"] == "REGISTERED"

def test_transport_workflow():
    verify_resp = client.post("/api/v1/auth/verify-otp", json={"phone_number": "+919876543210", "otp_code": "123456"})
    headers = {"Authorization": f"Bearer {verify_resp.json()['access_token']}"}

    # 1. Request Cab
    cab_req = client.post(
        "/api/v1/transport/request",
        headers=headers,
        json={
            "request_type": "CAB",
            "pickup_address": "Indiranagar 100ft Road",
            "destination_address": "Sri Ram Mandir Indiranagar"
        }
    )
    assert cab_req.status_code == 200
    cab_data = cab_req.json()
    assert cab_data["status"] == "QUOTED"
    cab_id = cab_data["id"]

    # 2. Confirm booking
    confirm_resp = client.post(f"/api/v1/transport/{cab_id}/confirm", headers=headers)
    assert confirm_resp.status_code == 200
    assert confirm_resp.json()["status"] == "CONFIRMED"
    assert confirm_resp.json()["driver_name"] is not None

    # 3. Emergency Ambulance Request
    amb_req = client.post(
        "/api/v1/transport/request",
        headers=headers,
        json={
            "request_type": "AMBULANCE",
            "pickup_address": "Home Address",
            "destination_address": "Manipal Hospital HAL Airport Road"
        }
    )
    assert amb_req.status_code == 200
    amb_data = amb_req.json()
    assert amb_data["emergency_direct_call_number"] == "112"

def test_appointments_and_prescriptions():
    verify_resp = client.post("/api/v1/auth/verify-otp", json={"phone_number": "+919876543210", "otp_code": "123456"})
    headers = {"Authorization": f"Bearer {verify_resp.json()['access_token']}"}

    # Book doctor appointment
    appt_resp = client.post(
        "/api/v1/appointments/",
        headers=headers,
        json={
            "doctor_name": "Dr. Ramesh Gupta",
            "specialty": "Cardiology",
            "clinic_or_hospital": "Fortis Hospital",
            "appointment_time": "2026-10-20T10:30:00",
            "recurrence_rule": "MONTHLY"
        }
    )
    assert appt_resp.status_code == 200
    assert appt_resp.json()["recurrence_rule"] == "MONTHLY"

    # Medicine search lookup
    med_resp = client.get("/api/v1/prescriptions/medicines/search?q=Metformin")
    assert med_resp.status_code == 200
    med_data = med_resp.json()
    assert "Metformin" in med_data["matched_salt"]
    assert len(med_data["verified_equivalents"]) >= 2
    assert "Informational only" in med_data["safety_disclaimer"]
