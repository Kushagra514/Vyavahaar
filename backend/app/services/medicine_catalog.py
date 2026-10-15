from typing import Optional, Dict, Any, List
from app.schemas.prescription import MedicineSearchResponse, MedicineAlternative

# Authentic, traceable Indian essential medicines database with salt formulations
MEDICINE_CATALOG: Dict[str, Dict[str, Any]] = {
    "metformin": {
        "salt": "Metformin Hydrochloride",
        "active_ingredients": ["Metformin Hydrochloride 500mg"],
        "primary_uses": ["Type 2 Diabetes Mellitus glycemic control"],
        "common_dosage_form": "Sustained Release Tablet",
        "equivalents": [
            {
                "brand_name": "Glycomet 500 SR",
                "active_ingredient": "Metformin Hydrochloride",
                "strength": "500mg SR",
                "dosage_form": "Sustained Release Tablet",
                "manufacturer": "USV Pvt Ltd",
                "approximate_price_inr": 28.50,
                "is_same_salt": True,
                "clinical_note": "Widely prescribed generic sustained-release formulation."
            },
            {
                "brand_name": "Glyciphage 500 SR",
                "active_ingredient": "Metformin Hydrochloride",
                "strength": "500mg SR",
                "dosage_form": "Sustained Release Tablet",
                "manufacturer": "Franco-Indian Pharmaceuticals",
                "approximate_price_inr": 26.00,
                "is_same_salt": True,
                "clinical_note": "Equivalent active ingredient and release profile."
            },
            {
                "brand_name": "Janaushadhi Metformin 500",
                "active_ingredient": "Metformin Hydrochloride",
                "strength": "500mg",
                "dosage_form": "Tablet",
                "manufacturer": "PMBJP (Pradhan Mantri Bhartiya Janaushadhi Pariyojana)",
                "approximate_price_inr": 6.50,
                "is_same_salt": True,
                "clinical_note": "Government verified low-cost generic equivalent."
            }
        ]
    },
    "paracetamol": {
        "salt": "Paracetamol (Acetaminophen)",
        "active_ingredients": ["Paracetamol 650mg"],
        "primary_uses": ["Analgesic (Mild pain relief)", "Antipyretic (Fever reduction)"],
        "common_dosage_form": "Oral Tablet",
        "equivalents": [
            {
                "brand_name": "Dolo 650",
                "active_ingredient": "Paracetamol",
                "strength": "650mg",
                "dosage_form": "Tablet",
                "manufacturer": "Micro Labs Ltd",
                "approximate_price_inr": 34.00,
                "is_same_salt": True,
                "clinical_note": "Standard 650mg antipyretic."
            },
            {
                "brand_name": "Crocin 650",
                "active_ingredient": "Paracetamol",
                "strength": "650mg",
                "dosage_form": "Tablet",
                "manufacturer": "GlaxoSmithKline",
                "approximate_price_inr": 33.50,
                "is_same_salt": True,
                "clinical_note": "Equivalent active formulation."
            },
            {
                "brand_name": "Calpol 650",
                "active_ingredient": "Paracetamol",
                "strength": "650mg",
                "dosage_form": "Tablet",
                "manufacturer": "GSK Consumer",
                "approximate_price_inr": 32.00,
                "is_same_salt": True,
                "clinical_note": "Equivalent paracetamol formulation."
            }
        ]
    },
    "amlodipine": {
        "salt": "Amlodipine Besylate",
        "active_ingredients": ["Amlodipine Besylate 5mg"],
        "primary_uses": ["Hypertension (High Blood Pressure)", "Angina"],
        "common_dosage_form": "Oral Tablet",
        "equivalents": [
            {
                "brand_name": "Amlong 5",
                "active_ingredient": "Amlodipine Besylate",
                "strength": "5mg",
                "dosage_form": "Tablet",
                "manufacturer": "Micro Labs Ltd",
                "approximate_price_inr": 42.00,
                "is_same_salt": True,
                "clinical_note": "Standard calcium channel blocker."
            },
            {
                "brand_name": "Stamlo 5",
                "active_ingredient": "Amlodipine Besylate",
                "strength": "5mg",
                "dosage_form": "Tablet",
                "manufacturer": "Dr. Reddy's Laboratories",
                "approximate_price_inr": 45.00,
                "is_same_salt": True,
                "clinical_note": "Equivalent brand with identical active strength."
            }
        ]
    },
    "pantoprazole": {
        "salt": "Pantoprazole Sodium Gastro-Resistant",
        "active_ingredients": ["Pantoprazole Sodium 40mg"],
        "primary_uses": ["Acid Reflux", "GERD", "Gastric Ulcer Protection"],
        "common_dosage_form": "Gastro-Resistant Tablet",
        "equivalents": [
            {
                "brand_name": "Pan 40",
                "active_ingredient": "Pantoprazole Sodium",
                "strength": "40mg",
                "dosage_form": "Enteric Coated Tablet",
                "manufacturer": "Alkem Laboratories",
                "approximate_price_inr": 155.00,
                "is_same_salt": True,
                "clinical_note": "Proton pump inhibitor; take on empty stomach."
            },
            {
                "brand_name": "Pantocid 40",
                "active_ingredient": "Pantoprazole Sodium",
                "strength": "40mg",
                "dosage_form": "Enteric Coated Tablet",
                "manufacturer": "Sun Pharmaceutical Industries",
                "approximate_price_inr": 150.00,
                "is_same_salt": True,
                "clinical_note": "Equivalent active ingredient and release mechanism."
            }
        ]
    }
}

def lookup_medicine(query: str) -> Optional[MedicineSearchResponse]:
    q_norm = query.lower().strip()
    
    # Check exact or partial matches
    matched_key = None
    for key in MEDICINE_CATALOG.keys():
        if key in q_norm or q_norm in key:
            matched_key = key
            break
            
    # Also check brand names
    if not matched_key:
        for key, entry in MEDICINE_CATALOG.items():
            for eq in entry["equivalents"]:
                if eq["brand_name"].lower() in q_norm or q_norm in eq["brand_name"].lower():
                    matched_key = key
                    break
            if matched_key:
                break
                
    if not matched_key:
        return None
        
    entry = MEDICINE_CATALOG[matched_key]
    equivalents = [MedicineAlternative(**eq) for eq in entry["equivalents"]]
    
    return MedicineSearchResponse(
        searched_query=query,
        matched_salt=entry["salt"],
        active_ingredients=entry["active_ingredients"],
        primary_uses=entry["primary_uses"],
        common_dosage_form=entry["common_dosage_form"],
        verified_equivalents=equivalents
    )
