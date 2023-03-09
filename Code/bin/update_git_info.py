#!/usr/bin/env python3

from git import Repo
from pathlib import Path
import utils
from pprint import pprint
from datetime import datetime

# pip3 install --user gitpython

# rorepo is a Repo instance pointing to the git-python repository.
# For all you know, the first argument to Repo is a path to the repository
# you want to work with
repo = Repo(search_parent_directories=True)
sha = repo.head.object.hexsha
branch = '{}'.format(repo.head.reference)
config_f = Path(repo.working_tree_dir).joinpath('./Code/resources/public/config.json')
env_f = Path(repo.working_tree_dir).joinpath('./Code/.env')
rita_agent_env_f = Path(repo.working_tree_dir).joinpath('./Code/Rita_Agent/.env')
print(branch, sha)
# print(repo.working_tree_dir)
print(config_f)
config = utils.read_json(config_f)
config['branch'] = branch
config['git-commit'] = sha

TB_VERSION = 3
minor_version = 0
if 'rita-minor-version' in config:
    minor_version = config['rita-minor-version']

rita_internal_tag = ''
if 'internal-tag' in config:
    rita_internal_tag = config['internal-tag']

d = datetime.now()
rita_ver = '{}.{}.{}-{}-{}-{}'.format(TB_VERSION, minor_version, d.year, d.month, d.day, rita_internal_tag)
config['rita-version'] = rita_ver
pprint(config)
utils.write_json(config, config_f)

print(f'RITA_VER={rita_ver}')


def update_env(f):
    # print('Updating .env file with rita-version')
    print(f'.env={f}')
    env_content = utils.read_lines(f)
    updated = []
    for l in env_content:
        # idx = 0
        x = l.strip()
        if l.startswith('RITA_VER'):
            print(f'Updating {x} ->\nRITA_VER={rita_ver}')
            x = f'RITA_VER={rita_ver}'
        updated.append(x)
        # print(f'x; {x}')
        # idx = idx + 1
    # pprint(updated)
    utils.write_lines(updated, f)


update_env(env_f)
update_env(rita_agent_env_f)

## Agent version notes and guidance from testbed/agent_versioninfo.md
# Version Numbering Guidence:
# Agents should use the format of the version as shown above (<Major>,<Minor>.<Patch>).  General guidence for when to increment a part of the version should follow the Semantic Versioning Guidence.
# - The agent should publish a different version identification whenever any change is made to the agent.
# - The major number of the agent should track the major version of the testbed that it is designed to work with
#     - The minor number should be incremented when significant functionality is added to the agent.   This should also track incompatability between minor versions of the the agent
# - The patch number should be incremented for each change to the agent
