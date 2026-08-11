# Local Setup

The project is a real Gradle Android project with the full flow implemented:
Supabase-backed login, photo capture, Gemini-powered AI analysis (via the
local Node backend), the reserve map, and report history. It opens and syncs
in Android Studio like any standard Android app.

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
3. Start the backend first (see below) — without it, AI analysis fails. Then
   let Gradle sync finish and **Run** the `app` config on a device/emulator.
   You'll land on Login (Supabase-backed) → Home → New Report → AI Analysis.

## Running the backend

The AI analysis flow (and the map's live fire layer) goes through the local
Node/Express server in `backend/`. It must be running before you use those
features in the app.

```bash
cd backend
npm install
node index.js
```

`backend/.env` must define `GEMINI_API_KEY`, `SUPABASE_URL`,
`SUPABASE_PUBLISHABLE_KEY`, and `NASA_FIRMS_MAP_KEY` — the backend refuses to
boot without `GEMINI_API_KEY`. From the repo root, `./run-demo.sh` does the
`npm install` + start for you, then builds/installs/launches the app.

The emulator reaches the backend at `http://10.0.2.2:5000/` by default (set
via `BACKEND_BASE_URL` in `local.properties`). A **physical device** needs
that overridden to your machine's LAN IP, e.g.
`BACKEND_BASE_URL=http://192.168.1.x:5000/`.

## Secrets — never commit them

Gemini is called only by the Express backend. Configure `GEMINI_API_KEY` in
`backend/.env` for local backend development, or in the hosting provider's
private environment for production. Never add it to Android `local.properties`,
`BuildConfig`, source code, or any committed file.

Android's git-ignored `local.properties` contains only its SDK path and client
configuration such as `SUPABASE_URL`, `SUPABASE_ANON_KEY`,
`BACKEND_BASE_URL`, and `MAPTILER_API_KEY`.

## Not used

- **Firebase**: `firebase-bom`/Auth/Firestore/Storage are still declared in
  `gradle/libs.versions.toml` and the `google-services` plugin is commented
  out in `app/build.gradle.kts`, but none of it is wired up. Auth, the
  database, and file storage all run on Supabase instead — see
  `SupabaseAuthRepository` and `SupabaseReportsRepository`. Ignore
  `firebase-setup.md`; it documents the superseded approach.
- **Gemini Android SDK:** not required. The app sends evidence to the
  backend, which performs Gemini analysis server-side.
