#!/usr/bin/env python3

# Given ground truth file and a directory containing HSR_Predictions*.metadata
# Create comparative metrics as written in "Study 2 evaluation document"

## Sample Metrics
## M1 is NRMSE
## M3, M6 and M7 are Mean Accuracy
# In simpler terms, given a set of data points from repeated measurements of the same quantity,
# the set can be said to be accurate if their average is close to the true value of the quantity being measured
## Trial-Name, M1 -- final_score, M3 -- , M6 -- , M7 --
import argparse
import csv
from pprint import pprint
import sys
import os
import pandas as pd
import ml_metrics

from pathlib import Path
import statistics


def setup_py_path():
    ## Setup PYTHONPATH to import from Code/bin
    pth = os.path.dirname(os.path.realpath(__file__))
    # print(pth, type(pth))
    proj_home = Path(pth).parents[3]
    # print(proj_home)
    code_bin = proj_home.joinpath('Code/bin')
    sys.path.append(str(code_bin))


setup_py_path()
import metadata_utils
import utils

trial_ground_truth = '/nfs/projects/RITA/HSR-data-mirror/study-2_2021.06/HSRData_GroundTruth-TrainingData_Study-2_Vers-6.csv'
test_ground_truth = '/nfs/projects/RITA/HSR-data-mirror/study-2_2021.06/HSRData_GroundTruth-TestData-Redacted_Study-2_Vers-6.csv'
GROUND_TRUTH = 'Ground Truth'


def read_ground_truth(gt_f):
    gt = []
    with open(gt_f, 'r') as data:
        for line in csv.DictReader(data):
            # print(line)
            gt.append(line)
    return gt


def parse_int(val):
    try:
        return int(val)
    except:
        ValueError
        pass
    return val


def organize_ground_truth(gt):
    gto = {}
    TRIAL_ID = 'Trial ID'
    TRIAL_NAME = 'Trial'
    MEASURE = 'Measure'

    START_ELAPSED_TIME = 'Start Elapsed Time'
    SUBJECT = 'Subject'
    DOOR_ID = 'Door ID'

    for x in gt:
        tid = x[TRIAL_ID]
        if tid not in gto:
            gto[tid] = {'M1': [],
                        'M3': {},
                        'M6': {},
                        'M7': [],
                        'Trial': x[TRIAL_NAME]}

        measure = x[MEASURE]
        elapsed_time = parse_int(x[START_ELAPSED_TIME])
        if isinstance(elapsed_time, int):
            elapsed_time_human = elapsed_time / (1000 * 60)
        else:
            elapsed_time_human = elapsed_time

        if measure == 'M1':
            gto[tid]['M1'].append({'value': x[GROUND_TRUTH],
                                   'elapsed_time': elapsed_time,
                                   # 'elapsed_time_human': elapsed_time_human
                                   })
        elif measure == 'M3':
            gto[tid]['M3'][x[SUBJECT]] = x[GROUND_TRUTH]
        elif measure == 'M6':
            gto[tid]['M6'][x[SUBJECT]] = x[GROUND_TRUTH]
        elif measure == 'M7':
            val = x[GROUND_TRUTH]
            if val == 'True':
                val_human = 'enter room'
            elif val == 'False':
                val_human = 'does not enter room'
            else:
                val_human = 'bad value'
            gto[tid]['M7'].append({'value': val,
                                   'value_human': val_human,
                                   'elapsed_time': elapsed_time,
                                   'subject': x[SUBJECT],
                                   'door_id': x[DOOR_ID],
                                   'elapsed_time_human': elapsed_time_human})

    print('GTO N Trials:', len(gto))
    return gto


def add_predictions(pmsgs, pred):
    for m in pmsgs:
        tid = m['msg']['trial_id']
        if tid not in pred:
            pred[tid] = {'M1': [],
                         'M3': [],
                         'M6': [],
                         'M7': []}
        trial_predictions = m['data']['predictions']
        for p in trial_predictions:
            measure = p['predicted_property']
            # print('measure', measure)
            if measure == 'M1:team_performance':
                pred[tid]['M1'].append([p['subject'], p['prediction']])
            elif measure == 'M3:participant_map':
                pred[tid]['M3'].append([p['subject'], p['prediction']])
            elif measure == 'M6:participant_block_legend':
                pred[tid]['M6'].append([p['subject'], p['prediction']])
            elif measure == 'M7:participant_room_enter':
                # pprint(p)
                pred[tid]['M7'].append([p['start_elapsed_time'], p['subject'], p['object'], p['action']])


def read_pred_msgs(dir):
    for filename in os.listdir(dir):
        if filename.startswith('HSRData_Predictions_DollMit_RitaAgent_Trial'):
            msgs = metadata_utils.read_file(filename)
            return msgs
    return []


def read_predictions(dir):
    preds = {}
    msgs = read_pred_msgs(dir)
    print(len(msgs))
    add_predictions(msgs, preds)
    print('PREDS N Trials', len(preds))
    return preds, msgs


def add_m1(gt, preds, tid, metrics):
    if len(preds[tid]['M1']):
        for subject, value in preds[tid]['M1']:
            met = {}
            met['trial_id'] = tid

            # Add Trial Name from GT
            if tid in gt:
                met['Trial'] = gt[tid]['Trial']

            met['prediction'] = parse_int(value)
            met['subject'] = subject
            if tid in gt and len(gt[tid]['M1']) > 0:
                met[GROUND_TRUTH] = parse_int(gt[tid]['M1'][-1]['value'])
                # print(subject, value, met[GROUND_TRUTH])
                if isinstance(met[GROUND_TRUTH], int):
                    met['m1_abs_error'] = abs(met['prediction'] - met[GROUND_TRUTH])
            metrics.append(met)


def add_m3(gt, preds, tid, metrics):
    if len(preds[tid]['M3']):
        for subject, value in preds[tid]['M3']:
            met = {}
            met['trial_id'] = tid
            if tid in gt:
                met['Trial'] = gt[tid]['Trial']

            met['prediction'] = value
            met['subject'] = subject
            # print(subject, value)
            # print(gt[tid]['M3'])
            if tid in gt and len(gt[tid]['M3']) > 0:
                met[GROUND_TRUTH] = gt[tid]['M3'][subject]
                # print(subject, value, met[GROUND_TRUTH])
                if value == met[GROUND_TRUTH]:
                    met['m3_score'] = 1
                else:
                    met['m3_score'] = 0
            metrics.append(met)


def add_m6(gt, preds, tid, metrics):
    if len(preds[tid]['M6']):
        for subject, value in preds[tid]['M6']:
            met = {}
            met['trial_id'] = tid
            if tid in gt:
                met['Trial'] = gt[tid]['Trial']

            met['prediction'] = value
            met['subject'] = subject
            # print(subject, value)
            # print(gt[tid]['M3'])
            if tid in gt and len(gt[tid]['M6']) > 0:
                met[GROUND_TRUTH] = gt[tid]['M6'][subject]
                # print(subject, value, met[GROUND_TRUTH])
                if value == met[GROUND_TRUTH] or met[GROUND_TRUTH].startswith(value):
                    met['m6_score'] = 1
                else:
                    met['m6_score'] = 0
            metrics.append(met)


def find_m7_in_gt(gt, tid, elapsed_time, subject, object):
    if tid not in gt:
        # print(tid, 'not in GT')
        return None
    if 'M7' not in gt[tid]:
        print('No M7 for tid', tid)
        return None

    # pprint(gt[tid]['M7'])
    # print('Matching', elapsed_time, subject, object)
    for m7 in gt[tid]['M7']:
        # print(m7)
        if elapsed_time == m7['elapsed_time'] and object == m7['door_id'] and subject == m7['subject']:
            return m7
        else:
            # print('No match')
            pass
    return None


def add_m7(gt, preds, tid, metrics):
    pred_count = len(preds[tid]['M7'])
    gt_count = 0
    trial = tid
    if tid in gt:
        gt_count = len(gt[tid]['M7'])
        trial = gt[tid]['Trial']

    if pred_count != gt_count:
        print(trial, 'm7 count MISMATCH', 'GT', gt_count, 'PRED', pred_count)
    else:
        print(trial, 'm7 count MATCH', 'GT', gt_count, 'PRED', pred_count)

    # print('Will match M7 exactly by elapsed_time, subject (participant id), object (door id)')
    if len(preds[tid]['M7']):
        # pprint(preds[tid]['M7'])
        for p in preds[tid]['M7']:
            met = {}
            met['trial_id'] = tid
            if tid in gt:
                met['Trial'] = gt[tid]['Trial']
            # pprint(p)
            elapsed_time, subject, object, prediction = p
            prediction_value = None
            if prediction == 'will_not_enter_room':
                prediction_value = 'False'
            elif prediction == 'will_enter_room':
                prediction_value = 'True'

            m7gt = find_m7_in_gt(gt, tid, elapsed_time, subject, object)
            # if m7gt:
            #     print(prediction_value, p, m7gt, )
            if prediction_value is not None and m7gt is not None and prediction_value == m7gt['value']:
                met['m7_score'] = 1
                # print('Got m7 success')
            else:
                met['m7_score'] = 0
            metrics.append(met)


def compute_m1_nrmse(metrics):
    predictions = []
    ground_truth = []
    for m in metrics:
        # pprint(m)
        if 'prediction' in m:
            prediction = m['prediction']
            gt = None
            if GROUND_TRUTH in m:
                gt = m[GROUND_TRUTH]
            # print(prediction, type(prediction), gt, type(gt))
            if isinstance(prediction, int) and isinstance(gt, int):
                predictions.append(prediction)
                ground_truth.append(gt)

    if len(ground_truth) == 0:
        return 1
    rmse = ml_metrics.rmse(ground_truth, predictions)
    nrmse = rmse / statistics.mean(predictions)
    # print('GT', len(ground_truth), ground_truth)
    # print('PR', len(predictions), predictions)
    # print('RMSE', rmse)
    print('Got m1 total values', len(ground_truth), 'Normalized RMSE ', nrmse, 'accuracy', 1 - nrmse)
    return nrmse


def compute_mean_accuracy(metrics, field):
    sum = 0
    count = 0
    for m in metrics:
        # pprint(m)
        if field in m:
            sum = sum + m[field]
            count = count + 1

    mean_accuracy = 0
    if count > 0:
        mean_accuracy = sum / count

    print('Got ', field, 'total values', count, 'mean accuracy', mean_accuracy)
    if count == 0:
        return None
    return mean_accuracy


def compare_metrics(gt, preds):
    metrics = []
    field_names = ['Trial', 'subject', GROUND_TRUTH, 'prediction', 'm1_abs_error', 'm3_score', 'm6_score', 'm1_nrmse',
                   'm1_accuracy', 'm3_accuracy', 'm6_accuracy']
    if not len(preds):
        print('No predictions found')
        return

    for tid, dat in preds.items():
        # print('For Trial', tid)
        # if tid not in gt:
        #     print('No Ground truth for:', tid)

        add_m1(gt, preds, tid, metrics)
        add_m3(gt, preds, tid, metrics)
        add_m6(gt, preds, tid, metrics)
        add_m7(gt, preds, tid, metrics)
    print('Ground Truth', len(gt))
    m1_nrmse = compute_m1_nrmse(metrics)
    # pprint(metrics)
    m3_accuracy = compute_mean_accuracy(metrics, 'm3_score')
    m6_accuracy = compute_mean_accuracy(metrics, 'm6_score')
    m7_accuracy = compute_mean_accuracy(metrics, 'm7_score')
    metrics.insert(0, {'Trial': 'All Trials',
                       'm1_nrmse': m1_nrmse,
                       'm1_accuracy': 1 - m1_nrmse,
                       'm3_accuracy': m3_accuracy,
                       'm6_accuracy': m6_accuracy,
                       'm7_accuracy': m7_accuracy})
    # wrtr = csv.DictWriter(sys.stdout, field_names)
    with open('metrics.csv', 'w', newline='') as csvfile:
        wrtr = csv.DictWriter(csvfile, field_names, extrasaction='ignore')
        wrtr.writeheader()
        wrtr.writerows(metrics)
    return metrics


def print_counts(gt, preds):
    counts = {'M1': {'gt': [],
                     'preds': []},
              'M3': {'gt': [],
                     'preds': []},
              'M6': {'gt': [],
                     'preds': []},
              'M7': {'gt': [],
                     'preds': []}}
    for tid, dat in preds.items():
        trial = tid
        if tid in gt:
            counts['M1']['gt'].extend(gt[tid]['M1'])
            counts['M3']['gt'].extend(gt[tid]['M3'])
            counts['M6']['gt'].extend(gt[tid]['M6'])
            counts['M7']['gt'].extend(gt[tid]['M7'])
            trial = gt[tid]['Trial']

        counts['M1']['preds'].extend(dat['M1'])
        counts['M3']['preds'].extend(dat['M3'])
        counts['M6']['preds'].extend(dat['M6'])
        counts['M7']['preds'].extend(dat['M7'])

        print('Trial: ', trial)
        xcounts = 0

        if tid in gt:
            xcounts = len(gt[tid]['M1'])
        print('M1 predictions: ', len(dat['M1']), 'GT: ', xcounts)
        if len(dat['M1']) == 0:
            print(trial, 'BAD M1')

        if tid in gt:
            xcounts = len(gt[tid]['M3'])
        print('M3 predictions: ', len(dat['M3']), 'GT: ', xcounts)
        if len(dat['M3']) == 0:
            print(trial, 'BAD M3')

        if tid in gt:
            xcounts = len(gt[tid]['M6'])
        print('M6 predictions: ', len(dat['M6']), 'GT: ', xcounts)
        if len(dat['M6']) == 0:
            print(trial, 'BAD M6')

        m7_unmatched = []
        m7_matched = []
        for m in dat['M7']:
            elapsed_time, subject, object, prediction = m
            found = find_m7_in_gt(gt, tid, elapsed_time, subject, object)
            if not found:
                m7_unmatched.append(m)
            else:
                m7_matched.append(m)

        if tid in gt:
            xcounts = len(gt[tid]['M7'])
        print('M7 predictions: ', len(dat['M7']), 'GT: ', xcounts, 'matched:', len(m7_matched),
              'unmatched',
              len(m7_unmatched))
        print('Unmatched M7')
        if len(m7_unmatched) < 5:
            pprint(m7_unmatched)

        if len(dat['M7']) == 0:
            print(trial, 'BAD M7')
        print()

    print()
    print('Total Counts')
    print('M1 predictions:', len(counts['M1']['preds']), 'GT:', len(counts['M1']['gt']) * 3)
    print('M3 predictions:', len(counts['M3']['preds']), 'GT:', len(counts['M3']['gt']) * 3)
    print('M6 predictions:', len(counts['M6']['preds']), 'GT:', len(counts['M6']['gt']) * 3)
    print('M7 predictions:', len(counts['M7']['preds']), 'GT:', len(counts['M7']['gt']))
    print()


def add_gt(p, gto):
    if p['predicted_property'] == 'M1:team_performance' and len(gto['M1']) > 0:
        p[GROUND_TRUTH] = parse_int(gto['M1'][-1]['value'])

    if p['predicted_property'] == 'M3:participant_map':
        for k, val in gto['M3'].items():
            if k == p['subject']:
                p[GROUND_TRUTH] = val

    if p['predicted_property'] == 'M6:participant_block_legend':
        for k, val in gto['M6'].items():
            if k == p['subject']:
                p[GROUND_TRUTH] = val

    if p['predicted_property'] == 'M7:participant_room_enter':
        elapsed_time = p['start_elapsed_time']
        subject = p['subject']
        object = p['object']
        for m7 in gto['M7']:
            if elapsed_time == m7['elapsed_time'] and \
                    subject == m7['subject'] and \
                    object == m7['door_id']:
                val = m7['value']
                if val == 'True':
                    p[GROUND_TRUTH] = 'will_enter_room'
                if val == 'False':
                    p[GROUND_TRUTH] = 'will_not_enter_room'
                # pprint(gto)
                # pprint(p)
        # exit(0)


def add_property(pname, frm, to):
    if pname in frm:
        to[pname] = frm[pname]


def make_data_frame(gto, pred_msgs):
    frame = []
    for m in pred_msgs:
        tid = m['msg']['trial_id']
        trial_predictions = m['data']['predictions']
        for p in trial_predictions:
            px = {}
            px['trial_id'] = tid
            px['predicted_property'] = p['predicted_property']
            px['subject'] = p['subject']
            add_property('prediction', p, px)
            add_property('start_elapsed_time', p, px)
            add_property('object', p, px)
            add_property('action', p, px)
            if tid in gto:
                px['Trial'] = gto[tid]['Trial']
                add_gt(px, gto[tid])
            frame.append(px)
    print('Predictions', len(frame))
    return frame


def main(pred_dir):
    gt_trial = read_ground_truth(trial_ground_truth)
    gt_test = read_ground_truth(test_ground_truth)
    gt = gt_trial
    gt.extend(gt_test)
    gto = organize_ground_truth(gt)
    utils.write_json(gto, 'ground-truth.json')
    # pprint(gto)
    preds, pred_msgs = read_predictions(pred_dir)
    utils.write_json(preds, 'predictions.json')
    # pprint(preds)
    print_counts(gto, preds)
    compare_metrics(gto, preds)
    # Create a data frame then collect and print stats. SOMEDAY if needed
    # frame = make_data_frame(gto, pred_msgs)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Compare our study-2 predictions with ground truth')
    parser.add_argument('pred_dir', help='Dir containing predictions in metadata format')
    args = parser.parse_args()
    main(args.pred_dir)
