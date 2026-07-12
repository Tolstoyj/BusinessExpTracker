# Changelog

All notable changes to this project are documented here.

## 2.0.0 - 2026-07-13

### Added

- First-class daily sales ledger alongside expenses.
- Sales fields for customer, total, channel, collection status, payment method, salesperson, quantity, reference, tax, discount, and notes.
- Received, pending, and refunded sales lifecycle with quick actions, editing, duplication, search, filters, and sorting.
- Operating cashflow dashboard using received sales minus paid expenses, with today and monthly context.
- Sales CSV and HTML reports with spreadsheet-injection protection and escaped user content.
- Portable backup schema v2 containing sales while remaining compatible with v1 expense-only archives.
- Sales validation, persistence, export, backup, cashflow, and end-to-end Android tests.

## 1.0.0 - 2026-07-12

### Added

- On-device invoice scanning, OCR, barcode recognition, extraction confidence, and confirmation workflow.
- Local-first expense dashboard with search, filters, sorting, duplicate detection, status actions, and vendor defaults.
- Portable `.betbackup.zip` backups with attachments, automatic updates, validation, and merge or replace restore modes.
- CSV and browser-readable HTML exports.
- First-launch guided tour with an option to replay it later.
- Adaptive launcher icon with themed-icon support.
- Unit, instrumentation, backup round-trip, invoice-processing, and end-to-end journey tests.

### Improved

- Material 3 interface, accessibility labels, responsive layouts, validation messages, and unsaved-draft protection.
- Corrupted local-data recovery and safer attachment lifecycle handling.
