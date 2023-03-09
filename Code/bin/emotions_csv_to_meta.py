#!/usr/bin/env python3

import argparse
import json
from pprint import pprint
import eyetracking
import metadata_utils
import os
import datetime
import utils


# Make a db of all files in the given directory (covered by make_db.py)
#   - For each file find trial id
#   - for each trial id, collect metadata and eye tracking file
#   - from metadata, figure out trial time
#   - Associate start time with data in eye tracking data

#   - Create testbed-messsage for eye tracking
#   - Combine eye tracking metadata with existing metadata, sort and write to a file


def make_metadata_timestamp(millis):
    # print('converting millis to metdata timestamp', millis, datetime.datetime.fromtimestamp(millis/1000).isoformat())
    return datetime.datetime.fromtimestamp(millis / 1000).isoformat() + 'Z'  # FIXME


def to_millis(x):
    return int(x * 1000)


def make_metadata_msg(dat, timestamp, exp_id, trial_id):
    msg = {
        'experiment_id': exp_id,
        'source': 'faceAnalyzer',
        'sub_type': 'state',
        'timestamp': timestamp,
        'trial_id': trial_id,
        'version': 0.1
    }

    hdr = {'message_type': 'observation',
           'timestamp': timestamp,
           'version': 0.1}
    return {'data': dat,
            'msg': msg,
            'header': hdr}


def read_data(tid, dat, read_meta=False):
    eyef = None
    if 'eyetracking' in dat:
        eyef = dat['eyetracking']
        if not os.path.exists(eyef):
            print(tid, 'Eye tracking file does not exists: ', eyef)
    else:
        print(tid, ':No eye tracking file')

    metaf = None
    if read_meta:
        if 'metadata' in dat and len(dat['metadata']) > 0:
            metaf = dat['metadata'][-1]
            if not os.path.exists(metaf):
                print(tid, 'metadata file does not exists: ', metaf)
        else:
            print(tid, ':No metadata file')

    if eyef:
        print('Reading', eyef)
        eyef = eyetracking.parse_csv(eyef)
    if metaf:
        print('Reading', metaf)
        metaf = metadata_utils.read_file(metaf)

    x = {'meta': metaf,
         'eye': eyef}
    return x


def make_eyetrackign_metadata_file_name(out_dir, fname):
    return out_dir + os.path.splitext(fname)[0] + '.metadata'


def write_affect_metadata(db_data, eye, fname):
    if 'metadata-starttime' not in db_data:
        print('No metadata-starttime')
        return

    metastart = db_data['metadata-starttime']
    eyestart = db_data['eyetracking-starttime']
    # print('metadata-starttime', metastart)
    # print('eyetracking-starttime', eyestart)
    # print('Eye Tracking file', db_data['eyetracking'])
    print('Writing:', fname)
    collected = []
    # for i in [0, 1]:
    #     x = eye[i]
    for x in eye:
        csv_time = float(x['timestamp'])
        delta = to_millis(csv_time) - eyestart
        ts = make_metadata_timestamp(metastart + delta)
        # print('meta timestamp', ts)
        x_msg = {'emotions': eyetracking.get_emotions(x),
                 'original-data': x}
        msg = make_metadata_msg(x_msg, ts, db_data['experiment_id'], db_data['trial_id'])
        collected.append(msg)
    utils.write_lines(collected, fname)


def combine_eye_tracking_with_metadata(dbf='db.json'):
    with open(dbf) as fd:
        db = json.load(fd)
        # pprint(db)
        combine_with_meta = False
        i = 0
        for tid, dat in db.items():
            if 'eyetracking' in dat:
                eye_out_file = make_eyetrackign_metadata_file_name('eyetracking-metadata/', dat['eyetracking'])
                if not os.path.exists(eye_out_file):
                    both = read_data(tid, dat, combine_with_meta)
                    # meta = both['meta']
                    eye = both['eye']
                    if dat is not None and eye is not None:
                        write_affect_metadata(dat, eye, eye_out_file)
                        print()
                    # i = i + 1
                    # if i == 2:
                    #     break
                else:
                    print('Exists: Skipping', eye_out_file)
            else:
                print(tid, 'No eye tracking file')


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Test reading of RMQ Log files')
    parser.add_argument('directory')
    args = parser.parse_args()
    combine_eye_tracking_with_metadata()
