# Design

**Source of truth for the UI.** Per [`AGENTS.md`](../AGENTS.md), engineering
matches these designs and does not invent layouts without asking.

## Source of truth

[`prototype/hima-white-system.html`](prototype/hima-white-system.html) — the
**"White-first system"** — is the current approved UI/UX (superseded
2026-08-08). It is Arabic-first with real RTL, 8 screens (Splash, Login,
Home, New report, AI analysis, Final report, AI investigation, Reports
history), a white/warm palette, a 4-level severity ramp, and IBM Plex Sans
Arabic + Inter typography. All screens and components are built to match it.
Do not invent layouts without asking.

[`prototype/hima-ai-redesign.html`](prototype/hima-ai-redesign.html) is the
**previous** approved design (6 screens, green/sand palette, EN/AR toggle).
Kept for historical reference only — no longer the source of truth.

[`prototype/hima-welcome-android-critique.html`](prototype/hima-welcome-android-critique.html)
is a design review of the Splash/Welcome screen. It flagged the shield-with-
pine-trees mark as geographically atypical for Saudi Arabia and proposed
alternatives (oryx horns, acacia tree, ح monogram). **Decision: kept the
original shield+mountains+trees mark** (`logo/hima-logo.png`) — an oryx-horns
version was tried and explicitly reverted back to this one. The review's
structural recommendation (one job per screen, no combined hero+dots+feature-
grid) was also **not** adopted — Splash intentionally keeps the full combined
layout per product decision.

## Structure

| Folder            | What goes here |
| ----------------- | -------------- |
| `prototype/`      | The approved HTML/CSS prototype — the canonical design. |
| `screens/`        | Exported mockups per screen: Login, Home, Capture, AI draft, Final Report, Report History. |
| `components/`     | Designs for reusable components (buttons, report cards, status chips, top bars). |
| `design-system/`  | The visual language, split into tokens below. |
| `design-system/colors/`     | Color palette and semantic roles. |
| `design-system/typography/` | Type scale, fonts, text styles. |
| `design-system/spacing/`    | Spacing scale, grid, corner radii. |
| `icons/`          | The app's icon set. |
| `logo/`           | Logo, wordmark, launcher-icon source art. [`logo/hima-logo.png`](logo/hima-logo.png) is the reference mark (shield + mountains + trees + bird + bilingual wordmark) — hand-vectorized into `app/.../res/drawable/ic_hima_mark.xml` (in-app, fuller detail) and `ic_launcher_foreground.xml` (launcher, simplified for legibility at small sizes). |
| `flows/`          | User-flow / navigation diagrams. |

## Conventions

- Name screen exports after the screen: `home.png`, `new-report.png`, etc.
- Keep one authoritative version per screen; archive older iterations elsewhere.
- Design tokens here map to code in `app/.../core/designsystem/`.
