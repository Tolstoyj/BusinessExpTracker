# Business Expense Tracker Requirements

## Current MVP

The first version is a local-first Android app for the CFO/accountant workflow.

- Record business expenses from bills, invoices, receipts, and ad hoc spend.
- Record daily business sales and their collection status.
- Save customer or sale label, amount, date, channel, payment method, salesperson, quantity, reference, tax, discount, notes, and received/pending/refunded status.
- Scan or import an invoice image, extract core fields on-device, and require user verification before saving.
- Save vendor, amount, date, category, payment method, submitted-by, invoice number, notes, status, and an optional document attachment reference. Expense and sales forms mark required and optional fields and keep a text Save action visible while scrolling.
- Save optional supplier GSTIN and tax amount extracted from invoices.
- Support adding, editing, deleting, searching, and filtering expenses.
- Support sorting, duplicating a similar expense, duplicate-invoice detection, and quick status progression.
- Track accounting status: draft, for review, approved, paid, rejected.
- Show dashboard totals in the user-selected home currency for total spend, this month, pending review, paid amount, and top category.
- Show operating cashflow as received sales minus paid expenses, with today's sales, monthly cashflow, received revenue, and pending collections. The dashboard states that this figure is not accounting profit.
- Keep summary metrics and filter controls inside the phone width. Essential figures wrap or stack instead of requiring horizontal scrolling.
- Export the current filtered and sorted records as CSV or as a browser-readable HTML report.
- Persist records on the device so the data remains available after app restarts.
- Keep a previous valid local snapshot for recovery if the primary data becomes unreadable.
- Preserve existing records during same-package, same-signature APK updates and run idempotent schema migration before loading repositories.
- Retain a validated pre-migration snapshot when upgrading legacy v1 expense data.
- Create a portable ZIP backup containing the complete expense and sales registers plus readable expense attachment files.
- Restore both schema-v2 business backups and schema-v1 expense-only backups.
- Automatically refresh the user-selected backup after every transaction change when Android grants persistent file access.
- Restore on the same or another device using either merge-by-record-ID or full replacement, after explicit user confirmation.
- Require a language choice on first launch and support English, Simplified Chinese, Spanish, Arabic, Hindi, Brazilian Portuguese, French, German, Japanese, and Turkish.
- After language and privacy, a first-run user may enter an optional business name and must confirm the home currency before the dashboard. Installations that already finished language onboarding keep their saved currency, defaulting to INR, and are not sent through that step again.
- Show a compact dashboard quick-start checklist until the business profile, first sale, first expense, and backup file are all in place.
- Provide a persistent Settings screen for business identity, language, home currency, onboarding replay, backup visibility, and privacy information.
- Treat home-currency changes as display/report preferences only; never reinterpret or convert saved numeric values.

## Assumptions

- This version stores the live register locally and does not yet sync between multiple users.
- Attachments are stored as Android document URIs during normal use. The app keeps read access to selected files, copies scanned and restored files into app-private storage, and copies readable attachments into portable backups.
- Backups are user-controlled files and can contain sensitive financial data; the app does not upload them.
- Authentication, multi-company support, cloud backup, audit logs, and accountant approval permissions are future enhancements.

## Recommended Next Phase

- Add Room database storage once the schema stabilizes.
- Add cloud sync and role-based access for owner, CFO, accountant, and auditor users.
- Add export to CSV/PDF and monthly reports.
- Add recurring expenses and payment reminders.
