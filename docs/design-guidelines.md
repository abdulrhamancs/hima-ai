# Design Guidelines

The look and feel Hima AI is aiming for, distilled from [`AGENTS.md`](../AGENTS.md).
The exported designs in [`/design`](../design) are the **source of truth**; this
document captures the principles behind them.

## Product feel

Premium · modern · minimal · professional. Built for rangers using the app
**outdoors, one-handed**.

## Do

- High readability in bright outdoor conditions (strong contrast, legible type).
- Large touch targets and one-handed reach.
- Consistent spacing and a clean visual hierarchy.
- Responsive layouts.
- Minimal visual clutter — show what matters, once.

## Avoid

- Government-dashboard appearance.
- Generic Material templates.
- Dense layouts.
- Unnecessary cards.
- Too many colors.
- Duplicate information.

## Design system

Tokens live under [`/design/design-system`](../design/design-system) and are
implemented in code under `core/designsystem/`:

- **Colors** — a restrained, semantic palette (`design-system/colors/`).
- **Typography** — a clear type scale tuned for outdoor legibility
  (`design-system/typography/`).
- **Spacing** — a consistent spacing/radius scale (`design-system/spacing/`).
- **Icons** — a single coherent icon set (`design/icons/`).

## Accessibility

- Meet contrast requirements, especially for outdoor use.
- Touch targets sized for gloves/quick taps.
- Content descriptions on interactive and informative imagery.
- Support larger system font scales without breaking layout.

## Screen inventory (MVP)

Login · Home · New Report · Camera / Gallery · AI Investigation · AI Result ·
Report History. Mockups for each live in [`/design/screens`](../design/screens).
