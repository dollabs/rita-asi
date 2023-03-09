function pull {
    cd ../../
    git checkout master
    git pull
    git submodule update --init --recursive
}

pull