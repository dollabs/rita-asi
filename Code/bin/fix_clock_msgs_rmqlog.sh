#!/usr/bin/env bash

temp_dir="fixed_clock_rkey"
mkdir -p "$temp_dir"

files=$(ls ./*.log)
#files="HSRData_TrialMessages_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-63_Team-na_Member-32_Vers-1.log"
#cd "$temp_dir"

for f in $files; do
  echo "$f"
  "$ritagit"/Code/bin/fix_clock_msgs_rmqlog.py "$f" "$temp_dir"
  echo
done
