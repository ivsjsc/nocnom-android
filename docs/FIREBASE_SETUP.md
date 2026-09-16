# Firebase Android setup

The Web app currently uses Firebase project `cocoa-35632`.

## One-time setup

1. In Firebase Console, register Android app ID: `vn.ivsjsc.nocnom`.
2. Download `google-services.json`.
3. Place it at `app/google-services.json`.
4. Do **not** commit that file unless repository policy explicitly allows it.
5. Enable the same Authentication providers used by Web.
6. Verify Firestore security rules permit only the authenticated user to read/write their own profile/state documents.

The Gradle script applies `com.google.gms.google-services` automatically only when the file exists. This keeps the starter buildable before Firebase registration.

## Contract paths

- `users/{uid}/profile/main`
- `users/{uid}/state/timetable`
- `users/{uid}/state/dishes`
- `users/{uid}/state/categories`
- `users/{uid}/state/logs`
- `users/{uid}/state/meta`

Schema version: `2`.
