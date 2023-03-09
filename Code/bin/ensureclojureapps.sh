#!/usr/bin/env bash
# DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
# Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

# These are the number of clojure components plus the tailer
EXPECTED_CONNS=6

RABBITMQ_HOST="${RABBITMQ_HOST:=localhost}"

PORT=15672

while [ "$(curl -u guest:guest "http://${RABBITMQ_HOST}:${PORT}/api/vhosts/%2F/connections" | jq length)" -lt "${EXPECTED_CONNS}" ]; do
  sleep 1
done

$@
