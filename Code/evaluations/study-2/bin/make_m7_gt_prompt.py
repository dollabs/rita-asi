#!/usr/bin/env python3
import argparse
import sys

import jsonschema
import json

import os

from pathlib import Path

from pprint import pprint


def setup_py_path():
    ## Setup PYTHONPATH to import from Code/bin
    pth = os.path.dirname(os.path.realpath(__file__))
    # print(pth, type(pth))
    proj_home = Path(pth).parents[3]
    # print(proj_home)
    code_bin = proj_home.joinpath('Code/bin')
    sys.path.append(str(code_bin))


setup_py_path()

import compare_with_ground_truth
import rmq_log_utils

ground_truth_train = '/Volumes/projects/RITA/HSR-data-mirror/study-2_2021.06/HSRData_GroundTruth-TrainingData_Study-2_Vers-6.csv'
ground_truth_test = '/Volumes/projects/RITA/HSR-data-mirror/study-2_2021.06/HSRData_GroundTruth-TestData-Redacted_Study-2_Vers-6.csv'


def read_m7_gt(fname):
    gt = compare_with_ground_truth.read_ground_truth(fname)
    print(fname + ' has GT', len(gt))
    gt_x = []
    for x in gt:
        if x['Measure'] == 'M7':
            gt_x.append(x)
    print(fname + 'has M7 GT', len(gt_x))
    return gt_x


def make_rmq_message(gt):
    msg = {'app-id': 'make_m7_ground_truth_prompt',
           'routing-key': 'ground_truth_prompt',
           'ground_truth_prompt_msgs': gt}
    msg = [0, msg]
    return msg


def main():
    gt_test = read_m7_gt(ground_truth_test)
    gt_train = read_m7_gt(ground_truth_train)
    print('Train ', len(gt_train), 'Test', len(gt_test))
    gt = []
    for x in gt_test:
        gt.append(x)

    for x in gt_train:
        gt.append(x)
    print('Total M7 GT', len(gt))
    rmq_message = make_rmq_message(gt)
    rmq_log_utils.write_log_file('m7_ground_truth_prompt.log', [rmq_message])


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Create a prompt message for groun truth')

    args = parser.parse_args()
    main()
