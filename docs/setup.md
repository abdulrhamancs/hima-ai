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

`local.properties` is git-ignored. Add your Gemini key there when you reach the
AI phase (it's wired into `BuildConfig.GEMINI_API_KEY`):

```properties
# local.properties  (do NOT commit)
GEMINI_API_KEY=your_key_here
```

## Deferred (added in their phases, not yet wired)

- **Firebase** (Phase 2 — Login): drop `app/google-services.json` in, then
  uncomment the `google-services` plugin and Firebase deps (both are already in
  the version catalog and flagged with comments). See
  [`firebase-setup.md`](firebase-setup.md).
- **Gemini SDK** (Phase 5): added when the AI draft screen is built.
