# Local Setup

The project is now a real Gradle Android project (Phase 0 + Phase 1 complete).
It opens and syncs in Android Studio and runs a themed placeholder flow that
proves the design-system pipeline.

## Prerequisites

- **Android Studio** (latest stable) with the Android SDK
- **JDK 17** (bundled with Android Studio as the JBR — no separate install needed)
- A device or emulator (minSdk 26 / Android 8.0+)

## Pinned toolchain

| Tool | Version |
| ---- | ------- |
| Android Gradle Plugin | 8.7.3 |
| Gradle | 8.11.1 (via wrapper) |
| Kotlin | 2.0.21 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |
| Compose BOM | 2024.12.01 |

All versions live in [`gradle/libs.versions.toml`](../gradle/libs.versions.toml).

## Open it in Android Studio

1. **File → Open** and select the project root (`Hima-AI`), then trust it.
2. On first sync, Android Studio may prompt to:
   - **install the Android 35 platform / build-tools** — accept (one click); and
   - **generate the Gradle wrapper** — accept if asked (the wrapper `.jar` isn't
     committed; Studio recreates it from `gradle/wrapper/gradle-wrapper.properties`).
   It also writes `local.properties` with your `sdk.dir` automatically.
3. Let Gradle sync finish, then **Run** the `app` config on a device/emulator.
   You'll see a themed "Sign in" placeholder → tap through to Home → Capture.

## Secrets — never commit them

Gemini is called only by the Express backend. Configure `GEMINI_API_KEY` in
`backend/.env` for local backend development, or in the hosting provider's
private environment for production. Never add it to Android `local.properties`,
`BuildConfig`, source code, or any committed file.

Android's git-ignored `local.properties` contains only its SDK path and client
configuration such as `SUPABASE_URL`, `SUPABASE_ANON_KEY`,
`BACKEND_BASE_URL`, and `MAPTILER_API_KEY`.

## Deferred (added in their phases, not yet wired)

- **Firebase** (Phase 2 — Login): drop `app/google-services.json` in, then
  uncomment the `google-services` plugin and Firebase deps (both are already in
  the version catalog and flagged with comments). See
  [`firebase-setup.md`](firebase-setup.md).
- **Gemini Android SDK:** not required. The app sends evidence to the existing
  backend, which performs Gemini analysis server-side.
