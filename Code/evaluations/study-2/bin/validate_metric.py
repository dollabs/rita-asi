#!/usr/bin/env python3
import argparse
import sys

import jsonschema
import json

## Given a directory containing HSR prediction messages in metadata format,
## validate all messages in all metadata files.
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

import utils
import metadata_utils

f_action_schema = '/Users/prakash/projects/asist-rita/git/testbed/MessageSpecs/Agent/Prediction/Action/agent_action_prediction_message.json'
action_sch = utils.read_json(f_action_schema)

f_state_schema = '/Users/prakash/projects/asist-rita/git/testbed/MessageSpecs/Agent/Prediction/State/agent_state_prediction_message.json'
state_sch = utils.read_json(f_state_schema)

f_prediction_schema = '/Users/prakash/projects/asist-rita/git/testbed/MessageSpecs/Agent/Prediction/agent_prediction_message.json'
pred_sch = utils.read_json(f_prediction_schema)

f_header_sch = '/Users/prakash/projects/asist-rita/git/testbed/MessageSpecs/Common_Header/common_header.json'
hdr_sch = utils.read_json(f_header_sch)

f_msg_sch = '/Users/prakash/projects/asist-rita/git/testbed/MessageSpecs/Common_Message/common_message.json'
msg_sch = utils.read_json(f_msg_sch)


def read_predictions(dir):
    preds = {}
    for filename in os.listdir(dir):
        if filename.startswith('HSRData_Predictions_'):
            msgs = metadata_utils.read_file(dir + filename)
            preds[filename] = msgs
    return preds


def main(pred_dir):
    preds = read_predictions(pred_dir)
    for f, msgs in preds.items():
        for m in msgs:
            jsonschema.validate(m['header'], hdr_sch)
            # jsonschema.validate(m['msg'], msg_sch)
            pred_msgs = m['data']['predictions']

            for p in pred_msgs:
                pred_property = p['predicted_property']
                # print('pred property', pred_property)
                try:
                    if pred_property.startswith('M7'):
                        jsonschema.validate(p, action_sch)
                    else:
                        jsonschema.validate(p, state_sch)
                except jsonschema.exceptions.ValidationError as err:
                    # print('validator', err.validator)
                    print('for prediction', p)
                    # pprint(p)
                    print('error: ', err.schema)
                    print('message:', err.message)
                    # print('context:', err.context)
                    # print(err)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='DOLL Tool to validate study-2 prediction messages')
    parser.add_argument('pred_dir', help='Dir containing predictions in metadata format')
    args = parser.parse_args()
    main(args.pred_dir)
