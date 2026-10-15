from datetime import datetime, timezone
from sqlalchemy import Column, Integer, String, Text, Boolean, DateTime, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship
from app.core.database import Base

class Event(Base):
    __tablename__ = "events"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(200), nullable=False)
    description = Column(Text, nullable=False)
    category = Column(String(50), nullable=False)  # Satsang, Yoga, Walking, HealthCamp, Cultural
    language = Column(String(20), default="hi")
    date_time = Column(DateTime, nullable=False)
    venue_name = Column(String(200), nullable=False)
    address = Column(String(300), nullable=True)
    city = Column(String(100), default="Bengaluru")
    is_online = Column(Boolean, default=False)
    online_meeting_url = Column(String(500), nullable=True)
    organizer_name = Column(String(150), nullable=False)
    is_organizer_verified = Column(Boolean, default=True)
    capacity = Column(Integer, default=50)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    registrations = relationship("EventRegistration", back_populates="event", cascade="all, delete-orphan")

class EventRegistration(Base):
    __tablename__ = "event_registrations"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    event_id = Column(Integer, ForeignKey("events.id", ondelete="CASCADE"), nullable=False)
    status = Column(String(20), default="REGISTERED")  # REGISTERED, CANCELLED, ATTENDED
    registered_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    user = relationship("User", back_populates="event_registrations")
    event = relationship("Event", back_populates="registrations")

    __table_args__ = (
        UniqueConstraint("user_id", "event_id", name="uq_user_event_registration"),
    )
