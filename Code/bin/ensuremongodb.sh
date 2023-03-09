#!/bin/bash
# DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
# Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

MONGO_HOST="${MONGO_HOST:=localhost}"
while ! nc -z "$MONGO_HOST" 27017; do
  sleep 1
done

$@
