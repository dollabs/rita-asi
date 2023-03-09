function saveLog() {
    cd public/data/output
    java -jar  ../../../../../Code/doll-components/pamela-tools/target/rmq-logger-0.2.0-SNAPSHOT.jar  -e "rita" > "$*"
}

saveLog "$*"