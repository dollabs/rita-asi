#!/usr/bin/env bash

## Assumes docker containers are current.

if [[ -n "$1" ]]; then
  echo "Using Experiment dir: $1"
  echo
else
  echo "Need Experiment dir:"
  echo "Usage: $0 exp_dir [rmq_port] [parallel_id]"
  exit 1
fi
exp_dir=$1

rmq_port=6672
if [[ -n "$2" ]]; then
  rmq_port=$2
fi
echo "Using RMQ_PORT : $rmq_port"
echo
export RMQ_PORT=$rmq_port ## For docker

par_id="not_parallel"
if [[ -n "$3" ]]; then
  par_id=$3
fi
echo "Using project / exp parallel id : $par_id"
echo

pushd .
cd $exp_dir
exp_dir=$(pwd -P)
popd
echo
echo "Exp Dir: $exp_dir"

dte=$(date "+%B-%Y-%d--%H")
echo "------- Long Eval Run $dte"
#echo "In Dir $PWD"

pushd .
pdir=$(dirname "$0")
cd $pdir
cd ../../../../
# Top level of the git repo.
PROJ_HOME=$(pwd -P)
echo "PROJ_HOME: $PROJ_HOME"
popd

log_parent=$(dirname $exp_dir)
exp_dir_name=$(basename $exp_dir)
nightly_dir="${log_parent}/data-${dte}/${exp_dir_name}"
export SERVICE_LOGS_DIR="$nightly_dir/"
#nightly_dir="$PROJ_HOME/Code/test/nightly-logs/dev-test-2"
mkdir -p $nightly_dir
echo "Today's logs are here: $nightly_dir"

function disk_cleanup() {
  echo "Disk usage before prune and cleanup"
  df -h /
  docker system prune -f
  docker volume prune -f
  echo "Disk usage after cleanup"
  df -h /
}

## Startup docker
function start_docker() {

  trial_id=$1
  docker_tag="$dte-$trial_id"
  export DB_DATA_DIR="-$dte"
  export SERVICE_LOGS_TAG="-$docker_tag"
  echo "Service Logs: $SERVICE_LOGS_TAG "
  echo "Mondo DB    : $DB_DATA_DIR"

  echo "In dir $PWD"
  docker-compose -f $PROJ_HOME/Code/docker-compose.yml -p $par_id up -d
  up_log="$nightly_dir/compose-up.log"
  docker-compose -f $PROJ_HOME/Code/docker-compose.yml -p $par_id up >$up_log &
  echo "Docker compose up log: $up_log"
  wtime=60
  echo "Wait for docker to start $wtime seconds"
  sleep $wtime
  echo
}

function stop_docker() {
  echo "In Dir $PWD"
  cur_dir=$PWD
  cd $PROJ_HOME/Code
  echo "In dir $PWD"
  ## Stop docker
  docker-compose -f $PROJ_HOME/Code/docker-compose.yml -p $par_id down
  echo "----- Done Nightly test $dte"
  cd $cur_dir
}

function publish_startup_rita() {
  log_file=$1
  echo
  echo "Publish Startup RITA Using RMQ Port: ${rmq_port}"
  echo "Publish Startup RITA Exp Dir: ${exp_dir}"
  echo "Publish Startup Log file: ${log_file}"
  echo
  java -jar $PROJ_HOME/Code/doll-components/target/experiment-control.jar -p $rmq_port -x $exp_dir -l $log_file
  wtime=5
  echo "Wait $wtime after experiment_control"
}

function publish_m7_ground_truth() {
#    gtf="/Volumes/projects/RITA/HSR-data-mirror/study-2_2021.06-rmq/m7_ground_truth_prompt.log"
    gtf="/nfs/projects/RITA/HSR-data-mirror/study-2_2021.06-rmq/m7_ground_truth_prompt.log"
    echo "Publishing M7 Ground Truth prompt ${gtf}"
    set -x
    java -jar $PROJ_HOME/Code/rmq-logger-tools/rmq-log-player-0.2.0-SNAPSHOT.jar -e rita -p $rmq_port ${gtf}
    set +x
}

echo "wd: $PWD"
test_files=$(ls $exp_dir/*.log)
#test_files="$PROJ_HOME/Code/test/data/jun10TB43-run7.log"
#test_files="$exp_dir/HSRData_TrialMessages_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-150_Team-na_Member-61_Vers-3.log"

error_collecting_logs=0
got_ctrl_c=0
function collect_rmq_logs() {
  echo
  echo "----- Collecting data: $dte"
  #sleep 60
  ## For each testfile, play RMQ data and collect output
  #  echo -e "Test files:\n"
  #  echo "$test_files"
  #  echo

  for f in $test_files; do

    if [ "$got_ctrl_c" -eq 1 ]; then
      echo "Got Control-C breaking out of file loop"
      break
    fi
    echo "--- For Trial log file $f ----------"

    trial_id=$(basename "$f" | cut -d'_' -f 3)
    #    echo $trial_id
    start_docker "$trial_id"

    if [ "$got_ctrl_c" -eq 1 ]; then
      echo "Got Control-C breaking out of file loop"
      break
    fi

    publish_startup_rita $f
    sleep 2
    publish_m7_ground_truth
    if [ "$got_ctrl_c" -eq 1 ]; then
      echo "Got Control-C breaking out of file loop"
      break
    fi
    export rita_rmq_port=$rmq_port
    $PROJ_HOME/Code/bin/collect-test-data-for.sh $f $nightly_dir
    RESULT=$?
    if [[ $RESULT != 0 ]]; then
      echo "####### Error collecting logs ! #######"
      error_collecting_logs=1
    fi

    stop_docker

    echo
    echo
  done
}

trap ctrl_c INT
function ctrl_c() {
  got_ctrl_c=1
  echo "** Trapped CTRL-C "
  echo $@
  stop_docker
}
collect_rmq_logs

#for f in $test_files; do
#  set -x
#  publish_startup_rita $f
#echo $f
#done
