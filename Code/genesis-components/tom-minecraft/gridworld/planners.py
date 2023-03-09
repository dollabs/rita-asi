import numpy as np
import time
import turtle
import pprint
import random
import pandas as pd
import networkx as nx
from networkx.drawing.nx_agraph import graphviz_layout

import matplotlib.pyplot as plt
from scipy.special import softmax
import abc
import copy
from datetime import datetime
import math
from tqdm import tqdm
from os.path import join, isfile, isdir
from os import mkdir
from collections import defaultdict
import json
from collections import namedtuple, defaultdict, deque
from itertools import count, product
import heapq as hq
import sys

import mapreader
import utils
import interface
import ASIST_settings

class Planner:
    """Generic class for planning
    """
    name = 'unnamed'
    output_name = 'unnamed_output'
    font_titles = ("Courier", 18, 'bold')  # 'normal', 'underline'
    font_large = ("Courier", 18, 'normal')
    font_medium = ("Courier", 14, 'normal')
    font_normal = ("Courier", 12, 'normal')
    text_top = 270
    text_left = None

    _plan = []
    _path = []
    _available_actions = None

    verbose = False
    GENERATE_TXT = True
    WIPE_VIZ = False

    ## room-macro-tile level planning
    SHOW_ROOMLEVEL_PLAN = False  ## debugging roomlevel
    SHOW_SEARCH_BRANCHES = False  ## debugging roomlevel
    SHOW_MACRO_ACTIONS_INDEX = False  ## debugging tilelevel
    CIRCLE_SEARCH_BRANCHES = False  ## debugging tilelevel
    VISUALIZE_BELIEF = False

    ## macro-tile level planning
    DRAW_MACRO_SIBLINGS = False
    DRAW_MACRO_BEST_PATH = True
    DRAW_Q_VALUES = True
    DRAW_ROOM_SEARCH_TREE = True

    step = 0
    discarded_rooms = []

    @abc.abstractmethod
    def __call__(self, state):
        """Make a plan given the state."""
        raise NotImplementedError("Override me!")

    def get_setting(self):
        ps = self.get_specific_setting()
        ps['timeout (step)'] = self._timeout
        ps['timeout (game)'] = self._timeout_game
        ps['max # steps'] = self._max_num_steps
        return self.name, ps

    @abc.abstractmethod
    def get_specific_setting(self):
        """Return the main parameters of the planner."""
        raise NotImplementedError("Override me!")

    @abc.abstractmethod
    def reset(self):
        """Initiate the values"""
        raise NotImplementedError("Override me!")

    @abc.abstractmethod
    def get_action(self, state, replan=True):
        """Return action given the state"""
        if replan:
            self.run(state)
        return self._get_greedy_action(state)

    def inc_step(self):
        self.step += 1

    def is_available(self, s, a):
        """ check if action is available """
        if self._available_actions == None: return True
        if self.MODE != 'ROOM' and isinstance(s[1], tuple):
            s = s[0]
        if a in self._available_actions[s]: return True
        return False

    @abc.abstractmethod
    def run(self, state):
        """Does the planning and returns the plan and path"""
        raise NotImplementedError("Override me!")

    @abc.abstractmethod
    def _get_greedy_action(self, state):
        """Way of finding the greedy and valid action based on Q/V"""
        raise NotImplementedError("Override me!")

    @abc.abstractmethod
    def get_VX(self, state):
        """For drawing the plan on the board"""
        raise NotImplementedError("Override me!")

    def get_Qs(self, state):
        """For inverse planning"""
        raise NotImplementedError("Override me!")

    def set_VIZ(self, viz):
        planners = [self]
        if isinstance(self, HierarchicalPlanner):
            planners.append(self._tilelevel_planner)
            planners.append(self._roomlevel_planner)
        if isinstance(self, MacroPlanner):
            planners.append(self._macro_planner)
        for planner in planners:
            planner.viz = viz
            planner.USE_INTERFACE = viz.USE_INTERFACE

        if hasattr(self, 'recordings_dir'):
            viz.output_name = self.output_name
            viz.recordings_dir = self.recordings_dir

    def set_params(self, name=None, seed=None, timeout=None, verbose=None,
                   max_num_steps=None, num_simulations=None, initial_Q=None,
                   gamma=None, epsilon_con=None, epsilon_exp=None, beta=None,
                   output_name=None, key=None, value=None):
        # if output_name != None: output_name = output_name.replace('-default', '')

        ## tested by scripts
        if value != None:
            if key == 'initial_Q': initial_Q = value
            if key == 'epsilon_con': epsilon_con = value
            if key == 'epsilon_exp': epsilon_exp = value
            if key == 'gamma': gamma = value
            if key == 'beta': beta = value
            if key == 'simulations': num_simulations = value

        ## some planners have components
        add_planners = []
        if isinstance(self, LRTDP): add_planners.append(self.wrapped)

        if isinstance(self, HierarchicalPlanner):
            if gamma != None: self._tilelevel_planner._gamma = gamma

        ## change the corresponding params of the components
        for planner in add_planners:
            if seed != None: planner._rng = np.random.RandomState(seed)
            if timeout != None: planner._timeout = timeout
            if verbose != None: planner.verbose = verbose
            if max_num_steps != None:
                planner._max_num_steps = min(max_num_steps, planner._max_num_steps)
            if num_simulations != None: planner._num_simulations = num_simulations
            if gamma != None: planner._gamma = gamma
            if epsilon_con != None: planner._epsilon_con = epsilon_con
            if epsilon_exp != None: planner._epsilon_exp = epsilon_exp
            if output_name != None: planner.output_name = output_name

        ## in general for all planners
        if name != None: self.name = name
        if seed != None: self._rng = np.random.RandomState(seed)
        if timeout != None:
            self._timeout = timeout
            self._timeout_game = timeout
            if hasattr(self, '_max_num_steps'):
                self._timeout_game = max(timeout, self._max_num_steps/10)
        if verbose != None: self.verbose = verbose
        if max_num_steps != None:
            self._max_num_steps = max_num_steps
            self._timeout_game = max(self._timeout, max_num_steps/10)
        if num_simulations != None:
            self._num_simulations = num_simulations
        if gamma != None: self._gamma = gamma
        if initial_Q != None: self._initial_Q = initial_Q
        if epsilon_con != None: self._epsilon_con = epsilon_con
        if epsilon_exp != None: self._epsilon_exp = epsilon_exp
        if beta != None: self._beta = beta
        if output_name != None: self.output_name = output_name

    def set_available_actions(self, available_actions):
        self._available_actions = available_actions

    def set_plan(self, path, plan):
        self._path = path
        self._plan = plan

    def draw_planning_panel(self, player, player_name):
        if self.USE_INTERFACE and not self.viz.SHOW_FINAL_ONLY:
            self.draw_planning_panel_basics(player, player_name)

    def draw_text(self, writer, text, font=None, left=None, left_shift=0, top=None, top_shift=None):

        ## format dictionary
        if isinstance(text, dict): text = pprint.pformat(text, indent=1, width=50)

        ## move writer to suit text length
        if font == None:
            if writer == self.titles: font = self.font_titles
            if writer == self.writer: font = self.font_normal

        ## find where to write
        if top != None:
            self.text_top = top
        else:
            if top_shift == None:
                ## margin_top of titles and body
                if writer == self.titles: self.text_top -= font[1]
                if writer == self.writer: self.text_top -= font[1] / 2

                ## text_height equals to font_size * line_height
                text_height = 1 + (len(text) - len(text.replace('\n', '')))
                top_shift = font[1] * text_height
            self.text_top -= top_shift
        if left == None: left = self.text_left

        ## write
        writer.goto(left+left_shift, self.text_top)
        writer.write(text, font=font)

    def get_player_profile(self, player):
        player_profile = copy.deepcopy(player)

        ## planner related params
        keys = ["exploration_reward", "certainty_boost_factor", "epsilon", "roomlevel_gamma",
                    "tilelevel_gamma", "temperature", "information_reward", "goal_boost_factor",
                    "cost_go_straight", "observation_reward"]
        if isinstance(self, MacroPlanner):
            keys.append('beta')
        for key in keys:
            if key in player_profile:
                player_profile.pop(key)

        ## belief
        if "prior_belief" in player_profile:
            player_profile["prior_belief"] = player_profile["prior_belief"].replace("BELIEF_", "")

        ## rewards and costs
        types = ['triage', "air", "wall", "door", "fire"]
        if '48by89' in self.viz.env.MAP:
            types.append('gravel')
        for type in types:
            if type in player_profile['rewards']:
                player_profile['rewards'].pop(type)
            if type in player_profile['costs']:
                player_profile['costs'].pop(type)
        return player_profile

    def draw_rollouts(self, s0, epsilon=0.1, num_rollouts=10, length=10):
        summary = {}
        for index in range(num_rollouts):
            s = s0
            for step in range(length):
                ## choose random action
                if self._rng.rand() > epsilon:
                    a = self._rng.choice(self._actions)
                else:
                    a = self._get_greedy_action(s)
                s = self.T(s,a)

                if s[0] not in summary: summary[s[0]] = 0
                summary[s[0]] += 1
        cir = interface.draw_heatmap(summary, self.viz.get_pos, self.viz.ts)
        # self.viz.screen.update()
        return cir

    def visualize_belief(self, unobserved_in_rooms, tilesummary, barked_rooms, room_is_room, fully_explored_rooms, rooms):

        for room, tiles in unobserved_in_rooms.items():
            for tile in tiles:
                if tilesummary[tile]['type'] == 'air':
                    r = tilesummary[tile]['reward']
                    bark = 0
                    if room in barked_rooms:
                        bark = barked_rooms[room]
                    if r > 0.8 or (bark == 2 and r > 0.2):
                        self.viz.draw_at_with(self.writer, tile=tile, type='victim-yellow-06')
                    elif r > 0.4 or (bark == 2 and r > 0.01):
                        self.viz.draw_at_with(self.writer, tile=tile, type='victim-yellow-03')
                    elif r == 0 and room_is_room(room):
                        self.viz.draw_at_with(self.writer, tile=tile, type='victim-00')

        for room in rooms:
            if room in fully_explored_rooms:
                ## just print once
                if room not in self.discarded_rooms:
                    print('fully observed room', room)
                self.discarded_rooms.append(room)

                for tile in rooms[room]['tiles']:
                    if tilesummary[tile]['type'] == 'air':
                        self.viz.draw_at_with(self.writer, tile=tile, type='obs-victim-no')

    def draw_planning_panel_basics(self, player, player_name, mission_summary=None):

        # if self.viz.MAGNIFY

        ## initialize top and margin:
        self.text_top = self.viz.WINDOW_HEIGHT / 2 - 60
        self.text_left = -int(self.viz.WINDOW_WIDTH / 2) + 30

        ## titles will stay static throughout game, usually is bold
        titles = turtle.Turtle()
        titles.hideturtle()
        titles.up()
        self.titles = titles

        ## writer will update text throughout game, usually text is not bold
        writer = turtle.Turtle()
        writer.hideturtle()
        writer.up()
        self.writer = writer

        if '48by89' in self.viz.env.MAP and self.viz.MAGNIFY:
            titles.color(utils.colors.orange)
            writer.color(utils.colors.orange)

        if isinstance(self, HierarchicalPlanner):
            self._tilelevel_planner.writer = writer
            self._roomlevel_planner.writer = writer

        ## which will be updated
        if not self.viz.SHOW_FINAL_ONLY:

            # self.draw_text(titles, 'Expected Tile Reward R(s)', top=260)
            # self.draw_text(titles, 'Current Best Plan V(s)', top=120)

            if self.viz.SHOW_R_TABLE:
                self.draw_text(titles, 'Expected Tile Reward R(s)')
                self.draw_text(titles, 'Current Best Plan V(s)', top=130)
                top = -40
            else:
                top = self.viz.WINDOW_HEIGHT/2 - 60

            player_profile = self.get_player_profile(player)
            self.draw_text(titles, 'Player Profile: ', top=top)
            self.draw_text(titles, player_name, font=self.font_large, left_shift=180, top=top)
            self.draw_text(titles, player_profile, font=self.font_medium)

        ## will not be updated but just show in the end
        else:

            # self.text_top = 300  ## need more space to write

            ## mission summary
            if mission_summary != None:
                self.draw_text(titles, 'Mission Summary')
                self.draw_text(writer, mission_summary, font=self.font_large)

            ## player profile
            player_profile = self.get_player_profile(player)
            self.draw_text(titles, 'Player Profile: ')
            self.draw_text(writer, player_name, font=self.font_large, left_shift=180, top_shift=0)
            self.draw_text(writer, player_profile, font=self.font_large)

            ## planner setting
            planner_name, planner_setting = self.get_setting()
            if planner_setting != {}:
                self.draw_text(titles, 'Planner Setting: ')
                self.draw_text(writer, planner_name, font=self.font_large, left_shift=180, top_shift=0)
                self.draw_text(writer, planner_setting, font=self.font_normal)

            ## planner mission stats
            mission_stats = self.get_mission_stats()
            if mission_stats != {}:
                self.draw_text(titles, 'Planner Performance Summary')
                self.draw_text(writer, mission_stats, font=self.font_normal)

    def update_planning_panel(self, env, s):
        if self.USE_INTERFACE and not self.viz.SHOW_FINAL_ONLY:
            self.update_planning_panel_basics(env, s)

    def update_planning_panel_basics(self, env, s):
        if self.viz.SHOW_R_TABLE and env.replan: ## and self.viz.world_width <= 13
            self.writer.clear()
            self.update_board(self.make_board(env, s, TYPE="R"), s, self.writer, 160)
            self.update_board(self.make_board(env, s, TYPE="V"), s, self.writer, 0)

    def make_board(self, env, s, TYPE="R"):

        board = pd.DataFrame('', index=range(self.viz.world_height), columns=range(self.viz.world_width))

        temp = {}
        for head in range(4):
            temp[head] = pd.DataFrame('', index=range(board.shape[0]), columns=range(board.shape[1]))

        for index in env.tilesummary.keys():
            tile = env.tilesummary[index]
            room = env.tiles2room[index]
            if TYPE=="R" and room != None and index in env.rooms[room]['tilesummary']:
                summary = env.rooms[room]['tilesummary'][index]
            if tile['type'] != 'wall':
                i, j = mapreader.coord(tile['pos'])

                if TYPE == "R":
                    reward = tile['reward']
                    max_obs_reward = -np.inf
                    for head in range(4):
                        state = (index, 90 * head)
                        obs_reward = env.get_obs_reward(state)
                        if obs_reward > max_obs_reward:
                            max_obs_reward = obs_reward
                    reward += max_obs_reward
                    board[j][i] = round(reward, 3)

                elif TYPE == "V":
                    max_V = -np.inf
                    for head in range(4):
                        state = (index, 90 * head)
                        V = self.get_VX(state)
                        if V > max_V:
                            max_V = V
                    board[j][i] = round(V, 3)

                # max_reward = -np.inf
                # for head in range(4):
                #     state = (index, 90 * head)
                #     if TYPE == "R":
                #         reward = max(env.R(state, 'go_straight'), env.R(state, 'triage'))
                #     elif TYPE=="V":
                #         reward = self.get_VX(state)
                #     else:
                #         return None
                #     max_reward = max(max_reward, reward)
                #
                #     temp[head][i][j] = reward
                #     if TYPE=="R" and room != None and index in env.rooms[room]['tilesummary']:
                #         summary['reward_facing_' + str(90 * head)] = reward
                #
                # board[j][i] = round(max_reward, 2)  # round(tile['reward'],2) #

        row = env.tilesummary[s[0]]['row']
        col = env.tilesummary[s[0]]['col']
        agent = {0: '>', 90: '^', 180: '<', 270: 'v'}[s[1]]
        board.loc[row, col] = agent + str(board.loc[row, col]) + agent

        return board, row, col

    def update_board(self, board_input, s, writer, ver_shift):

        if board_input == None: return
        board, row, col = board_input

        ## print the whole table directly
        if self.viz.world_width <= 12:
            writer.goto(self.text_left, ver_shift)
            writer.write(board.to_string(), font=("Courier", self.viz.FONT_SIZE, 'normal'))
            return

        ## directly draw on tile
        if self.viz.MAGNIFY and self.viz.env.step > 1:
            if ver_shift > 100: y_shift = -self.viz.ts//6  ## tile reward
            else: y_shift = self.viz.ts//6  ## converged V

            roomlevel_tiles = [t for t in self.viz.env.tilesummary if self.viz.env.tilesummary[t]['type'] != 'wall']
            if isinstance(self, HierarchicalPlanner):
                roomlevel_tiles, _ = self._get_roomlevel_states(s)
            for tile in roomlevel_tiles:
                self.viz.go_to_pos(writer, tile, x_shift=self.viz.ts//3, y_shift=y_shift)
                row = self.viz.env.tilesummary[tile]['row']
                col = self.viz.env.tilesummary[tile]['col']
                writer.write(board.loc[row, col], font=("Courier", self.viz.ts//4, 'normal'))

        ## select the 4 blocks near me to print
        else:
            fontsz = 8
            gaze = 6
            to_print = board.loc[max(0, row - gaze): min(board.shape[0], row + gaze + 1),
                       max(0, col - gaze): min(board.shape[1], col + gaze + 1)].to_string()

            writer.goto(self.text_left, ver_shift)
            writer.write(to_print, font=("Courier", fontsz, 'normal'))

    def init_recordings(self, recordings_dir, initial_text='', verbose=False):
        if not isdir(recordings_dir):
            mkdir(recordings_dir)
        self.recordings_dir = recordings_dir
        if verbose: self.verbose = True
        self.init_txt()
        self.init_viz()
        self.log(initial_text+'\n')

    def init_txt(self):
        txt_dir = join(self.recordings_dir, 'txt')
        if not isdir(txt_dir):
            mkdir(txt_dir)
        self.txt_name = join(txt_dir, self.output_name+'.txt')
        self.logger = open(self.txt_name, "w")

        if isinstance(self, HierarchicalPlanner):
            self._roomlevel_planner.logger = self.logger
            self._tilelevel_planner.logger = self.logger

    def init_viz(self):
        viz_dir = join(self.recordings_dir, 'viz')
        if not isdir(viz_dir):
            mkdir(viz_dir)

    def log(self, to_print='', theme=None, value=None, env=None, PRINT=True):
        if theme=='victim_to_change':  ## value will be victim_to_change
            if len(value) == 1:
                tile = value[0]
                row = env.tilesummary[tile]['row']
                col = env.tilesummary[tile]['col']
                to_print = f'   !! saved victim {tile} at {(row,col)}'
        if self.GENERATE_TXT and hasattr(self, 'logger'):
            # self.logger = open(self.txt_name, "w")
            self.logger.write(to_print + '\n')
            # self.logger.close()
        if PRINT and self.verbose and len(to_print) < 100 and len(to_print)>0:
            print(to_print)

    def get_mission_stats(self):
        """ info about max and average planner performance (convergence) """

        # if isinstance(self, LRTDP):
        #     self.mission_stats.update(self.wrapped.mission_stats)

        def round_up(num):
            if abs(num) == np.inf: return num  ## infinity
            elif int(num) == num: return int(num)  ## int
            else: return round(num, 2)  ## float

        ## add the average, max, min value of all values of the stat during the mission
        def add_three_stats(stat_item, stat_list):
            if len(stat_list) > 0:
                print(stat_item, stat_list)
                self.mission_stats[f'{stat_item} (ave)'] = round_up(sum(stat_list) / len(stat_list))
                self.mission_stats[f'{stat_item} (max)'] = round_up(max(stat_list))
                self.mission_stats[f'{stat_item} (min)'] = round_up(min(stat_list))

        for stat_item in ['converge at iteration', 'timeout at iteration', ## for methods involving convergence
                          '# solved states', 'solved states ratio', ## for LRTDP
                          'max # visits', 'max exploi2explor ratio']: ## for methods involving simulations/rollouts
            if stat_item in self.mission_stats:
                add_three_stats(stat_item, self.mission_stats[stat_item])

        if 'converge at iteration' in self.mission_stats and 'timeout at iteration' in self.mission_stats:
            converge_at = self.mission_stats['converge at iteration']
            timeout_at = self.mission_stats['timeout at iteration']
            total = len(converge_at) + len(timeout_at)
            if total > 0:
                self.mission_stats[f'converge at iteration (rate)'] = round_up(len(converge_at) / total)

        ## throw away the original lists of data
        keys = []
        for key, value in self.mission_stats.items():
            if isinstance(value, list) or '(last)' in key:
                keys.append(key)
        for key in keys:
            self.mission_stats.pop(key)

        return self.mission_stats

    def record_json(self, env, episode, duration):
        json_dir = join(self.recordings_dir, 'json')
        if not isdir(json_dir):
            mkdir(json_dir)

        file_name = join(json_dir, self.output_name+'.json')
        with open(file_name, 'w') as outfile:
            countdown_passed = round(env.player['countdown'] - env.countdown_real, 2)
            ep = {
                "map": env.MAP,
                "rooms_explored": f"{len(env.observed_rooms)} / {len(env.get_rooms())}",
                "victims_saved": {
                    'yellow': f"{len(env.victims_saved['VV'])} / {len(env.victim_summary['VV'])}",
                    'green': f"{len(env.victims_saved['V'])} / {len(env.victim_summary['V'])}",
                },
                'countdown': f"{countdown_passed} / {env.player['countdown']}",
                "real_time_passed (sec)": round(duration, 2),
                "steps_taken": f"{env.step} / {env.max_iter}",
                "score": f"{env.score} / {env.score_total}",
            }
            mission_summary = copy.deepcopy(ep)
            ep['player_name'] = env.player_name
            ep['player_profile'] = env.player
            ep['score_weighted'] = round(env.score_weighted, 3)
            ep["steps"] = {}
            for index in range(len(episode)):
                sa = {}
                sa["tile"] = int(episode[index][0][0])
                sa["heading"] = int(episode[index][0][1])
                sa["action"] = episode[index][1]
                ep["steps"][index] = sa
            json.dump(ep, outfile)

        return mission_summary

    def finish_recordings(self, env, ep, duration='unknown'):

        ## txt
        self.log(f'\n... finished game in {duration} seconds')
        # self.logger.close()

        ## json
        mission_summary = self.record_json(env, ep, duration)

        ## png
        if hasattr(self.viz, 'macros_marker'):
            self.viz.macros_marker.clear()
        if self.viz.SHOW_FINAL_ONLY:
            self.viz.make_screen()
            victim_to_change = env.victims_saved['V'] + env.victims_saved['VV']
            self.viz.update_maze(env._pos_agent, trajectory=ep, victim_to_change=victim_to_change)
            self.draw_planning_panel_basics(env.player, env.player_name, mission_summary=mission_summary)
        self.viz.take_screenshot(FINAL=True, CROP=False)

        if self.USE_INTERFACE:
            if self.WIPE_VIZ: self.viz.screen.clear()
            else: self.viz.screen.mainloop()

class AStar(Planner):
    """Planning with A* search,
        If it's shortest path problem (goal_state_action!=None), heuristic is Euclidian distance
        If it's max-reward problem, heuristic is 0 for all
    """

    Node = namedtuple("Node", ["state", "parent", "action", "g", "h"])
    DEBUG = False
    USE_HISTORY = False

    def __init__(self, actions, T, R, get_pos, get_dist, gamma=0.9, goal_state_action=None,
                 max_num_steps=50, beta=1, seed=0, timeout=20, verbose=False,
                 available_actions=None, traversed_rooms=None, obs_rewards=None, env=None,
                 EXHAUSTIVE = False, BnB = False):
        self.name = 'AStar'

        self._actions = actions
        self.T = T
        self.R = R
        self._available_actions = available_actions
        self._traversed_rooms = traversed_rooms

        self.get_pos = get_pos
        self.get_dist = get_dist
        self.goal_state_action = goal_state_action
        self.obs_rewards = obs_rewards
        self.env = env
        self.EXHAUSTIVE = EXHAUSTIVE
        if self.EXHAUSTIVE: self.name = 'DFS'
        self.BnB = BnB
        if self.BnB: self.name = 'BnB'

        self._living_cost = 1 ##0.5 ##2
        self._gamma = gamma
        self._beta = beta  ## how strong the goal is
        self._max_num_steps = max_num_steps
        self._timeout = timeout
        self._rng = np.random.RandomState(seed)
        self.verbose = verbose

        self.PRINT_V = False
        self.node_values = {}
        self.nodes_explored = []  ## avoid loops

        ## macro planner
        if callable(self._actions):
            self.MODE = 'MACRO'
            ## for room level planning
            self.edge_labels = {}
            self._living_cost = 30
            self.name = 'UCS'
            # self.history2countdown = {}  ## (macro, history): countdown

        ## observing path; state includes s and history
        elif isinstance(self.goal_state_action, list):
            self.MODE = 'OBS'
            self._room_tiles = None

        ## tilelevel planning, encourage shorter distance to goal
        elif isinstance(self.goal_state_action, tuple):
            self.MODE = 'TILE'

        ## roomlevel planning, encourage visiting less connected areas
        elif self.goal_state_action == None:
            self.MODE = 'ROOM'
            self.node_values = {room: f'area {room}:\n' for room in self.env.rooms}

        else:
            self.MODE = 'VIZ'

        # writer = turtle.Turtle()
        # writer.hideturtle()
        # writer.up()
        # self.writer = writer

        self.mission_stats = {
        }

    def get_goal(self):
        if self.MODE == 'TILE':  ##isinstance(self.goal_state_action, tuple):
            state, action = self.goal_state_action
            return  ((self.get_pos(state[0]), state[1]), action)
        return None

    def set_goal(self, state, action, beta=1):
        self.MODE = 'TILE'
        self.goal_state_action = (state, action)
        self._beta = beta

    def get_observed(self, visited_states):
        ## return the list of observed tiles and the new tiles observed in each new state
        observed_tiles = []
        observed_summary = {}
        for state in visited_states:
            observed_summary[state] = list( set(self.obs_rewards[state]) - set(observed_tiles) )
            observed_tiles.extend(self.obs_rewards[state])
        return set(observed_tiles), observed_summary

    def _heuristic(self, state, visited_states=None, last_state=None, g=None, verbose=False):

        def get_dist_total(unobserved_tiles, state):
            dist_total = 0
            for unobserved in unobserved_tiles:
                dist_total += self.get_dist(unobserved, state)
            return dist_total

        if self.MODE == 'OBS': ## isinstance(self.goal_state_action, list):  ## observing path; state includes s and history
            total = len(self.goal_state_action)
            if visited_states != None:
                if True:  ##self.USE_HISTORY:
                    state = state[0]
                    last_state = last_state[0]
                observed_tiles, _ = self.get_observed(visited_states)
                new_obs_tiles = list(set(self.obs_rewards[state]) - observed_tiles)
                new_obs_tiles = [t for t in new_obs_tiles if t in self.goal_state_action]
                unobserved_tiles = list(set(self.goal_state_action) - observed_tiles)
                dist_diff = get_dist_total(unobserved_tiles, state) - get_dist_total(unobserved_tiles, last_state)
                total = len(unobserved_tiles)
                # print(total, len(new_obs_tiles), total / (len(new_obs_tiles)+1), ' | ', dist_diff, dist_diff/len(unobserved_tiles), state, last_state)
                return total / (len(new_obs_tiles)+1) ## + dist_diff/len(unobserved_tiles) ## + cost_estimate ## self.obs_rewards[state])  ##
            return total

        elif self.MODE == 'TILE': ## isinstance(self.goal_state_action, tuple): ## tilelevel planning, encourage shorter distance to goal
            return self.get_dist(state[0], self.goal_state_action[0][0])

        elif self.MODE == 'ROOM': ## roomlevel planning, encourage visiting less connected areas
            return - len(self._available_actions[state]) + 10

        elif self.MODE == 'MACRO':  ## the amount of reward the player is able to get given the current rate of rewards
            macro, history, countdown = state
            h = 0

            if g != None:
                rewards = -g
                costs = self.countdown_original - countdown
                if costs == 0:
                    print('cost is 0', state, last_state)
                    costs = 1
                rate = rewards / costs
                h = - countdown * rate
                if verbose: print(f'  last state = {last_state}  |  state = {state}  |  sum rewards = {round(rewards, 2)}  |  '
                      f'costs = {round(costs, 2)}  |  rate = {round(rate, 2)}  |  heuristic = {round(h, 2)}  |  score = {round(h+g, 2)}')

            return h

        return 0

    def _check_goal(self, node, verbose=False):
        verbose = False
        visited_states = self.get_visited_nodes(node)[1]

        ## having observed all tiles in a room
        if self.MODE == 'OBS': ## isinstance(self.goal_state_action, list):
            observed_tiles, observed_summary = self.get_observed(visited_states)
            # print(len(observed_tiles), len(self.goal_state_action), list(observed_tiles - set(self.goal_state_action)))
            if set(self.goal_state_action).issubset(observed_tiles):
                # if self.verbose:
                #     for state, obs in observed_summary.items():
                #         print('  ', state, len(obs), obs)

                ## an extra condition of gathering all rewards of the room
                if self._room_tiles != None:
                    if sum([self.R(t, tile=t) for t in self._room_tiles]) > 0:
                        return False

                ## an extra condition of triaging all victims in the room
                if self.env != None:
                    env = self.env
                    remaining_to_save = copy.deepcopy(env.remaining_to_save['V'])
                    remaining_to_save.extend(env.remaining_to_save['VV'])
                    seen_not_saved_in_room = [v for v in remaining_to_save if v in env.observed_tiles and v not in node.state[2]] ##v in self.goal_state_action]
                    if len(seen_not_saved_in_room) > 0:
                        return False
                return True

        ## shortest path problem: have reached goal state
        elif self.MODE == 'TILE': ## isinstance(self.goal_state_action, tuple):
            # if self.goal_state_action[0][0] == 1000:
            #     print('      check goal!!!', node.state)
            state = node.state
            if state == self.goal_state_action[0]: return True

        ## max reward problem: have reached all rooms once
        elif self.MODE == 'ROOM':
            if self._traversed_rooms(visited_states): return True

        ## macro planner, stop when running out of time
        elif self.MODE == 'MACRO':
            state = node.state
            children = list(self._actions(state[0], state[1]).keys())
            if verbose: print(f'  state {state} --> {len(children)} macro children: {children}')
            # print(f"node.state {node.state}, len(node.state[1]) {len(node.state[1])}, max_num_steps {self._max_num_steps}")
            
            ## finished exploring all rooms
            rooms = [c[1] for c in children if c[0] == 'explore']
            if rooms == self.viz.env.get_rooms():
                return True
            
            ## reach max horizon
            if len(node.state[1]) > self._max_num_steps or len(children) == 0:
                return True
            # if node.state[2] < 0: return True

        return False

    def get_specific_setting(self):
        ps = {}
        return ps

    def reset(self):
        self._plan = []
        self._path = []
        self.node_values = {}
        self.nodes_explored = [] ## avoid loops

        if self.MODE in ['ROOM', 'MACRO']:
            if hasattr(self, 'writer'):
                self.writer.color('#e67e22')

            if self.MODE == 'MACRO':
                self.edge_labels = {}
                self.node_values = {room: f'area {room}:\n' for room in self.viz.env.get_rooms()}
            elif self.MODE == 'ROOM':
                self.node_values = {room: f'area {room}:\n' for room in self.env.rooms}

    def draw_rollout_path(self, tt=None, color='#f1c40f', CLEAR=False, width=4): ## EODO: comment out for replayer
        if not hasattr(self, 'viz') or not self.viz.USE_INTERFACE: return
        ROOM = (self.MODE == 'ROOM')
        if tt == None:
            if not hasattr(self, 'writer'): return
            tt = self.writer

        if CLEAR:
            tt.clear()

        tt.penup()
        tt.pencolor(color)  ## yellow
        tt.pensize(max(self.viz.ts//8, width))
        ori_color_wheel = self.viz.color_wheel
        self.viz.color_wheel = interface.initializee_color_wheel(len(self._path), rainbow=True)
        for i in range(len(self._path)):

            if ROOM:
                self.viz.use_color_wheel(tt, i, len(self._path), max_w=self.viz.ts, min_w=self.viz.ts//8)
                tile = self._path[i]
            else:
                if isinstance(self._path[i][0], str):  ## macro form
                    tile = self._path[i][2][0]
                else: tile = self._path[i][0]

            if i == 0:
                self.viz.go_to_pos(tt, tile, ROOM=ROOM)
                tt.pendown()
            else:
                tt.pendown()
                self.viz.go_to_pos(tt, tile, ROOM=ROOM)

        self.viz.color_wheel = ori_color_wheel
        tt.penup()

        ## write the converged value of each room
        if ROOM and False:
            visit_count = {}
            count = 0
            for i in range(len(self._path)):
                room = self._path[i]
                if room in self.V:

                    ## write visit count
                    if self.V[room] > 0:
                        count += 1
                        if room not in visit_count:
                            visit_count[room] = []

                            self.viz.go_to_pos(tt, room, ROOM=ROOM, y_shift=30)
                            tt.pencolor(utils.colors.red)
                            tt.write(f"({count})", font=("Arial", 26, 'normal'))
                        visit_count[room].append(count)

                    ## write room level value function
                    self.viz.go_to_pos(tt, room, ROOM=ROOM)
                    text = f"From {room}: \n"
                    for action in self.Q[room]:
                        text += f" to {action}: Q = {round(self.Q[room][action], 2)}\n"
                    tt.pencolor(utils.colors.orange)
                    tt.write(text, font=("Arial", 12, 'normal'))

        tt.pencolor('#000000')
        tt.penup()

    def get_action(self, state, replan=True, verbose=False):
        """Return action given the state"""

        if replan or len(self._plan) == 0:
            self.reset()
            timer = utils.Timer(verbose=verbose and False)

            # self.run(state, verbose=verbose)
            # while len(self._plan) == 0:
            #     if verbose: print(f'rerun A* on best children')
            #     self._timeout *= 2
            #     self.run(state, verbose=verbose)
            # timer.add('finding best children')

            ## need to rollout the other possible actions to get Q(s,a) values
            if self.MODE == 'MACRO':
                # if state[0] == [(28, 270), (457, 90)]:
                #     print(f'state {state[0]} has 0 children')

                self.Q = {state:{}}
                children = self._actions(state[0], state[1])
                if len(children) == 0 :
                    print(f'state {state[0]} has 0 children')
                timeout = self._timeout ##/len(children)
                if verbose: print('timeout is', timeout)

                self.best_plans = {}
                self.best_rates = {}
                # self.best_action = self._plan[0]
                # self.best_plans[self.best_action] = {(a[0], round(a[2], 1)): round(self.V[a[0]], 3) for a in self._path[1:]}  ## {a: self.V[a] for a in self._plan}
                if verbose: print(f'  state {state[0]} --> {len(children)} macro children: {list(children.keys())}')
                best_q = -np.inf
                best_c = {}
                best_action = None
                best_planner = None
                best_edge_labels = None
                for action in children:
                    if True or action != self.best_action:

                        # if action == self.best_action:
                        #     print('self.best_plans[self.best_action] old', self.best_plans[self.best_action])
                        new_state = self.T(state, action)

                        planner = AStar(self._actions, self.T, self.R, self.get_pos, self.get_dist,
                                     timeout=timeout, max_num_steps=self._max_num_steps-1, gamma=self._gamma,
                                        EXHAUSTIVE=self.EXHAUSTIVE, BnB=self.BnB)
                        planner.set_VIZ(self.viz)
                        planner.reset()

                        c = state[2] - new_state[2]
                        r = self.R(state, action)
                        self.Q[state][action] = r
                        self.best_plans[action] = {(action, round(new_state[2], 1)): round(r, 3)}

                        ## mission finished after the action
                        if len(self._actions(new_state[0], new_state[1])) == 0:
                            planner.pi = {}
                            planner._path = [new_state]
                        else:
                            planner.run(new_state, verbose=verbose or True)
                            while len(planner._plan) == 0:
                                if verbose: print(f'rerun A* on other children {action}')
                                planner._timeout *= 2
                                planner.run(new_state, verbose=verbose or True)
                            self.Q[state][action] += self._gamma * planner.V[new_state]
                            self.best_plans[action].update({(a[0], round(a[2], 1)): round(planner.V[a[0]], 3) for a in planner._path[1:]})

                        planner.edge_labels[(state[0], tuple([action[1]]))] = (round(r,2), round(c,2))
                        self.edge_labels.update(planner.edge_labels)
                        timer.add(f'finding children for action {action}') ##  get plan: {planner._plan}

                        # if action == self.best_action:
                        #     print('self.best_plans[self.best_action] new', self.best_plans[action])

                        _path = [state]
                        _path.extend([p for p in planner._path])
                        _plan = [action]
                        _plan.extend(planner._plan)
                        pi = {state[0]: action}
                        pi.update(planner.pi)
                        planner._path = _path
                        planner._plan = _plan
                        planner.pi = pi

                        ## choose by the rate of score instead of score
                        self.best_rates[action] = self.Q[state][action] / (state[2]-planner._path[-1][2])
                        q = self.Q[state][action]  ## self.best_rates[action]
                        c = round(state[2] - _path[-1][2], 2)

                        if verbose: print(f'      Q[{state[0]}][{action}] = {round(self.Q[state][action], 3)}')
                        if q > best_q or (q == best_q and best_c[best_q] < c):
                            if (q == best_q and best_c[best_q] < c):
                                print('\n      !!! same plan but smaller time cost', best_c[best_q], c,'\n')
                            best_q = q
                            best_action = action
                            best_planner = planner
                            best_edge_labels = planner.edge_labels
                            best_c[best_q] = c
                # if verbose: print(f'    * Q[{state[0]}][{self.best_action}] = {round(self.Q[state][self.best_action], 3)}')
                if best_planner != None:
                    self._path = [p[0] for p in best_planner._path]
                    self._plan = best_planner._plan
                    self.best_edge_labels = {}
                    rooms = [p[1] for p in best_planner._plan if p[0] == 'explore']
                    key = (state[0], tuple([rooms[0]]))
                    self.best_edge_labels[key] = best_edge_labels[key]
                    for i in range(1, len(rooms)):
                        key = (tuple(rooms[:i]), tuple(rooms[:i+1]))
                        self.best_edge_labels[key] = best_edge_labels[key]
                    self.pi = best_planner.pi
                    self.best_action = best_action
                else:
                    return 'triage'

            else:
                self.run(state, verbose=verbose)
                while len(self._plan) == 0:
                    if verbose: print(f'rerun A* on best children')
                    self._timeout *= 2
                    self.run(state, verbose=verbose)
                timer.add('finding best children')

                self.draw_rollout_path(CLEAR=not self.SHOW_ROOMLEVEL_PLAN)  ## only draw for MODEL in ['TILE', 'OBS']

        # self.draw_node_value()
        # self.viz.screen.update()

        if len(self._path) != 0 and state == self._path[0] and len(self._plan) != 0:
            act = self._plan.pop(0)
            self._path.pop(0)
            return act
        else:
            return self._get_greedy_action(state)

    def _get_greedy_action(self, state):
        if self.MODE == 'MACRO':
            state = state[0]
        if hasattr(self, 'pi') and state in self.pi:
            # if self.MODE == 'MACRO': print(f'policy at state {state} returned {self.pi[state]}')
            return self.pi[state]
        return self._rng.choice(self._actions)

    def register_node_value(self, node):
        # if self.DEBUG: print(node.state)

        visited_states = self.get_visited_nodes(node)[1]

        ## shortest path problem
        if self.goal_state_action != None:
            state = node.state
            if self.MODE == 'OBS':  ## self.USE_HISTORY or
                state = state[0]
                if isinstance(state, int):
                    print('register_node_value isinstance(state, int)')
            tile = state[0]
            if tile not in self.node_values:
                self.node_values[tile] = ''
            self.node_values[tile] += f'{state[1]//90}: ({round(node.g, 2)}, {round(node.h, 2)})\n'
            self.nodes_explored.append(node)
            if self.CIRCLE_SEARCH_BRANCHES and hasattr(self, 'viz') and self.viz.USE_INTERFACE: ##
                self.writer.clear()
                self.viz.circle_tiles(tiles=visited_states, CLEAR=True)
                self.viz.screen.update()

        ## max reward problem
        elif self.MODE in ['ROOM', 'MACRO']:

            if self.MODE == 'ROOM':
                visited_states.reverse()
                visited_states = str(visited_states).replace(', ', '-').replace('[', '').replace(']', '')
                self.node_values[node.state] += f'{visited_states}: {round(node.g, 2)}\n'

            elif self.MODE == 'MACRO':
                state, history, countdown = node.state

                def macro_to_rooms(macro):
                    s, h, t = macro
                    h = list(h)
                    h.append(s)
                    return tuple([s[1] for s in h if s[0] == 'explore'])

                if state[0] == 'explore':
                    rooms = macro_to_rooms(node.state)
                    self.node_values[state[1]] += f'{rooms}: {round(node.g, 2)}\n'

                    if node.parent != None:
                        parent = node.parent.state
                        rooms_parent = macro_to_rooms(parent)
                        reward = self.R(parent, state)
                        cost = parent[2] - countdown
                        self.edge_labels[(rooms_parent, rooms)] = (round(reward, 1), round(cost, 1))

            if self.SHOW_SEARCH_BRANCHES and hasattr(self, 'viz') and self.viz.USE_INTERFACE:
                self.writer.clear()
                self.draw_node_value()
                self.viz.screen.update()

    def draw_node_value(self):
        if (not self.viz.USE_INTERFACE or not self.PRINT_V): return
        ## tilelevel a*
        if self.goal_state_action != None:
            ROOM = False
            style = ("Courier", self.viz.ts // 4, 'normal')

        ## roomlevel a*
        else:
            ROOM = True
            style = ("Courier", self.viz.ts // 3, 'normal')

        for tile, value in self.node_values.items():
            self.viz.go_to_pos(self.writer, tile, x_shift=self.viz.ts/2, y_shift=self.viz.ts//2, ROOM=ROOM)
            self.writer.write(value, font=style)

    def runs(self, state, verbose=False):
        """ try out multiple parameters so at least one of them got good results"""

        verbose = verbose or self.verbose
        self._timeout = 0.5  ## 0.1
        this = time.time()

        shortest_plan = [[],[]]
        shortest_failed_plan = [[],[]]
        params = [(0.3, 0.01)] ##, (0.5, 0.0001), (0.2, 0.0001), (0.15, 0.0002), (0.1, 0.0005), (0.05, 0.001), (0, 0.005)]
        params = [(0.5, 0.001)]
        for param in params:
            self._living_cost, self._walking_cost = param
            self.reset()
            logs = self.run(state)[1]
            num_expansions, succeed = logs['node_expansions'], logs['succeed']

            plan_len = len(self._plan)
            # print(f'Finished A* with param {param}, expanded {num_expansions} nodes, got {succeed} plan of length {plan_len}')
            if succeed and ((shortest_plan == [[],[]] or plan_len < len(shortest_plan[0]))):
                shortest_plan = self._plan, self._path, param, succeed
            if not succeed and ((shortest_failed_plan == [[],[]] or plan_len < len(shortest_failed_plan[0]))):
                shortest_failed_plan = self._plan, self._path, param, succeed

            if verbose:
                print('planning time', round(time.time()-this, 2))
                this = time.time()

        if shortest_plan != [[],[]]:
            self._plan, self._path, param, succeed = shortest_plan
        else:
            self._plan, self._path, param, succeed = shortest_failed_plan
        if verbose: print(f'Found {succeed} A* plan of length {len(self._plan)} with param {param} in {round(time.time()-this, 2)} seconds')

        self.get_QVP()

        ## visualize roomlevel path
        if self.SHOW_ROOMLEVEL_PLAN:
            self.draw_rollout_path(CLEAR=True)

            ## for taking screenshot of the roomlevel plan
            # self.viz.screen.update()
            # self.viz.take_screenshot(FINAL=True, CROP=True)
            # exit()

            if self.SHOW_SEARCH_BRANCHES:
                self.draw_node_value()

    def run(self, state, verbose=False):
        # if isinstance(state[1], tuple):
        #     self.USE_HISTORY = True
        if self.MODE == 'MACRO':
            self.countdown_original = state[-1]

        ## augment the state space to be (state, tiles observed, states visited, victims to save)
        if self.MODE == 'OBS' and not isinstance(state[1], tuple):
            state = (state, tuple([]), tuple([state[0]]), False)

        print_threshold = 0.1
        problem = f'\n [{self.name}] [{self.MODE}] from {state} to {self.goal_state_action}\n'

        verbose = self.verbose or self.DEBUG or verbose
        if verbose: print()
        self.nodes_explored = []

        start_time = time.time()
        queue = []
        state_to_best_g = defaultdict(lambda : float("inf"))
        tiebreak = count()

        # root_parent = self.Node(state=None, parent=None, action=None, g=0, h=0)
        root_node = self.Node(state=state, parent=None, action=None, g=0, h=self._heuristic(state))
        self.register_node_value(root_node)
        hq.heappush(queue, (self._get_priority(root_node), next(tiebreak), root_node))
        num_expansions = 0

        while len(queue) > 0 and (self.DEBUG or (time.time() - start_time < self._timeout)):
            _, _, node = hq.heappop(queue)
            # verbose = self.verbose or self.DEBUG or verbose

            # If we already found a better path here, don't bother
            if state_to_best_g[node.state] < node.g:
                continue

            if self.MODE == 'MACRO' and len(node.state[1]) > self._max_num_steps:
                continue

            # If the goal holds or termination state reached, return
            if self._check_goal(node, verbose=True):
                time_passed = round(time.time() - start_time, 2)
                if verbose and time_passed > print_threshold: ## or True:
                    print(f"{problem}Plan found after expanding {num_expansions} nodes! time passed {time_passed}/{self._timeout}\n")
                return self._finish_plan(node), {'node_expansions' : num_expansions, 'succeed':True}

            if num_expansions > 100000: break

            num_expansions += 1
            if verbose: print(f"Expanding node {num_expansions}", end='\r', flush=True)
            # Generate successors
            for action, child_state in self._get_successors(node.state, verbose=verbose):
                if self.check_loop(child_state, node) or child_state == None:
                    if verbose: print('loop detected')
                    continue

                g = self.get_g(node, child_state, action)

                ## If we already found a better path to child, don't bother
                if self.goal_state_action != None and state_to_best_g[child_state] <= g: #node.g+1:
                    # if verbose: print('already found a better path to child')
                    continue

                # Add new node
                visited_states = self.get_visited_nodes(node)[1]
                h = self._heuristic(child_state, visited_states=visited_states, last_state=node.state, g=g)
                child_node = self.Node(state=child_state, parent=node, action=action, g=g, h=h) #node.g+1)
                # if verbose: print(child_state, action, g, num_expansions)
                self.register_node_value(child_node)

                priority = self._get_priority(child_node)
                hq.heappush(queue, (priority, next(tiebreak), child_node))
                state_to_best_g[child_state] = child_node.g

        time_passed = round(time.time() - start_time, 2)
        best_node = node
        best_node_obs = -1
        if self.MODE in ['MACRO', 'OBS']:  ##self.USE_HISTORY:
            for node in self.nodes_explored:
                if len(node.state[1]) > best_node_obs:
                    best_node_obs = len(node.state[1])
                    best_node = node
            node = best_node
        failed_plan = self._finish_plan(node)
        if verbose and time_passed > print_threshold: ## or True:
            print(f"{problem}Warning: planning failed. Best node gives {best_node_obs} observations in {len(failed_plan)} steps. "
                  f"node expanded {num_expansions}, time passed {time_passed}/{self._timeout}")
        return failed_plan, {'node_expansions' : num_expansions, 'succeed':False}

    def check_loop(self, child_state, node):
        """ check loop when planning roomlevel path """
        if self.MODE != 'ROOM': return False
        nodes, states = self.get_visited_nodes(node)

        ## the agent cannot triage multiple times at a single place
        to_triage = {}
        for node in nodes:
            if node.action == 'triage':
                if node.state not in to_triage:
                    to_triage.append(node.state)
                else: return False
        if child_state in to_triage: return False

        ## the agent is going to a node it has been fully explored
        fully_explored = []
        path = set(states)
        for state in path:
            explored = True
            for neighbor in self._available_actions[state]:
                if neighbor not in path:
                    explored = False
            if explored: fully_explored.append(state)

        return child_state in fully_explored

    def get_visited_nodes(self, node):
        ## get the list of rooms already visited
        nodes = [node]
        states = [node.state]
        while node.parent != None:
            node = node.parent
            nodes.append(node)
            states.append(node.state)
        if self.MODE in ['MACRO', 'OBS']: ##self.USE_HISTORY:
            states = [s[0] for s in states]

        return nodes, states

    def get_g(self, node, child_state, action):
        g = node.g + self._living_cost
        if self.MODE == 'OBS': ## isinstance(self.goal_state_action, list):  ## observing path
            if len(node.state) == 4 or self._room_tiles != None:  ## planning both observing and triaging
                r = self.R(child_state[0], action, visited=child_state[2])  ## not giving reward when visited again
                if r > 0:
                    # print(child_state[0], r)
                    g -= r
            return g

        elif self.MODE == 'TILE': ##isinstance(sel
            # f.goal_state_action, tuple):
            return g
            # g -= self.R(child_state, action)

        elif self.MODE == 'ROOM': ##
            g -= self.R(node.state, child_state, walking_cost=self._walking_cost,
                               visited_room_idx=self.get_visited_nodes(node)[1])

        elif self.MODE == 'MACRO':  ##
            g = g - self.R(node.state, action, verbose=self.verbose)  ## - self._living_cost

        return g

    def _get_successors(self, state, verbose=False):
        if self.MODE == 'MACRO': ## Macro planner
            children = self._actions(state[0], state[1])  ## each is a state
            actions = list(children.keys())

            padding = ""
            for i in range(len(state[1])):
                padding += "   "
            if verbose:
                aa = copy.deepcopy(actions)
                aa = [(a[1], a[2][0]) for a in aa if a[0] == 'explore']
                hh = [(h[1], h[2][0]) for h in state[1] if h[0] == 'explore']
                if state[0][0] == 'explore': hh.append((state[0][1], state[0][2][0]))
                if verbose and False:
                    print(f'{padding} depth {len(state[1])} macros {hh} has {len(children)} children: {aa}')

            for action in actions:
                next_state = self.T(state, action)
                # if verbose: print(f'        action {action} leads to {next_state}')
                yield action, next_state

        else:
            for action in self._actions:
                if self.is_available(state, action):
                    next_state = self.T(state, action)
                    yield action, next_state

    def get_plan(self, node):
        """ find the path for both tilelevel and roomlevel planning """
        plan = []
        path = []
        self.nodes = {}
        while node.parent is not None:
            self.nodes[(node.parent.state, node.action)] = node
            plan.append(node.action)
            path.append(node.state)
            node = node.parent
        self.nodes[(None, node.state)] = node
        path.append(node.state)
        plan.reverse()
        path.reverse()
        if isinstance(self.goal_state_action, tuple):
            plan.append(self.goal_state_action[1])

        self._plan = plan
        self._path = path
        if self.MODE in ['OBS']: ## self.USE_HISTORY:
            self._path = [p[0] for p in self._path]
        # elif self.MODE == 'MACRO':
        #     print('get_plan', self.edge_labels)

    def get_QVP(self):

        if self.MODE in ['OBS']: return

        ## initialize Q, V, pi
        self.pi = {}
        self.V = {}
        self.Q = {}

        ## max reward planning: get V,Q,pi for roomlevel planning
        if self.MODE == 'ROOM':
            ## need one entry for each room
            for room in self._actions:
                self.pi[room] = None
                self.V[room] = -100
                self.Q[room] = {}

        # final = self.nodes[(self._path[-2], self._plan[-1])].g
        visited_states = []
        macro_V = []
        for i in range(len(self._plan)):
            state, action = self._path[i], self._plan[i]
            visited_states.append(state)

            if state not in self.pi or self.pi[state] == None:
                if self.MODE == 'MACRO':
                    self.pi[state[0]] = action
                else:
                    self.pi[state] = action

            if state not in self.V or self.V[state] == None:
                self.V[state] = -100
                self.Q[state] = {}

            if action not in self.Q[state]:
                if self.MODE == 'ROOM':
                    self.Q[state][action] = self.R(state, action, walking_cost=self._walking_cost,
                                                   visited_room_idx=visited_states)  ##final - self.nodes[(state, action)].g
                    self.V[action] = max(self.V[action], self.Q[state][action])

                elif self.MODE == 'MACRO':
                    macro_V.append(self.R(state, action))
                    self.V[action] = self.R(state, action)

                else:
                    self.Q[state][action] = self.R(state, action)
                    self.V[state] = max(self.V[state], self.Q[state][action])

        ## macro-planning: q value
        if self.MODE == 'MACRO':
            if len(self._plan) > 0:
                state, action = self._path[0], self._plan[0]
                self.V[state] = sum([macro_V[i] * self._gamma**i for i in range(len(macro_V))])
                self.Q[state][action] = self.V[state]
            else:
                print(f'didnt find a plan after {self._timeout} seconds')

    def _finish_plan(self, node):

        ## find the path for both tilelevel and roomlevel planning
        self.get_plan(node)
        self.get_QVP()

        return self._plan

    def _get_priority(self, node):
        if self.EXHAUSTIVE:
            return (1,0)
        if self.MODE == 'OBS' and self._room_tiles != None:
            print(node.state, round(node.g, 2), node.h)
        return (node.g + self._beta * node.h, node.h)

    def get_VX(self, state):
        if state in self._plan: return self.V[state]
        return -1

class UCT(Planner):
    """ Implementation of UCT based on Leslie's lecture notes """

    def __init__(self, actions, T, R, max_num_steps=100, beta=1,
                 num_simulations=1000, gamma=0.8, seed=0, timeout=3, mode='ave'):
        self.name = 'UCT'
        self.mode = mode

        self._actions = actions
        self.R = R
        self.T = T

        self._max_num_steps = max_num_steps
        self._num_simulations = num_simulations
        self._gamma = gamma
        self._timeout = timeout

        self._rng = np.random.RandomState(seed)
        self.Q = defaultdict(lambda: defaultdict(lambda: defaultdict(float)))
        self._N = defaultdict(lambda: defaultdict(lambda: defaultdict(int)))
        self.VX = defaultdict(float)
        self._beta = beta  ## coeff of UCT exploration term

        self.mission_stats = {
            'timeout at iteration': [],
            'max # visits': [],
            'max exploi2explor ratio': [],
            'max # visits (last)': -np.inf,
            'max exploi2explor ratio (last)': -np.inf,
        }

    def get_specific_setting(self):
        ps = {}
        ps['beta (exp)'] = self._beta
        ps['gamma'] = self._gamma
        ps['max # simulations'] = self._num_simulations
        return ps

    def reset(self):
        # Initialize Q[s][a][d] -> float
        self.Q = defaultdict(lambda: defaultdict(lambda: defaultdict(float)))

        # Initialize N[s][a][d] -> int
        self._N = defaultdict(lambda: defaultdict(lambda: defaultdict(int)))

        # Initialize V[s] -> float
        self.VX = defaultdict(float)

        ## if it's MacroPlanner
        self.macros = {}

    def update_param(self, num_simulations=None, gamma=None, beta=None):
        if num_simulations!=None: self._num_simulations = num_simulations
        if gamma!=None: self._gamma = gamma
        if beta!=None: self._beta = beta

    def get_VX(self, state):
        if state in self.VX:
            return self.VX[state]
        return -1

    def get_actions(self, s, DRAW=False):
        if isinstance(self._actions, tuple):
            actions = self._actions
        else:  ## is a function
            children = self._actions(s[0], s[1])  ## each is a state
            self.macros.update(children)
            actions = list(children.keys())
            if DRAW and self.DRAW_MACRO_SIBLINGS:  ## draw the goals and paths
                self.viz.draw_macro_children(s[0], children=children, CLEAR=True)
        return actions

    def run(self, state, verbose=False):
        start = time.time()
        self.reset()

        # loop search
        TIMED_OUT = False
        for it in range(self._num_simulations):
            # Update Q
            self._search(state, 0, horizon=self._max_num_steps, verbose=verbose)
            if time.time() - start > self._timeout:
                TIMED_OUT = True
                self.mission_stats['timeout at iteration'].append(it)
                break
        if not TIMED_OUT: self.mission_stats['timeout at iteration'].append(self._num_simulations)
        self.mission_stats['max # visits'].append(self.mission_stats['max # visits (last)'])
        self.mission_stats['max exploi2explor ratio'].append(self.mission_stats['max exploi2explor ratio (last)'])

        ## if muct, store/print all final q value
        if isinstance(state[0], tuple):
            if verbose: print(f'\n------------ Q value after {round(time.time()-start, 2)} s {self._num_simulations} simulations of MCTS -------------')
            self.Qsa_all = {}
            index = 0
            for s in self.Q:
                history = list(s[1])
                history.append(s[0])
                history = tuple(history)
                self.Qsa_all[history] = {}
                for a in self.Q[s]:
                    for depth in self.Q[s][a]:
                        if a in self.Qsa_all[history]:
                            print(f'\n\n\n\n\n overwriting self.Qsa[{history}][{a}] from {self.Qsa_all[history][a]} to {round(self.Q[s][a][depth], 3)}')
                        self.Qsa_all[history][a] = round(self.Q[s][a][depth], 3)
                        if verbose:
                            trace = self.T(self.T(s, a), 'None')[1]
                            print(f'index {index} | length {depth} - state {trace}   |   value {self.Q[s][a][depth]}')
                            index += 1

        ## extract greedy plan
        plan = []
        path = []
        max_num_steps = 10
        self.Qsa = {}
        for t in range(max_num_steps):
            action = None
            if isinstance(state[0], tuple):  ## muct
                max_Q = -np.inf
                history = list(state[1])
                history.append(state[0])
                history = tuple(history)
                self.Qsa[history] = {}
                for a in self.get_actions(state):
                    q = self.Q[state][a][t]
                    if q > max_Q:
                        max_Q = q
                        action = a

                    self.Qsa[history][a] = round(q, 3)
            else:
                action = max(self._actions, key=lambda a: (self.Q[state][a][t], self._rng.uniform()))

            # action = max(self.get_actions(state), key=lambda a: (self.Q[state][a][t], self._rng.uniform()))
            self.VX[state] = self.Q[state][action][t]
            state = self.T(state, action)
            if state in path:
                break
            plan.append(action)
            path.append(state)
        self._plan = plan
        self._path = path

    def get_action(self, state, replan=True, verbose=False):
        if replan:
            self.run(state, verbose=verbose)
            self._t = 0
        else:
            self._t += 1

        # Return best action, break ties randomly
        t = self._t
        action = max(self.get_actions(state, DRAW=True), key=lambda a : (self.Q[state][a][t], self._rng.uniform()))
        # for a in self.Q[state]:
        #     self.R(state, a, verbose=True)

        to_print = '  |  '
        for a, di in self.Q[state].items():
            to_print += f'{a}: {str(round(di[t],3))}  | '
        self.log(f'... with Q value {to_print}')

        # if verbose: sys.exit()
        return action

    def _search(self, s, depth, horizon=100, verbose=False):

        # Base case
        if depth == horizon:
            return 0.

        # Select an action, balancing explore/exploit
        a = self._select_action(s, depth, horizon=horizon)

        # Create a child state
        next_state = self.T(s, a)
        if not next_state: ## game finished
            return self.R(s, a)

        # Get value estimate
        q = self.R(s, a) + self._gamma * self._search(next_state, depth+1, horizon=horizon, verbose=verbose)
        if verbose and s==((102, 0), ((59, 0),)):
            print(f'searching depth {depth} - state {s}    |    choose action {a}    |   value {q}')

        # Update values and counts
        num_visits = self._N[s][a][depth] # before now

        # First visit to (s, a, depth)
        if num_visits == 0:
            self.Q[s][a][depth] = q
        # We've been here before
        else:
            # Running average
            if self.mode == 'ave':
                self.Q[s][a][depth] =  (num_visits / (num_visits + 1.)) * self.Q[s][a][depth] + \
                                       (1 / (num_visits + 1.)) * q
            elif self.mode == 'max':
                self.Q[s][a][depth] = max(self.Q[s][a][depth], q)

            else:
                assert "unknown mode for UCT"

        # Update num visits
        self._N[s][a][depth] += 1
        if num_visits+1 > self.mission_stats['max # visits (last)']:
            self.mission_stats['max # visits (last)'] = num_visits+1

        return self.Q[s][a][depth]

    def _select_action(self, s, depth, horizon):
        actions = self.get_actions(s)

        # If there is any action where N(s, a, depth) == 0, try it first
        untried_actions = [a for a in actions if self._N[s][a][depth] == 0]
        if len(untried_actions) > 0:
            index = self._rng.choice(len(untried_actions))
            return untried_actions[index]
            # return self._rng.choice(untried_actions)
        # Otherwise, take an action to trade off exploration and exploitation
        N_s_d = sum(self._N[s][a][depth] for a in actions)
        best_action_score = -np.inf
        best_actions = []
        for a in actions:
            explore_bonus = (np.log(N_s_d) / self._N[s][a][depth])**((horizon + depth) / (2*horizon + depth))
            score = self.Q[s][a][depth] + explore_bonus * self._beta
            if score > best_action_score:
                best_action_score = score
                best_actions = [a]
            elif score == best_action_score:
                best_actions.append(a)

            if self._beta != 0:
                ratio = self.Q[s][a][depth] / (explore_bonus * self._beta)
                if ratio > self.mission_stats['max exploi2explor ratio (last)']:
                    self.mission_stats['max exploi2explor ratio (last)'] = ratio

        index = self._rng.choice(len(best_actions))
        return best_actions[index]
        # return self._rng.choice(best_actions)

class RTDP(Planner):
    """Implementation of RTDP based on Blai and Hector's 2003 paper
        Labeled RTDP: Improving the Convergence of Real-Time Dynamic Programming
    """
    def __init__(self, actions, T, R, num_simulations=1000, initial_Q=0, epsilon_exp=0.2,
                 epsilon_con=0.01, timeout=10, max_num_steps=250, seed=0, gamma=0.95):

        self.name = "RTDP"
        self._actions = actions
        self.T = T
        self.R = R

        self._num_simulations = num_simulations
        self._epsilon_con = epsilon_con
        self._epsilon_exp = epsilon_exp
        self._max_num_steps = max_num_steps
        self._timeout = timeout
        self._gamma = gamma
        self._initial_Q = initial_Q

        self._rng = np.random.RandomState(seed)
        self.V = {}

        self.mission_stats = {
            'converge at iteration': [],
            'timeout at iteration': [],
        }

    def get_specific_setting(self):
        ps = {}
        ps['epsilon (convergence)'] = self._epsilon_con
        ps['epsilon (exploration)'] = self._epsilon_exp
        ps['initial Q (exploration)'] = self._initial_Q
        ps['gamma'] = self._gamma
        ps['max # simulations'] = self._num_simulations
        return ps

    def reset(self):
        self.V = {}

    def get_Qs(self, state):
        Qs = []
        for a in self._actions:
            Qs.append(self._get_Q(state, a))
        return Qs

    def get_VX(self, state):
        if state in self.V: ##self._path:
            return self.V[state]
        return -1

    def run(self, state):
        self.reset()
        self.RTDP_trials(state)

    def get_action(self, state, replan=True):
        if replan or len(self._path) == 0:
            self.run(state)

        if state == self._path[0]:
            act = self._plan.pop(0)
            self._path.pop(0)
            return act
        else:
            return self._get_greedy_action(state)

    def _get_V(self, state):
        if state not in self.V:
            self.V[state] = self._initial_Q
        return self.V[state]

    def _get_Q(self, state, a):
        return self.R(state, a) + self._gamma*self._get_V(self.T(state, a))

    def _update_q(self, state, action):
        self.V[state] = self._get_Q(state, action)

    def _get_greedy_action(self, state):
        actions = [a for a in self._actions if self.is_available(state, a)]
        return max(actions, key=lambda a : (self._get_Q(state, a), self._rng.uniform()))

    def RTDP_trials(self, initial_state):
        ## update Values through simulations
        start_time = time.time()
        for i in range(self._num_simulations): ## tqdm(range(self._num_simulations), desc='RTDP simulations'):
            plan = []
            path = []
            last_V = copy.deepcopy(self.V)
            state = initial_state
            for t in range(self._max_num_steps):
                action = self._get_greedy_action(state)
                plan.append(action)
                path.append(state)
                self._update_q(state, action)
                state = self.T(state, action)

            if time.time() - start_time > self._timeout:
                self.log(f'.... stopped RTDP after {i} simulations (timeout = {self._timeout})')
                self.mission_stats['timeout at iteration'].append(i)
                break

            ## check if V converged
            converged = len(last_V) == len(self.V)
            for s in last_V:
                if abs(self._get_V(s) - last_V[s]) > self._epsilon_con:
                    converged = False
            if converged:
                self.log(f'.... RTDP converged after {i} simulations')
                self.mission_stats['converge at iteration'].append(i)
                break

        ## take the plan at last iteration as the plan
        self._plan = plan
        self._path = path

    def _get_residual(self, state):
        # print(self._get_V(state), self._get_Q(state, self._get_greedy_action(state)))
        action = self._get_greedy_action(state)
        return abs(self._get_V(state) - self._get_Q(state, action))

class LRTDP(Planner):
    """Implementation of LRTDP based on Blai and Hector's 2003 paper
        Labeled RTDP: Improving the Convergence of Real-Time Dynamic Programming
    """
    def __init__(self, actions, T, R, num_simulations=1000, initial_Q=0, epsilon_exp=0.2,
                 epsilon_con=0.01, timeout=10, max_num_steps=25, seed=0, gamma=0.9, available_actions=None):

        super().__init__()
        self.name = 'LRTDP'
        self._actions = actions
        self.T = T
        self.R = R
        self.wrapped = RTDP(actions, T, R, num_simulations, initial_Q, epsilon_exp, epsilon_con,
                            timeout, max_num_steps, seed, gamma)
        self._solved = {}
        self._timeout = timeout
        self._max_num_steps = max_num_steps
        self._rng = np.random.RandomState(seed)
        self._available_actions = available_actions

        self.mission_stats = {
            '# solved states': [],
            'solved states ratio': [],
            'timeout at iteration': [],
            'converge at iteration': [],
        }

    def get_specific_setting(self):
        ps = {}
        ps['epsilon (convergence)'] = self.wrapped._epsilon_con
        ps['epsilon (exploration)'] = self.wrapped._epsilon_exp
        ps['gamma'] = self.wrapped._gamma
        ps['max # simulations'] = self.wrapped._num_simulations
        return ps

    def set_plan(self, path, plan):
        self._path = path
        self._plan = {}
        for i in range(len(plan)):
            if path[i] not in self._plan:
                self._plan[path[i]] = plan[i]

    def reset(self):
        self.wrapped.reset()
        self._solved = {}

    def run(self, state):
        self.reset()
        self.LRTDP_trials(state)
        self.get_plan(state)

    def get_action(self, state, replan=True):
        if replan:
            self.run(state)

        if state in self._plan:
            return self._plan[state]
        else:
            return self._get_greedy_action(state)

    def _get_greedy_action(self, state):
        return self.wrapped._get_greedy_action(state)

    def get_plan(self, state):
        ## make the whole plan
        plan = []
        path = []
        for t in range(self._max_num_steps):
            action = self._get_greedy_action(state)
            plan.append(action)
            path.append(state)
            state = self.T(state, action)
        self._plan = plan
        self._path = path
        self.wrapped._plan = plan
        self.wrapped._path = path

    def get_Qs(self, state):
        return self.wrapped.get_Qs(state)

    def get_VX(self, state):
        return self.wrapped.get_VX(state)

    def _get_solved(self, state):
        if state not in self._solved:
            self._solved[state] = False
        return self._solved[state]

    def _get_residual(self, state):
        return self.wrapped._get_residual(state)

    def _percentage_solved(self, passed):
        count = 0
        for s in self._solved:
            if self._solved[s]:
                count += 1
        ratio = round(count/len(self._solved), 3)
        comment = f'{count} out of {len(self._solved)} ({ratio})'
        self.log(f'... LRTDP solved {comment} states in {passed} seconds')
        self.mission_stats['# solved states'].append(count)
        self.mission_stats['solved states ratio'].append(ratio)

    def _check_solved(self, state):
        rv = True
        open = []
        closed = []
        if not self._get_solved(state):
            open.append(state)

        while len(open) > 0:
            s = open[len(open)-1]
            open.remove(s)
            closed.append(s)

            ## check residual
            residual = self._get_residual(s)
            if residual > self.wrapped._epsilon_con:
                # print(len(open), len(closed), list(self._solved.keys()).index(s), residual, self.wrapped._epsilon)
                rv = False
                continue

            ## expand state
            action = self.wrapped._get_greedy_action(s)
            next = self.T(s, action)
            if (not self._get_solved(next)) and (next not in open and next not in closed):
                open.append(next)

        if rv:
            ## label relevant states
            for s in closed:
                self._solved[s] = True
        else:
            ## update states with residuals and ancestors
            while len(closed) > 0:
                s = closed[len(closed) - 1]
                closed.remove(s)
                action = self.wrapped._get_greedy_action(s)
                self.wrapped._update_q(s, action)
        return rv

    def LRTDP_trials(self, initial_state):
        start_time = time.time()
        self._check_solved(initial_state)
        count = 0
        while not self._get_solved(initial_state):
            state = initial_state
            visited = []
            last_V = copy.deepcopy(self.wrapped.V)
            while not self._get_solved(state):
                visited.append(state)
                action = self.wrapped._get_greedy_action(state)
                self.wrapped._update_q(state, action)
                state = self.T(state, action)

                if len(visited) > self._max_num_steps:
                    break

            count += 1
            if time.time() - start_time > self._timeout:
                self.log(f'.... stopped LRTDP after solving {count} simulations (timeout = {self._timeout})')
                self.mission_stats['timeout at iteration'].append(count)
                break

            ## try labeling visited states in reverse order
            while len(visited) > 0:
                s = visited[len(visited)-1]
                visited.remove(s)
                if not self._check_solved(s):
                    break

            ## check if V converged
            converged = len(last_V) == len(self.wrapped.V)
            for s in last_V:
                if abs(self.wrapped._get_V(s) - last_V[s]) > self.wrapped._epsilon_con:
                    converged = False
            if converged:
                self.log(f'.... LRTDP converged after solving {count} simulations')
                self.mission_stats['converge at iteration'].append(count)
                break

        ## print how many states are solved
        self._percentage_solved(passed=round(time.time() - start_time, 2))

# class LRRTDP(Planner):
#     """Implementation of LRTDP based on Blai and Hector's 2003 paper
#         Labeled RTDP: Improving the Convergence of Real-Time Dynamic Programming
#     """
#     def __init__(self, actions, T, R, num_simulations=100,
#                  epsilon = 0.01, timeout=10, max_num_steps=25, seed=0, gamma=0.9):
#
#         super().__init__()
#         self.name = 'LRTDP'
#         self.T = T
#         self.R = R
#         self.wrapped = RTDP(actions, T, R, num_simulations,
#                  epsilon, timeout, max_num_steps, seed, gamma)
#         self._solved = {}
#         self._timeout = timeout
#         self._max_num_steps = max_num_steps
#
#         self._timeouts = {}
#
#     def LRRTDP_trials(self, initial_state):
#         ## update Values through simulations
#         start_time = time.time()
#         plan = []
#         path = []
#         for i in range(self._num_simulations): ## tqdm(range(self._num_simulations), desc='RTDP simulations'):
#             last_V = copy.deepcopy(self.V)
#             state = initial_state
#             for t in range(self._max_num_steps):
#                 action = self._get_greedy_action(state)
#                 plan.append(action)
#                 path.append(state)
#                 self._update_q(state, action)
#                 state = self.T(state, action)
#
#             if time.time() - start_time > self._timeout:
#                 self.log(f'.... stopped RTDP after {t} iterations (timeout = {self._timeout})')
#                 break
#
#             ## check if V converged
#             converged = len(last_V) == len(self.V)
#             for s in last_V:
#                 if abs(self._get_V(s) - last_V[s]) > self._epsilon:
#                     converged = False
#             if converged:
#                 self.log(f'.... RTDP converged after {i} simulations')
#                 break
#             plan = []
#             path = []

class PI(Planner):
    def __init__(self, states, actions, T, R,
                 max_num_steps=150, gamma=0.9, epsilon_con=0.01, timeout=10, seed=0):

        self.name = 'PI'
        self._states = states
        self._actions = actions
        self.T = T
        self.R = R
        self.pi = None
        self.Q = None
        self.V = np.zeros((len(self._states),))

        self._max_num_steps = max_num_steps
        self._gamma = gamma
        self._epsilon_con = epsilon_con
        self._timeout = timeout

        self._rng = np.random.RandomState(seed)
        self.step = 0

        self.mission_stats = {
            'converge at iteration': [],
            'timeout at iteration': [],
        }

    def get_specific_setting(self):
        ps = {}
        ps['epsilon (convergence)'] = self._epsilon_con
        ps['gamma'] = self._gamma
        return ps

    def set_states(self, states):
        self._states = states
        self.reset()

    def reset(self):
        self.V = np.zeros((len(self._states),))
        self.pi = np.zeros((len(self._states), len(self._actions)))

    def get_VX(self, state):
        if state in self._path:
            return self.V[self._states.index(state)]
        return -1

    def _get_greedy_action(self, s):
        state = self._states.index(s)
        # policy = self.pi[state, :]
        # action = random.choice(np.argwhere(policy == np.amax(policy)).flatten().tolist())
        action = max(range(len(self._actions)), key=lambda a: (self.pi[state, a], self._rng.uniform()))
        a = self._actions[action]
        # s_new = self.T(s, a)
        # if isinstance(s_new, tuple):  ## when on the tile level
        #     while s_new == s and a == 'go_straight':  ## TODO: this is quite artificial
        #         self.pi[state, action] = -1
        #         a = self._get_greedy_action(s)
        #         s_new = self.T(s, a)
        return a

    def _normalize_Q(self):
        self.Q = np.true_divide(self.Q, np.max(self.Q))
        # self.V = np.true_divide(self.V, np.max(self.V))

    def get_plan(self, s):
        ## make a whole plan to be printed
        plan = []
        path = []
        for t in range(self._max_num_steps):
            a = self._get_greedy_action(s)
            s_new = self.T(s, a)
            plan.append(a)
            path.append(s)
            ## when running on tilelevel in hiearchical planning, there's no policy outside room
            if s_new not in self._states: break
            if a == 'triage': break  ## going to replan any way
            s = s_new
        self._plan = plan
        self._path = path

    def get_action(self, s, replan=True):

        if replan or len(self._path) == 0:
            self.run(s)

        if s == self._path[0]:
            a = self._plan.pop(0)
            self._path.pop(0)
        else:
            a = self._get_greedy_action(s)

        self.log(f'... with policy {self.pi[self._states.index(s), :]} ...')
        return a

    def run(self, s):
        self.policy_iteration()
        self.get_plan(s)

    def policy_iteration(self):
        """ performs value iteration and uses state action value to extract greedy policy for gridworld """

        start = time.time()

        ## save the value as a dictionary as it will be used repeatedly
        T = {}
        def get_T(s, a):
            if (s, a) not in T:
                next_s = self.T(self._states[s], self._actions[a])
                T[(s, a)] = self._states.index(next_s)
            return T[(s, a)]

        R = {}
        def get_R(s, a):
            if (s, a) not in R:
                R[(s, a)] = self.R(self._states[s], self._actions[a])
            return R[(s, a)]

        ## for iteration up to max_iter
        max_iter = self._max_num_steps
        for iter in range(max_iter):

            ## ------------------------------
            # Step 2 -- policy evaluation: compute V(s) given pi
            ## ------------------------------
            for s in range(len(self._states)):
                a = self._actions.index(self._get_greedy_action(self._states[s]))
                s_p = get_T(s, a)
                self.V[s] = get_R(s, a) + self._gamma * self.V[s_p]

            ## ------------------------------
            # Step 3 -- Policy improvement: compute greedy policy pi that maximize V(s')
            ## ------------------------------
            V_p = np.zeros((len(self._states),))
            pi_new = np.zeros((len(self._states), len(self._actions)))
            for s in range(len(self._states)):
                V_temp = []
                for a in range(len(self._actions)):
                    s_p = get_T(s, a)
                    V_temp.append(get_R(s, a) + self._gamma * self.V[s_p])
                a_max = (V_temp == np.amax(V_temp)).astype(float)

                pi_new[s, :] = a_max / np.sum(a_max)
                V_p[s] = np.amax(V_temp)
            self.pi = pi_new

            # termination condition
            if time.time()-start > self._timeout:
                self.mission_stats['timeout at iteration'].append(iter)
                break

            # Stop if V(s') = V_p is close enough to V(s)
            delta = np.amax(np.abs(V_p - self.V))
            if delta < self._epsilon_con:
                self.log('converge after ', iter, ' iterations')
                self.mission_stats['converge at iteration'].append(iter)
                break

class VI(Planner):
    def __init__(self, states, actions, T, R,
                 max_num_steps=150, gamma=0.9, epsilon_con=0.01, timeout=10, seed=0,
                 available_actions=None):

        self.name = 'VI'
        self._states = states
        self._actions = actions
        self._available_actions = available_actions

        self.T = T
        self.R = R
        self.pi = None
        self.Q = None
        self.V = np.zeros((len(self._states),))

        self._max_num_steps = max_num_steps
        self._gamma = gamma
        self._epsilon_con = epsilon_con  ## epsilon greedy method
        self._timeout = timeout

        self._rng = np.random.RandomState(seed)
        self.step = 0

        self.mission_stats = {
            'converge at iteration': [],
            'timeout at iteration': [],
        }

    def get_specific_setting(self):
        ps = {}
        ps['epsilon (convergence) '] = self._epsilon_con
        ps['gamma'] = self._gamma
        return ps

    def set_states(self, states):
        self._states = states
        self.reset()

    def reset(self):
        self.V = np.zeros((len(self._states),))

    def get_Qs(self, state):
        return self.Q[self._states.index(state)]

    def get_VX(self, state):
        if (state in self._states and self._states.index(state) in self.V):  ## state in self._path or
            return round(self.V[self._states.index(state)], 2)
        return -1

    def _get_greedy_action(self, s):
        state = self._states.index(s)
        # policy = self.pi[state, :]
        # action = random.choice(np.argwhere(policy == np.amax(policy)).flatten().tolist())
        action = max(range(len(self._actions)), key=lambda a: (self.pi[state, a], self._rng.uniform()))
            # action = max(self._actions, key=lambda a: (self.Q[state][a][t], self._rng.uniform()))
        a = self._actions[action]
        s_new = self.T(s, a)
        if isinstance(s_new, tuple):  ## when on the tile level
            while s_new == s and a == 'go_straight':  ## TODO: this is quite artificial
                self.pi[state, action] = -1
                a = self._get_greedy_action(s)
                s_new = self.T(s, a)
        return a

    def _normalize_Q(self):
        self.Q = np.true_divide(self.Q, np.max(self.Q))
        # self.V = np.true_divide(self.V, np.max(self.V))

    def get_plan(self, s):
        ## make a whole plan to be printed
        plan = []
        path = []
        for t in range(self._max_num_steps):

            a = self._get_greedy_action(s)
            s_new = self.T(s, a)
            plan.append(a)
            path.append(s)
            if s_new in path: break ## in need to visit the same state again
            ## when running on tilelevel in hiearchical planning, there's no policy outside room
            if s_new not in self._states: break
            if a == 'triage': break  ## going to replan any way
            s = s_new
        self._plan = plan
        self._path = path

    def get_action(self, s, replan=True):

        if replan or len(self._plan) == 0:
            self.run(s)
            print(f'===== replanned at {s} to path {self._path[0]} -> {self._path[-1]}')

        if s == self._path[0]:
            print(f']]]] choose at {s} from path {self._path[0]} -> {self._path[-1]}')
            a = self._plan.pop(0)
            self._path.pop(0)
        else:
            a = self._get_greedy_action(s)
            print(f'+++++ random action at {s} with self._path {self._path}')

        if isinstance(self.pi, np.ndarray) and s in self._states:
            self.log(f'... with policy {self.pi[self._states.index(s), :]} ...')
        return a

    def run(self, s):
        self.value_iteration()
        self.get_plan(s)

    def value_iteration(self):
        """ performs value iteration and uses state action value to extract greedy policy for gridworld """

        start = time.time()

        ## save the value as a dictionary as it will be used repeatedly
        T = {}
        def get_T(s, a):
            if (s, a) not in T:
                next_s = self.T(self._states[s], self._actions[a])
                # T[(s, a)] = self._states.index(next_s)
                if next_s in self._states:
                    T[(s, a)] = self._states.index(next_s) #self._actions[a]))
                else:  ## for tilelevel planning in hiearchical planning, boundary states are not counted
                    T[(s, a)] = None
            return T[(s, a)]

        R = {}
        def get_R(s, a):
            if (s, a) not in R:
                R[(s, a)] = self.R(self._states[s], self._actions[a])
            return R[(s, a)]

        # helper function
        def best_action(s):
            V_max = -np.inf
            for a in range(len(self._actions)):
                if self.is_available(self._states[s], self._actions[a]):  ## self.is_available(s,a):
                    s_p = get_T(s, a)
                    if s_p != None:
                        V_temp = get_R(s, a) + self._gamma * self.V[s_p]
                        V_max = max(V_max, V_temp)
            return V_max

        ## apply Bellman Equation until converge
        iter = 0
        while True:
            iter += 1
            delta = 0
            for s in range(len(self._states)):
                v = self.V[s]
                self.V[s] = best_action(s)
                delta = max(delta, abs(v - self.V[s]))

            # termination condition
            if delta < self._epsilon_con:
                self.log(f'... VI converged on {len(self._states)} states in {iter} iterations')
                self.mission_stats['converge at iteration'].append(iter)
                break
            if time.time()-start > self._timeout:
                self.mission_stats['timeout at iteration'].append(iter)
                break

        ## extract greedy policy
        self.pi = np.zeros((len(self._states), len(self._actions)))  # _actions)))
        self.Q = np.zeros((len(self._states), len(self._actions)))  # _actions)))

        for s in range(len(self._states)):
            Qs = np.zeros((len(self._actions),))

            for a in range(len(self._actions)):
                if self.is_available(self._states[s], self._actions[a]): ##self.is_available(s, a):
                    s_p = get_T(s, a)
                    if s_p != None:
                        Qs[a] = get_R(s, a) + self._gamma * self.V[s_p]
                        self.Q[s, a] = Qs[a]
                    else:
                        Qs[a] = -100 #-np.inf
                else:
                    Qs[a] = -1000

            ## highest state-action value
            Q_max = np.amax(Qs)

            ## collect all actions that has Q value very close to Q_max
            self.pi[s, :] = np.abs(Qs - Q_max) < 10 ** -3
            self.pi[s, :] /= np.sum(self.pi[s, :])
            self.pi[s, :] = softmax(Qs/ 0.01)
        #
        # w = open(f'recordings/planners-{self.step}-T-{len(self._states)}.txt', "w")
        # w.write(str(T))
        # w.close()
        #
        # w = open(f'recordings/planners-{self.step}-R-{len(self._states)}.txt', "w")
        # w.write(str(R))
        # w.close()

    # def value_iteration(self):
    #     """ performs value iteration and uses state action value to extract greedy policy for gridworld """
    #
    #     start = time.time()
    #
    #     ## save the value as a dictionary as it will be used repeatedly
    #     T = {}
    #     def get_T(s, a):
    #         if (s, a) not in T:
    #             next_s = self.T(self._states[s], self.action(a))
    #             # T[(s, a)] = self._states.index(next_s)
    #             if next_s in self._states:
    #                 T[(s, a)] = self._states.index(next_s) #self._actions[a]))
    #             else:  ## for tilelevel planning in hiearchical planning, boundary states are not counted
    #                 T[(s, a)] = None
    #         return T[(s, a)]
    #
    #     R = {}
    #     def get_R(s, a):
    #         if (s, a) not in R:
    #             R[(s, a)] = self.R(self._states[s], self.action(a)) #, self._actions[a])
    #         return R[(s, a)]
    #
    #     # helper function
    #     def best_action(s):
    #         V_max = -np.inf
    #         for a in range(len(self.actions(s))): ##len(self._actions)):
    #             s_p = get_T(s, a)
    #             if s_p != None:
    #                 V_temp = get_R(s, a) + self._gamma * self.V[s_p]
    #                 V_max = max(V_max, V_temp)
    #         return V_max
    #
    #     ## apply Bellman Equation until converge
    #     iter = 0
    #     while True:
    #         iter += 1
    #         delta = 0
    #         for s in range(len(self._states)):
    #             v = self.V[s]
    #             self.V[s] = best_action(s)
    #             delta = max(delta, abs(v - self.V[s]))
    #
    #         # termination condition
    #         if delta < self._epsilon_con:
    #             self.log(f'... VI converged on {len(self._states)} states in {iter} iterations')
    #             self.mission_stats['converge at iteration'].append(iter)
    #             break
    #         if time.time()-start > self._timeout:
    #             self.mission_stats['timeout at iteration'].append(iter)
    #             break
    #
    #     ## extract greedy policy
    #     if isinstance(self._actions, tuple):
    #         self.pi = np.zeros((len(self._states), len(self.actions(s)))) #_actions)))
    #         self.Q = np.zeros((len(self._states), len(self.actions(s)))) #_actions)))
    #     else:
    #         self.pi = np.zeros((len(self._states), len(self._states)))
    #         self.Q = np.zeros((len(self._states), len(self._states)))
    #
    #     for s in range(len(self._states)):
    #         if isinstance(self._actions, tuple):
    #             Qs = np.zeros((len(self.actions(s)),)) #_actions),))
    #         elif isinstance(self._actions, dict):
    #             Qs = np.ones((len(self._states),)) * -1000
    #         else:
    #             assert "what is the action space??"
    #
    #         for a in range(len(self.actions(s))): #_actions)):
    #             s_p = get_T(s, a)
    #             if s_p != None:
    #                 Qs[a] = get_R(s, a) + self._gamma * self.V[s_p]
    #                 self.Q[s, a] = Qs[a]
    #             else:
    #                 Qs[a] = -100 #-np.inf
    #
    #         ## highest state-action value
    #         Q_max = np.amax(Qs)
    #
    #         ## collect all actions that has Q value very close to Q_max
    #         self.pi[s, :] = np.abs(Qs - Q_max) < 10 ** -3
    #         self.pi[s, :] /= np.sum(self.pi[s, :])
    #         self.pi[s, :] = softmax(Qs/ 0.01)

class MacroPlanner(Planner):

    def __init__(self, macro_planner, env, countdown=60):
        self.name = 'Macro Planner'
        self._macro_planner = macro_planner
        self._max_num_steps_saved = self._macro_planner._max_num_steps
        self._env = env
        self._countdown = countdown
        self.mission_stats = {
        }

    def get_VX(self, state):
        return -1

    def get_setting(self):
        return self.name, {}

    def get_specific_setting(self):
        return {}

    def reset(self):
        self._macro_planner.reset()

    def draw_macro_graph(self):
        for macro, path in self._macro_planner.macros.items():
            print(path[0], path[-1], len(path))

    def get_evaluated_macro_actions(self, state):
        mp = self._macro_planner
        env = self._env
        self.get_action(state)

        macro_actions = {}
        for macro, q in mp.Q[self.macro_state].items():
            type, value, end_state = macro
            path = env.get_macro_path(state, macro)
            v = {
                'goal_general': (type, value),
                'goal_type': type,
                'q': q,
                'dist': len(path),
                'path': path,
                'path_macros': mp.best_plans[macro],
                'destination_tile': end_state[0]
            }
            goal = v['goal_type']
            # goal_room = env.tiles2room[tile]
            # if type == 'doorstep':
            #     goal_room = env.bark_positions[tile][0]
            #     room_name = env.get_room_name(goal_room)
            #     goal = f"{v['goal_type']} to room {goal_room} ({room_name})"

            if type == 'explore':
                room = value
                room_name = env.get_room_name(room)
                if room not in env.visited_rooms:
                    goal = f'explore room {room} ({room_name}) fully'
                else:
                    room_tiles = env.rooms[room]['tiles']
                    observed_tiles = len(env.get_observed(v['path'])[0].intersection(room_tiles))
                    goal = f"explore room {room} ({room_name}) {round(observed_tiles/len(room_tiles)*100, 1)} %"

            elif 'victim' in type:
                tile = value
                room = env.tiles2room[tile]
                room_name = env.get_room_name(room)
                goal = f"triage {v['goal_type']} in room {room} ({room_name}) at {end_state}"

            v['goal'] = goal
            v['goal_details'] = f"{v['goal']}, at {end_state}, {len(v['path'])-1} steps to reach, q = {round(v['q'], 2)}"
            macro_actions[macro] = v

        return macro_actions, mp.best_action

    def get_action(self, state, replan=True, verbose=True):
        env = self._env
        mp = self._macro_planner
        if len(self._plan) == 0:
            replan = True

        if replan:
            macro_state = (state, tuple([]), env.countdown_real)
            self.macro_state = macro_state
            start = time.time()
            children = list(mp._actions(macro_state[0], macro_state[1]).keys())
            # if verbose: print(f'macro children at state = {state}  ->  {children}')

            self._macro = mp.get_action(macro_state, replan, verbose=verbose and False)
            # verbose = True
            macro_path = []
            if isinstance(mp, UCT):
                Qsa = mp.Qsa
                Qsa_all = mp.Qsa_all
                macro_path.append(macro_state)
                if verbose: print(f'\n MCTS value at step {env.step} after {round(time.time() - start, 3)} sec')

            ## draw the chosen macro path
            macro_path.extend(mp._path)
            best_children = {}
            # print(' ....... final', list(env.get_macro_path.keys()))
            # print('\n macro path', [p[0] for p in macro_path])
            for i in range(1,len(macro_path)-1):
                old = macro_path[i-1]
                new = macro_path[i]

                if isinstance(mp, AStar):
                    best_children[new] = env.get_macro_path(old, new)

                elif isinstance(mp, UCT):
                    best_children[new[0]] = env.get_macro_path(old[0], new[0])

                    history = list(old[1])
                    history.append(old[0])
                    history = tuple(history)
                    for a, q in Qsa[history].items():
                        mark = '   '
                        path = ''
                        if a == new[0]:
                            mark = ' * '
                            path = f'    |    {best_children[new[0]]}'

                        if (history, a) in env.saved_macro_U[env.step]:
                            comment = env.saved_macro_U[env.step][(history, a)]
                        else:
                            comment = f' --------> no Macro_U saved for history = {history}   -->  a = {a}'
                        if verbose: print(f'{mark}{comment}  |  q = {q}   {path}')

            if hasattr(self, 'viz') and not self.viz.SHOW_FINAL_ONLY:

                if self.DRAW_MACRO_BEST_PATH:
                    self.viz.draw_macro_children(state, children=best_children, CLEAR=not self.DRAW_MACRO_SIBLINGS,
                                                 path_color=utils.colors.green, macro_color=utils.colors.dark_green)
                if self.DRAW_Q_VALUES:
                    tt = self.viz.macros_marker
                    tt.color(utils.colors.black)

                    q_values = list(self._macro_planner.Q[macro_state].values())
                    if isinstance(q_values[0], defaultdict):
                        q_values = [list(a.values())[0] for a in q_values]
                    q_sum = sum(q_values)
                    if q_sum == 0:
                        self._macro_planner._max_num_steps += 1
                        print(f'!!! changed horizon + 1 = {self._macro_planner._max_num_steps} after all q value = 0')
                    elif self._macro_planner._max_num_steps == self._max_num_steps_saved * 5:
                        self._macro_planner._max_num_steps = self._max_num_steps_saved
                        print(f'!!! changed horizon back to {self._max_num_steps_saved}')

                    if verbose: print(f'\n\nmacro children at state = {state}, countdown = {env.countdown_real}')
                    for a, q in self._macro_planner.Q[macro_state].items():
                        if isinstance(q, defaultdict):
                            q = list(q.values())[0]
                        tt.penup()
                        self.viz.go_to_pos(tt, a[2][0], x_shift=self.viz.ts / 3, y_shift=0)
                        if q_sum > 0:
                            tt.write(str(round(q/q_sum, 2)), font=("Courier", 12, 'normal'))

                        mp.best_plans[a] = {(a, cd): q for (a,cd), q in mp.best_plans[a].items()}
                        if verbose: print(f'   a = {a}  |  q = {round(q, 3)}  |  r(s) = {mp.best_plans[a]}')  ##   |  rate = {round(mp.best_rates[a], 3)}

            self._path = env.get_macro_path(state, self._macro)
            self._plan = env.path2plan(self._path, self._macro)
            if verbose: print(f'  chosen to do macro {self._macro} with path {self._path} and plan {self._plan}') ## ') ##

        action = self._plan.pop(0)
        return action

class HierarchicalPlanner(Planner):
    """Planning in hierarchical"""

    def __init__(self, roomlevel_planner, tilelevel_planner, env):

        self._roomlevel_planner = roomlevel_planner
        self._tilelevel_planner = tilelevel_planner
        self._max_num_steps = self._tilelevel_planner._max_num_steps

        self.name = 'hierarchical'
        if isinstance(self._roomlevel_planner, VI) and isinstance(self._tilelevel_planner, VI):
            self.name = 'hvi'
        elif isinstance(self._roomlevel_planner, VI) and isinstance(self._tilelevel_planner, AStar):
            self.name = 'hva*'
        elif isinstance(self._roomlevel_planner, AStar) and isinstance(self._tilelevel_planner, AStar):
            self.name = 'ha*'

        self.normalize = False  ## normalize for inverse planning
        self.last_room = None

        self.T = env.T  ## tilelevel transition

        ## helper data from mdp class
        self.tiles2room = env.tiles2room  ## map tile to room
        self.rooms = env.rooms  ## summary of room configuration
        self.rooms_to_plan = env.rooms_to_plan  ## room which player has identified rewarding

        ## helper functions from mdp class
        self.fully_explored = env.fully_explored  ## check if a room has been fully observed
        self.get_dist = env.get_dist  ## get the distance between two states
        self.add_temperary_reward = env.add_temperary_reward  ## add temp reward to rooms
        self.reset_temperary_reward = env.reset_temperary_reward
        self.get_observable_tiles = env.get_observable_tiles ## for taking into consideration when planning
        self.get_macro_actions = env.get_macro_actions
        self.prioritize_actions = env.prioritize_actions
        self.fully_explored = env.fully_explored
        self.set_bad_goals = env.set_bad_goals

        self.prioritized_actions = {}
        self.achieved_actions = []

        self.macro_actions = {}
        self.temperary_rewards = {}
        self.mission_stats = {}


    def get_specific_setting(self):
        ps = {}
        ps['room-level planner'] = self._roomlevel_planner.name
        ps['room-level gamma'] = self._roomlevel_planner._gamma
        ps['room-level max # steps'] = self._roomlevel_planner._max_num_steps

        ps['tile-level planner'] = self._tilelevel_planner.name
        ps['tile-level gamma'] = self._tilelevel_planner._gamma
        ps['tile-level max # steps'] = self._tilelevel_planner._max_num_steps

        return ps

    def reset(self):
        self._roomlevel_planner.reset()
        self._tilelevel_planner.reset()
        self.last_room = None
        self.last_goal_state = (0,0)

    def get_plan(self, state):
        self._tilelevel_planner.get_plan(state)
        self._path = self._tilelevel_planner._path
        self._plan = self._tilelevel_planner._plan
        self.V = self._tilelevel_planner.V

    def get_action(self, s, replan=True):

        self.previous_prioritized_actions = self.prioritized_actions

        if replan:
            self.run(s)

        a = self._tilelevel_planner.get_action(s, replan=False)

        ma = (s, a)
        if ma in self.previous_prioritized_actions:
            self.previous_prioritized_actions[ma]['step_achieved'] = self.step
            self.achieved_actions.append((ma, self.previous_prioritized_actions[ma]))
        return a

    def get_prioritized_macro_actions(self, s, TOP=None, replan=True, bad_goals=None):
        """ list of prioritized macro-actions """

        self.set_bad_goals(bad_goals)

        if replan:
            self.run(s, SKIP_TILE_LEVEL=True)

        keys = list(self.prioritized_actions.keys())
        if TOP != None and TOP <= len(keys):
            keys = keys[:TOP]
        top_macro_action = self.prioritized_actions[keys[0]]

        macro_actions = {}
        for key in keys:
            macro_actions[key] = self.prioritized_actions[key]
        # print('returned macro actions:', macro_actions)
        return macro_actions, top_macro_action

    def _get_roomlevel_states(self, s):

        ## roomlevel tiles include the tiles in the current room and door tiles in adjacent rooms
        current_room = self.tiles2room[s[0]]
        roomlevel_tiles = copy.deepcopy(self.rooms[current_room]['tiles'])
        rooms = [current_room]

        ## include the next room
        next_room = self._roomlevel_planner._get_greedy_action(current_room)
        if next_room != None:
            rooms.append(next_room)
            roomlevel_tiles.extend(self.rooms[next_room]['tiles'])

        ## plus all the tiles in the room that I can see right now
        observable_tiles = self.get_observable_tiles(s)
        roomlevel_tiles.extend(observable_tiles)
        # for tile in observable_tiles:
        #     another_room = self.tiles2room[tile]
        #     if another_room!=None and another_room not in rooms and tile not in self.rooms[another_room]['doors']:
        #         rooms.append(another_room)
        #         roomlevel_tiles.extend(self.rooms[another_room]['tiles'])

        ## visualize roomlevel value
        roomlevel_V, roomlevel_V_sum = self._normalize_V(self._roomlevel_planner.V, current_room)
        if hasattr(self, 'viz') and self.viz.SHOW_ROOMS:
            self.update_rooms(roomlevel_V, current_room)

        # ## in case you see a victim in the neighboring room, just go there
        # if neighbor in self.rooms_to_plan:
        #     self.log('      go consider room', neighbor)
        #     for tile in self.rooms[neighbor]['tiles']:
        #         if tile not in roomlevel_tiles:
        #             roomlevel_tiles.append(tile)

        self.temperary_rewards = self.reset_temperary_reward(self.temperary_rewards)
        # if self.fully_explored(current_room):
        #     ## choose the door location to be the tile closest to me
        #     adjacents = self.rooms[room]['neighbors'][neighbor]
        #     mydoor, yourdoor, heading = adjacents[0]
        #     for adjacent in adjacents:
        #         mydoor_t, yourdoor_t, heading_t = adjacent
        #         if self.get_dist(mydoor_t, s[0]) < self.get_dist(mydoor, s[0]):
        #             mydoor, yourdoor, heading = adjacent
        #
        #     roomlevel_tiles.append(yourdoor)
        #
        #     ## reassign roomlevel value to one tile
        #     if neighbor in self._roomlevel_planner.Q[room]:
        #         temp = self._roomlevel_planner.Q[room][neighbor] / roomlevel_V_sum
        #         self.rooms, self.temperary_rewards = self.add_temperary_reward(s, room, neighbor, mydoor, yourdoor, next_room, temp, rooms=self.rooms)


        # for room in rooms:
        #     next_room = self._roomlevel_planner._get_greedy_action(room)
        #
        #     self.rooms[room]['neighbors_temp_reward'] = {}
        #     for neighbor in self.rooms[room]['neighbors']:
        #
        #         ## choose the door location to be the tile closest to me
        #         adjacents = self.rooms[room]['neighbors'][neighbor]
        #         mydoor, yourdoor, heading = adjacents[0]
        #         for adjacent in adjacents:
        #             mydoor_t, yourdoor_t, heading_t = adjacent
        #             if self.get_dist(mydoor_t, s[0]) < self.get_dist(mydoor, s[0]):
        #                 mydoor, yourdoor, heading = adjacent
        #
        #         roomlevel_tiles.append(yourdoor)
        #
        #         ## reassign roomlevel value to one tile
        #         if neighbor in self._roomlevel_planner.Q[room]:
        #             temp = self._roomlevel_planner.Q[room][neighbor] / roomlevel_V_sum
        #             self.rooms, self.temperary_rewards = self.add_temperary_reward(s, room, neighbor, mydoor, yourdoor, next_room, temp, rooms=self.rooms)
        #

        ## record down which neighboring rooms contain victims
        for room in self.rooms.keys():
            if room == current_room:
                self.rooms[room]['rooms_to_plan'] = self.rooms_to_plan
            else:
                self.rooms[room]['rooms_to_plan'] = []

        roomlevel_states = []
        roomlevel_tiles = list(set(roomlevel_tiles))
        for tile in roomlevel_tiles:
            for head in [0,90,180,270]:
                roomlevel_states.append((tile,head))

        return roomlevel_tiles, roomlevel_states

    def get_Qs(self, state):
        return self._tilelevel_planner.get_Qs(state)

    def get_VX(self, state):
        return self._tilelevel_planner.get_VX(state)

    def draw_macro_actions(self, tt=None, pensize=None):
        if not hasattr(self, 'viz') or not self.viz.USE_INTERFACE or not self.viz.SHOW_MACRO_ACTIONS: return

        ma_tiles = {v[1][0] for v in self.macro_actions.values()}
        self.viz.circle_tiles(ma_tiles, pensize=pensize)

        if tt == None:
            tt = self.viz.writer
            # tt.clear()  ## TODO: comment out when replayer

        comment_line = 360 - 14 * (len(self.prioritized_actions) + len(self.achieved_actions))
        comment = f"Macro-actions at step {self.step}\n"

        index = 1
        drawn = []
        for state_action, value in self.prioritized_actions.items():

            ## Falcon maze
            if self.viz.ts < 15:
                # print(f'drawing top macro action {state_action[0]}')
                self.viz.circle_tiles([state_action[0]], color='#e74c3c', pensize=pensize, CLEAR=False)
                return

            tile = state_action[0][0]
            if tile not in drawn:
                drawn.append(tile)
                reward, cost, dist, reward_discounted, boosted, score = value['V']  ## \n{round(-cost, 2)}
                if self.SHOW_MACRO_ACTIONS_INDEX:  ## on 13 by 13 map
                    self.viz.go_to_pos(tt, tile, x_shift=self.viz.ts *3/ 10, y_shift=self.viz.ts / 4)
                    tt.write(f'#{index}', font=("Courier", 14, 'normal'))  ##
                else:
                    if self.viz.ts < 15:
                        fontsize = max(self.viz.ts // 9, 6)
                        text = f'{index}-{round(dist)}\n{round(reward, 2)}\n{round(reward_discounted, 2)}'
                    elif self.viz.ts < 30:
                        fontsize = max(self.viz.ts // 9, 6)
                        text = f'{index}-{round(dist)}\n{round(reward, 2)}\n{round(reward_discounted, 2)}'
                    else:
                        fontsize = max(self.viz.ts // 8, 8)
                        text = f'No.{index}\n{round(reward, 2)}\n{round(dist)}\n{round(reward_discounted, 2)}'

                    self.viz.go_to_pos(tt, tile, x_shift=self.viz.ts/3, y_shift=self.viz.ts/2)
                    tt.write(text, font=("Arial", fontsize, 'normal')) ## Courier

                comment += f"{round(value['V'][3], 2)}\t: {value['goal_type']} {value['dist']}\n"

            index += 1

        ## achieved actions
        comment += '\n'
        for state_action, value in self.achieved_actions:
            comment += f"\n@{value['step_achieved']}: {value['goal_type']}"

        tt.color('#ffffff')
        tt.goto(-720, comment_line)
        tt.write(comment, font=("Arial", 14, 'normal'))
        tt.color('#000000')
        # self.viz.screen.update()

    def update_tilelevel_values(self, roomlevel_tiles, current_room):
        tlp = self._tilelevel_planner
        for tile in roomlevel_tiles:
            for head in [0, 90, 180, 270]:
                state = (tile, head)
                self.rooms[current_room]['tilelevel_pi'][state] = tlp.pi[tlp._states.index(state)]
                self.rooms[current_room]['tilelevel_Q'][state] = tlp.Q[tlp._states.index(state)]
                self.rooms[current_room]['tilelevel_V'][state] = tlp.V[tlp._states.index(state)]

        for other_room in self.rooms:
            if other_room != current_room:
                self.rooms[other_room]['roomlevel_pi'] = None
                self.rooms[other_room]['roomlevel_Q'] = None
                self.rooms[other_room]['roomlevel_V'] = None
                self.rooms[other_room]['tilelevel_pi'] = None
                self.rooms[other_room]['tilelevel_Q'] = None
                self.rooms[other_room]['tilelevel_V'] = None

        if self.normalize:
            self._roomlevel_planner._normalize_Q()
            self._tilelevel_planner._normalize_Q()

    def run(self, s, SKIP_TILE_LEVEL=False):

        ## run room level planning, when changed rooms
        current_room = self.tiles2room[s[0]]
        if self.last_room != current_room or True:
            if isinstance(self._roomlevel_planner, AStar):
                self._roomlevel_planner.runs(current_room)
            else:
                self._roomlevel_planner.run(current_room)
            self.last_room = current_room
        next_room = self._roomlevel_planner._get_greedy_action(current_room)

        for room in self.rooms:
            self.rooms[room]['roomlevel_pi'] = self._roomlevel_planner.pi[room]
            self.rooms[room]['roomlevel_Q'] = self._roomlevel_planner.Q[room]
            self.rooms[room]['roomlevel_V'] = self._roomlevel_planner.V[room]
            self.rooms[room]['tilelevel_pi'] = {}
            self.rooms[room]['tilelevel_Q'] = {}
            self.rooms[room]['tilelevel_V'] = {}

        ## run tile level planning
        roomlevel_tiles, roomlevel_states = self._get_roomlevel_states(s)
        if hasattr(self, 'viz'):
            self.viz.draw_region(roomlevel_tiles)
            self.viz.draw_region(self.rooms[next_room]['tiles'], color='#1abc9c', CLEAR=False)
            self.viz.screen.update()

        self.macro_actions = self.get_macro_actions(s, self.macro_actions, roomlevel_states, roomlevel_tiles)
        self.prioritized_actions = self.prioritize_actions(self.macro_actions, s, next_room)

        if isinstance(self._tilelevel_planner, VI):
            if not SKIP_TILE_LEVEL:
                self._tilelevel_planner.set_states(roomlevel_states)
                self._tilelevel_planner.run(s)
                self.update_tilelevel_values(roomlevel_tiles, current_room)
                self.get_plan(s)

        elif isinstance(self._tilelevel_planner, AStar):
            self.draw_macro_actions()
            state, action = list(list(self.prioritized_actions.keys())[0])

            # ## use the previous plan if the goals are close and you are not so close
            # if self.get_dist(self.last_goal_state, state) <= 2 and self.get_dist(s, state) > 4:
            #     print('use last goal and path')
            #     self.last_goal_state = state
            #     return

            self._tilelevel_planner.set_goal(state, action, beta=1)
            self._tilelevel_planner.run(s)
            self._tilelevel_planner.get_QVP()
            self.last_goal_state = state

    def _normalize_V(self, V1, current_room):
        roomlevel_V = copy.deepcopy(V1)

        ## avoid visiting rooms that one has been to
        for room in self.rooms.keys():
            if self.fully_explored(room) and room != self.tiles2room[current_room]:
                roomlevel_V[room] = 0

        ## normalize
        roomlevel_V_sum = sum(roomlevel_V)  ## use numpy

        for room in self.rooms.keys():
            roomlevel_V[room] = roomlevel_V[room] / roomlevel_V_sum  # round(roomlevel_V[room]/roomlevel_V_sum, 1)

        return roomlevel_V, roomlevel_V_sum

    def update_rooms(self, roomlevel_V_arr, current_node):
        """ generate an image of the rooms as a directed graph """

        DEBUG = False
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
        rooms = self.rooms
        for index in rooms:
            for neighbor in rooms[index]['neighbors'].keys():
                if neighbor != index:
                    G.add_edge(index,neighbor)
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
        size = sizes[self.viz.ts]

        if self.viz.ts >= 30 or self.viz.USE_DARPA or True:
            if self.viz.USE_DARPA:
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
        if DEBUG: print('... before generating graph room',str(time.time() - start_graph), 'seconds')
        start_graph = time.time()
        plt.savefig("plots/rooms.png", bbox_inches='tight')
        plt.close()

        if DEBUG: print('... finished generating graph room',str(time.time() - start_graph), 'seconds')
        return "plots/rooms.png"

class RoomPlanner(HierarchicalPlanner):
    """ Planning the sequence of rooms then solve the room
            - room level can be branch and bound, while tile level A*
            - room level can be OP solver, while tile level UCT """

    def __init__(self, roomlevel_planner, tilelevel_planner, env):
        self._roomlevel_planner = roomlevel_planner
        self._tilelevel_planner = tilelevel_planner
        self._max_num_steps = self._tilelevel_planner._max_num_steps
        self._env = env

        self.tiles2room = env.tiles2room
        self.rooms = env.rooms
        self.next_room = ('explore', None, None)
        self._plan = []
        self._path = []

    def reset(self):
        self._roomlevel_planner.reset()
        self._tilelevel_planner.reset()
        self._tilelevel_planner._plan = []

    def _get_roomlevel_states(self, s):
        return self.rooms[self.tiles2room[s[0]]]['tiles'], None

    def get_rooms_to_explore(self):
        return [r for r in self._env.get_rooms() if r not in self._env.observed_rooms]

    def draw_room_search_tree(self, title, recordings_dir=None):
        if recordings_dir == None:
            recordings_dir = join(self.recordings_dir, 'trees')
        dG = nx.DiGraph()
        dG_best = nx.DiGraph()
        nodes_best = []
        node_labels = {}
        edge_labels = self._roomlevel_planner.edge_labels
        edge_labels_best = self._roomlevel_planner.best_edge_labels
        depth = len(self._roomlevel_planner._plan)
        nodes_in_depth = {d: len([k for k in edge_labels.keys() if len(k[1]) == d]) for d in range(depth)}
        leafs = max(list(nodes_in_depth.values()))
        ## len(edge_labels_best) *2/3  ## assuming branching factor is 3

        node_size, edge_width, arrow_size, font_size = 200, 3, 20, 10
        inchwidth = round(min(120, max(6, leafs * 1.2))) ## 20
        inchheight = round(max(5, depth * 2.4))
        # inchwidth, inchheight = { 13: [6, 5], 24: [120, 15], }[self.viz.world_width]

        for k, v in edge_labels.items():
            dG.add_edge(k[0], k[1])
        for k, v in edge_labels_best.items():
            if k[0] not in nodes_best: nodes_best.append(k[0])
            if k[1] not in nodes_best: nodes_best.append(k[1])
            dG_best.add_edge(k[0], k[1])
        for node in dG.nodes():
            node_labels[node] = str(node).replace(' ', '')

        pos = graphviz_layout(dG, prog='dot')
        plt.rcParams["figure.figsize"] = (inchwidth, inchheight)
        plt.box(False)
        plt.title(title)

        ## all nodes and edges in green, mark best nodes and edges in green
        nx.draw_networkx_nodes(dG, pos, node_color=utils.colors.yellow, node_shape='s', node_size=node_size, alpha=1)
        nx.draw_networkx_nodes(dG_best, pos, node_color=utils.colors.light_green, node_shape='s', nodelist=nodes_best, node_size=node_size, alpha=1)
        text = nx.draw_networkx_labels(dG, pos, node_labels, font_size=font_size)
        for t in text.values():
            if len(t._text) > 9:
                t.set_rotation('vertical')
        nx.draw_networkx_edges(dG, pos, edge_color=utils.colors.yellow, width=edge_width, arrowsize=arrow_size, alpha=1)
        nx.draw_networkx_edges(dG_best, pos, edge_color=utils.colors.light_green, width=edge_width, arrowsize=arrow_size, alpha=1)
        nx.draw_networkx_edge_labels(dG, pos, edge_labels=edge_labels, font_size=font_size)

        # nx.draw_networkx_nodes(dG, pos, node_color='#f09289', node_shape='s', nodelist=[max_node], node_size=node_size, alpha=1)
        # plt.set_size_inches(inchwidth, inchheight)
        # plt.show()

        output_name = f'{self.step}_{depth}'
        plt.savefig(join(recordings_dir, f'{output_name}_tree.png'), bbox_inches='tight')
        plt.close()

        return output_name

    def run_room_level(self, state):
        start = time.time()
        self._roomlevel_planner.writer.clear()
        next_rooms = self._roomlevel_planner._actions(state)

        ## run on room level, choose the room to go, include the path to go there in plan
        macro_state = (state, tuple([]), self._env.countdown_real)
        to_explore = self.get_rooms_to_explore()
        print('\n /-- planning on room level with the next room actions', list(next_rooms.keys()))
        print(' |-- explored rooms', self._env.observed_rooms, ' --- yet to explore', to_explore)

        ## get next room and shortest path to go there
        next_room = self._roomlevel_planner.get_action(macro_state, replan=True, verbose=False)
        self.next_room = next_room
        print(' \----> next room is', next_room)
        tilelevel_path = next_rooms[next_room]
        tilelevel_plan = self._env.path2plan(tilelevel_path)
        self._tilelevel_planner.set_plan(tilelevel_path, tilelevel_plan)

        ## draw next rooms
        if self.DRAW_MACRO_BEST_PATH:
            best_children = {}
            room_path = copy.deepcopy(self._roomlevel_planner._path)
            for i in range(1,len(room_path)):
                old = room_path[i-1]
                new = room_path[i]

                if isinstance(self._roomlevel_planner, AStar):
                    best_children[new] = self._env.get_macro_path(old, new)

            self.viz.draw_macro_children(state, children=best_children, CLEAR=not self.DRAW_MACRO_SIBLINGS,
                                         path_color=utils.colors.green, macro_color=utils.colors.dark_green)

            if self.DRAW_ROOM_SEARCH_TREE:

                self.viz.writer.up()
                self.viz.writer.color(utils.colors.red)
                for room in [t for t in self._env.get_rooms() if t not in self._env.observed_rooms]:
                    self.viz.go_to_pos(self.viz.writer, pos=room, ROOM=True)
                    self.viz.writer.write(room, font=("Courier", 22, 'bold'))

                time_passed = round(time.time() - start, 2)
                planner_name = f'{self._roomlevel_planner.name} - {time_passed} sec'
                rooms_planned = [m[1] for m in self._roomlevel_planner._plan]
                recordings_dir = join(self.recordings_dir, 'trees')
                if not isdir(recordings_dir): mkdir(recordings_dir)
                title = f'Step {self.step} | State = {state}, countdown = {round(self._env.countdown_real, 1)}\n' \
                        f'Rooms to do: {to_explore}\n Rooms planned: {rooms_planned}\n Planner: {planner_name}'
                output_name = self.draw_room_search_tree(title=title, recordings_dir=recordings_dir)

                self.viz.screen.update()
                self.viz.take_screenshot(PNG=True, CROP=True, FINAL=True, recordings_dir=recordings_dir, output_name=output_name)


    def run_tile_level(self, state):
        # self._tilelevel_planner.writer.clear()
        next_room = self.next_room[1]
        available_actions = self._env.get_actions_in_room(next_room, TRIAGE=True)
        self._tilelevel_planner.set_available_actions(available_actions)
        if isinstance(self._tilelevel_planner, VI):
            tilelevel_states = [(t, h) for t in self.rooms[next_room]['tiles'] for h in [0, 90, 180, 270]]
            # tilelevel_states.append([(t, h) for t in self.rooms[next_room]['doorsteps'] for h in [0, 90, 180, 270]])
            self._tilelevel_planner.set_states(tilelevel_states)
        elif isinstance(self._tilelevel_planner, AStar):
            self._tilelevel_planner.goal_state_action = self._env.get_tiles_to_observe(room=next_room)

        self._tilelevel_planner.run(state)
        # a = self._tilelevel_planner.get_action(state)

    def get_action(self, state, replan=True):

        this_room = self._env.tiles2room[state[0]]
        next_room = self.next_room[1]
        explored = next_room in self._env.observed_rooms

        ## set current room as goal if starts out in a room
        if next_room == None and self._env.room_is_room(this_room):
            self.next_room = ('explore', this_room, None)
            next_room = this_room

        if this_room == next_room:
            available_actions = self._env.get_actions_in_room(next_room, TRIAGE=True)
            self._tilelevel_planner.set_available_actions(available_actions)

        if this_room == next_room and replan and not explored:
            self.run_tile_level(state)

        elif explored or next_room == None:
            print('\n\nrun_room_level', state)
            self.run_room_level(state)

        a = self._tilelevel_planner.get_action(state, replan=False)
        return a


def get_planner(env, planner_name, verbose=False, seed=0, timeout=10, planner_param=None, goal_state_action=None):

    full_name = planner_name

    if ' ' in planner_name:
        index = planner_name.index(' ')
        planner_name = planner_name[:index]

    ## hierarhical: hvi, hva*, ha*
    if 'hv' in planner_name.lower() or 'ha' in planner_name.lower():

        roomlevel_planner = None
        tilelevel_planner = None

        ## roomlevel
        if 'hv' in planner_name.lower():
            roomlevel_planner = VI(env.roomlevel_states, env.roomlevel_states, env.roomlevel_T, env.roomlevel_R,
                               max_num_steps=len(env.roomlevel_states), gamma=env.player["roomlevel_gamma"],
                               available_actions=env.roomlevel_actions)
        elif 'ha' in planner_name.lower():
            roomlevel_planner = AStar(env.roomlevel_states, env.roomlevel_T, env.roomlevel_U,
                                      get_pos=None, get_dist=None, goal_state_action=None,
                                      gamma=env.player['gamma'],
                                      available_actions=env.roomlevel_actions,
                                      traversed_rooms=env.traversed_rooms, env=env)

        ## tilelevel
        if 'vi' in planner_name.lower():
            tilelevel_planner = VI(env.states, env.actions, env.T, env.R,
                                   max_num_steps=30, gamma=env.player["gamma"])
        elif 'a*' in planner_name.lower():
            tilelevel_planner = AStar(env.actions, env.T, env.R, env.get_pos, env.get_dist,
                                  gamma=env.player['gamma'], goal_state_action=None, env=env)

        planner = HierarchicalPlanner(roomlevel_planner, tilelevel_planner, env)

    ## orienteering algorithm on room level then A star in room
    elif 'r' in planner_name:
        EXHAUSTIVE = ('rdfs' == planner_name)
        BnB = ('rbnb' == planner_name)
        HS = ('rhs' == planner_name)
        if not HS:
            roomlevel_planner = AStar(env.get_room_actions, env.macro_T, env.macro_U, env.get_pos, env.get_dist,
                                      gamma=env.player['gamma'], timeout=20, max_num_steps=env.player['horizon'],
                                      EXHAUSTIVE=EXHAUSTIVE, BnB=BnB)
        else:
            roomlevel_planner = HS(env.get_room_actions, env.macro_T, env.macro_U, max_num_steps=env.player['horizon'])

        tilelevel_planner = AStar(env.actions, env.T, env.R, env.get_pos, env.get_dist, obs_rewards=env.obs_rewards_init,
                        gamma=env.player['gamma'], timeout=3, goal_state_action=env.get_tiles_to_observe(), env=env)
        planner = RoomPlanner(roomlevel_planner, tilelevel_planner, env)

    ## one level A* on the macros level
    elif 'mra*' in planner_name:
        planner = MacroPlanner(AStar(env.get_room_actions, env.macro_T, env.macro_U, env.get_pos, env.get_dist,
                                     timeout=30, max_num_steps=env.player['horizon'], gamma=env.player['gamma']),
                               env, countdown=env.player['countdown'])

    ## one level A* on the macros level
    elif 'ma*' in planner_name:
        planner = MacroPlanner(AStar(env.get_macro_children, env.macro_T, env.macro_U, env.get_pos, env.get_dist,
                                     timeout=env.player['timeout'], max_num_steps=env.player['horizon'],
                                     gamma=env.player['gamma']), env, countdown=env.player['countdown'])

    ## one level UCT on the macros level
    elif 'muct' in planner_name:
        planner = MacroPlanner(UCT(env.get_macro_children, env.macro_T, env.macro_U, timeout=10, beta=env.player['beta'],
                                   max_num_steps=10, gamma=env.player['gamma']), env)

    ## A Star, cost is 1-reward, heuristic is distance to a set goal
    elif 'a*' in planner_name:
        obs_rewards = None
        timeout = 2 ##1000
        if goal_state_action == 'ALL_TILES':
            goal_state_action = env.get_tiles_to_observe()
            obs_rewards = env.obs_rewards_init
        planner = AStar(env.actions, env.T, env.R, env.get_pos, env.get_dist, obs_rewards=obs_rewards,
                        gamma=env.player['gamma'], goal_state_action=goal_state_action, env=env)

    ## value iteration on the entire state space
    elif 'vi' in planner_name.lower():
        planner = VI(env.states, env.actions, env.T, env.R)

    elif 'pi' in planner_name.lower():
        planner = PI(env.states, env.actions, env.T, env.R)

    elif 'uct' in planner_name.lower():
        mode = 'ave'
        if '-max' in planner_name.lower(): mode = 'max'
        planner = UCT(env.actions, env.T, env.R, mode=mode)

    elif 'lrtdp' in planner_name.lower():
        planner = LRTDP(env.actions, env.T, env.R)

    elif 'rtdp' in planner_name.lower():
        planner = RTDP(env.actions, env.T, env.R)

    else:
        assert "I do not recognize this planner name"

    ## the output_name includes: planner_name, player_name, map_name, date
    planner_name = full_name
    map_name = env.MAP.replace('.csv','')
    output_name = f'{planner_name}-{env.player_name}-{map_name}-{utils.get_time(DATE=False)}'
    output_name = output_name.replace(' ','-').replace('(','').replace(')','')
    planner.set_params(name=planner_name, seed=seed, verbose=verbose, max_num_steps=env.max_iter,
                       gamma=env.player["gamma"], timeout=timeout, output_name=output_name)
    if planner_param != None:
        for key, value in planner_param.items():
            planner.set_params(key=key, value=value)
    return planner

def plan(env, planner, CHECK_TURNS=True):
    """ general planning framework """

    start = time.time()
    # cir = None
    s = env._pos_agent

    env.visited_tiles = [s[0]]
    ep = []  # collect episode

    ## initialize interface
    planner.draw_planning_panel(env.player, env.player_name)
    unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = env.observe(None, s[0])
    planner.viz.update_maze(s, tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change)
    # planner.viz.update_maze(s)
    planner.update_planning_panel(env, s)
    planner.viz.take_screenshot()
    if planner.viz.SHOW_FINAL_ONLY: planner.verbose=False

    planner.reset()
    timer = utils.Timer(verbose=False)
    for iter in tqdm(range(planner._max_num_steps), desc=planner.name):

        ## choose an action
        # planner._roomlevel_planner.step = env.step
        # planner._tilelevel_planner.step = env.step
        planner.inc_step()
        if isinstance(planner, AStar) and planner.MODE == 'OBS':
            planner.goal_state_action = env.get_tiles_to_observe()
        timer.add('before planning')
        a = planner.get_action(s, env.replan)
        timer.add('after planning')
        env.replan = False
        if CHECK_TURNS and env.check_turned_too_much(a, planner.log): break
        ep.append((s, a))
        planner.log(f'\nstep {env.step} | choose to {a} from {s} to {env.T(s, a)}  |   countdown {round(env.countdown_real, 3)}')

        # if cir != None: cir.clearstamps()
        # cir = planner.draw_rollouts(s, epsilon=0.1)

        ## arrive at new location
        s = env.T(s, a)
        if CHECK_TURNS and env.check_back_forth(s): break
        env._pos_agent = s

        ## collect reward at new location
        victim_to_change = env.collect_reward(a)
        planner.log(theme='victim_to_change', value=victim_to_change, env=env)

        ## observe the new location
        unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = env.observe(None, s[0])
        timer.add('observing tiles')
        if planner.VISUALIZE_BELIEF:
            planner.visualize_belief(unobserved_in_rooms, env.tilesummary, env.barked_rooms, env.room_is_room,
                                     env.fully_explored_rooms, env.rooms)
            planner.viz.screen.update()
        planner.viz.update_maze(s, tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change, victim_to_change=victim_to_change)
        timer.add('updating maze')

        # ## highlight the roomlevel region being considered  ## TODO: highlight next room
        # if isinstance(planner, HierarchicalPlanner):
        #     # ## print the areas within consideration of macro-action generation
        #     # roomlevel_tiles, roomlevel_states = planner._get_roomlevel_states(s)
        #     # planner.viz.draw_region(roomlevel_tiles)
        #
        #     ## print the next area to visit
        #     next_room = planner._roomlevel_planner._get_greedy_action(env.tiles2room[s[0]])
        #     # if next_room != None:
        #     #     planner.viz.draw_region(int(next_room))  ## roomlevel_tiles

        ## update tables of reward and plan
        planner.update_planning_panel(env, s)
        timer.add('updating planning panel')
        if env.replan:
            planner.viz.take_screenshot()

        ## exist if goals are all achieved
        if env.check_mission_finish(planner.log):  break
        # if time.time() - start > 2: break


    saved = len(env.victims_saved['VV']+env.victims_saved['V'])
    duration = round(time.time() - start, 4)
    planner.log(f'score: {env.score}   |   steps_taken: {env.step}/{env.max_iter}   |   time_taken: {duration}')
    planner.finish_recordings(env, ep, duration)
    return saved, env.step, duration

if __name__ == '__main__':
    import tests

    tests.test_planning()
    # tests.test_planning(map='6by6_3_Z.csv', player_name='tester', planner_name='uct')
    # tests.test_planning_methods(just_plot=False, seeds=3)
    # tests.test_planning_parameters(planner_name='uct-ave', just_plot=False)
