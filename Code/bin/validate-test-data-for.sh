#!/usr/bin/env bash

## Shell implementation that uses diff to compare formatted json output.
## If this turns out to be brittle, consider clojure diff to compare json output
## Note: clojure diff is much better than python diff

### Validates output for a test / trial and for all the components

pdir=$(dirname "$0")
source "$pdir/functions.sh"

function usage() {
  echo "Script to compare output of all components against a single test / trial. "
  echo
  echo "Usage $0 expected_log_dir test_dir"
  echo
  echo "For expected_log_dir see $PROJ_HOME/test/expected"
  echo "For test_dir see $PROJ_HOME/test/nightly-logs"
  echo
  echo "Example: $0 test/expected/sept-2020-28-falcon-easy ./test/nightly-logs/2020-08-19T16:56-04:00"
  echo
}

if [[ -z $1 ]]; then
  usage
  echo "expected_dir required"
  exit 1
fi

if [[ -z $2 ]]; then
  usage
  echo "test_dir required"
  exit 1
fi

verbose=0
if [[ -n $3 ]]; then
  echo "Will print verbose diff "
  verbose=1
fi

expected_dir=$1
trial_name=$(basename "$expected_dir")
test_dir=$2
test_failed=0

if [[ "$verbose" -eq 0 ]]; then
  diff_args="-qw"
else
  diff_args="-w"
fi

function compare_files() {
  expected=$1
  test_f=$2
  test_name=$(basename "$expected" .json)

  if [[ ! -f "$test_f" ]]; then
    echo "$test_name : Test Failed -> File not found: $trial_name"
    test_failed=1
    return 1
  fi

#  echo "--- Test \"$test_name\""
if [[ "$verbose" -eq 0 ]]; then
  diff "$diff_args" "$expected" "$test_f" > /dev/null
  RESULT=$?
else
  #set -x
  diff "$diff_args" "$expected" "$test_f"
  RESULT=$?
  #set +x
fi

  if [[ $RESULT != 0 ]]; then
    echo "$test_name : Test Failed -> Files differ $trial_name"
    echo "expected file: $expected"
    echo "test file    : $test_f"
    test_failed=1
  else
    echo "$test_name : Test Passed $trial_name"
  fi
}

test_files=$(ls "$expected_dir")
#echo "Tests in: \"$expected_dir\" are:"
#echo "$(basename $test_files .json)"
#echo
#echo "--------"

for f in $test_files; do
  #echo "--- Comparing expected: $expected_dir/$f  ----------"
  #echo "---           with    : $test_dir/$f"
  #$PROJ_HOME/Code/bin/test_with_rmq_log.sh $f $nightly_dir
  #echo
  test_name=$(basename "$expected_dir")
  compare_files "$expected_dir/$f" "$test_dir/$test_name/$f"
  echo
done

if [[ $test_failed != 0 ]]; then
  exit 1
fi
