# Architecture

Hima AI uses **MVVM** on top of a **lightweight Clean Architecture**. The goal is
a codebase that stays clean and testable while moving at hackathon speed.

## Layers

```
┌───────────────────────────────────────────────┐
│  presentation  (Compose screens + ViewModels)  │   knows: domain, core
├───────────────────────────────────────────────┤
│  domain        (entities, use cases, contracts)│   knows: nothing
├───────────────────────────────────────────────┤
│  data          (Firebase + Gemini + repos)     │   knows: domain
└───────────────────────────────────────────────┘
                di  wires it all together
```

**Dependency rule:** dependencies point inward. `domain` is pure Kotlin and
depends on no other layer or framework. `presentation` and `data` both depend on
`domain`, never on each other.

## Responsibilities

- **presentation** — Renders UI with Jetpack Compose and Material 3. Each screen
  has a `ViewModel` that exposes an immutable `UiState` (via `StateFlow`) and
  handles user events. Composables are stateless where possible, with state
  hoisted. No business logic and no direct Firebase/Gemini calls here.
- **domain** — The heart of the app: entities (`Report`, `AiAnalysis`, `User`),
  repository **interfaces**, and single-purpose **use cases**
  (`AnalyzeIncidentImage`, `SubmitReport`, `GetReportHistory`). Framework-free
  and the most stable layer.
- **data** — Implements the domain repository interfaces. Talks to Firebase
  (Auth, Firestore, Storage) and the Gemini API, and maps DTOs to domain models.
- **di** — Dependency-injection modules that bind interfaces to implementations,
  keeping layers decoupled and testable.

## Typical flow — "New Report"

1. User captures a photo (**camera** feature) and fills report fields
   (**report** feature).
2. `ReportViewModel` calls the `AnalyzeIncidentImage` use case.
3. The use case calls `ReportRepository` (domain interface).
4. `ReportRepositoryImpl` (data) uploads the image to **Firebase Storage**, sends
   it to the **Gemini API**, and maps the response to an `AiAnalysis` domain model.
5. The ViewModel updates its `UiState`; the **investigation** screen shows progress
   and the **result** screen renders the structured analysis.
6. On confirm, `SubmitReport` persists the report to **Firestore**.

## State management

- One immutable `UiState` per screen, exposed as `StateFlow`.
- ViewModels expose state; composables observe it with
  `collectAsStateWithLifecycle()`.
- Prefer `remember`/`derivedStateOf` to avoid unnecessary recompositions; render
  lists with `LazyColumn`.

## Testing strategy

- **`app/src/test/`** — unit-test ViewModels, use cases, and mappers by mocking
  repository interfaces. No emulator required.
- **`app/src/androidTest/`** — Compose UI tests and integration tests that need a
  device/emulator.

See [`project-structure.md`](project-structure.md) for the package tree that
implements these layers.
