# Implementation status

## Native Android parity baseline

Status: implemented on `main`.

### Authentication
- Email/password sign-in and account creation
- Google Sign-In -> Firebase Authentication
- Password reset
- Sign out
- Account + Firestore data deletion

### Shared Firestore data
- Schema v2 profile/timetable/dishes/categories/logs contract
- Realtime listeners for all user-facing shared domains
- Web `timetable.value` compatibility
- Web `dishes/categories/logs.items` compatibility
- Vendor and nutrition field parsing
- Android writes use the same documents as Web

### Daily meal workflow
- Three meals backed by actual timetable data
- Persisted skip-meal / “Không ăn buổi trưa”
- Change-dish search
- Daily Vietnam-date suggestion refresh
- Preserve skipped meals and meals already eaten
- Mark selected meal as eaten
- Eating log write preserves unknown fields from existing Web records

### Recommendation parity
- Shared profile fields `recommendationMode` and `mealBudgetVnd`
- Balanced / Budget / Variety / Quick modes
- Context-aware dish ranking using calories, preference/history, budget, convenience and variety signals
- Recommendation settings live under Account, not Dashboard

### Nutrition and health
- Dashboard calories and macros filter to the current Vietnam calendar date
- Profile daily kcal target
- BMI/health summary
- Macro 4/4/9 consistency rule; missing macros are not inferred from kcal

### Release
- Android CI
- Signed AAB release workflow
- Play Console documentation and public policy URLs

## Next parity work

The shared architecture is now in place. Future Web features should first extend the shared Firestore/domain contract and then add platform-specific UI. Rich Web-only dish authoring flows (nutrition lookup/image discovery/advanced vendor editing) should be ported as separate Android UX rather than duplicating browser-specific implementation.
