#!/usr/bin/env bash
# DISTRIBUTION STATEMENT C: U.S. Government agencies and their contractors.
# Other requests shall be referred to DARPA’s Public Release Center via email at prc@darpa.mil.

BOOT=`which boot`
program=$(basename $0)
dir=$(dirname $0)
cd $dir
dir=$(pwd -P)
cd ../
code=$(pwd -P)


echo "script dir = $dir"

echo "Custom script to build code/clojure-dcrypps"

echo "boot is $BOOT"
cd clojure-dcrypps
echo "Current dir $PWD"
boot build-jar
