#!/usr/bin/env bash

## Replay log file through RMQ
## Collect corresponding log file from RMQ logger
## Assumes docker containers are already started.

pdir=$(dirname $0)
# shellcheck source=./functions.sh
source "$pdir/functions.sh"

if [[ -n "$1" ]]; then
    echo "Using RMQ Log file: $1"
    echo
else
    echo "Need RMQ Log file"
    echo "Usage: $0 RMQ_file.log [out_dir]"
    exit 1
fi

base_out_dir="."
if [[ -n "$2" ]]; then
    base_out_dir=$2
fi

rmq_port=6672
if [[ -n "$rita_rmq_port" ]]; then
    rmq_port=$rita_rmq_port
fi

echo "Using RMQ Port: ${rmq_port}"

log_file=$1
base_dir=$(basename $log_file .log)
out_dir="$base_out_dir/$base_dir"
echo "Using out_dir: $out_dir"
mkdir -p $out_dir

log_file=$1
#out_file=$(basename $log_file)
out_log_file="$out_dir/rmq.log"
out_err_file="$out_dir/rmq.err"

java -jar $PROJ_HOME/Code/rmq-logger-tools/rmq-logger-0.2.0-SNAPSHOT.jar -e rita -p $rmq_port > ${out_log_file} 2> ${out_err_file} &
logger_pid=$!
echo "Logger PID $logger_pid and wait 5 secs"
sleep 5

function done_exit {
kill -TERM $logger_pid
#ls -lsath
wc -l $log_file $out_log_file
echo
echo
}

function process_rmq_log {
echo "--- Process RMQ Log file !!"
log_file=$out_log_file
$PROJ_HOME/Code/bin/rmq_log_2_json.sh $log_file $out_dir
echo

log_file="$out_dir/state-estimation.log"
grep StateEstimation $out_log_file > $log_file
$PROJ_HOME/Code/bin/rmq_log_2_json.sh $log_file $out_dir
echo

log_file="$out_dir/prediction-generator.log"
grep PredictionGenerator $out_log_file > $log_file
$PROJ_HOME/Code/bin/rmq_log_2_json.sh $log_file $out_dir
echo
echo "--- Process RMQ Log file !! Done --- "
}

trap ctrl_c INT
function ctrl_c() {
        echo "** Trapped CTRL-C"
        done_exit
}

speedup="-s 1"
#speedup="" #default
#speedup="-s 50"
echo
echo "--- RMQ Log Player"
java -jar $PROJ_HOME/Code/rmq-logger-tools/rmq-log-player-0.2.0-SNAPSHOT.jar -e rita -p $rmq_port $log_file "$speedup"
RESULT=$?
echo "--- RMQ Log Player done with return $RESULT"
echo
if [[ $RESULT != 0 ]]; then
  done_exit
  exit 1
else
## Ad hoc number to let all components finish processing.
wtime=60
  echo "Waiting for all components to finish processing $wtime seconds"
  sleep $wtime
  done_exit
  process_rmq_log
fi
