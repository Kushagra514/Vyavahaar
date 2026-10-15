from datetime import datetime, timezone
from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, Float, JSON
from sqlalchemy.orm import relationship
from app.core.database import Base

class PrescriptionRecord(Base):
    __tablename__ = "prescription_records"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    image_url = Column(String(500), nullable=False)
    doctor_name = Column(String(120), nullable=True)
    date_prescribed = Column(String(50), nullable=True)
    extracted_raw_text = Column(Text, nullable=False)
    
    # JSON list of extracted medicines:
    # [{"name": "Metformin", "strength": "500mg", "dosage": "Twice daily after meals", "confidence": 0.85, "needs_verification": true}]
    extracted_medicines = Column(JSON, default=list)
    
    confidence_score = Column(Float, default=0.8)
    status = Column(String(30), default="PENDING_REVIEW")  # PENDING_REVIEW, REVIEWED_BY_USER, ARCHIVED
    safety_disclaimer_accepted = Column(String(10), default="YES")
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    user = relationship("User", back_populates="prescriptions")
