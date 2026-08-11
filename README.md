# Hima AI

AI-powered environmental monitoring for Saudi Arabia's natural reserves.

Rangers capture a photo of an environmental incident (or a recyclable item),
Gemini analyzes it server-side, and the app generates a structured
environmental report with a severity, a recommended action, and — for
recyclable finds — a circular-economy result instead of an incident report.

> **Status:** Working prototype. Auth, report capture, AI analysis, the
> reserve map (with a live satellite fire layer), and report history are all
> implemented and backed by real services (Supabase + a local Gemini
> backend). See [`docs/project-structure.md`](docs/project-structure.md) for
> the folder-by-folder breakdown.

---

## Tech Stack

| Layer                 | Technology                                                                 |
| ---------------------- | --------------------------------------------------------------------------- |
| Language               | Kotlin                                                                     |
| UI                     | Jetpack Compose + Material 3                                               |
| Architecture           | MVVM + lightweight Clean Architecture (`presentation → domain ← data`)    |
| Dependency injection   | Hilt                                                                       |
| Auth                   | Supabase Auth                                                              |
| Database               | Supabase (Postgres) with Row Level Security                               |
| File storage           | Supabase Storage (evidence photos, per-user-scoped paths)                 |
| Backend                | Node.js + Express, local server in [`backend/`](backend/)                 |
| AI / image analysis    | Google Gemini API (`@google/generative-ai`), called **server-side only**  |
| Maps                   | MapLibre Native (Android SDK) rendering MapTiler vector tiles             |
| Satellite fire data    | NASA FIRMS (VIIRS), proxied and cached by the backend                     |
| Location               | Google Play Services Location (one-shot "my location" fix)                |
| Android networking     | Retrofit + OkHttp + Moshi                                                  |
| Image loading          | Coil                                                                       |
| Camera capture         | CameraX                                                                    |

> **Note:** Firebase (`firebase-bom`, Auth/Firestore/Storage) is still
> declared in `gradle/libs.versions.toml` but is **not applied or used
> anywhere in the app** — auth, database, and storage all run on Supabase.
> Treat the Firebase entries as leftover/reserved, not part of the real stack.

## Supported Languages

- **Arabic (RTL)** — the app's default/primary language (`res/values/`)
- **English** — secondary, with full string parity (`res/values-en/`)

The app declares `android:supportsRtl="true"` and a locale config, and the UI
mirrors correctly for Arabic.

## Features

The following screens are implemented and reachable in the app's navigation
graph (`core/navigation/HimaDestinations.kt`):

- **Splash** — branded launch screen
- **Login / Sign Up** — Supabase-backed authentication
- **Home** — entry point to reporting, the map, and history
- **Map** — pan/zoom reserve map with incident markers, a live NASA FIRMS
  active-fire layer, protected-area boundaries, and a manual location
  override (for demos where GPS isn't reliable)
- **New Report** — attach a photo (camera or gallery), add a description,
  optionally override the location
- **AI Analysis** — sends the photo to the backend for Gemini analysis, with
  a scanning animation, staged reveal, and an environmental-impact chart
- **Recyclable Result** — a separate circular-economy outcome shown when
  Gemini classifies the photo as recyclable waste rather than an
  environmental incident
- **Investigation** — AI follow-up Q&A used to refine an ambiguous report
- **Report Detail** — the final structured report (type, severity,
  recommended action, AI analysis)
- **History** — list of previously submitted reports
- **More / Settings** — menu for Contact, FAQ, Privacy, Terms, and Rate App
  (these five sub-screens are currently placeholder "coming soon" screens)

## Repository Layout

```
Hima-AI/
├── AGENTS.md      # Working agreement, product context, coding & UX standards
├── README.md      # You are here
├── docs/          # Engineering documentation
├── design/        # Approved designs — the visual source of truth
├── app/           # Android application module (Kotlin + Compose)
├── backend/       # Node/Express server — Gemini analysis, Supabase persistence, NASA FIRMS proxy
├── supabase/      # Supabase-side SQL (demo seed data)
└── run-demo.sh    # Starts the backend, then builds/installs/launches the app
```

### `app/src/main/java/com/hima/ai/` — package tree

```
com/hima/ai/
├── core/           # navigation, design system, map config, location, shared utils
├── data/           # remote/ (Retrofit clients for the backend + Supabase REST/Auth),
│                   # repository/ (implementations), mock/ (prototype session state)
├── domain/         # model/ (Report, AiAnalysis, FireHotspot, User…), repository/ (interfaces)
├── presentation/   # splash, auth, home, map, history, more, and
│                   # report/{newreport, capture, analysis, recyclable, investigation, detail}
└── di/             # Hilt modules (NetworkModule, RepositoryModule)
```

See [`docs/project-structure.md`](docs/project-structure.md) for the
full folder-by-folder explanation.

## Getting Started

The AI analysis flow depends on the local Node backend — without it running,
every "Analyze" request fails. The fastest path is the bundled script:

```bash
./run-demo.sh              # starts the backend, then builds + installs + launches the app
./run-demo.sh --backend    # backend only
./run-demo.sh --check      # verify the backend is reachable, changes nothing
```

Or manually:

```bash
cd backend
npm install
node index.js               # or: npx nodemon index.js
```

`backend/.env` must define `GEMINI_API_KEY`, `SUPABASE_URL`,
`SUPABASE_PUBLISHABLE_KEY`, and `NASA_FIRMS_MAP_KEY` — the backend refuses to
start without `GEMINI_API_KEY`.

Then open the project root in Android Studio, let Gradle sync, and run the
`app` configuration. See [`docs/setup.md`](docs/setup.md) for the full local
setup (prerequisites, secrets, emulator vs. physical device networking).

## Documentation Index

- [Project structure](docs/project-structure.md) — every folder, explained
- [Architecture](docs/architecture.md) — MVVM + Clean Architecture, data flow
- [Setup](docs/setup.md) — local development environment
- [Gemini integration](docs/gemini-integration.md) — AI analysis pipeline
- [Coding standards](docs/coding-standards.md) — conventions and quality bar
- [Design guidelines](docs/design-guidelines.md) — UI/UX principles

> `docs/firebase-setup.md` documents a Firebase-based architecture that was
> superseded by Supabase during development and no longer reflects the app.
> It's left out of the index above; ask if you'd like it updated or removed.
