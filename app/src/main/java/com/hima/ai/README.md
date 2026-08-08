# Package: `com.hima.ai`

Base package for the Hima AI app. Organized **by architectural layer first**,
with the UI split **by feature**. Dependencies point inward:
`presentation → domain ← data`. See
[`docs/architecture.md`](../../../../../../../docs/architecture.md) and
[`docs/project-structure.md`](../../../../../../../docs/project-structure.md).

```
com/hima/ai/
├── core/           # Cross-cutting foundation (design system, navigation, utils)
│   ├── common/         # Result/Resource wrappers, constants, shared extensions
│   ├── designsystem/   # Compose theme + reusable components + icons
│   │   ├── theme/
│   │   ├── component/
│   │   └── icon/
│   ├── navigation/     # NavHost, routes, nav args
│   └── util/           # Formatters, date/location helpers
│
├── data/           # How data is fetched/stored — implements domain contracts
│   ├── remote/
│   │   ├── firebase/   # Auth, Firestore, Storage
│   │   └── gemini/     # Gemini API client + models
│   ├── repository/     # Repository implementations
│   ├── model/          # DTOs
│   └── mapper/         # DTO ⇄ domain converters
│
├── domain/         # Pure-Kotlin business layer (no framework deps)
│   ├── model/          # Entities: Report, AiAnalysis, User…
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Single-purpose business actions
│
├── presentation/   # Compose UI, one package per screen (MVVM)
│   ├── auth/  home/  report/  camera/  investigation/  result/  history/
│
└── di/             # Dependency-injection modules
```

Each `presentation/<feature>/` package holds its `XxxScreen`, `XxxViewModel`,
`XxxUiState`, and a local `components/` for feature-private composables.
