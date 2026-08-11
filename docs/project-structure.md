# Project Structure

This document explains **every folder** in the Hima AI repository and why it
exists. The layout follows an **MVVM + lightweight Clean Architecture** approach
with a **layer-first, feature-within** package organization so the codebase
scales cleanly as screens are added.

> This reflects the current state of the repo: a working Android app plus a
> local Node/Express backend. It's still evolving screen-by-screen, but the
> core report → AI analysis → result flow, auth, and the reserve map are all
> implemented and wired to real services (Supabase + Gemini).

---

## Top-level

```
Hima-AI/
├── AGENTS.md      # Working agreement + product/coding/UX standards (source of truth for how we build)
├── README.md      # Project intro, stack, feature list, docs index
├── docs/          # Engineering documentation
├── design/        # Approved designs — the visual source of truth
├── app/           # The Android application module
├── backend/       # Node/Express server — Gemini analysis, Supabase persistence, NASA FIRMS proxy
├── supabase/      # Supabase-side SQL (demo seed data)
└── run-demo.sh    # Starts the backend, then builds/installs/launches the app
```

| Folder     | Purpose |
| ---------- | ------- |
| `docs/`    | All written engineering documentation: architecture, setup, integrations, standards. Keeps knowledge out of chat and in the repo. |
| `design/`  | Exported screen mockups, components, and design tokens. Per AGENTS.md these are the **source of truth** — UI must match them. |
| `app/`     | The Android app module. Holds the Kotlin package tree, resources, and tests. |
| `backend/` | The Node/Express server the app talks to. Holds the Gemini API key server-side, uploads evidence photos to Supabase Storage, inserts reports into Supabase (RLS-scoped to the caller), and proxies NASA FIRMS fire data and protected-area boundaries. |
| `supabase/`| SQL that runs against the Supabase project directly (e.g. demo seed data), separate from anything the backend does at request time. |

---

## `docs/`

Engineering documentation, one concern per file.

| File                     | Purpose |
| ------------------------ | ------- |
| `project-structure.md`   | This file — the folder-by-folder map. |
| `architecture.md`        | MVVM + Clean Architecture layers, dependency rules, data flow. |
| `setup.md`               | How to get a local dev environment running. |
| `firebase-setup.md`      | Configuring Firebase Auth, Firestore, and Storage. |
| `gemini-integration.md`  | The Gemini image-analysis pipeline and prompt strategy. |
| `coding-standards.md`    | Kotlin/Compose conventions and the quality bar. |
| `design-guidelines.md`   | UI/UX principles distilled from AGENTS.md. |

---

## `design/`

The approved visual design. Treated as read-only truth by engineering.

| Folder                       | Purpose |
| ---------------------------- | ------- |
| `design/screens/`            | Exported mockups for each screen (Login, Home, New Report, Camera/Gallery, AI Investigation, AI Result, History). One source of truth per screen. |
| `design/components/`         | Designs for reusable components (buttons, report cards, status chips, top bars). |
| `design/design-system/`      | The visual language, split into tokens. |
| `design/design-system/colors/`     | Color palette and semantic color roles. |
| `design/design-system/typography/` | Type scale, font choices, text styles. |
| `design/design-system/spacing/`    | Spacing scale, grid, corner radii. |
| `design/icons/`              | Icon set used across the app. |
| `design/logo/`               | App logo, wordmark, and launcher-icon source art. |
| `design/flows/`              | User-flow and navigation diagrams tying screens together. |

---

## `app/` — the Android module

Standard Android source-set layout.

```
app/src/
├── main/          # Production code + resources
│   ├── java/com/hima/ai/   # Kotlin packages (base package: com.hima.ai)
│   └── res/                # Android resources (drawables, values, fonts)
├── test/          # JVM unit tests (ViewModels, use cases, mappers)
└── androidTest/   # Instrumented / UI tests on a device or emulator
```

| Folder            | Purpose |
| ----------------- | ------- |
| `app/src/main/`         | Everything shipped in the APK: Kotlin source and resources. |
| `app/src/test/`         | Fast JVM-only unit tests. Mirrors the `com/hima/ai` package. |
| `app/src/androidTest/`  | Instrumented tests (Compose UI tests, integration) that need an emulator/device. |

### `app/src/main/res/` — resources

| Folder                       | Purpose |
| ---------------------------- | ------- |
| `res/drawable/`              | Vector/bitmap drawables and shapes. |
| `res/values/`               | `strings.xml` (Arabic, the app's default/primary language), `colors.xml`, `themes.xml`, dimensions — no hardcoded strings/colors in code. |
| `res/values-en/`             | English string overrides. Keys must stay in parity with `res/values/`. |
| `res/values-night/`          | Dark-theme color overrides. |
| `res/font/`                 | Bundled font families for the type scale. |
| `res/mipmap-anydpi-v26/`    | Adaptive launcher-icon definitions. |

---

## `app/src/main/java/com/hima/ai/` — package tree

Base package: **`com.hima.ai`** (`hima` = protected reserve; fitting the domain).

The tree is organized **by architectural layer first**, with UI split **by
feature**. Dependencies point inward only: `presentation → domain ← data`. The
`domain` layer knows nothing about Compose, Supabase, or Gemini.

```
com/hima/ai/
├── core/           # Cross-cutting foundation shared by all features
│   ├── common/         # Result/Resource wrappers, constants, shared extensions
│   ├── designsystem/   # The Compose implementation of the design system
│   │   ├── theme/          # Color, Typography, Shape, HimaTheme
│   │   └── component/      # Reusable composables (buttons, cards, chips, bars)
│   ├── location/       # One-shot "my location" fix + Saudi place lookups
│   ├── map/            # MapLibre/MapTiler config for the reserve map
│   ├── navigation/     # NavHost, routes/destinations, nav args
│   └── util/           # Formatters, date/location helpers, misc utilities
│
├── data/           # How data is fetched and stored (the "how")
│   ├── remote/         # Remote data sources (Retrofit APIs)
│   │   ├── backend/        # Hima's own Node/Express backend (/analyze, /fires, …)
│   │   └── supabase/       # Supabase Auth + REST APIs, called directly from the app
│   ├── repository/     # Repository IMPLEMENTATIONS of domain interfaces
│   │                   # (SupabaseAuthRepository, SupabaseReportsRepository,
│   │                   #  BackendAiAnalysisRepository, BackendFireHotspotsRepository)
│   └── mock/           # Prototype/demo-only state (see below)
│
├── domain/         # Business rules — pure Kotlin, no Android/framework deps
│   ├── model/          # Core entities (Report, AiAnalysis, FireHotspot, User, Severity…)
│   └── repository/     # Repository INTERFACES (contracts the data layer fulfills)
│
├── presentation/   # UI layer, one package per screen (MVVM)
│   ├── splash/         # Splash / Welcome
│   ├── auth/           # Login + Sign up
│   ├── home/           # Home
│   ├── map/            # Reserve map (pan/zoom, incident markers, NASA FIRMS fire layer,
│   │                   #  protected-area boundaries, manual location override)
│   ├── report/         # The report lifecycle:
│   │   ├── newreport/      #   New report (attach photo, description, location)
│   │   ├── capture/        #   Camera / gallery capture
│   │   ├── analysis/       #   AI analysis (scanning, staged reveal, impact chart)
│   │   ├── recyclable/     #   Circular-economy result (recyclable-waste path)
│   │   ├── investigation/  #   AI investigation (follow-up Q&A)
│   │   └── detail/         #   Final report (structured output)
│   ├── history/        # Reports history
│   └── more/           # More/Settings menu (contact, FAQ, privacy, terms, rate app)
│
└── di/             # Hilt modules (NetworkModule, RepositoryModule)
```

Prototype/demo-only state lives in `data/mock/` — `MockData` (fallback
content), `ReportDraft` (the report currently being composed, including any
manual location override), and `PrototypeSession` (in-memory state shared
across the flow, so an investigation answer visibly updates the final
report). Everything else in `data/` talks to a real backend: Supabase
directly for auth and report reads, or the Node backend for AI analysis and
fire data.

### Layer-by-layer

| Package         | Responsibility | Depends on |
| --------------- | -------------- | ---------- |
| `core`          | Foundation reused everywhere: design system, navigation, map/location config, shared utils. | — |
| `domain`        | The business model and rules. Pure Kotlin, framework-free, most stable. | nothing |
| `data`          | Implements `domain` repository contracts using Supabase and the Node backend. | `domain` |
| `presentation`  | Compose screens + ViewModels. Each feature holds its screen, ViewModel, and UI state. | `domain`, `core` |
| `di`            | Provides/binds implementations (e.g. Hilt modules) so layers stay decoupled. | all |

### Why this shape

- **Scales by feature** — a new screen is a new package under `presentation/`,
  with matching use cases in `domain/` and no impact on existing features.
- **Testable** — `domain` and ViewModels are plain Kotlin, unit-testable without
  an emulator; Supabase/the backend sit behind repository interfaces and are mockable.
- **Swap-friendly** — the AI provider or database can change by editing only
  `data/`, because `domain` depends on interfaces, not on Gemini or Supabase directly.
- **Matches AGENTS.md** — enforces MVVM, reusable composables, state hoisting,
  and "no hardcoded strings/colors" (those live in `res/values/`).

### Per-feature convention (inside each `presentation/<feature>/`)

Each feature package will typically contain:

- `XxxScreen.kt` — the composable screen (stateless where possible, state hoisted)
- `XxxViewModel.kt` — exposes UI state and handles events
- `XxxUiState.kt` — an immutable state model for that screen
- `components/` — composables private to the feature (promote to
  `core/designsystem/component` once reused elsewhere)
