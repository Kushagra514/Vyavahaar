from datetime import datetime, timezone, timedelta
from app.core.database import engine, Base, SessionLocal
from app.models import (
    User,
    EmergencyContact,
    Event,
    EventRegistration,
    TransportRequest,
    DoctorAppointment,
    PrescriptionRecord,
    Contact,
    Message
)

def init_db():
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    
    # Check if events already exist
    if db.query(Event).count() == 0:
        now = datetime.now(timezone.utc)
        sample_events = [
            Event(
                title="Pratahkaal Satsang evam Bhajan",
                description="Sri Ram Mandir Hall mein pratahkaal Ramcharitmanas paath evam bhajan kirtan. Sabhi varishtha nagrik saadar aamantrit hain.",
                category="Satsang",
                language="hi",
                date_time=now + timedelta(days=1, hours=2),
                venue_name="Sri Ram Mandir Community Hall",
                address="12th Main, Indiranagar, Bengaluru",
                city="Bengaluru",
                is_online=False,
                organizer_name="Indiranagar Senior Citizen Satsang Samiti",
                is_organizer_verified=True,
                capacity=80
            ),
            Event(
                title="Senior Morning Walk & Laughter Yoga",
                description="Taazi hawa mein morning walk aur hassya yoga. Doston se miliye aur swasth rahiye.",
                category="Walking",
                language="hi",
                date_time=now + timedelta(days=1),
                venue_name="Cubbon Park (Gate 2)",
                address="Kasturba Road, Bengaluru",
                city="Bengaluru",
                is_online=False,
                organizer_name="Elder Care Companions",
                is_organizer_verified=True,
                capacity=40
            ),
            Event(
                title="Ghar Se Pranayama evam Dhyaan Session",
                description="Online Google Meet par yoga guru ke saath saral sukshma vyayam aur anulom-vilom pranayama.",
                category="Yoga",
                language="hi",
                date_time=now + timedelta(days=2, hours=4),
                venue_name="Online (Google Meet)",
                address="Virtual Event",
                city="Bengaluru",
                is_online=True,
                online_meeting_url="https://meet.google.com/vya-vaah-eld",
                organizer_name="Aayush Swasthya Kendra",
                is_organizer_verified=True,
                capacity=100
            ),
            Event(
                title="Nishulk Health Camp & BP/Sugar Checkup",
                description="Specialist geriatric doctors dwara nishulk blood pressure, sugar, aur bone density checkup.",
                category="HealthCamp",
                language="hi",
                date_time=now + timedelta(days=3, hours=5),
                venue_name="Sanjaynagar RWA Clubhouse",
                address="8th Cross, Sanjaynagar, Bengaluru",
                city="Bengaluru",
                is_online=False,
                organizer_name="Rotary Club Bengaluru Elders Health",
                is_organizer_verified=True,
                capacity=60
            )
        ]
        db.add_all(sample_events)
        db.commit()
        
    db.close()

if __name__ == "__main__":
    init_db()
    print("Database initialized and seeded successfully.")
