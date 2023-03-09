#!/usr/bin/env bash

from_dir="${PWD}"
to_dir="/Volumes/projects/RITA/HSR-data-mirror/study-3_2022-rmq"

echo "Pushing RMQ log files from ${from_dir} -> ${to_dir}"
echo -n "Any key continue. Control-C to quit"
read -n 1
rsync --progress -v -v -a "${from_dir}"/*.log ${to_dir}

echo "Total rmq log files in source: ${from_dir} = $(ls -l "${from_dir}"/*.log | wc -l)"
echo "Total rmq log files in target: ${to_dir}   = $(ls -l ${to_dir}/*.log | wc -l)"
