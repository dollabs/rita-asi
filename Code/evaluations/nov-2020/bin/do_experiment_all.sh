#!/usr/bin/env bash

#dirs="exp-0002 exp-0003 exp-0004 exp-0005 exp-0006 exp-0007 exp-0008 exp-0009 exp-0010 exp-0011 exp-0012 exp-0013 exp-0014 exp-0015 exp-0016 exp-0017 exp-0018 exp-0019 exp-0020"
dirs=$(ls -d exp-*)
echo "$dirs"

pushd .
pdir=$(dirname "$0")
cd $pdir
cd ../../../../
# Top level of the git repo.
PROJ_HOME=$(pwd -P)
echo "PROJ_HOME: $PROJ_HOME"
popd

ls -lh $PROJ_HOME/Code/evaluations/nov-2020/bin/do_experiment.sh

for d in $dirs; do
echo "Starting exp: $d on $HOSTNAME `date`"
$PROJ_HOME/Code/evaluations/nov-2020/bin/do_experiment.sh $d | tee "$d-all.txt"
echo "Done exp: $d on $HOSTNAME `date`"
done
