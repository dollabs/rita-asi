#!/usr/bin/env bash

set -e
set -u

if [[ -n ${asist_testbed+x} ]]; then
  echo "Found asist_testbed dir:  $asist_testbed"
  if [[ ! -d ${asist_testbed} ]]; then
    echo "Given asist_testbed dir does not exists: $asist_testbed"
    exit 1
  fi
fi

function stop_mqtt() {
  echo "Stopping up the MQTT broker"
  pushd "$asist_testbed"/mqtt
  docker-compose down
  popd
}

function stop_rita() {
  echo "Bring down the Doll/MIT Rita Agent"
  pushd $asist_testbed/Agents/Rita_Agent
  docker-compose down --remove-orphans
  popd
}

stop_rita
stop_mqtt
docker ps -a
