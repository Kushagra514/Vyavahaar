from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings
from app.db_init import init_db
from app.api.v1.endpoints import (
    auth,
    profile,
    assistant,
    events,
    transport,
    appointments,
    prescriptions,
    contacts,
)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize SQLite/PostgreSQL schemas and seed initial data
    init_db()
    yield

app = FastAPI(
    title=settings.PROJECT_NAME,
    description="Backend API for Vyavahaar — A Voice-First Elder Care and Social Network Application",
    version="1.0.0",
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    lifespan=lifespan
)

# CORS middleware configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
import os

@app.get("/", include_in_schema=False)
@app.get("/preview", include_in_schema=False)
def serve_preview():
    static_file = os.path.join(os.path.dirname(__file__), "static", "index.html")
    return FileResponse(static_file)

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "Vyavahaar API",
        "version": "1.0.0",
        "tagline": "Aapke Vyavahar Mein Hi Humara Pyaar Hai"
    }

# Register all API v1 routers
app.include_router(auth.router, prefix=f"{settings.API_V1_STR}/auth", tags=["Authentication"])
app.include_router(profile.router, prefix=f"{settings.API_V1_STR}/profile", tags=["Profile & Settings"])
app.include_router(assistant.router, prefix=f"{settings.API_V1_STR}/assistant", tags=["Voice Assistant"])
app.include_router(events.router, prefix=f"{settings.API_V1_STR}/events", tags=["Events & Community"])
app.include_router(transport.router, prefix=f"{settings.API_V1_STR}/transport", tags=["Transport & Mobility"])
app.include_router(appointments.router, prefix=f"{settings.API_V1_STR}/appointments", tags=["Doctor Appointments"])
app.include_router(prescriptions.router, prefix=f"{settings.API_V1_STR}/prescriptions", tags=["Prescriptions & Medicine Info"])
app.include_router(contacts.router, prefix=f"{settings.API_V1_STR}/contacts", tags=["Contacts & Messaging"])
