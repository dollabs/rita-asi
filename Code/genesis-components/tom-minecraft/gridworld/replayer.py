import abc
from os import listdir
import json
import numpy as np
import collections
import time
import math
from math import pi, sqrt, exp
import time
import json
import turtle
from tqdm import tqdm
from turtle import Turtle
import operator
from PIL import Image
import os
from os import listdir, mkdir
from os.path import isfile, isdir, join
import random
from pprint import pprint, pformat
import threading
import copy
import imageio
import matplotlib.pyplot as plt
from tkinter import PhotoImage
from turtle import Turtle, Screen, Shape
from matplotlib.ticker import FuncFormatter
import numpy as np
import math
from datetime import datetime
import argparse
import csv
import sys
import pandas as pd

pd.set_option('display.max_columns', 10)
pd.set_option('display.width', 2000)

sys.path.insert(1, '../rita')
import rmq  ## can comment this out if you haven't install pika and threading

import mdp
import interface
import utils
import ASIST_settings
import mapreader
import planners
from planners import Planner


class Replayer(Planner):
    recordings_dir = join('recordings', 'test_replayer')
    HALF_HALF = False  ## separate first five and next five minutes

    RITA_ANALYZE_MACROS = False
    RITA_OBSERVE = True
    RITA_INFERENCE = True
    RITA_PREDICT = True
    RITA_EVALUATE = True
    RITA_REALTIME = True

    ## only to observe
    DEVELOP_MODE = False
    if DEVELOP_MODE:
        RECORD_TIME = False
        LOG = False

        RITA_OBSERVE = True
        RITA_INFERENCE = False
        RITA_PREDICT = False
        RITA_EVALUATE = False

    if not RITA_INFERENCE: player_types = []

    def __init__(self, file='', dir='', recordings_dir=None, planner_name='ma*', verbose=False,
                 USE_INTERFACE=True, PNG=True, GIF=False, CROP=False, SUMMARIZE=False):

        if dir != '':
            dir = utils.get_reL_path(dir)
            self.files = [f for f in listdir(dir) if isfile(join(dir, f)) and ('.log' in f or '.json' in f)]
            self.dir = dir
        elif file != '':
            file = utils.get_reL_path(file)
            self.dir, name = utils.get_dir_file(file)
            self.files = [name]

        if recordings_dir != None:
            self.recordings_dir = recordings_dir
        self.planner_name = planner_name

        self.PNG = PNG
        self.GIF = GIF
        self.CROP = CROP
        self.SUMMARIZE = SUMMARIZE
        self.USE_INTERFACE = USE_INTERFACE
        self.verbose = verbose

        self.summary = {}

    def add_to_summary(self, key, tile, condition=True):
        if condition:
            world = self.map
            if world not in self.summary:
                self.summary[world] = {}
            if key not in self.summary[world]:
                self.summary[world][key] = {}
            if tile not in self.summary[world][key]:
                self.summary[world][key][tile] = 0
            self.summary[world][key][tile] += 1

    @abc.abstractmethod
    def __call__(self, state):
        """Make a plan given the state."""
        raise NotImplementedError("Override me!")

    @abc.abstractmethod
    def run_file(self, trajectory_file):
        """Replay the data from one file."""
        raise NotImplementedError("Override me!")

    @abc.abstractmethod
    def reset(self):
        """Initiate the values"""
        raise NotImplementedError("Override me!")

    def get_angle(self, yaw):
        if yaw <= 0: yaw += 360

        ## 270 right, left
        if (yaw > 0 and yaw <= 45):
            angle = (45 + yaw, 45 - yaw)

        ## 180 right
        elif yaw > 45 and yaw <= 135:
            angle = (45 - yaw, 45 + (90 - yaw))

        ## 90 right
        elif yaw > 135 and yaw <= 225:
            angle = (45 - (180 - yaw), 45 + (180 - yaw))

        ## 0 right
        elif yaw > 225 and yaw <= 315:
            angle = (45 - (270 - yaw), 45 + (270 - yaw))

        ## 270 right left
        elif yaw > 315 and yaw <= 360:
            angle = (45 - (360 - yaw), 45 + (360 - yaw))

        return angle

    def get_heading(self, yaw, discretize=True):

        """        180
                    |
        yaw = 90 --- --- 270
                    |
                    0
        """
        yaw = (-yaw + 270) % 360
        if not discretize: return yaw

        if yaw <= 45 or yaw > 270 + 45:
            head = 0
        elif yaw <= 45 + 90:
            head = 90
        elif yaw <= 45 + 180:
            head = 180
        elif yaw <= 45 + 270:
            head = 270
        return head

    def generate_heat_map(self):
        summary = self.summary
        for map in summary:
            count = summary[map]['count'][0]
            print(f'......... summarizing results for map {map} in {count} missions')

            ## statistics
            # for key in summary[map]:
            #     if key in ['count', 'victim_location']: continue
            #
            #     print(f'... generating heatmap for {key}')
            #     viz = interface.VIZ(mdp.POMDP(map), MAGNIFY=True, GROUND_TRUTH=True)
            #     interface.draw_heatmap(summary[map][key], viz.get_pos, ts=viz.ts)
            #     viz.screen.update()
            #     output_name = map.replace('.csv','')
            #     output_name = f'{output_name}-{count}-{key}'
            #     viz.take_screenshot(PNG=True, FINAL=True, CROP=True, output_name=output_name,
            #                         recordings_dir=join(self.recordings_dir, 'heatmaps'))

            ## summary[map]['victim_location'][yellow][file] = []
            for type, by_files in summary[map]['victim_location'].items():
                print(type)
                for file, locations in by_files.items():
                    print(f'  {file}: {locations}')

    def run(self):
        if not self.HALF_HALF: print(f'going to read {len(self.files)} files')
        for file in self.files:
            self.file = file
            trajectory_file = join(self.dir, file)
            self.run_file(trajectory_file)
        if self.SUMMARIZE: self.generate_heat_map()


# class Replayer2D(Replayer):
#
#     def get_map(self, file):
#         if "by" in file:
#             return file[:file.index("by")]
#         elif "ASIST_data" in file:
#             self.mode = 'ASIST'
#             b = 'trial_id'
#             e = '_messages'
#             trial_id = file[file.index(b)+len(b):file.index(e)]
#             falcon_maps = [4,7,9,11,12,15]
#             if int(trial_id) in falcon_maps:
#                 return '48by89.csv'
#             else:
#                 return '46by45_2.csv'
#         else:
#             print(f'what map is this file {file}?')
#
#     def run_file(self, trajectory_file):
#         env = mdp.POMDP(self.get_map(trajectory_file))
#         viz = interface.VIZ(env, WIPE_SCREEN=False)
#         with open(trajectory_file) as f:
#             trajectory = json.load(f)
#         trajectory = trajectory['steps']
#         duration = len(trajectory)

class ReplayerDiscrete(Replayer):
    recordings_dir = join('recordings', 'test_replay_discrete')
    player_types = ASIST_settings.player_types
    mle_type = player_types[0]
    dist_type = {}
    # planner_name = 'ha*' ## 'hvi'

    window_length = 200
    SHOW_PLOT = True
    MAGNIFY = False
    xlabels = None

    def init_replay(self, duration=200):
        self.init_recordings(self.recordings_dir)
        self.timer = utils.Timer(log=self.log, verbose=self.verbose)

        self.map = ASIST_settings.get_map(self.output_name, default=self.map)
        self.env = mdp.POMDP(self.map)
        self.env.decide = True  ## TODO: check whether decide flag is useful
        self.env.decide_delay = 1

        self.timer.add('creating env')
        self.viz = interface.VIZ(self.env, USE_INTERFACE=self.USE_INTERFACE, MAGNIFY=self.MAGNIFY)
        self.timer.add('creating viz')
        self.envs = {}
        self.planners = {}

        if not isinstance(self, ReplayerRITA):
            self.window_length = duration
        self.xdata = np.linspace(0, duration - 1, duration)
        self.ydata = {}

        self.likelihoods = {}
        self.proposals_to_evaluate = {}  ## inverse on macro-action level
        self.proposed_macro_actions = []  ## inverse on macro-action level
        self.bad_goals = {}

        players = {}
        if self.RITA_REALTIME:  ## save timecalculating raycasting
            for player_name in self.player_types:
                env1 = mdp.POMDP(self.map, player_name=player_name, obs_rewards=self.env.obs_rewards)
                self.timer.add(f'adding env {player_name}')
                self.envs[player_name] = env1

                planner1 = planners.get_planner(env1, self.planner_name)
                planner1.set_VIZ(self.viz)
                planner1.reset()
                self.planners[player_name] = planner1

                self.likelihoods[player_name] = [1] * self.window_length
                self.ydata[player_name] = np.ones(duration) / len(self.player_types)

                # self.viz.IMGS[player_name] = join('texture', str(self.viz.ts), f'goals-{player_name}.gif')
                # self.viz.add_shape(self.viz.IMGS[player_name])

                players[player_name] = env1.player
        self.log(pformat(players, indent=1))
        self.timer.start_record(recordings_folder=self.recordings_dir, output_name=self.output_name)
        self.envs_all = [self.env]
        self.envs_all.extend([self.envs[player_name] for player_name in self.player_types])

    def get_trajectory_name(self, trajectory_file):
        true_type = 'unknown'
        trajectory_name = utils.get_dir_file(trajectory_file)[1].replace('.log', '').replace('.json', '')

        ## 24by24 data
        if 'player' in trajectory_file:
            name = f'{self.map}'

        ## HSR data
        elif 'HSRData_TrialMessages' in trajectory_file:
            ## HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-103_Team-na_Member-46_Vers-3.log
            bb, pp, CondBtwn, CondWin, Trial, Team, Player, Version = trajectory_name.split('_')
            trajectory_name = " ".join(
                [CondBtwn.replace('CondBtwn-', ''), CondWin.replace('CondWin-', ''), Trial, Player])
            name = trajectory_name

        ## other ASIST data
        else:
            true_type = trajectory_file.split('-')[1]
            trajectory_name = trajectory_name.replace('.json', '').replace(self.map.replace('.csv', ''), '')
            trajectory_name = ''.join("" if c.isdigit() else c for c in trajectory_name).replace('--', '')
            name = f'{true_type}-{self.map}'

        # self.output_name = f'{utils.get_class_name(self)}-{name}-{utils.get_time(DATE=False)}'
        self.output_name = f'{name}'.replace(' ', '_')
        return trajectory_name

    def get_new_obs_tiles(self, s, yaw):
        return self.env.obs_rewards[s]

    def run_file(self, trajectory_file):
        """ json files generated by planners """

        with open(trajectory_file) as f:
            trajectory = json.load(f)

        self.map = trajectory['map']

        trajectory = trajectory['steps']
        duration = len(trajectory)
        print(f'duration {duration} in file', trajectory_file)
        trajectory_name = self.get_trajectory_name(trajectory_file)

        self.init_replay(duration)
        self.timer.add('init_play')
        # self.timer.start_record()
        self.draw_replay_window(trajectory_name)  ## trajectory_file
        for player in self.player_types:
            self.envs[player].trial_name = trajectory_name
        self.env.trial_name = self.output_name = trajectory_name
        self.output_name = self.output_name.replace(' ', '_')

        # self.timer.add('draw_replay_window')

        index = 0
        start = time.time()
        a_p = None
        for key in tqdm(trajectory.keys(), desc=trajectory_name):
            index += 1
            t = int(key)
            if index == len(trajectory.keys()): break

            ## look up s, a, s', here a is a dictionary
            s, a, reward, s_p, real_pos, yaw = self.get_sa(trajectory, t)
            self.log(f'\n time {t} action {a} from state {s} to {s_p}')

            self.env._pos_agent = s
            victim_to_change = self.env.collect_reward(a)
            new_obs_tiles = self.get_new_obs_tiles(s, yaw)
            unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = self.env.observe(new_obs_tiles, s[0])
            self.viz.update_maze(s, a=a, tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change,
                                 victim_to_change=victim_to_change, real_pos=real_pos, real_yaw=yaw)

            if self.RITA_ANALYZE_MACROS and self.env.decide:
                ## draw next macro actions at every step
                children = self.viz.draw_macro_children(s, CLEAR=True)

            if self.RITA_INFERENCE:
                for player in self.player_types:
                    env1 = self.envs[player]
                    env1._pos_agent = s
                    env1.collect_reward(a=a)
                    env1.observe(new_obs_tiles, s[0], unobserved_in_rooms=unobserved_in_rooms, obs_rewards=obs_rewards)

                ## estimate Q, PI and posterior probability using planning algorithms
                self.timer.add('before inverse')
                most_likely = self.update_likelihoods(s, a, t)
                self.env.replan = False
                self.timer.add('after invserse')

                ## update the interface
                # self.update_inv_plots(most_likely, t)
                # self.draw_rollout_paths()
                # self.draw_max_tiles()
                # self.timer.add('update_inv_plots')

            ## debug action recognition problem
            self.draw_text(self.writer, a_p, font=self.font_large, left=90, top=230)
            a_p = a

        self.finish_recordings(duration=time.time() - start)

    def draw_max_tiles(self):
        if not self.viz.USE_INTERFACE or not self.viz.SHOW_MAX_TILES: return

        ## draw the goal tile
        for player_type in self.player_types:
            env1 = self.envs[player_type]
            if env1._max_tile != None:
                x, y = env1.tilesummary_truth[env1._max_tile]['pos']
                x_s, y_s = ASIST_settings.player_shifts[player_type]
                img_name = self.viz.IMGS[player_type]
                self.viz.draw_at_with(tt=self.writer, tile=(x + x_s, y + y_s), img=img_name)

    def draw_rollout_paths(self):

        if not self.viz.USE_INTERFACE: return

        ## red, blue, purple, grey, yellow, green
        ggplot_colors = ['#d83327', '#2b76b0', '#8577cb', '#646464', '#f9b44c', '#7db034']

        index = 0
        original_cw = self.viz.color_wheel
        for player in self.player_types:
            states = self.planners[player]._path
            path = []
            last_tile = -1
            for tile, head in states:
                if tile != last_tile:
                    path.append(tile)
                    last_tile = tile

            self.viz.color_wheel = interface.initializee_color_wheel(len(path), rollout=ggplot_colors[index])
            index += 1
            x_s, y_s = ASIST_settings.player_shifts[player]
            self.writer.penup()
            for i in range(len(path)):
                if i == 0: continue
                tile = path[i]
                x, y = self.env.tilesummary[tile]['pos']
                if i == 1:
                    self.viz.go_to_pos(self.writer, (x + x_s, y + y_s))
                    self.writer.pendown()
                else:
                    self.viz.use_color_wheel(self.writer, i, len(path))
                    self.writer.pendown()
                    self.viz.go_to_pos(self.writer, (x + x_s, y + y_s))

        self.viz.color_wheel = original_cw
        self.writer.pencolor('#000000')
        self.writer.penup()

    def finish_recordings(self, duration=0.0):
        ## txt
        self.log(f'\n... finished game in {round(duration, 3)} seconds')
        self.logger.close()

        ## png
        if hasattr(self.viz, 'macros_marker'):
            self.viz.macros_marker.clear()
        self.viz.take_screenshot(PNG=True, FINAL=True, CROP=True, output_name=self.output_name,
                                 recordings_dir=self.recordings_dir)

    def draw_replay_window(self, trajectory_file):

        if not self.viz.USE_INTERFACE: return

        ## initialize left margin
        self.text_left = -int(self.viz.WINDOW_WIDTH / 2) + 30

        ## titles will stay static throughout game, usually is bold
        titles = turtle.Turtle()
        titles.hideturtle()
        titles.up()
        self.titles = titles

        ## writer will update text throughout game, usually text is not bold
        writer = turtle.Turtle()
        writer.hideturtle()
        writer.speed(10)
        writer.up()
        self.writer = writer

        # ## marker throughout game, usually text is not bold
        # writer = turtle.Turtle()
        # writer.hideturtle()
        # writer.speed(10)
        # writer.up()
        # self.writer = writer

        ## for visualizing human data
        if self.MAGNIFY:
            titles.color('#ffffff')
            titles.goto(-740, 410)
            titles.write(self.get_trajectory_name(trajectory_file), font=("Courier", 14, 'normal'))
            titles.color('#000000')
        else:
            self.draw_text(titles, 'Trial Name', top=260)
            self.draw_text(titles, trajectory_file, font=self.font_medium, left_shift=140, top=260)
            self.draw_text(titles, 'Most Likely Type', top=230)

    def update_inv_plots(self, most_likely, t):

        if not self.viz.USE_INTERFACE: return

        # -----------------------------------------
        # turn on interactive mode
        if self.SHOW_PLOT:
            plt.ioff()
        else:
            plt.ion()
        plt.style.use('ggplot')

        fig, ax = plt.subplots()
        fig.set_size_inches(4.2, 4)
        if self.viz.USE_DARPA:
            fig.set_size_inches(7, 3.4)
            plt.subplots_adjust(left=0.11, right=0.95, top=0.9, bottom=0.12)
        box = ax.get_position()
        ax.set_ylim([0, 1])
        ax.set_position([box.x0, box.y0, box.width * 1.05, box.height * 0.9])

        lines = []
        if len(self.player_types) == 6:
            styles = ['-', '+', 's', '--', 'x', '>']
            sizes = [2, 5, 3, 2, 5, 3]
        else:
            styles = ['-', '+', '--', 'x']
            sizes = [2, 5, 2, 5]

        # -----------------------------------------
        player_index = 0
        for player_type in self.player_types:
            line, = ax.plot(self.xdata, self.ydata[player_type], styles[player_index], markersize=sizes[player_index])
            lines.append(line)
            lines[player_index].set_ydata(self.ydata[player_type][0:t + 1])
            lines[player_index].set_xdata(self.xdata[0:t + 1])
            player_index += 1

            ## ----------------------------------------
            #      update the PI table for debugging
            ## ----------------------------------------
            # env = self.envs[player_type]
            # if visualize.SHOW_PI_TABLE:
            #     writer_loc = [156, 133, 112]  # [175, 35, -105]
            #     action_map = pd.DataFrame("", index=range(visualize.WORLD_HEIGHT), columns=range(visualize.WORLD_WIDTH))
            #     for index in env.tilesummary.keys():
            #         tile = env.tilesummary[index]
            #         i, j = mapreader.coord(tile['pos'])
            #         string = ''
            #         for head in range(4):
            #             for item in PIs[player_type][(index, head)]:
            #                 string += str(acts[item[0]]) + ','
            #             string += '/'
            #         string += ')'
            #         string = string.replace(',/', '/')
            #         string = string.replace('/)', '')
            #         action_map.loc[(i, j)] = string
            #
            #     writers[player_type].clear()
            #     writers[player_type].goto(20, writer_loc[player_index])
            #     writers[player_type].write(action_map.to_string(), font=("Helvetica", 9, 'normal'))

        # ## adjust range of x data
        if self.xlabels != None:
            def format_fn(tick_val, tick_pos):
                if int(tick_val) in self.xdata:
                    return self.xlabels[int(tick_val)]
                else:
                    return ''

            ax.set_xticklabels(self.xlabels)
            ax.set_xlabel('Time countdown (seconds)')
            ax.xaxis.set_major_formatter(FuncFormatter(format_fn))

        ax.set_xlim(0, self.env.step)
        # if self.env.step > self.window_length:
        #     ax.set_xlim(0, self.env.step)
        # else:
        #     ax.set_xlim(0, min(40, self.window_length))

        # specify the lines and labels of the first legend
        if self.viz.USE_DARPA:
            ax.legend(lines, self.player_types, ncol=2, bbox_to_anchor=(0.55, 1.32),
                      loc='upper left', frameon=False)
        else:
            ax.legend(lines, self.player_types, ncol=2, bbox_to_anchor=(0, 1.28),
                      loc='upper left', frameon=False)

        plot_file = join('plots', 'plot.png')
        gif_file = plot_file.replace('png', 'gif')
        plt.savefig(plot_file, dpi=100)
        plt.clf()
        plt.close()

        if self.SHOW_PLOT:
            imageio.mimsave(gif_file, [imageio.imread(plot_file)])
            smaller_plot = PhotoImage(file=gif_file)  # .subsample(3, 3)
            self.viz.screen.addshape("plot", Shape("image", smaller_plot))

            self.writer.clear()
            self.writer.shape("plot")
            self.writer.goto(-230, -10)
            if self.viz.USE_DARPA:
                self.writer.goto(-490, -70)
            self.writer.showturtle()
            self.writer.stamp()

            self.writer.hideturtle()
            self.draw_text(self.writer, most_likely, font=self.font_large, left_shift=190, top=230)

        # player_index = 0
        # s = env._pos_agent
        # if visualize.SHOW_LIKELIHOODS:
        #     for player_type in visualize.PLAYERS:
        #
        #         ## print out the posterior
        #         last_likelihood = round(likelihoods[player_type][len(likelihoods[player_type]) - 1], 2)
        #         action = PIs[player_type][s[0], s[1] // 90]
        #         value = Qs[player_type][s[0], s[1] // 90]
        #         values = []
        #         for v in value:
        #             values.append(round(v))
        #         string = str(last_likelihood) + '  ' + str(action) + '  ' + str(values)
        #         # action = [env.actions[i] for i, j in enumerate(action) if j == max(action)]
        #         writers[player_type].clear()
        #         writers[player_type].goto(-250, writer_loc[player_index])
        #         writers[player_type].write(string, font=("Helvetica", 12, 'normal'))
        #
        #         player_index += 1

    def update_likelihoods_by_actions(self, s, actions):
        max_temp = 0.1

        DEBUG = False
        summary = pd.DataFrame(columns=['player',  ## "[Q(s,a')]", 'Q(s)',
                                        'Q(s,a)',
                                        ## 'soft Q(s,a)',
                                        'p(s,a)',
                                        ## 'Pi(s)',
                                        'a_max',
                                        # 'posterior'
                                        # 'goal_tile',
                                        ])
        index = 0
        likelihoods_temp = {}
        Vs_tile = {}
        Qs_tile = {}
        if DEBUG: self.timer.add('before getting likihood')
        for player_type in self.player_types:
            if DEBUG: self.timer.add(f'  getting likihood for {player_type}')

            env1 = self.envs[player_type]
            planner1 = self.planners[player_type]

            act = planner1.get_action(s, replan=self.env.replan)

            ## get the goal tile
            Vs_tile[player_type] = planner1.V
            max_tile = -1
            max_tile_discounted = -np.inf
            for ss in range(len(planner1._tilelevel_planner._states)):
                tile, head = planner1._tilelevel_planner._states[ss]
                # discounted = env1.tilesummary[tile]['reward'] * env1.player["tilelevel_gamma"] ** env1.get_dist(tile, s[0])
                discounted = Vs_tile[player_type][ss]
                if discounted > max_tile_discounted:
                    max_tile_discounted = discounted
                    max_tile = tile
            env1._max_tile = max_tile

            ## value function on state and low-level action pair
            Qs_tile[player_type] = utils.normalize(planner1.get_Qs(s))
            Q_p = 0  ## sum
            Q_sa = []  ## softened

            ## tilelevel inverse planning
            temperature = max(env1.player['temperature'], max_temp)
            for a_p in env1.actions:
                if not (a_p == 'go_straight' and env1.tilesummary[s[0]][s[1]] == 'wall'):
                    Q_p += np.exp(Qs_tile[player_type][env1.actions.index(a_p)] / temperature)
                    Q_sa.append(np.exp(Qs_tile[player_type][env1.actions.index(a_p)] / temperature))
                else:
                    # print('          encounter wall', a_p)
                    Q_sa.append(-1)

            ## since action is a mixture
            likelihood = 0
            for a, v in actions.items():
                if a in env1.actions: a = env1.actions.index(a)
                likelihood += v * np.exp(Qs_tile[player_type][a] / temperature) / Q_p  # softmax
            # Qs_soft = softmax(Qs[player_type][s[0],s[1]//90]/temperature)

            if math.isnan(likelihood):
                print('likelihood is NaN')
                return
            else:
                likelihoods_temp[player_type] = likelihood

            ## for debug log
            summary.loc[index] = [player_type,
                                  # round(np.exp(Qsa[player_type][s[0],s[1]//90,a]),2), # round(Q_p,2),
                                  np.round(Qs_tile[player_type], 2),
                                  # np.round(Qs_soft, 2),
                                  round(likelihood, 4),
                                  act,
                                  ## env.actions[np.argmax(PIs[player_type][s[0], s[1] // 90])],
                                  ## max_tile
                                  ]
            index += 1

        ## normalize likelihoods
        likelihoods = []
        norm = sum(likelihoods_temp.values())
        for player_type in likelihoods_temp:
            self.likelihoods[player_type].append(likelihoods_temp[player_type] / norm)
            self.likelihoods[player_type].pop(0)
            likelihoods.append(np.round(self.likelihoods[player_type][self.window_length - 5:], 3))

        summary['last_five_likelihood'] = likelihoods
        self.log(str(summary))
        # if visualize.REPLAY_IN_RITA or visualize.REPLAY_WITH_TOM:
        #     note.goto(-820, 200)
        #     note.write(summary.to_string(index=False), font=("Courier", 12, 'normal'))

    def add_row_softmax(self, row):
        if self.softmax_csv == None:
            self.softmax_csv = csv.writer(open('_softmax.csv', mode='a'), delimiter=',', quotechar='"',
                                          quoting=csv.QUOTE_MINIMAL)
        self.softmax_csv.writerow(row)

    def update_likelihoods_by_macro_actions(self, s, actions):
        max_temp = 0.1

        current_macro_action = s

        print('\n!!! current_macro_action: ', current_macro_action)

        ## TODO: when the macro-action isn't exactly the same
        ## if the player just hit a macro-action, evaluate all proposals
        if current_macro_action in self.proposed_macro_actions and False:
            current_name = f"step {self.env.step} {self.env.tilesummary[s[0]]['type']}"
            self.achieved_macro_actions.append(current_macro_action)
            likelihoods_temps = []  ## likelihoods_temp[player_name] = likelihood
            to_print = {p: [] for p in self.player_types}
            for s, all_macro_actions in self.proposals_to_evaluate:
                likelihoods_temp = {}
                for player_name, macro_actions in all_macro_actions.items():
                    temperature = max(self.envs[player_name].player['temperature'], max_temp)

                    ## the demonerator
                    Q_sum = 0
                    for macro_action in macro_actions:
                        m = macro_actions[macro_action]
                        Q_sum += np.exp(m['score'] / temperature)
                        self.add_row_softmax(
                            [len(self.achieved_macro_actions), current_macro_action, current_name,
                             player_name, macro_action, m['name'], m['score'], current_macro_action == macro_action])

                    Q = 0
                    if current_macro_action in macro_actions:
                        Q = np.exp(macro_actions[current_macro_action]['score'] / temperature)

                    # for i in range(len(macro_actions)):
                    #     Q_sum += np.exp(0.5**(i+1) / temperature)
                    #
                    # ## the numerator
                    # if current_macro_action in macro_actions:
                    #     Q = np.exp(0.5**(macro_actions.index(current_macro_action)) / temperature)
                    # else:
                    #     Q = np.exp(0.5**len(macro_actions) / temperature)

                    likelihood = Q / Q_sum
                    likelihoods_temp[player_name] = likelihood
                    to_print[player_name].append(round(likelihood, 3))
                likelihoods_temps.append(likelihoods_temp)

            ## print a summary
            summary = {}
            for player_name in self.player_types:
                summary[player_name] = sum(to_print[player_name])
            summary = {k: v for k, v in sorted(summary.items(), key=lambda item: item[1])}
            keys = list(summary.keys())
            keys.reverse()
            prioritized_summary = {k: summary[k] for k in keys}  ## if not (k[0][0]==s[0] and k[1]=='go_straight')
            pprint(prioritized_summary)

            ## update self.likelihoods
            norm = sum(prioritized_summary.values())
            for player_type in prioritized_summary:
                self.likelihoods[player_type].append(prioritized_summary[player_type] / norm)
                self.likelihoods[player_type].pop(0)

            self.proposals_to_evaluate = {}
            self.proposed_macro_actions = []
            self.timer.add('/ evaluting likelihoods given macro-action')
            # for env1 in self.envs_all:
            #     env1.proposed_macros = {}

        ## else, generate macro-actions from each player and add to proposals to evaluate
        else:
            all_macro_actions = {}
            for player_name in self.player_types:
                planner1 = self.planners[player_name]
                env1 = self.envs[player_name]

                if isinstance(planner1, planners.MacroPlanner):
                    macro_actions, top_action = planner1.get_evaluated_macro_actions(s)
                    tp = []
                    for k, v in macro_actions.items():
                        if hasattr(self, 'get_game_pos'):
                            macro_actions[k]['goal_location'] = str(self.get_game_pos(v['destination_tile']))
                        macro_actions[k]['countdown_created'] = env1.countdown_real
                        macro_actions[k]['state_created'] = str(s)
                        macro_actions[k]['path_macros'] = {str(k): v for k, v in
                                                           macro_actions[k]['path_macros'].items()}
                        macro_actions[k]['path'] = str(macro_actions[k]['path'])
                        print(f"{player_name} at {env1.countdown_real}: {macro_actions[k]['goal_details']}")
                        tp.append(macro_actions[k]['goal'])
                        if k not in self.proposed_macro_actions:
                            self.proposed_macro_actions.append(k)
                    all_macro_actions[player_name] = macro_actions
                    env1.proposed_macros[env1.countdown_real] = macro_actions
                    env1._max_tile = planner1._macro_planner.best_action[0]
                    # print(f'{player_name}: {tp}')
                    # print(f'   {player_name} has {len(macro_actions)} macro_actions: {macro_actions}')


                elif isinstance(planner1, planners.HierarchicalPlanner):
                    macro_actions, top_action = planner1.get_prioritized_macro_actions(s, TOP=3, replan=self.env.replan,
                                                                                       bad_goals=self.bad_goals)
                    # self.timer.add(f'/ getting prioritized macro-actions of {player_name}')
                    self.envs[player_name]._max_tile = top_action['goal_tile'] ## macro_actions[0][0][0]

                    for m in macro_actions:
                        if m not in self.proposed_macro_actions:
                            self.proposed_macro_actions.append(m)
                    all_macro_actions[player_name] = macro_actions
                    # print(f'   {player_name} has {len(macro_actions)} macro_actions: {macro_actions}')


            self.timer.add(f'getting all prioritized macro-actions')
            self.proposals_to_evaluate[self.env.step] = all_macro_actions
            print()

    def update_likelihoods(self, s, actions, t):
        """ calculate the most recent likelihoods and take the product along a window """

        if isinstance(actions, str): actions = {actions: 1}

        skipped = False  ## skip calculating likelihood because it stays the same
        if sum(actions.values()) == 0:
            skipped = True
        if not self.env.replan and t > 1:
            # and s in self.planners[self.player_types[0]]._tilelevel_planner._states:
            skipped = True

        ## calculating the likelihood for each player
        if not skipped:
            ## based on low-level action choices
            # self.get_likelihoods_by_actions(s, actions)

            ## based on the macro-actions achieved by player
            self.update_likelihoods_by_macro_actions(s, actions)

        self.timer.add('updating likelihoods by macro-actions')

        ## update the graph based on self.likelihoods
        def gauss(n=20):
            """ half of gaussian filter as the window """
            sigma = n
            r = range(-int(n), int(n) + 1)
            g = [1 / (sigma * sqrt(2 * pi)) * exp(-float(x) ** 2 / (2 * sigma ** 2)) for x in r][1:n + 1]
            return np.asarray([m / g[-1] for m in g])

        yvalue = {}
        yvalue_log = {}
        gaussian = gauss(self.window_length)

        ## ----------------------------------------
        #       update posterior distribution
        ## ----------------------------------------
        for player_type in self.player_types:
            filtered = np.multiply(gaussian, self.likelihoods[player_type])
            # filtered = self.likelihoods[player_type]
            # yvalue[player_type] = np.prod(filtered) # yvalue[player_type] = np.sum(filtered)
            yvalue_log[player_type] = np.log(filtered).sum()  ## use sum of log likelihood to avoid floating point loss

        # norm = sum(yvalue.values())
        norm_log = sum(yvalue_log.values())
        most_likely = ''
        max_posterior = 0
        for player_type in self.player_types:
            temp = yvalue_log[player_type] / norm_log
            # temp = yvalue[player_type] / norm
            self.ydata[player_type][t] = temp
            if temp >= max_posterior:
                most_likely = player_type
                max_posterior = temp

        return most_likely

    def get_sa(self, trajectory, t):

        reward = 0
        current = trajectory[str(t)]
        next = trajectory[str(t + 1)]
        s = (current["tile"], current["heading"])
        s_p = (next["tile"], next["heading"])
        a = current["action"]
        a_p = next["action"]
        return s, a, reward, s_p, None, None


class ReplayerContinuous(ReplayerDiscrete):
    recordings_dir = join('recordings', 'test_replay_continuous')

    def get_new_obs_tiles(self, s, yaw):
        new_obs_tiles = mapreader.print_shadow(self.env, s, angle=self.get_angle(yaw), radius=40)

        ## TODO: if it's facing a door, see the blocks inside the door too
        # head = self.get_heading(yaw)

        # ## bathroom
        # if s == (2888, 180): new_obs_tiles.extend([2884, 2885, 2886, 2974, 2975])
        # if s == (2087, 180): new_obs_tiles.extend([2083, 2084, 2085, 2173, 2174])
        # if s == (1731, 180): new_obs_tiles.extend([1727, 1728, 1729, 1817, 1818])
        #
        # ## Med:
        # if s[0] == 1000: new_obs_tiles.append(1178) ## computer farm
        # if s[0] == 2350: new_obs_tiles.append(2357) ## bathroom

        return new_obs_tiles

    def get_sa(self, trajectory, t):
        reward = 0
        current = trajectory[str(t)]
        next = trajectory[str(t + 1)]
        s = (self.get_tile(current), self.get_heading(current["yaw"]))
        s_p = (self.get_tile(next), self.get_heading(next["yaw"]))
        a = self.get_action(next['x'], next['z'], next['yaw'],
                            current['x'], current['z'], current['yaw'])
        real_pos = (current['x'] - 0.5, -current['z'] + 0.5)
        if 'new_vv' in next:
            reward = 2
        elif 'new_v' in next:
            reward = 1
        if reward != 0:
            a = 'triage'
        # print('........... action', a)
        if isinstance(a, dict):
            a = max(a, key=a.get)
        return s, a, reward, s_p, real_pos, current['yaw'] % 360

    def get_tile(self, value):

        ## reference point of the world
        if isinstance(self, ReplayerRITA):
            x_low, x_high, z_low, z_high, y_low, y_high = self.ranges
        else:
            x_low, z_low = 0, 0

        ## use 'x' and 'y' in recorded data
        x = math.floor(value['x'] - x_low)
        y = math.floor(value['z'] - z_low)
        x, y = self.correct_xy(x, y)
        if (int(y), int(x)) not in self.env.tile_indices:
            return None
        return self.env.tile_indices[(int(y), int(x))]

    def get_action(self, x, z, yaw, x_last, z_last, yaw_last):
        def r(s):
            return round(s, 2)

        ## walking in any direction is treated as walking forward
        actions = {
            'go_straight': math.sqrt((x - x_last) ** 2 + (z - z_last) ** 2),
            'turn_left': max(0, (yaw_last - yaw) / 90),
            'turn_right': max(0, (yaw - yaw_last) / 90),
        }
        # print((r(x_last), r(z_last)), r(yaw_last), '->', (r(x), r(z)), r(yaw))
        # print(actions)

        # ## if walking not in the direction of looking, add that to turn left/right
        # head = self.get_heading(yaw, discretize=False)
        # if z-z_last == 0 and x-x_last > 0:
        #     theta = 90
        # elif z-z_last == 0 and x-x_last < 0:
        #     theta = -90
        # elif x-x_last == 0:
        #     theta = head
        # else:
        #     theta = np.degrees(np.arctan((x-x_last)/(z-z_last)))
        #     if x-x_last > 0 and z-z_last < 0:
        #         theta = -theta
        #     elif x-x_last > 0 and z-z_last >= 0:
        #         theta = 360-theta
        #     elif x-x_last <= 0:
        #         theta = 180-theta
        # if theta-head > 45:
        #     actions['turn_left'] += 0.5*(theta-head)/90
        #     print('!!! adding to the left', 0.5*(theta-head)/90)
        # if theta-head < -45:
        #     actions['turn_right'] += 0.5*(head-theta)/90
        #     print('!!! adding to the right', 0.5*(head-theta)/90)

        ## normalize the probability
        norm = sum(actions.values())
        if norm != 0:
            actions = {k: v / norm for k, v in actions.items()}
        return actions

        # thre = 0.25
        # yaw_thre = 45
        # head = self.get_heading(yaw, discretize=False)
        # if yaw_last - yaw > yaw_thre:
        #     act = 'turn_left'
        # elif yaw - yaw_last > yaw_thre:
        #     act = 'turn_right'
        # elif (head == 0 and x - x_last > thre) or (
        #         head == 90 and z_last - z > thre) or (
        #         head == 180 and x_last - x > thre) or (
        #         head == 270 and z - z_last > thre):
        #     act = 'go_straight'
        # else:
        #     act = 'unknown'
        # print((x,z), yaw, '->', (x_last, z_last), yaw_last,  '|', act)
        # if act == 'unknown': act = 'go_straight'
        # return act

    def correct_xy(self, x, y):
        env = self.env
        ww = self.viz.world_width
        wh = self.viz.world_height
        depth = 0

        if (int(y), int(x)) not in env.tile_indices:
            if (int(y), int(x - 1)) in env.tile_indices and x - 1 > 0:
                x = x - 1
                depth = 1
            elif (int(y - 1), int(x)) in env.tile_indices and y - 1 > 0:
                y = y - 1
                depth = 2
            elif (int(y - 1), int(x - 1)) in env.tile_indices and y - 1 > 0 and x - 1 > 0:
                y = y - 1
                x = x - 1
                depth = 3
            elif (int(y - 1), int(x + 1)) in env.tile_indices and y - 1 > 0 and x + 1 < ww:
                y = y - 1
                x = x + 1
                depth = 4
            elif (int(y + 1), int(x - 1)) in env.tile_indices and y + 1 < wh and x - 1 > 0:
                y = y + 1
                x = x - 1
                depth = 5
            elif (int(y + 1), int(x)) in env.tile_indices and y + 1 < wh:
                y = y + 1
                depth = 6
            elif (int(y), int(x + 1)) in env.tile_indices and x + 1 < ww:
                x = x + 1
                depth = 7
            elif (int(y + 1), int(x + 1)) in env.tile_indices and y + 1 < wh and x + 1 < ww:
                y = y + 1
                x = x + 1
                depth = 8
            # else:
            #     print('not adjusted')

        # tile = env.tile_indices[(int(y), int(x))]
        # if depth!=0:
        # print(depth)
        # print_s(env, (tile,''))
        return x, y


class ReplayerRITA(ReplayerContinuous):
    recordings_dir = join('recordings', 'test_replay_RITA')
    window_length = 60
    victim_report = None
    start_time = None
    end_time = None
    countdowns = {}
    mission_id = None

    achieved_macro_actions = []  ## value['name']
    rows_in_softmax = []  ## [idx, name, player_name, player_alternative, player_Q, True/False]
    softmax_csv = None

    FOV = False
    FOVs = {}

    hypotheses = {}
    hypothesis_ID = 0

    uid = 0

    data_last = None
    x_last = 0
    z_last = 0
    yaw_last = 0
    s_last = (0, 0)
    countdown_last = 600
    action_event = None

    env = None
    screen = None
    dc = None
    agent = None
    note = None
    note_event = None
    note_time = None
    note_debug = None
    real_pos_last = None
    last_obs = None
    current_room = None
    next_room = None
    V1 = None
    V2 = None
    pi = None
    pi2 = None
    trace = []
    yellow_dead = False

    titles = None
    writers = None
    label = None
    xdata = None
    ydata = None
    yvalue = {}

    lines = None
    envs = None
    likelihoods = None
    Qs = None
    PIs = None
    Vs_room = None
    Vs_tile = None
    predictions = {"next_step": {}, "next_goal": {}, "next_room": {}}

    RECORD_TIME = False
    LOG = True

    def json_to_dict(self, row, LOG=True):
        data = row.replace('\n', '')
        if LOG:
            # data = '{' + data.split(', {')[-1]
            data = data[25:]
            if len(data.replace('{', '')) != len(data.replace('}', '')): return None
        data = json.loads(data)
        if 'testbed-message' in data:
            if self.mission_id == None: self.mission_id = data['mission-id']
            data['testbed-message']['data']['timestamp'] = data['timestamp']
            data = data['testbed-message']
        if "data" in data:
            if '@timestamp' in data:  ## in .json files
                data['format_time'] = utils.format_time(data['@timestamp'])
            else:  ## in .log files
                data['format_time'] = utils.format_time(data['header']['timestamp'])
            # data['data']['format_time'] = data['format_time']
            # data['format_time'] = data['header']['timestamp']
            return data
        return None

    def init_replay_map(self, world_name):

        # global MAP, ranges, room_snap, blocks_in_world, room_names
        # global MAP_ROOM, WORLD_WIDTH, WORLD_HEIGHT, TILE_SIZE, MAX_ITER

        world_name = world_name.lower()
        if world_name == 'singleplayer':
            world_name = 'sparky'
        elif 'falcon' in world_name or world_name == 'not set':
            world_name = 'falcon'

        self.map, self.ranges, self.room_snap = ASIST_settings.world_configs[world_name]
        self.add_to_summary('count', 0)

        # MAP_ROOM, WORLD_WIDTH, WORLD_HEIGHT, TILE_SIZE, MAX_ITER = ASIST_settings.MAP_CONFIG[MAP]

        ## mapping from location to block type
        with open(join('configs', world_name + '.json')) as json_file:
            blocks_in_world = json.load(json_file)
            if 'blocks' in blocks_in_world:
                blocks_in_world = blocks_in_world['blocks']
            self.blocks_in_world = blocks_in_world

        ## name of rooms to write in messages
        self.room_names = ASIST_settings.room_names

    def sort_data(self, trajectory_file, sorted_path=None):

        trajectory_name = self.get_trajectory_name(trajectory_file)
        if sorted_path == None:
            sorted_path = join('recordings', 'test_analyzer_RITA_saved')  ## self.recordings_dir

        SUMMARIZE_SUB_TYPES = False
        sorted_msg_file = join(sorted_path, 'log', trajectory_name + '.json')

        if isfile(sorted_msg_file):
            with open(sorted_msg_file) as f:
                sorted_msg = json.load(f)
                map_name = sorted_msg['map']
                rows = sorted_msg['rows']
                self.FOVs = sorted_msg['FOVs']
                self.FOVs = {int(k): v for k, v in self.FOVs.items()}
                xlabels = sorted_msg['xlabels']
        else:
            sorted_msg = {}

            sub_types = {}
            index = 0

            FOUND_STARTED = False  ## there might not be mission_start message
            f = open(trajectory_file, "r")
            rows = f.readlines()
            json_data = {}
            for row in rows:
                data = self.json_to_dict(row, LOG='.log' in trajectory_file)
                if data != None:
                    json_data[data['format_time']] = data
                    if 'mission_state' in data['data']:
                        FOUND_STARTED = True

            rows = []
            xlabels = 0
            STARTED = False
            STOPPED = False
            PAUSED = False
            start_time = None

            obs_numbers = []  ## all observation numbers appeared during mission
            block_types = []
            sorted_data = sorted(json_data)
            for timestamp in sorted_data:
                data = json_data[timestamp]
                data.pop('format_time')
                data['data']['format_time'] = data['header']['timestamp']

                sub_type = data['msg']['sub_type']

                if SUMMARIZE_SUB_TYPES:
                    if sub_type not in sub_types:
                        sub_types[sub_type] = [data]
                    # ## debugging sub_type
                    # if sub_type == 'Event:location':
                    #     sub_types[sub_type].append(data)
                    # else:
                    #     sub_types[sub_type].append(index)
                    sub_types[sub_type].append(index)
                    index += 1

                # if sub_type == "Mission:VictimList":
                #     print(data['data'])

                ## pauses
                if sub_type == "Event:Pause":
                    if data['data']['paused'] == True:
                        PAUSED = True
                    else:
                        PAUSED = False

                ## align observation messages
                if sub_type == "FoV":  # and not FOV:
                    # blocks = data['data']['blocks']
                    # blocks = {k:[{'location':v['location'], 'type':v['type']}] for k,vs in blocks.items() for v in vs}
                    # self.FOVs[int(data['data']['observation'])] = data['data']['blocks']
                    self.FOV = True
                    blocks = []
                    for block in data['data']['blocks']:
                        type = block['type']
                        location = block['location']
                        blocks.append({'location': location, 'type': type})
                        if type not in block_types:
                            block_types.append(type)
                    self.FOVs[int(data['data']['observation'])] = blocks

                ## when mission ends
                elif sub_type == "stop":
                    STOPPED = True

                ## when mission starts
                elif sub_type == "start":
                    sorted_msg['mission'] = data['data']

                ## when mission starts
                elif 'mission_state' in data['data'] or not FOUND_STARTED:
                    STARTED = True
                    start_time = timestamp
                    map_name = 'sparky'
                    if 'mission' in data['data']:
                        map_name = data['data']['mission']
                    elif 'NotHSR' in trajectory_file:
                        map_name = 'falcon'
                    sorted_msg['map'] = map_name
                    FOUND_STARTED = True

                ## all messages
                if STARTED and not STOPPED and not PAUSED or (sub_type not in ['state', 'FoV']):
                    if 'experiment_id' not in sorted_msg['mission']:
                        sorted_msg['mission']['experiment_id'] = data['msg']['experiment_id']
                        sorted_msg['mission']['trial_id'] = data['msg']['trial_id']
                    data['msg'] = {'sub_type': data['msg']['sub_type']}
                    data['header'].pop('version')

                    ## state messages
                    if 'observation_number' in data['data']:
                        obs_numbers.append(int(data['data']['observation_number']))
                        if 'human_id' not in sorted_msg['mission']:
                            sorted_msg['mission']['human_id'] = data['data']['id']
                        data['data'].pop('world_time')
                        data['data'].pop('id')
                        data['data'].pop('total_time')

                    ## get countdown
                    if start_time != None:
                        timediff = timestamp - start_time
                        data['data']['countdown'] = round(600 - timediff.total_seconds(), 1)
                    else:
                        data['data']['countdown'] = -1000

                    if 'mission_timer' in data['data']:
                        data['data']['countdown'] = self.get_countdown(data['data']['mission_timer'])

                    if data['data']['countdown'] <= 600 or (sub_type not in ['state', 'FoV']):  # 575:
                        rows.append(data)

                        ## count the time points
                        if sub_type == "state":
                            xlabels += 1

            ## analyzing
            if SUMMARIZE_SUB_TYPES:
                for type in ['state', 'FoV', 'Event:PlayerJumped', 'Event:PlayerSprinting', 'Event:PlayerSwinging']:
                    sub_types[type] = [sub_types[type][0], len(sub_types[type])]
                sub_types = {k: (len(v) - 1, v) for k, v in sub_types.items()}
                with open(join(self.recordings_dir, 'sub_types_locations.json'), 'w') as outfile:
                    json.dump(sub_types, outfile, indent=4, sort_keys=True)

            self.FOVs = {k: v for k, v in self.FOVs.items() if k in obs_numbers}

            ## saving
            sorted_msg['rows'] = rows
            sorted_msg['FOVs'] = self.FOVs
            sorted_msg['xlabels'] = xlabels
            with open(sorted_msg_file, 'w') as outfile:
                json.dump(sorted_msg, outfile, indent=4, sort_keys=True)

        ## stats
        if self.verbose: print(trajectory_name, '| # states =', xlabels, '| # FOV messages =', len(self.FOVs))
        if xlabels == 0: print('no valid data')
        self.xlabels = [''] * xlabels

        self.start_time_real = time.time()
        self.init_replay_map(map_name)
        self.init_replay_rita(duration=xlabels)
        self.draw_replay_window(utils.get_dir_file(trajectory_file)[1])  ## trajectory_name

        for env1 in self.envs_all:
            env1.trial_name = trajectory_name

            ## not using tilesummary truth but observations
            if self.FOV: self.set_FOV(env1)

        return rows

    def set_FOV(self, en):
        en.tilesummary_truth = self.env.tilesummary
        en.remaining_to_see = {'V': [0] * 26, 'VV': [0] * 10}
        en.remaining_to_save = {'V': [], 'VV': []}

    def init_replay_rita(self, duration=6000):
        self.init_replay(duration)
        # self.viz = interface.VIZ(self.env, MAGNIFY=True, USE_INTERFACE=not self.SUMMARIZE)
        self.viz.color_wheel = interface.initializee_color_wheel(6000, rainbow=True)

        ## mission related
        self.data_last = None
        self.countdown_last = 600
        self.action_event = None
        self.real_pos_last = None
        self.last_obs = None
        self.current_room = None
        self.next_room = None
        self.trace = []  ## to put the trace for the first 5 min
        self.blocks_observed = {}
        self.block_to_floor = ASIST_settings.block_to_floor

        ## interface related
        self.note = None
        self.note_event = None
        self.note_time = None
        self.note_debug = None

    def run_file(self, trajectory_file):

        ## includes initialization
        rows = self.sort_data(trajectory_file)
        for row in rows:  ## tqdm():
            self.replay_with_tom(row['data'], row['msg']['sub_type'])
        self.finish_recordings(time.time() - self.start_time_real)

    def get_new_obs_blocks(self, new_obs_tiles):
        new_obs_blocks = {}
        x_low, x_high, z_low, z_high, y_low, y_high = self.ranges
        for obs_tile in new_obs_tiles:
            x1 = self.env.tilesummary[obs_tile]['col'] + x_low
            z1 = self.env.tilesummary[obs_tile]['row'] + z_low
            for y1 in range(y_low, y_high + 1):
                key = str(x1) + ',' + str(y1) + ',' + str(z1)
                new_obs_blocks[key] = self.blocks_in_world[key]

            block_type = self.env.tilesummary_truth[obs_tile]
            if block_type in ['door', 'victim', 'victim-yellow']:
                self.env.decide = True
                self.env.decide_delay = 3
                for en in self.envs:
                    en.decide = True
                    en.decide_delay = 3
        return new_obs_blocks

    def get_xyz(self, data):
        x = data['x']
        z = data['z']
        y = data['y']
        yaw = data['yaw']
        head = self.get_heading(yaw)
        return x, y, z, yaw, head

    def get_player_speed(self, data):
        x, y, z, yaw, head = self.get_xyz(data)
        x_last, y_last, z_last, yaw_last, head_last = self.get_xyz(self.data_last)
        return round(utils.point_distance(x, z, x_last, z_last) / 0.2, 3)

    def get_game_pos(self, tile):
        if tile < 0: return None
        x_low, x_high, z_low, z_high, y_low, y_high = self.ranges
        x = self.env.tilesummary[tile]['row'] + x_low
        z = self.env.tilesummary[tile]['col'] + z_low
        return (x, z)

    def check_play_step(self, data):

        if 'mission_timer' in data and 'countdown' not in data:
            data['countdown'] = self.get_countdown(data['mission_timer'])

        def check_on_map(data):
            ## in case the player is in the wall
            tile = self.get_tile(data)
            if tile == None: return False
            ## in case the player is not in a room
            if self.env.tiles2room[tile] not in self.env.rooms_truth:
                return False
            return True

        ## the first data
        if self.data_last == None or not check_on_map(data) or not check_on_map(self.data_last):
            self.data_last = data
            return False, False

        x_low, x_high, z_low, z_high, y_low, y_high = self.ranges
        x, y, z, yaw, head = self.get_xyz(data)
        x_last, y_last, z_last, yaw_last, head_last = self.get_xyz(self.data_last)

        ## check that player has moved for inverse
        if x > x_low and x < x_high and z > z_low and z < z_high and y >= y_low:
            ROTATED = (abs(yaw - yaw_last) >= 20)
            MOVED = utils.tile_changed(x, z, yaw, x_last, z_last, yaw_last)
            return ROTATED, MOVED

        return False, False

    def check_heading_goal(self, data, goal_x, goal_z):
        """ return true if current position is closer to the goal than that from last timestamp """
        x, y, z, yaw, head = self.get_xyz(data)
        x_last, y_last, z_last, yaw_last, head_last = self.get_xyz(self.data_last)

        ## do not count if the player didn't move or didn't move by himself
        moved_distance = utils.point_distance(x, z, x_last, z_last)
        if moved_distance > 1 or moved_distance == 0: return None

        answer = utils.point_distance(x, z, goal_x, goal_z) <= utils.point_distance(x_last, z_last, goal_x, goal_z)
        # print(f'................{answer} -  moved from ({x_last}, {z_last}) to ({x}, {z}), goal is ({goal_x}, {goal_z})')
        return answer

    def get_sa(self, current, next):
        x_low, x_high, z_low, z_high, y_low, y_high = self.ranges
        s = (self.get_tile(current), self.get_heading(current["yaw"]))
        s_p = (self.get_tile(next), self.get_heading(next["yaw"]))
        a = self.get_action(next['x'], -next['z'], next['yaw'],
                            current['x'], -current['z'], current['yaw'])
        real_pos = (current['x'] - x_low - 0.5, z_low - current['z'] + 0.5)
        return s, a, 0, s_p, real_pos, current['yaw'] % 360

    def observe_FOV(self, blocks, DEBUG=False, SEE_ALL=False):
        notes = []
        x_low, x_high, z_low, z_high, y_low, y_high = self.ranges

        def update_tile(en, tile):
            tile_summary = en.tilesummary[tile]
            tile_summary['type'] = type
            tile_summary['reward'] = en.player['rewards'][type]
            en.tilesummary[tile] = tile_summary
            en.tilesummary_truth[tile] = tile_summary

            ## remember they are dead later
            if 'victim-yellow' in type and tile not in en.remaining_to_save['VV']:
                en.remaining_to_save['VV'].append(tile)
                if len(en.remaining_to_see['VV']) != 0:
                    en.remaining_to_see['VV'].pop(0)
            elif 'victim' in type and tile not in en.remaining_to_save['V']:
                en.remaining_to_save['V'].append(tile)
                if len(en.remaining_to_see['V']) != 0:
                    en.remaining_to_see['V'].pop(0)

            ## decision points
            if 'victim' in type or 'door' in type:
                en.replan = True
                en.decide = True
                en.decide_delay = 3

        # observed = False
        for block in blocks:
            x = int(block['location'][0] - x_low)
            y = int(block['location'][1] - y_low)
            z = int(block['location'][2] - z_low)
            type = block['type']

            if type in self.block_to_floor and (y == 0 or (y <= 2 and type == 'perturbation_opening')):

                if type == 'perturbation_opening' and (x, z) in self.blocks_observed and self.blocks_observed[
                    (x, z)] != 'perturbation_opening':
                    self.blocks_observed.pop((x, z))

                ## analyzer mode
                if SEE_ALL and ('victim' in type or type in ['bedrock', 'perturbation_opening']):
                    notes.append((block['location'], self.block_to_floor[type]))

                if (x, z) not in self.blocks_observed:  ##  or type == 'block_victim_expired'
                    self.blocks_observed[(x, z)] = type
                    tile = self.env.tile_indices[(int(z), int(x))]
                    type = self.block_to_floor[type]

                    ## update the tilesummary_truth of all environments
                    update_tile(self.env, tile)
                    for env1 in self.envs.values():
                        update_tile(env1, tile)

                    if 'victim' in type:
                        text = ""

                        yellow = len(self.env.victims_saved['VV'])
                        green = len(self.env.victims_saved['V'])
                        text += f'^^ have saved {yellow} yellow and {green} green, '

                        yellow = len(self.env.remaining_to_save['VV'])
                        green = len(self.env.remaining_to_save['V'])
                        text += f' skipped {yellow} yellow and {green} green, '
                        # text += f'^^ have skipped {yellow} yellow and {green} green\n'

                        yellow = len(self.env.remaining_to_see['VV'])
                        green = len(self.env.remaining_to_see['V'])
                        text += f'need to find {yellow} yellow and {green} green'
                        # text += f'^^ need to find {yellow} yellow and {green} green'

                        # if self.victim_report == None:
                        #     self.victim_report = turtle.Turtle()
                        #     self.victim_report.hideturtle()
                        #     self.victim_report.speed(10)
                        #     self.victim_report.up()
                        # self.victim_report.clear()
                        # self.draw_text(self.victim_report, text, font=self.font_normal, left=0, top=240)
                        if DEBUG: print(text)

                    if type in ['perturbation_opening', 'gravel']:
                        self.env.change_room_structure()
                        for env1 in self.envs.values():
                            env1.change_room_structure()
        return notes

    def get_countdown(self, mission_timer):
        if ':' not in mission_timer: return 0
        minute, second = mission_timer.replace(' ', '').split(':')
        seconds = 60 * int(minute) + int(second)
        if seconds not in self.countdowns:
            self.countdowns[seconds] = []
        this_time = seconds + (9 - len(self.countdowns[seconds])) / 10
        self.countdowns[seconds].append(this_time)
        return this_time

    def replay_with_tom(self, data, sub_type='state', verbose=False):

        msg = {}  ## message to be sent out

        ## for raycasting
        countdown_last = self.countdown_last
        env = self.env
        envs = self.envs
        predictions = self.predictions
        note = self.note

        x_low, x_high, z_low, z_high, y_low, y_high = self.ranges
        DEBUG = False

        ## "observations/events/player/door"
        if sub_type == 'Event:Door':  # 'open' in data.keys():
            self.print_time(data)
            location = (data['door_z'] - z_low, data['door_x'] - x_low)
            self.print_event('door opened!', location)

        ## "observations/events/player/location"
        elif sub_type == 'Event:location':  # 'entered_area_name' in data.keys():
            self.print_time(data)
            if 'exited_area_id' in data:
                self.print_event('changed area!', '(' + data['exited_area_id'] + ')', data['exited_area_name'],
                                 ' ->  (' + data['entered_area_id'] + ')', data['entered_area_name'])
            elif 'entered_area_id' in data:
                self.print_event('changed area! ->  (' + data['entered_area_id'] + ')', data['entered_area_name'])

        ## "observations/events/player/woof"
        elif sub_type == 'Event:Beep':  # 'source_entity' in data.keys():
            self.print_time(data)
            location = ASIST_settings.get_beep_location(data, self.ranges)

            ## record the room being barked at
            tile = self.env.tile_indices[location]
            barked_room = None
            bark = 0
            if tile in self.env.bark_positions:
                barked_room, _, bark = self.env.bark_positions[tile]
                if verbose: print(f' -- barked {bark} by msg at {tile}')
            else:
                for head in [0, 90, 180, 270]:
                    neighbor = self.env.tilesummary[tile][head]
                    if neighbor != 'wall' and neighbor in self.env.bark_positions:
                        barked_room, _, bark = self.env.bark_positions[neighbor]
                        print(f' -- barked {bark} by msg at {tile} (signal from {neighbor})')

            if barked_room != None:
                room_name = self.env.get_room_name(barked_room)
                print(f'\n\nbeeeeeeeeeeeeeep at room {room_name} @ {data}\n\n')

                for env1 in self.envs_all:
                    env1.barked_tiles.append(tile)
                    if tile not in env1.visited_tiles:
                        env1.visited_tiles.append(tile)
                    env1.barked_rooms_real[barked_room] = bark
                    # env1.achieved_macro(('doorstep', tile), data['countdown'], (tile, 0), MSG=True)

                self.print_event(f'device used for room {barked_room} ({ASIST_settings.room_names[barked_room]}) at',
                                 location, data['message'])
            else:
                print('!!!!!!!!!!!!!!!! suspicious device signal', tile, data)

        ## "observations/events/scoreboard"
        elif sub_type == 'Event:Scoreboard':  # 'scoreboard' in data.keys():
            self.print_time(data)
            self.print_event('score updated!', data['scoreboard'])

        ## "observations/events/lever"
        elif sub_type == 'Event:Lever':  # 'powered' in data.keys():
            self.print_time(data)
            self.print_event('lever powered!', data['lever_x'], data['lever_y'], data['lever_z'])

        elif sub_type == "FoV":
            self.observe_FOV(data['blocks'])

        ## "observations/state"
        elif sub_type == 'state' or sub_type == 'Event:Triage':  # 'Player' in data['name'] or data['name'] in ['K_Fuse', 'ASU_MC']:

            action_event = None
            ## "observations/events/player/triage"
            if sub_type == 'Event:Triage':  # 'triage_state' in data.keys():
                self.print_time(data)
                location = (int(data['victim_z'] - z_low), int(data['victim_x'] - x_low))
                if location not in self.env.tile_indices:
                    print(f'???????? victim {location} not found on map')
                    return
                tile_index = self.env.tile_indices[location]
                tile = self.env.tilesummary_truth[tile_index]
                self.add_to_summary('actions_triage', tile_index)
                self.print_event('triage', data['triage_state'], location, tile['type'])

                ## victim turn white
                if data['triage_state'] == 'SUCCESSFUL':
                    self.add_to_summary('victim_saved', tile_index)

                    for env1 in self.envs_all:
                        if tile['type'] == 'victim-yellow':
                            env1.score += 30
                            if tile_index in env1.remaining_to_save['VV']:
                                env1.remaining_to_save['VV'].remove(tile_index)
                                env1.victims_saved['VV'].append(tile_index)
                        elif tile['type'] == 'victim':
                            env1.score += 10
                            if tile_index in env1.remaining_to_save['V']:
                                env1.remaining_to_save['V'].remove(tile_index)
                                env1.victims_saved['V'].append(tile_index)
                        env1.achieved_macro((tile['type'], tile_index), data['countdown'], (tile_index, 0), MSG=True)
                        env1.replan = True
                        if mdp.PRINT_REPLAN: print(env1.player_name, '!!! replan after saving victim')

                    ## update the graphics through env
                    if self.viz.USE_INTERFACE:
                        if 'victim' in tile['type']:
                            img_name = join('texture', str(self.viz.ts), 'open-' + str(tile['type']) + '.gif')
                            self.viz.draw_at_with(self.viz.dc, tile=tile_index, img=img_name)
                            if self.RITA_REALTIME:
                                self.viz.screen.update()
                    return

                elif data['triage_state'] == 'IN_PROGRESS':
                    action_event = {'triage': 1}

                countdown = data['countdown']
                data = self.data_last
                data['countdown'] = countdown

            ROTATED, MOVED = self.check_play_step(data)
            if ROTATED or MOVED or action_event != None:

                self.timer.add('checking to play')
                time_start = time.time()
                time_received = utils.get_time(SECONDS=True)
                countdown = data['countdown']

                s, a, reward, s_p, real_pos, yaw = self.get_sa(self.data_last, data)
                self.trace.append(real_pos)
                self.env._pos_agent = s
                self.env.countdown_real = countdown
                if self.xlabels != None:
                    self.xlabels[env.step] = countdown
                x, z = self.get_game_pos(s[0])

                ## triage event
                if action_event != None:
                    a = action_event
                    self.env.decide = True
                    self.env.decide_delay = 3

                last_room = self.env.tiles2room[env._pos_agent[0]]
                current_room = self.env.tiles2room[s[0]]
                playback_speed = round((600 - countdown) / (time.time() - self.start_time_real), 2)
                player_speed = self.get_player_speed(data)

                ## add actions to summary
                # self.add_to_summary('tiles', tile)

                ## ----------------------
                ##  observe
                ## ----------------------
                if self.RITA_OBSERVE and not self.SUMMARIZE:
                    victim_to_change = self.env.collect_reward(SKIP_TRIAGE=True)
                    # victim_to_change.extend(self.env.remaining_to_save['V'])
                    # victim_to_change.extend(self.env.remaining_to_save['VV'])

                    new_obs_tiles = None
                    if self.RITA_REALTIME:
                        new_obs_tiles = self.get_new_obs_tiles(s, yaw)
                    self.timer.add('calculating raycasting')

                    # new_obs_blocks = self.get_new_obs_blocks(new_obs_tiles)
                    if self.FOV and int(data['observation_number']) in self.FOVs:
                        self.observe_FOV(self.FOVs[int(data['observation_number'])])
                    self.timer.add('adding FOV')

                    unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = self.env.observe(new_obs_tiles,
                                                                                                         s[0])
                    # self.env.check_room_observed(current_room, countdown=countdown, s=s)
                    self.timer.add('calculating observaction')

                    ## reset color to look more salient
                    countdown_rainbow = countdown
                    if countdown < 300: countdown_rainbow += 300
                    self.viz.update_maze(s, tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change,
                                         victim_to_change=victim_to_change, countdown=countdown_rainbow,
                                         real_pos=real_pos, real_yaw=yaw, SCREEN_UPDATE=self.RITA_REALTIME)
                    self.timer.add('updating maze')

                    if self.RITA_REALTIME:
                        for player in self.player_types:
                            env1 = self.envs[player]
                            env1._pos_agent = s
                            env1.countdown_real = countdown
                            env1.collect_reward(SKIP_TRIAGE=True)
                            env1.observe(new_obs_tiles, s[0], unobserved_in_rooms=unobserved_in_rooms,
                                         obs_rewards=obs_rewards)
                            # env1.check_room_observed(current_room, countdown=countdown, s=s)
                    self.timer.add('observing all envs')

                ## ----------------------
                ##  summarizee human decisions | compare human decision with agent decisions
                ## ----------------------
                if self.RITA_ANALYZE_MACROS and self.env.decide:
                    ## draw next macro actions at every step
                    children = self.viz.draw_macro_children(s, CLEAR=True)

                    # self.env.proposed_macros[countdown] = list(children.keys())

                ## ----------------------
                ##  inference
                ## ----------------------
                # if ROTATED: print('!!!!!!!!!! Skip inference and prediction')
                if self.RITA_INFERENCE and MOVED and self.env.decide:

                    ## ----------------------
                    ##  print out action recognition
                    ## ----------------------
                    if MOVED:
                        location = (round(data['x'], 1), round(data['z'], 1))
                        stats = 'Pos = ' + str(location) + '  Head = ' + str(s[1]) + '  Act = ' + str(a)
                        self.print_time(data, extra=stats)  ## takes 0.05 sec at the beginning and increases over time
                        self.timer.add('printing time')

                        log_console = f"countdown = {countdown}  |  playback speed = {playback_speed}"
                        if self.LOG:
                            location_last = (round(self.data_last['x'], 1), round(self.data_last['z'], 1))
                            yaw_last = self.data_last['yaw']
                            print(f'\n=============================  {log_console}  ===============================')
                            print(f'Human data: {location_last} {round(yaw_last)}={self.get_heading(yaw_last)} -> \n'
                                  f'            {location} {round(yaw)}={self.get_heading(yaw)} , a = {a}  ')
                            print(
                                '-----------------------------------------------------------------------------------------------')
                        else:
                            print(f"\n------ {log_console} ------")

                    t = self.env.step
                    self.mle_type = self.update_likelihoods(s, a, t)
                    self.env.replan = False
                    self.timer.add('updating likelihoods')

                    ## update the interface
                    # self.update_inv_plots(self.mle_type, t)
                    # self.draw_rollout_paths()
                    # self.draw_max_tiles()
                    self.timer.add('updating interface after inference')

                    # self.timer.add('update_inv_plots')
                    self.dist_type = {}
                    for type in self.player_types:
                        self.dist_type[type] = round(self.ydata[type][env.step], 3)

                ## ----------------------
                ##  predictions
                ## ----------------------
                if self.RITA_PREDICT and MOVED:

                    hypthesis_messages = []

                    env1 = envs[self.mle_type]  ## make no modifications
                    planner1 = self.planners[self.mle_type]
                    pi1 = planner1._roomlevel_planner.pi
                    pi2 = planner1._tilelevel_planner.pi

                    ############################# next room ###############################
                    room_names = self.room_names
                    next_room = np.argmax(pi1[current_room])
                    next_rooms = {}
                    for room in list(np.nonzero(pi1[current_room])[0]):
                        if isinstance(pi1[current_room], int):  ## in A*, policy is greedy
                            next_rooms[room_names[room]] = pi1[current_room]
                        else:
                            next_rooms[room_names[room]] = pi1[current_room][room]

                    ############################# next action (VI) ###############################
                    if isinstance(planner1._tilelevel_planner, planners.VI):

                        ss = planner1._tilelevel_planner._states.index(s)
                        acts = np.round(pi2[ss], 2)
                        next_actions = {}
                        for index in range(len(env.actions)):
                            prob = acts[index]
                            name = f'{env.actions[index]} ({prob})'
                            next_actions[name] = prob

                        ## TODO: print the rank of actions
                        first = None
                        second = None
                        first_value = -np.inf
                        second_value = -np.inf
                        for key, value in next_actions.items():
                            if value > first_value:
                                second = first
                                second_value = first_value
                                first = key
                                first_value = value
                            elif value > second_value:
                                second = key
                                second_value = value

                        next_step_top = first[:first.index('(') - 1]
                        next_acts = first + " | " + second + " | "
                        next_actions_temp = copy.deepcopy(next_actions)
                        next_actions_temp.pop(first)
                        next_actions_temp.pop(second)
                        next_acts += str(list(next_actions_temp.keys())[0])

                        ## make it again for msg
                        next_actions_temp = copy.deepcopy(next_actions)
                        next_actions = {}
                        for action, prob in next_actions_temp.items():
                            action = action.replace(str(prob), '').replace(' ()', '')
                            next_actions[action] = prob

                        explanation_next_action = self.mle_type
                        max_tile = env1._max_tile

                    # -----------------------------------------------------------------------------------------#

                    else:
                        ############################# next action (AStar) ###############################
                        if s in pi2:
                            first = next_step_top = next_actions = pi2[s]
                        else:
                            first = next_step_top = next_actions = random.choice(
                                ['go_straight', 'turn_left', 'turn_right'])

                        ## draw rollout of step level prediction
                        if self.viz.USE_INTERFACE:
                            planner1.set_VIZ(self.viz)
                            planner1._tilelevel_planner.draw_rollout_path(self.writer)
                            planner1.draw_macro_actions(self.writer, pensize=self.viz.ts // 4)

                        next_macro_action = planner1.get_prioritized_macro_actions(s, TOP=1, replan=self.env.replan,
                                                                                   bad_goals=self.bad_goals)[0]
                        next_macro_action = list(next_macro_action.values())[0]
                        max_tile = next_macro_action['destination_tile']
                        max_tile_type = next_macro_action['goal_type']
                        # explanation_next_action = (max_tile, max_tile_type)

                        ############################# next goal ###############################

                    max_tile_pos = self.get_game_pos(max_tile)
                    max_tile_type = env1.tilesummary[max_tile]['type']
                    max_tile_exp = ''
                    if 'victim' in max_tile_type:
                        max_tile_exp = f'the player wants to triage the {max_tile_type}'
                    elif max_tile_type == 'door':
                        max_tile_exp = self.env.get_door_explanation(current_room, max_tile, room_names)
                    elif max_tile_type == 'air':
                        blocks_count = len(self.env.obs_rewards[(max_tile, s[1])])
                        max_tile_exp = f'the player wants to observe {blocks_count} grids in {room_names[current_room]}'

                    # prediction_room = 'next_room = ' + str(next_room) + '   next_step = ' + str(first)
                    # prediction_goal = 'next_goal = ' + str(max_tile_pos) + '   next_goal_type = ' + str(max_tile_type)

                    ## print stats and predictions
                    if self.viz.USE_INTERFACE:
                        if note == None:
                            note = Turtle()
                            note.hideturtle()
                            note.up()
                        else:
                            note.clear()

                        # note.goto(-20, -270)
                        # note.write(prediction_room, font=("Courier", 14, 'normal'))
                        #
                        # note.goto(-20, -290)
                        # note.write(prediction_goal, font=("Courier", 14, 'normal'))

                        self.note = note

                        # ## print the player profile
                        # note.goto(-750, 115)
                        # note.write(pformat(env1.player, indent=1), font=("Courier", 12, 'normal'))

                    # if isinstance(self, ReplayerRMQ):
                    ## using RITA format, see make_hypothesis(action, object, probability, reason)
                    next_step_hypothesis, next_step_ID = self.make_hypothesis("take-action", next_step_top, 1,
                                                                              self.mle_type)
                    next_goal_hypothesis, next_goal_ID = self.make_hypothesis("go-to-object",
                                                                              (max_tile_pos, max_tile_type), 1,
                                                                              self.mle_type)
                    next_room_hypothesis, next_room_ID = self.make_hypothesis("go-to-room", room_names[int(next_room)],
                                                                              1, self.mle_type, time_upper=150)
                    hypthesis_messages.append(next_goal_hypothesis)
                    hypthesis_messages.append(next_room_hypothesis)
                    # hypthesis_messages.append(next_step_hypothesis)

                    if self.RITA_EVALUATE:
                        """
                            predictions = {
                                "next_step": {  ## evaluated at every step
                                    "last": 2  ## turn right
                                    "results": [0, 1, 1, 1, 0, 1]  # 1 for correct and 0 for wrong for every prediction
                                }
                                "next_goal": {  ## evaluated at every step
                                    "last": 235
                                    "results": [0, 1, 1, 1, 0, 1]  # 1 for approaching and 0 for moving away from that goal tile
                                }
                                "next_room": {  ## evaluated when changing rooms
                                    "last": [6, 6, 6, 8, 8, 8, 8, 8]  ## all predictions made in the current room
                                    "results": [0, 1, 1, 1, 0, 1]  # 1 for correct and 0 for wrong for every prediction
                                }
                            }
                        """
                        for item in ['next_step', 'next_goal', 'next_room']:

                            if item not in predictions:
                                predictions[item] = {}

                            if 'results' not in predictions[item]:
                                predictions[item]['results'] = []

                            if 'last' not in predictions[item]:
                                UPDATE_PLOT = False
                                predictions['next_room']['last'] = []

                            else:
                                last_prediction = predictions[item]['last']
                                results = predictions[item]['results']

                                # check with corresponding actual value of predicted items
                                if item == 'next_step':
                                    last_prediction, last_prediction_ID = last_prediction
                                    # print(f'....... predicted next step = {last_prediction}, actual = {max(a, key=a.get)}')
                                    if last_prediction == max(a, key=a.get):
                                        results.append(1)
                                        # hypthesis_messages.append(self.evaluate_hypothesis(last_prediction_ID, True))
                                    else:
                                        results.append(0)
                                        # hypthesis_messages.append(self.evaluate_hypothesis(last_prediction_ID, False))

                                if item == 'next_goal':
                                    last_prediction, last_prediction_ID = last_prediction
                                    # print(f'....... predicted next goal = {last_prediction}')
                                    goal_tile = self.env.tilesummary[last_prediction]
                                    goal_x = goal_tile['col'] + x_low
                                    goal_z = goal_tile['row'] + z_low

                                    answer = self.check_heading_goal(data, goal_x, goal_z)
                                    if answer != None:
                                        if answer:
                                            results.append(1)
                                            hypthesis_messages.append(
                                                self.evaluate_hypothesis(last_prediction_ID, True))
                                        else:
                                            results.append(0)
                                            hypthesis_messages.append(
                                                self.evaluate_hypothesis(last_prediction_ID, False))
                                            self.env.replan = True
                                            if last_prediction not in self.bad_goals:
                                                self.bad_goals[last_prediction] = []
                                            self.bad_goals[last_prediction].append(countdown)

                                if item == 'next_room' and current_room != last_room:
                                    for predicted in last_prediction:
                                        predicted, predicted_ID = predicted
                                        if predicted == current_room:
                                            results.append(1)
                                            hypthesis_messages.append(self.evaluate_hypothesis(predicted_ID, True))
                                        else:
                                            results.append(0)
                                            hypthesis_messages.append(self.evaluate_hypothesis(predicted_ID, False))
                                    predictions['next_room']['last'] = []

                        predictions['next_step']['last'] = (next_step_top, next_step_ID)  ## first[:first.index(' ')]
                        predictions['next_goal']['last'] = (max_tile, next_goal_ID)
                        predictions['next_room']['last'].append((next_room, next_room_ID))

                        output_accuracy = self.make_accuracy_plot(predictions, turt=note)
                        msg['accuracy'] = output_accuracy
                        self.timer.add('evaluating predictions')

                    #############################################################################
                    #
                    #    report inference, predictions, explanations, and evaluations
                    #
                    #############################################################################
                    process_time = round(time.time() - time_start, 2)
                    msg['time'] = {
                        'countdown': countdown,
                        'last_countdown': countdown_last,
                        'time_received': time_received,
                        'time_sent': utils.get_time(SECONDS=True),
                        'process_time': process_time,
                        'playback_speed': playback_speed
                    }
                    msg['player_state'] = {
                        'location_tile': (round(x, 1), round(z, 1)),
                        'location_area': room_names[current_room],
                        'heading': ['east', 'north', 'west', 'south'][s[1] // 90],
                        'speed': f'{player_speed} blocks/sec',
                        # 'visible_blocks': f'{str(len(new_obs_blocks))} blocks',  ## prevent it from printing too much
                        'score': self.env.score
                    }
                    msg['player_type'] = {
                        'top': self.mle_type,
                        'confidence': self.dist_type
                    }
                    msg['next_step'] = {
                        'top': next_step_top,
                        'confidence': next_actions
                    }
                    msg['next_room'] = {
                        'top': room_names[int(next_room)],
                        'confidence': next_rooms
                    }
                    msg['next_goal'] = {
                        'top': max_tile_pos,
                        'block_type': max_tile_type,
                        'explanation': max_tile_exp
                    }
                    msg['hypothesis'] = hypthesis_messages
                    msg['timestamp'] = data['timestamp']
                    msg['subject'] = data['name']

                    if self.env.decide:
                        print(f'......... finishing processing in {round(time.time() - time_start, 2)} seconds')

                self.data_last = data
                self.s_last = s
                self.countdown_last = countdown
                self.timer.record(countdown)

            return msg

    def make_hypothesis(self, action, object, probability, reason, time_upper=60):
        ID = f"tom{self.hypothesis_ID}"
        hypothesis = {
            "hypothesis-rank": 0,  # #rank of the hypothesis that is generating the prediction
            "hypothesis-id": "hypTOM",  ## uid of the hypothesis
            "state": "unknown",  ## initially "unknown" then one of "true" or "false"
            "subject": 'player',
            "using": "",

            ## all predictions have a subject, the actor, and action, that is predicted, and the object upon which the action is performed
            "uid": ID,  ## uid of the prediction
            "action": action,  ## the action refers to the method being invoked (Pamela method).
            "object": object,
            "reason": reason,  ## why the rita-agent is predicting this action
            "agent-belief": probability,  ## agent's belief in the prediction
            "bounds": [0, time_upper],  ## time bounds for the prediction
        }
        if 'triage' in 'action':
            hypothesis['using'] = 'first-aid-kit'
        self.hypotheses[ID] = hypothesis
        self.hypothesis_ID += 1
        return hypothesis, ID

    def evaluate_hypothesis(self, ID, result):
        hypothesis = self.hypotheses[ID]
        if result:
            result = True
        else:
            result = False
        hypothesis['state'] = result
        return hypothesis

    def print_time(self, data, extra=None):

        # return
        if not self.viz.USE_INTERFACE or 'mission_timer' not in data: return

        ## victim turn red at countdown 300
        if not self.yellow_dead and round(data['countdown']) <= 304 and round(
                data['countdown']) >= 285:  ## and not self.FOV
            self.yellow_dead = True

            yellow_pos = copy.deepcopy(self.env.remaining_to_save['VV'])
            self.env.remaining_to_save['VV'] = []

            for index in yellow_pos:
                ## change the true and believed environment
                self.env.tilesummary[index]['reward'] = 0
                self.env.tilesummary[index]['type'] = 'victim-red'
                self.env.tilesummary_truth[index]['reward'] = 0
                self.env.tilesummary_truth[index]['type'] = 'victim-red'

            if not self.viz.USE_INTERFACE: return

            image = join('texture', str(self.viz.ts), 'obs-victim-red.gif')
            for index in yellow_pos:
                if index in self.env.observed_tiles:
                    self.viz.draw_at_with(self.viz.dc, tile=index, img=image)

            ## separate first five and next five minutes
            self.viz.screen.update()
            self.viz.take_screenshot(PNG=True, FINAL=True, CROP=True, output_name=self.output_name + '_1',
                                     recordings_dir=self.recordings_dir)
            tt = turtle.Turtle()
            tt.hideturtle()
            tt.speed(10)
            tt.pencolor(utils.colors.clouds)
            tt.pensize(4)
            tt.penup()
            step = 0
            for real_pos in self.trace:
                if step == 0:
                    self.viz.go_to_pos(tt, real_pos)
                else:
                    tt.pendown()
                    self.viz.go_to_pos(tt, real_pos)
                step += 1
            self.viz.screen.update()

        # elif self.yellow_dead and round(data['countdown']) <= 7:
        #     self.yellow_dead = False
        #     self.viz.screen.update()
        #     self.viz.take_screenshot(PNG=True, FINAL=True, CROP=True, output_name=self.output_name + '_2',
        #                              recordings_dir=self.recordings_dir)

        ## preparing to print
        if self.note_time == None:
            self.note_time = Turtle()
            self.note_time.hideturtle()
            self.note_time.up()
            if self.MAGNIFY:
                self.note_time.color('#ffffff')
                self.note_time.goto(0, 410)
            else:
                self.note_time.goto(270, 260)
        else:
            self.note_time.clear()

        # format_time = utils.format_time(data['format_time'])
        if self.RITA_REALTIME:
            self.note_time.write('Countdown: ' + str(data['countdown']), font=("Courier", 14, 'normal'))

        # if extra != None:
        #     note_time.goto(-450, -290)
        #     note_time.write(extra, font=("Courier", 14, 'normal'))
        # screen.update()

    def print_event(self, *args):

        return
        if not self.viz.USE_INTERFACE: return

        note_event = self.note_event

        statement = ' '.join([str(item) for item in args])
        if self.LOG: print('--------------------', statement)

        ## preparing to print
        if note_event == None:
            note_event = Turtle()
            note_event.hideturtle()
            note_event.up()
        else:
            note_event.clear()

        note_event.goto(-20, -250)
        note_event.write(statement, font=("Courier", 14, 'normal'))
        # if 'area' not in statement: time.sleep(1)

    def make_accuracy_plot(self, data, turt=note, verbose=False):
        """ make accuracy plot, take in running list of prediction result 1/0 """

        # print(data)
        category_names = ['Correct', 'Wrong']
        print_accuracy = "       Prediction accuracy: "
        output_accuracy = {}
        labels = list(data.keys())
        results = {}
        for item, value in data.items():
            value = value['results']
            length = len(value)
            if length == 0:
                count_ones = 1
            else:
                count_ones = sum(value) / length
            results[item] = [count_ones, 1 - count_ones]
            print_accuracy += f"{item} = {round(count_ones, 3)}  |  "
            output_accuracy[item] = round(count_ones, 3)
        if verbose: print(print_accuracy)

        if self.viz.USE_INTERFACE:
            data = np.array(list(results.values()))
            data_cum = data.cumsum(axis=1)
            category_colors = plt.get_cmap('RdYlGn')(np.linspace(0.85, 0.15, data.shape[1]))

            fig, ax = plt.subplots(figsize=(4, 1.4))
            ax.invert_yaxis()
            ax.xaxis.set_visible(False)
            ax.set_xlim(0, np.sum(data, axis=1).max())

            for i, (colname, color) in enumerate(zip(category_names, category_colors)):
                widths = data[:, i]
                starts = data_cum[:, i] - widths
                ax.barh(labels, widths, left=starts, height=0.5, label=colname, color=color)
                xcenters = starts + widths / 2

                r, g, b, _ = color
                text_color = 'white' if r * g * b < 0.5 else 'darkgrey'
                for y, (x, c) in enumerate(zip(xcenters, widths)):
                    if c != 0:
                        ax.text(x, y, str(round(c, 2)), ha='center', va='center', color=text_color)

            ax.legend(ncol=len(category_names), bbox_to_anchor=(0, 1), loc='lower left')
            # plt.show()

            plot_file = join('plots', 'accuracy_plot.png')
            plt.savefig(plot_file, dpi=100, bbox_inches='tight')
            plt.clf()
            plt.close()

            gif_file = plot_file.replace('png', 'gif')
            imageio.mimsave(gif_file, [imageio.imread(plot_file)])
            smaller_plot = PhotoImage(file=gif_file)  # .subsample(3, 3)
            self.viz.screen.addshape("accuracy_plot", Shape("image", smaller_plot))

            turt.shape("accuracy_plot")
            turt.hideturtle()
            turt.speed(10)
            turt.penup()
            turt.goto(-630, 140)
            turt.showturtle()
            turt.stamp()

        return output_accuracy


class ReplayerRMQ(ReplayerRITA):
    recordings_dir = join('recordings', 'test_replayer_RMQ')

    def __init__(self, host='localhost', port='5672', USE_INTERFACE=True, recordings_dir=None):

        super().__init__(USE_INTERFACE=USE_INTERFACE, recordings_dir=recordings_dir)
        self.rabbit = rmq.Rmq('rita', host=host, port=port)
        self.rabbit.subscribe(['startup-rita', 'shutdown-rita', 'testbed-message'])

    def run(self):
        self.init_replay_map('falcon')
        self.init_replay_rita()
        self.draw_replay_window('Listening to RMQ messages ...')
        self.msg_index = 0
        self.start_time_real = time.time()

        self.rabbit.wait_for_messages(self.dispatch_fn)  # Blocking function call
        self.rabbit.done = True
        self.rabbit.close()

    def get_thread_name(self):
        return threading.current_thread().name, threading.active_count()

    def send_message(self, msg, routing_key):
        if self.LOG:
            pprint(msg, width=450)
        self.rabbit.send_message_now(msg, routing_key)

    def send_obs(self, msg):
        hypotheses = msg['hypothesis']
        msg.pop('hypothesis')

        msg['datetime'] = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")
        msg['routing-key'] = 'raycasting'
        msg['app-id'] = 'InversePlanning'
        msg['mission-id'] = self.mission_id
        # print('send_obs', self.get_thread_name())
        self.send_message(msg, 'raycasting')

        if self.RITA_PREDICT:
            for prediction_msg in hypotheses:
                template = {}
                template['timestamp'] = msg['timestamp']
                template['datetime'] = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")
                template['routing-key'] = 'predictions'
                template['app-id'] = 'InversePlanning'
                template['mission-id'] = self.mission_id
                template['predictions'] = prediction_msg
                template['predictions']['subject'] = msg['subject']
                self.send_message(template, 'predictions')

    def get_thread_name(self):
        return threading.current_thread().name, threading.active_count()

    def start_rita(self, msg):
        pass

    def shutdown_rita(self, msg):
        self.rabbit.done = True
        self.rabbit.close()

    def dispatch_fn(self, msg, routing_key):

        before = time.time()

        if routing_key == 'startup-rita':
            pprint(msg)
            self.start_rita(msg)

        elif routing_key == 'shutdown-rita':
            self.shutdown_rita(msg)

        elif routing_key == 'testbed-message' and 'testbed-message' in msg and 'header' in msg['testbed-message']:

            self.msg_index += 1
            # print() # msg['testbed-message']['header']['message_type']

            if msg['testbed-message']['header']['message_type'] != 'trial':

                sub_type = msg['testbed-message']['msg']['sub_type']

                ## find the mission START message
                if sub_type == 'Event:MissionState':
                    # print('timestart?',)
                    # msg['testbed-message']['data'] == {'mission': 'Falcon', 'mission_state': 'Start'}
                    if msg['testbed-message']['data']['mission_state'].lower() == 'start':
                        self.start_time = utils.format_time(msg['testbed-message']['msg']['timestamp'])
                        self.start_time_real = time.time()
                        self.mission_id = msg['mission-id']
                        print('timestart', self.start_time)

                elif sub_type == 'stop':
                    self.end_time = utils.format_time(msg['testbed-message']['msg']['timestamp'])
                    self.end_time_real = time.time()
                    self.finish_recordings(self.end_time_real - self.start_time_real)

                elif 'data' in msg['testbed-message'] and self.start_time != None:  # and data_type=='state'
                    msg['testbed-message']['data']['timestamp'] = msg['timestamp']
                    data = msg['testbed-message']['data']
                    if 'playername' in data.keys(): data['name'] = data['playername']
                    # format_time = utils.format_time(msg['testbed-message']['header']['timestamp'])
                    # timediff = format_time - self.start_time
                    # data['countdown'] = round(600 - timediff.total_seconds(), 1)
                    # data['format_time'] = format_time
                    if 'mission_timer' in data:
                        data['countdown'] = self.get_countdown(data['mission_timer'])
                    else:
                        data['countdown'] = -1
                    # data['format_time'] = msg['testbed-message']['header']['timestamp']

                    msg = self.replay_with_tom(data, sub_type)
                    if msg != {} and msg != None:
                        pprint(msg)
                        self.send_obs(msg)
                        now = time.time()
                        print('msg_index', self.msg_index, 'perf time (secs)', round(now - before, 5), 'rate',
                              round(1 / (now - before), 2))
                        # print('only msg_index', msg_index)
                    # else:
                    #     print('rmq message is None', get_thread_name(), 'msg_index', msg_index)

        # msg['player_state']['visible_blocks'] = new_obs_blocks


class AnalyzerRITA(ReplayerRITA):
    recordings_dir = join('recordings', 'test_analyzer_RITA')

    def __init__(self, filter, **kwds):
        self.filter = filter
        super().__init__(**kwds)

        self.trials = {}

        self.PAUSES = False
        self.LOCATION = True
        self.MOTION = True
        self.TRIAGE = True

    def init_recordings(self, recordings_dir):
        if not isdir(recordings_dir):
            mkdir(recordings_dir)

        ## plot of result
        pdf_dir = join(recordings_dir, 'pdf')
        if not isdir(pdf_dir):
            mkdir(pdf_dir)

        ## individual summary
        json_dir = join(recordings_dir, 'json')
        if not isdir(json_dir):
            mkdir(json_dir)

        ## video pauses & notes
        video_dir = join(recordings_dir, 'video')
        if not isdir(video_dir):
            mkdir(video_dir)

    def get_filtered_files(self):
        filtered_files = []
        for file in self.files:
            if self.filter in file:
                if self.filter.startswith('TriageNoSignal') and 'NoTriageNoSignal' in file:
                    continue
                filtered_files.append(file)
        self.files = filtered_files
        if not self.HALF_HALF: print(f'going to read {len(self.files)} files')

    def run(self):
        self.get_filtered_files()

        for i in tqdm(range(len(self.files))):
            # if i < 139: continue
            file = self.files[i]
            start = time.time()
            self.file = file
            self.init_trial_data(file)
            self.countdowns = {}
            trajectory_file = join(self.dir, file)

            self.run_file(trajectory_file)
            print('... finish in', round(time.time() - start, 2), 'seconds')
            # return

    def run_file(self, trajectory_file):
        rows = self.sort_data(trajectory_file)

        for row in rows:
            self.analyze(row['data'], row['msg']['sub_type'])

        if self.PAUSES:
            self.report_pauses()
        else:
            self.report_analysis()

    def group_in_sights(self, in_sights, continuous=10):
        episodes = []
        last_sec = None
        last_episode = []
        for sec in in_sights:
            if last_sec == None:
                last_sec = sec
                continue
            if sec - last_sec <= continuous:
                last_episode.append(sec)
            else:
                ## ignore when the player just glanced at it for 0.1 seconds
                if len(last_episode) != 0:
                    episodes.append(last_episode)
                last_episode = []
            last_sec = sec
        if len(last_episode) != 0:
            episodes.append(last_episode)

        in_sight = []
        for episode in episodes:
            begin = min(episode)
            end = max(episode)
            in_sight.append({
                'b': begin,
                'e': end
            })

        return in_sight

    def analyze_victims(self, DEBUG=False):
        if not self.TRIAGE: return

        all_triaged = {**self.trial['Triage']['triaged_yellow'], **self.trial['Triage']['triaged_green']}
        remembered_green_triaged = []
        first_green = 601
        first_yellow = 601

        for pos, victim in self.victims.items():
            victim['x'], victim['y'], victim['z'] = pos
            color = victim['color']

            ## find the episodes where victim is iin sight
            in_sight = self.group_in_sights(victim['in_sight'])
            victim['in_sight'] = in_sight
            total = len(victim['in_sight'])
            for i in range(total):
                episode = victim['in_sight'][i]
                index = total - i
                begin = episode['b']
                end = episode['e']

                ## Victim: skipped
                if index > 1:
                    ## although not acting immediately, triaging it right afterwards
                    watching_while_triaging = False
                    for triage_end in all_triaged:
                        if begin < triage_end and end > triage_end:
                            watching_while_triaging = True
                    if not watching_while_triaging:
                        if 'yellow' == color and begin < 300:
                            self.trial['Triage']['skipped_yellow'].append(begin)
                        if 'green' == color:
                            self.trial['Triage']['skipped_green'].append(begin)
                            self.trial['Triage']['remembered_green'][begin] = pos

            ## Event: watch it die while triagiing
            if victim['triage_begin'] != -1 and victim['triage_end'] == -1:
                begin = victim['triage_begin']

                ## unable to save
                if begin > 300 and (
                        ((600 - begin < 15) and color == 'yellow') or (600 - begin < 7.5) and color == 'green'):
                    text = f'watch {color} die'
                    end = 600
                elif begin < 300 and (
                        ((300 - begin < 15) and color == 'yellow') or (300 - begin < 7.5) and color == 'green'):
                    text = f'watch {color} die'
                    end = 300
                else:
                    text = f"stop\n triaging"
                    end = begin + 1

                self.trial['Events'][begin] = (text, begin, end)
                if DEBUG: print(f'!!!!! {text} during time {begin} to {end}')

            ## the first time starting to triage a color
            if victim['triage_end'] != -1:
                if color == 'green' and victim['triage_begin'] < first_green:
                    first_green = victim['triage_begin']
                if color == 'yellow' and victim['triage_begin'] < first_yellow:
                    first_yellow = victim['triage_begin']

            ## which green victims are found back
            if len(in_sight) > 1 and color == 'green':
                first_sight = in_sight[0]['b']
                last_sight = in_sight[-1]['b']
                finish_triage = victim['triage_end']
                if first_sight < 300 and last_sight > 300 and finish_triage > 300:
                    remembered_green_triaged.append(finish_triage)

        ## which green victims are missed but triaged in the end
        remembered_green_skipped = []
        for begin, pos in self.trial['Triage']['remembered_green'].items():
            if 'successful' in self.trial['Triage']['ground_truth_victims'][pos]:
                remembered_green_skipped.append(begin)
        self.trial['Triage']['remembered_green'] = {}
        self.trial['Triage']['remembered_green']['skipped'] = remembered_green_skipped
        self.trial['Triage']['remembered_green']['triaged'] = remembered_green_triaged

        ## find out the triage strategy
        times_yellow = list(self.trial['Triage']['triaged_yellow'].keys())
        last_yellow = 0
        if len(times_yellow) > 0: last_yellow = times_yellow[-1]
        self.trial['Triage']['strategy_yellow'][first_yellow] = ['triaging yellow', first_yellow, last_yellow]

        times_green = list(self.trial['Triage']['triaged_green'].keys())
        last_green = 0
        if len(times_green) > 0: last_green = times_green[-1]
        self.trial['Triage']['strategy_green'][first_green] = ['triaging green', first_green, last_green]

        first_green_skipped = 600
        if len(self.trial['Triage']['skipped_green']) > 0:
            first_green_skipped = self.trial['Triage']['skipped_green'][0]

        if first_green > 300:
            self.trial['Triage']['strategy'] = 'yellow first'
        elif first_green - first_yellow > 15 + 7:
            self.trial['Triage'][
                'strategy'] = f'yellow first, until countdown {ASIST_settings.countup_to_clock(first_green)}'
        elif first_green_skipped > 300:
            self.trial['Triage']['strategy'] = f'triage everything in sight'
        else:
            self.trial['Triage']['strategy'] = f'triage both, skip some green'

        self.victims = {v['id']: v for k, v in self.victims.items()}
        if DEBUG: pprint(self.victims, width=1500)

    def analyze_motion(self):
        if not self.MOTION: return
        walks = self.trial['Motion']['walks']
        turns = self.trial['Motion']['turns']

        Ya = []
        Yb = []
        X = np.linspace(0, 600, 601)
        for x in X:
            ya = 0
            yb = 0
            for y in range(10):
                y = round(x + y / 10, 1)
                if y in walks:
                    ya += walks[y]
                if y in turns:
                    yb += turns[y]
            Ya.append(ya / 10)
            Yb.append(yb / 10)
        self.trial['Motion']['Ya'] = Ya
        self.trial['Motion']['Yb'] = Yb
        self.trial['Motion']['X'] = X

    def analyze_rooms(self, DEBUG=False):
        """
        rooms_revisit = {
            20: ['revisit: room 1', 20, 115],
            470: ['revisit: room 2', 470, 480],
        }
        """
        if not self.LOCATION: return

        ## get exit time from the next enter time
        keys = list(self.room_visits.keys())
        if len(keys) > 0:
            for i in range(1, len(self.room_visits)):
                self.room_visits[keys[i - 1]]['exit_time'] = keys[i]
                self.room_visits[keys[i - 1]]['exit_to'] = self.room_visits[keys[i]]['name']
            self.room_visits[keys[-1]]['exit_time'] = 600
            self.room_visits[keys[-1]]['exit_to'] = 'THE END'

        first_visits = {}
        triaged_times = {**self.trial['Triage']['triaged_yellow'], **self.trial['Triage']['triaged_green']}
        # triaged_times.extend(self.trial['Triage']['triaged_yellow'])
        # triaged_times.extend(self.trial['Triage']['triaged_green'])
        for countup, v in self.room_visits.items():
            name = v['name']
            enter_time = v['enter_time']
            exit_time = v['exit_time']

            if "Entrance" not in name and "Hallway" not in name:
                self.room_visits[countup]['is_room'] = True
                if name in self.trial['Triage']['ground_truth_rooms']:
                    self.room_visits[countup]['ground_truth'] = self.trial['Triage']['ground_truth_rooms'][name]
                else:
                    self.room_visits[countup]['ground_truth'] = []

                ## rooms revisited
                if name not in first_visits:
                    first_visits[name] = countup
                else:
                    episode = [f"revisit: {name}", enter_time, exit_time]
                    self.trial['Navigation']['rooms_revisited'][countup] = episode

                # rooms visited with or without triaging
                TRIAGED = []
                for triaged_time, victim in triaged_times.items():
                    if enter_time < triaged_time and exit_time > triaged_time:
                        TRIAGED.append(victim)

                if len(TRIAGED) == 0:
                    episode = [f"not triaged: {name}", enter_time, exit_time]
                    self.trial['Navigation']['rooms_no_triage'][countup] = episode
                else:
                    episode = [f"triaged: {name}", enter_time, exit_time]
                    self.trial['Navigation']['rooms_triaged'][countup] = episode
                    self.room_visits[countup]['triaged'] = TRIAGED

            self.trial['Navigation']['rooms_visited'][countup] = self.room_visits[countup]

        if DEBUG:
            pprint(self.room_visits)
        self.trial['Navigation']['room_visits'] = self.room_visits

    def analyze_perturbations(self, DEBUG=False):

        # for perturb_type in ['openings', 'blockages']:
        perturb_type = 'blockages'
        perturbs = self.group_in_sights(self.trial['Navigation'][perturb_type]['in_sight'], continuous=4)
        clusters = {}  ## positions: episode
        for episode in perturbs:
            positions = []
            begin, end = episode['b'], episode['e']
            for t in range(int(begin * 10), int(end * 10) + 1):
                t = round(t / 10, 1)
                if t in self.trial['Navigation'][perturb_type]['by_time']:
                    positions.extend(self.trial['Navigation'][perturb_type]['by_time'][t])
            positions = set(positions)

            k = tuple(positions)
            if k not in clusters:  ## len(positions) == 4 and
                clusters[k] = []
                # clusters[k].append(episode)  ## find the first time see the blockage
            clusters[k].append(episode)

            if DEBUG:
                print(episode, positions)
                print('---------------------------------')

        ## merge small clusters into large ones  ## TODO: not perfect
        to_pop = {}
        keys = list(clusters.keys())
        for cluster, episodes in clusters.items():
            for key in keys:
                if cluster != key:
                    if set(cluster).issubset(key):
                        to_pop[cluster] = key
        for cluster, key in to_pop.items():
            if cluster in clusters and key in clusters:
                clusters[key].extend(clusters[cluster])
                clusters.pop(cluster)
        if DEBUG: pprint(clusters)

        ## find the first times seeing a cluster
        for cluster, episodes in clusters.items():
            smallest_b = 601
            smallest_e = 601
            for episode in episodes:
                if episode['b'] < smallest_b:
                    smallest_b = episode['b']
                    smallest_e = episode['e']
            # print(cluster, episode)
            self.trial['Events'][smallest_b] = ['observe\n blockage', smallest_b, smallest_e]

        # if DEBUG:
        #     print(perturb_type, perturbs)
        # openings = self.group_in_sights(self.trial['Navigation']['openings']['in_sight'])
        # blockages = self.group_in_sights(self.trial['Navigation']['blockages']['in_sight'])

    def export_json(self, output_name):
        self.trial['Triage']['ground_truth_victims'] = {str(k): v for k, v in
                                                        self.trial['Triage']['ground_truth_victims'].items()}
        self.trial['Navigation']['ground_truth_perturbations'] = {str(k): v for k, v in self.trial['Navigation'][
            'ground_truth_perturbations'].items()}
        self.trial['Triage']['victims'] = self.victims
        self.trial['Motion']['Ya'] = list(self.trial['Motion']['Ya'])
        self.trial['Motion']['Yb'] = list(self.trial['Motion']['Yb'])
        self.trial['Motion']['X'] = list(self.trial['Motion']['X'])
        # self.trial['Motion'].pop('Ya')
        # self.trial['Motion'].pop('Yb')
        # self.trial['Motion'].pop('X')

        with open(output_name, 'w') as outfile:
            json.dump(self.trial, outfile, indent=4, sort_keys=True)

    def report_pauses(self):
        output_name = join(self.recordings_dir, 'video', f"{self.trial['Name']}.json")
        with open(output_name, 'w') as outfile:
            json.dump(self.pauses, outfile, indent=4, sort_keys=True)

    def report_analysis(self):

        self.analyze_victims()
        self.analyze_motion()
        self.analyze_rooms()
        self.analyze_perturbations()

        ## individual analysis output
        output_name = join(self.recordings_dir, 'json', f"{self.trial['Name']}.json")
        self.export_json(output_name)

        ## individual analysis plot
        output_name = join(self.recordings_dir, 'pdf', f"{self.trial['Name']}.pdf")
        ASIST_settings.make_plot_individual(self.trial, output_name)

        ## group analysis
        self.trials[self.id] = self.trial

    def analyze(self, data, sub_type):

        ## correct countdown
        if 'mission_timer' in data:
            data['countdown'] = self.get_countdown(data['mission_timer'])
        countup = round(600 - data['countdown'], 1)

        if data['countdown'] < 0: return  ## countup < 0 or

        if self.PAUSES and sub_type == 'Event:Pause':
            if data['paused'] == False:
                self.pauses["Pauses"][countup] = {
                    'timestamp': data['format_time'],
                    '2 localization': True,  ## knowing or not
                    '1 strategy': '',
                    '1 strategy notes': '',
                    '3 confidence': '',
                    '3 confidence notes': '',
                    '4 anxiety': '',
                    '4 anxiety notes': '',
                    '5 efforts': '',
                    '5 efforts notes': '',
                    '6 events that follow': [{
                        'begin': {'min': 0, 'sec': 0},
                        'end': {'min': 0, 'sec': 0},
                        'event': ''
                    }],
                }

        ## perturbatiion distribution
        elif self.TRIAGE and sub_type == "Mission:BlockageList":

            ## build ground truth victims
            for block in data['mission_blockage_list']:
                room = block['room_name']
                loc = (block['x'], block['y'], block['z'])
                block_type = 'air'
                if block['block_type'] == 'bedrock':
                    block_type = 'wall'
                self.trial['Navigation']['ground_truth_perturbations'][loc] = {
                    "block_type": block_type, "room": room, 'location': loc, 'in_sight': []
                }

        ## victim distribution
        elif self.TRIAGE and sub_type == "Mission:VictimList":

            ## build ground truth victims
            for victim in data['mission_victim_list']:
                room = victim['room_name']
                loc = (victim['x'], victim['y'], victim['z'])
                color = 'green'
                if '_2' in victim['block_type']:
                    color = 'yellow'
                self.trial['Triage']['ground_truth_victims'][loc] = {
                    "color": color, "room": room, 'location': loc
                }

            ## build ground truth room
            for k, v in self.trial['Triage']['ground_truth_victims'].items():
                if room not in self.trial['Triage']['ground_truth_rooms']:
                    self.trial['Triage']['ground_truth_rooms'][room] = []
                self.trial['Triage']['ground_truth_rooms'][room].append(v)

            # print(list(self.trial['Triage']['ground_truth_victims'].keys()))

        ## score
        elif self.TRIAGE and sub_type == 'Event:Scoreboard':
            self.trial['Triage']['final_score'] = int(list(data['scoreboard'].values())[0])


        ## device use
        elif self.TRIAGE and sub_type == 'Event:Beep':
            message = data['message']
            location = [data['beep_x'], data['beep_y'], data['beep_z']]
            if message == "Beep Beep":
                self.trial['Device']['device_yellow'].append(countup)
                self.trial['Device']['device_yellow_loc'].append(location)
            elif message == "Beep":
                self.trial['Device']['device_green'].append(countup)
                self.trial['Device']['device_green_loc'].append(location)

        ## change rooms
        elif self.LOCATION and sub_type == 'Event:location':

            if 'locations' in data:
                if len(self.room_visits) == 0:
                    last_location = None
                else:
                    last_key = list(self.room_visits.keys())[-1]
                    last_location = self.room_visits[last_key]['name']
                location = data['locations'][0]['name']
                if last_location != location:
                    id = data['locations'][0]['id']
                    if id != 'UNKNOWN':
                        visit = {
                            'name': location,
                            'id': id,
                            'enter_time': countup,
                            'exit_to': None,
                            'exit_time': None,
                            'is_room': False,
                            'triaged': []  ## (id: type)
                        }
                        self.room_visits[countup] = visit


        ## observations and actions
        elif sub_type == 'state':

            ## Motion statistics
            if self.MOTION:
                motion = math.sqrt(data['motion_x'] ** 2 + data['motion_z'] ** 2)
                self.trial['Motion']['walks'][countup] = round(motion, 2)

                if self.last_yaw != None:
                    diff = abs(self.last_yaw - data['yaw']) + abs(self.last_pitch - data['pitch'])
                else:
                    diff = 0
                self.trial['Motion']['turns'][countup] = round(diff, 2)
                self.last_yaw = data['yaw']
                self.last_pitch = data['pitch']

            ## record all time that it's in FOV
            if self.TRIAGE and int(data['observation_number']) in self.FOVs:
                notes = self.observe_FOV(self.FOVs[int(data['observation_number'])], DEBUG=False, SEE_ALL=True)
                if bool(notes):
                    color_code = {'victim': 'green', 'victim-yellow': 'yellow', 'victim-red': 'yellow', }
                    for pos, type in notes:
                        pos = tuple(pos)

                        ## observed victims
                        if 'victim' in type:
                            if pos in self.victims:
                                self.victims[pos]['in_sight'].append(countup)
                                ## see the yellow one before but now it's dead
                                if type == 'victim-red':
                                    self.victims[pos]['dead'] = True
                            else:
                                victim = {
                                    'id': len(self.victims) + 1,
                                    'color': color_code[type],
                                    'in_sight': [countup],
                                    'triage_begin': -1,
                                    'triage_end': -1,
                                    'dead': False,
                                }
                                self.victims[pos] = victim

                        ## observed perturbations
                        elif pos in self.trial['Navigation']['ground_truth_perturbations']:
                            self.trial['Navigation']['ground_truth_perturbations'][pos]['in_sight'].append(countup)

                            if type == 'air':
                                self.trial['Navigation']['openings']['in_sight'].append(countup)
                                if countup not in self.trial['Navigation']['openings']['by_time']:
                                    self.trial['Navigation']['openings']['by_time'][countup] = []
                                self.trial['Navigation']['openings']['by_time'][countup].append((pos[0], pos[2]))

                            elif type == 'wall':
                                self.trial['Navigation']['blockages']['in_sight'].append(countup)
                                if countup not in self.trial['Navigation']['blockages']['by_time']:
                                    self.trial['Navigation']['blockages']['by_time'][countup] = []
                                self.trial['Navigation']['blockages']['by_time'][countup].append((pos[0], pos[2]))


        elif self.TRIAGE and sub_type == 'Event:Triage':
            pos = (data['victim_x'], data['victim_y'], data['victim_z'])
            color = data['color'].lower()

            if data['triage_state'] == 'SUCCESSFUL' and pos in self.victims:
                self.trial['Triage']['ground_truth_victims'][pos]['successful'] = countup
                victim = self.trial['Triage']['ground_truth_victims'][pos]
                self.victims[pos]['triage_end'] = countup
                if 'yellow' in color:
                    self.trial['Triage']['triaged_yellow'][countup] = victim
                if 'green' in color:
                    self.trial['Triage']['triaged_green'][countup] = victim

            elif data['triage_state'] == 'IN_PROGRESS' and pos in self.victims:
                self.victims[pos]['triage_begin'] = countup

    def init_trial_data(self, trajectory_file):
        bb, pp, CondBtwn, CondWin, Trial, Team, Player, Version = trajectory_file.split('_')
        trial_name = self.get_trajectory_name(trajectory_file).replace(' ', '_')
        trial_id = int(Trial.replace('Trial-', ''))
        member_id = int(Player.replace('Member-', ''))
        trial = {
            'Name': trial_name,
            'CondBtwn': CondBtwn.replace('CondBtwn-', ''),
            'CondWin': CondWin.replace('CondWin-', ''),
            'Trial': trial_id,
            'Member': member_id,
            'Motion': {
                'turns': {},
                'walks': {},
            },
            'Triage': {
                'final_score': -1,
                'strategy': {},
                'strategy_yellow': {},
                'strategy_green': {},
                'triaged_yellow': {},
                'triaged_green': {},
                'skipped_yellow': [],
                'skipped_green': [],
                'remembered_green': {},
                'ground_truth_victims': {},
                'ground_truth_rooms': {},
            },
            'Device': {
                'device_knowledge': {},
                'device_yellow': [],
                'device_green': [],
                'device_yellow_loc': [],
                'device_green_loc': [],
            },
            'Navigation': {
                'rooms_revisited': {},
                'rooms_triaged': {},
                'rooms_no_triage': {},
                'rooms_visited': {},
                'ground_truth_perturbations': {},
                'blockage_clusters': {},
                'openings': {'in_sight': [], 'by_time': {}},
                'blockages': {'in_sight': [], 'by_time': {}},
            },
            'Events': {
                # 'watch_yellow_die': -1,
            }
        }
        self.pauses = {
            "Comment": {
                "Navigation": "",
                "Strategy": "",
                "Learning": "",
                "Personality": "",
            },
            "Pauses": {}
        }
        self.trial = trial  ## current trial summary
        self.id = trial_id  ## current trial id
        self.victims = {}  ## current victim summary
        self.room_visits = {}  ## current area summary

        self.last_yaw = None
        self.last_pitch = None


class AggregateRITA(AnalyzerRITA):
    recordings_dir = join('recordings', 'test_aggregater_RITA')
    verbose = False

    traces = []
    traces_replay = {}
    beeps_count = {}
    beeped_positions = {}

    HIGHEST_SCORE = False
    COUNT_VICTIMS = True
    # COUNT_DOORSTEPS = True
    COUNT_BEEPS = False

    def init_recordings(self, recordings_dir):
        if not isdir(recordings_dir):
            mkdir(recordings_dir)

        ## plot of result
        viz_dir = join(recordings_dir, 'viz')
        if not isdir(viz_dir):
            mkdir(viz_dir)

        ## save trace summary
        json_dir = join(recordings_dir, 'json')
        if not isdir(json_dir):
            mkdir(json_dir)

        ## save json summary of beep location counts and victims counts in maps
        json_dir = join(recordings_dir, 'counts')
        if not isdir(json_dir):
            mkdir(json_dir)

    def get_analysis(self, file):
        trial_name = self.get_trajectory_name(file).replace(' ', '_')
        analysis_file = join('recordings', 'test_analyzer_RITA_saved', 'json', trial_name + '.json')
        with open(analysis_file) as json_file:
            analysis = json.load(json_file)
            return analysis

    def run(self):
        self.get_filtered_files()

        if len(self.files) == 0:
            print()
            return

        ## different modes
        if self.HIGHEST_SCORE:
            best_file = ''
            best_score = -100
            comment = None
            for file in self.files:
                analysis = self.get_analysis(file)
                score = analysis['Triage']['final_score']
                if score > best_score:
                    best_score = score
                    best_file = file
                    comment = f"{len(analysis['Triage']['triaged_yellow'])} yellow, " \
                              f"{len(analysis['Triage']['triaged_green'])} green"

            self.files = [best_file]
            print('best score', best_score, comment, best_file)
            # return

        if self.COUNT_VICTIMS:
            victims = {}  ## location to count
            print(self.filter)
            for file in self.files:
                count_y = 0
                count_g = 0
                analysis = self.get_analysis(file)
                for type in ['triaged_yellow', 'triaged_green']:
                    for k, v in analysis['Triage'][type].items():
                        if 'yellow' in v['color'].lower():
                            count_y += 1
                        else:
                            count_g += 1
                        loc = tuple(v['location'])
                        if loc not in victims:
                            victims[loc] = 0
                        victims[loc] += 1
                # print(count_y, count_g)
            for loc in victims:
                if self.filter == 'TriageSignal_CondWin-FalconEasy-Static':
                    victims[loc] /= (len(self.files) - 1)
                else:
                    victims[loc] /= len(self.files)
            self.victims_count = victims
            print(f'in total {len(victims)} victims found')

            self.reset_output_name()
            self.draw_aggregation_map()
            return

        self.load_saved_traces()
        for i in tqdm(range(len(self.files)), desc=self.filter):

            ## load from saved json
            k = self.files[i]
            print(k)
            if k in self.traces_replay and len(self.traces_replay[k]) > 0 and not self.COUNT_BEEPS:
                trace = []
                for step in self.traces_replay[k].values():
                    trace.append(step)
                self.traces.append([tuple(real_pos) for real_pos in trace])
                # print('loaded', trace[:3])

            ## rerun from log/csv file
            else:
                self.run_file(join(self.dir, self.files[i]))
                # print('ran', trace[:3])

            # print(len(self.traces_replay[k]), list(self.traces_replay[k].items())[:3])

        self.reset_output_name()
        self.save_traces()
        self.draw_aggregation_map()

    def load_saved_traces(self):
        self.output_name = self.reset_output_name()
        self.traces = []

        json_file = join(self.recordings_dir, 'json', self.output_name + '.json')
        if isfile(json_file):
            with open(json_file) as f:
                self.traces_replay = json.load(f)

    def save_traces(self):
        with open(join(self.recordings_dir, 'json', self.output_name + '.json'), 'w') as outfile:
            json.dump(self.traces_replay, outfile, indent=4)

    def draw_aggregation_map(self):
        def xyz2type(k):
            x, y, z = k
            tile = self.env.tile_indices[(z - 144, x + 2108)]
            type = self.env.tilesummary_truth[tile]['type']
            return tile, type

        map = ASIST_settings.get_map(self.files[0])  ## self.output_name
        print(map)
        self.env = mdp.POMDP(map, ORACLE_MODE=True)
        self.viz = interface.VIZ(self.env, MAGNIFY=True)

        if self.COUNT_VICTIMS:
            for k, v in self.victims_count.items():
                tile, type = xyz2type(k)
                # x, y, z = k
                # tile = self.env.tile_indices[(z - 144, x + 2108)]
                # type = self.env.tilesummary_truth[tile]['type']
                size = int(40 * v)
                v = round(v, 2)
                print(tile, type, v, 'size', size)

                tt = turtle.Turtle()
                tt.hideturtle()
                tt.speed(10)
                tt.penup()
                tt.color(utils.colors.orange)
                self.viz.draw_at_with(tt, tile=tile, type=type, ts=size)
                self.viz.go_to_pos(tt, tile)
                tt.write(str(v), font=("Courier", 24, 'bold'))
            self.viz.screen.update()

            output_name = self.output_name + '_victims'
            self.viz.take_screenshot(PNG=True, FINAL=True, CROP=True, output_name=output_name,
                                     recordings_dir=self.recordings_dir)
            with open(join(self.recordings_dir, 'counts', output_name + '.json'), 'w') as f:
                output = {}
                for type in ['victim', 'victim-yellow']:
                    output[type] = {str(k): v for k, v in self.victims_count.items() if xyz2type(k)[1] == type}
                json.dump(output, f)
            return

        if self.COUNT_BEEPS:
            self.viz.circle_tiles(self.beeps_count)
            mm = self.viz.macros_marker
            mm.color(utils.colors.black)
            for tile in self.beeps_count:
                self.viz.go_to_pos(mm, tile, x_shift=self.viz.ts / 3, y_shift=self.viz.ts / 2)
                mm.write(self.beeps_count[tile], font=("Courier", 12, 'normal'))  ## Courier
            self.viz.screen.update()

            output_name = self.output_name + '_beeps'
            self.viz.take_screenshot(PNG=True, FINAL=True, CROP=True, output_name=output_name,
                                     recordings_dir=self.recordings_dir)
            with open(join(self.recordings_dir, 'counts', output_name + '.json'), 'w') as f:
                json.dump({'beeps_count': self.beeps_count, 'beeps': self.beeped_positions}, f)
            return

        ## draw traces
        if self.HIGHEST_SCORE:
            self.viz.screen.clear()

        ## for drawing traces
        tt = turtle.Turtle()
        tt.hideturtle()
        tt.speed(10)
        tt.pencolor(utils.colors.orange)
        tt.pensize(1)
        for trace in self.traces:
            tt.penup()
            step = 0
            last_real_pos = None
            strange_points = [item for item, count in collections.Counter(trace).items() if count >= 5]
            # if len(strange_points) > 0: print(len(strange_points), strange_points)
            for real_pos in trace:
                if real_pos in strange_points: continue
                if last_real_pos != None:
                    x_l, z_l = last_real_pos
                    x, z = real_pos
                    if abs(x - x_l) > 10 or abs(z - z_l) > 10:
                        continue
                        # print(data['countdown'], last_real_pos, '->', real_pos)

                if step == 0:
                    self.viz.go_to_pos(tt, real_pos)
                else:
                    tt.pendown()
                    self.viz.go_to_pos(tt, real_pos)
                step += 1
                last_real_pos = real_pos

                # if self.HIGHEST_SCORE: self.viz.screen.update()

        self.viz.screen.update()
        self.viz.take_screenshot(PNG=True, FINAL=True, CROP=False, output_name=self.output_name,
                                 recordings_dir=self.recordings_dir)

    def reset_output_name(self):
        self.output_name = f"{self.filter}-[{len(self.files)}]"
        return self.output_name

    def clean_json(self):
        self.get_filtered_files()
        if len(self.files) > 0:
            self.output_name = self.reset_output_name().replace('19', '39')
            json_file = join(self.recordings_dir, 'json', self.output_name + '.json')
            new_traces_replay = {}
            with open(json_file) as f:
                traces_replay = json.load(f)
                for k, v in traces_replay.items():
                    if k in self.files:
                        new_traces_replay[k] = v
                print(self.filter, len(self.files), len(traces_replay), len(new_traces_replay))

                with open(json_file, 'w') as outfile:
                    json.dump(new_traces_replay, outfile, indent=4)

    def run_file(self, trajectory_file):
        rows = self.sort_data(trajectory_file, sorted_path=join('recordings', 'test_analyzer_RITA_saved'))
        self.beeped_positions[trajectory_file] = {}
        last_beeped_tile = None

        trace = []
        trace_replay = {}
        last_real_pos = None
        for row in rows:
            data = row['data']
            sub_type = row['msg']['sub_type']

            if sub_type == 'state' and data['y'] <= 63 and data['y'] >= 60:
                x_low, x_high, z_low, z_high, y_low, y_high = self.ranges
                real_pos = (data['x'] - x_low - 0.5, z_low - data['z'] + 0.5)

                ## data might be screwed up
                trace.append(real_pos)
                trace_replay[data['countdown']] = real_pos
                if last_real_pos != None:
                    x_l, z_l = last_real_pos
                    x, z = real_pos
                    if abs(x - x_l) > 10 or abs(z - z_l) > 10:
                        continue
                        # print(data['countdown'], last_real_pos, '->', real_pos)

                trace.append(real_pos)
                last_real_pos = real_pos

                # ## count beep in another way
                # tile = self.get_tile(data)
                # if tile != None and self.env.tilesummary[tile]['type'] == 'doorstep' and tile != last_beeped_tile:
                #     last_beeped_tile = tile
                #     self.beeped_positions[trajectory_file][tile] = data['countdown']
                #     if tile not in self.beeps_count:
                #         self.beeps_count[tile] = 0
                #     self.beeps_count[tile] += 1

            elif sub_type == 'Event:Beep':
                location = ASIST_settings.get_beep_location(data, self.ranges)
                tile = self.env.tile_indices[location]
                self.beeped_positions[trajectory_file][tile] = data['countdown']
                if tile not in self.beeps_count:
                    self.beeps_count[tile] = 0
                self.beeps_count[tile] += 1

        strange_points = [item for item, count in collections.Counter(trace).items() if count >= 5]
        trace = [t for t in trace if t not in strange_points]
        self.traces.append(trace)
        self.traces_replay[utils.get_dir_file(trajectory_file)[1]] = trace_replay
        if self.verbose: print(f"{len(trace)} steps taken")
        return trace


def test_discrete():
    # discrete 24by24 data
    discrete_dir = join('recordings', 'test_player_types_lrtdp', 'json')
    discrete_dir = join('recordings', '___DARPA', 'player_types', 'json')
    replayer = ReplayerDiscrete(dir=discrete_dir)
    replayer.run()


def test_continuous():
    ## continuous 24by24 data
    continuous_dir = join('trajectories', '24by24', '_done')  ##
    replayer = ReplayerContinuous(dir=continuous_dir)
    replayer.files = [f for f in listdir(continuous_dir) if isfile(join(continuous_dir, f)) and
                      ('test3' in f and '.json' in f)][1:]
    replayer.RITA_ANALYZE_MACROS = True
    replayer.RITA_INFERENCE = True
    replayer.MAGNIFY = True
    replayer.run()


def test_Falcon():
    ## old Falson dir
    # darpa_dir = join('..', '..', '..', 'test', 'falcon', 'used_falcon') #  'data'
    # replayer = ReplayerRITA(dir=darpa_dir)

    ## old Hackathon file
    # darpa_dir = join('..', '..', '..', 'test', 'falcon','used_falcon')
    # darpa_file = join(darpa_dir, 'ASIST_data_study_id_000001_condition_id_000005_trial_id_000015_messages.log')
    # replayer = ReplayerRITA(file=darpa_file)

    ## old nonHSR file
    darpa_dir = join('..', '..', '..', 'test', 'data')
    darpa_file = join(darpa_dir,
                      'NotHSRData_TrialMessages_CondBtwn-3_CondWin-Condition_3-StaticMap_Trial-000034_Team-na_Member-000023_Vers-1.0.log')
    replayer = ReplayerRITA(file=darpa_file)
    replayer.run()


def test_heatmap():
    ## summarize data in heatmap
    darpa_dir = join('..', '..', '..', 'test', 'data')
    replayer = ReplayerRITA(dir=darpa_dir, SUMMARIZE=True)
    replayer.run()


def test_RITA_replay():
    ## new HSR data
    HSR_dir = ASIST_settings.HSR_dir
    HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-43_Team-na_Member-26_Vers-3.log')
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-44_Team-na_Member-26_Vers-3.log')
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-58_Team-na_Member-31_Vers-3.log')
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-65_Team-na_Member-33_Vers-3.log')
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-64_Team-na_Member-33_Vers-3.log')
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-103_Team-na_Member-46_Vers-3.log')
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-105_Team-na_Member-46_Vers-3.log')

    replayer = ReplayerRITA(file=HSR_file, verbose=False, planner_name='ha*')  ##, USE_INTERFACE=False

    ## test the whole replay system
    # replayer.RITA_ANALYZE_MACROS = True
    # replayer.RITA_INFERENCE = False

    # ## just visualizing
    # replayer.RITA_PREDICT = False
    # replayer.RITA_EVALUATE = False
    # replayer.RITA_ANALYZE_MACROS = True
    # replayer.RITA_INFERENCE = False
    # replayer.MAGNIFY = True

    replayer.run()


def test_RITA_visualize():
    ## new HSR data
    HSR_dir = ASIST_settings.HSR_dir

    ## best scores
    HSR_file = join(HSR_dir,
                    'HSRData_TrialMessages_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-242_Team-na_Member-92_Vers-3.log')
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-243_Team-na_Member-92_Vers-3.log')
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-117_Team-na_Member-50_Vers-3.log')

    ## worst scores
    # HSR_file = join(HSR_dir, 'HSRData_TrialMessages_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-99_Team-na_Member-44_Vers-3.log')
    HSR_file = join(HSR_dir,
                    'HSRData_TrialMessages_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-57_Team-na_Member-30_Vers-3.log')

    replayer = ReplayerRITA(file=HSR_file, verbose=False)
    replayer.RITA_ANALYZE_MACROS = False  ## True
    replayer.RITA_INFERENCE = False
    replayer.RITA_PREDICT = False
    replayer.RITA_REALTIME = False
    replayer.MAGNIFY = True
    replayer.run()


def test_RITA_visualize_multiple():
    HSR_dir = utils.get_reL_path(ASIST_settings.HSR_dir)
    # conditions = ASIST_settings.get_conditions()
    conditions = ASIST_settings.get_conditions(
        ['NoTriageNoSignal_FalconEasy-Static', 'TriageSignal_FalconEasy-Static', 'TriageSignal_FalconEasy-Dynamic'])  ##
    for condition in tqdm(conditions, desc='conditions'):
        print('===================', condition)
        files = [f for f in listdir(HSR_dir) if condition in f]
        files.sort()
        index = 0
        for file in files:
            print('   ', index, file)
            index += 1
        index = 0
        for file in tqdm(files, desc='files'):
            print('-------', index, file)
            index += 1
            HSR_file = join(HSR_dir, file)
            replayer = ReplayerRITA(file=HSR_file, verbose=False)

            # replayer.RITA_ANALYZE_MACROS = True
            replayer.RITA_INFERENCE = False
            replayer.RITA_PREDICT = False
            replayer.RITA_REALTIME = False
            replayer.MAGNIFY = True
            replayer.HALF_HALF = True
            replayer.run()


def test_rmq():
    ## wait for RMQ messages
    replayer = ReplayerRMQ()
    replayer.run()


def test_after_action_report():
    ## create after-action report
    HSR_dir = ASIST_settings.HSR_dir
    # filter = 'Falcon'
    # filter = 'Member-46'
    filter = 'Trial-105'
    # filter = 'Trial-179'
    replayer = AnalyzerRITA(dir=HSR_dir, filter=filter, USE_INTERFACE=False)
    replayer.run()


def test_aggregator():
    ## plot all traces
    HSR_dir = ASIST_settings.HSR_dir
    # filter = 'Trial-242'
    filter = 'TriageSignal_CondWin-FalconEasy'
    # filter = 'TriageSignal_CondWin-FalconMed-Dynamic'
    replayer = AggregateRITA(dir=HSR_dir, filter=filter, USE_INTERFACE=False)  ##
    replayer.run()
    replayer.viz.screen.mainloop()


def test_aggregator_multiple():
    ## plot all traces
    HSR_dir = ASIST_settings.HSR_dir
    filters = []
    # filters.extend(['Easy', 'Med', 'Hard'])
    # filters.extend(ASIST_settings.get_conditions())
    # filters.extend(ASIST_settings.get_members())
    for filter in tqdm(filters):
        replayer = AggregateRITA(dir=HSR_dir, filter=filter, USE_INTERFACE=False)
        # replayer.HIGHEST_SCORE = True
        replayer.COUNT_VICTIMS = True
        replayer.run()


if __name__ == '__main__':
    ### -------- developing ----------
    # test_discrete()  ## discrete 24by24 data
    # test_continuous()  ## continuous 24by24 data
    # test_Falcon()  ## old Falcon data
    # test_heatmap()  ## summarize data in heatmap

    ### -------- Dec PI meeting ----------
    # test_RITA_replay()  ## new HSR data
    test_rmq()  ## wait for RMQ messagesso harsssssssssss
    # test_after_action_report()  ## create after-action report

    ### -------- understanding human data ----------
    # test_RITA_visualize()  ## new HSR data
    # test_RITA_visualize_multiple()  ## new HSR data
    # test_aggregator()  ## plot all traces
    # test_aggregator_multiple()  ## plot all traces



