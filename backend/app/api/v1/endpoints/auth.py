from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.config import settings
from app.core.security import create_access_token
from app.models.user import User
from app.schemas.auth import PhoneOtpRequest, PhoneOtpResponse, OtpVerifyRequest, TokenResponse

router = APIRouter()

# In-memory OTP storage for development/sandbox testing
# In production, this coordinates with Firebase Authentication or SMS gateway
MOCK_OTP_STORAGE = {
    "+919876543210": "123456"
}

@router.post("/request-otp", response_model=PhoneOtpResponse)
def request_otp(payload: PhoneOtpRequest, db: Session = Depends(get_db)):
    phone = payload.phone_number.strip().replace(" ", "")
    if not phone.startswith("+"):
        phone = "+91" + phone.lstrip("0")
        
    # Generate or assign OTP
    otp = "123456" if settings.DEV_MODE else "654321"
    MOCK_OTP_STORAGE[phone] = otp
    
    return PhoneOtpResponse(
        message="Aapke phone number par OTP bhej diya gaya hai.",
        phone_number=phone,
        cooldown_seconds=60,
        dev_otp=otp if settings.DEV_MODE else None
    )

@router.post("/verify-otp", response_model=TokenResponse)
def verify_otp(payload: OtpVerifyRequest, db: Session = Depends(get_db)):
    phone = payload.phone_number.strip().replace(" ", "")
    if not phone.startswith("+"):
        phone = "+91" + phone.lstrip("0")
        
    expected_otp = MOCK_OTP_STORAGE.get(phone)
    if not expected_otp:
        # If in dev mode, default fallback OTP
        if settings.DEV_MODE and payload.otp_code == settings.DEV_TEST_OTP:
            expected_otp = settings.DEV_TEST_OTP
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="OTP request not found or expired. Please request a new OTP."
            )
            
    if payload.otp_code != expected_otp:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Aapka darj kiya gaya OTP galat hai. Kripya punah prayas karein."
        )
        
    # Clean up OTP
    if phone in MOCK_OTP_STORAGE:
        del MOCK_OTP_STORAGE[phone]
        
    # Get or create user
    user = db.query(User).filter(User.phone_number == phone).first()
    if not user:
        user = User(
            phone_number=phone,
            name="Aadarniya Sadasya",
            preferred_language="hi",
            is_onboarded=False
        )
        db.add(user)
        db.commit()
        db.refresh(user)
        
    token = create_access_token(subject=user.id)
    return TokenResponse(
        access_token=token,
        token_type="bearer",
        user_id=user.id,
        is_onboarded=user.is_onboarded,
        phone_number=user.phone_number
    )
