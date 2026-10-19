from pydantic import BaseModel, Field, ConfigDict
from typing import Optional
from datetime import datetime

class TransportCreateRequest(BaseModel):
    request_type: str = Field(..., pattern="^(CAB|AMBULANCE)$")
    pickup_address: str = Field(..., min_length=3)
    pickup_latitude: Optional[float] = None
    pickup_longitude: Optional[float] = None
    destination_address: str = Field(..., min_length=3)
    destination_latitude: Optional[float] = None
    destination_longitude: Optional[float] = None
    notes: Optional[str] = None

class TransportConfirmRequest(BaseModel):
    transport_id: int
    confirm: bool = True

class TransportResponse(BaseModel):
    id: int
    user_id: int
    request_type: str
    pickup_address: str
    destination_address: str
    status: str
    fare_estimate: Optional[float]
    currency: str
    driver_name: Optional[str]
    driver_phone: Optional[str]
    vehicle_model: Optional[str]
    vehicle_number: Optional[str]
    created_at: datetime
    emergency_direct_call_number: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)
