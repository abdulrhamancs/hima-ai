# AGENTS.md

# Hima AI

## Project Overview

Hima AI is an AI-powered environmental monitoring platform built for Saudi Arabia's natural reserves.

The goal is to help rangers report environmental incidents by capturing a photo, allowing Gemini AI to analyze it, and generating a structured environmental report with recommended actions.

This project is built for a hackathon MVP and must be completed within one week.

---

## Your Role

You are my senior Android engineer and senior product designer.

You must help build a production-quality MVP while keeping development speed high.

Always prioritize clean architecture over shortcuts.

---

## MVP Scope

Only implement:

- Login
- Home
- New Report
- Camera / Gallery
- AI Investigation
- AI Result
- Report History

Do NOT implement future roadmap features unless I explicitly ask.

---

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Firebase Authentication
- Firestore
- Firebase Storage
- Google Maps
- Gemini API

---

## UI Principles

- Premium product feel
- Modern
- Minimal
- Professional
- High readability outdoors
- One-handed usability
- Large touch targets
- Consistent spacing
- Responsive layouts
- Clean hierarchy
- Minimal visual clutter

Avoid:
- Government dashboard appearance
- Generic Material templates
- Dense layouts
- Unnecessary cards
- Too many colors
- Duplicate information

---

## Design Source

The designs inside the `/design` folder are the source of truth.

Always match the approved design.

Do not invent layouts without asking.

---

## Coding Standards

- MVVM
- Reusable composables
- Small composable functions
- State hoisting
- No duplicated UI
- No hardcoded strings
- No hardcoded colors
- Follow Kotlin best practices

---

## UX Standards

Whenever discussing UI:

Always provide:

- UX reasoning
- Information hierarchy
- Accessibility
- Edge cases
- Suggested icons
- Material 3 component choices

---

## Performance

Prefer:

- LazyColumn
- Stable state
- Remember where appropriate
- Efficient recomposition

Avoid unnecessary recompositions.

---

## Definition of Done

A feature is complete only if:

- UI matches the approved design.
- Responsive.
- Accessible.
- Clean architecture.
- No duplicated code.
- Ready for backend integration.

## Working Style

- Implement one screen at a time.
- Never implement multiple unrelated features in a single response.
- If requirements are unclear, ask for clarification before writing code.
- Before coding:
  1. Explain the implementation plan.
  2. Wait for approval if the change is significant.
  3. Then implement the code.
- Keep the codebase clean, modular, and maintainable.
- Prefer reusable components over duplicated code.
- Keep commits and changes focused on a single task.
- Do not refactor unrelated code unless explicitly requested.