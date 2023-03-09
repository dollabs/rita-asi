from builtins import range
import os
import sys
import time
import io
import datetime
import json
from datetime import datetime

import logging, threading, pika

running = False

################################################################################
# Connection callback

def onConnection(isConnected, rc):
    print('Connected? {0} ({1})'.format(isConnected, rc))

# Message callback
def onMessage(message):
    try:
        global running
        print('################')
        print(f'onMessage callback: {message}')
        jsonDict = message.jsondata
        #messageType = jsonDict["message_type"]
        #print(f'....message type: {messageType}')

        # if (messageType == "control"):
        #     msg = jsonDict["msg"]
        #     command = msg["command"]
        #     if command == "init":
        #       print(f'Received init control message')
        #     elif command == "start":
        #       print(f'Received start control message')
        #       running = True
        #     elif command == "stop":
        #       print(f'Received stop control message')
        #       running = False
        # For now, let's publish all messages to RMQ
        routingkey = "testbed-message"
        dictForRMQ = {}
        dictForRMQ['timestamp'] = int(time.time() * 1000)
        dictForRMQ['routing-key'] = routingkey
        dictForRMQ['app-id'] = "TestbedBusInterface"
        # The real value for this should come from one of the testbed control messages
        dictForRMQ['mission-id'] = "MissionIDPlaceholder007"
        dictForRMQ['testbed-message'] = message.jsondata
        e1.channel.basic_publish(exchange=e1.key, routing_key=routingkey, body=json.dumps(dictForRMQ))

    except Exception as ex:
        print('-------')
        print(ex)
        print('RX Error, topic = {0}'.format(message.key))
        print()

# Add headers
def addHeader(jsonDict: dict):
    jsonDict["timestamp"] = datetime.now().isoformat()
    jsonDict["message_type"] = "data"
    jsonDict["version"] = "0.1"


class RMQEchoer(object):
    def __init__(self):
        self.log = logging.getLogger('rita.' + self.__class__.__name__)

    def initialize(self, options):
        # Temporarily set to LispMachine for RITA testing
        self.host = "192.168.11.100"
        self.connection =  pika.BlockingConnection(pika.ConnectionParameters(host=self.host))
        self.key = "rita"
        self.channel = self.connection.channel()
        self.channel.exchange_declare(self.key, exchange_type="topic")
        self.running = False


# SETTING UP MQTT CLIENT
from RawConnection import *

# MQTT Connection
p1 = RawConnection("Test-Agent")
e1 = RMQEchoer()

# Set up callbacks
p1.onConnectionStateChange = onConnection
p1.onMessage = onMessage

# Connect and subscribe to messages on a certain topic
p1.connect()
p1.subscribe("observation/state")

e1.initialize(None)

# Stay running for a certain amount of time
count = 0

while True:

  if count % 25 == 0:
      print(f"Count = {count}")

  if running:
    # Send a test message
    jsonDict = {}
    addHeader(jsonDict)

    msg = {}
    msg["count"] = count
    jsonDict["msg"] = msg

    p1.publish(RawMessage("malmo/ReferenceAgent", jsondata=jsonDict))

  count = count + 1
  time.sleep(1)

# ### Issue
# Count = 475
# Count = 476
# ################
# onMessage callback: RawMessage(key='observation/state',payload=b'{"header": {"timestamp": "2020-02-12T21:10:00.090470", "message_type": "observation", "version": "0.1"}, "msg": {"timestamp": "2020-02-12T21:10:00.090470", "source": "simulator", "sub_type": "state", "version": "0.1"}, "data": {"name": "Ed", "world_time": 12000, "total_time": 6686949, "entity_type": "human", "yaw": 0.0, "x": -2193.5, "y": 23.0, "z": 194.0, "pitch": 0.0, "id": "b5730d95-d519-3d6d-8764-250de4096d2b", "motion_x": 0.0, "motion_y": -0.0784000015258789, "motion_z": 0.0, "life": 20.0}}')
# -------
# Stream connection lost: ConnectionResetError(104, 'Connection reset by peer')
# RX Error, topic = observation/state

# ################
# onMessage callback: RawMessage(key='observation/state',payload=b'{"header": {"timestamp": "2020-02-12T21:10:00.140931", "message_type": "observation", "version": "0.1"}, "msg": {"timestamp": "2020-02-12T21:10:00.140931", "source": "simulator", "sub_type": "state", "version": "0.1"}, "data": {"name": "Ed", "world_time": 12000, "total_time": 6686950, "entity_type": "human", "yaw": 0.0, "x": -2193.5, "y": 23.0, "z": 194.0, "pitch": 0.0, "id": "b5730d95-d519-3d6d-8764-250de4096d2b", "motion_x": 0.0, "motion_y": -0.0784000015258789, "motion_z": 0.0, "life": 20.0}}')
# -------
# Channel is closed.
# RX Error, topic = observation/state

# Stack Over flow discussion
# https://stackoverflow.com/questions/54003433/rabbitmq-pika-connection-reset-1-connectionreseterror104-connection-rese

# Potential resolution
# https://stackoverflow.com/questions/54003433/rabbitmq-pika-connection-reset-1-connectionreseterror104-connection-rese
