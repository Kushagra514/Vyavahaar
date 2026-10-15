from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime

class ContactCreateRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    phone_number: str = Field(..., min_length=8, max_length=20)
    is_favorite: bool = False

class ContactResponse(BaseModel):
    id: int
    user_id: int
    name: str
    phone_number: str
    avatar_color: str
    is_favorite: bool
    is_vyavahaar_user: bool
    created_at: datetime

    class Config:
        from_attributes = True

class MessageCreateRequest(BaseModel):
    receiver_id: int
    content: str = Field(..., min_length=1)
    is_voice_transcribed: bool = False

class MessageResponse(BaseModel):
    id: int
    sender_id: int
    receiver_id: int
    content: str
    is_voice_transcribed: bool
    delivery_status: str
    created_at: datetime

    class Config:
        from_attributes = True
