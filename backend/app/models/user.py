from datetime import datetime, timezone
from sqlalchemy import Column, Integer, String, Float, DateTime, Boolean
from sqlalchemy.orm import relationship
from app.core.database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    phone_number = Column(String(20), unique=True, index=True, nullable=False)
    name = Column(String(100), nullable=True)
    preferred_language = Column(String(20), default="hi")  # hi (Hindi), en (English), kn (Kannada)
    font_scale = Column(Float, default=1.2)  # 1.0 to 1.6 accessibility scale
    voice_speed = Column(Float, default=0.9)  # 0.7 to 1.2
    theme_mode = Column(String(20), default="light")  # light, dark, system
    profile_photo_url = Column(String(500), nullable=True)
    city = Column(String(100), default="Bengaluru")
    is_onboarded = Column(Boolean, default=False)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    emergency_contacts = relationship("EmergencyContact", back_populates="user", cascade="all, delete-orphan")
    transport_requests = relationship("TransportRequest", back_populates="user", cascade="all, delete-orphan")
    appointments = relationship("DoctorAppointment", back_populates="user", cascade="all, delete-orphan")
    prescriptions = relationship("PrescriptionRecord", back_populates="user", cascade="all, delete-orphan")
    event_registrations = relationship("EventRegistration", back_populates="user", cascade="all, delete-orphan")
    contacts = relationship("Contact", back_populates="user", cascade="all, delete-orphan")
