import pandas as pd
import numpy as np
import turtle
from tkinter import PhotoImage
from turtle import Turtle, Screen, Shape
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter, MaxNLocator
import imageio
import math
from math import pi, sqrt, exp
from tqdm import tqdm
from os.path import join
import copy
from scipy.special import softmax
import time

import visualize
import mdp
import mapreader
import tabular
import hierarchical
import player
import dfs
import utils

def draw_inv_planning_window(duration=6000, trial_id=None):

    if not visualize.USE_INTERFACE: return None, None, None

    titles_loc = [260, 120, -20]
    titles = turtle.Turtle()
    titles.hideturtle()
    titles.up()
    writers = {}
    label = Turtle()
    label.hideturtle()
    label.penup()

    ## show plot in the visualization window
    if visualize.SHOW_PLOT:
        titles.goto(-420,260)
        titles.write('Trial ID:', font=("Courier", 16, 'normal', 'bold', 'underline'))
        titles.goto(-310,260)
        titles.write(trial_id, font=("Courier", 16, 'normal'))

        # titles.goto(-420,260)
        # titles.write('True Player Type:', font=("Courier", 16, 'normal', 'bold', 'underline'))
        # titles.goto(-230,260)
        # titles.write(visualize.PLAYER_NAME, font=("Courier", 16, 'normal'))

        titles.goto(-420,230)
        titles.write('Most Likely Type:', font=("Courier", 16, 'normal', 'bold', 'underline'))
        titles.goto(-420,200)
        titles.write('Possible Player Types', font=("Courier", 16, 'normal', 'bold', 'underline'))

    # # turn on interactive mode
    # if visualize.SHOW_PLOT:
    #     plt.ioff()
    # else:
    #     plt.ion()
    # plt.style.use('ggplot')
    #
    # fig, ax = plt.subplots()
    # fig.set_size_inches(3.5, 4)
    # if visualize.REPLAY_IN_RITA or visualize.REPLAY_WITH_TOM:
    #     fig.set_size_inches(8, 4)
    # box = ax.get_position()
    # ax.set_ylim([0,1])
    # ax.set_position([box.x0, box.y0, box.width*1.05, box.height * 0.9])
    # ax.set_xlabel('Step')
    #
    # lines = []
    # styles = ['-', '+', 's', '--', 'x', '>']
    # sizes = [2,5,3,2,5,3]
    # xdata = np.linspace(0, duration-1, duration)
    #
    # ydata = {}
    # player_index = 0
    # for player_type in visualize.PLAYERS:
    #     ydata[player_type] = np.ones(duration) /len(visualize.PLAYERS)
    #     line, = ax.plot(xdata, ydata[player_type], styles[player_index], markersize=sizes[player_index])
    #     lines.append(line)

    player_index = 0
    for player_type in visualize.PLAYERS:
        ## make the titles of the Pi table
        if visualize.SHOW_PI_TABLE:
            titles.goto(-420,titles_loc[player_index])
            titles.write(player_type, font=("Courier", 18, 'normal', 'bold', 'underline'))

        ## initialize observation board and score board writer turtle
        if visualize.SHOW_LIKELIHOODS:
            writer = turtle.Turtle()
            writer.hideturtle()
            writer.up()
            writers[player_type] = writer
        player_index += 1

    return titles, writers, label #, fig, ax, xdata, ydata, lines

# def update_inv_planning_window(screen, label, envs, writers, PIs, Qs, likelihoods, xdata, ydata, lines, t, duration=6000):
def update_inv_planning_window(screen, label, envs, writers, PIs, Qs, likelihoods, xdata, ydata, lines, t, window_length, xlabels):

    most_likely = ''
    max_posterior = 0
    acts = {'go_straight':'g', 'turn_left':'l', 'turn_right': 'r'}
    yvalue = {}
    yvalue_log = {}

    def gauss(n=20):
        """ half of gaussian filter as the window """
        sigma = n
        r = range(-int(n), int(n) + 1)
        g = [1 / (sigma * sqrt(2 * pi)) * exp(-float(x) ** 2 / (2 * sigma ** 2)) for x in r][1:n + 1]
        return np.asarray([m / g[-1] for m in g])
    gaussian = gauss(visualize.WINDOW_LENGTH)

    ## ----------------------------------------
    #       update posterior distribution
    ## ----------------------------------------
    for player_type in visualize.PLAYERS:
        filtered = np.multiply(gaussian,likelihoods[player_type])
        yvalue[player_type] = np.prod(filtered)
        yvalue_log[player_type] = np.log(filtered).sum() ## use sum of log likelihood to avoid floating point loss

    norm = sum(yvalue.values())
    norm_log = sum(yvalue_log.values())
    for player_type in visualize.PLAYERS:
        # temp = yvalue[player_type] / norm
        temp = yvalue[player_type] / norm
        ydata[player_type][t] = temp
        if temp >= max_posterior:
            most_likely = player_type
            max_posterior = temp

    if not visualize.USE_INTERFACE: return most_likely, ydata

    # -----------------------------------------
    # turn on interactive mode
    if visualize.SHOW_PLOT:
        plt.ioff()
    else:
        plt.ion()
    plt.style.use('ggplot')

    fig, ax = plt.subplots()
    fig.set_size_inches(3.5, 4)
    if visualize.REPLAY_IN_RITA or visualize.REPLAY_WITH_TOM:
        fig.set_size_inches(7, 4)
        plt.subplots_adjust(left=0.11, right=0.95, top=0.9, bottom=0.12)
    box = ax.get_position()
    ax.set_ylim([0, 1])
    ax.set_position([box.x0, box.y0, box.width * 1.05, box.height * 0.9])

    lines = []
    styles = ['-', '+', 's', '--', 'x', '>']
    sizes = [2, 5, 3, 2, 5, 3]

    # -----------------------------------------
    player_index = 0
    for player_type in visualize.PLAYERS:
        line, = ax.plot(xdata, ydata[player_type], styles[player_index], markersize=sizes[player_index])
        lines.append(line)
        lines[player_index].set_ydata(ydata[player_type][0:t+1])
        lines[player_index].set_xdata(xdata[0:t+1])
        player_index += 1


        ## ----------------------------------------
        #      update the PI table for debugging
        ## ----------------------------------------
        env = envs[player_type]
        if visualize.SHOW_PI_TABLE:
            writer_loc = [156, 133, 112] # [175, 35, -105]
            action_map = pd.DataFrame("", index=range(visualize.WORLD_HEIGHT), columns=range(visualize.WORLD_WIDTH))
            for index in env.tilesummary.keys():
                tile = env.tilesummary[index]
                i,j = mapreader.coord(tile['pos'])
                string = ''
                for head in range(4):
                    for item in PIs[player_type][(index,head)]:
                        string += str(acts[item[0]]) + ','
                    string += '/'
                string += ')'
                string = string.replace(',/', '/')
                string = string.replace('/)', '')
                action_map.loc[(i, j)] = string

            writers[player_type].clear()
            writers[player_type].goto(20,writer_loc[player_index])
            writers[player_type].write(action_map.to_string(), font=("Helvetica", 9, 'normal'))


    ## adjust range of x data
    def format_fn(tick_val, tick_pos):
        if int(tick_val) in xdata:
            return xlabels[int(tick_val)]
        else:
            return ''

    ax.set_xticklabels(xlabels)
    ax.set_xlabel('Time countdown (seconds)')
    ax.xaxis.set_major_formatter(FuncFormatter(format_fn))
    if env.step > window_length:
        ax.set_xlim(0, env.step)
    else:
        ax.set_xlim(0, window_length)

    # specify the lines and labels of the first legend
    if visualize.REPLAY_IN_RITA or visualize.REPLAY_WITH_TOM:
        ax.legend(lines, visualize.PLAYERS, ncol=2, bbox_to_anchor=(0.55, 1.28),
                  loc='upper left', frameon=False)
    else:
        ax.legend(lines, visualize.PLAYERS, ncol=2, bbox_to_anchor=(0, 1.28),
                  loc='upper left', frameon=False)

    plot_file = join(visualize.PLOT_FOLDER, 'plot.png')
    gif_file = plot_file.replace('png','gif')
    plt.savefig(plot_file, dpi=100)
    plt.clf()
    plt.close()

    if visualize.SHOW_PLOT:
        imageio.mimsave(gif_file, [imageio.imread(plot_file)])
        smaller_plot = PhotoImage(file=gif_file) #.subsample(3, 3)
        screen.addshape("plot", Shape("image", smaller_plot))
        label.clear()
        label.shape("plot")
        label.hideturtle()
        label.speed(10)
        label.penup()
        label.goto(-260, -10)
        if visualize.REPLAY_IN_RITA or visualize.REPLAY_WITH_TOM:
            label.goto(-490, -30)
        label.showturtle()
        label.stamp()
        # label.clear()
        label.hideturtle()
        label.goto(-230,230)
        label.write(most_likely, font=("Courier", 16, 'normal'))

    player_index = 0
    s = env._pos_agent
    if visualize.SHOW_LIKELIHOODS:
        for player_type in visualize.PLAYERS:

            ## print out the posterior
            last_likelihood = round(likelihoods[player_type][len(likelihoods[player_type]) - 1], 2)
            action = PIs[player_type][s[0], s[1] // 90]
            value = Qs[player_type][s[0], s[1] // 90]
            values = []
            for v in value:
                values.append(round(v))
            string = str(last_likelihood) + '  ' + str(action) + '  ' + str(values)
            # action = [env.actions[i] for i, j in enumerate(action) if j == max(action)]
            writers[player_type].clear()
            writers[player_type].goto(-250, writer_loc[player_index])
            writers[player_type].write(string, font=("Helvetica", 12, 'normal'))

            player_index += 1

    return most_likely, ydata

def estimate_Qs_PIs(envs, s, a, likelihoods, PIs, Qs, Vs_room, Vs_tile,
                    temperature, t, note=None, screen=None, skipped=False):

    actions = {} # {'go_straight':0, 'turn_left':1, 'turn_right':2, 'triage':3}
    for index in range(len(mdp.POMDP.actions)):
        actions[mdp.POMDP.actions[index]] = index

    shifts = {
        'green': (-1/3, -1/4), 'both': (0, -1/4), 'yellow':(1/3, -1/4),
        'with_dog_green': (-1/3, 1/4), 'with_dog_both': (0, 1/4), 'with_dog_yellow':(1/3, 1/4)
    }
    norm = 0
    pd.set_option('display.max_columns', None)
    summary = pd.DataFrame(columns=['player', #"[Q(s,a')]", 'Q(s)',
                                    'Q(s,a)',
                                    # 'soft Q(s,a)',
                                    'p(s,a)',
                                    # 'Pi(s)',
                                    'a_max',
                                    'goal_tile'])
    index = 0

    first_player = visualize.PLAYERS[0]
    likelihoods_temp = {}
    for player_type in visualize.PLAYERS:
        env = envs[player_type]

        ## use the newly converged result of the first player type when player just changed rooms
        V_room = Vs_room[player_type]
        V_tile = Vs_tile[player_type]
        # if player_type != first_player and env.replan:
        #     V_room = Vs_room[first_player]
        #     V_tile = Vs_tile[first_player]

        ## update the stats
        env.step = t
        env._pos_agent = s

        ## ----------------------------------------
        #       Estimating Q functions
        ## ----------------------------------------

        if visualize.INVERSE_PLANNING_HVI or visualize.REPLAY_IN_RITA or visualize.REPLAY_WITH_TOM:
            if env.replan:
                # (PIs[player_type], Qs[player_type], roomlevel_V, Vs[player_type]), (_,_,_) \
                # = hierarchical.policy_and_value(env, Vs[player_type], None, s)
                (_, _, roomlevel_V, Vs_room[player_type]), (PIs[player_type], Qs[player_type], Vs_tile[player_type]) \
                    = hierarchical.policy_and_value(env, V_room, V_tile, s, normalize=True)
            else:
                print('skipped planning because policy didnt change')

        else:
            print('nothing happening')
            # PIs[player_type], Qs[player_type], Vs[player_type] = tabular.value_iteration(env, Vs[player_type])

        Q_p = 0
        Q_sa = []

        ## get the goal tile
        max_tile = -1
        max_tile_discounted = -np.inf
        roomlevel_tiles = []
        for state in np.transpose(np.nonzero(Vs_tile[player_type])):
            tile, head = state
            if tile not in roomlevel_tiles:
                roomlevel_tiles.append(tile)
                discounted = env.tilesummary[tile]['reward'] * env.player["tilelevel_gamma"] ** hierarchical.dist(env, tile, s[0])
                if discounted > max_tile_discounted:
                    max_tile_discounted = discounted
                    max_tile = tile

        if screen != None:
            x, y = env.tilesummary_truth[max_tile]['pos']
            x_s, y_s = shifts[player_type]
            ts = visualize.TILE_SIZE
            img_name = join('texture','goals',player_type+'.gif')
            screen.register_shape(img_name)
            note.shape(img_name)
            note.penup()
            note.goto(visualize.MAZE_HOR_OFFSET+(x+x_s)*ts, visualize.MAZE_VER_OFFSET+(y+y_s)*ts)
            note.stamp()
        # print(player_type, max_tile, Vs_tile[player_type][max_tile], max_tile_discounted)

        ## roomlevel inverse planning
        # room = env.tiles2room[s[0]]
        # room_next = env.tiles2room[a[0]]
        # for a_p in env.roomlevel_actions[room]:
        #     Q_p += np.exp(Qs[player_type][room, a_p] / temperature)
        # likelihood = np.exp(Qs[player_type][room, room_next] / temperature) / Q_p  # softmax
        # summary.loc[index] = [player_type, #round(np.exp(Qs[player_type][s[0],s[1]//90,a]),2), # np.round(Q_sa,2), round(Q_p,2),
        #                       round(likelihood,2),
        #                       # np.round(PIs[player_type][room],2),
        #                       np.argmax(PIs[player_type][room])]

        ## tilelevel inverse planning
        env.print_policy_csv(PIs[player_type])
        temperature = env.player['temperature']
        for a_p in env.actions:
            if not (a_p == 'go_straight' and env.tilesummary[s[0]][s[1]] == 'wall'):
                Q_p += np.exp(Qs[player_type][s[0],s[1]//90,actions[a_p]]/temperature)
                Q_sa.append(np.exp(Qs[player_type][s[0],s[1]//90,actions[a_p]]/temperature))
            else:
                # print('          encounter wall', a_p)
                Q_sa.append(-1)

        if a in env.actions:
            a = actions[a]
        likelihood = np.exp(Qs[player_type][s[0],s[1]//90,a]/temperature) / Q_p # softmax
        # Qs_soft = softmax(Qs[player_type][s[0],s[1]//90]/temperature)
        summary.loc[index] = [player_type, # round(np.exp(Qsa[player_type][s[0],s[1]//90,a]),2), # round(Q_p,2),
                              np.round(Qs[player_type][s[0],s[1]//90], 2),
                              # np.round(Qs_soft, 2),
                              round(likelihood,5),
                              env.actions[np.argmax(PIs[player_type][s[0],s[1]//90])],
                              max_tile]
        env._max_tile = max_tile

        if math.isnan(likelihood):
            skipped = True
            print('likelihood is NaN')
            break
        else:
            likelihoods_temp[player_type] = likelihood
        # likelihoods[player_type].append(likelihood)
        # likelihoods[player_type].pop(0)
        norm += likelihood
        index += 1

    if not skipped:
        ## print the Q values for inspection
        if visualize.LOG:
            print(summary)
            print()
        for player_type in likelihoods_temp:
            likelihoods[player_type].append(likelihoods_temp[player_type])
            likelihoods[player_type].pop(0)

    # if visualize.REPLAY_IN_RITA or visualize.REPLAY_WITH_TOM:
    #     note.goto(-820, 200)
    #     note.write(summary.to_string(index=False), font=("Courier", 12, 'normal'))

    # last_index = len(likelihoods[player_type]) - 1
    # for player_type in visualize.PLAYERS:
    #     likelihoods[player_type][last_index] /= norm

    return PIs, Qs, Vs_room, Vs_tile, likelihoods

def get_angle(yaw):
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

def update_game_canvas(envs, screen, ts, dc, agent, s, a, trace, t, real_pos=None, timer=None, sa_last=None, length=None, yaw=None, reward=None, trajectory=None):

    ## for replaying human continuous trajectory, get the angles for updating shadow
    angle = visualize.AGENT_OBS_ANGLE

    if visualize.EXPERIMENT_REPLAY or yaw != None:
        angle = get_angle(yaw)

    ## print the current step count on canvas
    if timer == None:
        timer = Turtle()
        timer.hideturtle()
        timer.up()
    else:
        timer.clear()

    if visualize.USE_STATA:
        timer.goto(-380,-240)
    else:
        timer.goto(-20,-240)
    timer.write('Step: '+str(t), font=("Courier", 14, 'normal'))

    ## update newly observed area
    PRINT = False
    if visualize.LEARNING: PRINT = False

    new_obs_tiles = []
    if visualize.INVERSE_PLANNING:
        for player_type in visualize.PLAYERS:
            env = envs[player_type]

            ## ------------- remove rewards
            env.collect_reward()

            ## ------------------------------
            # observe tiles around the new location, update only through the first environment
            if new_obs_tiles == []:
                if not (visualize.LEARNING):
                    new_obs_tiles = mapreader.print_shadow(env, s, angle=angle)

                ## ---------- update observed tiles on maze  ## new_obs_tiles=new_obs_tiles,
                visualize.update_maze(env,agent,dc,screen,ts,s,trace,real_pos=real_pos,a=a,sa_last=sa_last, length=length, reward=reward, trajectory=trajectory)

            ## ---------- print action and new location
            if PRINT: print('t = ' +str(t)+ ', ', a, ' to ', mapreader.coord(env.tilesummary[s[0]]['pos']))

            envs[player_type] = env

    elif visualize.EXPERIMENT_REPLAY:
        visualize.update_maze(envs[0], agent, dc, screen, ts, s, trace, real_pos=real_pos, a=a, sa_last=sa_last,
                              length=length, reward=reward, trajectory=trajectory)

    return envs, timer

def print_s(env, s, t=''):
    tile = env.tilesummary[s[0]]
    type = tile['type']
    pos = (tile['row'], tile['col'])
    print('time',t, 'state', pos, s[1], type)

def get_sa(env, trajectory, t, trace):

    reward = None #TODO
    if visualize.INVERSE_PLANNING:
        current = trajectory[str(t)]
        next = trajectory[str(t+1)]
        s = (current["tile"], current["heading"])
        s_p = (next["tile"], next["heading"])
        a = current["action"]
        a_p = next["action"]

    elif visualize.EXPERIMENT_REPLAY:
        current = trajectory[str(t)]
        next = trajectory[str(t+1)]
        s = (get_tile(env, current), get_heading(current["yaw"]))
        s_p = (get_tile(env, next), get_heading(next["yaw"]))
        a = '--'
        a_p = '--'
        if 'new_vv' in next:
            reward = 2
        elif 'new_v' in next:
            reward = 1

    trace.append(s_p[0])

    # print_s(env, s, t)
    if reward!= None: print('__', reward)
    return s, a, s_p, a_p, reward, trace

def get_tile(env, value):

    ## use 'cell' in recorded data -- less smooth
    # cell = value['cell']
    # x = math.floor(float(cell[cell.index('(')+1:cell.index(',')])-0.5)
    # y = math.floor(float(cell[cell.index(',')+1:cell.index(')')])-0.5)

    ## use 'x' and 'y' in recorded data
    x = math.floor(value['x'])
    y = math.floor(value['z'])

    x,y = correct_xy(env,x,y)

    return env.tile_indices[(int(y),int(x))]

def correct_xy(env,x,y):
    ww = visualize.WORLD_WIDTH
    wh = visualize.WORLD_HEIGHT
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
    return  x,y

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

def print_trajectory(trajectory):
    for item,value in trajectory.items():
        x = math.floor(value['x'])
        z = math.floor(value['z'])
        heading = get_heading(value['yaw'])
        print(item, x, z, heading)

def repair_trajectory(trajectory, MAP, DEBUG = False):

    trajectory_new = {}
    x_last = None
    z_last = None
    heading_last = None
    item2 = 0
    for item,value in trajectory.items():
        x = math.floor(value['x'])
        z = math.floor(value['z'])
        heading = get_heading(value['yaw'])

        if x_last != None:
            if abs(x-x_last) + abs(z-z_last)>1:
                if DEBUG: print(item, x_last, z_last, heading_last, '  ->  ', x, z, heading)
                states, actions = dfs.dfs(MAP, start=((x_last,z_last),heading_last), end=(x,z))
                if DEBUG: print(states)

                for index in range(1,len(states)):
                    value2 = copy.deepcopy(value)
                    state = states[index]
                    value2['x'] = state[0][0]-1
                    value2['z'] = state[0][1]-1
                    value2['yaw'] = state[1] # 270-state[1]  ## convert to yaw system in Minecraft
                    if 'new_v' in value2: value2.pop('new_v')
                    if 'new_vv' in value2: value2.pop('new_vv')
                    trajectory_new[str(item2)] = value2
                    item2 += 1
                    if DEBUG:  print('   ',value2['x'], value2['z'], value2['yaw'])
            elif abs(heading-heading_last)==180:
                value2 = copy.deepcopy(value)
                value2['yaw'] = (heading+heading_last)/2
                if 'new_v' in value2: value2.pop('new_v')
                if 'new_vv' in value2: value2.pop('new_vv')
                trajectory_new[str(item2)] = value2
                item2 += 1
                if DEBUG:  print('   ',value2['x'], value2['z'], value2['yaw'])

        trajectory_new[str(item2)] = value
        item2 += 1
        if DEBUG:  print('   ',value['x'], value['z'], value['yaw'])

        x_last = x
        z_last = z
        heading_last = heading

    if DEBUG:
        print_trajectory(trajectory)
        print(len(trajectory), len(trajectory_new))
        print_trajectory(trajectory_new)

    return trajectory_new

def inverse_plan(screen, ts, dc, agent, trajectory, trajectory_file, MAP, temperature, main_loop=True):

    timer = None

    ## initialize writer turtle and plot
    if visualize.INVERSE_PLANNING:
        duration = len(trajectory.keys())
        titles, writers, label, fig, ax, xdata, ydata, lines = draw_inv_planning_window(duration)

    ## replay human trajectory
    elif visualize.EXPERIMENT_REPLAY:
        visualize.PLAYERS = ['systematic']

    ## discretize human trajectory -- needs testing
    # trajectory = repair_trajectory(trajectory, MAP)  ## for making up those missed in the middle

    #  Initialize planning agents
    envs = {}  ## environment of different player types
    likelihoods = {}  ## dictionary of list of likelihoods, used to calculate the product of likelihoods over a time window
    Qs = {}
    PIs = {}
    Vs_room = {}  ## for reusing the converged V from the last step by different agent players
    Vs_tile = {}  ## for reusing the converged V from the last step by different agent players

    for player_type in visualize.PLAYERS:

        ## initiate player
        env = mdp.POMDP(MAP, visualize.MAP_ROOM, player.players[player_type])
        envs[player_type] = env
        likelihoods[player_type] = [1] * visualize.WINDOW_LENGTH

        ## initialize value functions for VI
        Vs_room[player_type] = np.zeros((len(env.roomlevel_states), 4))
        Vs_tile[player_type] = np.zeros((env.ntiles, 4))

    ## ----------------------------------------
    #   for each step in the trajectory, compare the posterior
    ## ----------------------------------------
    trace = [env.start_tile]
    real_pos_last = None
    index = 0
    for key in tqdm(trajectory.keys()):
        index += 1
        t = int(key)
        if index == len(trajectory.keys()):
            break

        ## look up s, a, s' and a'
        s, a, s_p, a_p, reward, trace = get_sa(env, trajectory, t, trace)
        real_pos = None

        ## infer player type
        if visualize.INVERSE_PLANNING:

            real_pos = env.tilesummary[s[0]]['pos']

            ## estimate Q, PI and posterior probability using planning algorithms
            PIs, Qs, Vs_room, Vs_tile, likelihoods = estimate_Qs_PIs(envs, s, a, likelihoods, PIs, Qs, Vs_room, Vs_tile, temperature, t)

            ## update the Q or PI tables, or posterior plot
            update_inv_planning_window(screen, label, envs, writers, PIs, Qs, likelihoods, xdata, ydata, lines, t)
            envs, timer = update_game_canvas(envs, screen, ts, dc, agent, s_p, a, trace, t,
                                             real_pos=real_pos, timer=timer, sa_last=real_pos_last,
                                             length=len(trajectory), reward=reward)

            if visualize.GENERATE_GIF:
                visualize.take_screenshot(env, screen, 'POST_')

        ## or just visualize the human trajectory
        if visualize.EXPERIMENT_REPLAY:

            ## continuous original trace
            if not visualize.REPLAY_DISCRETIZE:
                pos = trajectory[str(t+1)]
                real_pos = mapreader.coord((pos['z'], pos['x']))

            ## discretized trace
            elif visualize.REPLAY_DISCRETIZE:
                real_pos = env.tilesummary[s_p[0]]['pos']

            envs, timer = update_game_canvas([env], screen, ts, dc, agent, s_p, a, trace, t,
                                             real_pos=real_pos, timer=timer, sa_last=real_pos_last,
                                             length=len(trajectory), yaw=trajectory[str(t)]['yaw'], reward=reward)

            if visualize.REPLAY_TO_PNGs:
                visualize.take_screenshot(env, screen, trajectory_file, PNG=True)

            elif visualize.REPLAY_TO_GIF:
                visualize.take_screenshot(env, screen, trajectory_file)

        env.collect_reward()
        real_pos_last = real_pos

    # ## keep the Turtle window open
    # if not visualize.EXPERIMENT_REPLAY:
    #     if main_loop: screen.mainloop()

    return env
