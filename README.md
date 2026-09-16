# nOcnOm Android

Native Android client for **nOcnOm**.

- Application ID: `vn.ivsjsc.nocnom`
- Kotlin + Jetpack Compose + Material 3
- Hilt
- Firebase Authentication + Firestore
- Firestore contract compatible with `ivsjsc/nocnom`
- Android SDK 36
- Gradle 9.5.1 / AGP 9.2.1 / Compose Kotlin plugin 2.3.20

## Current scope

- Native authentication gate
- Email/password sign-in and account creation
- Google Sign-In -> Firebase Authentication
- Password reset
- Sign out
- In-app account + Firestore data deletion
- Home calorie dashboard
- Macro summary
- Meal plan including “Không ăn buổi trưa”
- Dish library + search
- Health dashboard
- Eating history grouped by day
- Account/profile calorie target
- Firestore schema v2 compatibility
- Deterministic macro 4/4/9 calculations + tests

The production flow does not silently expose demo data when no Firebase user is authenticated.

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

Android follows the same nOcnOm data contract as Web. Macro values are never inferred from kcal if protein/carbs/fat are missing.
