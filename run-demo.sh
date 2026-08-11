#!/usr/bin/env bash
#
# One command to bring up everything Hima AI needs for a demo or a test run.
#
#   ./run-demo.sh              start the backend, then build + install + launch the app
#   ./run-demo.sh --backend    start the backend only
#   ./run-demo.sh --check      verify the backend is reachable, change nothing
#
# Why this exists: the AI analysis flow — the centrepiece of the app — goes
# through the Node backend in backend/. If that server isn't running, every
# analysis fails with "Couldn't reach the server". Forgetting to start it is
# the single easiest way to break the demo, so this removes the need to
# remember.
set -euo pipefail

cd "$(dirname "$0")"

BACKEND_PORT="${PORT:-5000}"
BACKEND_URL="http://localhost:${BACKEND_PORT}"
LOG_FILE="backend/backend.log"

green() { printf '\033[0;32m%s\033[0m\n' "$1"; }
red()   { printf '\033[0;31m%s\033[0m\n' "$1"; }
warn()  { printf '\033[0;33m%s\033[0m\n' "$1"; }

backend_is_up() {
  curl -fsS --max-time 2 "${BACKEND_URL}/" >/dev/null 2>&1
}

require_env() {
  if [ ! -f backend/.env ]; then
    red "backend/.env is missing."
    echo "It must define GEMINI_API_KEY, SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY"
    echo "and NASA_FIRMS_MAP_KEY. Without GEMINI_API_KEY the backend refuses to boot."
    exit 1
  fi
  for key in GEMINI_API_KEY SUPABASE_URL; do
    if ! grep -q "^${key}=" backend/.env; then
      red "backend/.env has no ${key} — the analysis flow will fail."
      exit 1
    fi
  done
}

start_backend() {
  if backend_is_up; then
    green "Backend already running on ${BACKEND_URL}"
    return
  fi

  require_env

  if [ ! -d backend/node_modules ]; then
    warn "Installing backend dependencies (first run)…"
    (cd backend && npm install --silent)
  fi

  echo "Starting backend on ${BACKEND_URL} (logging to ${LOG_FILE})…"
  (cd backend && node index.js > "../${LOG_FILE}" 2>&1 &)

  # Give it a moment, then confirm it actually came up rather than assuming.
  for _ in $(seq 1 20); do
    if backend_is_up; then
      green "Backend is up on ${BACKEND_URL}"
      return
    fi
    sleep 0.5
  done

  red "Backend did not come up within 10s. Last lines of ${LOG_FILE}:"
  tail -n 20 "${LOG_FILE}" || true
  exit 1
}

check_only() {
  if backend_is_up; then
    green "Backend is reachable at ${BACKEND_URL}"
    echo
    echo "The Android app reaches it at http://10.0.2.2:${BACKEND_PORT}/ (emulator alias for this host)."
    echo "On a physical device that address will NOT work — set BACKEND_BASE_URL in"
    echo "local.properties to this machine's LAN IP, e.g. http://192.168.1.x:${BACKEND_PORT}/"
  else
    red "Backend is NOT reachable at ${BACKEND_URL}"
    echo "Start it with: ./run-demo.sh --backend"
    exit 1
  fi
}

install_and_launch() {
  echo "Building and installing the app…"
  ./gradlew installDebug

  if command -v adb >/dev/null 2>&1; then
    adb shell monkey -p com.hima.ai -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
    green "App installed and launched."
    echo
    echo "Watch API failures live with:"
    echo "  adb logcat -s HimaApi"
  else
    warn "adb not on PATH — app built and installed via Gradle, launch it manually."
  fi
}

case "${1:-}" in
  --check)   check_only ;;
  --backend) start_backend ;;
  "")        start_backend; install_and_launch ;;
  *)         echo "Usage: $0 [--backend|--check]"; exit 1 ;;
esac
