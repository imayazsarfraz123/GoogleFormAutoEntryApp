# Google Form Auto Entry App v1.0

This is a buildable native Android project for GitHub Actions APK generation.

## Main features
- Google Form URL field
- CSV import from phone storage
- Mapping editor: `Google Form Question=CSV Column`
- WebView Google Form loading
- Auto-fill text/email/number/textarea fields
- Radio/checkbox selection by answer text
- Submit command
- Approve & Next / Reject / Stop workflow
- Google Form file chooser support when the user taps an upload field
- GitHub Actions workflow: `.github/workflows/android.yml`

## Important CSV format
Export Excel as CSV. First row must contain column names.

Example:
```csv
Name,CNIC,Phone,Address
Ali,3510212345671,03001234567,Lahore
```

## Mapping example
```text
Name=Name
CNIC=CNIC
Phone Number=Phone
Address=Address
```
Left side = exact or partial question text in Google Form.
Right side = CSV column name.

## GitHub APK build
1. Upload all extracted files/folders to a GitHub repository.
2. Open **Actions** tab.
3. Select **Build Android APK**.
4. Click **Run workflow**.
5. Download APK from **Artifacts**.

## Notes
Google Forms may change its internal HTML. Text fields and many radio/checkbox fields are supported. File upload cannot be filled silently due Android/browser security; when the Google Form upload field is tapped, this app opens the Android file picker and passes the selected file back to WebView.
