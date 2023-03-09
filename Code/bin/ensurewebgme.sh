#!/bin/bash
# DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
# Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

WEBGME_HOST="${WEBGME_HOST:=localhost}"
while ! nc -z "$WEBGME_HOST" 8888; do
  sleep 1
done

$@
