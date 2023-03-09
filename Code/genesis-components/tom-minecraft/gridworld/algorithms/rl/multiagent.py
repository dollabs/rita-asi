from __future__ import print_function
from __future__ import division
from builtins import range
from past.utils import old_div
import MalmoPython
import json
import logging
import math
import os
import random
import sys
import time
import re
import uuid
from collections import namedtuple
from operator import add
if sys.version_info[0] == 2:
    sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)  # flush print output immediately
else:
    import functools
    print = functools.partial(print, flush=True)

import minecraft

HEIGHT = 30 # 30 ## height of observer agent, 30 for 24by24, 40 for 36by64

def get_standing_point():
    
    m = minecraft.MAP
    z = int(m[:m.index('by')])+6 + 0.5
    x = int(m[m.index('by')+2:m.index('_')])/2 + 0.5
    # print(x,z)
    return x,z

def replay(trajectory=None):

    # Create one agent host for parsing:
    agent_hosts = [MalmoPython.AgentHost()]

    # Parse the command-line options:
    agent_hosts[0].addOptionalFlag( "debug,d", "Display debug information.")
    agent_hosts[0].addOptionalIntArgument("agents,n", "Number of agents to use, including observer.", 2)

    try:
        agent_hosts[0].parse( sys.argv )
    except RuntimeError as e:
        print('ERROR:',e)
        print(agent_hosts[0].getUsage())
        exit(1)
    if agent_hosts[0].receivedArgument("help"):
        print(agent_hosts[0].getUsage())
        exit(0)

    DEBUG = agent_hosts[0].receivedArgument("debug")
    INTEGRATION_TEST_MODE = agent_hosts[0].receivedArgument("test")
    agents_requested = agent_hosts[0].getIntArgument("agents")
    NUM_AGENTS = max(1, agents_requested - 1) # Will be NUM_AGENTS robots running around, plus one static observer.
    NUM_MOBS = NUM_AGENTS * 2
    NUM_ITEMS = NUM_AGENTS * 2

    # Create the rest of the agent hosts - one for each robot, plus one to give a bird's-eye view:
    agent_hosts += [MalmoPython.AgentHost() for x in range(1, NUM_AGENTS + 1) ]

    # Set up debug output:
    for ah in agent_hosts:
        ah.setDebugOutput(DEBUG)    # Turn client-pool connection messages on/off.

    def safeStartMission(agent_host, my_mission, my_client_pool, my_mission_record, role, expId):
        used_attempts = 0
        max_attempts = 5
        print("Calling startMission for role", role)
        while True:
            try:
                # Attempt start:
                agent_host.startMission(my_mission, my_client_pool, my_mission_record, role, expId)
                break
            except MalmoPython.MissionException as e:
                errorCode = e.details.errorCode
                if errorCode == MalmoPython.MissionErrorCode.MISSION_SERVER_WARMING_UP:
                    print("Server not quite ready yet - waiting...")
                    time.sleep(2)
                elif errorCode == MalmoPython.MissionErrorCode.MISSION_INSUFFICIENT_CLIENTS_AVAILABLE:
                    print("Not enough available Minecraft instances running.")
                    used_attempts += 1
                    if used_attempts < max_attempts:
                        print("Will wait in case they are starting up.", max_attempts - used_attempts, "attempts left.")
                        time.sleep(2)
                elif errorCode == MalmoPython.MissionErrorCode.MISSION_SERVER_NOT_FOUND:
                    print("Server not found - has the mission with role 0 been started yet?")
                    used_attempts += 1
                    if used_attempts < max_attempts:
                        print("Will wait and retry.", max_attempts - used_attempts, "attempts left.")
                        time.sleep(2)
                else:
                    print("Other error:", e.message)
                    print("Waiting will not help here - bailing immediately.")
                    exit(1)
            if used_attempts == max_attempts:
                print("All chances used up - bailing now.")
                exit(1)
        # print("startMission called okay.")

    def safeWaitForStart(agent_hosts):
        print("Waiting for the mission to start", end=' ')
        start_flags = [False for a in agent_hosts]
        start_time = time.time()
        time_out = 120  # Allow a two minute timeout.
        while not all(start_flags) and time.time() - start_time < time_out:
            states = [a.peekWorldState() for a in agent_hosts]
            start_flags = [w.has_mission_begun for w in states]
            errors = [e for w in states for e in w.errors]
            if len(errors) > 0:
                print("Errors waiting for mission start:")
                for e in errors:
                    print(e.text)
                print("Bailing now.")
                exit(1)
            time.sleep(0.1)
            print(".", end=' ')
        if time.time() - start_time >= time_out:
            print("Timed out while waiting for mission to start - bailing.")
            exit(1)
        print()
        print("Mission has started.")

    def getXML(reset):
        # Set up the Mission XML:
        
        xml, blocks, (x,y,z) = minecraft.get_mission_xml(OBS_MODE=True)
        x,z = get_standing_point()
        # x = 12.5
        # z = 12.5
        # xml = xml.replace('Survival','Creative')
        xml = xml.replace('</Mission>',
            '''<AgentSection mode="Creative">
            <Name>Observer</Name>
            <AgentStart>
              <Placement x="'''+str(x)+'''" y="'''+str(y+HEIGHT)+'''" z="'''+str(z)+'''" pitch="90" yaw="180"/>
            </AgentStart>
            <AgentHandlers>
              <ContinuousMovementCommands turnSpeedDegs="360"/>
              <MissionQuitCommands/>
              <!--VideoProducer>
                <Width>640</Width>
                <Height>640</Height>
              </VideoProducer-->
            </AgentHandlers>
          </AgentSection>
        </Mission>''')

        # new_blocks = {}
        # for key,value in blocks.items():
        #     if value == 'iron_block':
        #         new_blocks[key] = value
        # print(len(blocks), len(new_blocks))

        return xml, blocks, (x,y,z)

    def get_heading(yaw):
        if (yaw <= 45 and yaw > -45) or (yaw > 270 + 45) or (yaw < - 270 - 45):
            heading = 270
        elif (yaw <= 90 + 45 and yaw > 45) or (yaw <= -45 - 180 and yaw > -45 -270):
            heading = 180
        elif (yaw <= 180 + 45 and yaw > 90 + 45) or (yaw <= -45 -90 and yaw > -45 -180):
            heading = 90
        else:
            heading = 0
        return heading

    def get_xz(c):
        x = math.ceil(float(c[c.index('(')+1:c.index(',')]))
        z = math.ceil(float(c[c.index(',')+1:c.index(')')]))
        return x,z

    def should_attack(ob):
        if 'LineOfSight' in ob:
            look_at = ob['LineOfSight']['type']
            dist = ob['LineOfSight']['distance']
            # print(dist)
            if (dist < 4 and (look_at == 'chest' or look_at == 'ender_chest')) or (dist < 4 and (look_at == 'dark_oak_door' or look_at == 'dirt')):
                return True
        return False

    # Set up a client pool.
    # IMPORTANT: If ANY of the clients will be on a different machine, then you MUST
    # make sure that any client which can be the server has an IP address that is
    # reachable from other machines - ie DO NOT SIMPLY USE 127.0.0.1!!!!
    # The IP address used in the client pool will be broadcast to other agents who
    # are attempting to find the server - so this will fail for any agents on a
    # different machine.
    client_pool = MalmoPython.ClientPool()
    for x in range(10000, 10000 + NUM_AGENTS + 1):
        client_pool.add( MalmoPython.ClientInfo('127.0.0.1', x) )

    num_missions = 1 if INTEGRATION_TEST_MODE else 1

    for mission_no in range(1, num_missions+1):
        print("Running mission #" + str(mission_no))
        # Create mission xml - use forcereset if this is the first mission.
        xml, blocks, (x,y,z) = getXML("true" if mission_no == 1 else "false")
        my_mission = MalmoPython.MissionSpec(xml,True)
        # for i in range(-10,11):
        #     for j in range(-10,11):
        #         for k in range(2):
        my_mission.drawBlock( int(x),int(y+HEIGHT-2),int(z), 'stained_glass_pane')
        my_mission.drawBlock( int(x),int(y+HEIGHT-3),int(z), 'stained_glass_pane')

        # Generate an experiment ID for this mission.
        # This is used to make sure the right clients join the right servers -
        # if the experiment IDs don't match, the startMission request will be rejected.
        # In practice, if the client pool is only being used by one researcher, there
        # should be little danger of clients joining the wrong experiments, so a static
        # ID would probably suffice, though changing the ID on each mission also catches
        # potential problems with clients and servers getting out of step.

        # Note that, in this sample, the same process is responsible for all calls to startMission,
        # so passing the experiment ID like this is a simple matter. If the agentHosts are distributed
        # across different threads, processes, or machines, a different approach will be required.
        # (Eg generate the IDs procedurally, in a way that is guaranteed to produce the same results
        # for each agentHost independently.)
        experimentID = str(uuid.uuid4())

        # my_mission_record = MalmoPython.MissionRecordSpec()
        # recordingsDirectory = minecraft.get_recordings_directory()
        # my_mission_record.setDestination(recordingsDirectory)
        # my_mission_record.recordMP4(24,2000000)
        for i in range(len(agent_hosts)):
            safeStartMission(agent_hosts[i], my_mission, client_pool, MalmoPython.MissionRecordSpec(), i, experimentID)

        safeWaitForStart(agent_hosts)

        SLEEP = 1

        ## if replay or RL
        if trajectory != None:

            key = 1
            ah = agent_hosts[0]
            world_state = ah.getWorldState()
            while world_state.is_mission_running:
                time.sleep(0.1)
                world_state = ah.getWorldState()

                ## replay one step of the trajectory
                if world_state.number_of_observations_since_last_state > 0:

                    ## current stats
                    msg = world_state.observations[-1].text
                    ob = json.loads(msg)
                    x_p,z_p = get_xz(ob['cell'])
                    yaw_p = get_heading(int(float(ob['Yaw'])))

                    ## goal stats
                    x,z = get_xz(trajectory[str(key)]['cell'])
                    yaw = get_heading(trajectory[str(key)]['yaw'])
                    ATTACK = should_attack(ob)

                    # print('\n',x_p,z_p,yaw_p)
                    # print(x,z,yaw)

                    if ATTACK: 
                        ah.sendCommand("attack 1")
                        ah.sendCommand("attack 0")

                    ## turn!
                    yaw_diff = yaw - yaw_p
                    if yaw_diff == 90 or yaw_diff == -270:
                        ah.sendCommand("turn -1")
                    elif yaw_diff == -90 or yaw_diff == 270:
                        ah.sendCommand("turn 1")

                    if ATTACK: 
                        ah.sendCommand("attack 1")
                        ah.sendCommand("attack 0")
                    
                    ## move!
                    if yaw == 0:
                        if x - x_p >= 1: ah.sendCommand("move 1")
                        if x_p - x >= 1: ah.sendCommand("move -1")
                        if z - z_p >= 1: ah.sendCommand("strafe 1")
                        if z_p - z >= 1: ah.sendCommand("strafe -1")

                    elif yaw == 90:
                        if z - z_p >= 1: ah.sendCommand("move -1")
                        if z_p - z >= 1: ah.sendCommand("move 1")
                        if x - x_p >= 1: ah.sendCommand("strafe 1")
                        if x_p - x >= 1: ah.sendCommand("strafe -1")

                    elif yaw == 180:
                        if x - x_p >= 1: ah.sendCommand("move -1")
                        if x_p - x >= 1: ah.sendCommand("move 1")
                        if z - z_p >= 1: ah.sendCommand("strafe -1")
                        if z_p - z >= 1: ah.sendCommand("strafe 1")

                    elif yaw == 270:
                        if z - z_p >=1: ah.sendCommand("move 1")
                        if z_p - z >= 1: ah.sendCommand("move -1")
                        if x - x_p >= 1: ah.sendCommand("strafe -1")
                        if x_p - x >= 1: ah.sendCommand("strafe 1")

                    if ATTACK: 
                        ah.sendCommand("attack 1")
                        ah.sendCommand("attack 0")

                    ## check and reset
                    time.sleep(0.01)
                    ah.sendCommand("strafe 0")
                    ah.sendCommand("move 0")
                    ah.sendCommand("turn 0")
                    ah.sendCommand("attack 0")

                    # ## draw blocks that have been distroyed
                    # for block in blocks: 
                    #     x,y,z = block
                    #     my_mission.drawBlock( x,y,z, blocks[block])

                    ## check updated stats
                    # world_state = ah.getWorldState()
                    # msg = world_state.observations[-1].text
                    # ob = json.loads(msg)
                    # x_p,z_p = get_xz(ob['cell'])
                    # yaw_p = get_heading(int(float(ob['Yaw'])))
                    # print(x_p,z_p,yaw_p)

                    key += 1
                    if str(key) not in trajectory:
                        break

            agent_hosts[0].sendCommand("quit")
            print("\nMission ended")

        ## normal game play mode
        else:
            time.sleep(1)
            running = True
            timed_out = False

            while not timed_out:
                for i in range(NUM_AGENTS):
                    ah = agent_hosts[i]
                    world_state = ah.getWorldState()

                    if world_state.is_mission_running == False:
                        timed_out = True
                time.sleep(0.05)
            print()

            # if not timed_out:
                # All agents except the watcher have died.
                # We could wait for the mission to time out, but it's quicker
                # to make the watcher quit manually:
                # agent_hosts[-1].sendCommand("quit")

            print("Waiting for mission to end ", end=' ')

            # Mission should have ended already, but we want to wait until all the various agent hosts
            # have had a chance to respond to their mission ended message.
            hasEnded = False
            while not hasEnded:
                hasEnded = True # assume all good
                print(".", end="")
                time.sleep(0.1)
                for ah in agent_hosts:
                    world_state = ah.getWorldState()
                    if world_state.is_mission_running:
                        hasEnded = False # all not good

            time.sleep(2)

if __name__ == '__main__':
    replay()
