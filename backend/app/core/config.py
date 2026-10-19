from typing import List, Optional
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field

class Settings(BaseSettings):
    PROJECT_NAME: str = "Vyavahaar API"
    API_V1_STR: str = "/api/v1"
    SECRET_KEY: str = "vyavahaar-senior-elder-care-in-india-secret-key-change-in-prod"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 30  # 30 days for elders
    
    # Database
    DATABASE_URL: str = "sqlite:///./vyavahaar.db"
    
    # Dev / Test Mode
    DEV_MODE: bool = True
    DEV_TEST_PHONE: str = "+919876543210"
    DEV_TEST_OTP: str = "123456"
    
    # CORS
    BACKEND_CORS_ORIGINS: List[str] = ["*"]
    
    # Optional Cloud Keys
    GEMINI_API_KEY: Optional[str] = None
    FIREBASE_PROJECT_ID: Optional[str] = None

    model_config = SettingsConfigDict(env_file=".env", case_sensitive=True)

settings = Settings()
