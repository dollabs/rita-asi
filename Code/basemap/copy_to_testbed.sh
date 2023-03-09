#!/usr/bin/env bash

if [[ -n "$asist_testbed" ]]; then
    echo "Using testbed home as $asist_testbed"
    echo
else
    echo "Need env var asist_testbed set to testbed directory"
    echo $asist_testbed
    exit 1
fi

to=$asist_testbed/Tools/basemap

cp -a Dockerfile Falcon.json Sparky.json Pipfile Pipfile.lock Readme.md make_asist_base_map.sh make_asist_base_map_with_docker.sh $to/

echo "Done $0"