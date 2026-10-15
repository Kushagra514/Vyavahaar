from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user_id
from app.models.user import User
from app.models.emergency_contact import EmergencyContact
from app.schemas.profile import (
    ProfileResponse,
    ProfileUpdate,
    EmergencyContactCreate,
    EmergencyContactResponse,
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
