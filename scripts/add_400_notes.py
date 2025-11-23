import requests
import json
import uuid
import time
from typing import Dict, List, Any, Optional
from datetime import datetime

# Configuration - SET THESE VALUES BEFORE RUNNING
BEARER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY5MDk4ZmQ3NzgyOWY4YjFhOTdmZTAyZiIsImlhdCI6MTc2Mzk0MDg2NywiZXhwIjoxNzY0MDA5MjY3fQ.L9Pe9mttwBRT1ZNGSZwLz-0jEJrBKiQv_aYAINuKWDc"
WORKSPACE_ID = "69239a2a1927db3f78e8a159"
API_BASE_URL = "http://3.14.160.244/api/notes"
JSON_FILE_PATH = "500_notes.json"

# Rate limiting - add small delay between requests to avoid overwhelming the server
REQUEST_DELAY_SECONDS = 0.1

NUM_NOTES = 100


def generate_field_id() -> str:
    """Generate a unique field ID"""
    return str(uuid.uuid4())


def validate_datetime_format(date_str: str) -> bool:
    """
    Validate that datetime string is in ISO format that LocalDateTime.parse() can handle.
    Expected format: "YYYY-MM-DDTHH:MM:SS" (without timezone)
    """
    try:
        # LocalDateTime.parse() expects format like "2025-02-12T18:00:00"
        # Try parsing to ensure it's valid
        datetime.strptime(date_str, "%Y-%m-%dT%H:%M:%S")
        return True
    except (ValueError, AttributeError, TypeError):
        # If exact format fails, try with optional seconds
        try:
            datetime.strptime(date_str, "%Y-%m-%dT%H:%M")
            return True
        except (ValueError, AttributeError, TypeError):
            return False


def convert_note_to_api_format(note_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """
    Convert a note from JSON format to API request format.
    
    JSON format: {"title": "...", "description": "...", "tag": "...", "date": "..."}
    API format: {workspaceId, noteType, tags, fields}
    
    Returns None if required fields are missing or invalid.
    """
    fields: List[Dict[str, Any]] = []
    
    # Add title field (required)
    
    fields.append({
        "_id": generate_field_id(),
        "fieldType": "text",
        "label": "Title",
        "required": True,
        "placeholder": "Enter title",
        "content": str(note_data["title"])
    })
    
    # Add description field (required)

    fields.append({
        "_id": generate_field_id(),
        "fieldType": "text",
        "label": "Description",
        "required": False,
        "placeholder": "Enter description",
        "content": str(note_data["description"])
    })
    
    # Add datetime field if date is present and valid
    if "date" in note_data and note_data["date"]:
        date_str = str(note_data["date"])
        # Validate datetime format before adding
        if validate_datetime_format(date_str):
            fields.append({
                "_id": generate_field_id(),
                "fieldType": "datetime",
                "label": "Date",
                "required": False,
                "content": date_str
            })
        # If invalid format, skip datetime field but continue with note creation
    
    # Build tags array from the single tag field
    tags: List[str] = []
    tags.append(str(note_data["tag"]))
    
    # Build the API request
    api_request = {
        "workspaceId": WORKSPACE_ID,
        "noteType": "CONTENT",
        "tags": tags,
        "fields": fields
    }
    
    return api_request


def create_note(api_request: Dict[str, Any], note_index: int, total_notes: int) -> bool:
    """Create a single note via API"""
    headers = {
        "Authorization": f"Bearer {BEARER_TOKEN}",
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.post(API_BASE_URL, json=api_request, headers=headers, timeout=30)
        
        if response.status_code == 201:
            print(f"✓ Note {note_index + 1}/{total_notes} created successfully")
            return True
        else:
            print(f"✗ Note {note_index + 1}/{total_notes} failed: {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Note {note_index + 1}/{total_notes} error: {str(e)}")
        return False


def main():
    """Main function to load notes and create them via API"""
    # Validate configuration
    if not BEARER_TOKEN:
        print("ERROR: BEARER_TOKEN must be set before running this script")
        return
    
    if not WORKSPACE_ID:
        print("ERROR: WORKSPACE_ID must be set before running this script")
        return
    
    # Load notes from JSON file
    try:
        with open(JSON_FILE_PATH, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except FileNotFoundError:
        print(f"ERROR: Could not find JSON file at {JSON_FILE_PATH}")
        return
    except json.JSONDecodeError as e:
        print(f"ERROR: Invalid JSON in file: {e}")
        return
    
    notes = data.get("notes", [])
    if not notes:
        print("ERROR: No notes found in JSON file")
        return
    
    print(f"Loaded {len(notes)} notes from {JSON_FILE_PATH}")
    print(f"Using workspace ID: {WORKSPACE_ID}")
    print(f"API endpoint: {API_BASE_URL}")
    print("-" * 60)
    
    # Process each note
    success_count = 0
    failure_count = 0
    
    notes = notes[:NUM_NOTES]
    for index, note_data in enumerate(notes):
        # Skip number field (outdated, as per user request)
        if "number" in note_data:
            del note_data["number"]
        
        # Convert to API format
        api_request = convert_note_to_api_format(note_data)
        
        # Validate API request structure
        if api_request is None:
            print(f"Warning: Note {index + 1} has missing or invalid required fields (title, description, or tag), skipping")
            failure_count += 1
            continue
        
        if not api_request.get("fields") or len(api_request["fields"]) < 2:
            print(f"Warning: Note {index + 1} has insufficient fields, skipping")
            failure_count += 1
            continue
        
        # Create note via API
        if create_note(api_request, index, len(notes)):
            success_count += 1
        else:
            failure_count += 1
        
        # Rate limiting delay
        if index < len(notes) - 1:  # Don't delay after last request
            time.sleep(REQUEST_DELAY_SECONDS)
    
    # Summary
    print("-" * 60)
    print(f"Summary:")
    print(f"  Total notes processed: {len(notes)}")
    print(f"  Successfully created: {success_count}")
    print(f"  Failed: {failure_count}")


if __name__ == "__main__":
    main()
