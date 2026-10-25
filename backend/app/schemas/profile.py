from pydantic import BaseModel, Field, ConfigDict
from typing import List, Optional
from datetime import datetime

class EmergencyContactCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    phone_number: str = Field(..., min_length=8, max_length=20)
    relationship_type: str = Field(..., json_schema_extra={"example": "Daughter"})
    is_primary: bool = False
    can_view_location: bool = True

class EmergencyContactResponse(EmergencyContactCreate):
    id: int
    user_id: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class ProfileUpdate(BaseModel):
    name: Optional[str] = None
    preferred_language: Optional[str] = Field(None, pattern="^(hi|en|kn|te|ta|mr|gu|bn)$")
    font_scale: Optional[float] = Field(None, ge=1.0, le=1.6)
    voice_speed: Optional[float] = Field(None, ge=0.5, le=1.5)
    theme_mode: Optional[str] = Field(None, pattern="^(light|dark|system)$")
    city: Optional[str] = None
    is_onboarded: Optional[bool] = None

class ProfileResponse(BaseModel):
    id: int
    phone_number: str
    name: Optional[str]
    preferred_language: str
    font_scale: float
    voice_speed: float
    theme_mode: str
    city: str
    is_onboarded: bool
    emergency_contacts: List[EmergencyContactResponse] = []
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class SosAlertRequest(BaseModel):
    is_test_mode: bool = Field(False, description="Set true for judge demonstration or test runs without dispatching real SMS/calls")
    emergency_type: str = Field("GENERAL_SOS", description="GENERAL_SOS, MEDICAL_EMERGENCY, or FALL_DETECTED")
    location_latitude: Optional[float] = Field(12.9716, description="Optional GPS latitude")
    location_longitude: Optional[float] = Field(77.5946, description="Optional GPS longitude")
    location_name: Optional[str] = Field("Bengaluru", description="Readable landmark/location")

class GuardianDispatchChannel(BaseModel):
    name: str
    phone_number: str
    relationship_type: str
    whatsapp_url: str
    sms_url: str

class SosAlertResponse(BaseModel):
    alert_id: str
    user_id: int
    emergency_type: str
    is_test_mode: bool
    status: str
    direct_call_target: str = "112"
    notified_contacts_count: int
    notified_contacts: List[str]
    notified_phone_numbers: List[str] = []
    google_maps_url: str = ""
    sms_batch_uri: str = ""
    guardian_dispatches: List[GuardianDispatchChannel] = []
    alert_message_payload: str
    timestamp: datetime
