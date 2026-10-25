from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime, timezone
import urllib.parse
import re
import uuid

from app.core.database import get_db
from app.core.security import get_current_user_id
from app.models.user import User
from app.models.emergency_contact import EmergencyContact
from app.schemas.profile import (
    ProfileResponse,
    ProfileUpdate,
    EmergencyContactCreate,
    EmergencyContactResponse,
    GuardianDispatchChannel,
    SosAlertRequest,
    SosAlertResponse
)

router = APIRouter()

@router.get("/", response_model=ProfileResponse)
def get_my_profile(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user

@router.patch("/", response_model=ProfileResponse)
def update_profile(
    payload: ProfileUpdate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
        
    update_data = payload.model_dump(exclude_unset=True)
    for field, val in update_data.items():
        setattr(user, field, val)
        
    db.commit()
    db.refresh(user)
    return user

@router.post("/emergency-contacts", response_model=EmergencyContactResponse)
def add_emergency_contact(
    payload: EmergencyContactCreate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
        
    contact = EmergencyContact(
        user_id=user_id,
        name=payload.name,
        phone_number=payload.phone_number,
        relationship_type=payload.relationship_type,
        is_primary=payload.is_primary,
        can_view_location=payload.can_view_location
    )
    db.add(contact)
    db.commit()
    db.refresh(contact)
    return contact

@router.delete("/emergency-contacts/{contact_id}")
def delete_emergency_contact(
    contact_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    contact = db.query(EmergencyContact).filter(
        EmergencyContact.id == contact_id,
        EmergencyContact.user_id == user_id
    ).first()
    if not contact:
        raise HTTPException(status_code=404, detail="Emergency contact not found")
        
    db.delete(contact)
    db.commit()
    return {"message": "Emergency contact removed successfully"}

@router.post("/sos-alert", response_model=SosAlertResponse)
def trigger_sos_alert(
    payload: SosAlertRequest,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Trigger emergency SOS workflow with safe test mode.
    Dispatches notifications to registered caregiver contacts and prepares 112 direct emergency call.
    """
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    contacts = db.query(EmergencyContact).filter(EmergencyContact.user_id == user_id).all()
    contact_summaries = [f"{c.name} ({c.relationship_type}): {c.phone_number}" for c in contacts]
    phone_numbers = [c.phone_number for c in contacts]
    
    user_name = user.name or "Aadarniya Sadasya"
    loc_name = payload.location_name or "Bengaluru"
    lat = payload.location_latitude if payload.location_latitude is not None else 12.9716
    lon = payload.location_longitude if payload.location_longitude is not None else 77.5946
    google_maps_url = f"https://maps.google.com/?q={lat},{lon}"
    loc_str = f"near {loc_name} (Lat: {lat}, Lon: {lon})"
    
    alert_tag = "[EMERGENCY ALERT - TEST MODE / SIMULATION]" if payload.is_test_mode else "[CRITICAL EMERGENCY SOS ALERT]"
    message_body = (
        f"{alert_tag} {user_name} (Phone: {user.phone_number}) has triggered an emergency alert {loc_str}. "
        f"Live GPS Map: {google_maps_url} | "
        f"Emergency Type: {payload.emergency_type}. Direct dialing 112 initiated. Please check on them immediately."
    )
    
    encoded_msg = urllib.parse.quote(message_body)
    guardian_dispatches = []
    for c in contacts:
        clean_digits = re.sub(r"[^\d]", "", c.phone_number)
        guardian_dispatches.append(
            GuardianDispatchChannel(
                name=c.name,
                phone_number=c.phone_number,
                relationship_type=c.relationship_type,
                whatsapp_url=f"https://wa.me/{clean_digits}?text={encoded_msg}",
                sms_url=f"sms:{c.phone_number}?body={encoded_msg}"
            )
        )

    sms_batch_uri = f"smsto:{';'.join(phone_numbers)}?body={encoded_msg}" if phone_numbers else ""
    status_label = "SIMULATED_TEST_DISPATCHED" if payload.is_test_mode else "ACTIVE_ALERT_DISPATCHED"
    alert_id = f"SOS-{user_id}-{uuid.uuid4().hex[:8].upper()}"

    return SosAlertResponse(
        alert_id=alert_id,
        user_id=user_id,
        emergency_type=payload.emergency_type,
        is_test_mode=payload.is_test_mode,
        status=status_label,
        direct_call_target="112",
        notified_contacts_count=len(contacts),
        notified_contacts=contact_summaries,
        notified_phone_numbers=phone_numbers,
        google_maps_url=google_maps_url,
        sms_batch_uri=sms_batch_uri,
        guardian_dispatches=guardian_dispatches,
        alert_message_payload=message_body,
        timestamp=datetime.now(timezone.utc)
    )
