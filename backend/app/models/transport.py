from datetime import datetime, timezone
from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from app.core.database import Base

class TransportRequest(Base):
    __tablename__ = "transport_requests"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    request_type = Column(String(20), nullable=False)  # CAB, AMBULANCE
    pickup_address = Column(String(300), nullable=False)
    pickup_latitude = Column(Float, nullable=True)
    pickup_longitude = Column(Float, nullable=True)
    destination_address = Column(String(300), nullable=False)
    destination_latitude = Column(Float, nullable=True)
    destination_longitude = Column(Float, nullable=True)
    
    # State machine: DRAFT, QUOTED, CONFIRMED, DRIVER_ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    status = Column(String(30), default="DRAFT", nullable=False)
    fare_estimate = Column(Float, nullable=True)
    currency = Column(String(5), default="INR")
    
    # Driver & Vehicle details (when confirmed)
    driver_name = Column(String(100), nullable=True)
    driver_phone = Column(String(20), nullable=True)
    vehicle_model = Column(String(100), nullable=True)
    vehicle_number = Column(String(30), nullable=True)
    
    notes = Column(Text, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    user = relationship("User", back_populates="transport_requests")
