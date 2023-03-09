#!/usr/bin/env python3

# Util functions for parsing and writing RMQ logs
import argparse
import json


def parse_line(line):
    first_comma = line.find(',')
    if first_comma == -1:
        return [0, None]
    
    second_comma = line.find(',', first_comma + 1)
    if second_comma == -1:
        return [0, None]
    
    #print(first_comma, second_comma, len(line))
    timestamp = int(line[first_comma + 1:second_comma].strip())
    rest = line[second_comma + 1:].strip()
    
    # print(line[0:first_comma])
    # print(line[first_comma:second_comma])
    # print(rest)
    return [timestamp, rest]


def write_log_file(name, msgs):
    with open(name, 'w') as f:
        for ts, msg in msgs:
            jstr = json.dumps(msg, separators=(',', ':'))
            f.write(f'raw-data, {ts} , {jstr}\n')


def read_log_file(name):
    with open(name) as f:
        lines = f.readlines()
    print(f'Parsing RMQ lines: {len(lines)}')
    parsed = []

    for line in lines:
        ts, str = parse_line(line)
        if str and len(str) > 0:
            parsed.append([ts, json.loads(str)])
    return parsed


def main(filename):
    parsed = read_log_file(filename)
    print(f'Parsed lines: {len(parsed)}')


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Test reading of RMQ Log files')
    parser.add_argument('rmq_file')
    args = parser.parse_args()
    main(args.rmq_file)
