#!/usr/bin/env python3
import argparse
import utils
from pprint import pprint


## prediction example
# "StateEstimation--enter-room": {
#     "counts": {
#         "false": 24,
#         "true": 20
#     },
#     "probability": {
#         "false": 0.5454545454545455,
#         "true": 0.4545454545454545
#     },
#     "totals": 44
# }

## Probability example
# "StateEstimation--enter-room": {
#     "probability": {
#         "unknown": 0.3125,
#         "true": 0.25,
#         "false": 0.4375
#     },
#     "totals": 16,
#     "counts": {
#         "unknown": 5,
#         "true": 4,
#         "false": 7
#     },
#     "_id": "StateEstimation--enter-room"
# },
def update_probability(pred_id, values, probability):
    if pred_id not in probability:
        probability[pred_id] = {'totals': 0, 'counts': {}, 'probability': {}}
    probability[pred_id]['totals'] += values['totals']
    value_counts = values['counts']
    # pprint(value_counts)
    for tfu, tfu_val in value_counts.items():
        if tfu not in probability[pred_id]['counts']:
            probability[pred_id]['counts'][tfu] = tfu_val
        else:
            probability[pred_id]['counts'][tfu] += tfu_val
        if tfu not in probability[pred_id]['probability']:
            probability[pred_id]['probability'][tfu] = 0
        probability[pred_id]['probability'][tfu] = probability[pred_id]['counts'][tfu] / probability[pred_id]['totals']


def main(prediction_stats):
    stats = utils.read_json(prediction_stats)
    uniq_trials = {}
    probability = {}
    x = 0
    for exp_id, trials in stats.items():
        for trial_id, trial_stats in trials.items():
            if trial_id not in uniq_trials:
                predictions = trial_stats['prediction-stats']['mission-stats-detail']
                uniq_trials[trial_id] = predictions
                for pred_id, values in predictions.items():
                    update_probability(pred_id, values, probability)
            # if x == 5:
            #     break
    # pprint(probability)
    print('Creating prediction-probability for trials', len(uniq_trials.keys()))
    # print(uniq_trials.keys())
    utils.write_json(probability, 'prediction-probability.json')


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Creates prediction-probability.json from prediction-stats.json')
    parser.add_argument('prediction_stats')
    args = parser.parse_args()
    main(args.prediction_stats)
