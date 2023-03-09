#!/usr/bin/env bash

echo "Grabbing data from NAS for study-3 regression"
echo "Grabbing Spiral-3 Trial files"
tdir="spiral-3-and-4"
mkdir -p ${tdir}
#set -x
rsync --progress -v -a  /nfs/projects/RITA/HSR-data-mirror/study-3_spiral-3_pilot-rmq/*Trial-T0*.log ${tdir}
#set +x
echo
echo "Grabbing Spiral-4 Trial files"
#set -x
rsync --progress -v -a  /nfs/projects/RITA/HSR-data-mirror/study-3_spiral-4_pilot-rmq/*Trial-T0*.log ${tdir}
#set +x
echo

echo "Grabbing HSR Training data"
mkdir -p hsr-training
#set -x
rsync --progress -v -a /nfs/projects/RITA/HSR-data-mirror/study-3_2022-rmq/*Training*.log hsr-training
#set +x
echo

echo "Grabbing HSR data teams 201 - 210"
tdir="team-201-210"
mkdir -p ${tdir}
for x in 201 202 203 204 205 206 207 210
do
    echo "Grab Team ${x}"
    src="/nfs/projects/RITA/HSR-data-mirror/study-3_2022-rmq/HSRData_TrialMessages_Trial-T0*_Team-TM000${x}*_Member-na_CondBtwn-none_CondWin-na_Vers-3.log"
    ls -l ${src} | wc -l
    rsync --progress -v -a ${src} ${tdir}
done

#rsync --progress -v -a /nfs/projects/RITA/HSR-data-mirror/study-3_2022-rmq/HSRData_TrialMessages_Trial-T0*_Team-TM000201*_Member-na_CondBtwn-none_CondWin-na_Vers-1.log ${tdir}

echo "Grabbing HSR data teams 211 - 219"
tdir="team-211-219"
mkdir -p ${tdir}
for x in 211 212 213 214 216 217 218 219
do
    echo "Grab Team ${x}"
    src="/nfs/projects/RITA/HSR-data-mirror/study-3_2022-rmq/HSRData_TrialMessages_Trial-T0*_Team-TM000${x}*_Member-na_CondBtwn-none_CondWin-na_Vers-3.log"
    ls -l ${src} | wc -l
    rsync --progress -v -a ${src} ${tdir}
done
