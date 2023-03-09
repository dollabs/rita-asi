#! /usr/bin/env python3

import argparse

import os

import metadata_utils
import print_rmq_log_stats
from pprint import pprint
import dateutil


def make_key(msg):
    if 'source' not in msg['msg']:
        # pprint(msg)
        src = 'bad_msg_source'
    else:
        src = msg['msg']['source']

    if 'message_type' not in msg['header']:
        mtype = 'bad_message_type'
    else:
        mtype = msg['header']['message_type']

    if 'sub_type' not in msg['msg']:
        # pprint(msg)
        styp = 'bad_msg_sub_type'
    else:
        styp = msg['msg']['sub_type']

    if 'topic' not in msg:
        topic = 'no_topic'
    else:
        topic = msg['topic']

    return src + '__' + mtype + '__' + styp + '__' + topic


def get_file_duration(first, last):
    start_time = first['header']['timestamp']
    end_time = last['header']['timestamp']
    start_time_x = dateutil.parser.parse(start_time)
    end_time_x = dateutil.parser.parse(end_time)
    return (end_time_x - start_time_x), start_time, end_time


def get_mission_duration(msg_types):
    kee = 'simulator__event__Event:MissionState__observations/events/mission'
    msgs = msg_types[kee]
    start = end = None
    if len(msgs) > 0:
        start = msgs[0]
    if len(msgs) > 1:
        end = msgs[-1]

    print(f'Mission messages {len(msgs)}')
    start_time = end_time = 0
    if not start:
        print('Warn: No mission start message')
    else:
        start_time = start['header']['timestamp']
    if not end:
        print('Warn: No mission end message')
    else:
        end_time = end['header']['timestamp']
    if start and end:
        return dateutil.parser.parse(end_time) - dateutil.parser.parse(start_time), start_time, end_time
    return 0, start_time, end_time


def print_metadata_stats(meta_f):
    print(f'Reading file: {meta_f}')
    msgs = metadata_utils.read_file(meta_f)
    msg_types = {}

    for m in msgs:
        k = make_key(m)
        if k not in msg_types:
            msg_types[k] = []
        msg_types[k].append(m)
    print(meta_f)
    print_rmq_log_stats.print_tb_subtypes(msg_types)
    mdura, mstart, mend = get_mission_duration(msg_types)
    print(f'Mission start time {mstart}')
    print(f'Mission end time {mend}')
    print(f'Mission Duration {mdura}\t{meta_f}')

    print(f'Trial Duration {print_rmq_log_stats.get_trial_duration(msg_types)}\t{meta_f}')
    first = msgs[0]
    last = msgs[-1]
    duration, start, end = get_file_duration(first, last)
    print(f'Trial First msg {make_key(first)}: {start}')
    print(f'Trial Last msg {make_key(last)}: {end}')
    print(f'File Duration {duration}\t{meta_f}\n')


def main(file_or_dir):
    if os.path.isfile(file_or_dir):
        print_metadata_stats(file_or_dir)
    else:
        for filename in os.listdir(file_or_dir):
            if filename.endswith('.metadata'):
                # print('Working on', filename)
                print_metadata_stats(filename)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Print\'s info from metadata file(s)')
    parser.add_argument('metadata_file', help='Filename or Dir')
    args = parser.parse_args()
    # try:
    main(args.metadata_file)
    # except Exception as e:
    #     print(e)
    #     pprint(e)
    # print('Error: Some error in file', args.json_file)
