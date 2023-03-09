#!/usr/bin/env bash

pdir=$(dirname $0)
# shellcheck source=./functions.sh
#source "$pdir/functions.sh"
(cd $pdir/../doll-components ; pwd; boot -C build-jar)