import re
from typing import Dict, Any, Optional, Tuple, List
from app.schemas.assistant import VoiceCommandResponse, AllowlistedToolCall

class VoiceAssistantService:
    """
    Multilingual NLU intent parser and allowlisted tool router
    tailored for Indian senior citizens speaking Hindi, English, or Hinglish.
    Note: Strictly supports Hindi and English; other languages are politely declined.
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
        raw_text = transcript.strip().lower()
        # Clean punctuation for token checks
        text = re.sub(r'[^\w\s]', ' ', raw_text)
        tokens = text.split()

        # 1. Emergency / SOS / 112 (Highest priority, immediate dispatch)
        if any(w in text for w in ["sos", "emergency", "bachao", "madad", "help", "112", "khatra", "sahayata", "danger", "police"]):
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

        # 2. Unsupported Language Detection (e.g. Kannada requested)
        if any(w in text for w in ["kannada", "bengali", "marathi", "tamil", "telugu", "gujarati"]):
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback="Kshama karein, awaz sahayata keval Hindi aur English mein uplabdh hai.",
                intent="unsupported_language",
                confidence=0.99,
                tool_call=None,
                needs_clarification=True,
                clarification_options=[
                    "Hindi mein baat karein",
                    "Speak in English"
                ]
            )

        # 3. Negation & Cancellation Handling (Evaluated BEFORE positive action keywords)
        negation_terms = ["mat karo", "mat karna", "rok do", "band karo", "cancel", "don't", "dont", "abort", "wapas", "ruk jao", "rehne do"]
        is_negated = any(w in text for w in negation_terms)

        if is_negated:
            if any(w in text for w in ["cab", "taxi", "gaadi", "ride", "auto"]):
                return VoiceCommandResponse(
                    recognized_text=transcript,
                    spoken_feedback="Theek hai, cab booking radd kar di gayi hai.",
                    intent="cancel_action",
                    confidence=0.98,
                    tool_call=AllowlistedToolCall(
                        tool_name="navigate_to_screen",
                        parameters={"screen": "HOME"},
                        requires_confirmation=False
                    )
                )
            elif any(w in text for w in ["event", "satsang", "karyakram", "gathering"]):
                return VoiceCommandResponse(
                    recognized_text=transcript,
                    spoken_feedback="Theek hai, karyakram band kar diya gaya hai.",
                    intent="cancel_action",
                    confidence=0.98,
                    tool_call=AllowlistedToolCall(
                        tool_name="navigate_to_screen",
                        parameters={"screen": "HOME"},
                        requires_confirmation=False
                    )
                )
            else:
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

        # 4. Pure Rejection / Denial Responses (e.g. "nahi", "no")
        if any(w in tokens for w in ["nahi", "na", "no"]) and len(tokens) <= 3:
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback="Theek hai, anurodh radd kar diya gaya hai.",
                intent="cancel_action",
                confidence=0.98,
                tool_call=AllowlistedToolCall(
                    tool_name="navigate_to_screen",
                    parameters={"screen": "HOME"},
                    requires_confirmation=False
                )
            )

        # 5. Confirmation Responses (e.g. "haan", "yes", "confirm", "theek hai")
        is_confirmation = (
            any(w in tokens for w in ["haan", "ha", "yes", "confirm"]) or
            text in ["theek hai", "sahi hai", "kar do", "aage badho", "ok", "sure", "proceed"]
        ) and not any(w in text for w in ["cab", "ride", "taxi", "doctor", "satsang", "call", "prescription", "text", "size", "font", "akshar", "bhasha"])

        if is_confirmation:
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback="Theek hai, anurodh ki pushti ho gayi hai aur karya shuru kiya ja raha hai.",
                intent="confirm_action",
                confidence=0.97,
                tool_call=AllowlistedToolCall(
                    tool_name="navigate_to_screen",
                    parameters={"action": "CONFIRM_PENDING"},
                    requires_confirmation=False
                )
            )

        # 6. Prescription / Medicine Reading & Lookup (Evaluated before general doctor appointment)
        if any(w in text for w in ["prescription", "parchi", "dawai", "medicine", "tablet", "dawakhana", "pill", "syrup"]):
            if any(w in text for w in ["parchi", "prescription", "scan", "photo", "padh", "camera"]):
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
                        parameters={"query": transcript},
                        requires_confirmation=False
                    )
                )

        # 7. Ambulance (Immediate medical transport)
        if any(w in text for w in ["ambulance", "aspataal le jao", "hospital le jao"]):
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

        # 8. Cab / Taxi Booking (Requires two-step confirmation for elderly safety)
        if any(w in text for w in ["cab", "taxi", "gaadi", "auto", "ride", "rickshaw"]):
            dest = "Aas paas"
            if "mandir" in text or "temple" in text:
                dest = "Mandir"
            elif "hospital" in text or "doctor" in text or "clinic" in text:
                dest = "Doctor Clinic"
            elif "market" in text or "bazaar" in text or "dukaan" in text:
                dest = "Bazaar"
                
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback=f"{dest} ke liye cab booking shuru kar rahe hain. Kripya pushti karein.",
                intent="request_ride",
                confidence=0.95,
                tool_call=AllowlistedToolCall(
                    tool_name="request_ride",
                    parameters={"request_type": "CAB", "suggested_destination": dest},
                    requires_confirmation=True,
                    confirmation_prompt=f"Kya aap {dest} ke liye cab book karna chahte hain? Haan ya Na bole."
                )
            )

        # 9. Calling / Video Calling (Family / Contacts)
        if any(w in text for w in ["call", "video call", "baat karni", "phone lagao", "bulao", "dial", "ring"]):
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
            elif any(w in text for w in ["doctor", "dr"]):
                target = "Doctor"
                
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

        # 10. Events / Satsang / Walk / Yoga
        if any(w in text for w in ["satsang", "kirtan", "event", "gathering", "yoga", "walk", "tahalna", "program", "samaroh", "bajan", "community"]):
            category = "ALL"
            if "satsang" in text or "kirtan" in text or "bajan" in text:
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

        # 11. Doctor Appointment
        if any(w in text for w in ["doctor", "appointment", "clinic", "ilaaj", "checkup", "vaidya", "consultation"]):
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

        # 12. Accessibility Settings: Text Size
        if any(w in text for w in ["text size", "akshar", "bada kar do", "chhota kar do", "font", "larger font", "smaller font"]):
            is_larger = any(w in text for w in ["bada", "large", "increase", "mota", "bigger"])
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

        # 13. Accessibility Settings: Language Selection (Hindi and English only)
        if any(w in text for w in ["hindi", "english", "bhasha", "language"]):
            lang = "hi"
            lang_name = "Hindi"
            if "english" in text:
                lang = "en"
                lang_name = "English"
                
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

        # 14. Accessibility Settings: Theme (Dark / Light)
        if any(w in text for w in ["dark mode", "light mode", "theme", "raat", "din"]):
            mode = "dark" if any(w in text for w in ["dark", "raat"]) else "light"
            return VoiceCommandResponse(
                recognized_text=transcript,
                spoken_feedback=f"{'Dark' if mode == 'dark' else 'Light'} mode chalu kar diya gaya hai.",
                intent="change_accessibility_settings",
                confidence=0.96,
                tool_call=AllowlistedToolCall(
                    tool_name="change_accessibility_settings",
                    parameters={"theme_mode": mode},
                    requires_confirmation=False
                )
            )

        # 15. Fallback / Clarification needed
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
