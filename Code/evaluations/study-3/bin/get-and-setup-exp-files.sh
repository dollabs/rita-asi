#!/usr/bin/env bash

from="/nfs/projects/RITA/HSR-data-mirror/study-3_2022-rmq-experiments/exp-0002"
ed_f="${from}/experiment-definition.edn"
lpm_f="${from}/learned-participant-model.edn"
rmq_log_dir="/nfs/projects/RITA/HSR-data-mirror/study-3_2022-rmq"

function copy_files() {
    trial1=${1}
    trial2=${2}
    target_dir=${3}
    #rsync --progress -v -v -a ${from_dir}/*.metadata .
    echo "copy ${trial1} -> ${target_dir}"
    echo "copy ${trial2} -> ${target_dir}"
    echo "copy ${lpm_f} -> ${target_dir}"
    echo "copy ${rmq_log_dir} -> ${target_dir}"
    mkdir -p ${target_dir}
    rsync --progress  -a ${trial1} ${target_dir}
    rsync --progress  -a ${trial2} ${target_dir}
    rsync --progress  -a ${ed_f} ${target_dir}
    rsync --progress  -a ${lpm_f} ${target_dir}
    echo
    ls -l ${target_dir}
}

TEAM="TM000201"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0001"
copy_files ${first} ${exp_dir}

TEAM="TM000202"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0002"
copy_files ${first} ${exp_dir}

TEAM="TM000203"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0003"
copy_files ${first} ${exp_dir}

TEAM="TM000204"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0004"
copy_files ${first} ${exp_dir}

TEAM="TM000205"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0005"
copy_files ${first} ${exp_dir}

TEAM="TM000206"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0006"
copy_files ${first} ${exp_dir}

TEAM="TM000207"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0007"
copy_files ${first} ${exp_dir}

TEAM="TM000210"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0010"
copy_files ${first} ${exp_dir}

TEAM="TM000211"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0011"
copy_files ${first} ${exp_dir}

TEAM="TM000212"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0012"
copy_files ${first} ${exp_dir}

TEAM="TM000213"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0013"
copy_files ${first} ${exp_dir}

TEAM="TM000214"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0014"
copy_files ${first} ${exp_dir}

TEAM="TM000216"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0016"
copy_files ${first} ${exp_dir}

TEAM="TM000217"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0017"
copy_files ${first} ${exp_dir}

TEAM="TM000218"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0018"
copy_files ${first} ${exp_dir}

TEAM="TM000219"
first=${rmq_log_dir}/*Trial-T0*-${TEAM}*.log
exp_dir="exp-0019"
copy_files ${first} ${exp_dir}

