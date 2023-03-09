function compileMisson() {
    cd ../../Code/data/
    pamela --input mission.pamela --json-ir --output mission.pamela.json-ir build
    # pamela --input mission1.pamela --json-ir --output mission1.pamela.json-ir build
    # pamela --input mission2.pamela --json-ir --output mission2.pamela.json-ir build
}

compileMisson