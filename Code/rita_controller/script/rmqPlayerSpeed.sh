function sendMessage() {
    cd public/upload
    java -jar ../../../../Code/doll-components/pamela-tools/target/rmq-log-player-0.2.0-SNAPSHOT.jar -s $1 -e "rita" -i $2
}

sendMessage $1 $2
