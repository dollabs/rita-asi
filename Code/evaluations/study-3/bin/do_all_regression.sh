#!/usr/bin/env bash

dte_FMT="+%b-%d-%I%M-%p-%Y-%Z"
dte=$(date ${dte_FMT})
echo ${dte}
pushd .
cd $ritahome/workspace/regression-tests-all

base_d="data-$dte"

mkdir -p ${base_d}
cd ${base_d}

echo "Starting all regression at $(date ${dte_FMT})"

echo "Regression Test for Team 201 210 individual metadata at $(date ${dte_FMT})"
echo 
$ritagit/Code/evaluations/study-3/bin/par_check_rita.sh $ritahome/workspace/regression-data/team-201-210 | tee -a par_team-201-210.txt

echo "Regression Test for Team 211 219 individual metadata at $(date ${dte_FMT})"
$ritagit/Code/evaluations/study-3/bin/par_check_rita.sh $ritahome/workspace/regression-data/team-211-219 | tee -a par_team-211-219.txt

echo "Regression Test for combined teams one metadata per team at $(date ${dte_FMT})"
$ritagit/Code/evaluations/study-3/bin/par_check_rita.sh $ritahome/workspace/regression-data/rmq_log_for_each_team | tee -a par_combined_teams.txt

echo "Regression Test for spiral 3 and spiral 4 at $(date ${dte_FMT})"
$ritagit/Code/evaluations/study-3/bin/par_check_rita.sh $ritahome/workspace/regression-data/spiral-3-and-4 | tee -a par_spiral-3-and-4.txt

echo
echo "Stopping all regression at $(date ${dte_FMT})"

popd 
