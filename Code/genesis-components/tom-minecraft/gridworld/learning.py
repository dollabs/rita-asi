import random
import time
import matplotlib.pyplot as plt
from PIL import Image
import numpy as np
import imageio
import turtle
from turtle import Turtle, Screen, Shape
from tkinter import PhotoImage
import argparse
import os
from os import listdir
from os.path import isfile, join
import json
import pprint
import datetime
from sys import platform
import statistics
from shutil import copyfile

import sys
sys.path.insert(1, '..')
sys.path.insert(1, 'rl')

import mdp
import player
import visualize
import inverse
import reinforce
import actor_critic
import one_step_actor_critic
import TD

## for param experiment of Q-learning
USE_HUMAN_TRAJECTORY = True    ## start training with human trajectory
JUST_TRAJECTORY = True     ## False for learning with human trajectory
SHOW_FINAL = False      ## only show the end M&M map
SHOW_Q_TABLE = True     ## show Q-table on map
FOR_RECORDING = False      ## take screenshot of map after every episode
PLOT_STEPS = True       ## plot the # of steps to goal instead of reward
FAST_DEBUGGING = False   ## focus on showing the plot, minimize visualization
GENERATE_GIF = False    ## turned off for recording experiments
SHORTEST_PATH = 63     #12 for 6by6_6_T-0;  17 for 6by6_6_T-2;  63 for 13by13_3;

# USE_HUMAN_TRAJECTORY = False    ## start training with human trajectory
# SHOW_FINAL = False      ## only show the end M&M map
# SHOW_Q_TABLE = True     ## show Q-table on map
# JUST_TRAJECTORY = True
# FOR_RECORDING = False      ## take screenshot of map after every episode
# PLOT_STEPS = True       ## plot the # of steps to goal instead of reward
# FAST_DEBUGGING = False   ## focus on showing the plot, minimize visualization
# GENERATE_GIF = True    ## turned off for recording experiments
# SHORTEST_PATH = 63     #12 for 6by6_6_T-0;  17 for 6by6_6_T-2;  63 for 13by13_3;

# # ## for param experiment of policy space methods
# USE_HUMAN_TRAJECTORY = False    ## start training with human trajectory
# SHOW_FINAL = False      ## only show the end M&M map (both new state space and old state space)
# SHOW_Q_TABLE = False     ## show Q-table on map
# JUST_TRAJECTORY = False
# FOR_RECORDING = False      ## take screenshot of map after every episode
# PLOT_STEPS = False       ## plot the # of steps to goal instead of reward
# FAST_DEBUGGING = False   ## focus on showing the plot, minimize visualization
# GENERATE_GIF = True    ## turned off for recording experiments
# SHORTEST_PATH = 63     #12 for 6by6_6_T-0;  17 for 6by6_6_T-2;  63 for 13by13_3;

## visualization parameters
MAIN = False
DEBUG = False
MAX_STEP = 100  # use this when debug is true
EPISODE_TOTAL = 200
EPISODE_GAP = 25
if FAST_DEBUGGING: EPISODE_GAP = EPISODE_TOTAL
RECORDING_FOLDER = join('recordings','200427 RL')  # GIF will be output here

## select which algorithm to run
RL_TD1 = False
RL_TD0 = False
RL_SARSA = False
RL_Q_LEARNING = True
RL_REINFORCE = False
RL_ACTORCRITIC = False
RL_ONESTEP_ACTORCRITIC = False

## short-hand form of the algorithms
ALGORITHM = ''
if RL_TD1: ALGORITHM = 'TD1'
elif RL_TD0: ALGORITHM = 'TD0'
elif RL_SARSA: ALGORITHM = 'SARSA'
elif RL_Q_LEARNING: ALGORITHM = 'QL'
elif RL_REINFORCE: ALGORITHM = 'REIN'
elif RL_ACTORCRITIC: ALGORITHM = 'ACC'
elif RL_ONESTEP_ACTORCRITIC: ALGORITHM = 'OSAC'

## training parameters
parser = argparse.ArgumentParser(description='Parse arguments for the code.')

parser.add_argument('-n', '--player_name', type=str,
    default='Q-Learner', help='Player type')
parser.add_argument('-m', '--map', type=str,
    default='13by13_3.csv', help='Filname of map') # 6by6_6_T.csv   3by3_5.csv   24by24_5.csv   36by64_40.csv   test2.csv   6by6_6_T-1.csv    6by6_6_T-0.csv     24by24_5-1.csv    13by13_3.csv   3by3_2.csv

args = parser.parse_args()


def draw_reward_plot(episodeLimit, reward_max):

    plt.ioff()
    plt.style.use('ggplot')
    fig, ax = plt.subplots()
    fig.set_size_inches(3.5, 2.5)
    box = ax.get_position()

    ## single R/ when the agent has learned a trajectory
    if PLOT_STEPS:
        ax.set_ylim([0,MAX_STEP])
        ax.set_ylabel('# Steps')
        # plt.yscale("log")
        x_coordinates = [1, 2]
        y_coordinates = [3, 3]
        plt.plot([0,EPISODE_TOTAL], [SHORTEST_PATH, SHORTEST_PATH],visualize.GREEN,linestyle='--')

    ## when the agent hadn't learn, compare R
    else:
        ax.set_ylim([0,reward_max])
        ax.set_ylabel('Scores')

    ax.set_position([box.x0+0.06, box.y0+0.08, box.width, box.height]) # left, bottom, width, height
    ax.set_xlabel('Episodes')

    # plt.show()
    xdata = np.linspace(0, episodeLimit, episodeLimit)
    ydata = np.ones(episodeLimit)
    line, = ax.plot(xdata, ydata, visualize.BLUE, linestyle='-', linewidth=1)
    # ax.legend([line], ['score'], loc='upper right', frameon=False)
    return ax, xdata, ydata, line

def update_reward_plot(screen, y, writer, ax, line, xdata, ydata, ann, t, flag_stopping=False):

    ## add in the new data
    line.set_ydata(ydata[0:t+1])
    line.set_xdata(xdata[0:t+1])
    if flag_stopping: plt.plot([xdata[t]], [ydata[t]], visualize.RED,marker = 'x')

    ## annotate the most recent point
    if ann != None:
        ann.remove()
    if t<MAX_STEP/2:
        ann = ax.annotate('{0:.2f}'.format(ydata[t]),xy=(xdata[t],ydata[t]))
    else:
        ann = ax.annotate('{0:.2f}'.format(statistics.median(ydata)),xy=(xdata[t],ydata[t]))

    ## save as png
    plot_file = join(visualize.PLOT_FOLDER, 'rewards.png')
    plt.savefig(plot_file, dpi=100)
    start = time.time()

    paste_plot(screen, y, writer)
    print('finished stamping reward plot',t,'in', str(time.time()-start), 'seconds')

    # ## Turtle stamps only GIF images
    # gif_file = plot_file.replace('png','gif')
    # imageio.mimsave(gif_file, [imageio.imread(plot_file)])
    # smaller_plot = PhotoImage(file=gif_file)
    # screen.addshape("plot", Shape("image", smaller_plot))
    # if writer == None:
    #     writer = Turtle("plot")
    #     writer.hideturtle()
    #     writer.speed(10)
    #     writer.penup()
    # x = -int(visualize.WINDOW_WIDTH/2) + 200
    # writer.goto(x, y-130)
    # writer.showturtle()
    # writer.stamp()
    # screen.update()

    return writer, ann

def paste_plot(screen, y, writer):
    plot_file = join(visualize.PLOT_FOLDER, 'rewards.png')
     ## Turtle stamps only GIF images
    gif_file = plot_file.replace('png','gif')

    ## use PIL Image
    im = Image.open(plot_file)
    im.save(gif_file, save_all=True) #, append_images=[]

    ## use imageio
    # imageio.mimsave(gif_file, [imageio.imread(plot_file)])

    smaller_plot = PhotoImage(file=gif_file)
    screen.addshape("plot", Shape("image", smaller_plot))
    if writer == None:
        writer = Turtle("plot")
        writer.hideturtle()
        writer.speed(10)
        writer.penup()
    x = -int(visualize.WINDOW_WIDTH/2) + 200
    writer.goto(x, y-130)
    writer.showturtle()
    writer.stamp()
    screen.update()


def draw_parameters(params):

    x = -int(visualize.WINDOW_WIDTH/2) + 30

    params_mission = params['mission']
    params_learning = params['learning']

    ## initialize writer turtle
    titles = turtle.Turtle()
    titles.hideturtle()
    titles.up()

    if platform == "darwin":
        heading_above = 16
        heading_below = 6
        section_bdlow = 20
        FONT_SIZE = 14
    elif platform == "win32":
        FONT_SIZE = 10
        heading_above = 16
        heading_below = 16
        section_bdlow = 20

    ## mission parameters, related to map, # of victims, total score ...
    y = 250
    titles.goto(x,y)
    titles.write('Mission parameters', font=("Courier", 18, 'normal', 'bold', 'underline'))
    y -= heading_below

    y -= (len(params_mission.keys())*FONT_SIZE)
    titles.goto(x,y)
    titles.write(pprint.pformat(params_mission, width=30, indent=1), font=("Courier", FONT_SIZE, 'normal'))
    y -= section_bdlow

    ## learning parameters
    y -= heading_above
    titles.goto(x,y)
    titles.write('Learning parameters', font=("Courier", 18, 'normal', 'bold', 'underline'))
    y -= heading_below

    y -= (len(params_learning.keys())*FONT_SIZE)
    titles.goto(x,y)
    titles.write(pprint.pformat(params_learning, indent=1), font=("Courier", FONT_SIZE, 'normal'))
    y -= section_bdlow

    ## header for score board
    y -= heading_above
    titles.goto(x,y)
    if PLOT_STEPS: title = 'Steps to goal (max = '+str(MAX_STEP)+')'
    else: title = 'Scores over time'
    titles.write(title, font=("Courier", 18, 'normal', 'bold', 'underline'))
    y -= heading_below

    return y

def get_params(env, MAX_STEP=MAX_STEP):

    ## mission parameters, related to map, # of victims, total score
    temp1 = {}
    temp1['mission'] = {"map": args.map.replace('.csv','')}
    temp1['goal_count'] = env.victim_summary

    # temp1['total_score'] = len(env.tile_indices) * env.player["exploration_reward"]
    # ## the intrinsic reward
    # # for key, value in env.tilesummary_truth.items():
    # #     if value['type'] not in ['wall','grass','entrance']:
    # #         temp1['total_score'] += value['reward']
    # # temp1['total_score'] = round(temp1['total_score'],2)
    # temp1['total_score'] = env.victim_summary

    ## learning parameters
    temp2 = {}
    if RL_SARSA or RL_Q_LEARNING:
        if RL_SARSA: temp2['algorithm'] = 'SARSA'
        if RL_Q_LEARNING: temp2['algorithm'] = 'Q Learning'
        temp2['temperature'] = TD.TEMPERATURE
        temp2['discount_rate'] = TD.DISCOUNT_RATE
        temp2['learning_rate'] = TD.LEARNING_RATE
        temp2['max_steps'] = TD.MAX_STEPS
        temp2['epsilon'] = TD.EPSILON
        temp2['epsilon_decay'] = TD.EPSILON_DECAY
        temp2['exploration_reward'] = TD.player["exploration_reward"]

    elif RL_REINFORCE:
        temp2['algorithm'] = 'REINFORCE'
        temp2['policy_network'] = reinforce.NETWORK_NODES
        temp2['discount_rate'] = reinforce.DISCOUNT_RATE
        temp2['learning_rate'] = reinforce.LEARNING_RATE
        temp2['dropout_rate'] = reinforce.DROPOUT_RATE
        temp2['weight_decay'] = reinforce.WEIGHT_DECAY
        temp2['max_steps'] = reinforce.MAX_STEPS
        temp2['epsilon'] = reinforce.EPSILON
        temp2['epsilon_decay'] = reinforce.EPSILON_DECAY

    elif RL_ACTORCRITIC:
        temp2['algorithm'] = 'REINFORCE with baseline'
        temp2['policy_network'] = actor_critic.POLICY_NODES
        temp2['value_network'] = actor_critic.VALUE_NODES
        temp2['discount_rate'] = actor_critic.DISCOUNT_RATE
        temp2['learning_rate'] = actor_critic.LEARNING_RATE
        temp2['dropout_rate'] = actor_critic.DROPOUT_RATE
        temp2['weight_decay'] = actor_critic.WEIGHT_DECAY
        temp2['max_steps'] = actor_critic.MAX_STEPS
        temp2['epsilon'] = actor_critic.EPSILON
        temp2['epsilon_decay'] = actor_critic.EPSILON_DECAY

    elif RL_ONESTEP_ACTORCRITIC:
        temp2['algorithm'] = 'one step actor critic'
        temp2['policy_network'] = one_step_actor_critic.POLICY_NODES
        temp2['value_network'] = one_step_actor_critic.VALUE_NODES
        temp2['discount_rate'] = one_step_actor_critic.DISCOUNT_RATE
        temp2['learning_rate'] = one_step_actor_critic.LEARNING_RATE
        temp2['dropout_rate'] = one_step_actor_critic.DROPOUT_RATE
        temp2['weight_decay'] = one_step_actor_critic.WEIGHT_DECAY
        temp2['max_steps'] = one_step_actor_critic.MAX_STEPS
        temp2['epsilon'] = one_step_actor_critic.EPSILON
        temp2['epsilon_decay'] = one_step_actor_critic.EPSILON_DECAY

    else:
        print('Please select one of the methods')
        sys.exit()

    if not DEBUG:
        MAX_STEP = temp2['max_steps']
    else:
        temp2['max_steps'] = MAX_STEP

    return {'mission': temp1, 'learning': temp2}

def episode(env, Q_sa, PI, epsilon=0.5, trajectory=[]):
    """ connect to different algorithms """

    # env.print_tile_summary()
    # print('begin',np.sum(Q_sa))

    flag_stopping = False
    if RL_TD1 == True:
        reward, trajectory = TD.TD1_trajectory(max_steps=MAX_STEP)

    elif RL_TD0 == True:
        reward, trajectory = TD.TD0_trajectory(max_steps=MAX_STEP)

    elif RL_SARSA == True:
        reward, trajectory, Q_sa, flag_stopping = TD.SARSA_trajectory(env, Q_sa=Q_sa, max_steps=MAX_STEP, epsilon=epsilon)

    elif RL_Q_LEARNING == True:
        reward, trajectory, Q_sa, flag_stopping = TD.Q_learning_trajectory(env, Q_sa=Q_sa, max_steps=MAX_STEP, epsilon=epsilon, trajectory=trajectory)

    elif RL_REINFORCE == True:
        reward, trajectory, flag_stopping = reinforce.generate_trajectory(env, max_steps=MAX_STEP, epsilon=epsilon)

    elif RL_ACTORCRITIC == True:
        reward, trajectory, flag_stopping = actor_critic.generate_trajectory(env, max_steps=MAX_STEP, epsilon=epsilon)

    elif RL_ONESTEP_ACTORCRITIC == True:
        reward, trajectory, flag_stopping = one_step_actor_critic.generate_trajectory(env, max_steps=MAX_STEP, epsilon=epsilon)

    else:
        reward, trajectory = generate_trajectory(max_steps=MAX_STEP)

    print('    reward get from episode',reward)
    ## if learning using human trajectory
    if '0' in trajectory:
        trajectory_new = {}
        reward = 0
        for index,value in trajectory.items():
            s,a,r = value
            reward += r
            trajectory_new[int(index)] = [s,a]
        trajectory = trajectory_new
        print('    reward in human trajectory',reward)

    ## counting only the reward from victims to plot
    else:
        reward = env.victim_summary - env.remaining_to_save
        print('    reward in getting victims',reward)

    return reward, trajectory, Q_sa, PI, flag_stopping

def generate_trajectory(PI):
    """ default generator, random s,a, given policy, to be implemented"""
    trajectory = {}
    reward = 0
    return reward, trajectory

def output_EPS(envs, trajectory, prefix, MAP, screen, y, writer, ts, dc, agent, EPS_count, EPS_prefix, params, Q_sa = None, main_loop=True, SHOW_FINAL=SHOW_FINAL):

    t = 0
    trace = []
    timer = None
    real_pos = None
    length = None
    index = 0
    env = envs[visualize.PLAYER_NAME]

    ## clear the last drawing
    screen.clear()
    visualize.draw_maze(env)
    draw_parameters(params)
    paste_plot(screen, y, writer)

    start = time.time()

    VISIT_COUNT = {}
    Q_table = {}
    for index in range(len(trajectory)):
        step = trajectory[index]
        s,a = step

        ## show either vicit count or Q-table
        if not SHOW_Q_TABLE:
            if s not in trace:
                trace.append(s)
                VISIT_COUNT[s] = 1
            else:
                VISIT_COUNT[s] += 1
        else:
            ## old state space
            for i in range(env.ntiles):
                for j in range(4):
                    Q_table[(i,j*90)] = {}
                    for k in range(len(env.actions)):
                        Q_table[(i,j*90)][env.actions[k]] = Q_sa[i,j,k]

        ## show every step of the episode
        if (not SHOW_FINAL or USE_HUMAN_TRAJECTORY) and (not JUST_TRAJECTORY):
            sa_last = trajectory[max(0,index-1)]

            if USE_HUMAN_TRAJECTORY:
                real_pos = env.tilesummary[s[0]]['pos'] ## for hand crafted
                sa_last = env.tilesummary[sa_last[0][0]]['pos']
                length = len(trajectory)

            _, timer = inverse.update_game_canvas(envs, prefix, MAP, screen, ts, dc, agent, s, a, Q_table, t, real_pos=real_pos, timer=timer, sa_last=sa_last, length=length)
            t += 1

    print('finished computing Q_table in',str(time.time()-start),'seconds')
    start = time.time()

    ## show the entire trajectory with old state space
    if JUST_TRAJECTORY:
        VISIT_COUNT = Q_table
        sa_last = None
        inverse.update_game_canvas(envs, prefix, MAP, screen, ts, dc, agent, s, a,
                                   VISIT_COUNT, len(trajectory), real_pos=real_pos, timer=timer,
                                   sa_last=sa_last, length=length, trajectory=trajectory)

    ## show the last frame of the visualization only
    if SHOW_FINAL:
        t = len(trajectory)-1
        if USE_HUMAN_TRAJECTORY:
            env.step -= 1
            real_pos = env.tilesummary[s]['pos']
            sa_last = real_pos
            length = len(trajectory)
        else:
            sa_last = None

        inverse.update_game_canvas(envs, prefix, MAP, screen, ts, dc, agent, s, a, VISIT_COUNT, t, real_pos=real_pos, timer=timer, sa_last=sa_last, length=length)

    # if main_loop: screen.mainloop()  ## stop at the first episode for inspecting

    visualize.take_screenshot(env, screen, join(RECORDING_FOLDER, EPS_prefix+str(EPS_count)+'.eps'), PNG=True)

    print('finished visualization in', str(time.time()-start), 'seconds')


def reinforcement_learning(main_loop=True, MAX_STEP=MAX_STEP, USE_HUMAN_TRAJECTORY=USE_HUMAN_TRAJECTORY):
    """ main framework for reinforcement learning and visualization"""

    ## initiate agent and Turtle screen
    env = mdp.POMDP(visualize.MAP, visualize.MAP_ROOM, player.players[visualize.PLAYER_NAME])
    screen, ts, dc, agent = visualize.draw_maze(env)
    writer = None
    ann = None

    ## plot the rewards from episodes over time
    params = get_params(env)
    y = draw_parameters(params)
    ax, xdata, ydata, line = draw_reward_plot(EPISODE_TOTAL,
        params['mission']['goal_count'] + 1)

    ## initialize Q_sa and PI
    Q_sa = None
    PI = None

    ## start learning from episodes
    EPS_count = 0
    EPS_prefix = str(MAX_STEP)+'_'

    ## if learning from human trajectories, read those files as dict with t:(s,a,r)
    trajectories = []
    if USE_HUMAN_TRAJECTORY:
        test_map = visualize.MAP.replace('.csv','')
        test_map = 'test2'
        mypath = join("trajectories","24by24_Y")
        files = [f for f in listdir(mypath) if isfile(join(mypath, f)) and '.json' in f and test_map in f]
        files.sort()

        for file in files:

            ## auto generated
            # trajectory_file = join(mypath,file)
            # with open(trajectory_file) as f:
            #     trajectory = json.load(f)

            ## manually crafted
            with open(join(mypath,file)) as f:
                trajectory = json.load(f)
                # print(trajectory['0'])

                trajectory_new = {}
                reward = 0
                for index,value in trajectory.items():
                    s,a,r = value
                    tile = int(s[1:s.index(',')])
                    head = int(s[s.index(',')+1:-1])
                    trajectory_new[index] = [(tile,head),a,r]
                    if r>0: reward += r

                # s,a,r = trajectory_new
                # print(s,type())
                # print(len(trajectory_new), reward, file)

                trajectory = trajectory_new

            for i in range(24):
                trajectories.append(trajectory)

        print('number of human trajectories',len(trajectories))

    if 'epsilon' in params['learning']:
        epsilon = params['learning']['epsilon']
        epsilon_decay = params['learning']['epsilon_decay']
    else:
        epsilon = 0.1
        epsilon_decay = 0.98

    # max_reward = -np.Inf
    # best_Q_sa = Q_sa
    for t in range(EPISODE_TOTAL):

        ## new game
        env = mdp.POMDP(visualize.MAP, visualize.MAP_ROOM, player.players[visualize.PLAYER_NAME])
        envs = {visualize.PLAYER_NAME: env}
        start = time.time()
        if USE_HUMAN_TRAJECTORY and t < len(trajectories):
            reward, trajectory, Q_sa, PI, flag_stopping = episode(env, Q_sa, PI, epsilon=epsilon, trajectory=trajectories[t])
        else:
            USE_HUMAN_TRAJECTORY = False
            reward, trajectory, Q_sa, PI, flag_stopping = episode(env, Q_sa, PI, epsilon=epsilon)

        # if reward > max_reward:
        #     max_reward = reward/len(trajectory)
        #     best_Q_sa = Q_sa

        print('finished episode',t,'in', str(time.time()-start), 'seconds')

        ## when goal is found
        if env.remaining_to_save < env.victim_summary:
            print("Victim found!!! with epsilon {}".format(epsilon))
            if not USE_HUMAN_TRAJECTORY: epsilon *= epsilon_decay

        # epsilon *= epsilon_decay

        ## update the reward plot
        if PLOT_STEPS: ydata[t] = len(trajectory)
        else: ydata[t] = reward

        writer, ann = update_reward_plot(screen, y, writer, ax, line, xdata, ydata, ann, t, flag_stopping)

        ## for every k episodes, visualize the play
        checkpoint = 0
        if FAST_DEBUGGING: checkpoint = EPISODE_GAP - 1
        if t % EPISODE_GAP == checkpoint:
            output_EPS(envs, trajectory, '', visualize.MAP, screen, y, writer, ts, dc, agent, EPS_count, EPS_prefix, params, Q_sa)
            EPS_count += 1
        elif FOR_RECORDING:
            output_EPS(envs, trajectory, '', visualize.MAP, screen, y, writer, ts, dc, agent, EPS_count, EPS_prefix, params, Q_sa, SHOW_FINAL=True)
            EPS_count += 1

    # output_EPS(envs, trajectory, '', visualize.MAP, screen, y, writer, ts, dc, agent, EPS_count, EPS_prefix, params, Q_sa)
    if not FAST_DEBUGGING:
        output_EPS(envs, trajectory, '', visualize.MAP, screen, y, writer, ts, dc, agent, EPS_count, EPS_prefix, params, Q_sa)
    else:
        EPS_count -= 1

    ## ----------------------------------------
    #    Logging the experiment
    ## ----------------------------------------

    ## first make an informative name
    p = params['learning']
    file_name = ALGORITHM + '_lr=' + str(p['learning_rate']) + '_dr=' + str(p['discount_rate'])
    if RL_REINFORCE or RL_ACTORCRITIC or RL_ONESTEP_ACTORCRITIC:
        file_name +=  '_dr=' + str(p['dropout_rate']) + '_wd=' + str(p['weight_decay'])
    if RL_SARSA or RL_Q_LEARNING:
        file_name +=  '_ep=' + str(p['epsilon']) + '_ed=' + str(p['epsilon_decay'])
    file_name += '_len=' + str(p['max_steps']) + '_' + visualize.get_time(do_print=True)

    ## save the last frame as png
    old_name = join(RECORDING_FOLDER, EPS_prefix+str(EPS_count)+'.png')
    new_name = join(RECORDING_FOLDER, file_name+'.png')
    copyfile(old_name, new_name)

    ## generate GIF of the learning process
    if GENERATE_GIF:
        start = time.time()
        visualize.generate_GIF(EPS_count, join(RECORDING_FOLDER, EPS_prefix),
            join(RECORDING_FOLDER,file_name.replace('.','-')))
        # print('finished generating GIF in', str(time.time()-start), 'seconds')


    if main_loop:
        screen.mainloop()

def setup_visualize():
    """ certain control parameters in the visualize class need to be changed for the visualization for work for rl """

    ## control related parameters
    visualize.READ_ROOMS = False
    visualize.INVERSE_PLANNING = False
    visualize.PLANNING = False
    visualize.HIERARCHICAL_PLANNING = False
    visualize.EXPERIMENT_REPLAY = False
    visualize.LEARNING = True

    ## test related parameters
    visualize.PLAYER_NAME = args.player_name
    visualize.PLAYERS = [visualize.PLAYER_NAME]
    visualize.MAP = args.map

    size = args.map

    if visualize.USE_STATA or '36by64' in size:
        visualize.USE_STATA = True
        visualize.WORLD_WIDTH = 64
        visualize.WORLD_HEIGHT = 36
        visualize.TILE_SIZE = 14
        visualize.WINDOW_WIDTH = 1350
        visualize.LENGTH = 36
    else:
        if 'test' in size: size = 24
        else: size = int(size[size.index('by')+2:size.index('_')])
        ts = visualize.SIZER_TILE_SIZE[size]
        visualize.WORLD_WIDTH = size
        visualize.WORLD_HEIGHT = size
        visualize.TILE_SIZE = ts
        visualize.LENGTH = int(360/ts)
        visualize.FONT_SIZE = int(ts/5)

if __name__ == '__main__':

    MAIN = True

    setup_visualize()

    ## select algorithms and train
    if os.path.exists('policy_net.pt') and RL_REINFORCE == True:
        os.remove('policy_net.pt')
    if os.path.exists('actor_critic_net.pt') and RL_ACTORCRITIC == True:
        os.remove('actor_critic_net.pt')
    if os.path.exists('one_step_actor_critic_net.pt') and RL_ONESTEP_ACTORCRITIC == True:
        os.remove('one_step_actor_critic_net.pt')
    reinforcement_learning()


## in output_EPS for map '6by6_6_T.csv'
    # trajectory = [
    #     ((30,0),'go_straight'),
    #     ((31,0),'turn_left'),
    #     ((31,90),'go_straight'),
    #     ((25,90),'go_straight'),
    #     ((19,90),'go_straight'),
    #     ((13,90),'go_straight'),
    #     ((7,90),'go_straight'),
    #     ((1,90),'turn_left'),
    #     ((1,180),'turn_left'),
    #     ((1,270),'go_straight'),
    #     ((7,270),'go_straight'),
    #     ((13,270),'go_straight'),
    #     ((19,270),'turn_left'),
    #     ((19,0),'go_straight'),
    #     ((20,0),'go_straight'),
    #     ((21,0),'go_straight'),
    #     ((22,0),'turn_right'),
    #     ((22,270),'go_straight'),
    #     ((28,270),'go_straight'),
    #     ((34,270),'turn_right'),
    #     ((34,180),'go_straight'),
    #     ((33,180),'turn_right'),
    #     ((33,90),'turn_right'),
    #     ((33,0),'go_straight'),
    #     ((34,0),'turn_left'),
    #     ((34,90),'go_straight'),
    #     ((28,90),'go_straight'),
    #     ((22,90),'go_straight'),
    #     ((16,90),'go_straight'),
    #     ((10,90),'go_straight'),
    #     ((4,90),'turn_right'),
    #     ((4,0),'go_straight')
    # ]
