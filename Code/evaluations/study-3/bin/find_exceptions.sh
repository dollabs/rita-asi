#!/usr/bin/env bash

echo "Looking for exception in ${PWD}"
echo "Uniq exceptions: $(sudo find . -iname 'state*.log' -exec grep -i -H -e exception {} \; | uniq | wc -l)"
echo
sudo find . -iname 'state*.log' -exec grep -n -i -H -e exception {} \; | uniq | sort
echo


echo "All exception count"
#sudo find . -iname 'state*.log' -exec grep -i -H -e exception {} \; | wc -l
echo
