#!/usr/bin/env bash


#set -x
echo "Uniq mission ended messages"
sudo find . -iname 'state*.log' -exec grep -H "Mission ended" {} \; | grep -v Training | uniq | sort
echo
#set +x

#set -x
echo "Uniq mission ended messages"
sudo find . -iname 'state*.log' -exec grep -H "Mission ended" {} \; | grep -v Training | uniq | wc -l
echo
#set +x

#set -x
echo "All mission ended messages"
sudo find . -iname 'state*.log' -exec grep -H "Mission ended" {} \; | sort
echo
#set +x

#set -x
echo "Count of mission ended messages"
sudo find . -iname 'state*.log' -exec grep -H "Mission ended" {} \; | wc -l
echo
#set +x
