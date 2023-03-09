#!/usr/bin/env bash

if [ "$1" == "" ]; then
  echo "Usage $0 3_column_rmq_log_file.csv"
  exit 1
fi

orig_file=$1
col_name="${orig_file}.1_col.csv"

CUT_APP=gcut # On Macos;  brew install coreutils
#CUT_APP=cut # On other unices

set -x
grep 'raw-data' $orig_file | $CUT_APP --complement -d',' -f1 | $CUT_APP --complement -d',' -f1 > $col_name
set +x

echo "Sample output"
head -3 $col_name