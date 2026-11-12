# Firebase App Distribution CI/CD

GitHub Actions builds a **signed release APK** and uploads it to **Firebase App Distribution**. There is no Google Play Store deployment.

Workflow file: [`.github/workflows/firebase-app-distribution.yml`](../../.github/workflows/firebase-app-distribution.yml)

## Triggers

| Event | Behavior |
|-------|----------|
| Push to `master` or `release/**` | Full pipeline: validate → unit tests → Lint → assemble release → distribute |
| `workflow_dispatch` | Same pipeline; optional `release_notes` input for testers |

Failed Gradle steps stop the job; Firebase upload does not run.

## Required GitHub Secrets

Repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**.

| Secret | Description |
|--------|-------------|
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded release `.jks` / `.keystore` |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias |
| `RELEASE_KEY_PASSWORD` | Key password |
| `SUPABASE_URL` | Same value as local `local.properties` (required for `:app` BuildConfig) |
| `SUPABASE_ANON_KEY` | Client/anon or publishable key (never the service-role key) |
| `FIREBASE_APP_ID` | Firebase Android app id |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Full JSON contents of the Firebase/Google Cloud service account key |
| `FIREBASE_TESTER_GROUPS` | Comma-separated App Distribution group aliases (e.g. `qa-testers`) |

### Project defaults (TopUp)

| Item | Value |
|------|-------|
| Application id | `com.aungthiha.topup` |
| Firebase project | `mobile-topup-5d3e1` |
| Firebase App ID | `1:279743143519:android:a8b0f7f84873ec395705ea` |

Set `FIREBASE_APP_ID` to the App ID above unless you intentionally use another Firebase Android app.

## Encode the release keystore

Never commit the keystore. Encode it locally, then paste the single-line output into `RELEASE_KEYSTORE_BASE64`.

```bash
# macOS
base64 -i /path/to/your-release.jks | pbcopy

# Linux
base64 -w 0 /path/to/your-release.jks
```

The workflow decodes this to `keystore/release.jks` on the runner and points `RELEASE_KEYSTORE` at that path in an ephemeral `local.properties` (same keys as local development).

## Configure Firebase App Distribution

1. Open [Firebase Console](https://console.firebase.google.com/) → project `mobile-topup-5d3e1`.
2. Enable **App Distribution** for the Android app `com.aungthiha.topup`.
3. Confirm the App ID under **Project settings** → **Your apps** matches `FIREBASE_APP_ID`.

### Service account

Use your existing service account JSON as `FIREBASE_SERVICE_ACCOUNT_JSON` (paste the entire JSON object as the secret value).

The account needs permission to upload to App Distribution. Typical roles:

- **Firebase App Distribution Admin** (preferred), or
- **Firebase Admin** (broader)

Create or download the key from Google Cloud Console → **IAM & Admin** → **Service Accounts** → your account → **Keys** → **Add key** → JSON.

### Tester groups

1. Firebase Console → **App Distribution** → **Testers & groups**.
2. Create a group (alias is what CI uses, e.g. `qa-testers`).
3. Add tester emails to the group.
4. Set GitHub secret `FIREBASE_TESTER_GROUPS` to that alias (comma-separated for multiple groups).

Testers receive an email / invite link after a successful distribution.

## Release notes

- **Manual run:** Actions → **Firebase App Distribution** → **Run workflow** → fill `release_notes` (optional).
- **Push to `master` / `release/**` / empty input:** latest commit subject is used.

## Manual workflow run

1. GitHub → **Actions** → **Firebase App Distribution**.
2. **Run workflow** (branch usually `master`).
3. Optionally set release notes.
4. After success, open the job **Summary** for console / testing / download links when the distribution action provides them.
5. Confirm the build appears under Firebase → App Distribution.

## Automatic distribution on `master` and `release/**`

Every push to `master` or a `release/**` branch runs the same job. After merge/push, check Actions for the run and Firebase App Distribution for the new release.

## What the pipeline runs

```bash
./gradlew :build-logic:convention:validatePlugins
./gradlew testDebugUnitTest
./gradlew :app:lintRelease
./gradlew :app:assembleRelease
```

Then uploads `app/build/outputs/apk/release/*.apk` via Firebase App Distribution.

Runner: `ubuntu-latest`, Temurin **JDK 18** (`actions/setup-java@v5`), Gradle caching, Android SDK via `android-actions/setup-android@v4` (`platform-tools` only — the legacy `tools` package is no longer served by Google).

## Security notes

- Do not commit keystores, `local.properties`, or service-account JSON.
- CI writes those only on the runner and deletes the ephemeral keystore / `local.properties` at the end.
- The workflow may upload the **release APK** as a GitHub Actions artifact (`app-release-apk`); that is not a secret, but treat it as an internal build.
- Never put the Supabase **service-role** key in GitHub Secrets used by this Android build — only the client anon/publishable key.

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `Missing secret …` | Secret not set or empty | Add/fix the named repository secret |
| `Release keystore not found` / empty decode | Bad Base64 or wrong secret | Re-encode the `.jks` and update `RELEASE_KEYSTORE_BASE64` |
| `Missing RELEASE_*` / `SUPABASE_*` in local.properties | Secret omitted when writing CI properties | Ensure all signing + Supabase secrets are set |
| Unit test or Lint failure | Failing check before distribute | Fix locally with the same Gradle tasks; distribution is skipped until green |
| `assembleRelease` signing error | Wrong password/alias or corrupted keystore | Verify alias/passwords match the keystore locally |
| Firebase upload auth / permission error | Service account lacks App Distribution role or wrong JSON | Fix IAM roles; re-paste full JSON into `FIREBASE_SERVICE_ACCOUNT_JSON` |
| Upload succeeds but testers see nothing | Wrong `FIREBASE_APP_ID` or empty/wrong `FIREBASE_TESTER_GROUPS` | Match App ID and group aliases in Firebase Console |
| JDK / toolchain errors | Wrong Java on runner | Workflow must use Java **18** (already configured) |
| `Failed to find package 'tools'` in Set up Android SDK | Obsolete `android-actions/setup-android@v3` default | Use `@v4` with `packages: platform-tools` (already configured) |

## Out of scope

- Google Play Console / Play Store tracks
- Firebase Test Lab
- Changing Gradle build-logic signing APIs
