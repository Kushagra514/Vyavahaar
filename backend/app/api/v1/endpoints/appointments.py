from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user_id
from app.models.appointment import DoctorAppointment
from app.schemas.appointment import AppointmentCreateRequest, AppointmentResponse

router = APIRouter()

@router.get("/", response_model=List[AppointmentResponse])
def get_my_appointments(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    return db.query(DoctorAppointment).filter(
        DoctorAppointment.user_id == user_id
    ).order_by(DoctorAppointment.appointment_time.asc()).all()

@router.post("/", response_model=AppointmentResponse)
def schedule_appointment(
    payload: AppointmentCreateRequest,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    appointment = DoctorAppointment(
        user_id=user_id,
        doctor_name=payload.doctor_name,
        specialty=payload.specialty,
        clinic_or_hospital=payload.clinic_or_hospital,
        appointment_time=payload.appointment_time,
        recurrence_rule=payload.recurrence_rule,
        status="CONFIRMED",
        reminder_set=True,
        notes=payload.notes
    )
    db.add(appointment)
    db.commit()
    db.refresh(appointment)
    return appointment

@router.patch("/{appointment_id}/cancel", response_model=AppointmentResponse)
def cancel_appointment(
    appointment_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    appointment = db.query(DoctorAppointment).filter(
        DoctorAppointment.id == appointment_id,
        DoctorAppointment.user_id == user_id
    ).first()
    if not appointment:
        raise HTTPException(status_code=404, detail="Appointment not found")
        
    appointment.status = "CANCELLED"
    db.commit()
    db.refresh(appointment)
    return appointment
