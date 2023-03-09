import pandas as pd
import numpy as np
import turtle
import random
import time
import pprint
import networkx as nx
import matplotlib.pyplot as plt
import json

import imageio
import copy
import math
from tqdm import tqdm
from scipy.special import softmax
import operator

import visualize
import mapreader
from mcts import MonteCarloAgent
import dfs
import utils

JUST_TRAJECTORY = False
PRINT_CONSOLE = False
SOFT_MAX_POLICY = False


def roomlevel_value_iteration(env, V):
    """ performs value iteration on the room level
        state space is the number of rooms
        action space is the neighboring rooms of a room
    """

    R = {}
    def get_R(s):
        if s not in R:
            R[s] = env.roomlevel_R(s)
        return R[s]

    epsilon = env.player["epsilon"]
    gamma = env.player["roomlevel_gamma"]

    # helper function
    def best_action(s):
        V_max = -np.inf
        for a in env.roomlevel_actions[s]:
            s_p = env.roomlevel_T(s, a)
            V_temp = get_R(s) + gamma*V[s_p]
            V_max = max(V_max,V_temp)
        return V_max

    ## ------------------------------
    # Step 1 -- initialize V to be 0 ----- xxx no, initialize to that of last step
    ## ------------------------------
    if visualize.RANDOM_INITIALIZE_V:
        V = np.zeros((len(env.roomlevel_states), ))

    ## ------------------------------
    # Step 2 -- apply Bellman Equation until converge
    ## ------------------------------
    iter = 0
    while True:
        iter +=1
        delta = 0
        for s in env.roomlevel_states:
            v = V[s]
            V[s] = best_action(s)
            delta = max(delta, abs(v-V[s]))

        # termination condition
        if delta < epsilon: break


    ## ------------------------------
    # Step 3 -- extract greedy policy
    ## ------------------------------
    pi, Q_sa = extract_Qsa_pi_from_V1(env, V)  ## temp solution for inverse to retrieve pi and Q

    w = open(f'recordings/hierarchical-{env.step}-R-room.txt', "w")
    w.write(str(R))
    w.close()

    return pi, Q_sa, V, iter

def extract_Qsa_pi_from_V1(env, V):
    pi = np.zeros((len(env.roomlevel_states), len(env.roomlevel_states)))
    Q_sa = np.zeros((len(env.roomlevel_states), len(env.roomlevel_states)))
    # print(len(env.roomlevel_states), env.roomlevel_states)
    for s in env.roomlevel_states:
        Q = np.ones((len(env.roomlevel_states),)) * -10000
        for a in env.roomlevel_actions[s]:
            s_p = env.roomlevel_T(s, a)
            Q[a] = env.roomlevel_R(s) + env.player["roomlevel_gamma"] * V[s_p]
            Q_sa[s, a] = Q[a]

        ## -- highest state-action value
        Q_max = np.amax(Q)

        ## deterministic policy of the best room
        pi[s, :] = np.abs(Q - Q_max) < 10 ** -3
        pi[s, :] /= np.sum(pi[s, :])
    return pi, Q_sa

def tilelevel_value_iteration(env,V2,roomlevel_tiles):
    """     performs value iteration on tiles in the curret room and tiles adjacent to the room
                e.g., tiles [12,13,14,15] corresponds to states [0,1,2,3]
                    tile = states[state], state = tiles.index(tile)
            and uses state action value to extract greedy policy for gridworld
    """

    R = {}
    def get_R(s,a):
        if (s,a) not in R:
            R[(s,a)] = env.R(s,a)
        return R[(s,a)]

    T = {}
    def get_T(s,a):
        if (s,a) not in T:
            T[(s,a)] = env.T(s,a)
        return T[(s,a)]

    epsilon = env.player["epsilon"]
    gamma = env.player["tilelevel_gamma"]

    states = tuple([(tile,head) for tile in roomlevel_tiles for head in (0,90,180,270)])

    # helper function
    def best_action(s):
        V_max = -np.inf
        for a in env.actions:
            s_p = get_T(s, a)
            tile, head = s_p
            if not tile == 'wall' and tile in roomlevel_tiles:
                V_max = max(V_max, get_R(s_p,a) + gamma*V[inV[tile], head//90])
        return V_max

    ## ------------------------------
    # Step 1 -- initialize V to be 0
    ## ------------------------------
    V = np.zeros((len(roomlevel_tiles), 4))
    inV = {}
    sim_value = None
    for tile in roomlevel_tiles:
        # if V2[tile].all() == 0 and V2.sum() != 0: ## initialize to similar values
            # if sim_value == None:
            #     sim_value = np.true_divide(V2.sum(),(V2!=0).sum())
            #     print('initialized to', sim_value)
            # V[roomlevel_tiles.index(tile)] = sim_value * np.ones(4)
        #     V[roomlevel_tiles.index(tile)] = 7 * np.ones(4)
        # else:
        inV[tile] = roomlevel_tiles.index(tile)
        V[inV[tile]] = V2[tile]

    ## ------------------------------
    # Step 2 -- apply Bellman Equation until converge
    ## ------------------------------
    iter = 0
    while True:
        iter += 1
        delta = 0
        # start = time.time()
        for s in states:
            tile, head = s
            head = head//90
            tile = inV[tile]
            v = V[tile, head]
            V[tile, head] = best_action(s) # helper function used
            delta = max(delta, abs(v-V[tile, head]))
        # print(len(states), round(time.time()-start, 2), V[tile, head])

        # termination condition
        if delta < epsilon: break

    ## ------------------------------
    # Step 3 -- extract greedy policy
    ## ------------------------------
    pi = np.zeros((len(roomlevel_tiles), 4, len(env.actions)))
    Q_sa = np.zeros((len(roomlevel_tiles), 4, len(env.actions)))
    for s in states:
        Q = np.zeros((len(env.actions),))
        for a in range(len(env.actions)):
            s_p = get_T(s, env.actions[a])
            tile, head = s_p
            if not s_p[0] == 'wall' and tile in roomlevel_tiles:
                Q[a] = get_R(s_p, env.actions[a]) + gamma * V[inV[tile], head // 90]

        tile, head = s
        tile = inV[tile]
        Q_sa[tile, head // 90] = Q

        ## deterministic
        if False:
            ## highest state-action value
            Q_max = np.amax(Q)

            ## collect all actions that has Q value very close to Q_max
            # pi[s[0], s[1]//90, :] = softmax((Q * (np.abs(Q - Q_max) < 10**-2) / 0.01).astype(int))
            tile = roomlevel_tiles.index(s[0])
            pi[tile, s[1] // 90, :] = np.abs(Q - Q_max) < 10 ** -3
            pi[tile, s[1] // 90, :] /= np.sum(pi[tile, s[1] // 90, :])

        ## softmax
        else:
            pi[tile, head // 90,:] = softmax(Q/ 0.01)

    ## translate the new state space of roomlevel_tiles to the old state space of all tiles
    pi_old = np.zeros((env.ntiles, 4, len(env.actions)))
    Q_sa_old = np.zeros((env.ntiles, 4, len(env.actions)))
    V2 = np.zeros((env.ntiles, 4))  ## didn't reuse V2
    for tile in roomlevel_tiles:
        tile2 = inV[tile]
        pi_old[tile] = pi[tile2]
        Q_sa_normalized = Q_sa[tile2]
        Q_sa_normalized = Q_sa_normalized / np.linalg.norm(Q_sa_normalized)
        Q_sa_old[tile] = Q_sa_normalized
        V2[tile] = V[tile2]

    # print('converged to', round(np.true_divide(V2.sum(),(V2!=0).sum()),3))

    w = open(f'recordings/hierarchical-{env.step}-T-tile.txt', "w")
    w.write(str(T))
    w.close()

    w = open(f'recordings/hierarchical-{env.step}-R-tile.txt', "w")
    w.write(str(R))
    w.close()

    return pi_old, Q_sa_old, V2, iter

def show(obj, title):
    """ replacement of print() """
    if len(str(obj)) <= 20:
        print('\n',title+':',obj)
    else:
        print('\n',title+': \n',obj)

def dist(env, s1, s2):
    x1 = env.tilesummary[s1]['row']
    y1 = env.tilesummary[s1]['col']
    x2 = env.tilesummary[s2]['row']
    y2 = env.tilesummary[s2]['col']
    return math.sqrt((x2 - x1)**2 + (y2 - y1)**2)

def V1_to_roomlevelV(env, V1):
    roomlevel_V = copy.deepcopy(V1)

    ## avoid visiting rooms that one has been to
    for room in env.rooms.keys():
        if env.fully_explored(room) and room != env.tiles2room[env._pos_agent[0]]:
            roomlevel_V[room] = 0

    ## normalize
    roomlevel_V_sum = sum(roomlevel_V)  ## use numpy

    for room in env.rooms.keys():
        roomlevel_V[room] = roomlevel_V[room] / roomlevel_V_sum  # round(roomlevel_V[room]/roomlevel_V_sum, 1)

    return roomlevel_V, roomlevel_V_sum

def policy_and_value(env, V1, V2, s, TXT=None, normalize=False):

    DEBUG = PRINT_CONSOLE
    start_VI = time.time()

    ## ---- use VI to compute the value and policy of all rooms
    pi, Qsa, V1, room_iter = roomlevel_value_iteration(env, V1) ## reuse V1
    for room in env.rooms:
        env.rooms[room]['roomlevel_pi'] = pi[room]
        env.rooms[room]['roomlevel_Qsa'] = Qsa[room]
        env.rooms[room]['roomlevel_V'] = V1[room]
        env.rooms[room]['tilelevel_pi'] = {}
        env.rooms[room]['tilelevel_Qsa'] = {}
        env.rooms[room]['tilelevel_V'] = {}

    room_level_time = round(time.time() - start_VI,3)
    TXT = visualize.log('... finished hierarchical VI on room level ' + str(room_iter)+' in ' + str(time.time() - start_VI) +' seconds',TXT)
    # if DEBUG: print('... finished hierarchical VI on room level ' + str(room_iter)+' in ' + str(time.time() - start_VI) +' seconds')

    ## -------- to be printed on topology tree
    current_room = env.tiles2room[s[0]]
    next_room = choose_action(pi[current_room])
    roomlevel_V, roomlevel_V_sum = V1_to_roomlevelV(env, V1)

    ## for tile level value reassignment for all rooms and their neighbors
    roomlevel_tiles = copy.deepcopy(env.rooms[current_room]['tiles'])
    room = current_room

    env.rooms[room]['neighbors_temp_reward'] = {}
    for neighbor in env.rooms[room]['neighbors']:

        ## choose the door location to be the tile closest to me
        adjacents = env.rooms[room]['neighbors'][neighbor]
        mydoor, yourdoor, heading = adjacents[0]
        for adjacent in adjacents:
            mydoor_t, yourdoor_t, heading_t = adjacent
            if dist(env,mydoor_t,s[0]) < dist(env,mydoor,s[0]):
                mydoor, yourdoor, heading = adjacent

        roomlevel_tiles.append(yourdoor)

        ## reassign roomlevel value to one tile
        temp = Qsa[(room, neighbor)] / roomlevel_V_sum
        env.temperary_rewards[yourdoor] = env.tilesummary[yourdoor]['reward']
        env.tilesummary[yourdoor]['reward'] += temp
        env.rooms[room]['neighbors_temp_reward'][neighbor] = (yourdoor, temp)

        ## give extra reward to the tile leading to the next room but far away
        if neighbor == next_room and dist(env,mydoor,s[0]) > 5:
            env.tilesummary[yourdoor]['reward'] *= env.player['goal_boost_factor']
            temp = env.tilesummary[yourdoor]['reward'] - env.temperary_rewards[yourdoor]
            env.rooms[room]['neighbors_temp_reward'][neighbor] = (yourdoor, temp)

        ## in case you see a victim in the neighboring room, just go there
        if neighbor in env.rooms_to_plan:
            if visualize.LOG: print('go consider room',neighbor)
            for tile in env.rooms[neighbor]['tiles']:
                if tile not in roomlevel_tiles:
                    roomlevel_tiles.append(tile)

    ## record down which neighboring rooms contain victims
    for room in env.rooms.keys():
        if room == current_room:
            env.rooms[room]['rooms_to_plan'] = env.rooms_to_plan
        else:
            env.rooms[room]['rooms_to_plan'] = []

    start_VI = time.time()

    if visualize.TILE_LEVEL_DFS:

        ## for chooing a goal for dfs
        max_tile = -1
        max_tile_discounted = -np.inf
        for tile in env.tilesummary_truth.keys():
            tile_discounted = env.player["tilelevel_gamma"] ** dist(env, tile, s[0]) * env.R(tile)
            if tile_discounted > max_tile_discounted and tile != s[0]:
                max_tile_discounted = tile_discounted
                max_tile = tile

        ## initalize for dfs
        s_pos = (env.tilesummary[s[0]]['row'],env.tilesummary[s[0]]['col'])
        s_door = (env.tilesummary[max_tile]['row'],env.tilesummary[max_tile]['col'])
        if PRINT_CONSOLE: print('!!!! chosen',max_tile, (s_pos,s[1]), s_door)
        states, actions = dfs.dfs(env.MAP, (s_pos,s[1]), s_door)
        return actions, roomlevel_V

    elif visualize.ROOM_LEVEL_MCTS:
        agent = MonteCarloAgent(env)
        pi = agent.get_action()
        TXT = visualize.log('... finished MCTS on room level in ' + str(time.time() - start_VI) + ' seconds',TXT)
        return pi, roomlevel_V

    else:
        pi2, Qsa2, V2, tile_iter = tilelevel_value_iteration(env,V2,roomlevel_tiles)
        TXT = visualize.log('... finished hierarchical VI on tiles level ' + str(tile_iter) + ' in ' + str(time.time() - start_VI) + ' seconds',TXT)
        if DEBUG: print('... finished HVI | room level', room_iter, 'itr', room_level_time, 'sec | tile level', tile_iter, 'itrs', round(time.time() - start_VI,3))
        # if DEBUG: print('... finished hierarchical VI on tile level ' + str(room_iter)+' in ' + str(time.time() - start_VI) +' seconds')

        for tile in roomlevel_tiles:
            env.rooms[current_room]['tilelevel_pi'][tile] = pi2[tile]
            env.rooms[current_room]['tilelevel_Qsa'][tile] = Qsa2[tile]
            env.rooms[current_room]['tilelevel_V'][tile] = V2[tile]

        for other_room in env.rooms:
            if other_room != current_room:
                env.rooms[other_room]['roomlevel_pi'] = None
                env.rooms[other_room]['roomlevel_Qsa'] = None
                env.rooms[other_room]['roomlevel_V'] = None
                env.rooms[other_room]['tilelevel_pi'] = None
                env.rooms[other_room]['tilelevel_Qsa'] = None
                env.rooms[other_room]['tilelevel_V'] = None

        if normalize:
            Qsa = np.true_divide(Qsa, np.max(Qsa))
            Qsa2 = np.true_divide(Qsa2, np.max(Qsa2))
            # V1 = np.true_divide(V1, np.max(V1))
            # V2 = np.true_divide(V2, np.max(V2))

        return (pi, Qsa, roomlevel_V, V1), (pi2, Qsa2, V2)

def draw_planning_window(env):

    # initialize observation board and score board writer turtle
    writer = turtle.Turtle()
    writer.hideturtle()
    writer.up()

    scores = turtle.Turtle()
    scores.hideturtle()
    scores.up()

    titles = update_planning_window(env)

    return writer, scores, titles

def update_planning_window(env, titles = None):

    # initialize writer turtle
    font = ("Courier", 18, 'normal', 'bold', 'underline')

    if titles != None:
        titles.clear()
    else:
        titles = turtle.Turtle()

    titles.hideturtle()
    titles.up()
    titles.goto(-420, 260)
    titles.write('Room Values', font=font)
    titles.goto(-420, 50)
    titles.write('Expected Tile Rewards', font=font)
    titles.goto(-420, -110)
    if visualize.ROOM_LEVEL_MCTS:
        titles.write('Monte Carlo Search Tree: ', font=font)
    else:
        titles.write('Player Profile: ', font=font)
        titles.goto(-240, -110)
        titles.write(env.player_name, font=("Courier", 18, 'normal', 'bold'))
        titles.goto(-420, -270)
        titles.write(pprint.pformat(env.player, indent=1), font=("Courier", 11, 'normal'))

    return titles

def choose_action(listy):
    return random.choice(np.argwhere(listy == np.amax(listy)).flatten().tolist())

def plan(env, agent, dc, screen, ts, s0, TXT_name):
    """ play the game using hierarchical value iteraction and MCTS """

    start = time.time()
    player = env.player
    s = s0
    env.visited_tiles = [s[0]]
    ep = [] # collect episode

    real_pos = env.tilesummary[s[0]]['pos']

    if TXT_name != None:
        TXT = open(TXT_name,"w")
    else:
        TXT = None
    TXT = visualize.log(str(player),TXT)

    # initialize interface
    # unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = env.observe(None, s[0])
    writer, scores, titles = draw_planning_window(env)
    visualize.update_maze(env,agent,dc,screen,ts,s,trace=env.visited_tiles,real_pos=real_pos,length=visualize.MAX_ITER)
    visualize.update_reward_board(env,writer,s,-80)

    if visualize.GENERATE_GIF:
        visualize.take_screenshot(env, screen, 'HIER_VI_')
    elif visualize.GENERATE_PNGs:
        visualize.take_screenshot(env, screen, 'HIER_VI_', PNG=True)

    # initialize value functions
    V1 = np.zeros((len(env.roomlevel_states), ))
    V2 = np.zeros((env.ntiles, 4))

    for iter in tqdm(range(visualize.MAX_ITER)):
        this = time.time()

        ## ------------------------------
        ##  Step 1 --- choose action
        ## ------------------------------
        if env.replan or visualize.ROOM_LEVEL_MCTS:
            replanned = True
            if visualize.TILE_LEVEL_DFS:
                pi, roomlevel_V = policy_and_value(env,V1,V2,s,TXT)
            elif visualize.ROOM_LEVEL_MCTS:
                pi, roomlevel_V = policy_and_value(env,V1,V2,s,TXT)
            else:
                (pi1,Qsa1,roomlevel_V,V1), (pi,Qsa2, V2) = policy_and_value(env,V1,V2,s,TXT)
            env.replan = False
        else:
            replanned = False

        ## select actions
        if visualize.TILE_LEVEL_DFS:
            a = pi[0]
            pi.remove(pi[0])
        elif visualize.ROOM_LEVEL_MCTS:
            a = random.choice(pi[s])[0]
            print(pi)
            print(a)
        else:
            if SOFT_MAX_POLICY:
                a = np.random.choice(len(env.actions), p=pi[s[0], s[1] // 90, :])
            else:
                a = choose_action(pi[s[0], s[1] // 90, :])

        s_new = env.T(s,env.actions[a])
        start_time = time.time()
        while s_new[0] == "wall":
            if visualize.TILE_LEVEL_DFS or visualize.ROOM_LEVEL_MCTS:
                a = random.choice(pi[s])
            else:
                if SOFT_MAX_POLICY:
                    a = np.random.choice(len(env.actions), p=pi[s[0], s[1] // 90, :])
                else:
                    pi[s[0], s[1] // 90, a] = 0
                    a = choose_action(pi[s[0], s[1] // 90, :])
                    env.replan = True
                    print('!!! replan after force changing policy')
            s_new = env.T(s,env.actions[a])

            if time.time() - start_time > 0.05:  ## just randomly turn left or right
                a = random.choice([0,1])
                s_new = env.T(s, env.actions[a])
                print('!!! randomly turn left or right')
                break
        print(pi[s[0], s[1] // 90, :], env.actions[a], 'from', s, 'to', s_new)

        # if env.step == 200:
        #     env.change_player('with_dog_both')
        #     print('!!!!!!!!!!!!!!!!!! changed player type to with_dog_both')
        titles = update_planning_window(env, titles)

        a = env.actions[a]
        ep.append((s,a))
        s = s_new
        env._pos_agent = s
        if PRINT_CONSOLE: print('___ finished choosing action in', str(time.time()-this),'seconds')

        ## increase roomlevel gamma if the agent has no preference over all actions (values all zero)
        policy = pi[s[0], s[1] // 90, :]
        if policy[0] - 0.33 < 0.01 and policy[1] - 0.33 < 0.01 and policy[2] - 0.33 < 0.01:
            env.replan = True
            env.player['roomlevel_gamma'] = env.roomlevel_gamma_high
            print('!!! replan, changed roomlevel_gamma to',env.roomlevel_gamma_high,'after getting random policy')

        ## increase roomlevel gamma if the agent has fully explored an area
        # elif env.fully_explored(env.tiles2room[s[0]]):
        #     env.replan = True
        #     env.player['roomlevel_gamma'] = 0.99
        #     print('!!! replan, change roomlevel_gamma to 0.99 if the place has been fully explored')

        else:
            env.player['roomlevel_gamma'] = env.roomlevel_gamma_saved
            # print('restore roomlevel_gamma', env.player['roomlevel_gamma'])


        if visualize.ROOM_LEVEL_MCTS:
            visualize.update_graph("plots/mctree.png", screen,ver_shift=-200)

        ## update the graph if room value is just recalculated or the agent moved to a different room
        if visualize.SHOW_ROOMS and replanned:
            this = time.time()
            room_png = update_rooms(env, roomlevel_V, env.tiles2room[s[0]])
            if PRINT_CONSOLE: print('___ finished updating room in', str(time.time() - this), 'seconds')

            this = time.time()
            visualize.update_graph(room_png, screen, ver_shift=160)
            if PRINT_CONSOLE: print('___ finished stamping maze in', str(time.time() - this), 'seconds')

        if env.check_turned_too_much(a): break

        ## ---------- print action and new location
        pos = env.tilesummary[s[0]]['pos']
        TXT = visualize.log('t = ' +str(iter)+ ', ' + str(a) + ' to tile '+ str(s[0]) + ' at '+ str(mapreader.coord(pos))+'\n',TXT)

        ## ------------------------------
        ##  Step 3 --- collect rewards
        ## ------------------------------
        this = time.time()
        env.collect_reward(a)
        if PRINT_CONSOLE: print('___ finished collecting reward in', str(time.time()-this),'seconds')

        this = time.time()
        visualize.update_reward_board(env,writer,s,-80)
        if PRINT_CONSOLE: print('___ finished updating reward board in', str(time.time()-this),'seconds')

        ## ------------------------------
        ##  Step 2 --- observe
        ## ------------------------------
        writer.clear()

        sa_last = real_pos
        real_pos = env.tilesummary[s[0]]['pos']

        this = time.time()
        reward = env.tilesummary_truth[s[0]]['reward']
        unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = env.observe(None, s[0])
        visualize.update_maze(env,agent,dc,screen,ts,s,trace=env.visited_tiles,real_pos=real_pos,sa_last=sa_last,reward=reward,
                              tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change)
        if PRINT_CONSOLE: print('___ finished updating maze in', str(time.time()-this),'seconds')

        if visualize.GENERATE_GIF:
            visualize.take_screenshot(env, screen, 'HIER_VI_')
        elif visualize.GENERATE_PNGs:
            visualize.take_screenshot(env, screen, 'HIER_VI_', PNG=True)

        if env.check_mission_finish():
            break

        # w = open(f'recordings/hvi/hierarchical-{env.step}.txt', "w")
        # w.write(str(env.rooms[env._pos_agent_room]))
        # w.close()

    if JUST_TRAJECTORY:
        Q_table = None
        s = ep[len(ep)-1][0]
        visualize.update_maze(env,agent,dc,screen,ts,s,Q_table,real_pos=env.tilesummary[s[0]]['pos'],trajectory=ep)
        visualize.take_screenshot(env, screen, 'HIER_VI_')

    TXT = visualize.log('\n... finished game in ' + str(time.time() - start) + ' seconds',TXT)
    if TXT != None: TXT.close()
    return ep

def update_rooms(env,roomlevel_V_arr,current_node):
    """ generate an image of the rooms as a directed graph """

    if current_node == None:
        return "plots/rooms.png"

    ## use numpy
    roomlevel_V = {}
    for index in range(len(roomlevel_V_arr)):
        roomlevel_V[index] = round(roomlevel_V_arr[index]*10,2)

    start_graph = time.time()
    G = nx.Graph()
    dG = nx.DiGraph()
    labels = {}
    pos = {}
    rooms = env.rooms
    for index in rooms:
        for neighbor in rooms[index]['neighbors'].keys():
            if neighbor != index:
                G.add_edge(index,neighbor)
                tiles = rooms[index]['neighbors'][neighbor]
                if roomlevel_V[index] < roomlevel_V[neighbor]:
                    dG.add_edge(index,neighbor)
                else:
                    dG.add_edge(neighbor, index)

    sizes = {
        60: {
            'fontsize':9,
            'nodesize':300,
            'edgewidth':2,
            'margins':0.2,
            'arrowsize':20,
            'arrowstyle':'->',
            'inchwidth':4,
            'inchheight':2
        },
        30: {
            'fontsize':8,
            'nodesize':100,
            'edgewidth':1,
            'margins':0.1,
            'arrowsize':15,
            'arrowstyle':'-',
            'inchwidth':4.4,
            'inchheight':2.2
        },
        16: {
            'fontsize':8,
            'nodesize':100,
            'edgewidth':1,
            'margins':0.1,
            'arrowsize':10,
            'arrowstyle':'-',
            'inchwidth':4.4,
            'inchheight':2
        },
        12: {
            'fontsize':8,
            'nodesize':30,
            'edgewidth':1,
            'margins':0.1,
            'arrowsize':5,
            'arrowstyle':'-',
            'inchwidth':3,
            'inchheight':2
        },
        10: {
            'fontsize':6,
            'nodesize':30,
            'edgewidth':1,
            'margins':0.1,
            'arrowsize':5,
            'arrowstyle':'-',
            'inchwidth':4.8,
            'inchheight':2  ## 2, if only one plot
        }
    }
    size = sizes[visualize.TILE_SIZE]

    if visualize.TILE_SIZE >= 30 or visualize.REPLAY_IN_RITA or True:
        if visualize.REPLAY_IN_RITA:
            fig, (ax1, ax2) = plt.subplots(2, 1)
            size['inchheight'] = 6
            size['fontsize'] = 8
        else:
            fig, (ax1, ax2) = plt.subplots(1, 2)

        plt.box(False)
        plt.sca(ax1)
        ax1.margins(x=size['margins'], y=size['margins'])
        ax1.set_title('Rooms Numbers',fontsize=size['fontsize'])
        for node in G.nodes():
            labels[node] = str(node)
            room = rooms[node]
            pos[node] = ((room['left']+room['right'])/2,-(room['top']+room['bottom'])/2)
        nx.draw_networkx_labels(G, pos, labels, font_size=size['fontsize'])
        nx.draw_networkx_nodes(G, pos, node_color='#F2F09E', node_shape='s', node_size=size['nodesize'], alpha=1)
        nx.draw_networkx_nodes(dG, pos, node_color='#1abc9c', node_shape='s', nodelist=[current_node], node_size=size['nodesize'], alpha=1)
        nx.draw_networkx_edges(G, pos, edge_color='#F2F09E', width=size['edgewidth'], alpha=1)

        plt.box(False)
        plt.sca(ax2)
        # ax2.margins(x=size['margins'], y=size['margins'])
        ax2.set_title('Room Values',fontsize=size['fontsize'])
        max_node = 0
        max_value = 0
        for node in G.nodes():
            if roomlevel_V[node] > max_value:
                max_value = roomlevel_V[node]
                max_node = node

        nx.draw_networkx_labels(dG, pos, roomlevel_V, font_size=size['fontsize'])
        nx.draw_networkx_nodes(dG, pos, node_color='#F2F09E', node_shape='s', node_size=size['nodesize'], alpha=1)
        nx.draw_networkx_nodes(dG, pos, node_color='#f09289', node_shape='s', nodelist=[max_node], node_size=size['nodesize'], alpha=1)
        nx.draw_networkx_edges(dG, pos, edge_color='#F2F09E', arrowstyle=size['arrowstyle'], arrowsize=size['arrowsize'], width=size['edgewidth'], alpha=1)

    else:
        fig, ax1 = plt.subplots()

        plt.box(False)
        plt.sca(ax1)
        ax1.margins(x=size['margins'], y=size['margins'])
        ax1.set_title('Rooms Values',fontsize=size['fontsize'])
        max_node = 0
        max_value = 0
        for node in G.nodes():
            if roomlevel_V[node] > max_value:
                max_value = roomlevel_V[node]
                max_node = node
            labels[node] = str(node)
            room = rooms[node]
            pos[node] = ((room['left']+room['right'])/2,-(room['top']+room['bottom'])/2)

        nx.draw_networkx_nodes(G, pos, node_color='#F2F09E', node_shape='s', node_size=size['nodesize'], alpha=1)
        nx.draw_networkx_nodes(dG, pos, node_color='#1abc9c', node_shape='s', nodelist=[current_node], node_size=size['nodesize'], alpha=1)
        nx.draw_networkx_nodes(dG, pos, node_color='#f09289', node_shape='s', nodelist=[max_node], node_size=size['nodesize'], alpha=1)
        nx.draw_networkx_labels(dG, pos,roomlevel_V,font_size=6)
        nx.draw_networkx_edges(G, pos, edge_color='#F2F09E', width=size['edgewidth'], alpha=1)

    fig.set_size_inches(size['inchwidth'], size['inchheight'])
    # plt.rcParams['savefig.facecolor']='#F7F7F7'
    if PRINT_CONSOLE: print('... before generating graph room',str(time.time() - start_graph), 'seconds')
    start_graph = time.time()
    plt.savefig("plots/rooms.png", bbox_inches='tight')
    plt.close()

    if PRINT_CONSOLE: print('... finished generating graph room',str(time.time() - start_graph), 'seconds')
    return "plots/rooms.png"
