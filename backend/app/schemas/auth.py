from pydantic import BaseModel, Field
from typing import Optional

class PhoneOtpRequest(BaseModel):
    phone_number: str = Field(..., example="+919876543210", description="E.164 phone number")

class PhoneOtpResponse(BaseModel):
    message: str
    phone_number: str
    cooldown_seconds: int = 60
    dev_otp: Optional[str] = None

class OtpVerifyRequest(BaseModel):
    phone_number: str = Field(..., example="+919876543210")
    otp_code: str = Field(..., min_length=4, max_length=6, example="123456")

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: int
    is_onboarded: bool
    phone_number: str
