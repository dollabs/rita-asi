#!/bin/sh

ant webstart-test && \
rsync -avz --delete ./signed/ r@fimfinder.net://home/r/proj/genesisWebStart && \
javaws http://fimfinder.net/genesisWebStart/genesis.jnlp
