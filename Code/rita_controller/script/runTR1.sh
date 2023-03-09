function runTR_1() {
    cd ../../Code/genesis-components/tom-minecraft/
    pipenv install #would take sometimes if its the 1st time doing pipenv install
    # pipenv shell
    cd gridworld
    pipenv run python rita_tom.py -m REPLAY_IN_RITA -i 1
}

runTR_1