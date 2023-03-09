#!/usr/bin/env python

# Copyright 2019 Dynamic Object Language Labs Inc.
#
# This software is licensed under the terms of the
# Apache License, Version 2.0 which can be found in
# the file LICENSE at the root of this distribution.


# A trivial example of a plant that executes any command for 2 seconds.
# This is for trivial testing only. You may see exceptions like this one
# # Expect to see pika.exceptions.ConnectionClosed: (505, 'UNEXPECTED_FRAME - expected content header for class 60, got non content header frame instead')
import sys
import time
import argparse
import rmq
import threading

## Note Work in Progress. Consider as example and is not fully functional. 5/18/2020

# Global
rabbit = None
message_buffer = []  # not sure if this buffer is thread safe


def start_rita(msg):
    pass


def shutdown_rita(msg):
    rabbit.done = True
    rabbit.close()


def do_some_message_processing(msg):
    global message_buffer
    message_buffer.append(msg)
    if len(message_buffer) == 3:
        tmp_buffer = message_buffer
        message_buffer = []

        def inner_fn():
            time.sleep(2)
            # pprint(tmp_buffer)
            print('processing', len(tmp_buffer))
            ms = {'timestamp': time.time() * 1000,
                  'routing-key': 'some-routing-key',
                  'app-id': 'test App',
                  'mission-id': 'mission id - 1'}
            rabbit.send_message(ms, 'some-routing-key')

        th = threading.Thread(target=inner_fn)
        th.setDaemon(True)
        th.start()
    else:
        print('not enough messages', len(message_buffer))


def dispatch_fn(msg, routing_key):
    # msg is python data structure
    # print(routing_key)
    # pprint(msg)
    if routing_key == 'startup-rita':
        start_rita(msg)
    elif routing_key == 'shutdown-rita':
        shutdown_rita(msg)
    elif routing_key == 'testbed-message':
        do_some_message_processing(msg)
    # This function is called on RMQ threads. So we should not be processing messages in this callback function
    # and returning from this function asap to enable future incoming messages


def send_messages():
    def inner_fn():
        time.sleep(2)
        ms = {'timestamp': time.time() * 1000,
              'routing-key': 'startup-rita',
              'app-id': 'test App',
              'mission-id': 'mission id - 1'}
        rabbit.send_message(ms, 'startup-rita')
        time.sleep(2)
        # ms['timestamp'] = time.time() * 1000
        # ms['routing-key'] = 'shutdown-rita'
        # rabbit.send_message(ms, 'shutdown-rita')
        # time.sleep(1)


# def main(args):
#     global plant_g, fail_all_activities
#
#     if args.fail:
#         print 'Args fail', args.fail
#         fail_all_activities = True
#
#     plantid = args.plantid
#     if plantid == '':
#         plantid = '#'
#
#     local_plant = plant.Plant(plantid, args.exchange, args.host, args.port)
#     plant_g = local_plant
#
#     local_plant.wait_for_messages(dispatch_func)
#     local_plant.close()
#     plant_g = None


if __name__ == "__main__":
    print("rmq-main.py as script")
    parser = argparse.ArgumentParser(description='Plant Sim (Python)')

    parser.add_argument('--host', default='localhost', help='RMQ host')
    parser.add_argument('-p', '--port', default=5672, help='RMQ Port', type=int)
    parser.add_argument('-e', '--exchange', default='rita', help='RMQ Exchange')

    # parser.add_argument('--fail', dest='fail', action='store_true', help='Will fail all activities')
    args = parser.parse_args()
    # sys.exit(main(args))
