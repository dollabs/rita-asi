#!/usr/bin/env bash

from_dir="/Volumes/projects/RITA/HSR-data-mirror/study-3_2022"
to_dir="${PWD}"

echo "Retrieving metadata files from ${from_dir} -> ${to_dir}"
echo -n "Any key continue. Control-C to quit"
read -n 1
rsync --progress -v -v -a ${from_dir}/*.metadata .

echo ""
echo "Total metadata files in source: ${from_dir} $(ls -l "${from_dir}"/*.metadata | wc -l)"
echo "Total metadata files in target: ${to_dir} $(ls -l "${to_dir}"/*.metadata | wc -l)"
