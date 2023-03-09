# Run the Prediction Generation Component
function prepareDB {
    mongo rita-db <<EOF
    db.predictions.deleteMany({})
EOF
}

function runPG() {
    prepareDB
    cd ../../Code/doll-components/
    java -jar target/prediction-generator.jar 
}

runPG

