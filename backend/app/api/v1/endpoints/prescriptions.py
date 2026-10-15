from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import get_current_user_id
from app.models.prescription import PrescriptionRecord
from app.schemas.prescription import (
    PrescriptionScanRequest,
    PrescriptionResponse,
    MedicineItem,
    MedicineSearchResponse
)
from app.services.medicine_catalog import lookup_medicine

router = APIRouter()

@router.post("/scan", response_model=PrescriptionResponse)
def scan_prescription(
    payload: PrescriptionScanRequest,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    # Simulated OCR extraction and entity parsing
    # In production, this invokes Google Cloud Vision / ML Kit OCR
    raw_ocr_text = (
        "Rx: Dr. A. K. Sharma (MD Internal Med)\n"
        "Patient: Aadarniya Sadasya, Age 68\n"
        "1. Tab Metformin 500mg SR - 1 tab twice daily after meals\n"
        "2. Tab Amlodipine 5mg - 1 tab in the morning\n"
        "3. Tab Pan 40 - 1 tab before breakfast\n"
        "Follow up in 2 weeks."
    )
    
    extracted_items = [
        MedicineItem(
            name="Metformin 500mg SR",
            strength="500mg",
            formulation="Sustained Release Tablet",
            instructions="Twice daily after meals",
            confidence=0.92,
            needs_manual_check=False
        ),
        MedicineItem(
            name="Amlodipine 5mg",
            strength="5mg",
            formulation="Tablet",
            instructions="Once daily in morning",
            confidence=0.88,
            needs_manual_check=False
        ),
        MedicineItem(
            name="Pan 40 (Pantoprazole)",
            strength="40mg",
            formulation="Gastro-Resistant Tablet",
            instructions="Once daily before breakfast",
            confidence=0.90,
            needs_manual_check=False
        )
    ]
    
    record = PrescriptionRecord(
        user_id=user_id,
        image_url="https://storage.vyavahaar.app/prescriptions/sample_prescription.jpg",
        doctor_name="Dr. A. K. Sharma",
        date_prescribed="2026-10-15",
        extracted_raw_text=raw_ocr_text,
        extracted_medicines=[item.model_dump() for item in extracted_items],
        confidence_score=0.90,
        status="PENDING_REVIEW"
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    
    return PrescriptionResponse(
        id=record.id,
        user_id=record.user_id,
        image_url=record.image_url,
        doctor_name=record.doctor_name,
        extracted_raw_text=record.extracted_raw_text,
        extracted_medicines=extracted_items,
        confidence_score=record.confidence_score,
        status=record.status
    )

@router.get("/medicines/search", response_model=MedicineSearchResponse)
def search_medicine_information(
    q: str = Query(..., min_length=2, description="Medicine or salt name e.g. Metformin, Paracetamol, Dolo, Pan")
):
    result = lookup_medicine(q)
    if not result:
        raise HTTPException(
            status_code=404,
            detail=f"Dawai '{q}' ki satyapit jankaari nahi mili. Kripya apne doctor ya chemist se sampark karein."
        )
    return result
