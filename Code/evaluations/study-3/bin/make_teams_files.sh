#!/usr/bin/env bash
from="/nfs/projects/RITA/HSR-data-mirror/study-3_2022-rmq"

files=$(cd ${from} && ls *Trial-T0*.log)

for f in ${files}; do
    echo "$f"
    TEAM=$(echo $f | cut -d'_' -f4)
    echo ${TEAM}
    out_f=${TEAM}.log
    cat ${from}/${f} >> ${out_f}
    #echo >> ${out_f}
    wc -l ${from}/*T0*${TEAM}*
    wc -l ${out_f}
done
