#!/usr/bin/env bash

function remove_metadata_web() {
  f=$1
  echo "Removing metadata-web"
  echo "before $(wc -l $f)"
  sed -i '' "/metadata-web/d" $f
  echo "after $(wc -l $f)"
}

function remove_ihmc() {
  f=$1
  echo "Removing IHMCLocationMonitorAgent"
  echo "before $(wc -l $f)"
  sed -i '' "/IHMCLocationMonitorAgent/d" $f
  echo "after $(wc -l $f)"
}

function remove_pygl() {
  f=$1
  echo "Removing PyGL"
  echo "before $(wc -l $f)"
  sed -i '' "/PyGL/d" $f
  echo "after $(wc -l $f)"
}

function remove_audio() {
  f=$1
  echo "Removing metadata/audio"
  echo "before $(wc -l $f)"
  sed -i '' "/metadata\/audio/d" $f
  echo "after $(wc -l $f)"
}

function remove_tomcat_start() {
  f=$1
  echo "Removing tomcat_textAnalyzer__start"
  echo "before $(wc -l $f)"
  sed -i '' "/tomcat_textAnalyzer\",\"sub_type\":\"start/d" $f
  echo "after $(wc -l $f)"
}

#files=$(ls *.log)
files=$(ls *.metadata)
function clean_files() {
  for f in $files; do
    echo $f
    echo
    remove_audio $f
    echo

    remove_pygl $f
    echo

#    remove_ihmc $f
#    echo

    remove_metadata_web $f
    echo

    remove_tomcat_start $f
    echo

  done
}

clean_files

## On Mac
#sed -i '' "/2021-06-05/d" HSRData_TrialMessages_Trial-T000401_Team-TM000101_Member-na_CondBtwn-2_CondWin-SaturnB_Vers-2.log
#sed -i '' "/metadata-web/d" HSRData_TrialMessages_Trial-T000401_Team-TM000101_Member-na_CondBtwn-2_CondWin-SaturnB_Vers-2.log
#sed -i '' "/2021-07-08/d" HSRData_TrialMessages_Trial-T000401_Team-TM000101_Member-na_CondBtwn-2_CondWin-SaturnB_Vers-2.log
#man sed
#sed -i "/2021-07-08/d" HSRData_TrialMessages_Trial-T000401_Team-TM000101_Member-na_CondBtwn-2_CondWin-SaturnB_Vers-2.log
#sed -i /2021-07-08/d HSRData_TrialMessages_Trial-T000401_Team-TM000101_Member-na_CondBtwn-2_CondWin-SaturnB_Vers-2.log
#sed /2021-07-08/d HSRData_TrialMessages_Trial-T000401_Team-TM000101_Member-na_CondBtwn-2_CondWin-SaturnB_Vers-2.log
