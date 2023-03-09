#!/bin/bash
# DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
# Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

RABBITMQ_HOST="${RABBITMQ_HOST:=localhost}"

while ! nc -z "$RABBITMQ_HOST" 5672; do
  echo "Waiting for RMQ "$RABBITMQ_HOST""
  sleep 1
done

echo "Starting CMD:"
echo "$@"
$@
