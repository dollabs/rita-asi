#!/usr/bin/env python3

import sys
import os
import argparse
from pathlib import Path
from pprint import pprint
import json

# print(f"this script {sys.argv[0]}")
pth = os.path.dirname(os.path.realpath(__file__))
# print(pth, type(pth))

proj_home = Path(pth).parents[1]
code_data = proj_home.joinpath('Code/data')
# print(proj_home)
# print(code_data)
sys.path.append(str(code_data))
# pprint(sys.path)
import rmq_reader


def clean_msgs(msgs):
    # for msg in msgs:
    # for i in range(10):
    for msg in msgs:
        # msg = msgs[i]
        msg.pop('timestamp', None)
        # print('--------')
        # pprint(msg)
    return msgs


def main(input, output):
    print(f'Reading json lines from: {input}')
    print(f'Writing json to: {output}')
    msgs = rmq_reader.read_rmq_log(input)
    print(f'Got {len(msgs)}')
    msgs = clean_msgs(msgs)
    with open(output, 'w') as outfile:
        json.dump(msgs, outfile, sort_keys=True, indent=2)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Script to rmq json messages to single json file')
    parser.add_argument('rmq_file')
    parser.add_argument('json_file')
    args = parser.parse_args()
    main(args.rmq_file, args.json_file)
