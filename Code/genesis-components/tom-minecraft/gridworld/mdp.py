import numpy as np
import pandas as pd
import math
import re
import pdb
import time
import pprint
import random
import copy
from random import sample
import turtle
import os, shutil
from os import listdir
from os.path import isfile, isdir, join
import csv
from ast import literal_eval as make_tuple
import json
import heapq as hq

import mapreader
import planners
import ASIST_settings
import visualize
from mcts import PrintDiGraph
from player import players
from uct import UCT
import utils

PRINT_REPLAN = False

class POMDP:

    actions = ('go_straight', 'turn_left', 'turn_right', 'triage')
    head_oppo = {0: 180, 90: 270, 180: 0, 270: 90}

    def __init__(self, MAP, map_room = None, player = None, player_name = 'tester', obs_rewards = None, ORACLE_MODE = False):

        ## attributes about the mission, which will not change
        self.ORACLE_MODE = ORACLE_MODE
        self.MAP = self.trial_name = MAP
        if map_room == None: map_room = MAP_CONFIG[MAP][0]
        self.MAP_ROOM = map_room
        self.max_iter = MAP_CONFIG[MAP][4]
        self.countdown_real = self.countdown = MAP_CONFIG[MAP][-1]

        ## attributes that change with player type or observations
        self.player_name = player_name
        if player == None: player = players[player_name]
        self.player = player
        self.initiate_player_profile()
        self.temp_player_rewards = self.player['rewards']
        self.initiate_algo_parameter(self.player)

        self.world_grids_truth, self.tile_indices, self.tilesummary_truth, self.start_tile, self.start_heading, self.victim_summary = mapreader.read_cvs(MAP, self.player["rewards"])
        self.rooms_truth, self.tiles2room_truth, self.floor_truth = mapreader.read_rooms(self.MAP_ROOM, self.tilesummary_truth)

        self._pos_agent = (self.start_tile, self.start_heading)
        self._pos_agent_room = self.tiles2room_truth[self.start_tile]
        self.observed_tiles = []
        self.observed_rooms = []
        self.on_boundary = {}  ## for macro actions
        self.initiate_expections()
        self.initiate_obs_reward(obs_rewards)
        self.initiate_dog_barks()
        if self.ORACLE_MODE:
            self.tilesummary = self.tilesummary_truth
            self.rooms = self.rooms_truth
            self.tiles2room = self.tiles2room_truth
            self.floor = self.floor_truth
            self.obs_rewards_init = self.obs_rewards
        self.initiate_obs_paths()  ## for observing rooms

        ## records & memory about the mission
        self.score_weighted = 0
        self.score = 0
        self.step = 0
        self.score_total = len(self.victim_summary['V']) * self.player['rewards']['victim']
        self.score_total += len(self.victim_summary['VV']) * self.player['rewards']['victim-yellow']

        self.consecutive_actions = {a:0 for a in ['go_straight', 'triage', 'turn']}  ## terminate when the agent turns too much
        self.start_time = time.time()  ## ternimate when time is up
        self._max_tile = None
        self.remaining_to_see = copy.deepcopy(self.victim_summary)
        self.remaining_to_save = copy.deepcopy(self.victim_summary)
        self.victims_saved = {'VV':[], 'V':[]}
        self.triageables = {}   ## save triageble results
        self.rooms_to_plan = []
        self.last_three_tiles = [-1, -1, -1]
        self.visited_tiles = []
        self.visited_counts = []
        self.victim_tiles = []
        self.visited_rooms = []
        self.fully_explored_rooms = []
        self.removed_rooms = []
        self.barked_tiles = []
        self.barked_rooms = {}  ## memory of the recent bark result of each room
        self.barked_rooms_real = {}  ## according to msg
        self.temperary_rewards = {}  ## for roomlevel value assignment
        self.replan = True  ## for hierarchical planning
        self.decide = False  ## for inverse planning
        self.decide_delay = 0

        ## need function to verify that two macros are the same thing
        self.achieved_macros = {}  ## {macro: {countdown: *, next: []}}
        self.proposed_macros = {}  ## {countdown: []}
        self.macro_paths = {}  ## {(current_state, next_macro): path}
        self.saved_macro_U = {0:{}}  ## save the macro_U calculated in all steps
        self.paths = {}  ## saved calculated path between two states
        # self.state2macro = {}

        self.update_belief()

        ## -------------------------------
        ##  State space
        ## -------------------------------
        # -------- state space: (1) player location (2) player orientation
        self.ntiles = len(self.tilesummary)
        self.states = tuple([(tile,head)
                        for tile in range(len(self.tilesummary))
                        for head in (0,90,180,270)])

        # -------- state and action space for room-level
        self.roomlevel_states = list(self.rooms.keys())
        self.roomlevel_actions = self.update_roomlevel_actions()
        if visualize.PLANNING or visualize.HIERARCHICAL_PLANNING:
            if max(self.roomlevel_states) != len(self.roomlevel_states) -1:
                print('WARNING! The room numbering is not continuous, may encounter problems in hierarchical planning')

        # -------- MCTS
        self.G = PrintDiGraph()
        self.uct = None
        self.uct_max_num_steps = 40

    """
        agent initiation 
    """
    def initiate_uct(self):
        self.uct = UCT(self.actions, self.R, self.T, done_fn=self.check_done,
                       num_search_iters=1000, gamma=0.99, seed=0)
        return self.uct

    def initiate_expections(self):
        maps = {
            '46by45_2.csv': ('46by45_0.csv','46by45_0_rooms.csv'),
            '48by89.csv': ('48by89_0.csv','48by89_rooms.csv'),
            '48by89_easy.csv': ('48by89_0.csv','48by89_rooms.csv'),
            '48by89_med.csv': ('48by89_0.csv','48by89_rooms.csv'),
            '48by89_hard.csv': ('48by89_0.csv','48by89_rooms.csv'),
        }
        if self.ORACLE_MODE: maps = {}
        # ---------- summary of believed tile type and reward
        if self.MAP in maps: ## if the actual map is different from the map given to human
            self.world_grids, _, self.tilesummary, _, _, _ = mapreader.read_cvs(maps[self.MAP][0], self.player["rewards"])
            self.rooms, self.tiles2room, self.floor = mapreader.read_rooms(maps[self.MAP][1], self.tilesummary_truth)
        else:
            self.tilesummary = copy.deepcopy(self.tilesummary_truth)
            for tile in self.tilesummary.keys():
                if self.tilesummary[tile]['type'] not in ['wall', 'door']:
                    self.tilesummary[tile]['type'] = 'air'
                self.tilesummary[tile]['reward'] = 0

            self.world_grids = []
            for row in self.world_grids_truth:
                new_row = []
                for cell in row:
                    if cell in ['W', 'D', 'S', '0', '90', '180', '270']:
                        new_row.append(cell)
                    else:
                        new_row.append('')
                self.world_grids.append(new_row)

            self.rooms = self.rooms_truth
            self.tiles2room = self.tiles2room_truth
            self.floor = self.floor_truth

        self.tiles_to_observe = [t for t,v in self.tilesummary.items() if v['type']!='wall']

    def initiate_algo_parameter(self, player):
        self.player["epsilon"] = 1e-2

        ## 'BELIEF_UNIFORM' # 'BELIEF_ONE_EACH_ROOM' # 'BELIEF_UNIFORM_IN_ROOM'

    def initiate_obs_reward(self, obs_rewards=None): ## cache is supposed to help speed up initiation

        # if obs_rewards != None:
        #     self.obs_rewards_truth = obs_rewards
        #     self.obs_rewards = copy.deepcopy(obs_rewards)
        #     self.get_obs_rewards()
        # else:
        self.obs_rewards = None  ## obs_rewards
        self.get_obs_rewards()
        self.obs_rewards_truth = copy.deepcopy(self.obs_rewards)

    def initiate_player_profile(self, temperature=1, gamma=0.7, horizon=10, obs_thre=0.05, timeout=2, beta=3,
                                 prior_belief='BELIEF_UNIFORM_IN_ROOM', ##'BELIEF_ONE_EACH_ROOM', ## 'BELIEF_UNIFORM',  ## 'BELIEF_UNIFORM_IN_ROOM',
                                 observation_reward=0.05, certainty_boost_factor=4, rewards=None, costs=None):

        if 'countdown' in self.player:
            self.countdown = self.player['countdown']
        else:
            self.player['countdown'] = self.countdown

        if 'timeout' not in self.player:
            self.player['timeout'] = timeout

        if 'beta' not in self.player:
            self.player['beta'] = beta

        if "observation_reward" not in self.player:
            self.player["observation_reward"] = observation_reward

        if 'certainty_boost_factor' not in self.player:
            self.player['certainty_boost_factor'] = certainty_boost_factor

        ## for fitting softmax
        if 'temperature' not in self.player:
            self.player['temperature'] = temperature

        ## for discounting future rewards on macro action level
        if 'gamma' not in self.player:
            self.player["gamma"] = gamma

        ## for planning horizon on macro action level
        if 'horizon' not in self.player:
            self.player['horizon'] = horizon

        ## for threshold of judging a room as fully observed and ready to move on
        if 'obs_thre' not in self.player:
            self.player['obs_thre'] = obs_thre

        ## for victim distribution
        if 'prior_belief' not in self.player:
            self.player["prior_belief"] = prior_belief

        ## for costs of actions and of types
        if costs == None:
            costs = { 'triage': 1, 'turn_left': 0.1, 'turn_right': 0.1, 'go_straight': 0.05,
                      'victim': 5, 'victim-yellow': 10, 'gravel': 2}
            if '48by89' in self.MAP:
                costs['victim-yellow'] = 15
                costs['victim'] = 7.5
        if 'costs' not in self.player:
            self.player["costs"] = {}
        for obj, value in costs.items():
            if obj not in self.player["costs"]:
                self.player["costs"][obj] = value

        ## rewards
        if rewards == None:
            rewards = {'wall': -1000, 'air': 0, 'door': 0, 'fire': -0.5, 'victim': 10, 'victim-yellow': 10, 'victim-red': 0}
            if '48by89' in self.MAP:
                rewards['victim-yellow'] = 30
        if 'rewards' not in self.player:
            self.player["rewards"] = {}
        for obj, value in rewards.items():
            if obj not in self.player["rewards"]:
                self.player["rewards"][obj] = value
                if obj == 'victim' and self.player["rewards"][obj] == 1:
                    self.player["rewards"][obj] *= 10
                if obj == 'victim-yellow' and self.player["rewards"][obj] == 1:
                    self.player["rewards"][obj] *= 30

        self.get_wanted_barks()

    """
        DARPA env changes
    """
    def get_wanted_barks(self):
        self.wanted_barks = []
        if self.player["rewards"]['victim-yellow'] > 0:
            self.wanted_barks.append(2)
        if self.player["rewards"]['victim'] > 0:
            self.wanted_barks.append(1)

    def get_observable_tiles(self, s):
        observable_tiles = []
        for tile in self.obs_rewards_truth[s]:
            if self.tilesummary[tile]['type']!='wall':
                observable_tiles.append(tile)
        return observable_tiles

    def change_rewards(self, rewards):
        if self.temp_player_rewards != rewards:
            old = copy.deepcopy(self.tilesummary_truth)
            _, _, self.tilesummary_truth, _, _, _ = mapreader.read_cvs(self.MAP, rewards)

            ## change the believed rewards of seen but unvisited tiles
            for tile in self.observed_tiles:
                self.tilesummary[tile]['reward'] = self.tilesummary_truth[tile]['reward']
            for tile in self.visited_tiles:
                self.tilesummary[tile]['reward'] = old[tile]['reward']
                self.tilesummary_truth[tile]['reward'] = old[tile]['reward']

            ## the expected tiles reward have changed
            self.update_belief()

        self.temp_player_rewards = rewards

    def change_player(self, player_name, player=None):

        ## the basic paramaters have changed
        if player == None:
            player = players[player_name]
        self.player_name = player_name
        self.player = player
        self.initiate_algo_parameter(player)
        self.replan = True
        if PRINT_REPLAN: print('!!! Replan after changing players')

        ## the reward for all tiles may have changed
        self.change_rewards(player["rewards"])
        self.get_wanted_barks()

        ## the barking result have changed
        self.rooms, self.tiles2room, self.floor = mapreader.read_rooms(None, self.tilesummary_truth, self.floor)

        ## the observation reward might have changed

    def change_room_structure(self):
        self.rooms, self.tiles2room, self.floor = mapreader.read_rooms(None, self.tilesummary_truth, self.floor)
        self.tiles_to_observe = [t for t,v in self.tilesummary.items() if v['type']!='wall']
        self.initiate_dog_barks()

        ## update memory according to barked_rooms
        for room_index in self.barked_rooms:
            self.rooms[room_index]['bark'] = self.barked_rooms[room_index]
        for room_index in self.barked_rooms_real:
            if room_index not in self.barked_rooms:
                self.rooms[room_index]['bark'] = self.barked_rooms_real[room_index]

        ## check if the rooms have been fully explored
        for room_index in self.fully_explored_rooms:
            self.fully_explored(room_index, CHECK=True)

        ## update connectivity
        self.roomlevel_actions = self.update_roomlevel_actions()
        self.replan = True
        if PRINT_REPLAN:
            print(self.player_name, '!!! replan after changing room configuration')

    def update_uct(self, num_search_iters=1000, gamma=0.99, beta=1):
        if self.uct != None:
            self.uct.update_param(num_search_iters, gamma, beta)
            if not (num_search_iters==1000 and gamma==0.99 and beta==1):
                print('changed uct params: num_search_iters', num_search_iters, 'gamma', gamma, 'beta', beta)
                self.player["observation_reward"] *= 10
            else:
                self.player["observation_reward"] = players[self.player_name]["observation_reward"]

    def update_roomlevel_actions(self):
        self.roomlevel_actions = {}
        for s in self.roomlevel_states:
            self.roomlevel_actions[s] = [a for a in self.rooms[s]['neighbors'] if a not in self.removed_rooms]
        return self.roomlevel_actions

    def get_door_explanation(self, current_room, door_index, room_names = None):
        explanation = 'None'
        for neighbor, adjacents in self.rooms[current_room]['neighbors'].items():
            for adjacent in adjacents:
                mydoor, yourdoor, heading = adjacent
                frm, to = current_room, neighbor
                if room_names != None:
                    frm = room_names[frm]
                    to = room_names[to]
                else:
                    frm = f'Area {frm}'
                    to = f'Area {to}'

                if door_index == mydoor:
                    explanation = f"the player wants to leave {frm} to enter {to}"
                elif door_index == yourdoor:
                    explanation = f"the player wants to enter {to} from {frm}"
        return explanation

    def initiate_dog_barks(self):
        self.bark_positions = {}
        for room_index in self.rooms:
            room = self.rooms[room_index]
            for neighbor in room['neighbors']:
                if self.room_is_room(neighbor):
                    for adjacent in room['neighbors'][neighbor]:
                        mydoor, yourdoor, angle = adjacent

                        ## the situation where the doorstep is determined to be between walls instead of before the wall
                        if self.tilesummary[yourdoor]['type'] != 'door':
                            t = self.tilesummary_truth[mydoor]
                            if t[0] == t[180] == 'wall':
                                for corrected in [90, 270]:
                                    if self.tiles2room_truth[t[corrected]] == room_index:
                                        yourdoor = mydoor
                                        mydoor = t[corrected]
                            elif t[90] == t[270] == 'wall':
                                for corrected in [0, 180]:
                                    if t[corrected] == 'wall':
                                        print(room_index, neighbor, adjacent)
                                    if self.tiles2room_truth[t[corrected]] == room_index:
                                        yourdoor = mydoor
                                        mydoor = t[corrected]

                        self.bark_positions[mydoor] = (neighbor, yourdoor, self.rooms[neighbor]['bark'])

        self.door2doorstep = {}
        for tile in self.bark_positions:
            if self.tilesummary_truth[tile]['type'] != 'wall':
                self.tilesummary_truth[tile]['type'] = 'doorstep'
            self.tilesummary[tile]['type'] = 'doorstep'
            room, door, bark = self.bark_positions[tile]
            self.door2doorstep[(door, room)] = tile
            self.rooms[room]['doorsteps'].append(tile)
            if door not in self.rooms[room]['doors']:
                # print(f'    !!!! door {door} should belong to room {room} too')
                self.rooms[room]['doors'].append(door)

    def get_room_name(self, room):
        if '48by89' in self.MAP:
            room = ASIST_settings.room_names[room]
        return room

    def dog_bark(self, s):

        DEBUG = False

        def list_minus(li1, li2):
            return list(set(li1) - set(li2))

        bark = None
        barked_neighbor = None

        if s[0] in self.bark_positions:
            # self.achieved_macro(('doorstep', s[0]), self.countdown_real, s)

            barked_neighbor, yourdoor, bark = self.bark_positions[s[0]]
            room = self.rooms[barked_neighbor]

            if s[0] not in self.barked_tiles:
                self.barked_tiles.append(s[0])
                # room_name = self.get_room_name(barked_neighbor)
                # print(f'\n\nbeeeeeeeeeeeeeep at room {room_name} @ state {s}\n\n')

            if 'dog' in self.player.keys():

                ## calculate average reward
                unobserved_tiles = list_minus(room['tiles'], self.observed_tiles)
                n_unobserved = len(unobserved_tiles)
                ave_reward = 0
                if n_unobserved != 0:
                    if bark == 2 and len(self.remaining_to_see['VV']) != 0:
                        ave_reward = self.player['rewards']['victim-yellow'] / n_unobserved
                        if DEBUG: print(f'  \n yellow in room {barked_neighbor}, r = {round(ave_reward,2)} for {n_unobserved} tiles')
                    elif bark == 1 and len(self.remaining_to_see['VV']) != 0:
                        ave_reward = self.player['rewards']['victim'] / n_unobserved
                        if DEBUG: print(f'  \n green in room {barked_neighbor}, r = {round(ave_reward, 2)} for {n_unobserved} tiles')
                    else:
                        if DEBUG: print(f'  \n no victim in room {barked_neighbor}')

                for tile in unobserved_tiles:
                    self.tilesummary[tile]['reward'] = ave_reward

                ## update belief
                if barked_neighbor not in self.barked_rooms or self.barked_rooms[barked_neighbor] != bark:
                    self.replan = True
                    if PRINT_REPLAN:
                        print(self.player_name, '!!! replan at dog bark position')
                    self.decide = True
                    self.decide_delay = 3
                self.barked_rooms[barked_neighbor] = bark

            return barked_neighbor, bark

        return barked_neighbor, bark

    def update_belief(self, unobserved_in_rooms=None):

        if visualize.EXPERIMENT_REPLAY and not (visualize.REPLAY_WITH_TOM or visualize.REPLAY_IN_RITA):
            return {}

        def list_minus(li1, li2):
            return list(set(li1) - set(li2))

        def get_ave_reward(n_tiles):
            if n_tiles == 0: return 0
            pos_total_reward = len(self.remaining_to_see['V']) * max(0, self.player['rewards']['victim'])
            pos_total_reward += len(self.remaining_to_see['VV']) * max(0, self.player['rewards']['victim-yellow'])
            ave_reward = pos_total_reward / n_tiles
            # print(pos_total_reward, n_tiles, ave_reward)
            return ave_reward

        ## if the player has a dog and dog barked, update belief of this room
        self.dog_bark(self._pos_agent)

        belief = self.player['prior_belief']
        tilesummary = self.tilesummary

        ## compute once for the different types of beliefs
        if unobserved_in_rooms == None:
            unobserved_in_rooms = {}
            for room in self.rooms:
                unobserved_in_rooms[room] = list_minus(self.rooms[room]['tiles'], self.observed_tiles)
        self.unobserved_in_rooms = unobserved_in_rooms

        ## compute reward based on different types of beliefs
        if belief == 'BELIEF_UNIFORM':
            n_tiles = len(list_minus(self.tiles_to_observe, self.observed_tiles))
            ave_reward = get_ave_reward(n_tiles)
            for room in self.rooms:
                if room not in self.barked_rooms:
                    for tile in unobserved_in_rooms[room]:
                        tilesummary[tile]['reward'] = ave_reward

        elif belief == 'BELIEF_UNIFORM_IN_ROOM':
            tiles = []
            for room in self.get_rooms():
                if room not in self.barked_rooms:
                    tiles.extend(unobserved_in_rooms[room])
            ave_reward = get_ave_reward(len(tiles))

            for tile in tiles:
                tilesummary[tile]['reward'] = ave_reward

        elif belief == 'BELIEF_ONE_EACH_ROOM':
            max_reward = max(self.player['rewards']['victim'], self.player['rewards']['victim-yellow'])
            for room in self.get_rooms():

                ## at most two victims in a room, there may be two victims that include one green
                observed_victims = []
                for t in self.rooms[room]['tiles']:
                    if t in self.observed_tiles and 'victim' in self.tilesummary[t]['type']:
                        observed_victims.append(t)
                if len(observed_victims) == 1:
                    max_reward = min(self.player['rewards']['victim'], self.player['rewards']['victim-yellow'])
                elif len(observed_victims) == 2:
                    max_reward = 0

                ## uniformly distribute rewards
                if not self.fully_explored(room) and room not in self.barked_rooms:
                    tiles = unobserved_in_rooms[room]
                    if len(tiles) == 0:
                        ave_reward = 0
                    else:
                        ave_reward = max_reward / len(tiles)
                    # print(f'ave reward for room {room} is {ave_reward}')

                    for tile in tiles:
                        tilesummary[tile]['reward'] = ave_reward

        ## update the tilesummary to room summary
        for room_index in self.rooms:
            room = self.rooms[room_index]
            for tile in room['tiles']:
                room['tilesummary'][tile]['type'] = tilesummary[tile]['type']
                room['tilesummary'][tile]['reward'] = tilesummary[tile]['reward']

        self.tilesummary = tilesummary
        # self.print_belief_csv()
        return unobserved_in_rooms

    def revise_room(self, tile):

        ## actually the tile is not in the room, but a different room
        # self.tiles2room[tile] = self.tiles2room_truth[tile]
        i = self.tilesummary[tile]['row']
        j = self.tilesummary[tile]['col']
        if self.floor[i][j] != self.floor_truth[i][j]:
            # print(tile, '|',self.floor[i][j],'->',self.floor_truth[i][j])
            self.floor[i][j] = self.floor_truth[i][j]
            return True
        return False

    """
        POMDP planning framework
    """
    def observe(self, new_obs_tiles, current_pos, unobserved_in_rooms=None, obs_rewards=None):
        """ observe the tiles in front and update the expected reward accordingly """

        start = time.time()

        ##  --------- observe in the environment
        ## if not given the special obsersed tiles, use default angle and distance
        if new_obs_tiles == None:
            if visualize.USE_SAVED_OBS:
                new_obs_tiles = self.obs_rewards[self._pos_agent]
            else:
                new_obs_tiles = mapreader.print_shadow(env, self._pos_agent)

        # update belief of observed rewards
        tiles_to_color = list(set(new_obs_tiles) - set(self.observed_tiles))
        tiles_to_change = {}  ## tile: (old_type, new_type)
        tiles_to_change_room = []
        to_delete = []  ## for obs_path recalculate
        for tile in new_obs_tiles:
            if tile not in self.observed_tiles:
                self.tilesummary[tile]['reward'] = self.tilesummary_truth[tile]['reward']

                old_type = self.tilesummary[tile]['type']
                new_type = self.tilesummary_truth[tile]['type']
                if old_type != new_type:
                    ## change the type
                    tiles_to_change[tile] = (old_type, new_type)
                    self.tilesummary[tile] = self.tilesummary_truth[tile]

                    ## change also the neighbors
                    for head in [0, 90, 180, 270]:
                        other_tile = self.tilesummary[tile][head]
                        if other_tile != 'wall':
                            head_oppo = {0: 180, 90: 270, 180: 0, 270: 90}[head]
                            if new_type == 'wall':
                                self.tilesummary[other_tile][head_oppo] = 'wall'
                            else:
                                self.tilesummary[other_tile][head_oppo] = tile

                if self.revise_room(tile):
                    tiles_to_change_room.append(tile)

                # update the summary of the victims haven't seen
                block_type = self.tilesummary[tile]['type']
                if 'victim' in block_type:
                    self.replan = True
                    self.decide = True
                    self.decide_delay = 3
                    if PRINT_REPLAN: print(self.player_name, '!!! replan after seeing victims')
                    if 'victim-yellow' in block_type and tile in self.remaining_to_see['VV']:
                        self.remaining_to_see['VV'].remove(tile)
                    elif 'victim' in block_type and tile in self.remaining_to_see['V']:
                        self.remaining_to_see['V'].remove(tile)
                    self.tilesummary[tile]['reward'] *= self.player['certainty_boost_factor']

                    # if not (visualize.REPLAY_IN_RITA or visualize.REPLAY_WITH_TOM) and self.player_name != None:
                    #     print('-------------------------- found', block_type, tile)

                    # if self.tilesummary[tile]['reward'] > 0:
                    #     self.rooms_to_plan.append(self.tiles2room[tile])

                ## obs_path neR(s=(ed to change
                elif block_type == 'gravel':
                    # print(' found blockage at', tile)
                    if self.tile_in_room(tile):
                        self.replan = True
                        # if PRINT_REPLAN:
                        #     print(self.player_name, f'!!! replan after seeing gravel at {tile}')

                    for k in self.obs_paths:
                        if isinstance(k, tuple):  ## observe a partial room
                            if tile in k[1]:
                                to_delete.append(k)
                        # elif isinstance(k, int):  ## observe the whole room
                        #     if self.tiles2room[k] == self.tiles2room[tile]:
                        #         to_delete.append(k)
                    room = self.tiles2room[tile]
                    # if room == 9:
                    #     print(tile)
                    for tt in self.rooms[room]['tiles']:
                        # if tt in new_obs_tiles or tt in self.observed_tiles:
                        for h in [0, 90, 180, 270]:
                            self.obs_rewards_init[(tt, h)] = self.obs_rewards[(tt, h)]
                    for k in to_delete:
                        if k in self.obs_paths:
                            # old_path = self.obs_paths[k]
                            self.obs_paths.pop(k)
                            # new_path = self.get_obs_path(k[0], room)
                            # self.obs_paths[k] = new_path
                            # print(f'\n  replaned obs path from {k} that covers blockage {tile}')
                            # print(f'       old: {old_path}')
                            # print(f'       new: {new_path}')

        # if len(to_delete) > 0:
        #     rooms = set([self.tiles2room[t[0]] for t in to_delete])
        #     for room in rooms:
        #     for tt in self.rooms[room]['tiles']:
        #         if tt in new_obs_tiles and tt not in self.observed_tiles:
        #             for h in [0, 90, 180, 270]:
        #                 self.obs_rewards_init[(tt,h)] = self.obs_rewards[(tt,h)]
        #     for k in to_delete:
        #         old_path = self.obs_paths[k]
        #         self.obs_paths.pop(k)
        #         new_path = self.get_obs_path(k[0], room)
        #         self.obs_paths[k] = new_path
        #         print(f'\n  replaned obs path from {k} that covers blockage {tile}')
        #         print(f'       old: {old_path}')
        #         print(f'       new: {new_path}')

        ## for inverse planning, decide whether to consider step in inv planning
        if self.decide and self.decide_delay != 0:
            self.decide_delay -= 1
        else:
            self.decide = False

        # ## decide if see interesting things in FoV
        # for tile in self.obs_rewards_truth[self._pos_agent]:
        #     block_type = self.tilesummary[tile]['type']
        #     if 'victim' in block_type or 'door' in block_type:
        #         self.decide = True
        #         self.decide_delay = 3
        #         if tile in new_obs_tiles and 'victim' in block_type:
        #             self.replan = True
        #             if PRINT_REPLAN: print(self.player_name, '!!! replan after making new observations')

        ## summarize the rooms again
        if len(tiles_to_change_room) > 0:
            self.change_room_structure()

        ## add to observed tiles
        self.observed_tiles.extend(new_obs_tiles)
        self.observed_tiles = list(set(self.observed_tiles))

        ## update belief states: believed reward in every tile = total_reward/unobserved_tiles
        unobserved_in_rooms = self.update_belief(unobserved_in_rooms)  ## takes 0.001/0.002 sec

        ## more than 90 % of tiles have been observed
        room = self.tiles2room[self._pos_agent[0]]
        macro = ('explore', room)
        if self.room_is_room(room) and macro not in self.achieved_macros and self.countdown_real != None:
            if len(self.unobserved_in_rooms[room]) / len(self.rooms[room]['tiles']) == 0 and \
                    sum([self.tilesummary[t]['reward'] for t in self.rooms[room]['tiles']]) < 0.5: ## < self.player['obs_thre']:
                self.achieved_macro(macro, self.countdown_real, self._pos_agent)
                self.observed_rooms.append(room)

        ## update the number of tiles you can observe in a tile
        obs_rewards = self.get_obs_rewards(obs_rewards)  ## takes the most of time and increasing

        ## tiles_to_color is for coloring observed tiles to yellow
        ## tiles_to_change is to change tiles if different from original map
        return unobserved_in_rooms, obs_rewards, tiles_to_color, list(tiles_to_change.keys())

    def get_tiles_to_observe(self, room=None):
        if room != None:
            return [t for t in self.rooms[room]['tiles'] if self.tilesummary[t]['type']!='wall' and t not in self.observed_tiles]
        return [t for t in self.tilesummary if self.tilesummary[t]['type'] != 'wall' and t not in self.observed_tiles]

    def check_back_forth(self, s):
        if s == self._pos_agent:
            self.replan = True
            if PRINT_REPLAN:
                print(self.player_name, f'!!! replan after going into wall at {s}')

        A, B, C = self.last_three_tiles
        new_tile = s[0]
        if A == C and B == new_tile:
            self.replan = True
            if PRINT_REPLAN:
                print(self.player_name, f'!!! replan after back and forth at {s} after {self.last_three_tiles}')
            # return True
        if new_tile != C:
            self.last_three_tiles = B, C, new_tile
        return False

    def collect_reward(self, a=None, SKIP_TRIAGE=False):
        """ collect the reward from the current state, return if there is victim """

        self.step += 1
        if a != 'triage' and a != None and not SKIP_TRIAGE:
            self.countdown_real -= self.player['costs'][a]
        # self.saved_macro_U[self.step+1] = {}
        victim_to_change = []

        ## mark the tile and room visited
        s = self._pos_agent
        if s[0] not in self.visited_tiles:
            self.visited_tiles.append(s[0])

        ## replan once got into a different room
        if self.tiles2room[s[0]] != self._pos_agent_room:
            self._pos_agent_room = self.tiles2room[s[0]]
            if self._pos_agent_room not in self.visited_rooms:
                self.visited_rooms.append(self._pos_agent_room)
                # if self.room_is_room(self._pos_agent_room):
                #     self.replan = True
                #     if PRINT_REPLAN: print(self.player_name, '!!! replan after visiting a new area')

        block_type = self.tilesummary[s[0]]['type']

        ## ugly codes for removing reward of gravels once one is cleared
        tilesummary = self.tilesummary
        if block_type == 'gravel':
            for head in [0,90,180,270]:
                tile = tilesummary[s[0]][head]
                if tile!='wall':
                    if tilesummary[tile]['type'] == 'gravel':
                        tilesummary[tile]['type'] = 'air'
                        tilesummary[tile]['reward'] = 0
                        for head2 in [0,90,180,270]:
                            tile2 = tilesummary[tile][head2]
                            if tile2!='wall':
                                if tilesummary[tile2]['type'] == 'gravel':
                                    tilesummary[tile2]['type'] = 'air'
                                    tilesummary[tile2]['reward'] = 0
                                    for head3 in [0,90,180,270]:
                                        tile3 = tilesummary[tile2][head3]
                                        if tile3!='wall':
                                            if tilesummary[tile3]['type'] == 'gravel':
                                                tilesummary[tile3]['type'] = 'air'
                                                tilesummary[tile3]['reward'] = 0

        elif block_type in ['door', 'victim', 'victim-yellow']:
            self.decide = True
            self.decide_delay = 3

        # reset all temperary_rewards that come from roomlevel VI
        for tile in self.temperary_rewards.keys():
            self.tilesummary[tile]['reward'] = self.temperary_rewards[tile]
            if tile == s[0]:
                self.replan = True
                if PRINT_REPLAN: print(self.player_name, '!!! replan after taking assigned reward')
        for room in self.rooms:
            self.rooms[room]['neighbors_temp_reward'] = {}

        # if not visualize.USE_DARPA or 'dog' in visualize.PLAYER_NAME: ## TODO investigate
        self.temperary_rewards = {}

        ## turn block into air
        self.tilesummary[s[0]]['reward'] = 0
        # self.tilesummary[s[0]]['type'] = 'air'
        self.tilesummary_truth[s[0]]['reward'] = 0

        ## add score, update dict of victims haven't been saved, change dog bark results
        facing_block = self.triageable(s)
        if facing_block and a == 'triage':
            s = (facing_block, s[1])
            block_type = self.tilesummary[facing_block]['type']

            self.tilesummary[s[0]]['reward'] = 0
            # self.tilesummary[s[0]]['type'] = 'air'
            self.tilesummary_truth[s[0]]['reward'] = 0

            self.replan = True
            if PRINT_REPLAN: print(self.player_name, '!!! replan after saving victim')

            ## in replay, the score and successful triage event will be given
            if not SKIP_TRIAGE:
                score = 0
                if 'victim-yellow' in block_type and s[0] in self.remaining_to_save['VV']:
                    score = 30
                    self.countdown_real -= self.player['costs']['victim-yellow']
                    self.remaining_to_save['VV'].remove(s[0])
                    self.victims_saved['VV'].append(s[0])
                elif 'victim' in block_type and s[0] in self.remaining_to_save['V']:
                    score = 10
                    self.countdown_real -= self.player['costs']['victim']
                    self.remaining_to_save['V'].remove(s[0])
                    self.victims_saved['V'].append(s[0])
                victim_to_change.append(s[0])
                self.score += score
                self.score_weighted += score * math.pow(visualize.SCORE_GAMMA, self.step)
                self.achieved_macro((block_type, facing_block), self.countdown_real, self._pos_agent)

            ## change dog bark results
            BARK = 0
            if self.fully_explored(self._pos_agent_room):
                for tile in self.rooms[self._pos_agent_room]['tiles']:
                    if tilesummary[tile]['type'] == 'victim-yellow':
                        BARK = 2
                    elif tilesummary[tile]['type'] == 'victim' and BARK == 0:
                        BARK = 1
                self.rooms[self._pos_agent_room]['bark'] = BARK
                self.barked_rooms[self._pos_agent_room] = BARK

            # print('+++++++++         new BARK of this room is',BARK)

            if BARK == 0 and self._pos_agent_room in self.rooms_to_plan:
                self.rooms_to_plan.remove(self._pos_agent_room)

        return victim_to_change

    def _check_on_boundary(self, tile):

        def check_observable(a,b):
            if (a, b) in self.tile_indices:
                t = self.tile_indices[(a,b)]
                if t not in self.observed_tiles and self.tilesummary[t]['type'] != 'wall':
                    return True
            return False

        x, y = self.tilesummary[tile]['row'], self.tilesummary[tile]['col']
        # print('checking',(x,y))
        for a, b in [(x-1, y), (x+1, y), (x, y-1), (x, y+1)]:
            if check_observable(a, b): return True
        return False

    def check_turned_too_much(self, a, log=None, MAX_TURN=8):
        """ terminate when the agent keeps turning around """
        if isinstance(a,int):
            a = self.actions[a]

        ## replan if the policy keeps doing the same action
        if 'turn' in a:
            self.consecutive_actions['turn'] += 1
        else:
            self.consecutive_actions['turn'] = 0

        if self.consecutive_actions['turn'] >= MAX_TURN / 2:
            self.replan = True
            if PRINT_REPLAN: print(self.player_name, f'!!! replan after turning too many times')

        elif self.consecutive_actions['turn'] >= MAX_TURN:
            self.replan = True
            if log != None: log(f'!!! terminated after turning too many times')
            return True

        return False

    def check_mission_finish(self, log=None):
        """ check termination """
        if self.countdown_real < min([self.player['costs'][a] for a in self.actions]):
            return True

        yellow_goal = self.player['rewards']['victim-yellow']<=0 or self.remaining_to_save['VV'] == []
        green_goal = self.player['rewards']['victim']<=0 or self.remaining_to_save['V'] == []
        if yellow_goal and green_goal:
            if log != None: log('\n\\\\\\\\\\\\\\\\\\\\ FOUND ALL VICTIMS /////////////')
            return True
        elif time.time() - self.start_time > 5000:
            if log != None: log(f'\n\\\\\\\\\\\\\\\\\\\\ timeout ={1000} seconds /////////////')
            return True
        return False

    def check_done(self, s, a):
        return self.check_mission_finish()

    """
        macro level transition function and reward function
            * macros: ((tile, head), type), e.g. ((345, 90), 'obs')
            * types of macros:
                - 'obs': observing the whole room that the door state leads to
                - 'reach': go to a door, or doorstep, or air for observing a corner
                - 'triage': go near to a victim and triage it
    """

    def get_actions_in_room(self, room, TRIAGE=True):
        room_tiles = self.rooms_truth[room]['tiles']
        doors = self.rooms[room]['doors']

        ## plan within room states
        room_states = [(tile, head) for tile in room_tiles for head in [0, 90, 180, 270]]
        available_actions = {state: ['go_straight', 'turn_left', 'turn_right'] for state in room_states}
        for neighbor in self.rooms_truth[room]['neighbors']:
            for adjacent in self.rooms_truth[room]['neighbors'][neighbor]:
                mydoor, yourdoor, heading = adjacent
                available_actions[(mydoor, heading)] = ['turn_left', 'turn_right']

                ## the door might belong to another room, but also connect this room
                if yourdoor in doors:
                    for h in [0, 90, 180, 270]:
                        if h == self.head_oppo[heading]:
                            available_actions[(yourdoor, h)] = ['turn_left', 'turn_right', 'go_straight']
                        else:
                            available_actions[(yourdoor, h)] = ['turn_left', 'turn_right']
        
        if TRIAGE:
            for k, v in available_actions.items():
                v.append('triage')
                available_actions[k] = v

        return available_actions

    def initiate_obs_path(self, room, verbose=False):
        paths = {}
        room_tiles = self.rooms_truth[room]['tiles']
        doors = self.rooms[room]['doors']  ## not just tiles of type door, but all those tiles connected to the outside that are inside of rooms
        available_actions = self.get_actions_in_room(room)

        ## plan from doors
        for door in doors: ## self.rooms_truth[room]['doors']:
            start_state = (door, self.get_entrance_heading(door, room)[0])
            room_name = self.get_room_name(room)
            if verbose: print(f'\nenter room {room_name} from door {door} to observe {len(room_tiles)} tiles')

            planner = planners.AStar(self.actions, self.T, self.R, self.get_pos, self.get_dist,
                                     gamma=self.player['gamma'],
                                     goal_state_action=room_tiles, obs_rewards=self.obs_rewards_init,
                                     available_actions=available_actions)
            planner.run((start_state, tuple([])))

            new_path = []
            if (door, room) in self.door2doorstep:
                doorstep = self.door2doorstep[(door, room)]
            elif (door, room) == (16, 1): ## special case in 6 by 6 where one doorstep serve two doors
                doorstep = 22
            else:
                print(door, room)

            new_path.append((doorstep, self.get_entrance_heading(door, room)[0]))
            new_path.extend(planner._path)
            # print(f' ----- room {room}   |   door {door}   |  path {new_path}' )
            planner._path = new_path
            if verbose: print(room, len(new_path), new_path)

            new_plan = ['go_straight']
            new_plan.extend(planner._plan)
            planner._plan = new_plan

            # paths[door] = {}
            paths[doorstep] = {
                'plan': planner._plan,
                'path': planner._path,
                'start_state': start_state,
                'end_state': planner._path[-1],
                'room_name': room_name,
            }
        return paths

    def initiate_obs_paths(self, rooms=None):
        ## initiate observation paths
        if rooms == None: rooms = self.rooms

        paths_file = join('maps', 'paths', self.MAP.replace('.csv', '_obs.json'))
        ## read from saved file
        if isfile(paths_file):
            with open(paths_file) as f:
                self.obs_paths = {}
                for k, v in json.load(f).items():
                    v['path'] = [tuple(s) for s in v['path']]
                    v['start_state'] = tuple(v['start_state'])
                    v['end_state'] = tuple(v['end_state'])
                    self.obs_paths[int(k)] = v
        ## calculate at the beginning
        else:
            paths = {}
            for room in rooms:
                if self.room_is_room(room):
                    paths.update(self.initiate_obs_path(room))
            self.obs_paths = paths
            with open(paths_file, 'w') as json_file:
                json.dump(paths, json_file, cls=utils.NpEncoder)

        return self.obs_paths

    def get_obs_path(self, s, room, ROOMONLY=False):
        """ get the path to observe the remaining part of a room """

        ## observe tiles inside the room
        room_tiles = self.rooms[room]['tiles']
        unobserved_tiles = self.unobserved_in_rooms[room]

        # saved
        if (s, tuple(unobserved_tiles)) in self.obs_paths:
            return self.obs_paths[(s, tuple(unobserved_tiles))]

        room_states = [(tile, head) for tile in room_tiles for head in [0, 90, 180, 270]]
        available_actions = {state: ['go_straight', 'turn_left', 'turn_right'] for state in room_states}
        for neighbor in self.rooms[room]['neighbors']:
            for adjacent in self.rooms[room]['neighbors'][neighbor]:
                available_actions[(adjacent[0], adjacent[2])] = ['turn_left', 'turn_right']

        ## not outside the room
        other_states = [(tile, head) for tile in self.tilesummary if tile not in room_tiles for head in [0, 90, 180, 270]]
        available_actions.update({state: ['turn_left', 'turn_right'] for state in other_states})

        ## also care about triaging the victims if solving room level orienteering problem
        if ROOMONLY:
            available_actions_new = {}
            for k, v in available_actions.items():
                v.append('triage')
                available_actions_new[k] = v
            available_actions = available_actions_new

        planner = planners.AStar(self.actions, self.T, self.R, self.get_pos, self.get_dist,
                                 gamma=self.player['gamma'], timeout=0.5,
                                 goal_state_action=unobserved_tiles, obs_rewards=self.obs_rewards_init,
                                 available_actions=available_actions)
        if ROOMONLY:
            # planner._room_tiles = room_tiles
            planner._timeout = 10
        planner.run((s, tuple([])))
        path = planner._path
        self.obs_paths[(s, tuple(unobserved_tiles))] = path
        return path

    def get_entrance_heading(self, door_tile, room):
        door_room = self.tiles2room_truth[door_tile]
        for neighbor, adjacents in self.rooms_truth[room]['neighbors'].items():
            for adjacent in adjacents:
                if (door_room == room and adjacent[0] == door_tile) or (door_room != room and adjacent[1] == door_tile):
                    return self.head_oppo[adjacent[2]], adjacent[2]
        print(door_tile, door_room, room, self.rooms_truth[room]['neighbors'])
        return None

    def get_path_between(self, initial, end, USE_SAVED=False, verbose=False):
        # self.paths = {}

        if USE_SAVED:
            paths_file = join('maps', 'paths', self.MAP.replace('.csv', '_macros.json'))

            ## read from saved file
            if isfile(paths_file):
                with open(paths_file) as f:
                    self.paths = json.load(f)

        ## just calculated something similar in the last five steps
        for k, v in self.paths.items():
            if k[1] == end and initial in v['path'][:5] and self.step-v['step'] < 5:
                index = v['path'][:5].index(initial)
                if verbose: print(f"!! step {self.step} from {initial} to {end}, reuse a path from step {v['step']} from {k[0]} to {end} after index {index}")
                v['path'] = v['path'][index:]
                v['plan'] = v['plan'][index:]
                v['start_state'] = initial
                v['step'] = self.step
                self.paths[str((initial, end))] = v
                break

        if str((initial, end)) in self.paths:
            v = self.paths[str((initial, end))]
            path = v['path']
            if verbose: print(f"step {self.step} from {initial} to {end}, reuse a path from step {v['step']}")


        ## plan shortest path
        else:
            start = time.time()
            planner = planners.AStar(self.actions, self.T, self.R, self.get_pos, self.get_dist,
                                gamma=self.player['gamma'], goal_state_action=(end, 'go_straight'))
            planner.run(initial, verbose=False)
            path = planner._path
            self.paths.update({
                str((initial, end)): {
                    'path': path,
                    'plan': planner._plan,
                    'start_state': initial,
                    'end_state': end,
                    'step': self.step
                }
            })

            # ## save the reversed path too
            # path_r = copy.deepcopy(path)
            # path_r.reverse()
            # plan_r = self.path2plan(path_r)
            # self.paths.update({
            #     str((end, initial)): {
            #         'path': path_r,
            #         'plan': plan_r,
            #         'start_state': end,
            #         'end_state': initial,
            #         'step': self.step
            #     }
            # })

            if verbose: print(f"step {self.step} from {initial} to {end}, computing a path using {round(time.time()-start, 3)} seconds")
            if USE_SAVED:
                with open(paths_file, 'w') as json_file:
                    json.dump(self.paths, json_file, cls=utils.NpEncoder)

        return path

    def achieved_macro(self, macro, countdown, s, MSG=False):
        # self.replan = True
        # if PRINT_REPLAN: print('!!! Replan after achieving macros')
        if PRINT_REPLAN: print(f'\n {self.player_name} achieved macro {macro} at {countdown}, MSG = {MSG}' )

        ## add some explanation to the macro
        type, value = macro
        # if type == 'doorstep':
        #     room = self.bark_positions[value][0]
        #     goal = f'doorstep to room {room} ({self.get_room_name(room)})'
        goal = 'what?'
        if type == 'explore':
            room = value
            goal = f'explore room {room} ({self.get_room_name(room)}) fully'

        elif 'victim' in type:
            room = self.tiles2room[value]
            goal = f'triage {type} in room {room} ({self.get_room_name(room)}) at {value}'

        ## evaluate whether agent predictions mean the same macro action
        correct_predictions = []
        for cd, mc_value in self.proposed_macros.items():
            for mc, data in mc_value.items():
                if data['goal_general'] == macro:
                    correct_predictions.append(mc)

                # mc_type = data['goal_type']
                # mc_tile = data['goal_tile']
                # mc_room = data['goal_value']
                #
                # # if type == mc_type == 'doorstep':
                # #     doorsteps = [t for t in self.rooms[mc_room]['doorsteps'] if self.get_dist(t, value)<=3]
                # #     if value in doorsteps:
                # #         correct_predictions.append(mc)
                #
                # if type == mc_type == 'explore':
                #     if value == mc_room:
                #         correct_predictions.append(mc)
                #
                # elif type == mc_type and 'victim' in mc_type:  ## victim
                #     if mc_room == value:
                #         correct_predictions.append(mc)

        self.achieved_macros[macro] = {
            'countdown': countdown,
            'goal': goal,
            'correct_predictions': [str(v) for v in set(correct_predictions)],
            'proposed': {k:{str(kk): vv for kk, vv in v.items()} for k, v in self.proposed_macros.items()}
        }

        ## generate json file
        macros_dir = join('recordings', 'test_achieved_macro', 'json')
        if not isdir(macros_dir): os.mkdir(macros_dir)
        with open(join(macros_dir, f'{self.trial_name} Planner-{self.player_name}.json'), 'w') as f:
            json.dump({str(k):v for k, v in self.achieved_macros.items()}, f, cls=utils.NpEncoder, indent=4)

        ## generate csv file
        macros_dir = join('recordings', 'test_achieved_macro', 'csv')
        macros_csv = join(macros_dir, f'{self.trial_name} Planner-{self.player_name}.csv')
        if not isdir(macros_dir): os.mkdir(macros_dir)
        if len(self.achieved_macros) == 1 and isfile(macros_csv): os.remove(macros_csv)
        with open(macros_csv, 'a') as times_file:
            times_writer = csv.writer(times_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

            if len(self.achieved_macros) == 1:
                titles = ['Countdown', 'Macro', 'Macro (explained)', 'Countdown at proposal',
                          'Proposed macro', 'Proposed macro (explained)', 'Proposed value', 'Result']
                times_writer.writerow(titles)

            for cd, mc_value in self.proposed_macros.items():
                for mc, data in mc_value.items():
                    row = [countdown, macro, goal, cd, (data['goal_type'], mc), data['goal'], data['q'], (mc in correct_predictions)]
                    times_writer.writerow(row)

        if not MSG:
            self.proposed_macros = {}

    def path2plan(self, path, macro=None, verbose=False):
        plan = []
        to_print = f'{path[0]}'
        for i in range(1, len(path)):
            old = path[i-1]
            new = path[i]
            skip = False

            if old[0] != new[0] and old[1] == new[1]:
                plan.append('go_straight')
                to_print += ' ^'
            elif old[1] + 90 == new[1] or old[1] - 270 == new[1]:
                plan.append('turn_left')
                to_print += ' <'
            elif old[1] - 90 == new[1] or old[1] + 270 == new[1]:
                plan.append('turn_right')
                to_print += ' >'
            elif old == new: ## sometimes two states are the same
                skip = True
            elif old[0] == new[0] and abs(old[1] - new[1]) == 180:
                plan.append('turn_right')
                plan.append('turn_right')
            else:
                print('\n\n\n what is this !!!!', old, new)

            if not skip: to_print += f' {new}'

        # type = self.tilesummary[macro[0]]['type']
        # # if 'victim' in type:
        if self.triageable(path[-1]):
            plan.append('triage')
            to_print += f' #'
        # if verbose:
        #     print(f'Executing macro {macro} [{type}]: {len(path)} states in {len(plan)} actions', to_print)
        return plan

    def obs_in_path(self, path, old_obs_tiles=[]):
        new_obs_tiles = []
        for state in path:
            for tile in self.obs_rewards_init[state]:
                if tile not in self.observed_tiles and tile not in new_obs_tiles and tile not in old_obs_tiles:
                    new_obs_tiles.append(tile)
        return new_obs_tiles

    def macro_T(self, s, a):
        macro, history, countdown = s
        history = list(history)
        history.append(macro)
        cost = self.macro_c(a, tuple(history))
        # print(f'  history = {history}   |  a = {a}   |  countdown = {countdown}  |  c = {cost}')
        countdown -= cost
        return (a, tuple(history), countdown)

    def get_macro_path(self, last_macro, macro):
        if isinstance(last_macro[-1], tuple): ## actual macro, not just state
            last_macro = last_macro[-1]
        if (last_macro, macro) not in self.macro_paths:
            print('(last_macro, macro) not in self.macro_paths')
        return self.macro_paths[(last_macro, macro)]

    def get_last_path(self, macro, history):
        last_macro = list(history)
        if isinstance(last_macro[-1], tuple): ## only one macro in history
            last_macro = last_macro[-1]
        return self.get_macro_path(last_macro, macro)

    def macro_c(self, macro, history, reward=None, verbose=False):
        if reward == None:
            reward = self.macro_r(macro, history)

        path = self.get_last_path(macro, history)
        plan = self.path2plan(path, macro)
        if verbose: print(plan)

        cost = 0
        type, value, end_state = macro

        ## cost of confirmed triage
        for a in plan:
            if a == 'triage':
                if 'yellow' in type: cost += self.player['costs']['victim-yellow']
                elif 'victim' in type: cost += self.player['costs']['victim']
            else:
                cost += self.player['costs'][a]

        ## cost of potential triage and travel
        if type == 'explore' and reward != 0:
            room = self.rooms[value]
            walking_cost = abs(room['right'] - room['left']) + abs(room['bottom'] - room['top'])
            walking_cost *= self.player['costs']['go_straight']
            cost += (reward/2 + walking_cost)

        return cost

    def macro_r(self, macro, history):
        ## find out what rewards have been taken
        # tiles_in_history = set([t[0] for t in history])
        # triageables_in_history = set([self.state2macro[h][1] for h in history
        #                               if h in self.state2macro and 'victim' in self.state2macro[h][0]])
        reward = 0
        observed_in_history = []
        for i in range(1, len(history)):
            path = self.get_macro_path(history[i - 1], history[i])
            new_obs_tiles = self.obs_in_path(path, old_obs_tiles=observed_in_history)
            observed_in_history.extend(new_obs_tiles)

        if isinstance(macro[0], str):  ## new macro actions
            type, value, state = macro
            room_explored, victims_triaged = self.get_explored_triaged(history)

            if 'victim' in type and value not in victims_triaged:
                reward = self.tilesummary[value]['reward']

            elif type == 'explore' and value not in room_explored:
                room_tiles = self.rooms[value]['tiles']
                new_obs_tiles = self.obs_in_path(self.get_last_path(macro, history), old_obs_tiles=observed_in_history)
                new_obs_tiles = [t for t in new_obs_tiles if t in room_tiles]
                reward = sum([self.tilesummary[t]['reward'] for t in new_obs_tiles])

        elif isinstance(macro[0], int):
            ## find out what reward can be taken this step
            type, tile = self.macro2tile(macro)
            # if macro[0] != tile:
            #     print(macro[0], tile, type)

            ## the reward
            # if type == 'doorstep' and tile not in tiles_in_history and tile not in self.visited_tiles:
            #     reward = 0  ## self.player['information_reward']

            if 'victim' in type and tile not in triageables_in_history: ## and countdown > 0:
                reward = self.tilesummary[tile]['reward']
                # print(f" ------- reward for victim is {reward}, instead of action {self.player['rewards'][type]}")

            elif self.tile_in_room(tile):
                room = self.tiles2room[tile]
                room_tiles = self.rooms[room]['tiles']
                new_obs_tiles = self.obs_in_path(self.get_last_path(macro, history), old_obs_tiles=observed_in_history)
                new_obs_tiles = [t for t in new_obs_tiles if t in room_tiles]
                reward = sum([self.tilesummary[t]['reward'] for t in new_obs_tiles])
                # sum_tiles = [t for t in room_tiles if t not in self.observed_tiles]
                # sum_reward = sum([self.tilesummary[t]['reward'] for t in sum_tiles])
                # print(f'\n============= sum of unobserved rewards in room {room} is {reward} ({sum_reward})')
                # print(set(sum_tiles) - set(new_obs_tiles))

        # if self._pos_agent_room == macro[1] == 6 and macro[0] == 'explore':
        #     print('(explore, 6, (550, 270))',reward)

        return reward  ##, tile, type

    def macro_U(self, state, a, verbose=False):
        s = self.macro_T(state, a)
        macro, history, countdown = s
        reward = self.macro_r(macro, history)  ## , tile, type
        cost = self.macro_c(macro, history, reward=reward) ## , tile=tile, type=type, verbose=verbose

        ## save for debug {new_obs_tiles}
        stats = f'{history}  ->  {macro} [{type}]  |  reward: {round(reward, 3)} |  cost: {round(cost, 3)}   -> remainiing time {round(s[2], 3)}'
        if self.step not in self.saved_macro_U:
            self.saved_macro_U[self.step] = {}
        self.saved_macro_U[self.step][(history, a)] = stats
        if verbose:
            space = ''.join([' '] * len(history) * 6)
            print(f'{space}-------------------')
            print(f'{space}{stats}')
        return reward

    def get_observed(self, visited_states):
        ## return the list of observed tiles and the new tiles observed in each new state
        observed_tiles = []
        observed_summary = {}
        for state in visited_states:
            observed_summary[state] = list( set(self.obs_rewards[state]) - set(observed_tiles) )
            observed_tiles.extend(self.obs_rewards[state])
        return set(observed_tiles), observed_summary

    def get_explored_triaged(self, history):
        room_explored = [h[1] for h in history if h[0] == 'explore']
        room_explored.extend([h[1] for h in self.achieved_macros if h[0] == 'explore'])
        room_explored.extend([h[1] for h in self.achieved_macros if h[0] == 'explore'])
        room_explored = set(room_explored)

        victims_triaged = [h[1] for h in history if 'victim' in h]
        return room_explored, victims_triaged

    def get_closest_doors(self, s, num=5, room_explored=set([]), ROOMONLY=False):

        ## if in roomplanner (ROOMS=True), choose from extended neighbors
        ## if in macroplanner, choose from rooms that's not fully explored
        candidate_roomdoor = []  ## (room, (door, head_in))
        current_room = self.tiles2room[s[0]]
        checked_areas = [current_room]
        queue = []
        def add_neighbors(room):
            added = []
            ## the first round, make sure all doors are added
            for neighbor, adjs in self.rooms[room]['neighbors'].items():
                for adj in adjs:
                    mydoor, yourdoor, heading = adj
                    if yourdoor in self.rooms[neighbor]['doors']:
                        if neighbor not in added: added.append(neighbor)
                        if neighbor not in checked_areas:
                            hq.heappush(queue, (neighbor, (yourdoor, heading)))
            ## the second round, make sure one neighboring corridor is added
            for neighbor, adjs in self.rooms[room]['neighbors'].items():
                if neighbor not in added and neighbor not in checked_areas:
                    mydoor, yourdoor, heading = adjs[0]
                    hq.heappush(queue, (neighbor, (yourdoor, heading)))
        add_neighbors(current_room)
        while len(queue) > 0:
            neighbor, (yourdoor, heading) = hq.heappop(queue)
            checked_areas.append(neighbor)
            found = False
            if self.room_is_room(neighbor):
                if (ROOMONLY and neighbor not in self.observed_rooms and neighbor not in room_explored) or (not ROOMONLY and neighbor not in self.fully_explored_rooms):
                    candidate_roomdoor.append((neighbor, (yourdoor, heading)))
                    found = True
            if not found:
                add_neighbors(neighbor)

        ## return the nearest rooms and paths_to_doors = { room: path }
        paths_to_doors = {}
        for room, (door, heading) in candidate_roomdoor:
            path = self.get_path_between(s, (door, heading))
            if room not in paths_to_doors or len(path) < len(paths_to_doors[room]):
                paths_to_doors[room] = path

        paths_to_doors = {k: v for k, v in sorted(paths_to_doors.items(), key=lambda item: len(item[1]))}
        top_five = list(paths_to_doors.keys())[:min(num, len(paths_to_doors))]

        # print('\n+++++++++++ candidate room door', candidate_roomdoor)
        return top_five, paths_to_doors

        # else:  ## MacroPlanner, next_rooms = {room: path}
        #     next_rooms = {}  ## {room: path}
        #     candidate_roomdoor = [(r, d) for (r, (d, h)) in candidate_roomdoor]
        #     for doorstep in list(self.bark_positions.keys()):
        #         room, door, bark = self.bark_positions[doorstep]
        #         if (room, door) in candidate_roomdoor:  ## self.get_dist(doorstep, s[0]) < range and
        #             outer_path = self.get_path_between(s, self.get_goal_state(doorstep, s))
        #             if len(outer_path) >= 2 and outer_path[-2][0] == doorstep: outer_path = outer_path[:-1]
        #
        #             ## only those rooms that are next in line
        #             passed_rooms = set([self.tiles2room[t[0]] for t in outer_path])
        #             unwanted_rooms = [current_room, room]
        #             unwanted_rooms.extend(self.observed_rooms)
        #             passed_rooms = [r for r in passed_rooms if r not in unwanted_rooms and self.room_is_room(r)]
        #             if len(passed_rooms) > 0: continue
        #
        #             ## only one shortest path chosen to enter a room
        #             if room not in next_rooms or len(outer_path) < len(next_rooms[room]):
        #                 next_rooms[room] = outer_path
        #
        #     next_rooms = {k: v for k, v in sorted(next_rooms.items(), key=lambda item: len(item[1]))}
        #     top_five = list(next_rooms.keys())[:min(num, len(next_rooms))]
        #     return top_five, next_rooms

    def get_macro_children(self, s, history=[], verbose=False, update_path=True, ROOMONLY=False):
        """ return a dict of next macro and shortest path to get there
                macro:
                    ('explore', room, state_to_end): including the current room
                    ('victim', victim_tile, state_to_triage)
                always include paths to go into and explore 5 nearest rooms
                if inside a room, can choose to observe or triage

        """

        if isinstance(s[-1], tuple):  ## input can be state or macro
            history = list(history)
            history.append(s)
            s = s[-1]
        current_room = self.tiles2room[s[0]]
        range = 40
        children = {}

        room_explored, victims_triaged = self.get_explored_triaged(history)

        ## go to the doorstep
        top_five, paths_to_doors = self.get_closest_doors(s, room_explored=room_explored, ROOMONLY=ROOMONLY)

        ## go inside the chosen door
        for room in top_five:

            outer_path = paths_to_doors[room]
            doorstep = outer_path[-2][0]
            outer_path = outer_path[:-1]
            while doorstep not in self.bark_positions:
                if len(outer_path) < 2:
                    # print(f'\n ++++ strange \npaths_to_doors[room] = {paths_to_doors[room]} \nouter_path = {outer_path} \ndoorstep = {doorstep}')
                    doorstep = self.tilesummary[doorstep][self.head_oppo[outer_path[-1][1]]]
                    outer_path = []
                    # print(f'doorstep new = {doorstep}')
                else:
                    doorstep = outer_path[-2][0]
                    outer_path = outer_path[:-1]
                    # print(f'doorstep new = {doorstep}')
                    # print(f'outer_path new = {outer_path}')
            # if doorstep == 180:
            #     print()
            # room, door, bark = self.bark_positions[doorstep] ## cause problem to 6 by6

            if doorstep not in self.obs_paths:
                # print(f'planning path to observe room {room} from doorstep {doorstep}')
                self.obs_paths.update(self.initiate_obs_path(room))
            room_path = self.obs_paths[doorstep]['path']

            ## if already on door
            if s[0] in self.rooms[room]['doors']:
                total_path = room_path[1:]

            ## first go to doorstep and then visit
            else:
                total_path = copy.deepcopy(outer_path)
                if len(outer_path) >= 2 and room_path[0] == outer_path[-2]:
                    total_path = total_path[:-2]
                elif room_path[0] == outer_path[-1]:
                    total_path = total_path[:-1]
                total_path.extend(room_path)

            if total_path != [s]:
                children[('explore', room, room_path[-1])] = total_path

        ## if inside a room, observe the rest of room after going to the doorstep
        if self.room_is_room(current_room) and current_room not in room_explored:
            path = self.get_obs_path(s, current_room, ROOMONLY=ROOMONLY)
            children[('explore', current_room, path[-1])] = path

        ## if an observed untriaged victim is within 20 step range
        victims = []
        for type in ['V', 'VV']:
            victims.extend([v for v in self.remaining_to_save[type] if
                            v not in self.remaining_to_see[type] and v not in victims_triaged])

        for victim in victims:
            goal_state = self.get_goal_state(victim, s)
            victim_type = self.tilesummary[victim]['type']
            if self.get_dist(victim, s[0]) < range and s != goal_state:
                children[(victim_type, victim, goal_state)] = self.get_path_between(s, goal_state)

        path = list(history)
        path.append(s)
        if verbose: print(f' ... {path} |   children {list(children.keys())}')

        # self.state2macro.update({c[2]: c for c in children})
        # children = {k[2]:v for k,v in children.items()}
        if update_path:
            self.macro_paths.update({(s, k): v for k, v in children.items()}) ##  if v[0] == s

        # if isinstance(history, list) and history[-1] == ('explore', 4, (37, 180)):
        #     print('mayiyahei', list(children.keys()))

        return children

    def get_bad_children(self, children, s):
        ### return a list of tiles
        bad_children = []
        for macro in children:
            tile = macro[0]
            type = self.tilesummary[tile]['type']

            ## no need to visit other doorsteps to get redundant info
            if type == 'doorstep':
                room = self.bark_positions[tile][0] ## self.tiles2room[tile]
                doorsteps = self.rooms[room]['doorsteps']
                # print(doorsteps)
                visited = False
                for doorstep in doorsteps:
                    if doorstep in self.visited_tiles:
                        visited = True
                if visited:
                    bad_children.extend(doorsteps)

            ## no need to go into rooms that have no victims in it
            elif type == 'air':
                room = self.tiles2room[tile]
                if self.rooms[room]['bark'] == 0:
                    bad_children.append(tile)

        return bad_children

    def get_macro_tiles(self):
        macros = []

        ## generating macros for observing unvisited rooms
        for door, notes in self.obs_paths.items():
            macros.append(notes['end_state'][0])

        ## generating macros for observing a corner remaining in the room
        ## both require findiing the shortest path to cover all tiles in obs
        # current_room = self.tiles2room[2]
        # self.unobserved_in_rooms[current_room]
        # planner = planners.AStar()
        # obs_path = planner.get_plan()

        ## generating macros for going to all doorsteps
        ## if tile not in self.visited_tiles: self.player['information_reward'] - walking_cost * to_order[m[1]]
        macros.extend(list(self.bark_positions.keys()))

        ## generating macros for going to all untriaged victims
        for type in ['V', 'VV']:
            macros.extend([v for v in self.remaining_to_save[type] if v not in self.remaining_to_see[type]])

        ## build a graph of macros
        self.macro_tiles = macros
        return macros

    """
        room level transition function and reward function
    """
    def get_room_actions(self, state, history=[]):
        room_actions = {}  ## {(room, door_entrance, door_exit): path}

        ## get the 5 closest doors
        children = self.get_macro_children(state, history, ROOMONLY=True)  ## , update_path=False
        children = {k:v for k, v in children.items() if k[0] == 'explore'}
        return children

        ## if the state space include the door to exit from
        for macro, path in children.items():
            _, room, end_state = macro

            ## ways to exit from room, end_state is doorstep of the neighboring room
            for neighbor, adjs in self.rooms[room]['neighbors'].items():
                for adj in adjs:
                    mydoor, yourdoor, head = adj
                    exit_state = (mydoor, head)
                    exit_path = self.get_path_between(end_state, exit_state)
                    new_path = copy.copy(path)[:-1]
                    new_path.extend(exit_path)
                    room_actions[('explore', room, exit_state)] = new_path
                        # print(f"state {state}  -->  room_action {('explore', room, exit_state)}")
                        # print(f'\nroom {room} enter from {entrance} exit from {door} has length {len(new_path)}')
                        # print(f'   enter path: {path}')
                        # print(f'   exit path: {exit_path}')

            # tiles = [s[0] for s in path]
            # doors = self.rooms[room]['doors']
            ## entrance = list(set(tiles).intersection(set(doors)))[0]
            # for door in doors:
            #     heads = self.get_entrance_heading(door, room)
            #     exit_state = (door, heads[1])
            #     exit_path = self.get_path_between(end_state, exit_state)
            #     new_path = copy.copy(path)[:-1]
            #     new_path.extend(exit_path)
            #     room_actions[('explore', room, exit_state)] = new_path
            #     # print(f"state {state}  -->  room_action {('explore', room, exit_state)}")
            #     # print(f'\nroom {room} enter from {entrance} exit from {door} has length {len(new_path)}')
            #     # print(f'   enter path: {path}')
            #     # print(f'   exit path: {exit_path}')
        for k, v in room_actions.items():
            s = state
            if isinstance(state[0], str): ## from macro to end state, e.g., ('explore', 1, (337, 90)) -> (337, 90)
                s = state[2]
            self.macro_paths.update({(s, k): v })
        return room_actions

    def roomlevel_T(self, s, a):
        """ transition function """
        return a

    def roomlevel_R(self, room_index, a = None):
        """ reward function for room-level value iteration """

        room = self.rooms[room_index]

        ## sum together the rewards in the next room
        reward = 0
        for tile in room['tiles']:
            reward += self.tilesummary[tile]['reward']

        ## minus the cost of travelling through the current room
        reward -= self.player['costs']['go_straight'] * ( (room['right']-room['left']) + (room['bottom']-room['top']) )

        room['roomlevel_R'] = reward
        return reward

    def roomlevel_U(self, current_room_idx, next_room_idx, walking_cost = 0.01, visited_room_idx=[]):
        """ reward function for room-level value iteration """

        current_room = self.rooms[current_room_idx]
        next_room = self.rooms[next_room_idx]

        ## sum together the rewards in the next room
        reward = 0
        if next_room_idx not in visited_room_idx:
            for tile in next_room['tiles']:
                reward += self.tilesummary[tile]['reward']

        ## minus the cost of travelling to the next room
        # walking_cost = self.player['costs']['go_straight']
        if next_room_idx not in current_room['actions']:
            return -1000
        cost = walking_cost * current_room['actions'][next_room_idx][0]

        ## raw utility
        utility = reward - cost
        if 'roomlevel_U' not in current_room:
            current_room['roomlevel_U'] = {}
            if next_room_idx not in current_room['roomlevel_U']:
                current_room['roomlevel_U'][next_room_idx] = utility

        # current_name, next_name = 'corridor', 'corridor'
        # if self.room_is_room(current_room_idx): current_name = 'room'
        # if self.room_is_room(next_room_idx): next_name = 'room'
        # print(f'------ from {current_name} {current_room_idx} to {next_name} {next_room_idx} ',
        #       'reward', reward, 'cost', cost)

        return utility

    def traversed_rooms(self, visited_rooms):
        for room in self.rooms:
            if self.room_is_room(room) and room not in visited_rooms and room not in self.fully_explored_rooms:
                return False
        return True

    def get_rooms(self):
        if '6by6' in self.MAP:
            return [0,1,4]
        if '12by12' in self.MAP:
            return [0,1,2, 6, 7, 9, 10, 11]
        elif '13by13' in self.MAP:
            return [0, 2, 3]
            # return [0,1,2,5]
        if 'test2' in self.MAP or '24by24_6.csv' in self.MAP:
            return [0,1,4,5,7,8]
        if 'test3' in self.MAP or '24by24_7.csv' in self.MAP:
            return [1,4,5,6,7,8,9]
        if 'test4' in self.MAP or '24by24_9.csv' in self.MAP:
            return [0,4,5,7,8,9,10,11]
        if 'test5' in self.MAP or '24by24_8.csv' in self.MAP:
            return [0,1,4,6,7,9,10]
        if '46by45' in self.MAP:
            return [1,2,3,5,12,13,14,15,16,17,18,19,27,28,29,30,31,32,33,34]
        if '48by89' in self.MAP:
            return [2,3,5,6,10,13,14,15,18,21,22,23,24,25,26,27,28,29,30,31,39,40,41,42]
        print('WARNING! please define the rooms in mdp.py -> get_rooms(self)')

    def room_is_room(self,room):
        return room in self.get_rooms()

    def tile_in_room(self,one):
        return self.room_is_room(self.tiles2room[one])

    def tile_is_door(self,one):
        return one in self.rooms[self.tiles2room[one]]['doors']

    def fully_explored(self, current_room, CHECK=False):

        if current_room in self.fully_explored_rooms and not CHECK: return True

        FULLY_OBSERVED = True
        ALL_NEIGHBORS_VISITED = True
        NO_REWARD_LEFT = True

        hopeless_rooms = [r for r in self.barked_rooms if self.barked_rooms[r]==0]

        ## more than 80% of the tiles have been observed #TODO: Make the 80% here part of player profile
        tiles_in_room = self.rooms[current_room]['tiles']
        unobserved_in_room = self.unobserved_in_rooms[current_room]
        if len(unobserved_in_room) >= len(tiles_in_room) * self.player['obs_thre']:
            FULLY_OBSERVED = False

        ## known rewards have all been collected
        for tile in tiles_in_room:
            if 'victim' in self.tilesummary[tile]['type']:
                NO_REWARD_LEFT = False

        ## if it's a dead end, has been to all of its neighbor at least once
        if len(self.rooms[current_room]['neighbors']) == 1:
            for neighbor in self.rooms[current_room]['neighbors']:
                if neighbor not in self.visited_rooms:
                    ALL_NEIGHBORS_VISITED = False
        ## if not a dead end, has explored or used device on all its neighbors except for one
        else:
            count = 0
            for neighbor in self.rooms[current_room]['neighbors']:
                if neighbor not in self.fully_explored_rooms and neighbor not in hopeless_rooms:
                    count += 1
            if count > 1:
                ALL_NEIGHBORS_VISITED = False

        result = (FULLY_OBSERVED and NO_REWARD_LEFT) and ALL_NEIGHBORS_VISITED
        self.rooms[current_room]['visit_status'] = {
            'FULLY_OBSERVED': FULLY_OBSERVED,
            'ALL_NEIGHBORS_VISITED': ALL_NEIGHBORS_VISITED,
            'NO_REWARD_LEFT': NO_REWARD_LEFT,
            'fully_explored': result
        }

        ## reset value assignedment after fully visiting the room
        if result:

            ## remove the node from roomlevel graph thus no longer go there
            if current_room not in self.fully_explored_rooms:
                self.fully_explored_rooms.append(current_room)

            if len(self.rooms[current_room]['neighbors'].keys()) == 1 and current_room not in self.removed_rooms:
                neighbor = list(self.rooms[current_room]['neighbors'].keys())[0]
                if current_room in self.roomlevel_actions[neighbor]:
                    self.roomlevel_actions[neighbor].pop(self.roomlevel_actions[neighbor].index(current_room))
                # self.roomlevel_states.remove(current_room)
                self.removed_rooms.append(current_room)

            print(f'!!!!!{current_room} is fully observed!!!!!')

        return result

    def available_actions(self):

        ## MCTS on rooms
        if visualize.ROOM_LEVEL_MCTS:
            return list(self.roomlevel_actions[self.tiles2room[self._pos_agent[0]]].keys())

        ## MCTS on all tiles
        else:
            actions = []  # 'turn_left','turn_right'

            ## delete actions that go into walls
            if self.tilesummary[self._pos_agent[0]][self._pos_agent[1]] != 'wall':
                actions.append('go_straight')

            ## delete actions that turn towards walls
            next_state = self.T(self._pos_agent, 'turn_left')
            if self.tilesummary[next_state[0]][next_state[1]] != 'wall':
                actions.append('turn_left')
            next_state = self.T(self._pos_agent, 'turn_right')
            if self.tilesummary[next_state[0]][next_state[1]] != 'wall':
                actions.append('turn_right')

            ## ensure there's at least one kind of turning to prevent the corner case
            if 'turn_left' not in actions and 'turn_right' not in actions:
                actions.append(random.choice(['turn_left', 'turn_right']))

        return actions

    def get_macro_actions(self, s, macro_actions, roomlevel_states, roomlevel_tiles):

        type_to_action = {
            'victim': 'triage',
            'victim-yellow': 'triage',
            'door': 'open',
            'doorstep': 'use device',
            # 'gravel': 'break',
            # 'fire': 'put off'
        }

        on_boundary = {}
        potential_obs = {}
        last_macro_actions = copy.deepcopy(macro_actions)

        ## go interact
        for state in roomlevel_states:
            tile = state[0]
            type = self.tilesummary[tile]['type']

            ## Action type 2: special interactions
            if type not in ['wall', 'gravel', 'fire', 'air']:
                # macro_actions[tile] = type_to_action[self.tilesummary[tile]['type']]
                goal_state = self.get_goal_state(tile, s)
                # print(f'considering macro-action of type {type}')

                ## skip those unwanted actions, but actually need it for inference
                # if (type == 'victim' and 1 not in self.wanted_barks) or (type == 'victim-yellow' and 2 not in self.wanted_barks):
                #     continue

                # ## too far away
                # if self.get_dist(tile, s[0]) > 20:
                #     continue

                macro_actions[(tile, goal_state[1])] = (type_to_action[self.tilesummary[tile]['type']], goal_state)

            ## Action type 3: go and observe
            elif type not in ['wall', 'gravel']:
                ## if tile has been observed but at least one of its neighbors hasen't been
                tile_types = [self.tilesummary[t]['type'] for t in self.obs_rewards[state]]
                observed_interesting = len(set(tile_types).intersection(set(['door', 'victim', 'victim_yellow']))) > 1
                expected_value = sum([self.tilesummary[e]['reward'] for e in self.obs_rewards[state]])

                if observed_interesting or len(self.obs_rewards[state]) > 3: ##
                    if observed_interesting: print('!!!!!!!!! observe something interesting')

                    ## cached tile to save computations
                    if state[0] in on_boundary:
                        answer = on_boundary[state[0]]
                    else:
                        answer = state[0] in self.observed_tiles and self._check_on_boundary(state[0])
                        on_boundary[state[0]] = answer

                    if answer or observed_interesting:
                        potential_obs[state] = expected_value ## len(self.obs_rewards[state]) ## self.get_obs_reward(state)
                        # pos = self.tilesummary[state[0]]['row'], self.tilesummary[state[0]]['col']
                        # print(f'------------ added {state} at {pos} with {len(self.obs_rewards[state])}')

        ## remove the macroactions for and for objects already interacted with
        to_remove = []
        for state in macro_actions:
            tile, head = state
            type = self.tilesummary[tile]['type']
            this_room = self.tiles2room[tile]

            ## where action has been taken
            if tile in self.victims_saved['V'] or tile in self.victims_saved['VV']:
                to_remove.append(state)

            ## where the room will never be entered again
            elif this_room in self.fully_explored_rooms:
                to_remove.append(state)

            ## where it's for observations
            elif type == 'air':  ## and tile in roomlevel_tiles
                to_remove.append(state)

            ## where it's for information and it has been visited
            elif type == 'doorstep' and tile in self.visited_tiles:
                to_remove.append(state)

            ## where it's for entering a room and it has been fully explored
            elif type == 'door' and this_room in self.fully_explored_rooms:
                to_remove.append(state)

            ## where the room is barked and not what the player wants
            elif type == 'door' and this_room in self.barked_rooms:
                if self.barked_rooms[this_room] not in self.wanted_barks:
                    to_remove.append(state)

        for state in to_remove:
            macro_actions.pop(state)

        ## go observe at most 3 tiles
        if len(potential_obs) > 0:
            potential_obs = {k: v for k, v in sorted(potential_obs.items(), key=lambda item: item[1])}
            keys = list(potential_obs.keys())
            keys.reverse()
            for i in range(min(20, len(keys))):
                macro_actions[keys[i]] = ('obs', keys[i])
                # print(f'to observe {keys[i]} of length {len(self.obs_rewards[keys[i]])} of reward {self.get_obs_reward(keys[i])}')

        ## Action type 1: turn to observe
        for head in [0, 90, 180, 270]:
            turn_state = (s[0], head)
            if s[1] != head:
                if len(self.obs_rewards[turn_state]) > 4:
                    # print('!!! observe by turning')  ## TODO
                    macro_actions[turn_state] = ('obs', turn_state)

        if len(macro_actions) == 0:
            macro_actions = last_macro_actions

        ## manually remove bad goals that don't suit the human player
        if not hasattr(self, 'bad_goals'): self.set_bad_goals()
        to_pop = []
        for state, (action, goal_state) in macro_actions.items():
            for tile in [state[0], goal_state[0]]:
                if tile in self.bad_goals and len(self.bad_goals[tile]) >= 3:
                    if state not in to_pop:
                        to_pop.append(state)
                        # print(f'!!!!!!! {self.player_name} popped macro_action: {state}')
        for state in to_pop:
            macro_actions.pop(state)

        return macro_actions

    def get_goal_state(self, tile, s):
        """ get the end state given goal tile and current state """

        def loc_to_head(loc):
            j, i = loc
            if j < i and j > -i:
                head = 0
            elif j >= i and j >= -i:
                head = 270
            elif j < -i and j > i:
                head = 180
            elif j <= i and j <= -i:
                head = 90
            return head

        type = self.tilesummary[tile]['type']

        ## goals are different for triaging and other arriving
        if 'victim' in type:

            locations = []
            for i in range(-2, 3):
                for j in range(-2, 3):
                    if abs(i) + abs(j) <=2:
                        locations.append((i, j))

        else:
            locations = [(-1, 0), (1, 0), (0, -1), (0, 1)]

        ## find all possible locations to triage it
        nearest_tile = None
        nearest_loc = None
        nearest_dist = np.inf
        x, y = self.tilesummary[tile]['row'], self.tilesummary[tile]['col']
        for i, j in locations:
            if (x + i, y + j) in self.tile_indices:
                tile_p = self.tile_indices[(x + i, y + j)]  ## action location

                ## goal tile must be observable from the action location
                head_at_goal = loc_to_head((-i, -j))
                if not tile in self.obs_rewards_truth[(tile_p, head_at_goal)]:
                    continue

                if self.tilesummary[tile_p]['type'] != 'wall' and self.get_dist(tile_p, s[0]) < nearest_dist:
                    if len(self.get_path_between((tile_p, head_at_goal), (tile, head_at_goal))) <= 2:
                        nearest_dist = self.get_dist(tile_p, s[0])
                        nearest_tile = tile_p
                        nearest_loc = (-i, -j)

        ## get the orientation of triaging
        if nearest_loc == None:
            head = 0
        else:
            head = loc_to_head(nearest_loc)

        state = (tile, head)
        if 'victim' in type:
            state = (nearest_tile, head)

        return state

    def prioritize_actions(self, macro_actions, s, next_room, beta=1, PRINT=False):
        current_room = self.tiles2room[s[0]]
        actions = {}
        for state, (type, goal_state) in macro_actions.items():

            ## determine the action to do at location
            tile, head = state
            action = 'go_straight'
            if type == 'triage':
                action = 'triage'

            ## calculate the reward of taking the macro-action
            obs_reward = self.get_obs_reward(goal_state)
            sure_reward = self.tilesummary[tile]['reward']
            goal_type = self.tilesummary[tile]['type']
            if 'victim' in goal_type:
                sure_reward *= self.player['certainty_boost_factor']

            dist = self.get_dist(s[0], goal_state[0])
            cost = dist * self.player['costs']['go_straight']
            reward = sure_reward + obs_reward
            gamma = self.player['gamma']
            reward_discounted = reward * gamma ** dist

            boosted = False
            tile_room = self.tiles2room[tile]
            ## if player is in a room, get a boost for the goals in the current room if player hasn't fully observe it
            if self.room_is_room(current_room):
                ## when the tile is marked as in the same room
                if tile_room == current_room:
                    if current_room not in self.fully_explored_rooms and self.tilesummary[tile]['type'] != 'door':
                        boosted = True

                ## they are actually the same room but not marked so in the _rooms.csv - redundant for new room maps
                elif current_room in self.rooms[tile_room]['neighbors']:
                    if len(self.rooms[tile_room]['neighbors'][current_room]) >= 3:
                        boosted = True

            ## if player is not in a room, get a boost for the goals in the next room
            else:
                if tile_room == next_room:
                    boosted = True

            actions[(goal_state, action)] = {
                'V': [reward, cost, dist, reward_discounted, boosted, None],
                'goal_type': goal_type,
                'goal_tile': state[0],
                'destination_tile': goal_state[0],
                'dist': int(dist),
                'action': action,
            }
            # print(self.get_pos(tile), ':')
            # self.get_priority_score([reward, - cost], beta)

        def r(n):
            return round(n,3)

        actions = {k: v for k, v in sorted(actions.items(), key=lambda item: self.get_priority_score(item[1], beta))}
        keys = list(actions.keys())
        keys.reverse()

        prioritized_actions = {}
        cap = 10
        for k in keys:
            if cap == 0: break
            cap -= 1
            prioritized_actions[k] = actions[k]
        # prioritized_actions = {k: actions[k] for k in keys} ## if not (k[0][0]==s[0] and k[1]=='go_straight')
        if PRINT:
            for goal, v in prioritized_actions.items():
                reward, cost, dist, reward_discounted, boosted, score = v
                (tile, head), action = goal
                goal = (self.get_pos(tile), head), action
                print(f'   {goal}:  score = {r(score)}, dist = {r(dist)}, reward = {r(reward)}, cost = {r(cost)}, '
                      f'reward_discounted = {r(reward_discounted)}, boosted = {boosted}')

        prioritized_actions = self.remove_bad_goals(prioritized_actions)
        return prioritized_actions

    def get_priority_score(self, reward_cost, beta=0.05):
        reward, cost, dist, reward_discounted, boosted, _ = reward_cost['V']

        # score = reward * beta + cost
        # score = reward * beta
        # score = self.player['tilelevel_gamma']**(-cost) * reward

        ## boost reward by 10 if it's in the current room
        # score = reward * self.player['certainty_boost_factor']**boosted  - cost
        score = reward_discounted * self.player['certainty_boost_factor']**boosted  - cost
        # print('______', round(dist), round(reward_discounted, 3), round(cost, 3), boosted, score)
        reward_cost['V'][-1] = score
        reward_cost['score'] = score
        reward_cost['name'] = f"step {self.step} {reward_cost['goal_type']} {reward_cost['dist']}"
        return score

    def remove_bad_goals(self, prioritized_actions):

        ## manually remove bad goals that don't suit the human player
        to_pop = []
        for state_action, value in prioritized_actions.items():
            tile = state_action[0][0]
            if tile in self.bad_goals and len(self.bad_goals[tile]) >= 3:
                to_pop.append(state_action)
        for state_action in to_pop:
            prioritized_actions.pop(state_action)
            # print('\n\n\n\n!!!!!!! popped macro_action:', state_action)
        return prioritized_actions

    def set_bad_goals(self, bad_goals=None):
        if bad_goals == None: bad_goals = {}

        # ## not remembering bad goals for too long
        # to_pop = []
        # for goal, those_times in bad_goals.items():
        #     if those_times[-1] - countdown > 10:
        #         to_pop.append(goal)
        # for to in to_pop:
        #     bad_goals.pop(to)
        # print('??? bad_goals:', bad_goals)

        self.bad_goals = bad_goals

    def reset_temperary_reward(self, temperary_rewards=None):
        if temperary_rewards == None: temperary_rewards = self.temperary_rewards
        for tile in temperary_rewards:
            self.tilesummary[tile]['reward'] = temperary_rewards[tile]
        temperary_rewards = {}
        return temperary_rewards

    def add_temperary_reward(self, s, room, neighbor, mydoor, yourdoor, next_room, temp, rooms=None):
        if rooms != None: self.rooms = rooms

        if yourdoor not in self.temperary_rewards:
            self.temperary_rewards[yourdoor] = self.tilesummary[yourdoor]['reward']

        self.tilesummary[yourdoor]['reward'] += temp
        pos = self.tilesummary[yourdoor]
        pos = (pos['row'], pos['col'])
        # print(f'....... added {temp} to {pos}')
        self.rooms[room]['neighbors_temp_reward'][neighbor] = (yourdoor, temp)

        # ## give extra reward to the tile leading to the next room but far away
        # if neighbor == next_room and self.get_dist(mydoor, s[0]) > 5:
        #     self.tilesummary[yourdoor]['reward'] *= self.player['goal_boost_factor']
        #     temp = self.tilesummary[yourdoor]['reward'] - self.temperary_rewards[yourdoor]
        #     self.rooms[room]['neighbors_temp_reward'][neighbor] = (yourdoor, temp)

        return self.rooms, self.temperary_rewards

    """
        tile/action level transition function, reward function, and observation
    """
    # def T(self,s,a):
    #     """ transition function """
    #
    #     tile,head = s
    #     if a == 'go_straight':
    #         tile = self.tilesummary[tile][head]
    #         if tile == 'wall': return s
    #     elif a == 'turn_left':
    #         head = np.mod(head+90,360)
    #     elif a == 'turn_right':
    #         head = np.mod(head-90,360)
    #
    #     return (tile,head)

    def T(self,s,a, observed=None, visited=None, to_triage=False):
        """ transition function """

        if len(s) == 4: ## augmented state space that includes tiles observed and victims triaged
            s, observed, visited, to_triage = s
        elif isinstance(s[1], tuple):  ## augmented state space that includes tiles observed
            s, observed = s  ## , countdown

        ## position and heading change
        s_new = s
        tile,head = s
        if a == 'go_straight':
            tile = self.tilesummary[tile][head]
            if tile != 'wall':
                s_new = (tile, head)
        elif a == 'turn_left':
            s_new = (tile, np.mod(head+90,360))
        elif a == 'turn_right':
            s_new = (tile, np.mod(head-90,360))

        ## augmented state change
        if observed!=None:  ## isinstance(observed, tuple):
            observed = list(observed)
            observed.extend(self.obs_rewards[s])
            observed = tuple(set(observed))
            # countdown -= self.player['costs'][a]

            if visited!=None:  ##len(s) == 4:
                visited = list(visited)
                if s_new[0] not in visited:
                    visited.append(s_new[0])

                if to_triage: ## after triage
                    visited.append(to_triage)
                    # print(f'T(s={s}, a={a}, observed={observed}, visited={visited})')
                    # print(f'    change to_triage {to_triage} to visited')
                    to_triage = None

                if a == 'triage':
                    victim = self.triageable(s)
                    if victim and victim not in visited and victim not in self.visited_tiles and \
                            victim not in self.victims_saved['V'] and victim not in self.victims_saved['VV']:
                        to_triage = victim
                        # print(f'T(s={s}, a={a}, observed={observed}, visited={visited})')
                        # print(f'    found to_triage {to_triage}')

                return (s_new, observed, tuple(visited), to_triage)

            return (s_new, observed)  ## , countdown

        return s_new

    def get_obs_rewards(self, obs_rewards=None):

        def list_minus(li1, li2):
            # return list(set(li1).difference(set(li2)))
            return list(set(li1) - set(li2))

        if visualize.LEARNING:
            return {}

        DEBUG = False
        start = time.time()

        ## the first time
        if self.obs_rewards == None:
            mypath  = join('maps', 'raycasted')
            file_name = join(mypath,'obs_'+self.MAP.replace('/','_'))
            file_init_name = join(mypath, 'obs_init_' + self.MAP.replace('/', '_'))
            fieldnames = ['states','observations']

            ## if there's saved file in maps/raycasted, read it
            # files = [f for f in listdir(mypath) if isfile(join(mypath, f)) and self.MAP in str(f)]

            ## if no saved file or has the dog, calculate it and save it as csv
            if not isfile(file_init_name):  ##len(files) == 0:
                print('initializing observed tiles')
                obs_rewards = {}
                obs_rewards_init = {}
                for tile in self.tilesummary_truth.keys():
                    for head in [0,90,180,270]:
                        obs_rewards[(tile, head)] = mapreader.print_shadow(self, (tile,head))
                        obs_rewards_init[(tile, head)] = mapreader.print_shadow(self, (tile, head), INIT=True)

                with open(file_name, 'w') as f:
                    w = csv.writer(f)
                    w.writerow(fieldnames)
                    w.writerows(obs_rewards.items())

                with open(file_init_name, 'w') as f:
                    w = csv.writer(f)
                    w.writerow(fieldnames)
                    w.writerows(obs_rewards_init.items())

            else:
                obs_rewards = {}
                reader = csv.DictReader(open(file_name))
                for row in reader:
                    key = make_tuple(''.join(row[fieldnames[0]]))
                    value = list(make_tuple(''.join(row[fieldnames[1]])))
                    obs_rewards[key] = value

                obs_rewards_init = {}
                reader = csv.DictReader(open(file_init_name))
                for row in reader:
                    key = make_tuple(''.join(row[fieldnames[0]]))
                    value = list(make_tuple(''.join(row[fieldnames[1]])))
                    obs_rewards_init[key] = value

            # for tile in self.tilesummary_truth.keys():
            #     for head in [0, 90, 180, 270]:
            #         if tile == 499 or tile == 500:
            #             print('\n', tile, head)
            #             print(len(obs_rewards[(tile, head)]), obs_rewards[(tile, head)])
            #             print(len(obs_rewards_init[(tile, head)]), obs_rewards_init[(tile, head)])

            self.obs_rewards = obs_rewards
            self.obs_rewards_init = obs_rewards_init
            if DEBUG: print(f'..... finished reading obs_rewards in {round(time.time() - start, 3)} seconds')

        ## future times
        elif obs_rewards == None:

            # obs_rewards = {}
            # for state, tiles in self.obs_rewards.items():
            #     obs_rewards[state] = list_minus(tiles, self.observed_tiles)
            # self.obs_rewards = obs_rewards

            if self._pos_agent_room != None:
                ## reduce to computing only the observation rewards in the current room and adjacent rooms
                room_tiles = copy.deepcopy(self.rooms_truth[self._pos_agent_room]['tiles'])
                for neighbor in self.rooms_truth[self._pos_agent_room]['neighbors']:
                    room_tiles.extend(self.rooms_truth[neighbor]['tiles'])
                # room_tiles = self.tilesummary.keys()

                for tile in room_tiles:
                    for head in [0, 90, 180, 270]:
                        state = (tile,head)
                        self.obs_rewards[state] = list_minus(self.obs_rewards[state], self.observed_tiles)

                if DEBUG: print(f'1 finished 2 in {round(time.time() - start, 3)} seconds')

        ## else, just use the obs_rewards computed in other env, as in INVERSE_PLANNING and REPLAY_WITH_TOM
        return self.obs_rewards

    def triageable(self, s):
        """ return true if the player is in valid range of triaging a victim"""

        # if s in self.triageables:
        #     tile = self.triageables[s]
        #     if tile in self.victims_saved['V'] or tile in self.victims_saved['VV']:
        #         return False
        #     else:
        #         return True

        tile = self.tilesummary[s[0]]
        row, col = tile['row'], tile['col']
        for ii in range(- 2, + 3):
            for jj in range(- 2, + 3):
                if abs(ii) + abs(jj) < 4:
                    i = ii + row
                    j = jj + col
                    if (i, j) in self.tile_indices:
                        tile = self.tile_indices[(i, j)]
                        if 'victim' in self.tilesummary[tile]['type'] and tile in self.obs_rewards_truth[s]:
                            if s not in self.triageables:
                                # print(f'  !! victim {tile} can be observed from state {s}')
                                self.triageables[s] = tile
                            return tile
        return False

    def get_obs_reward(self, s):
        ## observation rewards
        return 0

        obs_reward = 0
        obs_room = 0
        obs_corridor = 0
        if s[0] == None:
            print('!!!! investigate')
            return 0
        for tile in self.obs_rewards[s]:
            if self.room_is_room(self.tiles2room[tile]):
                obs_room += 1
            else:
                obs_corridor += 1
        obs_reward += obs_room * self.player["observation_reward"]
        if self.player['prior_belief'] == 'BELIEF_UNIFORM':
            obs_reward += obs_corridor * self.player["observation_reward"]

        return obs_reward

    def R(self,s,a=None, tile=None, visited=tuple([])):
        """ reward function """

        ## just retrieve the expected reward
        if tile != None: return self.tilesummary[tile]['reward']

        obs_reward = 0
        reward = 0

        ## default values before different processing
        if 'victim' not in self.tilesummary[s[0]]['type'] and s[0] not in visited:
            reward += self.tilesummary[s[0]]['reward']

        ## ------------------------
        ## for planning, add exp reward as unvisited tiles
        if self.tilesummary[s[0]]['type'] != 'wall' or True:  ## TODO: So many flags need fixing
                # and and self.tiles2room[s[0]] not in self.barked_rooms:

            ## cost of taking the action
            if a != None:
                reward -= self.player['costs'][a]

            ## cost and reward of triaging
            if a == 'triage':
                facing_victim = self.triageable(s)
                if facing_victim and facing_victim not in visited and \
                        facing_victim not in self.victims_saved['V'] and facing_victim not in self.victims_saved['VV']:
                    reward += self.tilesummary[facing_victim]['reward']
                    # print(f'R(s={s}, a={a}, visited={visited})')
                    # print(f'    triage victim {facing_victim} reward is {reward}')

            # ## observation rewards
            # obs_reward = self.get_obs_reward(s)

        ## ------------------------
        ## for RL, add exp reward as unexplored tiles
        if visualize.LEARNING:

            self.tilesummary[s[0]]['reward'] = 0
            if reward != 0:
                PRINT = True

            if self.tilesummary[s[0]]['type'] in ['victim','victim-yellow','hand-sanitizer','fire','gravel'] and self.tilesummary_truth[s[0]]['reward'] != 0:
                reward += self.tilesummary_truth[s[0]]['reward']  ## true reward
                self.tilesummary_truth[s[0]]['reward'] = 0
                PRINT = True

            # type = self.tilesummary[s[0]]['type']
            # if type not in ['air','gravel','door','wall']: print('tilesummary',type,s)

            self._pos_agent = s
            self.collect_reward()

            # if a != 'go_straight':
            #     reward1 -= 0.005

            # if PRINT and DEBUG:
            #     print(reward, reward1)

        ## ------------------------
        # print(obs_reward)
        return reward + obs_reward

    def get_pos(self, tile):
        if isinstance(tile, tuple): tile = tile[0]
        return self.tilesummary[tile]['row'], self.tilesummary[tile]['col']

    def get_dist(self, s1, s2):
        if isinstance(s1, tuple): s1 = s1[0]
        if isinstance(s2, tuple): s2 = s2[0]
        if s1 == None or s2 == None: return 1000
        x1 = self.tilesummary[s1]['row']
        y1 = self.tilesummary[s1]['col']
        x2 = self.tilesummary[s2]['row']
        y2 = self.tilesummary[s2]['col']
        return math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2)

    """
        debugging/experiment related
    """
    def random_initialize(self):
        tiles = []
        for key in self.tilesummary.keys():
            if self.tilesummary[key]['type'] == 'air':
                tiles.append(key)
        position = random.choice(tiles)
        heading = random.choice([0,90,180,270])
        self._pos_agent = (position, heading)

    def tile_to_cell(self, tile):
        """ during debugging, find the cell index in csv by the tile index """
        row = self.tilesummary_truth[tile]['row']+1
        col = utils.num2col(self.tilesummary_truth[tile]['col']+1)
        return (row, col)

    def cell_to_tile(self, csv_string):
        """ during debugging, find the tile index by the cell index in csv """
        _, row, col = re.split('(\d+)', csv_string)
        return self.tile_indices[(int(row)-1, utils.col2num(col)-1)]

    def print_belief_csv(self):

        if self.player_name != None:

            obs_map = pd.DataFrame('', index=range(visualize.WORLD_HEIGHT), columns=range(visualize.WORLD_WIDTH))
            for index in self.tilesummary.keys():
                tile = self.tilesummary[index]
                i, j = mapreader.coord(tile['pos'])
                obs_map[i][j] = round(tile['reward'], 2)

            s = self._pos_agent
            row = self.tilesummary[s[0]]['row']
            col = self.tilesummary[s[0]]['col']
            agent = {0: '>', 90: '^', 180: '<', 270: 'v'}[s[1]]
            obs_map.loc[row, col] = agent + str(obs_map.loc[row, col]) + agent

            dir_name = join('plots','beliefs', self.player_name)
            if self.step==0:
                if isdir(dir_name):
                    shutil.rmtree(dir_name)
                os.mkdir(dir_name)
            obs_map.to_csv(join(dir_name, str(self.step)+'.csv'))

    def print_policy_csv(self, pi):

        return None

        acts = ['g','l','r'] #{'go_straight': 'g', 'turn_left': 'l', 'turn_right': 'r'}

        if self.player_name != None:

            action_map = pd.DataFrame('', index=range(visualize.WORLD_HEIGHT), columns=range(visualize.WORLD_WIDTH))
            for index in self.tilesummary.keys():
                tile = self.tilesummary[index]
                i, j = mapreader.coord(tile['pos'])
                # string = ''
                string = '__(1)__\n(2)__(0)\n__(3)__'
                for head in range(4):
                    li = pi[index, head]
                    if sum(li) == 0:
                        answer = '[_]'
                    else:
                        answer = str(np.argwhere(li == np.amax(li)).flatten().tolist())
                    # string += answer + '|'
                    string = string.replace('('+str(head)+')', answer)
                # string += ')'
                # string = string.replace('|)', '').replace(' ','')
                # action_map[i][j] = string.replace(' ','')
                action_map.loc[(j, i)] = string.replace(' ','').replace('0','^').replace('1','<').replace('2','>')

            dir_name = join('plots','policies', self.player_name)
            if self.step == 1:
                if isdir(dir_name):
                    shutil.rmtree(dir_name)
                os.mkdir(dir_name)
            action_map.to_excel(join(dir_name, str(self.step)+'.xlsx'))

    def print_tile_summary(self):
        # for key in self.tilesummary.keys():
        #     print(key, ':', self.tilesummary[key],',') #['reward'])
        for key in self.tilesummary_truth.keys():
            print(key, ':', self.tilesummary_truth[key],',') #['reward'])

    def find_victims(self):
        for tile, value in self.tilesummary_truth.items():
            if value['reward'] == self.player['rewards']['victim'] or value['reward'] == self.player['rewards']['victim-yellow']:
                print(tile, value['row'], value['col'])


## specify the MAP_ROOM, WORLD_WIDTH, WORLD_HEIGHT, TILE_SIZE, MAX_ITER, COUNTDOWN
MAP_CONFIG = {
    # '6by6_6_T-0.csv': [None, 6, 6, 60, None],
    # '12by12/12by12_R5A-V2-P10.csv': ['12by12/12by12_R5A-rooms.csv', 12, 12, 30, 100],
    '6by6_3_Z.csv': ['6by6_rooms.csv', 6, 6, 60, 50, 15],
    '12by12_6.csv': ['12by12_rooms.csv', 12, 12, 30, 120, 40],
    '13by13_2.csv': ['13by13_2_rooms.csv', 13, 13, 30, 120, 40],
    '13by13_3.csv': ['13by13_3_rooms.csv', 13, 13, 30, 120, 40],
    '24by24_6.csv': ['24by24_6_rooms.csv', 24, 24, 16, 300, 200], ## 24by24_6_rooms

    'test2.csv': ['test2_rooms.csv', 24, 24, 16, 300, 200],
    'test3.csv': ['test3_rooms.csv', 24, 24, 16, 300, 200],
    'test4.csv': ['test4_rooms.csv', 24, 24, 16, 300, 200],
    'test5.csv': ['test5_rooms.csv', 24, 24, 16, 300, 200],

    '36by64_40.csv': ['36by64_40_rooms.csv', 64, 36, 14, 250, 300],
    '46by45_2.csv': ['46by45_2_rooms.csv', 46, 45, 10, 900, 300],

    '48by89.csv': ['48by89_rooms.csv', 89, 48, 10, 1300, 600],
    '48by89_easy.csv': ['48by89_easy_rooms.csv', 89, 48, 10, 1300, 600],
    '48by89_med.csv': ['48by89_med_rooms.csv', 89, 48, 10, 1300, 600],
    '48by89_hard.csv': ['48by89_hard_rooms.csv', 89, 48, 10, 1300, 600],
}

# ## -------------------------------
# ## MCTS
# ## -------------------------------
# def act(self, action):
#     """Applies the agent action [action] on the environment."""
#
#     ## MCTS on rooms
#     if visualize.ROOM_LEVEL_MCTS:
#         return self.roomlevel_R(action)
#
#     ## MCTS on all tiles
#     else:
#         # transit to the next state
#         self._pos_agent = self.T(self._pos_agent,action)
#
#         # take rewards
#         return self.R(self._pos_agent,action)
#
# def mode_exploration(self):
#     # ## drastically boost rewards
#     # rewards = copy.deepcopy(self.player['rewards'])
#     # if rewards['victim'] > 0:
#     #     rewards['victim'] *= 10
#     # if rewards['victim-yellow'] > 0:
#     #     rewards['victim-yellow'] *= 10
#     # self.change_rewards(rewards)
#
#     ## give a reward to door to get out of room
#     if self.fully_explored(self._pos_agent_room):
#         doors = self.rooms[self._pos_agent_room]['doors']
#         if len(doors) > 0:
#             for door in doors:
#                 self.tilesummary[door]['reward'] = self.player['rewards']['door']
#                 print('give reward to door', door)
#
#     ## make UCT plan more long term
#     self.uct_max_num_steps = 40
#     self.update_uct(num_search_iters=1000, gamma=0.999, beta=0)
#
# def mode_exploitation(self):
#     ## reset expected reward
#     self.change_rewards(self.player['rewards'])
#
#     ## reset default UCT params
#     self.uct_max_num_steps = 15
#     self.update_uct()

if __name__ == '__main__':
    # env = MDP(visualize.MAP)
    env = POMDP(visualize.MAP, visualize.MAP_ROOM, visualize.EXPO_REWARD)