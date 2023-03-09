#!/usr/bin/env python3

### Given a combined metadata file containing all the prediction metrics and for all the trials,
# create a HSR*** file for each trial

# Study 2 evaluation document
# https://docs.google.com/document/d/1tKOmGW6Xf42VmKi2WUYHFm8DQKz4W_3OSARDsVactIQ/edit#
# TA1 should name prediction filenames using this format:
# HSRData_Predictions_performerID_agent ID_Trial-T######_Vers-#.metadata
# PerformerID and AgentID are user defined, but keep them concise if possible and delimited by “_”.
# Please use camelcase for each filename component.
# For example: HSRData_Predictions_CRA_Agent1_Trial-T000101_Vers-1.metadata

import argparse
import os
import sys
from pathlib import Path
from pprint import pprint

def setup_py_path():
    ## Setup PYTHONPATH to import from Code/bin
    pth = os.path.dirname(os.path.realpath(__file__))
    # print(pth, type(pth))
    proj_home = Path(pth).parents[3]
    # print(proj_home)
    code_bin = proj_home.joinpath('Code/bin')
    sys.path.append(str(code_bin))
setup_py_path()

import utils
import metadata_utils

### Globals
file_prefix = 'HSRData_Predictions_DollMit_RitaAgent_'


# {:header {:timestamp 2021-06-19T00:23:19.072Z, :message_type agent, :version 0.1}, :msg {:timestamp 2021-06-19T00:23:19.072Z, :source Rita_Agent, :sub_type prediction:state, :version 0.2_study-2-july-2021, :trial_id 2faec8cd-4a2d-439d-80e2-7d52cfc2281c, :experiment_id 534d2acd-35ea-484c-a5e8-9143f91db009}, :data {:created 2021-06-19T00:23:19.072Z, :group {:start 2021-06-19T00:23:19.072Z, :duration 0, :explanation nil}, :predictions [{:predicted-property team_performance, :explanation nil, :confidence_type nil, :unique_id se16974, :prediction 380, :start 2021-06-19T00:23:19.072Z, :duration 0, :probability 0.5, :confidence nil, :probability_type probability, :subject TM000113}]}}
def collect_messages_by_trial_id(msgs):
    by_trials = {}
    for msg in msgs:
        tid = msg['msg']['trial_id']
        if tid not in by_trials:
            by_trials[tid] = []
        by_trials[tid].append(msg)
    return by_trials


def get_meta_files_version(meta_files):
    if len(meta_files) > 0:
        fname = meta_files[-1]
        tokens = fname.split('_')
        vers_idx = 7
        if len(tokens) < vers_idx + 1:
            return None
        else:
            return tokens[vers_idx]
    else:
        return None


def db_by_trial_id(db):
    by_trials = {}
    for t_name, dat in db.items():
        tid = dat['trial_id']
        if tid not in by_trials:
            by_trials[tid] = {}
        tid = dat['trial_id']
        meta_files = dat['metadata']
        by_trials[tid]['trial_name'] = t_name
        by_trials[tid]['metadata'] = meta_files
        by_trials[tid]['vers_with_ext'] = get_meta_files_version(meta_files)
    return by_trials


def generate_study_2_files(msgs_by_trials, by_trial_id):
    for tid, msgs in msgs_by_trials.items():
        trial_name = by_trial_id[tid]['trial_name']
        vers_with_ext = by_trial_id[tid]['vers_with_ext']
        fname = file_prefix + trial_name + '_' + vers_with_ext
        print('Writing:', fname, ' ', len(msgs))
        utils.write_lines(utils.list_to_json(msgs), fname)


def main(db_f, meta_f):
    db = utils.read_json(db_f)
    msgs = metadata_utils.read_file(meta_f)
    msgs_by_trials = collect_messages_by_trial_id(msgs)
    by_trial_id = db_by_trial_id(db)
    generate_study_2_files(msgs_by_trials, by_trial_id)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Create HSR study-2 eval metrics for each trial')
    parser.add_argument('db_file', help='DB.json file')
    parser.add_argument('meta_file', help='metadata file containing eval metrics')
    args = parser.parse_args()
    main(args.db_file, args.meta_file)
