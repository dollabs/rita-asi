#!/usr/bin/env bash

echo "ARGS: " "$@"
echo "ENV"
env

echo "----"
if [[ -n "$RABBITMQ_HOST" ]]; then
    RABBITMQ_OPTION="--host $RABBITMQ_HOST"
    echo "Using RMQ Host : $RABBITMQ_HOST"
    echo
else
    RABBITMQ_HOST=""
    echo "Not Using RMQ option : $RABBITMQ_HOST"
fi

if [[ -n "$MQTT_HOST" ]]; then
    MQTT_HOST_OPTION="--mqhost $MQTT_HOST"
    echo "Using MQTT Host : $MQTT_HOST"
    echo
else

    echo "Not Using MQTT_HOST option : $MQTT_HOST"
fi

CMD="/Applications/bin/ensurerabbitmq.sh java -jar ${APPDIR}/mqt2rmq/build/libs/mqt2rmq-all.jar ${RABBITMQ_OPTION} ${MQTT_HOST_OPTION}"
echo $CMD > /Applications/logs/mqt2rmq.out 2> /Applications/logs/mqt2rmq.err
$CMD
echo

