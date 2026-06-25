# Google Form Auto Entry App

Android WebView app for loading a Google Form, selecting a CSV file, mapping CSV columns to form questions, filling one row at a time, submitting, and moving to the next record.

## CSV format
Export your Excel file as CSV. Example:

```csv
CNIC,Name,Phone,Address
3520112345678,Ali Khan,03001234567,Lahore
```

## Field mapping inside app
Write one mapping per line:

```text
CNIC=CNIC
Name=Name
Mobile=Phone
Address=Address
```

Left side = Google Form question text.
Right side = CSV column name.

## GitHub APK build
1. Upload this full folder to a GitHub repository.
2. Open GitHub repo.
3. Go to **Actions**.
4. Select **Build Android APK**.
5. Press **Run workflow**.
6. Download APK from workflow **Artifacts**.

## Codemagic build command
Use this command:

```bash
gradle assembleDebug
```

Artifact path:

```text
app/build/outputs/**/*.apk
```
