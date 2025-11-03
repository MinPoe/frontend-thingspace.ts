#!/usr/bin/env python3
import os, json, sys
import requests

CODACY_API_TOKEN = look on discord, ill send it. 
PROVIDER = "gh"            # gh|ghe|gl|gle|bb|bbe
ORG = "thingSpace-ts"              # e.g. my-org
REPO = "ThingSpace.ts"            # e.g. my-repo
LIMIT = 1000               # single-shot max; Codacy returns up to ~1000 per call

URL = f"https://app.codacy.com/api/v3/analysis/organizations/{PROVIDER}/{ORG}/repositories/{REPO}/issues/search"

if not CODACY_API_TOKEN:
    sys.exit("Set CODACY_API_TOKEN in your env")

# (Optional) tweak filters below, or leave empty {} for all
body = {
    # "levels": ["Error","Warning","Info"],
    # "categories": ["Security","CodeStyle"],
}

resp = requests.post(
    URL,
    params={"limit": str(LIMIT)},
    headers={"api-token": CODACY_API_TOKEN, "Content-Type": "application/json"},
    json=body,
    timeout=60,
)
resp.raise_for_status()
data = resp.json()

# Write a single big NDJSON file (easy for tools / editors)
out_path = "codacy_issues.ndjson"
with open(out_path, "w", encoding="utf-8") as f:
    for issue in data.get("data", []):
        f.write(json.dumps(issue, ensure_ascii=False) + "\n")

print(f"Wrote {len(data.get('data', []))} issues to {out_path}")
# If you need CSV instead, say the word and I’ll swap it in.
