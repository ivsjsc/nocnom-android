# Firebase Android setup

The Android app uses Firebase project `cocoa-35632` and application ID `vn.ivsjsc.nocnom`.

## Confirmed configuration

The Firebase Android configuration supplied for nOcnOm matches:

- Firebase project: `cocoa-35632`
- Android package: `vn.ivsjsc.nocnom`

`app/google-services.json` stays ignored by Git and is injected into CI/release builds from the GitHub Actions secret `GOOGLE_SERVICES_JSON_BASE64`.

## GitHub Actions secret

1. Base64-encode `google-services.json` without changing its contents.
2. Open repository Settings -> Secrets and variables -> Actions.
3. Create repository secret `GOOGLE_SERVICES_JSON_BASE64`.
4. Paste the Base64 value.

Android CI validates both project ID and package name before using the file. Play release builds require this secret; release must not silently fall back to demo/no-Firebase configuration.

## Local development

Place the downloaded file at:

`app/google-services.json`

Do not commit it to this public repository.

The Gradle script applies `com.google.gms.google-services` automatically when this file exists.

## Authentication

Enable the same Firebase Authentication providers used by the Web client.

### Google Sign-In

The first supplied configuration contains only the Web OAuth client. Before Google Sign-In is considered production-ready on Android:

1. Create the Android upload keystore.
2. Add its SHA-1 and SHA-256 fingerprints to the Firebase Android app.
3. After the first Play Console upload with Play App Signing enabled, copy the Play **App signing key certificate** SHA-1 and SHA-256 from Play Console -> App integrity and add them to the same Firebase Android app.
4. Download a refreshed `google-services.json`.
5. Replace the GitHub secret `GOOGLE_SERVICES_JSON_BASE64` with the refreshed file.
6. Test Google Sign-In from an app installed through a Play testing track.

Email/password authentication and Firestore do not depend on the Android OAuth client fingerprint in the same way, but Firestore security rules still control access.

## Firestore contract paths

- `users/{uid}/profile/main`
- `users/{uid}/state/timetable`
- `users/{uid}/state/dishes`
- `users/{uid}/state/categories`
- `users/{uid}/state/logs`
- `users/{uid}/state/meta`

Schema version: `2`.

## Security requirements

- Firestore rules must restrict users to their own profile/state documents.
- Never use client-side Firebase configuration as authorization.
- Review Google Cloud API-key restrictions before production release.
- Do not commit upload keystores, passwords, service-account JSON, or GitHub secret values.
