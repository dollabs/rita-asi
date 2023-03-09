#!/usr/bin/env python

import time
import json
import turtle
from tqdm import tqdm
from turtle import Turtle
import operator
from PIL import Image
import pandas as pd
import os
from os import listdir
from os.path import isfile, join
import random
from pprint import pprint, pformat
import threading
import copy
import imageio
import matplotlib.pyplot as plt
from tkinter import PhotoImage
from turtle import Turtle, Screen, Shape
import numpy as np
import math
from datetime import datetime
import argparse
import csv
import sys
# sys.path.insert(1, 'algorithms')
# sys.path.insert(1, 'algorithms/rl')
sys.path.insert(1, '../rita')

import mdp
import tabular
import mcts
import uct
import mapreader
import player
import inverse
import hierarchical
# import learning
# import observe_malmo  ## can comment this out if you haven't install Malmo
import rmq  ## can comment this out if you haven't install pika and threading
import raycasting
import planners
import interface
import utils

## python visualize.py -m REPLAY_IN_RITA
parser = argparse.ArgumentParser(description='Parse arguments for the code.')
parser.add_argument('-m', '--mode', type=str,
    default='', help='Mode of code you want to test')
parser.add_argument('-f', '--file', type=str,
    default='', help='Relative path of log file you want to test')
parser.add_argument('-d', '--directory', type=str,
    default='', help='Relative path of directory of log files that you want to test')
parser.add_argument('-t', '--host', type=str,
    default='localhost', help='RabbitMQ host, if not localhost')
parser.add_argument('-p', '--port', type=str,
    default='5672', help='RabbitMQ port, if not 5672')
parser.add_argument('-i', '--interface', type=str,
    default='1', help='flag if interface is used')
args = parser.parse_args()

# ----------------------------------
#  STEP 1 - choosing test mode
# ----------------------------------
MODE = 'PLANNING'   ## normal VI
# MODE = 'PLANNING_BY_MCTS'   ## normal monte carlo tree search on the entire state space
MODE = 'HIERARCHICAL_PLANNING'  ## two-level VI
# MODE = 'TILE_LEVEL_DFS'     ## VI-DFS
# MODE = 'ROOM_LEVEL_MCTS'     ## VI-MCTS
# MODE = 'INVERSE_PLANNING'   ## default is VI
# MODE = 'INVERSE_PLANNING_HVI'   ## use hierarchical VI for inverse planning
# MODE = 'EXPERIMENT_REPLAY'  ## visualize human trajectory, generate PNG, or PNGs, or GIF
# MODE = 'REPLAY_WITH_TOM'  ## replaying human trajectory, making inference and predictions
MODE = 'REPLAY_IN_RITA'   ## receiving real-time messages from RITA test-bed
# MODE = 'REPLAY_IN_MALMO'  ## visualize in Malmo environment
# MODE = 'REPLAY_DISCRETIZE'  ## generate discretized trajectory file
# MODE = 'EXPERIMENT_PARAM'   ## test how parameters affect planning algorithms
# MODE = 'EXPERIMENT_ALGO'    ## test how different algorithms affect planning performance
# MODE = 'PRINT_MAP'  ## take screenshot of map, random initialize starting location
# MODE = 'LEARNING'   ## for reinforcement learning

# ----------------------------------
#  STEP 2 - choosing the test maze
# ----------------------------------
MAP = None                  ## use the default testing map for the mode
MAP = '6by6_3_Z.csv'        ## for testing that planning algorithms work
# MAP = '6by6_6_T-0.csv'      ## for testing that RL algorithms work
# MAP = '12by12_darpa.csv'    ## for testing the efficiency of algorithms
# MAP = '12by12/12by12_R5A-V2-P10.csv'
# MAP = '13by13_3.csv'        ## part of the 24 by 24 test mazes
# MAP = '24by24_6.csv'        ## the 24 by 24 mazes
MAP = 'test2.csv'           ## for replaying human experiments in 24 by 24 mazes
# MAP = '36by64_40.csv'       ## Stata maze
# MAP = '46by45.csv'          ## DARPA maze
# MAP = '46by45_2.csv'          ## DARPA maze 2
MAP = '48by89.csv'

if args.mode != '':
    MODE = args.mode
    if args.mode == 'REPLAY_IN_RITA':
        print('Choosing the mode of listening to rabbitmq messages')
        if args.file != '' or args.directory != '':
            print('... ignoring the other args because it listens to messages instead of reading files')
    elif args.mode == 'REPLAY_WITH_TOM':
        if args.directory != '':
            print('Choosing the mode of replaying all files in a directory: ', args.directory)
        elif args.file != '':
            print('Choosing the mode of replaying one file: ')
        else:
            print('You have to specify a file (-f) or a directory (-d) to replay. Quiting now ...')
            sys.exit()

# ----------------------------------
#  STEP 3 - choosing the agent type
# ----------------------------------
PLAYER_NAME = 'hvi' #  Q-Learner   difficult   with_dog    test   dfs
PLAYERS = [
    # 'with_dog_green', 'with_dog_yellow'
    'both', 'green', 'yellow', 'with_dog_both', 'with_dog_green', 'with_dog_yellow'
    ]  ## to be inferred in inverse planning

# PLAYER_NAME = 'systematic' #  Q-Learner   difficult   with_dog    test   dfs
# PLAYERS = ['systematic', 'with_dog_both', 'with_dog_yellow']  ## to be inferred in inverse planning

# ----------------------------------
#  STEP 4 - choosing output style and location
# ----------------------------------
PLOT_FOLDER = 'plots'
TRAJECTORY_FILE = None
TRAJECTORY_FILE = join('..', '..', '..', 'test', 'falcon', 'used_falcon', 'ASIST_data_study_id_000001_condition_id_000005_trial_id_000015_messages.log') #
# TRAJECTORY_FOLDER = join('..', '..', '..', 'test', 'data') #
TRAJECTORY_FOLDER = 'trajectories/24by24' # 'recordings/200410 12by12/' #
# TRAJECTORY_FOLDER = join(TRAJECTORY_FOLDER, 'hackathon','Falcon') # 'Sparky')  # join(TRAJECTORY_FOLDER,"24by24") #
RECORDING_FOLDER = 'recordings/_test cases/'
OUTPUT_NAME = ''

# ----------------------------------------------------------------------------------
#  default configuration
# ----------------------------------------------------------------------------------
## for algorithms
RANDOM_INITIALIZE_V = False     ## for value iteration
ROOM_WEIGHTS = 1        ## for hierarchical planning, calculating room-level value
WINDOW_LENGTH = 20      ## for inverse planning
TEMPERATURE = 0.01       ## for inverse planning, calculating posterioir
SCORE_GAMMA = 0.99      ## for experiments, calculating discounted scores

## for MODEs
PRINT_MAP = False
EXPERIMENT_PARAM = False
EXPERIMENT_ALGO = False

EXPERIMENT_REPLAY = False
REPLAY_TO_PNGs = False  ## for generating screenshots
REPLAY_TO_GIF = False  ## for generating a gif of trace
REPLAY_TO_PNG = True  ## for generating final trace
REPLAY_WITH_TOM = False
REPLAY_IN_RITA = False
REPLAY_IN_MALMO = False
REPLAY_DISCRETIZE = False  ## for generating discretized trajectory file ------- ##TODO

INVERSE_PLANNING = False
INVERSE_PLANNING_HVI = False
PLANNING = False
PLANNING_BY_MCTS = False
HIERARCHICAL_PLANNING = False
TILE_LEVEL_DFS = False
ROOM_LEVEL_MCTS = False
MCTS_RUNTIME = 3
MCTS_DEPTH = 14

CROP_PNG = False        ## usually false because we want to see the whole Turtle interface
GENERATE_PNG = True     ## usually true
GENERATE_PNGs = False   ## for debugging frame by frame
GENERATE_GIF = False    ## for visualizing the process
GENERATE_TXT = False    ## for experiments comparing algorithms, recorded the log of each run

LEARNING = False        ## reinforcement learning mode

# ----------------------------------
#  automatic configuration
# ----------------------------------
READ_ROOMS = True

if 'PLANNING' in MODE or 'LEVEL' in MODE:

    if 'INVERSE_PLANNING' in MODE:
        INVERSE_PLANNING = True

        if MODE == 'INVERSE_PLANNING_HVI':
            INVERSE_PLANNING_HVI = True

    else:
        PLANNING = True  ## normal VI

        if MODE == 'PLANNING_BY_MCTS':
            PLANNING_BY_MCTS = True

        elif MODE == 'HIERARCHICAL_PLANNING':   ## hierarchical VI, used by default by inverse planning
            HIERARCHICAL_PLANNING = True

        elif MODE == 'TILE_LEVEL_DFS':     ## VI-DFS
            TILE_LEVEL_DFS = True
            HIERARCHICAL_PLANNING = True

        elif MODE == 'ROOM_LEVEL_MCTS':     ## VI-MCTS
            ROOM_LEVEL_MCTS = True
            HIERARCHICAL_PLANNING = True

    if MAP == None: MAP = '6by6_3_Z.csv'

elif 'REPLAY' in MODE:  ## visualize human trajectory
    EXPERIMENT_REPLAY = True
    READ_ROOMS = False
    CROP_PNG = True

    if MAP == None: MAP = 'test2.csv'

    if MODE == 'EXPERIMENT_REPLAY':  ## visualize human trajectory, generate PNG, or PNGs, or GIF
        REPLAY_TO_PNG = True

    elif MODE == 'REPLAY_WITH_TOM' or MODE == 'REPLAY_IN_RITA':

        if MODE == 'REPLAY_WITH_TOM': ## replaying human trajectory, making inference and predictions
            REPLAY_WITH_TOM = True

        elif MODE == 'REPLAY_IN_RITA': ## receriving real-time messages from RITA test-bed
            REPLAY_IN_RITA = True

        if join('test', 'data') not in TRAJECTORY_FOLDER:
            TRAJECTORY_FOLDER = join(TRAJECTORY_FOLDER, 'hackathon', 'Falcon')

        ## by default, test in Falcon map
        MAP = '48by89.csv'
        READ_ROOMS = True

        # if 'Sparky' in TRAJECTORY_FOLDER:
        #     MAP = '46by45_2.csv'
        # elif 'Falcon' in TRAJECTORY_FOLDER:
        #     MAP = '48by89.csv'

    elif MODE == 'REPLAY_IN_MALMO':  ## visualize in Malmo environment
        REPLAY_IN_MALMO = True

    elif MODE == 'REPLAY_DISCRETIZE':  ## generate discretized trajectory file
        REPLAY_DISCRETIZE = True



elif MODE == 'EXPERIMENT_PARAM':   ## test how parameters affect planning algorithms
    EXPERIMENT_PARAM = True
elif MODE == 'EXPERIMENT_ALGO':    ## test how different algorithms affect planning performance
    EXPERIMENT_ALGO = True
elif MODE == 'PRINT_MAP':  ## take screenshot of map, random initialize starting location
    PRINT_MAP = True
elif MODE == 'LEARNING':  ## for reinforcement learning
    LEARNING = True
elif MODE == 'TESTING':
    TESTING = True

## change configuration according to player file
if 'planning' in player.players[PLAYER_NAME]:
    algo = player.players[PLAYER_NAME]['planning']
    if 'tile-' in algo:
        HIERARCHICAL_PLANNING = True
    else:
        HIERARCHICAL_PLANNING = False
    if '-dfs' in algo:
        TILE_LEVEL_DFS = True
        ROOM_LEVEL_MCTS = False
    elif 'vi' in algo:
        TILE_LEVEL_DFS = False
        ROOM_LEVEL_MCTS = False

## specify the MAP_ROOM, WORLD_WIDTH, WORLD_HEIGHT, TILE_SIZE, MAX_ITER
MAP_CONFIG = {
    '6by6_3_Z.csv': ['6by6_rooms.csv', 6, 6, 60, 50],
    '6by6_6_T-0.csv': [None, 6, 6, 60, None],
    '12by12_darpa.csv': ['12by12_rooms.csv', 12, 12, 30, 100],
    '12by12/12by12_R5A-V2-P10.csv': ['12by12/12by12_R5A-rooms.csv', 12, 12, 30, 100],
    '13by13_3.csv': ['13by13_6_rooms.csv', 13, 13, 30, 150],
    '24by24_6.csv': ['24by24_6_rooms.csv', 24, 24, 16, 250],
    'test2.csv': [None, 24, 24, 16, 1000],
    '36by64_40.csv': ['36by64_40_rooms.csv', 64, 36, 14, 250],
    '46by45.csv': ['46by45_rooms.csv', 45, 46, 10, 1000],
    '46by45_2.csv': ['46by45_2_rooms.csv', 45, 46, 10, 1000],
    '48by89.csv': ['48by89_rooms.csv', 89, 48, 10, 1300]
}

MAP_ROOM, WORLD_WIDTH, WORLD_HEIGHT, TILE_SIZE, MAX_ITER = MAP_CONFIG[MAP]

## ----------------------------------------
#    Configurations that usually don't change
## ----------------------------------------

## colors for visualizing Q-table in reinforcement learning
BLUE = '#3498db'
GREEN = '#2ecc71'
YELLOW = '#f1c40f'
RED = '#e74c3c'

# for visualization
PRINT_CONSOLE = False

SHOW_R_TABLE = True
SHOW_ROOMS = False  ## show room level value and destination
SHOW_LIKELIHOODS = False  # for debugging inverse
SHOW_PI_TABLE = False  # for debugging inverse, TODO: needs fixing
SHOW_PLOT = True  # show inverse planning plot in a separate window
SHOW_MCTREE = True
SHOW_WALLS = True

## for agent config
MAX_TURN = 8
AGENT_OBS_ANGLE = 45
AGENT_OBS_N_RAYS = 70   ## reduce it might not save 0.01 seconds but miss several blocks crucial for observation
AGENT_OBS_RANGE = 40

## for map config
USE_INTERFACE = True
USE_SAVED_OBS = True    ## instead of using raycasting every time
USE_SAVED_MAP = True    ## instead of printing
USE_SAVED_TRAJECTORIES = True   ## instead of using the most recently generated trajectories filee
USE_STATA = '36by64' in MAP
USE_DARPA = '46by45' in MAP or '48by89' in MAP
USE_CHEST = True    # has chest open effect to indicate reward has been collected

if int(args.interface) == 0:
    print("!! not using interface")
    USE_INTERFACE = False

def no_interface():
    SHOW_R_TABLE = False
    SHOW_ROOMS = False
    SHOW_LIKELIHOODS = False
    SHOW_PI_TABLE = False
    SHOW_PLOT = False
    SHOW_MCTREE = False
    SHOW_WALLS = False

if not USE_INTERFACE: no_interface()

# for window visualization
PAUSE_AT_BEGINNING = False
MAZE_HOR_OFFSET = -40 + TILE_SIZE
MAZE_VER_OFFSET = 220 - TILE_SIZE
# MAZE_HOR_OFFSET = 140
# MAZE_VER_OFFSET = 0
WINDOW_HEIGHT = 600
WINDOW_WIDTH = 900
if USE_STATA:
    WINDOW_WIDTH = 1350
if '48by89' in MAP:
    WINDOW_WIDTH = 1680
    # MAZE_VER_OFFSET = 30
    # MAZE_HOR_OFFSET = 60
elif '46by45' in MAP and (REPLAY_IN_RITA or REPLAY_WITH_TOM):
    WINDOW_WIDTH = 1680
if USE_DARPA:
    # MAZE_VER_OFFSET = 70
    USE_CHEST = False
    # SHOW_ROOMS = False

FONT_SIZE = int(TILE_SIZE/5)
VISIT_COUNT = {} # for RL, a dict of dict for all frequencies of (s,a)
COLOR_WHEEL = []
COLOR_DENSITY = 4

## visualization texture

SIZER_TILE_SIZE = {
    3: 60,
    6: 60,
    12: 30,
    13: 30,
    24: 16,
    46: 12
}

IMG_PLAYER = join("texture","TILE_SIZE","playerHEAD.gif")
IMGS = {
    'wall': join("texture","TILE_SIZE","wall.gif"),
    'entrance': join("texture","TILE_SIZE","entrance.gif"),
    'grass': join("texture","TILE_SIZE","grass.gif"),
    'fire': join("texture","TILE_SIZE","fire.gif"),
    'air': join("texture","TILE_SIZE","air.gif"),
    'gravel': join("texture","TILE_SIZE","gravel.gif"),
    'door': join("texture","TILE_SIZE","door.gif"),
    'victim': join("texture","TILE_SIZE","victim-green.gif"),
    'victim-yellow': join("texture","TILE_SIZE","victim-yellow.gif")
}

def draw_maze(env, IMG_PLAYER=IMG_PLAYER, IMGS=IMGS, main_loop=False):
    """ draws the gridworld """

    if not USE_INTERFACE: return None, None, None, None

    ## show green and yellow chests instead of yellow and green victims
    if (EXPERIMENT_REPLAY and not REPLAY_IN_RITA and not REPLAY_WITH_TOM) or USE_CHEST:
        IMGS['victim'] = join("texture","TILE_SIZE","chest.gif")
        IMGS['victim-yellow'] = join("texture","TILE_SIZE","chest-green.gif")
    elif LEARNING and USE_STATA:
        IMGS['victim'] = join("texture","TILE_SIZE","hand-sanitizer.gif")

    IMG_PLAYER = IMG_PLAYER.replace('TILE_SIZE',str(TILE_SIZE))
    for key in IMGS.keys():
        IMGS[key] = IMGS[key].replace('TILE_SIZE',str(TILE_SIZE))

    # -------------------------------------
    ## ------- initialize screen and shapes
    # -------------------------------------
    screen = turtle.Screen()
    # screen.bgcolor('#F7F7F7') # background color
    screen.bgcolor('#FFFFFF') # background color
    screen.setup(width = WINDOW_WIDTH, height = WINDOW_HEIGHT) # window size
    screen.tracer(0,0)
    screen.title("Yang's Minecraft Playground")
    for type in IMGS.keys():
        screen.register_shape(IMGS[type])
        screen.register_shape(IMGS[type].replace(str(TILE_SIZE)+'/',str(TILE_SIZE)+'/obs-').replace(str(TILE_SIZE)+'\\',str(TILE_SIZE)+'\\obs-'))
        if 'victim' in type:
            screen.register_shape(IMGS[type].replace(str(TILE_SIZE)+'/',str(TILE_SIZE)+'/open-').replace(str(TILE_SIZE)+'\\',str(TILE_SIZE)+'\\open-'))

    if REPLAY_IN_RITA or REPLAY_WITH_TOM:
        screen.register_shape(join('texture',str(TILE_SIZE),'open-victim.gif'))
        screen.register_shape(join('texture', str(TILE_SIZE), 'obs-victim-red.gif'))

    ts = TILE_SIZE
    s0 = env._pos_agent

    ## find the boundary of the maze
    x_max = -100
    x_min = 100
    y_max = -100
    y_min = 100
    for tile in env.tilesummary_truth.keys():
        i,j = env.tilesummary[tile]['pos']
        if i > x_max:
            x_max = i
        if i < x_min:
            x_min = i
        if j > y_max:
            y_max = j
        if j < y_min:
            y_min = j

    # -------------------------------------
    ## ------- if there is a saved map gif, use it
    # -------------------------------------
    mypath = join('texture','screens')
    file_name = join(mypath,'screen_'+env.MAP.replace('/','_'))
    files = [join(mypath,f) for f in listdir(mypath) if env.MAP.replace('.csv','.gif') in str(f) and '.gif' in str(f)]
    start = time.time()

    if False: #USE_SAVED_MAP and len(files) == 1:
        screen.register_shape(files[0])
        dc = turtle.Turtle()
        dc.shape(files[0])
        dc.penup()
        dc.speed(10)
        dc.goto(-4,4)
        dc.stamp()
        screen.update()

    # -------------------------------------
    ## ------- otherwise, print and save it as gif
    # -------------------------------------
    else:
    # if True:

        ## initialize main interface
        dc = turtle.Turtle() # dc is just some arbitrary name
        dc.hideturtle()
        dc.speed(10)

        ## find locations of wall be removing the other blocks
        outer_walls = []
        for y in range(y_min-1,y_max+2):
            for x in range(x_min-1,x_max+2):
                outer_walls.append((x,y))

        ## print all tiles onto the screen
        for tile in range(len(env.tilesummary)):
            x,y = env.tilesummary[tile]['pos']
            outer_walls.remove((x,y))

            # set each grid with Minecraft texture
            type = env.tilesummary[tile]['type']
            dc.shape(IMGS[type])
            dc.penup()
            dc.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
            dc.showturtle()
            dc.stamp()

        if SHOW_WALLS:
            for loc in outer_walls:
                x,y = loc
                dc.shape(IMGS['wall'])
                dc.penup()
                dc.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
                dc.showturtle()
                dc.stamp()

        # -------------------------------------
        ## ------- save the portion as a GIF to be directly stamped the next time
        # -------------------------------------
        screen.update()
        screen.getcanvas().postscript(file=file_name.replace('.csv','.eps'))

        ## convert it to gif
        # images = [imageio.imread(file_name.replace('.csv','.eps'))]
        # imageio.mimsave(file_name.replace("csv","gif"), images)
        # os.remove(file_name.replace('.csv','.eps'))

        im = Image.open(file_name.replace('.csv','.eps'))
        fig = im.convert('RGBA')
        fig = fig.resize((WINDOW_WIDTH,WINDOW_HEIGHT), Image.ANTIALIAS)
        image_gif= file_name.replace("csv","gif")
        fig.save(image_gif, save_all=True, append_images=[fig])
        im.close()
        os.remove(file_name.replace('.csv','.eps'))


    # -------------------------------------
    ## ------- initialize player
    # -------------------------------------
    screen.register_shape(join('texture',str(TILE_SIZE),'player0.gif'))
    screen.register_shape(join('texture',str(TILE_SIZE),'player90.gif'))
    screen.register_shape(join('texture',str(TILE_SIZE),'player180.gif'))
    screen.register_shape(join('texture',str(TILE_SIZE),'player270.gif'))
    screen.register_shape(IMGS['air'])

    agent = turtle.Turtle()
    agent.shape(IMG_PLAYER.replace('HEAD',str(s0[1])).replace('TILE_SIZE',str(TILE_SIZE)))
    agent.hideturtle()
    agent.penup()

    # draw initial position
    if not REPLAY_WITH_TOM and not REPLAY_IN_RITA:
        x,y = env.tilesummary[s0[0]]['pos']
        agent.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
        agent.showturtle()
        agent.pendown()
        agent.stamp()

    # -------------------------------------
    ## ------- if RL, initialize heatmap dict
    # -------------------------------------
    if LEARNING:
        # initializee_color_wheel()
        VISIT_COUNT['count'] = 0
        for state in env.states:
            temp = {}
            for action in env.actions:
                temp[action] = 0
            VISIT_COUNT[state] = temp


    if PRINT_CONSOLE: print('... finished drawing the maze in ', str(time.time() - start), 'seconds')

    ## show the room segmentation map
    if (REPLAY_IN_RITA or REPLAY_WITH_TOM) and 'Falcon' not in TRAJECTORY_FOLDER and False:

        screen.addshape(room_snap)
        plotter = Turtle(room_snap)
        plotter.hideturtle()
        plotter.speed(10)
        plotter.penup()
        plotter.goto(620, 130)
        plotter.showturtle()
        plotter.stamp()

    if main_loop:
        screen.mainloop()

    return screen, ts, dc, agent

def update_reward_board(env,writer,s,ver_shift):

    if SHOW_R_TABLE:

        to_evaluate = {}
        for head in range(4):
            to_evaluate[head] = pd.DataFrame('', index=range(WORLD_HEIGHT), columns=range(WORLD_WIDTH))

        obs_map = pd.DataFrame('', index=range(WORLD_HEIGHT), columns=range(WORLD_WIDTH))
        for index in env.tilesummary.keys():
            tile = env.tilesummary[index]
            room = env.tiles2room[index]
            if room!=None: summary = env.rooms[room]['tilesummary'][index]
            if tile['type'] != 'wall':
                i,j = mapreader.coord(tile['pos'])

                max_reward = -np.inf
                for head in range(4):
                    reward = env.R((index, 90 * head), 'go_straight') + env.R((index, 90 * head), 'triage')
                    max_reward = max(max_reward, reward)

                    to_evaluate[head][i][j] = reward
                    if room!=None: summary['reward_facing_'+str(90 * head)] = reward

                obs_map[i][j] = round(max_reward,2) # round(tile['reward'],2) #

        row = env.tilesummary[s[0]]['row']
        col = env.tilesummary[s[0]]['col']
        agent = {0:'>', 90:'^', 180:'<', 270:'v'}[s[1]]
        obs_map.loc[row,col] = agent + str(obs_map.loc[row,col]) + agent

        ## print the whole table directly
        if TILE_SIZE >= 30:
            writer.goto(-420,ver_shift)
            writer.write(obs_map.to_string(), font=("Courier", FONT_SIZE, 'normal'))

        ## select the 4 blocks near me to print
        else:
            fontsz = 8
            gaze = 6
            to_print = obs_map.loc[max(0,row-gaze): min(WORLD_HEIGHT,row+gaze+1), max(0,col-gaze): min(WORLD_WIDTH,col+gaze+1)].to_string()
            writer.goto(-420,ver_shift)
            writer.write(to_print, font=("Courier", fontsz, 'normal'))

        print()

def generate_color_wheel(original_color, size):

    def hex2int(hex1):
        return int('0x'+str(hex1),0)

    def int2hex(int1):
        hex1 = str(hex(int1)).replace('0x','')
        if len(hex1) == 1:
            hex1 = '0'+hex1
        return hex1

    def hex2ints(original_color):
        R_hex = original_color[0:2]
        G_hex = original_color[2:4]
        B_hex = original_color[4:6]
        R_int = hex2int(R_hex)
        G_int = hex2int(G_hex)
        B_int = hex2int(B_hex)
        return R_int, G_int, B_int

    def ints2hex(R_int, G_int, B_int):
        return '#'+int2hex(R_int)+int2hex(G_int)+int2hex(B_int)

    def portion(total, size, index):
        return total + round((225-total) / size * index)

    def gradients(start, end, size, index):
        return start + round((end-start) / size * index)

    color_wheel = []

    ## for experience replay, find all the colors between two colors
    if len(original_color) == 2:

        color1, color2 = original_color
        R1_int, G1_int, B1_int = hex2ints(color1.replace('#',''))
        R2_int, G2_int, B2_int = hex2ints(color2.replace('#',''))
        for index in range(size):
            color_wheel.append(ints2hex(
                gradients(R1_int, R2_int, size, index),
                gradients(G1_int, G2_int, size, index),
                gradients(B1_int, B2_int, size, index)
            ))

    ## for RL, the color of different shades symbolizes frequency
    else:

        R_int, G_int, B_int = hex2ints(original_color.replace('#',''))

        seq = list(range(size))
        seq.reverse()
        for index in seq:
            color_wheel.append(ints2hex(
                portion(R_int, size, index),
                portion(G_int, size, index),
                portion(B_int, size, index)
            ))

    return color_wheel

def initializee_color_wheel(color_density=None, rainbow=False):

    COLOR_WHEEL = []

    if EXPERIMENT_REPLAY or INVERSE_PLANNING or PLANNING or rainbow: # or learning.USE_HUMAN_TRAJECTORY

        ## rainbow color of the material UI style
        colors = ['#F44336','#E91E63','#9C27B0','#673AB7', '#3F51B5', '#2196F3', '#03A9F4', '#00BCD4', '#009688', '#4CAF50', '#8BC34A', '#CDDC39', '#FFEB3B', '#FFC107', '#FF9800', '#FF5722']
        COLOR_DENSITY = math.ceil(color_density/(len(colors)-1))
        for i in range(1,len(colors)):
            COLOR_WHEEL += generate_color_wheel((colors[i-1],colors[i]), COLOR_DENSITY)

    elif LEARNING or learning.MAIN or (PLANNING and tabular.SHOW_Q_TABLE):

        ## a small range of colors of the flat UI color style
        if learning.USE_HUMAN_TRAJECTORY:
            color_density *= 2

        ## Q-table type
        if LEARNING and learning.SHOW_Q_TABLE:
            colors = [GREEN,RED]
            if color_density==None:
                color_density = math.ceil(learning.MAX_STEP)

        elif PLANNING and tabular.SHOW_Q_TABLE:
            colors = [RED,GREEN]
            if color_density==None:
                color_density = math.ceil(learning.MAX_STEP)

        ## M&M type
        else:
            colors = [BLUE, GREEN, YELLOW, RED]
            if color_density==None:
                color_density = math.ceil(learning.MAX_STEP/100)

        for original_color in colors:
            little_wheel = generate_color_wheel(original_color, color_density)
            if learning.SHOW_Q_TABLE and original_color == RED: little_wheel.reverse()
            COLOR_WHEEL += little_wheel

    return COLOR_WHEEL

def get_color(sa):
    s,a = sa
    COLOR_WHEEL = initializee_color_wheel()
    s = [s]

    if USE_STATA:
        count = 0
        tile = s[0]
        for a in ['go_straight', 'turn_right', 'turn_left']:
            for heading in [0,90,180,270]:
                count += VISIT_COUNT[(tile, heading)][a]
    else:
        count = VISIT_COUNT[s][a]
    count = max(0,min(count,len(COLOR_WHEEL)-1))

    return COLOR_WHEEL[count]

def update_maze(env,agent,dc,screen,ts,s,trace,
    real_pos=None,
    a=None,sa_last=None,
    length=None,
    reward=None,  ## used for updating nearby victim status based on scores given
    trajectory=None,
    tiles_to_color=None,
    tiles_to_change=None):

    if not USE_INTERFACE: return

    DEBUG = False
    start = time.time()

    ## ------------------------------
    ##  Step 1 --- update the environment, on screen and in env
    ## ------------------------------

    ##  --------- adjust visualization texture for the environment
    ## show green and yellow chests instead of yellow and green victims
    if (EXPERIMENT_REPLAY and not REPLAY_IN_RITA and not REPLAY_WITH_TOM) or USE_CHEST:
        # print('USE CHESTS')
        IMGS['victim'] = join("texture","TILE_SIZE","chest.gif")
        IMGS['victim-yellow'] = join("texture","TILE_SIZE","chest-green.gif")
    if LEARNING and USE_STATA:
        IMGS['victim'] = join("texture","TILE_SIZE","hand-sanitizer.gif")


    ##  --------- update observation when in planning/inverse planning
    if not LEARNING:

        # if DEBUG: print('___________ finished printing shadow in', round(time.time()-start,3))
        start = time.time()

        ##  --------- color the observed tiles on screen to yellow

        ## when updating only the last time after all actions are chosen, color all
        if (PLANNING and not HIERARCHICAL_PLANNING and tabular.JUST_TRAJECTORY) or (HIERARCHICAL_PLANNING and hierarchical.JUST_TRAJECTORY):
            tiles_to_color = env.observed_tiles


        # ################ WHAT DOES TRAJECTORY DO?
        # if not tabular.JUST_TRAJECTORY or trajectory != None:
        dc.speed(10)
        if tiles_to_color != None:
            for tile in tiles_to_color:
                x,y = env.tilesummary_truth[tile]['pos']

                ## add 'obs-' prefix to texture file
                type = env.tilesummary[tile]['type']
                if (not REPLAY_IN_RITA and not REPLAY_WITH_TOM) or 'TILE_SIZE' in IMGS[type]:
                    img_name = IMGS[type].replace('TILE_SIZE/', str(TILE_SIZE) + '/obs-').replace('TILE_SIZE\\', str(TILE_SIZE) + '\\obs-')
                else:
                    img_name = IMGS[type].replace(str(TILE_SIZE)+'/', str(TILE_SIZE) + '/obs-').replace(str(TILE_SIZE)+'\\', str(TILE_SIZE) + '\\obs-')
                dc.shape(img_name)
                dc.penup()
                dc.goto(MAZE_HOR_OFFSET+x*ts, MAZE_VER_OFFSET+y*ts)
                dc.stamp()

        ## change tiles to what they actually are
        if tiles_to_change != None:
            for tile in tiles_to_change:
                x,y = env.tilesummary_truth[tile]['pos']
                type = env.tilesummary[tile]['type']
                dc.shape(IMGS[type].replace('TILE_SIZE/', str(TILE_SIZE) + '/obs-').replace('TILE_SIZE\\', str(TILE_SIZE) + '\\obs-'))
                dc.penup()
                dc.goto(MAZE_HOR_OFFSET+x*ts, MAZE_VER_OFFSET+y*ts)
                dc.stamp()

    ## ------------------------------
    ##  Step 2 --- update the agent, in many different ways
    ## ------------------------------
    ##  --------- update turtle (past state)
    agent.shape(IMG_PLAYER.replace('HEAD',str(s[1])).replace('TILE_SIZE',str(TILE_SIZE)))
    agent.hideturtle()
    agent.speed(10)

    ## for replay, show past trajectory as a trace #
    if EXPERIMENT_REPLAY or INVERSE_PLANNING or (PLANNING and not tabular.JUST_TRAJECTORY): # or learning.USE_HUMAN_TRAJECTORY:

        ##  --------- change chests to open
        if reward != None:
            y,x = mapreader.coord(env.tilesummary[s[0]]['pos'])
            combinations = [(0,0)]
            ## recorded human position can be within 3 block range from the reward
            if EXPERIMENT_REPLAY:
                combinations = [ (i,j) for i in range(-1,2) for j in range(-1,2)]
            for comb in combinations:
                i,j = comb
                if (x+i,y+j) in env.tile_indices.keys():
                    tile = env.tile_indices[(x+i,y+j)]
                    x_k,y_k = env.tilesummary_truth[tile]['pos']
                    type = env.tilesummary_truth[tile]['type']
                    if (reward == 1 or reward == 2) and (type == 'victim' or type == 'victim-yellow'):
                        dc.shape(IMGS[type].replace('TILE_SIZE/',str(TILE_SIZE)+'/open-').replace('TILE_SIZE\\',str(TILE_SIZE)+'\\open-'))
                        dc.penup()
                        dc.speed(10)
                        dc.goto(MAZE_HOR_OFFSET+x_k*ts,MAZE_VER_OFFSET+y_k*ts)
                        dc.stamp()

        x,y = real_pos

        ############ SOMEHOW THERE NEEDS A SHIFT
        # if EXPERIMENT_REPLAY:
        #     if TILE_SIZE == 16:
        #         HOR_OFFSET = -188
        #         VER_OFFSET = 8
        #     elif TILE_SIZE == 10:
        #         HOR_OFFSET = -206
        #         VER_OFFSET = 76
        #         # HOR_OFFSET = -244
        #         # VER_OFFSET = 76
        #     if 'Falcon' in TRAJECTORY_FOLDER or REPLAY_WITH_TOM or REPLAY_IN_RITA:
        #         HOR_OFFSET = -286
        #         VER_OFFSET = 76
        # else:
        #     HOR_OFFSET = MAZE_HOR_OFFSET
        #     VER_OFFSET = MAZE_VER_OFFSET
        HOR_OFFSET = MAZE_HOR_OFFSET
        VER_OFFSET = MAZE_VER_OFFSET

        ## draw a colorful trace from the last position to the current position
        if sa_last != None:
            if length == None: length = MAX_ITER
            dc = Turtle()
            dc.shape(IMGS['air'].replace('TILE_SIZE/',str(TILE_SIZE)+'/obs-').replace('TILE_SIZE\\',str(TILE_SIZE)+'\\obs-'))
            dc.hideturtle()
            dc.speed(10)
            color = initializee_color_wheel(length+1)[env.step]
            dc.pencolor(color)
            dc.fillcolor(color)
            dc.pensize( 4 - (4-2)*env.step/MAX_ITER ) ## 2

            dc.penup()
            x_last, y_last = sa_last
            dc.goto(HOR_OFFSET+x_last*ts,VER_OFFSET+(y_last)*ts)
            dc.pendown()
            dc.goto(HOR_OFFSET+x*ts,VER_OFFSET+(y)*ts)

        agent.clear()
        agent.penup()
        agent.goto(HOR_OFFSET+x*ts,VER_OFFSET+(y)*ts)
        agent.showturtle()
        agent.stamp()

        if DEBUG: print('___________ finished drawing trace in', round(time.time()-start,3))
        start = time.time()

    ## the M&M style visualization
    elif LEARNING and learning.SHOW_FINAL:

        cir = Turtle()
        cir.hideturtle()
        cir.penup()
        cir.shape("circle")
        cir.shapesize(0.2, 0.2, 0)

        color_wheel = initializee_color_wheel()

        # VISIT_COUNT = trace
        for ss,count in trace.items():   ## trace here is visit count

            ss = ss[0]
            x, y = env.tilesummary[ss]['pos']
            x = MAZE_HOR_OFFSET+x*ts
            y = MAZE_VER_OFFSET+y*ts
            if 'victim' in env.tilesummary_truth[ss]['type']:
                # print(env.tilesummary_truth[ss]['type'], (env.tilesummary[ss]['row'],env.tilesummary[ss]['col']))
                dc.shape(IMGS[env.tilesummary_truth[ss]['type']].replace('TILE_SIZE/',str(TILE_SIZE)+'/open-').replace('TILE_SIZE\\',str(TILE_SIZE)+'\\open-'))
            else:
                dc.shape(IMGS['air'])
            dc.goto(x,y)
            dc.showturtle()
            dc.stamp()

            color = color_wheel[min(len(color_wheel)-1, count)]
            cir.pencolor(color)
            cir.fillcolor(color)
            cir.goto(x,y)
            cir.pendown()
            cir.showturtle()
            cir.stamp()
            cir.penup()

        ## print my current avatar
        s = s[0]
        x,y = env.tilesummary[s]['pos']
        agent.clear()
        agent.penup()
        agent.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
        agent.showturtle()
        agent.stamp()

    elif (LEARNING and learning.SHOW_Q_TABLE) or (PLANNING and tabular.SHOW_Q_TABLE and trajectory != None and False):

        Q_table = trace   ## trace here is Q-table

        amp = learning.MAX_STEP
        color_wheel = initializee_color_wheel(amp)  ## max Q value is 1 for 1 reward

        for ss,action_value in Q_table.items():
            tile, head = ss

            if env.tilesummary[tile]['type'] != 'wall':
                x, z = env.tilesummary[tile]['pos']
                x = MAZE_HOR_OFFSET+x*ts
                z = MAZE_VER_OFFSET+z*ts

                for a, value in action_value.items():

                    value *= amp

                    cir = Turtle()
                    cir.hideturtle()
                    cir.penup()
                    if a == 'go_straight':
                        cir.shape("square")
                    else:
                        cir.shape("triangle")

                    if '3by3_' in env.MAP or '6by6_' in env.MAP:
                        cir.shapesize(0.25, 0.25, 0)
                        b = 20  # arrow from center
                        d = 8  # tilted arror from axis
                    elif '13by' in env.MAP or '12by' in env.MAP:
                        cir.shapesize(0.12, 0.12, 0)
                        b = 10  # arrow from center
                        d = 4  # tilted arror from axis
                    else: #if 'test' in env.MAP:
                        cir.shapesize(0.05, 0.05, 0)
                        b = 5  # arrow from center
                        d = 2  # tilted arror from axis

                    # if value != 0: print(value,len(color_wheel))
                    if value < 0:
                        value += len(color_wheel)
                        count = round(float(max(min(value,len(color_wheel)-1),0)))
                    else:
                        value /= 2
                        count = round(float(max(min(value,len(color_wheel)/2-1),0)))
                    # if value != 0:
                    #     print(count,len(color_wheel))
                    #     print()
                    cir.pencolor(color_wheel[count])
                    cir.fillcolor(color_wheel[count])

                    if head == 0:
                        if a == 'turn_right':
                            cir.left(-90) #(-30)
                            cir.goto(x + b, z - d)
                        elif a == 'go_straight':
                            cir.left(0)
                            cir.goto(x + b, z)
                        elif a == 'turn_left':
                            cir.left(90) #(30)
                            cir.goto(x + b, z + d)

                    elif head == 90:
                        if a == 'turn_right':
                            cir.left(0) #(60)
                            cir.goto(x + d, z + b)
                        elif a == 'go_straight':
                            cir.left(90)
                            cir.goto(x, z + b)
                        elif a == 'turn_left':
                            cir.left(180) #(120)
                            cir.goto(x - d, z + b)

                    elif head == 180:
                        if a == 'turn_right':
                            cir.left(90) #(150)
                            cir.goto(x - b, z + d)
                        elif a == 'go_straight':
                            cir.left(180)
                            cir.goto(x - b, z)
                        elif a == 'turn_left':
                            cir.left(270) #(210)
                            cir.goto(x - b, z - d)

                    elif head == 270:
                        if a == 'turn_right':
                            cir.left(180) #(240)
                            cir.goto(x - d, z - b)
                        elif a == 'go_straight':
                            cir.left(270)
                            cir.goto(x, z - b)
                        elif a == 'turn_left':
                            cir.left(0) #(300)
                            cir.goto(x + d, z - b)

                    cir.pendown()
                    cir.showturtle()
                    cir.stamp()
                    cir.penup()

        ## print my current avatar
        if LEARNING:
            s = s[0]
            x,y = env.tilesummary[s]['pos']
            agent.clear()
            agent.penup()
            agent.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
            agent.showturtle()
            agent.stamp()

    ## for rl simulations, show past trajectory as colored map - old state space
    elif LEARNING:

        # if learning.SHOW_Q_TABLE:
        #     VISIT_COUNT = trace
        # else:
        VISIT_COUNT[s][a] += 1
        VISIT_COUNT['count'] += 1

        s_last, a_last = sa_last

        ## change tile to air so we know it has been visited
        x_last, y_last = env.tilesummary[s_last[0]]['pos']
        x_last = MAZE_HOR_OFFSET+x_last*ts
        y_last = MAZE_VER_OFFSET+y_last*ts
        dc.shape(IMGS['air'])
        dc.goto(x_last,y_last)
        dc.showturtle()
        dc.stamp()

        ## update heat map of the grid

        cir = Turtle()
        cir.hideturtle()
        cir.penup()
        if a_last == 'go_straight':
            cir.shape("square")
        else:
            cir.shape("triangle")

        if '3by3_' in env.MAP or '6by6_' in env.MAP:
            cir.shapesize(0.25, 0.25, 0)
            b = 20  # arrow from center
            d = 8  # tilted arror from axis
        elif '13by' in env.MAP or '12by' in env.MAP:
            cir.shapesize(0.12, 0.12, 0)
            b = 10  # arrow from center
            d = 4  # tilted arror from axis
        else: #if 'test' in env.MAP:
            cir.shapesize(0.05, 0.05, 0)
            b = 5  # arrow from center
            d = 2  # tilted arror from axis

        cir.pencolor(get_color(sa_last))
        cir.fillcolor(get_color(sa_last))

        if USE_STATA:
            cir.shape("circle")
            cir.shapesize(0.2, 0.2, 0)
            cir.goto(x_last, y_last)
        else:
            if s_last[1] == 0:
                if a_last == 'turn_right':
                    cir.left(-90) #(-30)
                    cir.goto(x_last + b, y_last - d)
                elif a_last == 'go_straight':
                    cir.left(0)
                    cir.goto(x_last + b, y_last)
                elif a_last == 'turn_left':
                    cir.left(90) #(30)
                    cir.goto(x_last + b, y_last + d)

            elif s_last[1] == 90:
                if a_last == 'turn_right':
                    cir.left(0) #(60)
                    cir.goto(x_last + d, y_last + b)
                elif a_last == 'go_straight':
                    cir.left(90)
                    cir.goto(x_last, y_last + b)
                elif a_last == 'turn_left':
                    cir.left(180) #(120)
                    cir.goto(x_last - d, y_last + b)

            elif s_last[1] == 180:
                if a_last == 'turn_right':
                    cir.left(90) #(150)
                    cir.goto(x_last - b, y_last + d)
                elif a_last == 'go_straight':
                    cir.left(180)
                    cir.goto(x_last - b, y_last)
                elif a_last == 'turn_left':
                    cir.left(270) #(210)
                    cir.goto(x_last - b, y_last - d)

            elif s_last[1] == 270:
                if a_last == 'turn_right':
                    cir.left(180) #(240)
                    cir.goto(x_last - d, y_last - b)
                elif a_last == 'go_straight':
                    cir.left(270)
                    cir.goto(x_last, y_last - b)
                elif a_last == 'turn_left':
                    cir.left(0) #(300)
                    cir.goto(x_last + d, y_last - b)
        cir.pendown()
        cir.showturtle()
        cir.stamp()
        cir.penup()


        ## print my current avatar
        x,y = env.tilesummary[s[0]]['pos']
        agent.clear()
        agent.penup()
        agent.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
        agent.showturtle()
        agent.stamp()

    ## for inverse planning, show past trajectory as all avatars
    else:
        x,y = env.tilesummary[s[0]]['pos']
        agent.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
        agent.showturtle()
        agent.stamp()
        screen.update()

        ##  ------------- animation
        type = env.tilesummary[s[0]]['type']
        if type == 'gravel' or type == 'fire':
            time.sleep(0.5)
            dc.shape(IMGS[type].replace('TILE_SIZE/',str(TILE_SIZE)+'/obs-').replace('TILE_SIZE\\',str(TILE_SIZE)+'\\obs-'))
            dc.penup()
            dc.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
            dc.showturtle()
            dc.stamp()
            screen.update()

            time.sleep(0.5)
            agent.stamp()
            screen.update()

        if type == 'gravel':
            time.sleep(0.5)
            dc.stamp()
            screen.update()

            time.sleep(0.5)
            agent.stamp()
            screen.update()

    if PRINT_CONSOLE: print('finished printing raycasting in',str(time.time()-start),'seconds')

    ## for RL, show the entire trajectory along with Q-table or visit count
    if (LEARNING and learning.JUST_TRAJECTORY) or (PLANNING and tabular.JUST_TRAJECTORY and trajectory != None):

        tile_last = None

        dc = Turtle()
        dc.hideturtle()
        dc.speed(10)
        dc.pensize(2)
        dc.penup()

        length = len(trajectory)
        for index in range(len(trajectory)):
            (tile, head), action = trajectory[index]
            x, y = env.tilesummary[tile]['pos']
            dc.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
            dc.pendown()
            if index > 0:
                # dc.shape(IMGS['air'].replace('TILE_SIZE/',str(TILE_SIZE)+'/obs-').replace('TILE_SIZE\\',str(TILE_SIZE)+'\\obs-'))
                # dc.hideturtle()

                color = initializee_color_wheel(length,rainbow=True)[index]
                dc.pencolor(color)
                dc.fillcolor(color)
                dc.pendown()
                dc.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)

        if PLANNING:
            x,y = env.tilesummary[s[0]]['pos']
            agent.clear()
            agent.penup()
            agent.goto(MAZE_HOR_OFFSET+x*ts,MAZE_VER_OFFSET+y*ts)
            agent.showturtle()
            agent.stamp()

    # if DEBUG: print('___________ before updating screen in', round(time.time() - start, 3))
    start = time.time()
    screen.update()

    if DEBUG: print('___________ finished updating screen in', round(time.time() - start, 3))


def update_graph(img_png,screen,ver_shift=160, hor_shift=-260):
    img_gif = img_png.replace('png','gif')
    imageio.mimsave(img_gif, [imageio.imread(img_png)])
    smaller_plot = PhotoImage(file=img_gif)
    screen.addshape(img_png, Shape("image", smaller_plot))
    plotter = Turtle(img_png)
    plotter.hideturtle()
    plotter.speed(10)
    plotter.penup()
    plotter.goto(hor_shift, ver_shift)
    plotter.showturtle()
    plotter.stamp()

## ----------------------------------------
#    Logging related
## ----------------------------------------

def take_screenshot(env, screen, img_name, PNG=False, FINAL=False):
    """
        take a screenshot of the visualized screen with a name specified by img_name
        save the EPS or PNG into RECORDING_FOLDER
        PNG indicates whether to convert eps to png
        Final indicates whether it's the last image of the plan
    """

    if not img_name.endswith('_'): img_name += '_'

    ## save the screenshots at every step for inspecting the GIF or PNGs
    ## save as eps for later generating GIF directly without converting to PNG
    if EXPERIMENT_PARAM or (EXPERIMENT_REPLAY and REPLAY_TO_PNGs) or (EXPERIMENT_REPLAY and REPLAY_TO_GIF):
        recording_name = RECORDING_FOLDER + str(env.step)+'.eps'

    elif EXPERIMENT_REPLAY and REPLAY_TO_PNG:
        recording_name = RECORDING_FOLDER + img_name +'.eps'

    elif INVERSE_PLANNING and REPLAY_TO_PNG:
        recording_name = RECORDING_FOLDER + 'INV_' + img_name +'.eps'

    elif LEARNING:
        recording_name = img_name

    ## planning and inverse planning
    elif GENERATE_PNGs or GENERATE_GIF:
        recording_name = RECORDING_FOLDER + img_name + MAP.replace('.csv', '_'+PLAYER_NAME+'_'+str(env.step)+'.eps')
        if GENERATE_PNGs: PNG = True
        if GENERATE_GIF: PNG = False

    ## when taking the last screenshot
    elif GENERATE_PNG and FINAL:
        recording_name = RECORDING_FOLDER + img_name + MAP.replace('.csv', '_'+PLAYER_NAME+'_'+str(env.step) +'_' +get_time()+'.eps')
        PNG = True

    else:
        return

    print(recording_name)
    screen.getcanvas().postscript(file=recording_name)

    if PNG:
        im = Image.open(recording_name)
        fig = im.convert('RGBA')

        CROP_SIZE = get_CROP_SIZE(recording_name)
        if CROP_SIZE != None:
            fig = fig.crop(CROP_SIZE)

        png_name= recording_name.replace(".eps",".png")
        fig.save(png_name, lossless=True)
        im.close()
        os.remove(recording_name)

## crop the map part of the Turtle screen
def get_CROP_SIZE(file_name):
    if not CROP_PNG: return None
    CROP_SIZE = None
    if "12by12" in file_name:
        CROP_SIZE = (388, 68, 899-72, 600-90)
    elif "13by13" in file_name:
        CROP_SIZE = (402, 88, 899-57, 600-74)
    elif "24by24" in file_name or "test" in file_name or (EXPERIMENT_REPLAY and REPLAY_TO_PNGs):
        CROP_SIZE = (412, 107, 899-80, 600-80)
    return CROP_SIZE

def generate_GIF(duration, pre, file_name=None, PNG=True):

    if file_name == None: file_name = pre

    if PRINT_CONSOLE: print("... generating gif")

    gif_time = time.time()
    CROP_SIZE = get_CROP_SIZE(file_name)

    frames = []
    frame_first = None
    for index in range(duration+1):
        if PNG:
            screenshot_name = pre+str(index)+'.png'
        else:
            screenshot_name = pre+str(index)+'.eps'

        im = Image.open(screenshot_name)
        fig = im.convert('RGBA')

        if not PNG:
            if CROP_SIZE != None:
                fig = fig.crop(CROP_SIZE)
                fig = fig.resize((200,200))

        if frame_first == None:
            frame_first = fig
        else:
            frames.append(fig)

        if LEARNING:
            frames.append(fig)
            frames.append(fig)
            frames.append(fig)
            frames.append(fig)
            frames.append(fig)
            frames.append(fig)

        im.close()
        os.remove(screenshot_name)

    image_gif= file_name.replace(".json",".gif")
    if ".gif" not in image_gif: image_gif += ".gif"
    frame_first.save(image_gif, save_all=True, append_images=frames)

    #     image_png= screenshot_name.replace("eps","png")
    #     fig.save(image_png, lossless = False)
    #     os.remove(screenshot_name)

    # ## generate gif into RECORDING_FOLDER
    # images = []
    # for index in range(duration+1):
    #     filename = pre+str(index)+'.png'
    #     images.append(imageio.imread(filename))
    #     # images.append(imageio.imread(filename))
    #     os.remove(filename)
    # file_name = file_name.replace(".json",".gif")
    # if ".gif" not in file_name: file_name += ".gif"
    # imageio.mimsave(file_name, images)

    if PRINT_CONSOLE: print("... finished generating gif in", str(time.time() - gif_time), 'seconds', file_name)

def log(to_print, TXT):
    if GENERATE_TXT: TXT.write(to_print+'\n')
    if PRINT_CONSOLE: print(to_print)
    return TXT

def get_time(SECONDS = False):
    if SECONDS: return datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")
    return datetime.now().strftime("%m-%d-%H-%M-%S")

## ----------------------------------------
#    Planning related
## ----------------------------------------

def planning(env,main_loop=True):
    """ let agent plan in the gridworld """

    time_stamp = get_time()

    OUTPUT_NAME = MAP.replace('.csv', '_'+PLAYER_NAME) + '_'
    if GENERATE_TXT:
        TXT_name = join(TRAJECTORY_FOLDER, OUTPUT_NAME+time_stamp+'.txt').replace('_/','_')
    else:
        TXT_name = None

    s0 = env._pos_agent
    screen, ts, dc, agent = draw_maze(env)
    if PAUSE_AT_BEGINNING:
        time.sleep(7)

    ## -------------------------------
    ##   planning
    ## -------------------------------
    start = time.time()
    if HIERARCHICAL_PLANNING:
        episode = hierarchical.plan(env, agent, dc, screen, ts, s0, TXT_name)

        # env.change_player('hvi')
        # roomlevel_planner = planners.VI(env.roomlevel_states, env.roomlevel_actions, env.roomlevel_T, env.roomlevel_R,
        #                              max_num_steps=len(env.roomlevel_states), gamma=env.player["roomlevel_gamma"])
        # tilelevel_planner = planners.VI(env.states, env.actions, env.T, env.R,
        #                              max_num_steps=30, gamma=env.player["tilelevel_gamma"])
        # planner = planners.HierarchicalPlanner(roomlevel_planner, tilelevel_planner, env)
        # episode = planners.plan(env, agent, dc, screen, ts, s0, TXT_name, planner)

    elif PLANNING_BY_MCTS:

        planner = planners.UCT(env.actions, env.T, env.R, max_num_steps=MAX_ITER,
                               num_search_iters=1000, timeout=1, gamma=env.player["tilelevel_gamma"])
        episode = planners.plan(env, agent, dc, screen, ts, s0, TXT_name, planner)

    else:
        # env.change_player('uct')
        # planner = planners.UCT(env.actions, env.T, env.R, max_num_steps=MAX_ITER,
        #                        num_search_iters=1000, timeout=1, gamma=env.player["tilelevel_gamma"])
        # episode = planners.plan(env, agent, dc, screen, ts, s0, TXT_name, planner)

        # env.change_player('rtdp')
        # planner = planners.LRTDP(env.actions, env.T, env.R, num_simulations=1000,
        #                         max_num_steps=MAX_ITER, timeout=1, gamma=env.player["tilelevel_gamma"])
        # episode = planners.plan(env, agent, dc, screen, ts, s0, TXT_name, planner)

        # env.change_player('rtdp')
        # planner = planners.RTDP(env.actions, env.T, env.R, num_simulations=1000,
        #                         max_num_steps=MAX_ITER, timeout=5, gamma=env.player["tilelevel_gamma"])
        # episode = planners.plan(env, agent, dc, screen, ts, s0, TXT_name, planner)

        # env.change_player('vi')
        # planner = planners.VI(env.states, env.actions, env.T, env.R,
        #                      max_num_steps=MAX_ITER, gamma=env.player["tilelevel_gamma"])
        # episode = planners.plan(env, agent, dc, screen, ts, s0, TXT_name, planner)

        episode = tabular.plan(env, agent, dc, screen, ts, s0, TXT_name)


    if PRINT_CONSOLE: print('finished planning')

    ## -------------------------------
    ##   output a trajectory
    ## -------------------------------
    file_name = join(TRAJECTORY_FOLDER, OUTPUT_NAME+time_stamp+'.json')
    with open(file_name, 'w') as outfile:
        ep = {
            "map": MAP,
            'player_name': PLAYER_NAME,
            'player_profile': env.player,
            'max_iter': MAX_ITER,
            "time_to_complete": time.time() - start,
            "step_to_complete": env.step,
            "score_total": len(env.victim_summary.values()) - len(env.remaining_to_save.values()),
            "score_weighted": env.score_weighted
        }
        ep["steps"] = {}
        for index in range(len(episode)):
            sa = {}
            sa["tile"] = int(episode[index][0][0])
            sa["heading"] = int(episode[index][0][1])
            sa["action"] = episode[index][1]
            ep["steps"][index] = sa
        json.dump(ep, outfile)

    ## -------------------------------
    ##   output a gif
    ## -------------------------------

    if GENERATE_GIF:
        if EXPERIMENT_PARAM:
            pre = join(RECORDING_FOLDER, '')
        else:
            if HIERARCHICAL_PLANNING:
                pre = 'HIER_VI_'
            else:
                pre = 'VI_'
            pre = join(RECORDING_FOLDER, pre + OUTPUT_NAME)

        generate_GIF(env.step, pre, file_name)

    elif GENERATE_PNG:
        if HIERARCHICAL_PLANNING:
            pre = 'HIER_VI_'
        else:
            pre = 'VI_'

        take_screenshot(env, screen, pre, PNG=True)

    if not EXPERIMENT_PARAM:
        if main_loop:
            screen.mainloop()

def inverse_planning(trajectory, trajectory_file, temperature=TEMPERATURE, main_loop=True):
    """ evaluates human trajectory in the gridworld """

    players = player.players
    env = mdp.POMDP(MAP, MAP_ROOM, players[PLAYER_NAME])
    screen, ts, dc, agent = draw_maze(env)

    env = inverse.inverse_plan(screen, ts, dc, agent, trajectory, trajectory_file, MAP, temperature, main_loop=True)

    if REPLAY_TO_PNG:
        take_screenshot(env, screen, trajectory_file, PNG=True)
        screen.clear()

    elif REPLAY_TO_GIF: # EXPERIMENT_REPLAY and
        generate_GIF(len(trajectory), RECORDING_FOLDER, join(RECORDING_FOLDER, trajectory_file))

    else:
        if main_loop:
            screen.mainloop()

## for testing raycasting in mazes
def get_trajectory_test():
    k = 10
    get_trajectory_test = {}
    m = 0
    seq = list(range(0,360))
    seq.reverse()
    for i in seq:
        if i % k == 0:
            li = {}
            li['x'] = 9
            li['z'] = 19
            li['yaw'] = i
            li['cell'] = "(9,19)"
            get_trajectory_test[str(m)] = li
            m += 1

    # print(trajectory_test.keys())
    return get_trajectory_test

def repair_trajectory(trajectory, MAP, trajectory_file):
    LEARNING = True
    _, tile_indices, _, _, _, _ = mapreader.read_cvs(MAP, player.players[PLAYER_NAME]["rewards"], LEARNING=True)
    # trajectory = inverse.repair_trajectory(trajectory, MAP)
    episode = {}

    env = mdp.POMDP(MAP, MAP_ROOM, player.players[PLAYER_NAME])
    env.find_victims()

    count = 0
    for index in range(len(trajectory)-1):
        sar = []
        current = trajectory[str(index)]
        x = math.floor(current['x'])
        z = math.floor(current['z'])

        next = trajectory[str(index+1)]
        x_p = math.floor(next['x'])
        z_p = math.floor(next['z'])

        # print(x,z,'  ->  ',x_p,z_p)

        actions = []
        reward = 0
        if x_p > x:
            actions.append('right')
        if x_p < x:
            actions.append('left')
        if z_p > z:
            actions.append('down')
        if z_p < z:
            actions.append('up')
        if 'new_v' in next or 'new_vv' in next:
            reward = 1

        ## method 1
        # if len(actions) != 0:
        #     sar.append(tile_indices[(z,x)])
        #     sar.append((z,x))
        #     sar.append(random.choice(actions))
        #     sar.append(reward)
        #     episode[count] = sar
        #     count += 1
        # elif reward == 1 and sar[count-1][0] == tile_indices[(z,x)]:
        #     sar[count-1] = [sar[count-1][0],sar[count-1][1],reward]
        #     print(index, tile_indices[(z,x)])

        if len(actions) == 0 and reward:
            print(index, tile_indices[(z,x)])

        ## method 2
        if len(actions) == 0 and reward == 1:
            actions = ['random']
            # actions = ['up','down','left','right']

        if len(actions) != 0:
            sar.append(tile_indices[(z,x)])
            sar.append((z,x))
            sar.append(random.choice(actions))

            if reward: sar.append(1)
            else: sar.append(0)

            ## end
            episode[count] = sar
            count += 1

    trajectory_file = trajectory_file.replace('24by24','24by24_T')
    with open(trajectory_file, 'w') as fp:
        json.dump(episode, fp)


## ----------------------------------------
#   RITA related
## ----------------------------------------
rabbit = None
start_time = None
start_time_real = None
msg_index = 0
uid = 0

data_last = None
x_last = 0
z_last = 0
yaw_last = 0
s_last = (0,0)
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
xlabels = []
window_length = 40
lines = None
envs = None
likelihoods = None
Qs = None
PIs = None
Vs_room = None
Vs_tile = None
predictions = {"next_step":{}, "next_goal":{}, "next_room":{}}

RITA_OBSERVE = True
RITA_INFERENCE = True
RITA_PREDICT = True
RITA_EVALUATE = True

RECORD_TIME = True
LOG = True
TEST_DATASET = False
if TEST_DATASET:
    RECORD_TIME = False
    LOG = False
    USE_INTERFACE = False
    no_interface()

def start_rita(msg):
    pass

def shutdown_rita(msg):
    rabbit.done = True
    rabbit.close()

def state_changed(x, z, head, x_last, z_last, head_last):
    print(f'from ({x_last},{z_last})-{head_last} to ({x},{z})-{head}')
    if point_distance(x,z,x_last,z_last) > 2: return False
    return math.floor(x) != math.floor(x_last) or (math.floor(z) != math.floor(z_last)) or head != head_last

def point_distance(x1, y1, x2, y2):
    return math.sqrt((x1-x2)**2 + (y1-y2)**2)

def heading_to_goal(x, z, x_last, z_last, goal_x, goal_z):
    """ return true if current position is closer to the goal than that from last timestamp """

    ## do not count if the player didn't move or didn't move by himself
    moved_distance = point_distance(x, z, x_last, z_last)
    if moved_distance > 1 or moved_distance == 0: return None

    answer = point_distance(x, z, goal_x, goal_z) < point_distance(x_last, z_last, goal_x, goal_z)
    # print(f'................{answer} -  moved from ({x_last}, {z_last}) to ({x}, {z}), goal is ({goal_x}, {goal_z})')
    return answer


def get_thread_name():
    return threading.current_thread().name, threading.active_count()

def dispatch_fn(msg, routing_key):

    global start_time
    global start_time_real
    global msg_index

    before = time.time()

    if routing_key == 'startup-rita':
        pprint(msg)
        start_rita(msg)

    # elif routing_key == 'shutdown-rita':
    #     shutdown_rita(msg)

    elif routing_key == 'testbed-message' and 'testbed-message' in msg and 'header' in msg['testbed-message']:

        msg_index += 1
        # print() # msg['testbed-message']['header']['message_type']

        if msg['testbed-message']['header']['message_type'] != 'trial':

            sub_type = msg['testbed-message']['msg']['sub_type']

            ## find the mission START message
            if sub_type == 'Event:MissionState':
                # print('timestart?',)
                # msg['testbed-message']['data'] == {'mission': 'Falcon', 'mission_state': 'Start'}
                if msg['testbed-message']['data']['mission_state'].lower() == 'start':
                    start_time = format_time(msg['testbed-message']['msg']['timestamp'])
                    start_time_real = time.time()
                    print('timestart', start_time)

            elif 'data' in msg['testbed-message'] and start_time!=None: # and data_type=='state'
                data = msg['testbed-message']['data']
                if 'playername' in data.keys(): data['name'] = data['playername']
                timestamp = format_time(msg['testbed-message']['header']['timestamp'])
                timediff = timestamp - start_time
                data['countdown'] = round(600 - timediff.total_seconds(), 1)
                data['timestamp'] = timestamp

                msg = replay_with_tom(data, sub_type)
                if msg != None:
                    pprint(msg)
                    send_obs(msg)
                    now = time.time()
                    print('msg_index', msg_index, 'perf time (secs)', now - before, 'rate', 1/(now-before))
                    # print('only msg_index', msg_index)
                # else:
                #     print('rmq message is None', get_thread_name(), 'msg_index', msg_index)


def format_time(timestamp):
    if '.' in timestamp:
        return datetime.strptime(timestamp, '%Y-%m-%dT%H:%M:%S.%fZ')
    else:
        return datetime.strptime(timestamp, '%Y-%m-%dT%H:%M:%SZ')


def send_obs(msg):
    msg['timestamp'] = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")
    msg['routing-key'] = 'raycasting'
    msg['app-id'] = 'tom test app'
    msg['mission-id'] = 'mission id - 1'
    # print(msg['timestamp'])
    # pprint(msg)
    # print('send_obs', get_thread_name(), 'msg_index', msg_index)
    rabbit.send_message_now(msg, 'raycasting')

def test_rita_rmq():
    # Trivial function to setup subscriptions and publish messages
    global rabbit

    init_replay_with_tom()

    rabbit = rmq.Rmq('rita', host=args.host, port=args.port)
    rabbit.subscribe(['startup-rita', 'shutdown-rita', 'testbed-message'])
    # send_messages()
    rabbit.wait_for_messages(dispatch_fn)  # Blocking function call
    # When we are done
    rabbit.done = True
    rabbit.close()
    print('Done')

def init_replay_with_tom(duration=7000, trial_id=None):

    global env, envs
    global dc, agent, screen
    global data_last, x_last, z_last, yaw_last, s_last
    global countdown_last, action_event, real_pos_last, last_obs
    global note, note_event, note_time, note_debug
    global current_room, next_room
    global V1, V2
    global titles, writers, label
    global xdata, ydata, xlabels
    global lines, predictions
    global likelihoods, Qs, PIs, Vs_room, Vs_tile

    ## similar process as REPLY_IN_RITA
    env = mdp.POMDP(MAP, MAP_ROOM, player.players[PLAYER_NAME])
    if screen != None:  screen.clearscreen()
    screen, ts, dc, agent = draw_maze(env)

    ## interface related
    data_last = None
    x_last = 0
    z_last = 0
    yaw_last = 0
    s_last = (0, 0)
    countdown_last = 600
    action_event = None
    note = None
    note_event = None
    note_time = None
    note_debug = None
    real_pos_last = None
    last_obs = None
    current_room = None
    next_room = None

    ## hierarchical planning
    V1 = np.zeros((len(env.roomlevel_states),))  ## use numpy
    V2 = {s: 0 for s in env.states}

    # titles, writers, label, fig, ax, xdata, ydata, lines = inverse.draw_inv_planning_window(trial_id)
    # if writers != None:
    #     for writer in writers: writer.clear()
    # if titles != None:  titles.clear()
    # if label != None:  label.clear()

    titles, writers, label = inverse.draw_inv_planning_window(trial_id)
    xdata = np.linspace(0, duration - 1, duration)
    ydata = {}
    lines = []
    for player_type in PLAYERS:
        ydata[player_type] = np.ones(duration) /len(PLAYERS)
    predictions = {"next_step": {}, "next_goal": {}, "next_room": {}}

    if len(xlabels) != duration:
        for i in range(duration): xlabels.append('')

    #  Initialize planning agents
    envs = {}  ## environment of different player types
    likelihoods = {}  ## dictionary of list of likelihoods, used to calculate the product of likelihoods over a time window
    Qs = {}
    PIs = {}
    Vs_room = {}  ## for reusing the converged V from the last step by different agent players
    Vs_tile = {}  ## for reusing the converged V from the last step by different agent players

    for player_type in PLAYERS:

        ## initiate player
        env1 = mdp.POMDP(MAP, MAP_ROOM, player.players[player_type],
                         player_name=player_type, obs_rewards=env.obs_rewards)
        envs[player_type] = env1
        likelihoods[player_type] = [0.5] * WINDOW_LENGTH

        ## initialize value functions for VI
        if INVERSE_PLANNING_HVI or REPLAY_IN_RITA or REPLAY_WITH_TOM:
            Vs_room[player_type] = np.zeros((len(env1.roomlevel_states),))
            Vs_tile[player_type] = np.zeros((env1.ntiles, 4))
        else:
            print('whats happening')

    if RECORD_TIME:
        with open(join('plots','times.csv'), mode='w') as times_file:
            times_writer = csv.writer(times_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
            times_writer.writerow(['countdown', 'observe', 'maze', 'rewards', 'inference', 'plot', 'prediction', 'evaluation'])

def init_replay_map(world_name):

    global MAP, ranges, room_snap, blocks_in_world, room_names
    global MAP_ROOM, WORLD_WIDTH, WORLD_HEIGHT, TILE_SIZE, MAX_ITER

    world_name = world_name.lower()
    if world_name == 'singleplayer':
        world_name = 'sparky'
    elif 'falcon' in world_name or world_name == 'not set':
        world_name = 'falcon'

    rita_configs = {  ## MAP, ranges, json_folder
        'sparky': ('46by45_2.csv', (-2153, -2108, 153, 207, 52, 54), join('maps', '46by45_2_rooms_colored.gif')),
        'falcon': ('48by89.csv', (-2108, -2020, 144, 191, 60, 62), None)
    }

    MAP, ranges, room_snap = rita_configs[world_name]
    MAP_ROOM, WORLD_WIDTH, WORLD_HEIGHT, TILE_SIZE, MAX_ITER = MAP_CONFIG[MAP]

    with open(join('configs', world_name + '.json')) as json_file:
        blocks_in_world = json.load(json_file)
        if 'blocks' in blocks_in_world: blocks_in_world = blocks_in_world['blocks']

    room_names = {
        0: 'Entrance Lobby',
        1: 'Left Hallway',
        2: 'Security Office',
        3: 'The Computer Farm',
        4: 'Left Hallway',
        5: 'Open Break Area',
        6: 'Executive Suite 1',
        7: 'Center Hallway Bottom',
        8: 'Left Hallway',
        9: 'Center Hallway Bottom',
        10: 'Janitor',
        11: 'Left Hallway',
        12: 'Left Hallway',
        13: 'Executive Suite 2',
        14: "King Chris's Office",
        15: "The King's Terrace",
        16: 'Center Hallway Middle',
        17: 'Center Hallway Middle',
        18: 'Herbalife Conference Room',
        19: 'Center Hallway Top',
        20: 'Center Hallway Top',
        21: 'Room 101',
        22: 'Room 102',
        23: 'Room 103',
        24: 'Room 104',
        25: 'Room 105',
        26: 'Room 106',
        27: 'Room 107',
        28: 'Room 108',
        29: 'Room 109',
        30: 'Room 110',
        31: 'Room 111',
        32: 'Right Hallway',
        33: 'Right Hallway',
        34: 'Right Hallway',
        35: 'Right Hallway',
        36: 'Right Hallway',
        37: 'Right Hallway',
        38: 'Right Hallway',
        39: 'Amway Conference Room',
        40: 'Mary Kay Conference Room',
        41: "Women's Room",
        42: "Men's Room",
    }

def print_event(*args):

    if not USE_INTERFACE: return

    global note_event

    statement = ' '.join([str(item) for item in args] )
    if LOG: print('--------------------', statement)

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

def print_time(data, extra=None):
    global note_time
    global yellow_dead

    ## victim turn red at countdown 300
    if round(data['countdown']) <= 300 and not yellow_dead:
        yellow_dead = True

        yellow_pos = copy.deepcopy(env.remaining_to_save['VV'])
        env.remaining_to_save['VV'] = []

        for index in yellow_pos:

            ## change the true and believed environment
            env.tilesummary[index]['reward'] = 0
            env.tilesummary[index]['type'] = 'air'
            env.tilesummary_truth[index]['reward'] = 0
            env.tilesummary_truth[index]['type'] = 'air'

        if not USE_INTERFACE: return

        img_name = join('texture', str(TILE_SIZE), 'obs-victim-red.gif')
        dc.shape(img_name)
        dc.speed(10)
        for index in yellow_pos:
            x, y = env.tilesummary_truth[index]['pos']
            dc.penup()
            dc.goto(MAZE_HOR_OFFSET + x * ts, MAZE_VER_OFFSET + y * ts)
            dc.stamp()

    if not USE_INTERFACE: return

    ## preparing to print
    if note_time == None:
        note_time = Turtle()
        note_time.hideturtle()
        note_time.up()
        note_time.goto(-330, -250)
    else:
        note_time.clear()

    note_time.write(str(data['timestamp']) + '     ' + str(data['countdown']), font=("Courier", 14, 'normal'))

    # if extra != None:
    #     note_time.goto(-450, -290)
    #     note_time.write(extra, font=("Courier", 14, 'normal'))
    # screen.update()

def make_accuracy_plot(data, turt=note, verbose=False):
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

    if USE_INTERFACE:
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

        plot_file = join(PLOT_FOLDER, 'accuracy_plot.png')
        gif_file = plot_file.replace('png', 'gif')
        plt.savefig(plot_file, dpi=100, bbox_inches='tight')
        plt.clf()
        plt.close()

        imageio.mimsave(gif_file, [imageio.imread(plot_file)])
        smaller_plot = PhotoImage(file=gif_file)  # .subsample(3, 3)
        screen.addshape("accuracy_plot", Shape("image", smaller_plot))

        turt.shape("accuracy_plot")
        turt.hideturtle()
        turt.speed(10)
        turt.penup()
        turt.goto(-630, 210)
        turt.showturtle()
        turt.stamp()

    return output_accuracy

def make_hypothesis(action, object, probability, reason):

    hypothesis = {
        "hypothesis-rank": 0,  # #rank of the hypothesis that is generating the prediction
        "hypothesis-id": "hyp000Z",  ## uid of the hypothesis

        "uid": "se13519",  ## uid of the prediction
        "state": "unknown",  ## initially "unknown" then one of "true" or "false"
        "bounds": [0, 5],  ## time bounds for the prediction
        "subject": data['name'],
        "using": "",

        ## all predictions have a subject, the actor, and action, that is predicted, and the object upon which the action is performed
        "action": action,  ## the action refers to the method being invoked (Pamela method).
        "object": object,
        "reason": reason,  ## why the rita-agent is predicting this action
        "agent-belief": probability,  ## agent's belief in the prediction
    }
    return hypothesis

def replay_with_tom(data, sub_type='state'):

    ## for raycasting
    global data_last
    global x_last
    global z_last
    global yaw_last
    global s_last
    global countdown_last
    global real_pos_last
    global action_event
    global env
    global blocks_in_world

    ## for inference
    global titles
    global writers
    global label
    global xdata
    global ydata
    global yvalue
    global xlabels
    global lines
    global envs
    global likelihoods
    global Qs
    global PIs
    global Vs_room
    global Vs_tile
    global trace
    global predictions

    ## for predicting
    global note
    global note_debug
    global last_obs
    global current_room
    global next_room
    global V1
    global V2
    global pi
    global pi2

    x_low, x_high, z_low, z_high, y_low, y_high = ranges
    DEBUG = False

    ## "observations/events/player/door"
    if sub_type == 'Event:Door': #'open' in data.keys():
        print_time(data)
        location = (data['door_z']-z_low, data['door_x']-x_low)
        print_event('door opened!', location)

    ## "observations/events/player/location"
    elif sub_type == 'Event:location': #'entered_area_name' in data.keys():
        print_time(data)
        if 'exited_area_id' in data:
            print_event('changed area!', '('+data['exited_area_id']+')', data['exited_area_name'],  ' ->  ('+data['entered_area_id']+')', data['entered_area_name'])
        elif 'entered_area_id' in data:
            print_event('changed area! ->  (' + data['entered_area_id'] + ')', data['entered_area_name'])

    ## "observations/events/player/woof"
    elif sub_type == 'Event:Beep': #'source_entity' in data.keys():
        print_time(data)
        if 'woof_z' in data:
            location = (data['woof_z']-z_low, data['woof_x']-x_low)
        elif 'beep_z' in data:
            location = (data['beep_z']-z_low, data['beep_x']-x_low)

        print_event('device used!', location, data['message'])

    ## "observations/events/scoreboard"
    elif sub_type == 'Event:Scoreboard': #'scoreboard' in data.keys():
        print_time(data)
        print_event('score updated!', data['scoreboard'])

    ## "observations/events/lever"
    elif sub_type == 'Event:Lever': #'powered' in data.keys():
        print_time(data)
        print_event('lever powered!', data['lever_x'], data['lever_y'], data['lever_z'])

    ## "observations/state"
    elif sub_type == 'state' or sub_type == 'Event:Triage': #'Player' in data['name'] or data['name'] in ['K_Fuse', 'ASU_MC']:

        action_event = None
        ## "observations/events/player/triage"
        if sub_type == 'Event:Triage':  # 'triage_state' in data.keys():
            print_time(data)
            location = (int(data['victim_z'] - z_low), int(data['victim_x'] - x_low))
            if location not in env.tile_indices:
                print(f'???????? victim {location} not found on map')
                return
            tile_index = env.tile_indices[location]
            tile = env.tilesummary[tile_index]
            print_event('triage', data['triage_state'], location, tile['type'])

            ## victim turn white
            if data['triage_state'] == 'SUCCESSFUL':

                ## update the graphics through env
                if tile['type'] == 'victim-yellow':
                    env.score += 30
                    env.remaining_to_save['VV'].remove(tile_index)
                elif tile['type'] == 'victim':
                    env.score += 10
                    env.remaining_to_save['V'].remove(tile_index)

                ## assume the agent has been to the tile of the victim to pick up the rewards
                for env1 in envs.values():
                    env1._pos_agent = (tile_index, env1._pos_agent[1])
                    env1.collect_reward()

                if USE_INTERFACE:
                    x, y = tile['pos']
                    img_name = join('texture', str(TILE_SIZE), 'open-' + str(tile['type']) + '.gif')
                    dc.shape(img_name)
                    dc.penup()
                    dc.goto(MAZE_HOR_OFFSET + x * TILE_SIZE, MAZE_VER_OFFSET + y * TILE_SIZE)
                    dc.stamp()
                    screen.update()

                return

            elif data['triage_state'] == 'IN_PROGRESS':

                action_event = 'triage'

            countdown = data['countdown']
            data = data_last
            data['countdown'] = countdown

        start = time.time()
        time_start = start
        time_received = get_time(SECONDS=True)
        time_1, time_2, time_3, time_4, time_5, time_6, time_7 = 'N/A', 'N/A', 'N/A', 'N/A', 'N/A', 'N/A', 'N/A'

        x = data['x']
        z = data['z']
        y = data['y']
        yaw = data['yaw']
        head = inverse.get_heading(yaw)
        head_last = inverse.get_heading(yaw_last)
        countdown = data['countdown']

        # print(ranges, state_changed(x, z, head, x_last, z_last, head_last))
        if (x>x_low and x<x_high and z>z_low and z<z_high and y>=y_low \
                and state_changed(x, z, head, x_last, z_last, head_last)) \
                or action_event !=None:
            data_last = data

            ## ----------------------
            ##  raycasting - takes around 0.1 sec
            ## ----------------------
            xlabels[env.step] = countdown

            start0 = time.time()
            msg = {}  ## message to be sent out

            ## continuous location on map
            x -= x_low
            z -= z_low
            real_pos = mapreader.coord((x, z))

            ## descretized for raycasting and inference
            x = math.floor(x)
            z = math.floor(z)
            x, z = inverse.correct_xy(env, x, z)
            if (int(z), int(x)) not in env.tile_indices:
                return
            tile = env.tile_indices[(int(z), int(x))]
            if env.tiles2room[tile] == None: return ## somehow the position is not in any of the rooms
            trace.append(tile)
            s = (tile, head)
            last_room = env.tiles2room[env._pos_agent[0]]
            env._pos_agent = s
            current_room = env.tiles2room[s[0]]

            ## discretize actions
            x = data['x']
            z = data['z']
            if action_event == None:
                thre = 0.25
                yaw_thre = 30
                if (head == 0 and x - x_last > thre) or (
                        head == 90 and z_last - z > thre) or (
                        head == 180 and x_last - x > thre) or (
                        head == 270 and z - z_last > thre):
                    act = 'go_straight'
                # elif head_last == head - 90 or head_last == head + 270:
                elif yaw_last - yaw > yaw_thre:
                    act = 'turn_left'
                # elif head_last != head:
                elif yaw - yaw_last > yaw_thre:
                    act = 'turn_right'
                else:
                    act = 'go_straight'
            ## action of triaging or turning on lights
            else:
                act = action_event
                env.decide = True
                env.decide_delay = 3

            location = (round(data['x'], 1), round(data['z'], 1))
            stats = 'Pos = ' + str(location) + '  Head = ' + str(head) + '  Act = ' + str(act)
            print_time(data, extra=stats)  ## takes 0.05 sec at the beginning and increases over time

            playback_speed = round((600 - countdown) / (time.time() - start_time_real), 2)
            player_speed = round(point_distance(x,z,x_last,z_last)/0.2, 3)
            if LOG:
                print(f"------ countdown = {countdown}  |  playback speed = {playback_speed}")
                print(f'       Human data: s = {s}, a = {act}  |  Human position: ',
                      (round(x_last, 1), round(z_last, 1)), head_last, ' -> ', (round(x, 1), round(z, 1)), head)

            ## the most time consuming command in part 1 - observation
            ## get raycasting results to be sent through message bus
            new_obs_tiles = mapreader.print_shadow(env, s, angle=inverse.get_angle(yaw))
            new_obs_blocks = {}
            for obs_tile in new_obs_tiles:
                x1 = env.tilesummary[obs_tile]['col'] + x_low
                z1 = env.tilesummary[obs_tile]['row'] + z_low
                for y1 in range(y_low, y_high + 1):
                    key = str(x1) + ',' + str(y1) + ',' + str(z1)
                    new_obs_blocks[key] = blocks_in_world[key]

                block_type = env.tilesummary_truth[obs_tile]
                if block_type in ['door', 'victim', 'victim-yellow']:
                    env.decide = True
                    env.decide_delay = 3
                    for en in envs:
                        en.decide = True
                        en.decide_delay = 3
            # print(new_obs_blocks)

            time_1 = round(time.time() - start, 3)
            if DEBUG: print(f'1 finished calculating raycasting in {time_1} seconds')
            start = time.time()

            ## ----------------------
            ##  updating in maze - share information across environments
            ## ----------------------
            env.collect_reward()  ## takes no time
            unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = env.observe(new_obs_tiles, s[0])
            print(countdown, new_obs_tiles, tiles_to_color)
            update_maze(env, agent, dc, screen, TILE_SIZE, s, trace, real_pos=real_pos, sa_last=real_pos_last,
                        tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change)
            real_pos_last = real_pos

            time_2 = round(time.time() - start, 3)
            if DEBUG: print(f'2 finished updating maze in {time_2} seconds')
            start = time.time()

            ## ----------------------
            ##  inference
            ## ----------------------
            if RITA_INFERENCE:

                if USE_INTERFACE:
                    if note_debug == None:
                        note_debug = Turtle()
                        note_debug.hideturtle()
                        note_debug.up()
                    else:
                        note_debug.clear()

                if env.replan: ## env.decide or env.step == 1: ## use each player's temperature
                    if env.tiles2room[s_last[0]] == None: s_last = s
                    PIs, Qs, Vs_room, Vs_tile, likelihoods = inverse.estimate_Qs_PIs(envs, s_last, act,
                                                             likelihoods, PIs, Qs, Vs_room, Vs_tile, None,
                                                             env.step, note_debug, screen, skipped=(not env.decide))
                    time_4 = round(time.time() - start, 3)
                    if DEBUG: print(f'4 finished calculating inference in {time_4} seconds')
                    start = time.time()

                most_likely, ydata = inverse.update_inv_planning_window(screen, label, envs, writers, PIs, Qs, likelihoods,
                                     xdata, ydata, lines, env.step-1, window_length=WINDOW_LENGTH, xlabels=xlabels)

                player_types = {}
                for type in PLAYERS:
                    player_types[type] = round(ydata[type][env.step], 3)

                time_5 = round(time.time() - start, 3)
                if DEBUG: print(f'5 finished updating inverse window in {time_5} seconds')
                start = time.time()


            ## ----------------------
            ##  updating all environments - takes no time
            ## ----------------------
            if RITA_OBSERVE:
                for player in PLAYERS:
                    env1 = envs[player]
                    env1._pos_agent = s
                    env1.collect_reward()
                    env1.observe(new_obs_tiles, s[0], unobserved_in_rooms=unobserved_in_rooms, obs_rewards=obs_rewards)

            time_3 = round(time.time() - start, 3)
            if DEBUG: print(f'3 finished observing in {time_3} seconds')
            start = time.time()


            ## ----------------------
            ##  predictions
            ## ----------------------
            if RITA_PREDICT:

                env1 = envs[most_likely]  ## make no modifications
                (pi1, Qsa1, roomlevel_V, V1), (pi2, Qsa2, V2) = hierarchical.policy_and_value(env1, Vs_room[most_likely], Vs_tile[most_likely], s)
                # V1, pi2, Qsa2, V2 = Vs_room[most_likely], PIs[most_likely], Qs[most_likely], Vs_tile[most_likely]
                # roomlevel_V = hierarchical.V1_to_roomlevelV(env1, V1)[0]
                # pi1, Qsa1 = hierarchical.extract_Qsa_pi_from_V1(env1, V1)

                ############################# next room ###############################
                next_room = np.argmax(pi1[current_room])
                next_rooms = {}
                for room in list(np.nonzero(pi1[current_room])[0]):
                    next_rooms[room_names[room]] = pi1[current_room][room]

                ############################# next action + explanation ###############################
                acts = np.round(pi2[(s[0], s[1] // 90)], 2)
                next_actions = {}
                for index in range(len(env.actions)):
                    prob = acts[index]
                    name = f'{env.actions[index]} ({prob})'
                    next_actions[name] = prob
                # next_actions = {
                #     'go_straight ('+str(acts[0])+')' : acts[0],
                #     'turn_left ('+str(acts[1])+')' : acts[1],
                #     'turn_right ('+str(acts[2])+')' : acts[2],
                #     'triage ('+str(acts[3])+')' : acts[3]
                # }

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

                next_acts = first + " | " + second + " | "
                next_actions_temp = copy.deepcopy(next_actions)
                next_actions_temp.pop(first)
                next_actions_temp.pop(second)
                next_acts += str(list(next_actions_temp.keys())[0])

                ## make it again for msg
                next_actions_temp = copy.deepcopy(next_actions)
                next_actions = {}
                for action, prob in next_actions_temp.items():
                    action = action.replace(str(prob),'').replace(' ()','')
                    next_actions[action] = prob

                ############################# next goal + type + explanation ###############################
                max_tile = env1._max_tile
                max_tile_type = env1.tilesummary[max_tile]['type']
                max_tile_pos = None
                if max_tile != -1:
                    x = env1.tilesummary[max_tile]['row'] + x_low
                    z = env1.tilesummary[max_tile]['col'] + z_low
                    max_tile_pos = (x, z)

                max_tile_exp = ''
                if 'victim' in max_tile_type:
                    max_tile_exp = f'the player wants to triage the {max_tile_type}'
                elif max_tile_type == 'door':
                    max_tile_exp = env.get_door_explanation(current_room, max_tile, room_names)
                elif max_tile_type == 'air':
                    blocks_count = len(env.obs_rewards[(max_tile, head)])
                    max_tile_exp = f'the player wants to observe {blocks_count} grids in {room_names[current_room]}'
                # -----------------------------------------------------------------------------------------#

                prediction_room = 'next_room = ' + str(next_room) + '   next_step = ' + str(first)
                prediction_goal = 'next_goal = ' + str(max_tile_pos) + '   next_goal_type = ' + str(max_tile_type)

                ## print stats and predictions
                if USE_INTERFACE:
                    if note == None:
                        note = Turtle()
                        note.hideturtle()
                        note.up()
                    else:
                        note.clear()

                    note.goto(-20, -270)
                    note.write(prediction_room, font=("Courier", 14, 'normal'))

                    note.goto(-20, -290)
                    note.write(prediction_goal, font=("Courier", 14, 'normal'))

                    # ## print the player profile
                    # note.goto(-750, 115)
                    # note.write(pformat(env1.player, indent=1), font=("Courier", 12, 'normal'))

                time_6 = round(time.time() - start, 3)
                if DEBUG: print(f'6 finished packaging prediction in {time_6} seconds')
                start = time.time()

                if RITA_EVALUATE:
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

                            ## check with corresponding actual value of predicted items
                            if item == 'next_step':
                                # print(f'....... predicted next step = {last_prediction}, actual = {act}')
                                if last_prediction == act:
                                    results.append(1)
                                else:
                                    results.append(0)

                            if item == 'next_goal':
                                # print(f'....... predicted next goal = {last_prediction}')
                                goal_tile = env.tilesummary[last_prediction]
                                answer = heading_to_goal(data['x']-x_low, data['z']-z_low, x_last-x_low, z_last-z_low,
                                                   goal_tile['col'], goal_tile['row'])
                                if answer != None:
                                    if answer:
                                        results.append(1)
                                    else:
                                        results.append(0)

                            if item == 'next_room' and current_room != last_room:
                                for predicted in last_prediction:
                                    if predicted == current_room:
                                        results.append(1)
                                    else:
                                        results.append(0)
                                predictions['next_room']['last'] = []

                    predictions['next_step']['last'] = first[:first.index(' ')]
                    predictions['next_goal']['last'] = max_tile
                    predictions['next_room']['last'].append(next_room)

                    output_accuracy = make_accuracy_plot(predictions, turt=note)
                    msg['accuracy'] = output_accuracy

                    time_7 = round(time.time() - start, 3)
                    if DEBUG: print(f'7 finished updating evaluation plot {time_7} seconds')

                #############################################################################
                #
                #    report inference, predictions, explanations, and evaluations
                #
                #############################################################################

                msg['time'] = {
                    'countdown': countdown,
                    'last_countdown': countdown_last,
                    'time_received': time_received,
                    'time_sent': get_time(SECONDS=True),
                    'process_time': round(time.time() - time_start),
                    'playback_speed': playback_speed
                }
                msg['player_state'] = {
                    'location_tile': (round(x, 1), round(z, 1)),
                    'location_area': room_names[current_room],
                    'heading': ['east', 'north', 'west', 'south'][head//90],
                    'speed': f'{player_speed} blocks/sec',
                    'visible_blocks': f'{str(len(new_obs_blocks))} blocks',  ## prevent it from printing too much
                    'score': env.score
                }
                msg['player_type'] = {
                    'top': most_likely,
                    'confidence': player_types
                }
                msg['next_step'] = {
                    'top': first[:first.index('(')-1],
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

                if LOG:
                    pprint(msg, width=250)
                    print()
                else:
                    global PRINT_COUNT
                    print(countdown, playback_speed)
                    PRINT_COUNT -= 1

                if REPLAY_IN_RITA:
                    ## using Paul's format  make_hypothesis(action, object, probability, reason)

                    msg['hypothesis_room'] = make_hypothesis("go-to-room", room_names[int(next_room)], 1, most_likely)
                    msg['hypothesis_action'] = make_hypothesis("take-action", msg['next_step']['top'], 1, most_likely)
                    msg['hypothesis_goal'] = make_hypothesis("go-to-object", (max_tile_pos, max_tile_type), 1, most_likely)
                    # msg['player_state']['visible_blocks'] = new_obs_blocks
                    return msg

            s_last = s
            countdown_last = countdown

            ## write all time costs to a csv file
            if RECORD_TIME:
                with open(join('plots','times.csv'), mode='a') as times_file:
                    times_writer = csv.writer(times_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
                    times_writer.writerow([countdown, time_1, time_2, time_3, time_4, time_5, time_6, time_7])

        x_last = data['x']
        z_last = data['z']
        yaw_last = data['yaw']

        ## add tile to list of visited tiles of all environments
        x = math.floor(data['x'] - x_low)
        z = math.floor(data['z'] - z_low)
        x, z = inverse.correct_xy(env, x, z)
        if (int(z), int(x)) in env.tile_indices:
            tile = env.tile_indices[(int(z), int(x))]
            if tile not in env.visited_tiles:
                for player in PLAYERS:
                    envs[player].visited_tiles.append(tile)
        x_last = data['x']
        z_last = data['z']
        return None


def json_to_dict(row, LOG=True):
    topics = ["observations/state", "observations/events/player/door", "observations/events/player/triage",
              "observations/events/player/beep", "observations/events/scoreboard", "observations/events/mission",
              "observations/events/player/woof", "observations/events/player/location"]
    data = row.replace('\n', '')
    if LOG:
        data = '{'+data.split(', {')[-1]
        if len(data.replace('{','')) != len(data.replace('}','')): return None
    data = json.loads(data)
    if 'testbed-message' in data: data = data['testbed-message']
    if ("topic" in data and data["topic"] in topics) or "data" in data:
        if '@timestamp' in data: ## in .json files
            data['timestamp'] = format_time(data['@timestamp'])
        else: ## in .log files
            data['timestamp'] = format_time(data['header']['timestamp'])
        data['data']['timestamp'] = data['timestamp']
        return data
    return None


## ----------------------------------------
#    Main function for different experiments
## ----------------------------------------
if __name__ == '__main__':

    ## receriving real-time messages from RITA test-bed
    if REPLAY_IN_RITA:
        env = mdp.POMDP(MAP, MAP_ROOM, player.players[PLAYER_NAME])
        screen, ts, dc, agent = draw_maze(env)
        init_replay_map('falcon')
        test_rita_rmq()

    ## compare reinforcement learning algorithms
    elif LEARNING:
        learning.reinforcement_learning()

    ## conduct experiments on parameters
    elif PRINT_MAP or EXPERIMENT_PARAM:

        skip_files = ['R5A-V10-P6","R5A-V10-P10',
            'R5A-V2-P10","R5A-V2-P6', 'R5A-V4-P10', 'R5A-V4-P6']

        ## get all mazes
        player = player.players[PLAYER_NAME]
        mypath = join("maps","12by12")
        files = [f for f in listdir(mypath) if isfile(join(mypath, f)) and '-V' in str(f)]
        files.sort()
        TRAJECTORY_FOLDER_ORIGINAL = TRAJECTORY_FOLDER

        for file in files:
            MAP = file
            print(MAP)
            MAP_ROOM = MAP[:11]+'rooms.csv'  ## '12by12_rooms.csv'

            skip = False
            for ff in skip_files:
                if ff in file:
                    skip = True

            if PRINT_MAP:
                ## new MDP agent with random initialized starting state
                env = mdp.POMDP(join('12by12',MAP), join('12by12',MAP_ROOM), player)
                env.random_initialize()
                screen, ts, dc, agent = draw_maze(env)
                take_screenshot(env, screen, 'MAP_')
                screen.reset()

            elif EXPERIMENT_PARAM and not skip:
                for exp in [0, 0.001, 0.01, 0.025]:
                    TRAJECTORY_FOLDER = join(TRAJECTORY_FOLDER, '') + "exp=" + str(exp) + "_"
                    print(TRAJECTORY_FOLDER)
                    player["exploration_reward"] = exp
                    env = mdp.POMDP(join('12by12',MAP), join('12by12', MAP_ROOM), player)
                    planning(env)
                    TRAJECTORY_FOLDER = TRAJECTORY_FOLDER_ORIGINAL

            print()

    ## POMDP Planning
    elif PLANNING:
        env = mdp.POMDP(MAP, MAP_ROOM, player.players[PLAYER_NAME], PLAYER_NAME)
        planning(env)

    ## replay human trajectory
    elif EXPERIMENT_REPLAY:

        global PRINT_COUNT

        mypath = None
        if args.file != '':
            files = [args.file]
        else:
            if args.directory != '':
                TRAJECTORY_FOLDER = args.directory

            if TRAJECTORY_FILE != None:  ## for replaying one file using RIPLAY_WITH_TOM
                files = [TRAJECTORY_FILE]
            else:
                mypath = TRAJECTORY_FOLDER
                files = [f for f in listdir(mypath) if
                         isfile(join(mypath, f)) and ('.log' in f or '.json' in f)]

                ## in testing, skip certain files
                new_files = []
                for f in files:
                    FOUND = False
                    for keyword in []: ##'ASIST_data', '13_mes', '14_mes', '10_mes', '06_mes', '01_mes', '05_mes', '08_mes', '02_mes', '03_mes',  'ASIST_data', '2020-2', 'Vers-1.0.metadata'
                        if keyword in f: FOUND = True
                    if not FOUND: new_files.append(f)
                files = new_files
                files.sort()

        print(f'going to read {len(files)} files')
        for file in files:
            if mypath:
                trajectory_file = join(mypath, file)
            else:
                trajectory_file = file

            if REPLAY_WITH_TOM:  ## testbed data format

                FOUND_STARTED = False
                f = open(trajectory_file, "r")
                rows = f.readlines()
                json_data = {}
                for row in rows:
                    data = json_to_dict(row, LOG='.log' in file)
                    if data != None:
                        json_data[data['timestamp']] = data
                        if 'mission_state' in data['data']:
                            FOUND_STARTED = True

                rows = []
                STARTED = False
                sorted_data = sorted(json_data)
                for timestamp in sorted_data:

                    if 'mission_state' in json_data[timestamp]['data'] or not FOUND_STARTED:
                        STARTED = True
                        start_time = timestamp
                        name = 'sparky'
                        if 'mission' in json_data[timestamp]['data']:
                            name = json_data[timestamp]['data']['mission']
                        elif 'NotHSR' in trajectory_file:
                            name = 'falcon'
                        init_replay_map(name)
                        FOUND_STARTED = True
                    else:
                        if STARTED:
                            data = json_data[timestamp]
                            timediff = timestamp - start_time
                            data['data']['countdown'] = round(600 - timediff.total_seconds(),1)
                            if data['data']['countdown'] < 600: #575:
                                rows.append(data)

                            ## count the time points
                            if 'world_time' in data['data'].keys():
                                xlabels.append('')
                print(trajectory_file, '| # steps =', len(xlabels))
                if len(xlabels) == 0:
                    print('no valid data')
                    continue

                start_time_real = time.time()
                file_name = file[27:-14].replace('_id','').replace('0000','')
                init_replay_with_tom(duration=len(xlabels), trial_id=file_name) ##

                ## sample five steps in each log file
                if TEST_DATASET:
                    PRINT_COUNT = 5
                    index = 0
                    for row in rows:
                        index += 1
                        if index % 20 == 0:
                            start = time.time()
                            replay_with_tom(row['data'], row['msg']['sub_type'])
                            if PRINT_COUNT == 0: break
                else:
                    for row in rows:
                        start = time.time()
                        replay_with_tom(row['data'], row['msg']['sub_type'])

            else:  ## our data format
                with open(trajectory_file) as f:
                    trajectory = json.load(f)

                MAP = trajectory['map']
                if USE_STATA:  MAP = '36by64_40.csv'
                duration = len(trajectory['steps'])
                print(f'duration {duration} in file', trajectory_file)

                ## initiate the observer agent on top of the maze in Malmo, need Malmo client running
                if REPLAY_IN_MALMO:
                    observe_malmo.replay(trajectory['steps'])

                else:  ## otherwise, repaly as images and animations
                    trajectory = trajectory['steps']

                    if REPLAY_DISCRETIZE:  ## generate discretized trajectory
                        repair_trajectory(trajectory, MAP, trajectory_file)

                    else:  ## normal replay
                        inverse_planning(trajectory, file)

    ## POMDP Inverse Planning
    elif INVERSE_PLANNING:
        # trajectory_file = MAP.replace('.csv', '_'+PLAYER_NAME+'.json')

        ## select the most recent trajectory in Trajectory folder
        if USE_SAVED_TRAJECTORIES:
            trajectory_file = MAP.replace('.csv','') + '_' + PLAYER_NAME + '.json'
            TRAJECTORY_FOLDER = join(TRAJECTORY_FOLDER, 'saved')

        ## use the most recent;y generated trajectory file by planning algorithms for the same map and player
        else:
            mypath = TRAJECTORY_FOLDER
            files = [f for f in listdir(mypath) if isfile(join(mypath, f)) and '.json' in f and MAP.replace('.csv','_'+PLAYER_NAME) in f]
            files.sort()
            trajectory_file = files[len(files)-1]

        with open(join(TRAJECTORY_FOLDER, trajectory_file)) as f:
            trajectory = json.load(f)
            print(trajectory_file,' | score_total:',trajectory["score_total"])
            trajectory = trajectory['steps']

        inverse_planning(trajectory, trajectory_file.replace('.json',''))

    else:
        print('No Mode selected!')
