from __future__ import print_function
from builtins import range
import MalmoPython
import os
import sys
import time
import random

if sys.version_info[0] == 2:
    sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)  # flush print output immediately
else:
    import functools
    print = functools.partial(print, flush=True)

# import visualize
import minecraft

## -------------------------------
##  my config
## -------------------------------
TUT_1 = False

## -------------------------------
##  Step 1 - initialize the mission
## -------------------------------

if TUT_1:
    my_mission = MalmoPython.MissionSpec()
else:
    ## specify agent initial position as in map
    mission_xml, blocks, _ = minecraft.get_mission_xml()
    my_mission = MalmoPython.MissionSpec(mission_xml, True)

    ## draw blocks (except for doors) on map
    for block in blocks: 
        x,y,z = block
        # print(x,y,z, blocks[block])
        my_mission.drawBlock( x,y,z, blocks[block])

# print(my_mission.getAsXML(True))


## -------------------------------
##  Step 2 - specify the data collection 
## -------------------------------
my_mission_record = MalmoPython.MissionRecordSpec()
recordingsDirectory = minecraft.get_recordings_directory()
my_mission_record.setDestination(recordingsDirectory)
my_mission_record.recordRewards()
my_mission_record.recordObservations()
# my_mission_record.recordCommands()
# my_mission_record.recordMP4(24,2000000)


## -------------------------------
##  Step 3 - run mission
## -------------------------------
agent_host = MalmoPython.AgentHost()
max_retries = 3
for retry in range(max_retries):
    try:
        agent_host.startMission( my_mission, my_mission_record )
        break
    except RuntimeError as e:
        if retry == max_retries - 1:
            print("Error starting mission:",e)
            exit(1)
        else:
            time.sleep(1)

# Loop until mission starts:
print("Waiting for the mission to start ", end=' ')
world_state = agent_host.getWorldState()
while not world_state.has_mission_begun:
    print(".", end="")
    time.sleep(0.1)
    world_state = agent_host.getWorldState()
    for error in world_state.errors:
        print("Error:",error.text)

print()
print("Mission running ", end=' ')


######### 3 Getmoving

### jump around a circle
# agent_host.sendCommand("turn -0.5")
# agent_host.sendCommand("move 1")
# agent_host.sendCommand("jump 1")

### dig a hole
# agent_host.sendCommand("pitch 1")
# time.sleep(1)
# agent_host.sendCommand("attack 1")


# Loop until mission ends:
while world_state.is_mission_running:
    print(".", end="")
    time.sleep(0.1)
    world_state = agent_host.getWorldState()
    for error in world_state.errors:
        print("Error:",error.text)

print()
print("Mission ended")
