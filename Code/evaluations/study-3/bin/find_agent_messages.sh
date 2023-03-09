#!/usr/bin/env bash

echo "ASI_DOLL_TA1_RITA messages:"
sudo find . -iname 'rmq.log' | grep -v HSR | xargs grep -c ASI_DOLL_TA1_RITA
echo

#echo "heartbeat"
#sudo find . -iname 'rmq.log' | grep -v HSR | xargs grep -c heartbeat
#echo

echo "Intervention:Chat"
sudo find . -iname 'rmq.log' | grep -v HSR | xargs grep -c Intervention:Chat 
echo

echo "Prediction:Action"
sudo find . -iname 'rmq.log' | grep -v HSR | xargs grep -c Prediction:Action 
echo

echo "anomaly"
sudo find . -iname 'rmq.log' | grep -v HSR | xargs grep -c anomaly
echo
