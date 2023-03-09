#!/usr/bin/env bash

## Validates output for all the trials and for all components.

pdir=$(dirname "$0")
# shellcheck source=./functions.sh
source "$pdir/functions.sh"

function usage() {
  echo "Script to compare output for all trials / tests and for all components"
  echo
  echo "Usage $0 test_dir"
  echo
  echo "For test_dir see $PROJ_HOME/test/nightly-logs"
  echo
  echo "Example: $0 ./test/nightly-logs/2020-08-19T16:56-04:00"
  echo
}

if [[ -z $1 ]]; then
  usage
  echo "test_dir required"
  exit 1
fi

test_dir=$(cd "$1" && echo $PWD)

for x in "${test_names_arr[@]}"; do
  expected_log_dir="$PROJ_HOME/Code/test/expected/$x"
  echo "#### Validate Trial \"$x\""
  echo
#  ls -d "$expected_log_dir"
#  ls -d "$test_dir"
  $PROJ_HOME/Code/bin/validate-test-data-for.sh "$expected_log_dir" $test_dir -v
done

