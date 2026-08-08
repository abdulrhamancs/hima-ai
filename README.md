# Hima AI

AI-powered environmental monitoring for Saudi Arabia's natural reserves.

Rangers capture a photo of an environmental incident, Gemini AI analyzes it, and
the app generates a structured environmental report with recommended actions.

> **Status:** Project scaffold. Folder structure and documentation only — no
> application code yet. See [`docs/project-structure.md`](docs/project-structure.md).

---

## Tech Stack

| Layer            | Technology                                        |
| ---------------- | ------------------------------------------------- |
| Language         | Kotlin                                            |
| UI               | Jetpack Compose + Material 3                       |
| Architecture     | MVVM + lightweight Clean Architecture             |
| Auth             | Firebase Authentication                           |
| Database         | Cloud Firestore                                   |
| File storage     | Firebase Storage                                  |
| Maps             | Google Maps                                       |
| AI               | Gemini API (image + text analysis)                |

## MVP Scope

Login · Home · New Report · Camera / Gallery · AI Investigation · AI Result ·
Report History.

Roadmap features are intentionally out of scope for the one-week hackathon MVP.

## Repository Layout

```
Hima-AI/
├── AGENTS.md      # Working agreement, product context, coding & UX standards
├── README.md      # You are here
├── docs/          # Engineering documentation
├── design/        # Approved designs — the visual source of truth
└── app/           # Android application module (package tree only, no code yet)
```

Start with [`docs/project-structure.md`](docs/project-structure.md) for a
folder-by-folder explanation, and [`docs/setup.md`](docs/setup.md) to get a dev
environment running.

## Documentation Index

- [Project structure](docs/project-structure.md) — every folder, explained
- [Architecture](docs/architecture.md) — MVVM + Clean Architecture, data flow
- [Setup](docs/setup.md) — local development environment
- [Firebase setup](docs/firebase-setup.md) — Auth, Firestore, Storage
- [Gemini integration](docs/gemini-integration.md) — AI analysis pipeline
- [Coding standards](docs/coding-standards.md) — conventions and quality bar
- [Design guidelines](docs/design-guidelines.md) — UI/UX principles
