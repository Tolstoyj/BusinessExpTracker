# Business Expense Tracker Requirements

## Current MVP

The first version is a local-first Android app for the CFO/accountant workflow.

- Record business expenses from bills, invoices, receipts, and ad hoc spend.
- Save vendor, amount, date, category, payment method, submitted-by, invoice number, notes, status, and an optional document attachment reference.
- Support adding, editing, deleting, searching, and filtering expenses.
- Track accounting status: draft, for review, approved, paid, rejected.
- Show dashboard totals in INR for total spend, this month, pending review, paid amount, and top category.
- Export all records as CSV for spreadsheets or as a browser-readable HTML report.
- Persist records on the device so the data remains available after app restarts.

## Assumptions

- This version stores data locally on the device and does not yet sync between multiple users.
- Attachments are stored as Android document URIs. The app keeps read access to the chosen file, but it does not upload or copy the file.
- Authentication, multi-company support, cloud backup, OCR, audit logs, and accountant approval permissions are future enhancements.

## Recommended Next Phase

- Add Room database storage once the schema stabilizes.
- Add cloud sync and role-based access for owner, CFO, accountant, and auditor users.
- Add OCR extraction for invoice amount, vendor, invoice number, GST/tax number, and date.
- Add export to CSV/PDF and monthly reports.
- Add recurring expenses and payment reminders.
