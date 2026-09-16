# nOcnOm Android

Native Android client for **nOcnOm**.

- Application ID: `vn.ivsjsc.nocnom`
- Kotlin + Jetpack Compose + Material 3
- Hilt
- Firebase Authentication + Firestore
- Firestore schema v2 compatible with `ivsjsc/nocnom`
- Android SDK 36
- Gradle 9.5.1 / AGP 9.2.1 / Compose Kotlin plugin 2.3.20

## Current scope

- Native authentication gate
- Email/password sign-in and account creation
- Google Sign-In -> Firebase Authentication
- Password reset and sign out
- In-app account + Firestore data deletion
- Realtime Firestore synchronization for profile, timetable, dishes, categories and eating logs
- Home calorie dashboard calculated from the current Vietnam calendar day only
- Macro summary calculated from the current day only
- Three-meal daily plan backed by the shared Web timetable contract
- “Không ăn buổi trưa” / skip-meal state persisted to Firestore
- Change-dish flow with search and context-aware ranking
- Daily meal rotation using the same date marker/version contract as Web
- Recommendation modes: Balanced / Budget / Variety / Quick
- Per-meal budget preference shared with Web
- Mark-meal-as-eaten flow persisted to shared eating logs
- Dish library + search
- Vendor/price data parsing from shared dish records
- Health/BMI screen
- Eating history grouped by Vietnam date
- Account/profile editor and daily kcal target
- Firestore schema v2 compatibility
- Deterministic macro 4/4/9 calculations + tests

Authenticated production flow does not silently expose demo dishes when Firestore data is empty.

## Shared data contract

Android and Web use the same documents:

- `users/{uid}/profile/main`
- `users/{uid}/state/timetable` -> `value`
- `users/{uid}/state/dishes` -> `items`
- `users/{uid}/state/categories` -> `items`
- `users/{uid}/state/logs` -> `items`
- `users/{uid}/state/meta`

The timetable keeps `suggestionDate` and `suggestionVersion` so a daily automatic rotation does not overwrite a user's manual selection again during the same Vietnam calendar day.

## Firebase

See `docs/FIREBASE_SETUP.md`.

GitHub Actions restores `app/google-services.json` from the `GOOGLE_SERVICES_JSON_BASE64` repository secret and validates both Firebase project `cocoa-35632` and package `vn.ivsjsc.nocnom` before building.

Google Sign-In additionally requires the Android signing certificate SHA-1/SHA-256 to be registered in Firebase. For Play builds, register the Upload Key fingerprints for testing and later the Google Play App Signing certificate fingerprints.

## Build

CI installs Gradle 9.5.1 directly.

Local:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

Or import the project in Android Studio and configure Gradle 9.5.1.

## Google Play release

- Guided upload-key helper: `scripts/create-upload-key.ps1`
- Release runbook: `docs/PLAY_STORE_RELEASE.md`
- Play Console content/Data Safety working sheet: `docs/PLAY_CONSOLE_SUBMISSION.md`
- Release workflow: `.github/workflows/android-release.yml`

The release workflow requires:

- `GOOGLE_SERVICES_JSON_BASE64`
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

It builds and verifies a signed `app-release.aab` for Google Play.

## Repository

`https://github.com/ivsjsc/nocnom-android`

## Data integrity

Android follows the same nOcnOm data contract as Web. Macro values are never inferred from kcal if protein/carbs/fat are missing. Android appends/replaces the current meal log without rewriting unknown fields from historical Web records.
