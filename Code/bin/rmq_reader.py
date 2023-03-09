#!/usr/bin/env python3

import sys
import json
from pprint import pprint

# Deprecated, 3/21/2021. Anything useful here should go to rmq_log_utils
# See rmq_log_utils


def read_rmq_log(rmq_log_file):
    """
    Assumes rmq_log_file contains json message only. i.e first 2 columns of rmq log file are removed.
        RMQ log files have messages in 3 column CSV format. 1st Column is raw-data, 2nd Column is Unix TIMESTAMP since epoch in millis
        and 3rd column is a JSON message.
        Sometimes, RMQ log files will also have messages pretty printed.

     To go from 3 column log format to only json messages, use
        grep raw-data valentine-2.csv.x | gcut --complement -d',' -f1 | gcut --complement -d',' -f1 > valentine-2.csv
        where valentine-2.csv.x is log file with 3 columns
              valentine-2.csv is output file with json message per line
        Or
        ./rmq_log_2_json_per_line.sh march-5-5.csv
            and output file will be `march-5-5.csv.1_col.csv`

    Pass the output file as input to this func
    :param filepath:
    :return:
    """
    msgs = []
    with open(rmq_log_file) as fp:
        line = fp.readline()
        number = 1
        while line:
            # print(line.strip())
            line.rstrip()
            if len(line) > 0:
                try:
                    jsn = json.loads(line)
                    msgs.append(jsn)
                except ValueError:
                    print(f'Value error at line {number} length {len(line)}\nline ->{line}')
            number = number + 1
            line = fp.readline()
    return msgs


def read_rmq_log_with_skip(file_name, skip_value):
    """

    :param file_name:
    :param skip_value: skips rows every skip_value
    :return:
    """

    data = read_rmq_log(file_name)
    filtered = []
    for i in range(0, len(data), skip_value):
        filtered.append(data[i])
    return data, filtered


def to_csv(data, of):
    header = 'name,x,y,z,rmq_recv_time,msg_header_time'
    if of is not None:
        of.write(header)
        of.write('\n')
    else:
        print(header)
    xv = []
    yv = []
    zv = []
    xdelta = []
    ydelta = []
    zdelta = []

    prev_msg = None
    for msg in data:
        # pprint(msg)
        rmq_recv_ts = msg['timestamp']
        d = msg['testbed-message']['data']
        header_ts = msg['testbed-message']['header']['timestamp']
        xv.append(d['x'])
        yv.append(d['y'])
        zv.append(d['z'])
        line = '{}, {}, {}, {}, {}, {}'.format(d['name'], d['x'], d['y'], d['z'], rmq_recv_ts, header_ts)
        if of is not None:
            of.write(line)
            of.write('\n')
        else:
            print(line)

        if prev_msg is not None:
            pd = prev_msg['testbed-message']['data']
            dx = abs(d['x'] - pd['x'])
            dy = abs(d['y'] - pd['y'])
            dz = abs(d['z'] - pd['z'])
            threshold = 2
            if dx > threshold:
                print('delta X between 2 consecutive messages >', threshold, 'actual:', dx)
                print(d)
                print(pd)
                print()

            if dy > threshold:
                print('delta Y between 2 consecutive messages >', threshold, 'actual:', dy)
                print(d)
                print(pd)
                print()

            if dz > threshold:
                print('delta Z between 2 consecutive messages >', threshold, 'actual:', dz)
                print(d)
                print(pd)
                print()

        prev_msg = msg

    for i in range(1, len(xv)):
        xdelta.append(abs(xv[i] - xv[i - 1]))
        ydelta.append(abs(yv[i] - yv[i - 1]))
        zdelta.append(abs(zv[i] - zv[i - 1]))

    print('x range {} {}'.format(min(xv), max(xv)))
    print('y range {} {}'.format(min(yv), max(yv)))
    print('z range {} {}'.format(min(zv), max(zv)))
    print('x-delta range {} {}'.format(min(xdelta), max(xdelta)))
    print('y-delta range {} {}'.format(min(ydelta), max(ydelta)))
    print('z-delta range {} {}'.format(min(zdelta), max(zdelta)))

    # print('Unique x {}'.format(set(xv)))
    # print('Unique y {}'.format(set(yv)))
    # print('Unique z {}'.format(set(zv)))


def main(rmq_file, of):
    data, filtered = read_rmq_log_with_skip(rmq_file, 10)  # 1 per second
    print('For {} got {} messages'.format(rmq_file, len(data)))
    print('Filtered messages {}'.format(len(filtered)))
    # to_csv(filtered)
    to_csv(data, of)


if __name__ == "__main__":
    args = sys.argv
    argc = len(args)
    if argc < 3:
        print('Usage {} \'rmq csv file with json msg per line\' out_file.csv'.format(args[0]))
        sys.exit(1)
    of = None
    if argc > 2:
        of = open(args[2], 'w')

    main(args[1], of)
    if of is not None:
        of.close()
