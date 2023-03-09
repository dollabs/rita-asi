#!/usr/bin/env bash

rmq_log_file=
rmq_port=6672
rmq_speedup=2
rmq_player_pid=
compose_file=
env_file=
project_dir="./"
project_name="par_id_0"

function usage() {
  echo "Usage: $0 rmq_log_file [rmq_port project_name project_dir ritagit compose_file env_file]"
  echo
  echo "rmq_port: is used to publish contents of rmq_log_file to this port. localhost is assumed"
  echo "project_name: unique id to distinguish one instance of docker-compose from another."
  echo "project_dir: The directory in which we will run docker compose command"
  echo "ritagit: Top level RITA git directory. Used to derive docker-compose.yml and .env file"
  echo "compose_file: To override the derived compose file."
  echo "env_file: To override the derived .env file"
  echo ""
  exit 1
}

if [[ -n "$1" ]]; then
  rmq_log_file=$1
fi

if [[ -n "$2" ]]; then
  rmq_port=$2
fi

if [[ -n "$3" ]]; then
  project_name=$3
fi

if [[ -n "$4" ]]; then
  if [[ ! -d $4 ]]; then
    echo "Given project_dir $4 does not exists"
    exit
  fi
  project_dir=$4
fi

if [[ -n "$5" ]]; then
  ritagit=$5
fi

if [[ -n "$ritagit" ]]; then
  #  echo "ritagit is set: ${ritagit}"
  compose_file="${ritagit}/Code/docker-compose.yml"
  env_file="${ritagit}/Code/.env"
fi

if [[ -n "$6" ]]; then
  compose_file=$6
fi

if [[ -n "$7" ]]; then
  env_file=$7
fi

if [[ -z "${rmq_log_file}" ]]; then
  usage
fi

if [[ -z "${ritagit}" ]]; then
  usage
fi

function print_config() {
  echo "My config"
  echo "rmq_log_file: ${rmq_log_file}"
  echo "rmq_port: ${rmq_port}"
  echo "project_name: ${project_name}"
  echo "project_dir: ${project_dir}"
  echo "ritagit: ${ritagit}"
  echo "compose_file: ${compose_file}"
  echo "env_file: ${env_file}"
  echo ""
}

function start_docker() {
  pushd . >/dev/null
  echo "Starting docker ${project_name}"
  echo
  print_config
  export RMQ_PORT=${rmq_port}
  set -x
  docker-compose --project-name ${project_name} --project-directory ${project_dir} --env-file ${env_file} -f ${compose_file} up -d
  set +x
  popd >/dev/null
}

function stop_docker() {
  pushd . >/dev/null
  echo "Stopping docker ${project_name}"
  set -x
  docker-compose --project-name ${project_name} --project-directory ${project_dir} --env-file ${env_file} -f ${compose_file} down
  set +x
  popd >/dev/null
}

function replay_log() {
  if [[ ! -f ${rmq_log_file} ]]; then
    echo "RMQ Log file ${rmq_log_file} does not exists"
  fi
  set -x
  java -jar $ritagit/Code/rmq-logger-tools/rmq-log-player-0.2.0-SNAPSHOT.jar -p ${rmq_port} -s ${rmq_speedup} ${rmq_log_file}
  set +x
}

function wait_for_rabbit() {
  ret=1
  while [ $ret -eq 1 ]; do
    nc -v -z localhost ${RMQ_PORT}
    ret=$?
    echo "Waiting for RMQ on port ${RMQ_PORT} ${ret}"
    sleep 1
  done
  echo "RMQ is ready for connections ! ${rmq_log_file}"
}

function my_exit() {
  echo "Exiting $0"
}
trap my_exit EXIT

print_config
start_docker
wait_for_rabbit
sleep 60
replay_log
sleep 60
stop_docker
echo "Done"
