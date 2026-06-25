# Google Form Auto Entry App v1.0

Build APK with GitHub Actions: Actions -> Build Android APK -> Run workflow.

Usage:
1. Export Excel as CSV.
2. Open app, paste Google Form URL.
3. Load CSV.
4. Enter mappings, one per line: `Google Form Question=CSV Column`.
5. Open Form, Auto Fill, Submit, Approve Next.

Limit: Google Forms change internal HTML often; exact question labels must match visible form text.
