from datetime import datetime, timezone
from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, Boolean
from sqlalchemy.orm import relationship
from app.core.database import Base

class DoctorAppointment(Base):
    __tablename__ = "doctor_appointments"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    doctor_name = Column(String(120), nullable=False)
    specialty = Column(String(100), nullable=False)  # General Physician, Cardiologist, Orthopedic, etc.
    clinic_or_hospital = Column(String(200), nullable=False)
    appointment_time = Column(DateTime, nullable=False)
    
    # Recurrence rules: NONE, WEEKLY, MONTHLY
    recurrence_rule = Column(String(20), default="NONE")
    
    # Status: REQUESTED, CONFIRMED, COMPLETED, CANCELLED
    status = Column(String(30), default="REQUESTED", nullable=False)
    
    reminder_set = Column(Boolean, default=True)
    notes = Column(Text, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    user = relationship("User", back_populates="appointments")
