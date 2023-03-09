#!/usr/bin/env python3
import json


def read_json(json_file_name):
    try:
        with open(json_file_name) as jsn_file:
            return json.load(jsn_file)
    except FileNotFoundError:
        print('File not found error', json_file_name)
    return {}


def write_json(jsn, fname):
    with open(fname, 'w') as fp:
        json.dump(jsn, fp, sort_keys=True, indent=True)


def write_lines(dat, fname):
    with open(fname, 'w') as fp:
        for l in dat:
            fp.write(f'{l}\n')


## Given a list of objects, convert each element of the list to json string and return a list.
def list_to_json(lst):
    ret = []
    for x in lst:
        ret.append(json.dumps(x))
    return ret


def read_lines(fname):
    ret = []
    try:
        with open(fname, 'r') as f:
            ret = f.readlines()
    except FileNotFoundError:
        print('File not found error', fname)
    return ret
