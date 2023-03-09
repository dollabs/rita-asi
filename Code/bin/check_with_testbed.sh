#!/usr/bin/env bash

set -u

function set_git_home() {
  pushd . >>/dev/null
  my_dir=$(dirname $0)
  cd $my_dir
  cd ../..
  git_home=$PWD
  popd >/dev/null
}
set_git_home

tb_agent_dir=${asist_testbed}/Agents/Rita_Agent
git_agent_dir=${git_home}/Code/Rita_Agent
echo "Testbed Agent is checked out at: $tb_agent_dir"
echo "DOLL Agent is checked out at:    $git_agent_dir"

echo "Diff Testbed vs Doll Git"
echo
echo "Diff .env"
diff ${tb_agent_dir}/settings.env ${git_agent_dir}/.env
echo

echo "Diff compose file"
diff ${tb_agent_dir}/docker-compose.yml ${git_agent_dir}/docker-compose.yml
echo
