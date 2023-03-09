#!/usr/bin/env bash

# exit on any error
#set -e # Exits when return value of a function is 1
# and warn about unset variables
set -u

## Use metadata dir from command line if given
metadata_dir="./"
if [[ -n ${1+x} ]]; then
  metadata_dir=$1
  echo "metadata dir:  $metadata_dir"
  if [[ ! -d ${metadata_dir} ]]; then
    echo "Given metadata dir does not exists: $metadata_dir"
    exit 1
  fi
fi

# Assume the script is in a well-known location in the ritagit directory
if [ -z ${ritagit+x} ]; then
  ritagit="$(dirname "$(dirname "$(dirname $0)")")"
fi

# Let's see if the testbed repo is a sibling directory
if [ -z ${asist_testbed+x} ]; then
  my_guess1="$(dirname $ritagit)/adminless-testbed"
  my_guess2="$(dirname $ritagit)/testbed"
  echo $my_guess1 $my_guess2
  if [ -d $my_guess1 ]; then
    asist_testbed=$my_guess1
  elif [ -d $my_guess2 ]; then
    asist_testbed=$my_guess2
  fi
fi

echo "Using env ritagit = $ritagit"
echo "Using env asist_testbed = $asist_testbed"
echo
#EXT=".json"
EXT=".metadata"

function replay_metadata() {
  json_f=$1
  log_f="$(basename "$json_f" "$EXT").log"

  if [[ -f "$log_f" ]]; then
    echo "Exists: $log_f"
    return 0
  fi
  echo "Working on $log_f"

  echo "Converting testbed .metadata or .json to rmq log: " "$log_f"

  java -jar "$ritagit"/Code/rmq-logger-tools/rmq-logger-0.2.0-SNAPSHOT.jar --clear-queue true -e rita >"$log_f" &
  logger_pid=$!
  echo "Logger pid $logger_pid"
  echo "Waiting 5 seconds"
  sleep 5
  echo
  echo "Starting Playback \"$json_f\""
  echo "NOTE: We are filtering out the rita_anomaly messages"
  #  python3 "$asist_testbed"/Tools/replayers/testbed_playback/Playback.py "$json_f" # For real time playback. Very slow and error prone.
  python3 "$asist_testbed"/Tools/replayers/elkless_replayer/elkless_replayer "$json_f" --exclude_sources metadata-web --exclude_topics "agent/ASI_DOLL_TA1_RITA/rita_anomaly"
  echo "Done Playback \"$json_f\""

  echo "Stopping rmq logger $logger_pid"
  kill -TERM $logger_pid

  echo "Waiting for $logger_pid to finish"
  wait $logger_pid
  echo "Logger finished with exit code $?"
  echo "nLines: "
  wc -l "$json_f" "$log_f"
  echo
}

function start_mq2rmq_relay() {
  if jps | grep mqt2rmq > /dev/null
  then
    echo "Error: MQT2RMQ is already runnning. Multiple relays will result in duplications."
    echo "Review the existing process and kill it if it's an orphan"
    exit 1
  fi
  echo "Starting MQTT to RMQ Replay"
  java -jar "$ritagit"/Code/mqt2rmq/build/libs/mqt2rmq-all.jar --host localhost --mqhost localhost &
  mqpid=$!
  echo "mtq relay pid $mqpid"
  echo "Wait 5"
  sleep 5
}

function stop_mq2rmq_replay() {
echo "Stopping mqpid $mqpid"
kill -TERM $mqpid
}

function replay_metadata_dir() {
    pushd . > /dev/null
    cd $metadata_dir
    echo "Reading metadata files from: $PWD"
    IFS=$'\n'
    files=$(ls *$EXT)
    for f in $files; do
      echo "metadata file: $f"
      replay_metadata "$f"
    done
    popd > /dev/null
}

start_mq2rmq_relay
echo
replay_metadata_dir
echo
stop_mq2rmq_replay
echo "Done $0"

#files="ASIST_data_study_id_000001_condition_id_000001_trial_id_000001_messages.json"
#files="ASIST_data_study_id_000001_condition_id_000006_trial_id_000004_messages.json ASIST_data_study_id_000001_condition_id_000006_trial_id_000011_messages.json ASIST_data_study_id_000001_condition_id_000007_trial_id_000007_messages.json ASIST_data_study_id_000001_condition_id_000007_trial_id_000012_messages.json"
#files="HSRData_CompetencyTestMessages_CondBtwn-na_CondWin-na_Trial-na_Team-na_Member-26_Vers-1.metadata"
