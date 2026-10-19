from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List

class VoiceCommandRequest(BaseModel):
    transcript: str = Field(..., min_length=1, json_schema_extra={"example": "Mujhe satsang dikhao"})
    detected_language: Optional[str] = "hi"
    context_screen: Optional[str] = "HOME"

class AllowlistedToolCall(BaseModel):
    tool_name: str
    parameters: Dict[str, Any] = {}
    requires_confirmation: bool = False
    confirmation_prompt: Optional[str] = None

class VoiceCommandResponse(BaseModel):
    recognized_text: str
    spoken_feedback: str  # Text to read aloud via TTS in user's language
    intent: str
    confidence: float
    tool_call: Optional[AllowlistedToolCall] = None
    needs_clarification: bool = False
    clarification_options: List[str] = []
