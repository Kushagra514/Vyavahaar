from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime

class EmergencyContactCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    phone_number: str = Field(..., min_length=8, max_length=20)
    relationship_type: str = Field(..., example="Daughter")
    is_primary: bool = False
    can_view_location: bool = True

class EmergencyContactResponse(EmergencyContactCreate):
    id: int
    user_id: int
    created_at: datetime

    class Config:
        from_attributes = True

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

    class Config:
        from_attributes = True
