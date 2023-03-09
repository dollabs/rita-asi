#!/usr/bin/env python3

import argparse
from pathlib import Path
import json
from pprint import pprint
import eyetracking
import metadata_utils
import utils


# Make a db of all files in the given directory
#   - For each file, find trial id
#   - for each trial id, collect metadata and eye tracking file
#   - from metadata, figure out trial time
#   - Associate start time with data in eye tracking data

def get_trial_id(f):
    name = f.name.split('_')
    for x in name:
        if x.startswith('Trial-'):
            return x
    return None


def get_experiment_info(metadata_f):
    with open(metadata_f) as mf:
        line = mf.readline()
        jsn = json.loads(line)
        # pprint(jsn)
        return {'experiment_id': jsn['msg']['experiment_id'],
                'trial_id': jsn['msg']['trial_id']}


def get_eye_tracking_times(fname):
    dat = eyetracking.parse_csv(fname)
    dat = eyetracking.filter_pre_post(dat)
    timing = eyetracking.get_start_stop_times(dat)
    return timing


def get_metadata_times(fname):
    # print(fname)
    messages = metadata_utils.read_file(fname)
    messages = metadata_utils.get_mission_timer_msgs(messages)
    return metadata_utils.get_mission_timer_start_stop_times(messages)


def update_db_mission_start_stop_time(db):
    for tid, dat in db.items():
        print('Trial:', tid)
        # pprint(dat)
        eye = None
        meta = None

        if 'eyetracking' in dat:
            eye = get_eye_tracking_times(dat['eyetracking'])
        else:
            print('No Eye Tracking file')

        if 'metadata' in dat and len(dat['metadata']) > 0:
            meta = get_metadata_times(dat['metadata'][-1])
        else:
            print('No metadata file')

        if eye is not None:
            dat['eyetracking-starttime'] = eye['start-time']
            dat['eyetracking-stoptime'] = eye['stop-time']

        if meta is not None:
            dat['metadata-starttime'] = meta['start-time']
            dat['metadata-stoptime'] = meta['stop-time']

        pprint(dat)
    return db


def make_db_from_dir_(dir_name):
    dirp = Path(dir_name)
    bytrial_id = {}
    for f in dirp.iterdir():
        if f.is_file():
            # print(f.suffix)
            tid = get_trial_id(f)
            if tid is not None:
                if tid not in bytrial_id:
                    bytrial_id[tid] = {}

                if f.suffix == '.metadata':
                    if 'metadata' not in bytrial_id[tid]:
                        bytrial_id[tid]['metadata'] = []
                    bytrial_id[tid]['metadata'].append(f.name)
                    bytrial_id[tid]['metadata'] = sorted(bytrial_id[tid]['metadata'])
                    if 'experiment_id' not in bytrial_id[tid]:
                        edata = get_experiment_info(f)
                        bytrial_id[tid]['trial_id'] = edata['trial_id']
                        bytrial_id[tid]['experiment_id'] = edata['experiment_id']
                elif f.suffix == '.csv':
                    if 'eyetracking-files' not in bytrial_id:
                        bytrial_id[tid]['eyetracking-files'] = []
                    bytrial_id[tid]['eyetracking-files'].append(f.name)
                    bytrial_id[tid]['eyetracking-files'] = sorted(bytrial_id[tid]['eyetracking-files'])
                    bytrial_id[tid]['eyetracking'] = bytrial_id[tid]['eyetracking-files'][-1]
                else:
                    print('Unknown file', f)
    return bytrial_id


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Create db.json for a given directory')
    parser.add_argument('directory')
    args = parser.parse_args()
    db = make_db_from_dir_(args.directory)
    # db = update_db_mission_start_stop_time(db)
    utils.write_json(db, 'db.json')
