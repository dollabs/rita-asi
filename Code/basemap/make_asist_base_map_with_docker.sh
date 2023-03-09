#!/usr/bin/env bash

if [[ -n "$asist_testbed" ]]; then
    echo "Using testbed home as $asist_testbed"
    echo
else
    echo "Need env var asist_testbed set to testbed directory"
    echo $asist_testbed
    exit 1
fi

docker run --rm -u $(id -u ${USER}):$(id -g ${USER}) \
       -e "asist_testbed=/testbed" \
       -v $PWD:/work \
       -v$asist_testbed:/testbed asist_base_map \
       /work/make_asist_base_map.sh $@
