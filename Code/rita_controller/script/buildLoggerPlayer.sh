function buildPlayerLogger() {
    cd ../doll-components/pamela-tools
    boot uber-rmq-logger
    java -jar target/rmq-logger-0.2.0-SNAPSHOT.jar --help
    boot uber-log-player
    java -jar target/rmq-log-player-0.2.0-SNAPSHOT.jar --help
}

buildPlayerLogger