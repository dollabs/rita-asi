#!/usr/bin/env bash

#set -e
set -u


if [[ -n ${asist_testbed+x} ]]; then
  echo "Found asist_testbed dir:  $asist_testbed"
  if [[ ! -d ${asist_testbed} ]]; then
    echo "Given asist_testbed dir does not exists: $asist_testbed"
    exit 1
  fi
fi

echo "Latest mod in $asist_testbed"
  ls -l $asist_testbed/Local/data/mods/asistmod*.jar
echo

for r in rita1 rita2 rita3
do
  echo "mods on $r"
  ssh $r ls -l "Applications/Malmo-0.37.0-Linux-Ubuntu-18.04-64bit_withBoost_Python3.6/Minecraft/run/mods/asistmod*.jar"
  echo

  echo "Removing all mods on $r. There should be only 1 asistmod"
  ssh $r rm "Applications/Malmo-0.37.0-Linux-Ubuntu-18.04-64bit_withBoost_Python3.6/Minecraft/run/mods/asistmod*.jar"
  echo

  echo "Copying latest mod from $asist_testbed"
  scp $asist_testbed/Local/data/mods/asistmod*.jar $r:~/Applications/Malmo-0.37.0-Linux-Ubuntu-18.04-64bit_withBoost_Python3.6/Minecraft/run/mods/
  echo

  echo "mods on $r"
  ssh $r ls -l "Applications/Malmo-0.37.0-Linux-Ubuntu-18.04-64bit_withBoost_Python3.6/Minecraft/run/mods/asistmod*.jar"
  echo
done
