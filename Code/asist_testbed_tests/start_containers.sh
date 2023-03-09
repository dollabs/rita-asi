#!/usr/bin/env bash

set -e
set -u

MQTT_LOCAL_PORT=2883

if [[ -n ${asist_testbed+x} ]]; then
  echo "Found asist_testbed dir:  $asist_testbed"
  if [[ ! -d ${asist_testbed} ]]; then
    echo "Given asist_testbed dir does not exists: $asist_testbed"
    exit 1
  fi
fi

function start_mqtt() {
  echo "Bringing up the MQTT broker local port ${MQTT_LOCAL_PORT}"
  pushd "$asist_testbed"/mqtt
#  docker-compose up -d
  docker-compose run -d --publish ${MQTT_LOCAL_PORT}:1883 --name mosquitto mosquitto
  echo "Finished launching the Mosquitto container, waiting for 5 seconds to ensure " \
    "everything works properly..."
  sleep 5
  popd
}

function start_rita() {
    echo "Bringing up the Doll/MIT Rita Agent"
    pushd "$asist_testbed"/Agents/Rita_Agent
        echo "$PWD: Starting Rita Agent"
        dte=$(date "+%B-%Y-%d--%H")
        log_dir="logs-${dte}"
        export SERVICE_LOGS_DIR="$PWD/$log_dir/"
        echo "${SERVICE_LOGS_DIR} Log dirs for this instantiation of Rita"
        docker-compose up -d
    popd
    echo "Waiting few seconds for Rita Agent containers to be up and running."
    sleep 40
    echo "Rita Containers now should be up and running."
}

start_mqtt
start_rita
docker ps -a