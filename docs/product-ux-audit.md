# Product and UX audit — international release

## What was reviewed

The running Android app was reviewed as a small-business owner would use it: first launch, dashboard comprehension, expense capture, sales capture, search/filtering, exports, backup/restore, and recovery of settings.

## Changes included in this update

1. **Language-first onboarding.** A new install starts with a native-name language list, followed by a short local-data privacy explanation, then an optional business name and an explicit home-currency confirmation. Installations that already completed language onboarding keep their saved defaults and are not shown the new step. The existing guided feature tour follows onboarding.
2. **Ten launch languages.** English, Simplified Chinese, Spanish, Arabic, Hindi, Brazilian Portuguese, French, German, Japanese, and Turkish are selectable. Arabic switches the Compose layout to RTL.
3. **Persistent Settings screen.** Business name, language, home currency, tour replay, local-storage explanation, and current automatic-backup status are now grouped in one predictable place.
4. **Business identity.** An optional business name replaces the generic dashboard title.
5. **Home currency.** INR remains the safe default for existing users; USD, EUR, GBP, TRY, AED, SAR, CNY, JPY, and BRL are available. Currency selection changes display and report labelling but deliberately does not convert historical numeric values.
6. **Purpose-built launcher icon.** The receipt, ledger, cashflow arrow, and chart mark is currency-neutral and works across the selected markets.

## Highest-priority product gaps found

### P0 — protect trust and prevent accounting mistakes

- **Cashflow is not profit.** The dashboard now states that operating cashflow is received sales minus paid expenses and is not accounting profit. Inventory cost, depreciation, and other accounting adjustments are still not modeled.
- **Currency changes need guardrails.** The implemented warning prevents the most dangerous misunderstanding. A future true multi-currency mode must store the currency on every transaction and never reinterpret old amounts.
- **Backup confidence is too implicit.** Show “Last successful backup”, destination, attachment coverage, and a warning when a configured backup becomes unwritable.
- **Restore should offer a preview.** Before merge/replace, show date range, business name, currency, duplicate count, and conflicts—not only record totals.

### P1 — help an owner understand the business

- Add date-range controls for Today, This week, This month, Quarter, Year, and Custom.
- Add a monthly trend view for sales, expenses, cashflow, and pending collections.
- Add receivables aging: due now, 1–30 days, 31–60 days, and over 60 days.
- Add vendor, customer, category, channel, payment-method, and salesperson breakdowns.
- Add a simple owner summary: revenue, paid expenses, expected collections, largest cost movement, and comparison with the prior period.
- Add transaction currency to the data model before advertising real multi-currency accounting.

### P2 — reduce repetitive work

- Recurring expenses and scheduled reminders.
- Saved vendors/customers and faster repeat entry.
- Custom categories, payment methods, tax labels, and financial-year start in Settings.
- Bulk selection for status changes, export, and deletion.
- Import from CSV with preview, validation, and reversible error reporting.
- Optional invoice due date and payment due date.

## UX observations

- Dashboard summary metrics use a two-column grid, and filter controls use full-width buttons, so essential figures stay on screen at normal phone width.
- Expense and sales forms label required and optional fields and keep a text Save action in a bottom bar. Leaving the form with unsaved changes still asks for confirmation.
- An empty business sees a quick-start checklist for profile, first sale, first expense, and backup file. It checks items off as they are completed and collapses when all four are done.
- New interface copy is translated for German and Turkish. Other languages keep the English text until a reviewed translation exists.
- Expense capture offers three clear routes, but “Scan invoice” and “Import invoice image” overlap conceptually. The copy should clarify camera scan versus existing image.
- Filters use internal enum labels. They need the same localization layer as screen copy and should expose an active-filter count.
- The sales and expense forms are long. Group fields into Essentials, Payment and tax, and More details, with optional sections collapsed initially.
- Destructive actions use confirmation dialogs, which is correct, but a short undo snackbar is safer and faster for routine mistakes.
- Empty states are useful but should include one short example transaction so first-time owners understand the expected level of detail.

## Recommended next release sequence

1. Complete translation QA with native reviewers and screenshot tests for long German strings, CJK glyphs, Hindi shaping, and Arabic RTL.
2. Add backup health and last-success status.
3. Add date-range analytics and period comparison.
4. Add configurable categories, tax terminology, and financial-year settings.
5. Migrate persistence to Room before recurring items, bulk edits, and transaction-level currencies.

## Acceptance checks for this update

- A clean install cannot reach the dashboard without selecting a language and confirming the home currency. The business name may be left blank.
- An installation that already completed language onboarding opens the dashboard with its saved currency, or INR when none was saved.
- Language can be changed later without deleting business data.
- Arabic mirrors navigation and layout direction.
- Business name and home currency survive relaunch.
- Dashboard and exported reports use the selected home-currency code.
- Existing installations continue to default to INR.
- Changing currency never mutates stored amounts.
- The guided tour can be replayed from Settings.
- Launcher icon renders in legacy, round, adaptive, and themed-icon contexts.
