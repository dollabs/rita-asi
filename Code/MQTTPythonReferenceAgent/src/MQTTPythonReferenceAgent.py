from builtins import range
import os
import sys
import time
import io
import datetime
import json
from datetime import datetime

running = False

################################################################################
# Connection callback

def onConnection(isConnected, rc):
    print('Connected? {0} ({1})'.format(isConnected, rc))

# Message callback
def onMessage(message):
    print('Yeo got onMessage')
    try:
        global running

        print(f'onMessage callback: {message}')
        jsonDict = message.jsondata
        messageType = jsonDict["message_type"]
        print(f'message type: {messageType}')
        if (messageType == "control"):
            msg = jsonDict["msg"]
            command = msg["command"]
            if command == "init":
              print(f'Received init control message')
            elif command == "start":
              print(f'Received start control message')
              running = True
            elif command == "stop":
              print(f'Received stop control message')
              running = False

    except Exception as ex:
        print(ex)
        print('RX Error, topic = {0}'.format(message.key))

# Add headers
def addHeader(jsonDict: dict):
    jsonDict["timestamp"] = datetime.now().isoformat()
    jsonDict["message_type"] = "data"
    jsonDict["version"] = "0.1"

# SETTING UP MQTT CLIENT
from RawConnection import *

# Connection
p1 = RawConnection("Test-Agent")

# Set up callbacks
p1.onConnectionStateChange = onConnection
p1.onMessage = onMessage

# Connect and subscribe to messages on a certain topic
p1.connect()
p1.subscribe("observation/state")

# Stay running for a certain amount of time
count = 0

while True:
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

