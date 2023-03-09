#!/usr/bin/env bash
set -e

cd `dirname $0`
echo $PWD

git pull
./update_git_info.py
# This script can take an optional argument that is the name of a tagged
# release. If provided, it will checkout that tag and then update the
# submodules to that point in history. Otherwise, it will just update all
# submodules to the latest versions.
if [ "$1" == "" ]; then
    git submodule update --recursive # --remote --merge
else
    git checkout "$1"
    git submodule update
fi
