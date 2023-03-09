#!/usr/bin/env bash

echo "Looking at runtime-learned-models"
set -x
sudo find . -iname 'expt.known-participants.edn' | sort
set +x
echo

sudo find . -iname 'expt.known-participants.edn' | wc -l
echo
echo
