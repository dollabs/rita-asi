#!/usr/bin/env bash

echo "Interventions count in ${PWD}"
sudo find . -iname 'state*.log' -exec grep -H -c "Publishing intervention" {} \;
echo
