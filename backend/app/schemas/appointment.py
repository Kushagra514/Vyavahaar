from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime

class AppointmentCreateRequest(BaseModel):
    doctor_name: str = Field(..., min_length=2)
    specialty: str = Field(..., min_length=2)
    clinic_or_hospital: str = Field(..., min_length=2)
    appointment_time: datetime
    recurrence_rule: str = Field("NONE", pattern="^(NONE|WEEKLY|MONTHLY)$")
    notes: Optional[str] = None

class AppointmentResponse(BaseModel):
    id: int
    user_id: int
    doctor_name: str
    specialty: str
    clinic_or_hospital: str
    appointment_time: datetime
    recurrence_rule: str
    status: str
    reminder_set: bool
    notes: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True
