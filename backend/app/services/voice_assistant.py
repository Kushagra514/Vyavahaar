import re
from typing import Dict, Any, Optional, Tuple, List
from app.schemas.assistant import VoiceCommandResponse, AllowlistedToolCall

class VoiceAssistantService:
    """
    Multilingual NLU intent parser and allowlisted tool router
    tailored for Indian senior citizens speaking Hindi, English, Kannada, or Hinglish.
    """

    SUPPORTED_TOOLS = [
        "navigate_to_screen",
        "find_contact",
        "start_call",
        "search_events",
        "join_event",
        "request_ride",
        "schedule_appointment",
        "scan_prescription",
        "search_medicine_information",
        "change_accessibility_settings",
        "send_sos_alert"
    ]

    def process_command(self, transcript: str, detected_language: str = "hi") -> VoiceCommandResponse:
        text = transcript.strip().lower()
        
        # 1. Emergency / SOS / 112 (Highest priority, immediate dispatch)
        if any(w in text for w in ["sos", "emergency", "bachao", "madad", "help", "112", "khatra", "sahayata"]):
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback="Aapka SOS alert khola ja raha hai. Direct 112 call aur parivar ko sandesh taiyaar hai.",
                intent="send_sos_alert",
                confidence=0.99,
                tool_call=AllowlistedToolCall(
                    tool_name="send_sos_alert",
                    parameters={"emergency_type": "GENERAL_SOS", "trigger_112": True},
                    requires_confirmation=False
                )
            )

        # 2. Ambulance
        if any(w in text for w in ["ambulance", "aspataal", "hospital le jao"]):
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback="Ambulance sahayata screen kholi ja rahi hai. 112 emergency dialing uplabdh hai.",
                intent="request_ride",
                confidence=0.98,
                tool_call=AllowlistedToolCall(
                    tool_name="request_ride",
                    parameters={"request_type": "AMBULANCE", "immediate_dialer": True},
                    requires_confirmation=False
                )
            )

        # 3. Cab Booking
        if any(w in text for w in ["cab", "taxi", "gaadi", "auto", "ride", "rickshaw"]):
            # Extract destination if specified e.g. "mandir ke liye cab book karo"
            dest = "Aas paas"
            if "mandir" in text:
                dest = "Mandir"
            elif "hospital" in text or "doctor" in text:
                dest = "Doctor Clinic"
            elif "market" in text or "bazaar" in text:
                dest = "Bazaar"
                
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback=f"{dest} ke liye cab booking shuru kar rahe hain. Kripya pickup address dekhein.",
                intent="request_ride",
                confidence=0.95,
                tool_call=AllowlistedToolCall(
                    tool_name="request_ride",
                    parameters={"request_type": "CAB", "suggested_destination": dest},
                    requires_confirmation=True,
                    confirmation_prompt=f"Kya aap {dest} ke liye cab book karna chahte hain?"
                )
            )

        # 4. Calling / Video Calling (Family / Friends)
        if any(w in text for w in ["call", "video call", "baat karni", "phone lagao", "bulao"]):
            is_video = "video" in text
            target = "contact"
            if any(w in text for w in ["beta", "bete", "son"]):
                target = "Beta"
            elif any(w in text for w in ["beti", "daughter"]):
                target = "Beti"
            elif any(w in text for w in ["pota", "poti", "grandchild"]):
                target = "Pota"
            elif any(w in text for w in ["friend", "dost", "mitra"]):
                target = "Dost"
                
            feedback = f"{target} ko {'video call' if is_video else 'voice call'} lagayi ja rahi hai."
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback=feedback,
                intent="start_call",
                confidence=0.94,
                tool_call=AllowlistedToolCall(
                    tool_name="start_call",
                    parameters={"target_name": target, "is_video": is_video},
                    requires_confirmation=False
                )
            )

        # 5. Events / Satsang / Walk / Yoga
        if any(w in text for w in ["satsang", "kirtan", "event", "gathering", "yoga", "walk", "tahalna", "program", "samaroh"]):
            category = "ALL"
            if "satsang" in text or "kirtan" in text:
                category = "Satsang"
            elif "yoga" in text:
                category = "Yoga"
            elif "walk" in text or "tahalna" in text:
                category = "Walking"
            elif "health" in text or "camp" in text:
                category = "HealthCamp"

            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback=f"Aapke aas paas ke {category if category != 'ALL' else ''} karyakram dikhaye ja rahe hain.",
                intent="search_events",
                confidence=0.96,
                tool_call=AllowlistedToolCall(
                    tool_name="search_events",
                    parameters={"category": category},
                    requires_confirmation=False
                )
            )

        # 6. Doctor Appointment
        if any(w in text for w in ["doctor", "appointment", "clinic", "ilaaj", "checkup", "vaidya"]):
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback="Doctor appointment booking screen khol di gayi hai.",
                intent="schedule_appointment",
                confidence=0.93,
                tool_call=AllowlistedToolCall(
                    tool_name="schedule_appointment",
                    parameters={"action": "OPEN_SCHEDULER"},
                    requires_confirmation=False
                )
            )

        # 7. Prescription / Medicine Reading
        if any(w in text for w in ["prescription", "parchi", "dawai", "medicine", "tablet", "dawakhana"]):
            if any(w in text for w in ["parchi", "prescription", "scan", "photo", "padh"]):
                return VoiceCommandResponse(
                    recognized_text=transcript,
                    spoken_feedback="Prescription scan karne ke liye camera khol rahe hain.",
                    intent="scan_prescription",
                    confidence=0.95,
                    tool_call=AllowlistedToolCall(
                        tool_name="scan_prescription",
                        parameters={"mode": "CAMERA_CAPTURE"},
                        requires_confirmation=False
                    )
                )
            else:
                return VoiceCommandResponse(
                    recognized_text=transcript,
                    spoken_feedback="Dawai jankaari khoj rahe hain.",
                    intent="search_medicine_information",
                    confidence=0.90,
                    tool_call=AllowlistedToolCall(
                        tool_name="search_medicine_information",
                        parameters={"query": text},
                        requires_confirmation=False
                    )
                )

        # 8. Accessibility Settings (Text size, language, dark mode)
        if any(w in text for w in ["text size", "akshar", "bada kar do", "chhota kar do", "font"]):
            is_larger = any(w in text for w in ["bada", "large", "increase", "mota"])
            new_scale = 1.4 if is_larger else 1.0
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback=f"Font size {'bada' if is_larger else 'samanya'} kar diya gaya hai.",
                intent="change_accessibility_settings",
                confidence=0.95,
                tool_call=AllowlistedToolCall(
                    tool_name="change_accessibility_settings",
                    parameters={"font_scale": new_scale},
                    requires_confirmation=False
                )
            )

        if any(w in text for w in ["hindi", "english", "kannada", "bhasha", "language"]):
            lang = "hi"
            lang_name = "Hindi"
            if "english" in text:
                lang = "en"
                lang_name = "English"
            elif "kannada" in text:
                lang = "kn"
                lang_name = "Kannada"
                
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback=f"Bhasha {lang_name} mein badal di gayi hai.",
                intent="change_accessibility_settings",
                confidence=0.97,
                tool_call=AllowlistedToolCall(
                    tool_name="change_accessibility_settings",
                    parameters={"preferred_language": lang},
                    requires_confirmation=False
                )
            )

        if any(w in text for w in ["dark mode", "light mode", "theme", "raat", "din"]):
            mode = "dark" if any(w in text for w in ["dark", "raat"]) else "light"
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback=f"{'Dark' if mode == 'dark' else 'Light'} mode chalu kar diya gaya hai.",
                intent="change_accessibility_settings",
                parameters={"theme_mode": mode},
                tool_call=AllowlistedToolCall(
                    tool_name="change_accessibility_settings",
                    parameters={"theme_mode": mode},
                    requires_confirmation=False
                )
            )

        # 9. Cancellation
        if any(w in text for w in ["cancel", "rok do", "band karo", "wapas", "back", "nahi"]):
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback="Thik hai, karya radd kar diya gaya hai. Main aapki kya madad kar sakta hoon?",
                intent="cancel_action",
                confidence=0.98,
                tool_call=AllowlistedToolCall(
                    tool_name="navigate_to_screen",
                    parameters={"screen": "HOME"},
                    requires_confirmation=False
                )
            )

        # Fallback / Clarification needed
        return VoiceCommandResponse(
            recognized_text=transcript,
            spoken_feedback="Mujhe samajh nahi aaya. Kya aap Satsang dekhna chahte hain, Cab book karna chahte hain, ya Call karna chahte hain?",
            intent="unknown",
            confidence=0.35,
            needs_clarification=True,
            clarification_options=[
                "Satsang aur events dekhein",
                "Cab book karein",
                "Bete ko call karein",
                "Emergency SOS"
            ]
        )

voice_assistant_service = VoiceAssistantService()
