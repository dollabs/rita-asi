#!/usr/bin/env bash

## Setup PROJ_HOME path
cur_dir=$PWD
pdir=$(dirname $0)
cd $pdir
cd ../../
# Top level of the git repo.
PROJ_HOME=$(pwd -P)
#echo "PROJ_HOME: $PROJ_HOME"
cd $cur_dir

## Setup which cut to use
OS=`uname`
if [ "$OS" == "Darwin" ]
then
echo "On Mac. Assuming coreutils / gcut is installed"
my_cut=gcut
else
echo "On Linux assumed"
my_cut=cut
fi

## the function
function convert_log_to_json {
rmq_log_file=$1
out_dir=$(dirname $rmq_log_file)

output_file=$(basename $rmq_log_file .log)
output_file=${output_file}.json

intermediate_file=$(basename $rmq_log_file)
intermediate_file="$out_dir/processed.${intermediate_file}"

$my_cut -d',' -f1 --complement $rmq_log_file | $my_cut -d',' -f1 --complement > $intermediate_file
$PROJ_HOME/Code/bin/rmq_log_2_json.py $intermediate_file ${out_dir}/$output_file
rm $intermediate_file
}

# Check pre-conditions
if [[ -n "$1" ]]; then
    echo "Using RMQ Log file $1"
    echo
else
    echo "Need RMQ Log file"
    echo "Usage: $0 RMQ_file.log"
    exit 1
fi

convert_log_to_json $1