from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

class EventResponse(BaseModel):
    id: int
    title: str
    description: str
    category: str
    language: str
    date_time: datetime
    venue_name: str
    address: Optional[str]
    city: str
    is_online: bool
    online_meeting_url: Optional[str]
    organizer_name: str
    is_organizer_verified: bool
    capacity: int
    is_registered: bool = False

    class Config:
        from_attributes = True

class EventRegistrationResponse(BaseModel):
    id: int
    event_id: int
    user_id: int
    status: str
    registered_at: datetime
    message: str = "Aapka registration safaltapoorvak ho gaya hai."

    class Config:
        from_attributes = True
