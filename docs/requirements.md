# Business Expense Tracker Requirements

## Current MVP

The first version is a local-first Android app for the CFO/accountant workflow.

- Record business expenses from bills, invoices, receipts, and ad hoc spend.
- Scan or import an invoice image, extract core fields on-device, and require user verification before saving.
- Save vendor, amount, date, category, payment method, submitted-by, invoice number, notes, status, and an optional document attachment reference.
- Save optional supplier GSTIN and tax amount extracted from invoices.
- Support adding, editing, deleting, searching, and filtering expenses.
- Support sorting, duplicating a similar expense, duplicate-invoice detection, and quick status progression.
- Track accounting status: draft, for review, approved, paid, rejected.
- Show dashboard totals in INR for total spend, this month, pending review, paid amount, and top category.
- Export the current filtered and sorted records as CSV or as a browser-readable HTML report.
- Persist records on the device so the data remains available after app restarts.
- Keep a previous valid local snapshot for recovery if the primary data becomes unreadable.
- Create a portable ZIP backup containing the complete expense register and readable attachment files.
- Automatically refresh the user-selected backup after every transaction change when Android grants persistent file access.
- Restore on the same or another device using either merge-by-record-ID or full replacement, after explicit user confirmation.

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
