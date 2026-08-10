# Gemini Integration

Gemini is the AI engine behind the core flow: a ranger's incident photo is sent
to Gemini, which returns a **structured environmental analysis** used to build
the report.

## The pipeline

```
Photo (camera/gallery)
   └─> Android sends authenticated multipart request to Hima backend
        └─> backend sends image (+ context) to Gemini
             └─> backend returns structured analysis
                  └─> Android maps it to AiAnalysis and shows the result
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

- Store `GEMINI_API_KEY` only in the backend's private environment
  (`backend/.env` locally or the hosting provider's secret configuration).
- Never expose the key through Android `BuildConfig`, resources, source code,
  or network requests from the device.
- Keep the model name, prompt, and response schema in `backend/config/gemini.js`.

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

- Gemini client, prompt, and response schema: `backend/config/gemini.js`
- Android backend API contract: `data/remote/backend/HimaBackendApi.kt`
- Android response mapping: `data/repository/BackendAiAnalysisRepository.kt`
