# Implementation status

## Phase 1 — Native Android foundation

Status: implemented on `main`.

- Kotlin + Jetpack Compose + Material 3
- Hilt dependency injection
- nOcnOm Firestore schema v2 contract
- Home kcal + macro dashboard
- Meal plan including skip-lunch UX
- Dish library/search
- Health/BMI screen
- Eating history
- Profile daily kcal target
- Nutrition 4/4/9 consistency logic and unit tests
- Android CI producing a debug APK artifact

## Pending Firebase activation

The app intentionally runs with demo fallback until Firebase Android app `vn.ivsjsc.nocnom` is registered in project `cocoa-35632` and `app/google-services.json` is provided locally/through CI configuration.

Next phase: Firebase Auth, realtime Firestore sync, Room cache, WorkManager retry/sync, then Health Connect and notifications.
