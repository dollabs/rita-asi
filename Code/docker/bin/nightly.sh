#!/usr/bin/env bash

## Assumes docker containers are current.

dte=$(date "+%B-%Y-%d--%H-%M-%S")
echo "------- Nightly Test $dte"
#echo "In Dir $PWD"

pdir=$(dirname "$0")
cd $pdir
cd ../../../
# Top level of the git repo.
PROJ_HOME=$(pwd -P)
echo "PROJ_HOME: $PROJ_HOME"

nightly_dir="$PROJ_HOME/Code/test/nightly-logs/$dte"
#nightly_dir="$PROJ_HOME/Code/test/nightly-logs/dev-test-2"
mkdir -p $nightly_dir
echo "Today's logs are here: $nightly_dir"
export TEST_MODE='-t'
echo "Regression TestMode ${TEST_MODE}"

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
  disk_cleanup
  export SERVICE_LOGS_TAG="-$dte"
  export DB_DATA_DIR="-$dte"
  cur_dir=$PWD
  cd $PROJ_HOME/Code
  echo "In dir $PWD"
  docker-compose up -d
  up_log="$nightly_dir/compose-up.log"
  docker-compose up >$up_log &
  echo "Docker compose up log: $up_log"
  wtime=60
  echo "Wait for docker to start $wtime seconds"
  sleep $wtime
  echo
  cd $cur_dir
}

function stop_docker() {
  echo "In Dir $PWD"
  cur_dir=$PWD
  cd $PROJ_HOME/Code
  echo "In dir $PWD"
  ## Stop docker
  docker-compose down
  echo "----- Done Nightly test $dte"
  cd $cur_dir
}

SPM_PID=""
function start_spm_d() {
  spm &
  SPM_PID=$!
  echo "SPM_PID $SPM_PID"
  echo
}

function stop_spm_d() {
  kill -TERM "$SPM_PID"
  echo "Stopped SPM $SPM_PID"
}

function start_spm() {
  cur_d=$PWD
  cd $PROJ_HOME/Code
  spm start
  cd $cur_d
  sleep 3
  spm list
}

function stop_spm() {
  cur_d=$PWD
  cd $PROJ_HOME/Code
  spm stop
  sleep 4
  spm list
  cd $cur_d
}

test_files=$(ls $PROJ_HOME/Code/test/data/*.log)
#test_files="$PROJ_HOME/Code/test/data/ASIST_data_study_id_000001_condition_id_000001_trial_id_000001_messages.log"
#test_files="$PROJ_HOME/Code/test/data/jun10TB43-run7.log"
#test_files=$(ls $PROJ_HOME/Code/test/data/sept-2020-28-falcon-*.log)
error_collecting_logs=0
function collect_rmq_logs() {
  echo
  echo "----- Collecting data: $dte"
  #sleep 60
  ## For each testfile, play RMQ data and collect output
  echo -e "Test files:\n"
  echo "$test_files"
  echo

  for f in $test_files; do
    echo "--- For Trial log file $f ----------"

    start_docker
#    start_spm

    $PROJ_HOME/Code/bin/collect-test-data-for.sh $f $nightly_dir
    RESULT=$?
    if [[ $RESULT != 0 ]]; then
      echo "####### Error collecting logs ! #######"
      error_collecting_logs=1
    fi

    stop_docker
#    stop_spm

    echo
    echo
  done
}

## For each collected output, run individual tests
some_tests_failed=0
function compare_test_output() {
  echo

  for f in $test_files; do
    echo "----- Validate Trial $(basename $f) ----------"
    test_name=$(basename $f .log)
    expected_dir=$PROJ_HOME/Code/test/expected/$test_name
    test_dir="$nightly_dir"
#    echo "expected_dir: $expected_dir"
#    echo "test_dir: $test_dir"
#    echo

    $PROJ_HOME/Code/bin/validate-test-data-for.sh $expected_dir $test_dir
    RESULT=$?
    if [[ $RESULT != 0 ]]; then
      echo "--- Test: Compare output failed! $(basename $f)"
      some_tests_failed=1
    fi

    echo
  done

}

collect_rmq_logs
compare_test_output

if [[ $some_tests_failed != 0 ]]; then
  exit 1
fi

if [[ $error_collecting_logs != 0 ]]; then
  exit 1
fi
