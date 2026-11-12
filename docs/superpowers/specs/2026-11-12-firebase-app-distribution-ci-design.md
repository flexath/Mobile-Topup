# Firebase App Distribution CI/CD Design

**Date:** 2026-11-12  
**Status:** Approved  
**Scope:** GitHub Actions + Firebase App Distribution (no Play Store)

## Problem

The TopUp Android app has release signing and Supabase BuildConfig secrets wired through root `local.properties` (fail-hard at configuration). There is no automated pipeline to produce a signed release APK and share it with testers.

## Decisions (approved)

| Decision | Choice |
|----------|--------|
| Credential injection | **Approach 1** — CI synthesizes ephemeral `local.properties` + keystore; no build-logic changes |
| Auto-trigger branch | `master` |
| Manual trigger | `workflow_dispatch` with optional `release_notes` |
| Pre-distribution checks | `validatePlugins` + `testDebugUnitTest` + `:app:lintRelease` + `:app:assembleRelease` |
| Distribution target | Firebase App Distribution only |
| Job shape | Single job so failed validation never reaches upload |

## Goals

1. Build a signed release APK on GitHub Actions using existing Gradle signing.
2. Inject signing and Supabase secrets securely via GitHub Secrets.
3. Upload the APK to Firebase App Distribution for configured tester groups.
4. Document secrets, Firebase setup, keystore encoding, triggers, and troubleshooting.
5. Never commit keystores, passwords, or service-account credentials.

## Non-goals

- Google Play Store deployment or Fastlane supply.
- Firebase Test Lab.
- Changing `AndroidApplicationConfigurer` / `LocalPropertiesExtensions`.
- Environment flavors (`dev` / `stg` / `prod`) or additional build types.

## Architecture

```
push master | workflow_dispatch
  → checkout
  → JDK 18 + Android SDK + Gradle cache
  → decode keystore + write local.properties
  → validatePlugins
  → testDebugUnitTest
  → :app:lintRelease
  → :app:assembleRelease
  → Firebase App Distribution
  → GitHub Actions job summary
```

Existing app Firebase client config (`app/google-services.json`, `topup.firebase`) remains unchanged. CI uses a **service account** for App Distribution API access.

## Secrets

| Secret | Purpose |
|--------|---------|
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded release keystore |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias |
| `RELEASE_KEY_PASSWORD` | Key password |
| `SUPABASE_URL` | `:app` BuildConfig (fail-hard) |
| `SUPABASE_ANON_KEY` | `:app` BuildConfig (fail-hard) |
| `FIREBASE_APP_ID` | Firebase Android app id |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Service account JSON (App Distribution) |
| `FIREBASE_TESTER_GROUPS` | Comma-separated tester group aliases |

Default Firebase app id for this project: `1:279743143519:android:a8b0f7f84873ec395705ea` (project `mobile-topup-5d3e1`).

## Release notes

- Manual runs: use `workflow_dispatch` input `release_notes` when provided.
- Otherwise: latest commit subject.

## Documentation

Operational guide: `docs/ci-cd/firebase-app-distribution.md` (linked from README).

## Out of scope

- Merging to `master` or changing the remote default branch.
- Committing keystore or service-account files.
