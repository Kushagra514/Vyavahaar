from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime, timezone, timedelta
from app.core.database import get_db
from app.core.security import get_current_user_id
from app.models.event import Event, EventRegistration
from app.schemas.event import EventResponse, EventRegistrationResponse

router = APIRouter()

@router.get("/", response_model=List[EventResponse])
def get_events(
    category: Optional[str] = Query(None, description="Filter: Satsang, Yoga, Walking, HealthCamp, Cultural"),
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    query = db.query(Event)
    if category and category.upper() != "ALL":
        query = query.filter(Event.category.ilike(f"%{category}%"))
        
    events = query.order_time = query.order_by(Event.date_time.asc()).all()
    
    # Check registration status for current user
    user_regs = set(
        r.event_id for r in db.query(EventRegistration).filter(
            EventRegistration.user_id == user_id,
            EventRegistration.status == "REGISTERED"
        ).all()
    )
    
    results = []
    for ev in events:
        ev_dict = {
            "id": ev.id,
            "title": ev.title,
            "description": ev.description,
            "category": ev.category,
            "language": ev.language,
            "date_time": ev.date_time,
            "venue_name": ev.venue_name,
            "address": ev.address,
            "city": ev.city,
            "is_online": ev.is_online,
            "online_meeting_url": ev.online_meeting_url,
            "organizer_name": ev.organizer_name,
            "is_organizer_verified": ev.is_organizer_verified,
            "capacity": ev.capacity,
            "is_registered": ev.id in user_regs
        }
        results.append(EventResponse(**ev_dict))
    return results

@router.post("/{event_id}/register", response_model=EventRegistrationResponse)
def register_for_event(
    event_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    event = db.query(Event).filter(Event.id == event_id).first()
    if not event:
        raise HTTPException(status_code=404, detail="Event not found")
        
    existing_reg = db.query(EventRegistration).filter(
        EventRegistration.user_id == user_id,
        EventRegistration.event_id == event_id
    ).first()
    
    if existing_reg:
        if existing_reg.status == "REGISTERED":
            # Idempotent response: already registered
            return EventRegistrationResponse(
                id=existing_reg.id,
                event_id=existing_reg.event_id,
                user_id=existing_reg.user_id,
                status=existing_reg.status,
                registered_at=existing_reg.registered_at,
                message="Aap pehle se is karyakram mein registered hain."
            )
        else:
            existing_reg.status = "REGISTERED"
            existing_reg.registered_at = datetime.now(timezone.utc)
            db.commit()
            db.refresh(existing_reg)
            return existing_reg
            
    # New registration
    reg = EventRegistration(
        user_id=user_id,
        event_id=event_id,
        status="REGISTERED"
    )
    db.add(reg)
    db.commit()
    db.refresh(reg)
    return reg

@router.delete("/{event_id}/register")
def cancel_event_registration(
    event_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    reg = db.query(EventRegistration).filter(
        EventRegistration.user_id == user_id,
        EventRegistration.event_id == event_id,
        EventRegistration.status == "REGISTERED"
    ).first()
    if not reg:
        raise HTTPException(status_code=404, detail="Active registration not found")
        
    reg.status = "CANCELLED"
    db.commit()
    return {"message": "Registration cancelled successfully"}
