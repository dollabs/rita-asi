#!/usr/bin/env bash

# A script to replay all RMQ log files in a directory in parallel using docker.
echo "Start $(date)"
rmq_log_dir="./"
rmq_port=6671
project_dir=
log_files=

dte=$(date '+%B-%Y-%d--%H')
project_dir="run-${dte}"
my_dir=$(dirname $0)
check_rita="${my_dir}/check_rita.sh"
echo "My dir ${my_dir}"

par_name="par_"
par_counter=1
rmq_port=6671

#${check_rita}

if [[ -n "$1" ]]; then
  rmq_log_dir=$1
fi

function set_log_files() {
  pushd . >/dev/null
  cd $rmq_log_dir
  log_files=$(ls *.log)
  ret=$?
  if [[ $ret -eq 0 ]]; then
      log_files=$(ls *.log | sort )
  fi  
  
  popd >/dev/null
#  echo "ret ${ret}"
  if [[ ${ret} -gt 0 ]]; then
      echo "No log files"
      exit 1
  fi
  
}

function print_config() {
  echo "rmq_log_dir: ${rmq_log_dir}"
  echo "project_dir: ${project_dir}"
  echo "RMQ Log files"
  for f in ${log_files}; do
    echo "${f}"
  done
}

function do_check_rita() {
  echo
  for f in ${log_files}; do
    #    echo "${f}"

    rmq_log_file=${rmq_log_dir}/${f}
    trial_dir=$(basename ${f} .log)
    run_dir="${project_dir}/${trial_dir}"
    proj_name="${par_name}${par_counter}"
    
    echo "RMQ Log file: ${rmq_log_file}"
    echo "rmq_port ${rmq_port}"
    echo "proj_name ${proj_name}"
    echo "Trial dir: ${run_dir}"
    echo
    mkdir -p ${run_dir}
    
    echo "check_rita ${rmq_log_file} ${rmq_port} ${proj_name} ${run_dir}"
    ${ritagit}/Code/evaluations/study-3/bin/check_rita.sh ${rmq_log_file} ${rmq_port} ${proj_name} ${run_dir} | tee ${run_dir}/${trial_dir}.out &
    echo
    
    ((par_counter = par_counter + 1))
    ((rmq_port = rmq_port + 1))
  done
}

set_log_files
print_config
do_check_rita
echo "Stop $(date)"
