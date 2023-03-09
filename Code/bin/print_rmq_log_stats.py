#! /usr/bin/env python3

import argparse

import dateutil.parser
import os
import datetime
#import basemap
import fix_timestamps_in_log
import utils
import rmq_log_utils
from pprint import pprint

underLine = ''.join([60 * n for n in '_'])
underLine_small = ''.join([35 * n for n in '_'])


def update_appid_rkey(col, msg):
    # print(col)
    # pprint(msg)
    appid = msg['app-id']
    rkey = msg['routing-key']
    key = f'{appid}__{rkey}'
    # print(key)
    if not key in col:
        col[key] = {}
        col[key]['values'] = []
        col[key]['app-id'] = appid
        col[key]['routing-key'] = rkey
    col[key]['values'].append(msg)


def formatted_print_appid_rkey(appid, rkey, count):
    print('{:25}{:25}:  {:<10}'.format(appid, rkey, count))


def print_appid_rkey(col):
    # print('Uniq App and Rkeys:')
    # pprint(col.keys())
    print('{:25}{:25}:  {:10}'.format('app-id', 'routing-key', 'Count'))
    print(underLine)
    for _, val in col.items():
        formatted_print_appid_rkey(val['app-id'], val['routing-key'], len(val['values']))


def print_tb_subtypes(msgs):
    print('Testbed Sub_Type messages')
    print('{:50}:  {:10}:  {:25}'.format('sub_type-id', 'Count', 'First msg Time'))
    print(underLine_small)
    for k in sorted(msgs.keys()):
        print('{:50}:  {:<10}  {:<25}'.format(k, len(msgs[k]), msgs[k][0]['header']['timestamp']))


def get_trial_duration(msgs):
    start_key = 'gui__trial__start__trial'
    stop_key = 'gui__trial__stop__trial'
    if start_key in msgs:
        start_time = msgs[start_key][0]['header']['timestamp']
    else:
        print('Trial Start message not found')
        return 0

    if stop_key in msgs:
        end_time = msgs[stop_key][0]['header']['timestamp']
    else:
        return 0

    start_time = dateutil.parser.parse(start_time)
    end_time = dateutil.parser.parse(end_time)
    return (end_time - start_time)


def get_testbed_msgs(msg_list):
    for x in msg_list:
        if x['routing-key'] == 'testbed-message':
            return x['values']
    return None


def print_triage_stats(msgs):
    print('Triage Stats')
    print(underLine_small)
    triage_state = {}
    for x in msgs:
        # pprint(x)
        t_state = None

        if 'data' in x['testbed-message'] and 'triage_state' in x['testbed-message']['data']:
            t_state = x['testbed-message']['data']['triage_state']

        if 'msg' in x['testbed-message'] and 'data' in x['testbed-message']['msg']:
            t_state = x['testbed-message']['msg']['data']['action']

        # print(t_state)
        if t_state is not None and t_state not in triage_state:
            triage_state[t_state] = []
        if t_state is not None:
            triage_state[t_state].append(x)

    for k in sorted(triage_state.keys()):
        print('{:25}:  {:<10}'.format(k, len(triage_state[k])))


def print_location_min_max(minx, miny, minz, maxx, maxy, maxz):
    print('X: {:.2f}     {:.2f}'.format(minx, maxx))
    print('Y: {:.2f}     {:.2f}'.format(miny, maxy))
    print('Z: {:.2f}     {:.2f}'.format(minz, maxz))


def print_location_box(msgs, map_file, mbounds):
    xv = []
    yv = []
    zv = []

    for x in msgs:
        # pprint(x)
        d = x['testbed-message']['data']
        xv.append(d['x'])
        yv.append(d['y'])
        zv.append(d['z'])

    print('Trial Min and Max co-ordinates')
    print(underLine_small)
    minx, miny, minz, maxx, maxy, maxz = min(xv), min(yv), min(zv), max(xv), max(yv), max(zv)
    print_location_min_max(minx, miny, minz, maxx, maxy, maxz)

    print()
    if map_file:
        print(map_file, ' Min and Max co-ordinates ')
        print(underLine_small)
        minbx, minby, minbz, maxbx, maxby, maxbz = mbounds['min']['x'], mbounds['min']['y'], mbounds['min']['z'], \
                                                   mbounds['max']['x'], mbounds['max']['y'], mbounds['max']['z']
        print_location_min_max(minbx, minby, minbz, maxbx, maxby, maxbz)

        print()
        print('Trial within map bounds')
        print(underLine_small)
        print('X: ', minx >= minbx, maxx <= maxbx)
        print('Y: ', miny >= minby, maxy <= maxby)
        print('Z: ', minz >= minbz, maxz <= maxbz)

        print()
        print('Trial Map bounds delta')
        print(underLine_small)
        print('X: {:.2f}     {:.2f}'.format(minx - minbx, maxbx - maxx))
        print('Y: {:.2f}     {:.2f}'.format(miny - minby, maxby - maxy))
        print('Z: {:.2f}     {:.2f}'.format(minz - minbz, maxbz - maxz))


def group_messages(msgs, map_file, mbounds):
    tbm = get_testbed_msgs(msgs.values())

    # Group messages by subtype
    grouped = {}
    state_start_stop = []  # Only contains state messages between start and stop
    for m in tbm:  # [:5]:
        # pprint(m)
        add_start_stop = False
        if 'msg' in m['testbed-message'] and 'sub_type' in m['testbed-message']['msg']:
            st = m['testbed-message']['msg']['sub_type']
            source = m['testbed-message']['msg']['source']
            if st not in grouped:
                grouped[st] = []
            grouped[st].append(m)
            #
            if 'start' in grouped:
                add_start_stop = True
            if 'stop' in grouped:
                add_start_stop = False

            if add_start_stop and st == 'state' and source == 'simulator':
                state_start_stop.append(m)

    grouped['state-start-stop'] = state_start_stop
    print_tb_subtypes(grouped)
    print()

    if 'Event:Triage' in grouped:
        print_triage_stats(grouped['Event:Triage'])
    else:
        print('No Event:Triage messages')
        print('Keys debug', sorted(grouped.keys()))

    print()
    if 'state' in grouped:
        if len(state_start_stop) > 0:
            print_location_box(state_start_stop, map_file, mbounds)
        else:
            print_location_box(grouped['state'], map_file, mbounds)
    else:
        print('No state messages')
        print('Keys debug', sorted(grouped.keys()))


def main_old(rmq_log_file, map_file):
    print(f"Reading Rita messages from {rmq_log_file}")
    print(f"Reading Map data from {map_file}")
    print(underLine)

    mbounds = None
    if map_file:
        mbounds = basemap.get_map_bounds(map_file)

    # msgs = utils.read_json(json_f)
    msgs = rmq_log_utils.read_log_file(rmq_log_file)
    print()
    # fix_timestamps_in_log.debug_timestamps(msgs)
    print()
    tb_clock = []
    appid_rkey = {}
    others = []

    # for i in range(min(5, len(msgs))):
    #     pprint(msgs[i])
    #     if 'app-id' in msgs[i] and 'routing-key' in msgs[i]:
    #         update_appid_rkey(appid_rkey, msgs[i])

    for msg_x in msgs:
        msg = msg_x[1]
        # pprint(msg)
        if 'app-id' in msg and 'routing-key' in msg:
            update_appid_rkey(appid_rkey, msg)
        elif 'tb_clock' in msg:
            tb_clock.append(msg)
        else:
            others.append(msg)

    print_appid_rkey(appid_rkey)
    formatted_print_appid_rkey('', 'clock', len(tb_clock))
    formatted_print_appid_rkey('Others', '', len(others))
    formatted_print_appid_rkey('Total', '', len(msgs))

    print()
    group_messages(appid_rkey, map_file, mbounds)
    print()


def print_rmq_log_stats(rmq_log_file):
    print(f'Reading file: {rmq_log_file}')
    print(underLine)
    msgs = rmq_log_utils.read_log_file(rmq_log_file)
    start, fmsg = msgs[0]
    end, lmsg = msgs[-1]
    start_date = datetime.datetime.fromtimestamp(start/1000)
    end_date = datetime.datetime.fromtimestamp(end/1000)
    print(f'File Duration: {end_date - start_date}')
    # for msg_x in msgs:
    #     pprint(msg_x)
    #     msg = msg_x[1]
    #     break
    print()

def main(file_or_dir):
    if os.path.isfile(file_or_dir):
        print_rmq_log_stats(file_or_dir)
    else:
        for filename in os.listdir(file_or_dir):
            if filename.endswith('.log'):
                # print('Working on', filename)
                print_rmq_log_stats(filename)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Print\'s various Rita RMQ message stats')
    parser.add_argument('rmq_log_file', help='Filename or Dir')
    # parser.add_argument('--map_file', help='Map file such as Falcon.json from basemap', default=None)
    args = parser.parse_args()
    try:
        # main_old(args.rmq_log_file, args.map_file)
        main(args.rmq_log_file)
    except Exception as e:
        print(e)
        pprint(e)
        print('Error: Some error in file', args.rmq_log_file)
