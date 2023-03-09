#!/usr/bin/env bash

set -x
find . -name prediction-generator.log | grep clojure | xargs grep asist-prediction- > study-2-predictions.txt

cut -d',' -f1 --complement study-2-predictions.txt > HSRData_Predictions_DollMit_RitaAgent_Trial.metadata
set +x
