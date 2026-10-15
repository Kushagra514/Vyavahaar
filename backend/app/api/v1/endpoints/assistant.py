from fastapi import APIRouter, Depends
from typing import List
from app.core.security import get_current_user_id
from app.schemas.assistant import VoiceCommandRequest, VoiceCommandResponse
from app.services.voice_assistant import voice_assistant_service

router = APIRouter()

@router.post("/process-command", response_model=VoiceCommandResponse)
def process_voice_command(
    payload: VoiceCommandRequest,
    user_id: int = Depends(get_current_user_id)
):
    return voice_assistant_service.process_command(
        transcript=payload.transcript,
        detected_language=payload.detected_language or "hi"
    )

@router.get("/supported-tools", response_model=List[str])
def list_supported_tools():
    return voice_assistant_service.SUPPORTED_TOOLS
