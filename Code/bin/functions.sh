#!/usr/bin/env bash

### Commonly used functions by various top level scripts

function setup() {
local cur_dir=$PWD
#program=$(basename $0)
pdir=$(dirname "$0")
cd "$pdir"
cd ../../
# Top level of the git repo.
PROJ_HOME=$(pwd -P)
#echo "PROJ_HOME: $PROJ_HOME"
cd $cur_dir
}

function init_log_files() {
  local cur_dir=$PWD
  cd $PROJ_HOME
  log_files=`ls Code/test/data/*.log`
#  echo "Log files:"
#  echo "$log_files"
  cd $cur_dir
}

function logfile_to_test_name() {
    local lfile="$1"
    local tname=$(basename "$lfile" .log)
    echo "$tname"
}

test_names_arr=()
function make_test_names(){
  for x in $1; do
    xx=$(logfile_to_test_name "$x")
#    echo "$xx <== $x"
    test_names_arr+=( $xx )
  done
}

function print_all_test_names() {
    for x in "${test_names_arr[@]}"; do
      echo "$x"
    done

}

setup
init_log_files
make_test_names "$log_files"
#print_all_test_names "$log_files"

