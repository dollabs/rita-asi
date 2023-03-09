#!/usr/bin/env python3

# Elkless player pumps all MQTT messages to RMQ as fast as it could. So we loose ability to replay
# trial data in real time. This script fixes the timestamp(second column) in RMQ log file to the same value
# as header timestamp

import argparse
from dateutil.parser import parse
from pathlib import Path
import rmq_log_utils
import signal
import sys

force_process = False


def debug_timestamps(msgs):
    ht = []
    mt = []
    first_hts = None
    last_hts = None
    first_mts = None
    last_mts = None
    for x in msgs:
        # pprint(x)
        # print(x['testbed-message']['header']['timestamp'])

        try:
            if 'testbed-message' in x:

                hts = parse(x['testbed-message']['header']['timestamp'])
                last_hts = hts
                if first_hts is None:
                    first_hts = hts

                mts = parse(x['testbed-message']['msg']['timestamp'])
                last_mts = mts
                if first_mts is None:
                    first_mts = mts

                ht_millis = hts.timestamp()
                mt_millis = mts.timestamp()

                ht.append(int(ht_millis * 1000))
                mt.append(int(mt_millis * 1000))
        except:
            # print('timestamp lookup failed:')
            # pprint(x)
            pass

    header_diff = (max(ht) - min(ht)) / 1000
    message_diff = (max(mt) - min(mt)) / 1000
    print('Header First message Time:', first_hts)
    print('Header Last message Time:', last_hts)
    # print('___________________________________')
    # print('Message First message Time:', first_mts)
    # print('Message Last message Time:', last_mts)
    print('___________________________________')
    print('duration from header', header_diff)
    # print('duration from message', message_diff)
    # print('Delta:', message_diff - header_diff)
    return header_diff


def print_duration(f):
    lines = rmq_log_utils.read_log_file(f)
    msgs = []
    ts = []
    for t, msg in lines:
        ts.append(t)
        msgs.append(msg)
    print('Stats: ', f)
    header_duration = debug_timestamps(msgs)
    rmq_duration = (max(ts) - min(ts)) / 1000
    delta = abs(header_duration - rmq_duration)
    print('RMQ Duration:', rmq_duration)
    if delta > 1000:
        print('Warn Header and RMQ duration differ by: secs', delta)


def get_ts_from_header(msg):
    millis = None
    if 'testbed-message' in msg:
        ts = parse(msg['testbed-message']['header']['timestamp'])
        # print(ts, ts.timestamp())
        millis = ts.timestamp() * 1000

    if 'tb_clock' in msg:
        ts = parse(msg['tb_clock'])
        # print(ts, ts.timestamp())
        millis = ts.timestamp() * 1000
    return int(millis)


def process_rmq_log(f: Path):
    of = Path(f.name)
    if of.exists() and not force_process:
        print('Exists not processing:', of)
        return
    print('Creating ...', of)
    lines = rmq_log_utils.read_log_file(f)
    fixed = []
    for _, msg in lines:
        millis = get_ts_from_header(msg)
        if millis is not None:
            fixed.append([millis, msg])
    # print(fixed[0])
    rmq_log_utils.write_log_file(of, fixed)


def main(file, stats):
    f = Path(file)
    if f.is_file():
        print('Processing ...', f)
        if not stats:
            process_rmq_log(f)
        else:
            print_duration(f)
    elif f.is_dir():
        print('Processing dir:', f)
        rmq_files = [x for x in f.iterdir() if x.is_file() and x.suffix == '.log']
        rmq_files = sorted(rmq_files)
        # print(rmq_files)
        for x in rmq_files:
            if not stats:
                process_rmq_log(x)
            else:
                print_duration(x)
            print()
    else:
        print('Unknown file:', f)


def signal_handler(sig, frame):
    print('You pressed Ctrl+C!')
    sys.exit(0)


signal.signal(signal.SIGINT, signal_handler)

# Ex: In a empty CWD, run this file as
# $ritagit/Code/bin/fix_timestamps_in_log.py ../

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Script to fix timestamps in RMQ log file from headers')
    parser.add_argument('file')
    parser.add_argument('--stats', action='store_true')
    # parser.add_argument('temp_dir')
    args = parser.parse_args()
    try:
        main(args.file, args.stats)
    except KeyboardInterrupt:
        print('Keyboard Interrupt')

## To be cleanedup

# def main_x(infile, out_dir):
#     if check_out_file_exists(out_dir, infile):
#         print('Exists: ', out_dir, infile)
#         return
#
#     print('Working on: ', infile)
#
#     msgs = rmq_reader.read_rmq_log(infile)
#     print(f'line count: {len(msgs)}', infile)
#
#     debug_timestamps(msgs)
#     fix_ts_and_write(msgs, out_dir, infile)

# def read_msgs(infile):
#     f = open(infile)
#     lines = f.readlines()
#     json_lines = []
#     for line in lines:
#         # line.replace('\n', '')
#         # jsn_str = line.split(', ')[-1]
#         # print(len(jsn_str), len(json_lines))
#         # print(jsn_str)
#         jsn_str = line
#         jsn = None
#         try:
#             jsn = json.loads(jsn_str)
#         except json.decoder.JSONDecodeError as e:
#             print(e)
#             print('json error', len(json_lines))
#             print(line)
#             print(jsn_str)
#         if jsn is not None:
#             json_lines.append(jsn)
#         else:
#             print('jsn is none for', line)
#
#     return json_lines
#
#
# def to_rmq_log_format(fixed_ts):
#     all_lines = []
#     for f in fixed_ts:
#         all_lines.append('raw-data, {}, {}'.format(f[0], json.dumps(f[1])))
#     return all_lines
#
#
# def fix_ts_and_write(msgs, out_dir, out_file_x):
#     fixed = fix_timestamps(msgs)
#     if not os.path.isdir(out_dir):
#         print('Creating temp dir', out_dir)
#         os.mkdir(out_dir)
#     out_file = out_dir + '/' + os.path.basename(out_file_x)
#     print('writing output log to', out_file)
#     rmq_log_utils.write_log_file(out_file, fixed)
#     # write_lines(to_rmq_log_format(fixed), out_file)
#
#
# def check_out_file_exists(out_dir, out_file_x):
#     out_file = out_dir + '/' + os.path.basename(out_file_x)
#     return os.path.exists(out_file)

# try:
#     import rmq_reader
# except ModuleNotFoundError:
#     print('Failed to import rmq_reader')
#     print('Ensure Code/data in in PYTHONPATH ')
#     sys.exit(1)


# def write_lines(lines, filename):
#     out_f = open(filename, "w")
#     for line in lines:
#         out_f.write(line)
#         out_f.write("\n")
#     out_f.close()

# def fix_timestamps(msgs):
#     # print(msgs)
#     fixed = []
#
#     for x in msgs:
#         # pprint(x)
#         # print(x['testbed-message']['header']['timestamp'])
#         millis = get_ts_from_header()
#         if millis is not None:
#             fixed.append([millis, x])
#         else:
#             print('timestamp not found for ', x)
#
#     return fixed
