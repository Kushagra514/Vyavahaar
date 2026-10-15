from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime, timezone
from app.core.database import get_db
from app.core.security import get_current_user_id
from app.models.transport import TransportRequest
from app.schemas.transport import (
    TransportCreateRequest,
    TransportConfirmRequest,
    TransportResponse,
)

router = APIRouter()

@router.post("/request", response_model=TransportResponse)
def create_transport_request(
    payload: TransportCreateRequest,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    is_ambulance = payload.request_type == "AMBULANCE"
    
    # Generate realistic estimate or emergency routing
    fare = 0.0 if is_ambulance else 149.0
    status = "CONFIRMED" if is_ambulance else "QUOTED"
    
    req = TransportRequest(
        user_id=user_id,
        request_type=payload.request_type,
        pickup_address=payload.pickup_address,
        pickup_latitude=payload.pickup_latitude,
        pickup_longitude=payload.pickup_longitude,
        destination_address=payload.destination_address,
        destination_latitude=payload.destination_latitude,
        destination_longitude=payload.destination_longitude,
        fare_estimate=fare,
        status=status,
        driver_name="Ramesh Kumar (Emergency Verified)" if is_ambulance else None,
        driver_phone="+919876500001" if is_ambulance else None,
        vehicle_model="Basic Life Support Ambulance" if is_ambulance else None,
        vehicle_number="KA-01-EA-108" if is_ambulance else None,
        notes=payload.notes
    )
    db.add(req)
    db.commit()
    db.refresh(req)
    
    resp_dict = req.__dict__.copy()
    if is_ambulance:
        resp_dict["emergency_direct_call_number"] = "112"
        
    return TransportResponse(**resp_dict)

@router.post("/{transport_id}/confirm", response_model=TransportResponse)
def confirm_transport_booking(
    transport_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    req = db.query(TransportRequest).filter(
        TransportRequest.id == transport_id,
        TransportRequest.user_id == user_id
    ).first()
    if not req:
        raise HTTPException(status_code=404, detail="Transport request not found")
        
    if req.status == "CANCELLED":
        raise HTTPException(status_code=400, detail="Cannot confirm a cancelled ride request")
        
    req.status = "CONFIRMED"
    req.driver_name = "Sunil Sharma"
    req.driver_phone = "+919811223344"
    req.vehicle_model = "Maruti Suzuki Dzire (Silver)"
    req.vehicle_number = "KA-05-MM-4421"
    
    db.commit()
    db.refresh(req)
    return req

@router.post("/{transport_id}/cancel", response_model=TransportResponse)
def cancel_transport_booking(
    transport_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    req = db.query(TransportRequest).filter(
        TransportRequest.id == transport_id,
        TransportRequest.user_id == user_id
    ).first()
    if not req:
        raise HTTPException(status_code=404, detail="Transport request not found")
        
    req.status = "CANCELLED"
    db.commit()
    db.refresh(req)
    return req

@router.get("/requests", response_model=List[TransportResponse])
def get_user_transport_history(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    return db.query(TransportRequest).filter(
        TransportRequest.user_id == user_id
    ).order_by(TransportRequest.created_at.desc()).all()
