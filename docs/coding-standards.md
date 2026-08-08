# Coding Standards

Distilled from [`AGENTS.md`](../AGENTS.md). These are the quality bar for all
Hima AI code.

## Architecture

- **MVVM** throughout; follow the layering in [`architecture.md`](architecture.md).
- Business logic lives in `domain/` use cases, never in composables.
- No Firebase/Gemini calls from the UI — go through repository interfaces.

## Kotlin

- Follow standard Kotlin style and idioms.
- Prefer immutability (`val`, immutable data classes, immutable `UiState`).
- Wrap fallible operations in a `Result`/`Resource` type (`core/common/`).

## Compose

- **Small, reusable composables** — one responsibility each.
- **State hoisting** — stateless composables receive state + event callbacks.
- **No duplicated UI** — promote shared composables to
  `core/designsystem/component/`.
- **Performance** — `LazyColumn` for lists, `remember`/`derivedStateOf` where
  appropriate, and avoid unnecessary recompositions.

## No hardcoding

- **No hardcoded strings** — use `res/values/strings.xml`.
- **No hardcoded colors** — use the design-system theme
  (`core/designsystem/theme/`), not literal color values.
- **No hardcoded dimensions** — use spacing tokens from the design system.

## Consistency with design

- The `/design` folder is the **source of truth**. Match it.
- Do not invent layouts without asking.

## UX bar (when building/discussing UI)

Always account for: information hierarchy, accessibility (contrast, touch-target
size, content descriptions), edge cases (loading/empty/error), suggested icons,
and the right Material 3 component. Optimize for **outdoor readability** and
**one-handed use** with large touch targets.

## Definition of Done

A feature is done only when it: matches the approved design, is responsive, is
accessible, follows clean architecture, has no duplicated code, and is ready for
backend integration.

## Working style

- One screen at a time; no unrelated features bundled together.
- Explain the plan before significant changes; wait for approval, then implement.
- Keep changes focused; don't refactor unrelated code unless asked.
