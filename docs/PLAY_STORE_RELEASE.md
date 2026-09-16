# nOcnOm — Google Play release runbook

Package: `vn.ivsjsc.nocnom`

Google Play should receive a **signed Android App Bundle (`.aab`)**, not the debug APK used for local testing.

## 1. Register Firebase Android app first

Use Firebase project `cocoa-35632` and add Android application:

- Android package name: `vn.ivsjsc.nocnom`
- Download `google-services.json`
- Do not commit this file to Git; `.gitignore` already excludes it.

For GitHub Actions, encode the JSON and store it as repository secret `GOOGLE_SERVICES_JSON_BASE64`.

PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("google-services.json")) | Set-Clipboard
```

## 2. Create a dedicated Play upload key

Create this key once and back it up securely. Do not commit it to Git.

```bash
keytool -genkeypair -v \
  -keystore nocnom-upload.jks \
  -alias nocnom-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Use Google Play App Signing. The local `nocnom-upload.jks` should be treated as the **upload key**, while Google Play manages the app signing key.

Encode the keystore for GitHub Actions.

PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("nocnom-upload.jks")) | Set-Clipboard
```

## 3. Add GitHub Actions secrets

Repository: `ivsjsc/nocnom-android`

Settings → Secrets and variables → Actions → New repository secret

Required:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS` = `nocnom-upload`
- `ANDROID_KEY_PASSWORD`

Recommended before Play testing:

- `GOOGLE_SERVICES_JSON_BASE64`

Never commit raw passwords, keystores, or `google-services.json`.

## 4. Build the signed AAB

GitHub → Actions → **Android Release AAB** → Run workflow.

The workflow will:

1. run unit tests;
2. restore the upload key from GitHub Secrets;
3. restore Firebase config if configured;
4. build `app-release.aab`;
5. verify the bundle signature;
6. upload artifact `nocnom-play-release-aab`.

Expected bundle:

```text
app/build/outputs/bundle/release/app-release.aab
```

## 5. Create the app in Google Play Console

Create a new app with:

- App name: `nOcnOm`
- Default language: Vietnamese (or the product's intended primary language)
- App or game: App
- Free or paid: choose deliberately; this choice has consequences after publication.

Package/application ID must remain:

```text
vn.ivsjsc.nocnom
```

Do not create another Play app with a different package ID for the same product.

## 6. Complete Play Console declarations

Before production, complete all account-specific Play Console requirements, including as applicable:

- Store listing
- App icon and screenshots
- Feature graphic
- Privacy policy URL
- Data safety form
- App access instructions if login is required
- Ads declaration
- Content rating
- Target audience / age groups
- News / health / financial or other special declarations when applicable
- Countries/regions and pricing

Because nOcnOm handles profile, nutrition and potentially health-related information, the Data safety declaration and privacy policy must match the actual implementation. Do not claim that health data is collected or shared unless the released build actually does so.

## 7. Release strategy

Do **not** make the first Android build production-public immediately.

Recommended order:

1. Internal testing
2. Closed testing if required by the developer account / Play Console
3. Production after Firebase authentication, Firestore sync, account flows and privacy declarations have been verified

Google Play's testing requirements vary by developer account type and can change. Follow the exact production-access checklist shown in the Play Console for the account.

## 8. Versioning rule

Every bundle uploaded to Play must have a unique, increasing `versionCode`.

Current bootstrap:

```gradle
versionCode 1
versionName '0.1.0'
```

Next upload must use at least `versionCode 2` even if the previous release never reached production.

## 9. Release gate for nOcnOm

Before production-public release, verify:

- Firebase Android app registered for `vn.ivsjsc.nocnom`
- `google-services.json` matches that package
- Email/Google authentication works on physical Android devices
- Web and Android read/write the same Firestore user data contract
- No demo-only state is presented as cloud-synced data
- Nutrition and macro calculations match the Web domain rules
- Privacy policy and Data safety form match actual data handling
- Delete-account/data process is available if the released authentication/account flow requires it
- Release AAB is signed and the Play upload key is backed up securely
