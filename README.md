# Business Expense Tracker

Business Expense Tracker is a native Android application for recording, reviewing, and exporting business expenses. It is designed for a CFO, accountant, or finance operator who receives bills, invoices, receipts, and payment details, then needs a simple register for later review and reporting.

## Screenshots

| Dashboard | Add Expense | Export Menu |
| --- | --- | --- |
| ![Dashboard](docs/screenshots/dashboard.png) | ![Add expense](docs/screenshots/add-expense.png) | ![Export menu](docs/screenshots/export-menu.png) |

## Features

- Add, edit, and delete business expense records.
- Track vendor, amount, date, category, payment method, submitted-by, invoice number, notes, and status.
- Attach a bill, receipt, or invoice file using Android's document picker.
- View dashboard analytics for total spend, current month spend, paid amount, pending review count, and top category.
- Search and filter by vendor, invoice, notes, category, and status.
- Display all money values in INR.
- Export records as CSV for spreadsheets.
- Export a browser-readable HTML report that can be shared with anyone.
- Work offline with local device persistence.

## Expense Statuses

- Draft
- For review
- Approved
- Paid
- Rejected

## Export Formats

The app supports two export formats:

- CSV: best for Excel, Google Sheets, accounting imports, and further analysis.
- HTML report: best for sharing a readable report with owners, auditors, or stakeholders who do not have the app installed.

The HTML export includes summary cards and a full expense table. The CSV export includes raw INR amounts and all key expense fields.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android document picker
- SharedPreferences JSON persistence
- Gradle Kotlin DSL

## Requirements

- Android Studio with Android SDK installed
- JDK 11 or newer
- Android SDK 36.1, matching the current project configuration
- A device or emulator running Android 10 or newer

## Build

From the project root:

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Test And Lint

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug
```

Full verification command:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## Project Structure

```text
app/src/main/java/com/dps/businessexpensetracker/
  MainActivity.kt                 # Compose UI and Android file picker/export wiring
  data/
    ExpenseModels.kt              # Expense model, draft model, enums, validation
    ExpenseRepository.kt          # Local persistence
    ExpenseExporter.kt            # CSV and HTML export generation
  ui/theme/                       # Material theme

docs/
  requirements.md                 # Product requirements and assumptions
  screenshots/                    # README screenshots
```

## Current Data Model

Each expense stores:

- Vendor
- Amount in INR
- Category
- Payment method
- Expense date
- Accounting status
- Submitted by
- Invoice number
- Attachment URI and attachment name
- Notes
- Last updated timestamp

## Current Limitations

- Data is stored locally on one device.
- Attachments are referenced by Android document URI; files are not uploaded or copied into a backend.
- There is no authentication, cloud sync, role-based approval, OCR, or audit log yet.
- Exports include the saved attachment name and URI, but not the binary attachment file itself.

## Roadmap

- Move persistence from SharedPreferences to Room.
- Add cloud sync and role-based access for owner, CFO, accountant, and auditor.
- Add OCR extraction for vendor, invoice number, amount, date, and tax details.
- Add recurring expenses and reminders.
- Add monthly PDF reports.
- Add import/export backup workflows.

## Contact

For project ownership or business follow-up, use:

```text
tolstoyjustin@gmail.com
```
