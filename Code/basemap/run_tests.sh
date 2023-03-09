#!/usr/bin/env bash

## A script that tests generated files vs curated set of json files.


function run_test {
echo "Comparing $1 $2"
# $1 is test-file, $2 is expected
diff -q -w $1 $2
RES=$?
#echo "RES $RES"
if [[ $RES -ne 0 ]]; then
  echo "Unit test failed for $1 $2"
  exit 1
else
  echo "Test passed $1 $2"
fi

}

echo "--------------------------------"
echo "Unit Test Sparky and Falcon.json"
echo
./make_asist_base_map_with_docker.sh Falcon
./make_asist_base_map_with_docker.sh Sparky

run_test Sparky.json test/data/Sparky.json
echo
run_test Sparky.json test/data/Sparky.json
echo