function sendMessage() {
    cd public/upload
    java -jar ../../../../Code/doll-components/pamela-tools/target/rmq-log-player-0.2.0-SNAPSHOT.jar -e "rita" -i "$*"
    
}

sendMessage "$*"
