# nOcnOm Android — Architecture

## Goal

A native Android client for the same nOcnOm product, not a second independent nutrition system.

## Source of truth

Web and Android must share the same Firebase identity and Firestore data contract.

### User state schema v2

- `users/{uid}/state/timetable`
- `users/{uid}/state/dishes`
- `users/{uid}/state/categories`
- `users/{uid}/state/logs`
- `users/{uid}/state/meta` with `schemaVersion = 2`

Profile/health settings:

- `users/{uid}/profile/main`

Legacy path (read/migration compatibility only):

- `users/{uid}/data/appState`

Android must not create an Android-only dish/macro database that can drift from Web.

## Layers

- `domain/model`: shared business entities mirrored from the Web contract.
- `domain/nutrition`: deterministic nutrition math.
- `data`: Firestore paths and repository boundary.
- `ui`: Compose screens and state rendering.
- `di`: Hilt wiring.

## Offline-first evolution

Initial bootstrap uses an in-memory fallback so the app opens before Firebase Android registration.
Next implementation step: Room cache + Firestore sync metadata + conflict policy.

## Planned native capabilities

1. Firebase Auth: Google + email/password.
2. Firestore realtime sync with schema v2.
3. Room cache for dishes/history/timetable.
4. WorkManager for background retry/sync.
5. Health Connect integration behind explicit user permission.
6. Notifications for meal plan reminders.

## Data integrity rules

- Never derive protein/carbs/fat from kcal when those fields are absent.
- Macro energy uses 4/4/9 kcal per gram.
- Macro consistency thresholds mirror Web: <=10% consistent; <=20% review; >20% inconsistent.
- User-entered nutrition must retain source/origin metadata.
- Daily calorie target is stored in `users/{uid}/profile/main.dailyCalorieTarget`.
