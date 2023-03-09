#!/usr/bin/env python3

import csv
import argparse
from pprint import pprint
import sys

aus = {'AU01_c', 'AU02_c', 'AU04_c', 'AU05_c', 'AU05_c', 'AU06_c', 'AU07_c', 'AU09_c', 'AU12_c', 'AU14_c', 'AU15_c',
       'AU17_c', 'AU20_c', 'AU23_c', 'AU26_c'}


def parse_csv(fname):
    dat = []
    with open(fname, newline='') as csvfile:
        rdr = csv.DictReader(csvfile)
        for row in rdr:
            for x, val in row.items():
                if x.startswith('AU'):
                    row[x] = float(val)
            dat.append(row)
    return dat


def filter_pre_post(arr):
    dat = []
    for item in arr:
        mission = item['mission']
        if not mission.startswith('Pre -') and not mission.startswith('Post -'):
            dat.append(item)
    return dat


def sec_to_millis(tm):
    return int(tm * 1000)


def get_start_stop_times(filt_data):
    mn = min(filt_data, key=lambda k: k['timestamp'])
    mx = max(filt_data, key=lambda k: k['timestamp'])
    # print(mn['timestamp'])
    # print(mx['timestamp'])
    return {'start-time': sec_to_millis(float(mn['timestamp'])),
            'stop-time': sec_to_millis(float(mx['timestamp']))}


def get_duration(dat):
    if len(dat) > 0:
        timing = get_start_stop_times(dat)
        mn = timing['start-time']
        mx = timing['stop-time']
        return float(mx) - float(mn)
    return 0


def get_emotions(au):
    labels = set()
    if au["AU06_c"] == 1 and au["AU12_c"] == 1:
        labels.add("happiness")

    if au["AU01_c"] == 1 and au["AU04_c"] == 1 and au["AU15_c"] == 1:
        labels.add("sadness")

    if au["AU01_c"] == 1 and au["AU02_c"] == 1 and au["AU05_c"] == 1 and au["AU26_c"] == 1:
        labels.add("surprise")

    if (au["AU01_c"] == 1 and au["AU02_c"] == 1 and
            au["AU04_c"] == 1 and au["AU05_c"] == 1 and
            au["AU07_c"] == 1 and au["AU20_c"] == 1 and
            au["AU26_c"] == 1):
        labels.add("fear")

    if au["AU04_c"] == 1 and au["AU05_c"] == 1 and au["AU07_c"] == 1 and au["AU23_c"] == 1:
        labels.add("anger")

    if au["AU09_c"] == 1 and au["AU15_c"] == 1 and au["AU17_c"] == 1:
        labels.add("disgust")

    if au["AU12_c"] == 1 and au["AU14_c"] == 1:
        labels.add("contempt")

    return [x for x in labels]


def update_emotions(dat):
    for d in dat:
        labels = get_emotions(d)
    return dat


def print_au_min_max(dat):
    x = {}
    for d in dat:
        for au in aus:
            if au not in x:
                x[au] = []
            x[au].append(d[au])

    mm = {}
    for au in aus:
        if au not in mm:
            mm[au] = {}
        mm[au]['min'] = min(x[au])
        mm[au]['max'] = max(x[au])

    # pprint(mm)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Eye Tracking CSV file')
    parser.add_argument('csv_file')
    args = parser.parse_args()
    data = parse_csv(args.csv_file)
    data = filter_pre_post(data)
    duration = get_duration(data)
    print('Data len', len(data))
    print('Duration', duration)
    # print(data)
    update_emotions(data)
    print_au_min_max(data)

    ########
    # // Reference: Friesen, W. V., & Ekman, P. (1983). EMFACS-7: Emotional facial
    # // action coding system. Unpublished manuscript, University of California at
    # // San Francisco, 2(36), 1.
    # // Refer to: https://en.wikipedia.org/wiki/Facial_Action_Coding_System
    # // and https://imotions.com/blog/facial-action-coding-system/
    #
    # unordered_set<string> WebcamSensor::get_emotions(json au) {
    #     unordered_set<string> labels;
    #
    # if (au["AU06"]["occurrence"] == 1 && au["AU12"]["occurrence"] == 1) {
    #     labels.insert("happiness");
    # }
    # if (au["AU01"]["occurrence"] == 1 && au["AU04"]["occurrence"] == 1 &&
    #         au["AU15"]["occurrence"] == 1) {
    # labels.insert("sadness");
    # }
    # if (au["AU01"]["occurrence"] == 1 && au["AU02"]["occurrence"] == 1 &&
    # au["AU05"]["occurrence"] == 1 && au["AU26"]["occurrence"] == 1) {
    # labels.insert("surprise");
    # }
    # if (au["AU01"]["occurrence"] == 1 && au["AU02"]["occurrence"] == 1 &&
    # au["AU04"]["occurrence"] == 1 && au["AU05"]["occurrence"] == 1 &&
    # au["AU07"]["occurrence"] == 1 && au["AU20"]["occurrence"] == 1 &&
    # au["AU26"]["occurrence"] == 1) {
    # labels.insert("fear");
    # }
    # if (au["AU04"]["occurrence"] == 1 && au["AU05"]["occurrence"] == 1 &&
    # au["AU07"]["occurrence"] == 1 && au["AU23"]["occurrence"] == 1) {
    # labels.insert("anger");
    # }
    # if (au["AU09"]["occurrence"] == 1 && au["AU15"]["occurrence"] == 1 &&
    # au["AU17"]["occurrence"] == 1) {
    # labels.insert("disgust");
    # }
    # if (au["AU12"]["occurrence"] == 1 && au["AU14"]["occurrence"] == 1) {
    # labels.insert("contempt");
    # }
    #
    # return labels;
