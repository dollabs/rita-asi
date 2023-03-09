#!/usr/bin/env bash

pdir=$(dirname $0)
source "$pdir/functions.sh"

if [[ -n "$1" ]]; then
    echo "Using RMQ Log file: $1"
    echo
else
    echo "Need RMQ Log file"
    echo "Usage: $0 RMQ_file.log"
    exit 1
fi

# base_out_dir="."
# if [[ -n "$2" ]]; then
#     base_out_dir=$2
# fi

rmq_port=5672
if [[ -n "$rita_rmq_port" ]]; then
    rmq_port=$rita_rmq_port
fi

echo "Using RMQ Port: ${rmq_port}"

log_file=$1
base_dir=$(basename $log_file .log)

function done_exit {
kill -TERM $logger_pid
#ls -lsath
wc -l $log_file $out_log_file
echo
echo
}

trap ctrl_c INT
function ctrl_c() {
        echo "** Trapped CTRL-C"
        done_exit
}

speedup="-s 5"
#speedup="-s 10"
#speedup="" #default
#speedup="-s 50"
echo
echo "--- RMQ Log Player"
java -jar $PROJ_HOME/Code/rmq-logger-tools/rmq-log-player-0.2.0-SNAPSHOT.jar -e rita -p $rmq_port $log_file "$speedup"
RESULT=$?
echo "--- RMQ Log Player done with return $RESULT"
echo
# if [[ $RESULT != 0 ]]; then
#   done_exit
#   exit 1
# else
# ## Ad hoc number to let all components finish processing.
# wtime=30
#   echo "Waiting for all components to finish processing $wtime seconds"
#   sleep $wtime
#   done_exit
#   # process_rmq_log
# fi
