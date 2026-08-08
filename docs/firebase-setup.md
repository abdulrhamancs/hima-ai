# Firebase Setup

Hima AI uses three Firebase services. All access is wrapped behind repository
interfaces in `domain/`, implemented in `data/remote/firebase/`.

| Service                     | Used for |
| --------------------------- | -------- |
| **Firebase Authentication** | Ranger login. |
| **Cloud Firestore**         | Storing structured environmental reports and their AI analysis. |
| **Firebase Storage**        | Storing incident photos captured by rangers. |

## One-time console setup

1. Create a Firebase project in the [Firebase console](https://console.firebase.google.com/).
2. Register an **Android app** using the app's package name (base package
   `com.hima.ai`; the final applicationId is set in the Gradle config).
3. Download **`google-services.json`** and place it at `app/google-services.json`
   (git-ignored — never commit it).
4. Enable the services:
   - **Authentication** → enable the sign-in method(s) you'll support.
   - **Firestore Database** → create the database.
   - **Storage** → enable the default bucket.

## Suggested Firestore shape (draft)

Finalize alongside the domain models. A starting point:

```
reports/{reportId}
  ├── reporterId: string        # ranger uid
  ├── reserveName: string
  ├── location: geopoint
  ├── photoUrl: string          # Firebase Storage download URL
  ├── incidentType: string      # from Gemini analysis
  ├── severity: string
  ├── summary: string
  ├── recommendedActions: array<string>
  ├── status: string            # e.g. submitted / reviewed
  └── createdAt: timestamp
```

Photos live in Storage under `reports/{reportId}/{photoId}.jpg`; Firestore stores
only the download URL.

## Security rules

Lock reads/writes to authenticated rangers. Draft rules and Storage rules before
any public testing — do not ship open rules.

## Where it lives in code

- Interfaces: `domain/repository/`
- Implementations + Firebase clients: `data/remote/firebase/`, `data/repository/`
- DTO ⇄ domain mapping: `data/mapper/`
