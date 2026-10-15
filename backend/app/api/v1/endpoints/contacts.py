from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user_id
from app.models.contact import Contact, FriendRequest
from app.models.message import Message
from app.schemas.contact import (
    ContactCreateRequest,
    ContactResponse,
    MessageCreateRequest,
    MessageResponse
)

router = APIRouter()

@router.get("/", response_model=List[ContactResponse])
def get_contacts(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    contacts = db.query(Contact).filter(Contact.user_id == user_id).all()
    if not contacts:
        # Seed default helpful trusted contacts for the senior user
        default_contacts = [
            Contact(user_id=user_id, name="Beta (Rahul)", phone_number="+919876543211", avatar_color="#E65100", is_favorite=True),
            Contact(user_id=user_id, name="Beti (Priya)", phone_number="+919876543212", avatar_color="#00897B", is_favorite=True),
            Contact(user_id=user_id, name="Dr. Sharma (Physician)", phone_number="+919876543213", avatar_color="#1E88E5", is_favorite=False),
            Contact(user_id=user_id, name="Sharma Ji (Neighbor)", phone_number="+919876543214", avatar_color="#F57C00", is_favorite=False)
        ]
        db.add_all(default_contacts)
        db.commit()
        contacts = db.query(Contact).filter(Contact.user_id == user_id).all()
    return contacts

@router.post("/", response_model=ContactResponse)
def add_contact(
    payload: ContactCreateRequest,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    contact = Contact(
        user_id=user_id,
        name=payload.name,
        phone_number=payload.phone_number,
        is_favorite=payload.is_favorite
    )
    db.add(contact)
    db.commit()
    db.refresh(contact)
    return contact

@router.get("/messages", response_model=List[MessageResponse])
def get_user_messages(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    messages = db.query(Message).filter(
        (Message.sender_id == user_id) | (Message.receiver_id == user_id)
    ).order_by(Message.created_at.desc()).all()
    
    if not messages:
        # Seed welcome message
        welcome_msg = Message(
            sender_id=0,
            receiver_id=user_id,
            content="Namaste! Vyavahaar parivartan mein aapka hardik swagat hai. Kripya microphone dabayein aur batayein aapko kya chahiye.",
            is_voice_transcribed=False,
            delivery_status="DELIVERED"
        )
        db.add(welcome_msg)
        db.commit()
        messages = [welcome_msg]
        
    return messages

@router.post("/messages", response_model=MessageResponse)
def send_message(
    payload: MessageCreateRequest,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    msg = Message(
        sender_id=user_id,
        receiver_id=payload.receiver_id,
        content=payload.content,
        is_voice_transcribed=payload.is_voice_transcribed,
        delivery_status="SENT"
    )
    db.add(msg)
    db.commit()
    db.refresh(msg)
    return msg
