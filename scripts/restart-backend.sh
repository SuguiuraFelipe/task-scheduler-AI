#!/bin/bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
JAR_PATH="$BACKEND_DIR/target/task-scheduler-ai-1.0.0.jar"
LOG_PATH="${TMPDIR:-/tmp}/taskscheduler-backend.log"

cd "$PROJECT_ROOT"

if [ -f "$PROJECT_ROOT/.env" ]; then
  set -a
  source "$PROJECT_ROOT/.env"
  set +a
fi

if lsof -nP -iTCP:8080 -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Stopping existing backend on port 8080..."
  PIDS="$(lsof -tiTCP:8080 -sTCP:LISTEN || true)"
  if [ -n "$PIDS" ]; then
    kill $PIDS || true
    sleep 2

    REMAINING_PIDS="$(lsof -tiTCP:8080 -sTCP:LISTEN || true)"
    if [ -n "$REMAINING_PIDS" ]; then
      kill -9 $REMAINING_PIDS || true
      sleep 1
    fi
  fi
fi

echo "Building backend jar..."
cd "$BACKEND_DIR"
mvn -q -DskipTests package

echo "Starting fresh backend..."
nohup java -jar "$JAR_PATH" > "$LOG_PATH" 2>&1 &
sleep 5

echo "Health check:"
curl --max-time 10 -sS http://127.0.0.1:8080/actuator/health
echo
echo "Backend log: $LOG_PATH"
