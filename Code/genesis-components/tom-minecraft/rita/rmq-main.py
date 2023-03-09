#!/usr/bin/env python

# Copyright 2019 Dynamic Object Language Labs Inc.
#
# This software is licensed under the terms of the
# Apache License, Version 2.0 which can be found in
# the file LICENSE at the root of this distribution.

import sys
import time
import argparse
import rmq
import threading
from pprint import pprint
import copy
import math

sys.path.insert(1, '../gridworld')
import raycasting

## Note Work in Progress. Consider as example and is not fully functional. 5/18/2020

# Global
rabbit = None
blocks_file = '../world-builder/outputs/blocks_in_building.json'
x_last = 0
z_last = 0
count = 0
count_changed = 0

def start_rita(msg):
    pass

def shutdown_rita(msg):
    rabbit.done = True
    rabbit.close()

def position_changed(x, z, x_last, z_last):
    # if (math.floor(x) != math.floor(x_last)): print('       x changed')
    # if (math.floor(z) != math.floor(z_last)): print('       z changed')
    return (math.floor(x) != math.floor(x_last)) or (math.floor(z) != math.floor(z_last))

def dispatch_fn(msg, routing_key):
    # msg is python data structure

    global x_last
    global z_last
    global count
    global count_changed

    if routing_key == 'startup-rita':
        pprint(msg)
        start_rita(msg)

    # elif routing_key == 'shutdown-rita':
    #     shutdown_rita(msg)

    elif routing_key == 'testbed-message':
        count += 1

        time = msg['testbed-message']['header']['timestamp']
        data = msg['testbed-message']['data']
        x = data['x']
        z = data['z']

        # -2192, -2142, 144, 191, 28, 30 if
        if data['name'] == 'Player396':
            if position_changed(x, z, x_last, z_last) and x > -2192 and x < -2142 and z > 144 and z < 191:
                count_changed += 1
                # print(count_changed, count)

                position = (math.floor(x),math.floor(z))
                yaw = data['yaw']

                print('       ', time, position, yaw)
                blocks_observed = raycasting.test_3(position, yaw, blocks_file)
                send_obs(blocks_observed)

                # send_messages() ## processing msg

                print()

            x_last = x
            z_last = z

    # This function is called on RMQ threads. So we should not be processing messages in this callback function
    # and returning from this function asap to enable future incoming messages


def send_obs(msg):
    def inner_fn():
        # ms = copy.deepcopy(msg)
        ms = {'timestamp': time.time() * 1000,
              'routing-key': 'raycasting',
              'app-id': 'Genesis python test App',
              'mission-id': 'mission id - 1'}
        ms['raycasting'] = 'hello'
        rabbit.send_message(ms, 'raycasting')

    th = threading.Thread(target=inner_fn)
    th.setDaemon(True)
    th.start()


def send_messages():
    def inner_fn():
        time.sleep(2)
        ms = {'timestamp': time.time() * 1000,
              'routing-key': 'startup-rita',
              'app-id': 'test App',
              'mission-id': 'mission id - 1'}
        rabbit.send_message(ms, 'startup-rita')
        time.sleep(2)
        ms['timestamp'] = time.time() * 1000
        ms['routing-key'] = 'shutdown-rita'
        rabbit.send_message(ms, 'shutdown-rita')
        time.sleep(1)

    th = threading.Thread(target=inner_fn)
    th.setDaemon(True)
    th.start()

def test_rita_rmq():
    # Trivial function to setup subscriptions and publish messages
    global rabbit
    rabbit = rmq.Rmq('rita')
    rabbit.subscribe(['startup-rita', 'shutdown-rita', 'testbed-message'])
    # send_messages()
    rabbit.wait_for_messages(dispatch_fn)  # Blocking function call
    # When we are done
    rabbit.done = True
    rabbit.close()
    print('Done')


if __name__ == "__main__":
    print("rmq-main.py as script")
    parser = argparse.ArgumentParser(description='Plant Sim (Python)')

    parser.add_argument('--host', default='localhost', help='RMQ host')
    parser.add_argument('-p', '--port', default=5672, help='RMQ Port', type=int)
    parser.add_argument('-e', '--exchange', default='rita', help='RMQ Exchange')

    # parser.add_argument('--fail', dest='fail', action='store_true', help='Will fail all activities')
    args = parser.parse_args()
    test_rita_rmq()
    # sys.exit(main(args))
