#!/usr/bin/env bash

set -Eeuo pipefail

trap cleanup SIGINT SIGTERM ERR EXIT

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  kill -SIGTERM "$PID"
}

rm -rf ./spring-boot-demo-project/.conveyor-cache
java -jar ./conveyor-cli-1.0.0-executable.jar --file ./spring-boot-demo-project/conveyor.json clean archive
java -jar ./spring-boot-demo-project/.conveyor/demo-0.0.1-SNAPSHOT-executable.jar &
PID=$!
sleep 5
RESPONSE=$(curl http://localhost:8080 || true)
if [[ "$RESPONSE" != hello ]]; then
  exit 1
fi