#!/usr/bin/env bash

# Deprecated. Use fix_timestamps_in_log.py
echo "Deprecated. Use fix_timestamps_in_log.py"

cur_dir=$PWD
process_dir="processed_1_col"
temp_dir="fixed_log_timestamps"

mkdir -p "$process_dir"
mkdir -p "$temp_dir"

files=$(ls *.log)
#files="HSRData_TrialMessages_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-63_Team-na_Member-32_Vers-1.log"
#cd "$temp_dir"

for f in $files; do
  echo "$f"
  echo $PWD

  gcut -d',' -f1 --complement "$cur_dir/$f" | gcut -d',' -f1 --complement >"$process_dir/$f"
  $ritagit/Code/bin/fix_timestamps_in_log.py "$process_dir/$f" "$temp_dir"

  echo
done

cd $cur_dir
