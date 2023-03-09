#!/usr/bin/env bash

#if [[ -n ${1+x} ]]; then
#  metadata_file=$1
#  echo "metadata file:  $metadata_file"
#  else
#    echo "Need metadata file."
#    exit 1
#fi

#MQTT_LOCAL_PORT=2883

if [[ -n ${asist_testbed+x} ]]; then
  echo "Found asist_testbed dir:  $asist_testbed"
  if [[ ! -d ${asist_testbed} ]]; then
    echo "Given asist_testbed dir does not exists: $asist_testbed"
    exit 1
  fi
fi

if [[ -n ${ritagit+x} ]]; then
  echo "Found ritagit dir:  $ritagit"
  if [[ ! -d ${ritagit} ]]; then
    echo "Given ritagit dir does not exists: $ritagit"
    exit 1
  fi
fi

#function run_metadata() {
#  $asist_testbed/Tools/replayers/elkless_replayer/elkless_replayer -p ${MQTT_LOCAL_PORT} $1
#}

test_files=$(ls $PWD/*.log)
#test_files="./NotHSRData_TrialMessages_Trial-T000370_Team-TM000048_Member-na_CondBtwn-ASI-ALL_CondWin-na_Vers-1.log"
function do_stuff() {

  echo -e "Test files:\n"
  echo "$test_files"
  echo

  for f in $test_files; do
    echo "For echo $PWD $f"

    $ritagit/Code/asist_testbed_tests/start_containers.sh
    sleep 3
    set -x

    java -jar $ritagit/Code/rmq-logger-tools/rmq-log-player-0.2.0-SNAPSHOT.jar -e rita -p 6672 -s 1 $f
    set +x
    sleep 40
    $ritagit/Code/asist_testbed_tests/stop_containers.sh
  done

}

do_stuff
