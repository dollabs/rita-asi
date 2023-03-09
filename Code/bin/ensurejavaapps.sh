#!/bin/bash
# DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
# Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

# Currently this only checks if the tailer is running and needs elaboration
while ! jps -ml | grep tailer.jar; do
  sleep 1
done

$@