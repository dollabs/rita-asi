#!/usr/bin/env python3

import sys
import os
from pathlib import Path
from pprint import pprint
import json


def setup_py_path():
    ## Setup PYTHONPATH to import from Code/bin
    pth = os.path.dirname(os.path.realpath(__file__))
    print(pth, type(pth))
    proj_home = Path(pth).parents[3]
    print(proj_home)
    code_bin = proj_home.joinpath('Code/bin')
    sys.path.append(str(code_bin))


setup_py_path()
import rmq_log_utils

## Assume CWD has all exp-00xx dirs only.
## find all rmq.log files
## from RMQ.log file, extract prediction-stats
## Add the stats to structure as follows
## write to prediction-stats.json

stats_ex = {'exp-0001': {'Trial-100': {'rmq_log_file': 'path to file',
                                       'prediction-stats': "data from rmq log file"}}}


def get_rmq_log_files(dir, files):
    # print('Working on: ', os.path.abspath(dir))
    dirs = [x for x in Path(dir).iterdir() if x.is_dir()]
    dirs.sort()
    for d in dirs:
        get_rmq_log_files(d, files)

    for f in Path(dir).iterdir():
        if f.is_file():
            # print(type(f), f)
            if f.name == 'rmq.log':
                files.append(os.path.abspath(f))


# Given '/Users/prakash/mount-points/deepblue/projects/asist-rita/workspace/study-1_2020.08-rmq-experiments/exp-0001/December-2020-05--15/HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-188_Team-na_Member-74_Vers-3/rmq.log'
# Create above structure for each file
def make_structure(rmq_log_files):
    dat = {}
    if os.path.exists('prediction-stats-exp-0001-exp-0020.json'):
        dat = read_prediction_stats()

    for f in rmq_log_files:
        ff = Path(f)
        parts = ff.parts
        #print(f'{parts}')
        exp_id = parts[-3]
        #print(f'{exp_id}')
        #print("", parts[-2].split('_'))
        trial_id = parts[-2].split('-')[7]

        
        #print('parts', parts[-2])        
        print(exp_id, ':', trial_id )

        if exp_id not in dat:
            dat[exp_id] = {}
        if trial_id not in dat[exp_id]:
            dat[exp_id][trial_id] = {}
        else:
            print('Exists:', exp_id, ":", trial_id)
            if f != dat[exp_id][trial_id]['rmq_log_file']:
                print('rmq-log-file', dat[exp_id][trial_id]['rmq_log_file'])
                print('Will overwrite with: ', f)
            else:
                print('rmq-log-file', f, '\n')
            # pprint(dat[exp_id][trial_id])

        dat[exp_id][trial_id]['rmq_log_file'] = f
    return dat


def get_prediction_stats(fil):
    dat = rmq_log_utils.read_log_file(fil)
    for d in dat:
        ts, msg = d
        # pprint(msg)
        if 'prediction-stats' in msg:
            # pprint(msg)
            return msg['prediction-stats']


def collect_prediction_stats(dat):
    for exp, v in dat.items():
        for tid, vv in v.items():
            # print(exp, ':', vv)
            rmq_file = vv['rmq_log_file']
            if 'prediction-stats' not in vv:
                print('Retrieving stats for:', rmq_file)
                stats = get_prediction_stats(rmq_file)
                if stats is not None:
                    dat[exp][tid]['prediction-stats'] = stats
            else:
                print('Have prediction-stats... Not Retrieving stats for:', rmq_file, '\n')


def write_data(dat):
    fname = 'prediction-stats-exp-0001-exp-0020.json'
    print('Reading initial stats from:', os.path.abspath(fname))
    with open(fname, 'w') as outfile:
        json.dump(dat, outfile, sort_keys=True, indent=2)


def read_prediction_stats():
    with open('prediction-stats-exp-0001-exp-0020.json') as jsn_file:
        dat = json.load(jsn_file)
        # print(f'Got Rita RMQ messages {len(msgs)}')
        return dat


def main():
    print('Current Dir', Path('.'), '\n')
    here = Path('.')
    files = []
    get_rmq_log_files('.', files)
    filtered = []
    for f in files:
        if 'tailer_service_logs' in f:
            print(f)
            filtered.append(f)
            
    print('RMQ log files')
    #pprint(files)
    dat = make_structure(filtered)
    collect_prediction_stats(dat)
    write_data(dat)
    #pprint(dat)


if __name__ == "__main__":
    # parser = argparse.ArgumentParser(description='Setup experiments')
    # parser.add_argument('log_dir')
    # args = parser.parse_args()
    main()
