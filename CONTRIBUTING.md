# Contributing

Thanks for helping improve Business Expense Tracker.

## Development setup

1. Install Android Studio, JDK 11 or newer, and Android SDK 36.1.
2. Clone the repository and open its root directory in Android Studio.
3. Let Gradle sync, then run `./gradlew :app:assembleDebug`.
4. Use an Android 10 or newer emulator or physical device for UI tests.

## Before opening a pull request

Run:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

If your change affects a user journey, also run the instrumentation suite on an emulator:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Keep pull requests focused, explain the user impact, and include or update tests for changed behavior. Never commit invoices, backup archives, signing keys, credentials, or other private financial data.

By participating, you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).
