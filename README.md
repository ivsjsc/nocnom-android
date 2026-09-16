# nOcnOm Android

Native Android client for **nOcnOm**.

- Application ID: `vn.ivsjsc.nocnom`
- Kotlin + Jetpack Compose + Material 3
- Hilt
- Firebase Auth / Firestore contract compatible with `ivsjsc/nocnom`
- Android SDK 36
- Gradle 9.5.1 / AGP 9.2.1 / Compose Kotlin plugin 2.3.20

## Current bootstrap scope

The project establishes the Android architecture and native UX shell for:

- Home calorie dashboard
- Macro summary
- Meal plan including “Không ăn buổi trưa”
- Dish library + search
- Health dashboard
- Eating history grouped by day
- Account/profile calorie target
- Firestore schema v2 compatibility
- Deterministic macro 4/4/9 calculations + tests

The app falls back to demo state until the Android Firebase app is registered and `app/google-services.json` is present.

## Firebase

See `docs/FIREBASE_SETUP.md`.

## Build

CI installs Gradle 9.5.1 directly.

Local:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

Or import the project in Android Studio and configure Gradle 9.5.1.

## Repository target

`https://github.com/ivsjsc/nocnom-android`

## Data integrity

Android must follow the same nOcnOm data contract as Web. In particular, macro values are never inferred from kcal if protein/carbs/fat are missing.
