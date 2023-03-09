function runMqt2rmq {
    cd ../../Code/mqt2rmq
    java -jar build/libs/mqt2rmq-all.jar --host localhost --mqhost localhost
}

runMqt2rmq

