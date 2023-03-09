#!/usr/bin/env bash

## Takes component name and test_dir as input
## Copies component files to expected.
## Assumes this script dir is `Code/bin`

function usage() {
  echo "Usage: $0 test_dir component_name"
  echo "test_dir is a dir in Code/test/nightly-logs"
  echo "component_name is state-estimation. prediction-generator etc"
  exit 1
}

if [[ -z $1 ]]; then
  usage
fi

if [[ -z $2 ]]; then
  usage
fi

test_dir=$1
component_name=$2

echo "In Dir $PWD"
#program=$(basename $0)
pdir=$(dirname "$0")
cur_dir="$PWD"
cd "$pdir"
cd ../../
# Top level of the git repo.
PROJ_HOME=$(pwd -P)
echo "PROJ_HOME: $PROJ_HOME"
cd $cur_dir

test_files=$(ls "$PROJ_HOME"/Code/test/data/*.log)
expected_dir=$PROJ_HOME/Code/test/expected

for f in $test_files; do

  test_name=$(basename "$f" .log)
  test_component_file="$test_dir/$test_name/$component_name.json"
  expected_component_file="$expected_dir/$test_name/$component_name.json"
  #  echo $test_component_file

  if [[ -f "$test_component_file" ]]; then
    if [[ ! -f $expected_component_file ]]; then
      echo "Warn: Expected component_file $expected_component_file does not exists"

      if [[ ! -d $expected_dir/$test_name ]]; then
        echo "\"$expected_dir/$test_name\" does not exist!! Creating "
        mkdir -p "$expected_dir/$test_name"
        echo
      fi
    fi

    echo "Copying $test_component_file to $expected_component_file"
    cp -a "$test_component_file" "$expected_component_file"
    echo
  else
#    echo $PWD
    echo "Warn: test component_file $test_component_file does not exists"
  fi

done
