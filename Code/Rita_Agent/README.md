### Rita Agent Integration and Release for Evaluation.

This document is intended for instantiating `Rita_Agent` with _Testbed_ (`Asist Testbed`).

#### Rita_Agent release directory contains following artifacts.
 * This README.md
 * `docker-compose.yml`
 * `settings.env` file. This is used by docker to initialize enviroment variables used by containers.

#### Instantiating Rita Agent with Testbed
 * Pull latest version as `docker-compose docker-compose.yml pull`
 * In `settings.env` update `MQTT_HOST` to point to the appropriate IP or HOSTNAME for the MQTT server.
 * Following dirs and log files are created by `Rita_Agent` and contain useful content.
   * `Agents/Rita_Agent/tailer_service_logs/rmq.log`
   * `Agents/Rita_Agent/clojure_service_logs/*.log`
   * Mongo DB files `Agents/Rita_Agent/mongo_data_db`
   * MQT2RMQ Relay `Agents/Rita_Agent/mqtt_service_logs/*`

We are interested in receiving `tailer_service_logs/rmq.log` and `clojure_service_logs/*.log` to ensure our agent is operating as expected.


