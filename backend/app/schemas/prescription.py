from pydantic import BaseModel, Field, ConfigDict
from typing import List, Optional

class MedicineItem(BaseModel):
    name: str
    strength: Optional[str] = None
    formulation: Optional[str] = "Tablet"  # Tablet, Syrup, Capsule, Drops
    instructions: Optional[str] = None
    confidence: float = 0.85
    needs_manual_check: bool = True

class PrescriptionScanRequest(BaseModel):
    image_base64_or_url: str = Field(..., description="Uploaded prescription base64 or URL")
    optional_doctor_hint: Optional[str] = None

class PrescriptionResponse(BaseModel):
    id: int
    user_id: int
    image_url: str
    doctor_name: Optional[str]
    extracted_raw_text: str
    extracted_medicines: List[MedicineItem]
    confidence_score: float
    status: str
    medical_safety_warning: str = (
        "CRITICAL DISCLAIMER: This OCR transcription is provisional and may contain errors. "
        "Never modify dosages or substitute medications without direct confirmation from your treating physician or a licensed pharmacist."
    )

    model_config = ConfigDict(from_attributes=True)

class MedicineAlternative(BaseModel):
    brand_name: str
    active_ingredient: str
    strength: str
    dosage_form: str
    manufacturer: str
    approximate_price_inr: float
    is_same_salt: bool = True
    clinical_note: str

    model_config = ConfigDict(from_attributes=True)

class MedicineSearchResponse(BaseModel):
    searched_query: str
    matched_salt: str
    active_ingredients: List[str]
    primary_uses: List[str]
    common_dosage_form: str
    verified_equivalents: List[MedicineAlternative]
    safety_disclaimer: str = (
        "Informational only. Equivalent medicines contain the same active salt, but bio-equivalence, excipients, and release mechanisms vary. Always consult your doctor or chemist before changing brands."
    )

    model_config = ConfigDict(from_attributes=True)
