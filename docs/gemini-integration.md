# Gemini Integration

Gemini is the AI engine behind the core flow: a ranger's incident photo is sent
to Gemini, which returns a **structured environmental analysis** used to build
the report.

## The pipeline

```
Photo (camera/gallery)
   └─> upload to Firebase Storage
        └─> send image (+ prompt) to Gemini API        [data/remote/gemini]
             └─> parse structured response into AiAnalysis   [data/mapper]
                  └─> show on AI Investigation → AI Result screens
```

## What Gemini returns

The prompt asks Gemini to analyze the image and return a **structured** result so
it maps cleanly onto a domain model. Target fields (finalize with the domain
model):

- `incidentType` — e.g. illegal dumping, wildfire risk, vegetation damage,
  animal distress
- `severity` — low / medium / high
- `summary` — short, human-readable description
- `recommendedActions` — ordered list of concrete steps for the ranger
- `confidence` — the model's confidence, when useful

Prefer requesting a strict JSON shape so parsing is deterministic; keep the
prompt template in the data layer, not scattered across the UI.

## Configuration

- Store the key as `GEMINI_API_KEY` in `local.properties` (git-ignored) and read
  it through the build config — never hardcode it and never commit it.
- Keep the model name and generation settings in one place in
  `data/remote/gemini/` so they're easy to tune.

## Error handling & UX

The **AI Investigation** screen represents the in-progress state. Handle and
surface these edge cases gracefully:

- No network / request timeout
- Gemini returns an unparseable or empty response
- Low-confidence or ambiguous analysis (let the ranger edit before submitting)
- Rate limiting

Wrap results in a `Result`/`Resource` type (see `core/common/`) so the ViewModel
can render loading, success, and error states cleanly.

## Where it lives in code

- Gemini client + request/response models: `data/remote/gemini/`
- Mapping to the `AiAnalysis` domain model: `data/mapper/`
- Use case orchestrating upload + analysis: `domain/usecase/`
