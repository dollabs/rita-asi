import numpy as np
import time
import turtle
import pprint
from tqdm import tqdm
import math
import random

import mapreader
import visualize
import utils

JUST_TRAJECTORY = False  ## output the final trajectory and observation only, cover all tiles as air
SHOW_Q_TABLE = False

def value_iteration(env, V, TXT=None):
    """ performs value iteration and uses state action value to extract greedy policy for gridworld """

    start = time.time()

    epsilon = env.player["epsilon"]
    gamma = env.player["tilelevel_gamma"]

    # helper function
    def best_action(s):
        V_max = -np.inf
        for a in env.actions:
            s_p = env.T(s,a)
            if not s_p[0] == 'wall':
                V_temp = env.R(s_p,a) + gamma*V[s_p[0], s_p[1]//90]
                V_max = max(V_max,V_temp)
        return V_max

    ## ------------------------------
    # Step 1 -- initialize V to be 0
    ## ------------------------------
    if visualize.RANDOM_INITIALIZE_V:
        V = np.zeros((env.ntiles, 4))

    ## ------------------------------
    # Step 2 -- apply Bellman Equation until converge
    ## ------------------------------
    iter = 0
    while True:
        iter += 1
        delta = 0
        for s in env.states:
            tile, head = s
            v = V[tile, head//90]
            V[tile, head//90] = best_action(s) # helper function used
            delta = max(delta, abs(v-V[tile, head//90]))

        # termination condition
        if delta < epsilon: break

    ## ------------------------------
    # Step 3 -- extract greedy policy
    ## ------------------------------
    ## Q_sa can be used for visualizing the Q table
    pi = np.zeros((env.ntiles, 4, len(env.actions)))
    Q_sa = np.zeros((env.ntiles, 4, len(env.actions)))
    for s in env.states:
        Q = np.zeros((len(env.actions),))
        for a in range(len(env.actions)):
            s_p = env.T(s,env.actions[a])
            if not s_p[0] == 'wall':
                Q[a] = env.R(s_p,env.actions[a]) + gamma*V[s_p[0], s_p[1]//90]
                Q_sa[s[0], s[1]//90, a] = Q[a]

        ## highest state-action value
        Q_max = np.amax(Q)

        ## collect all actions that has Q value very close to Q_max
        # pi[s[0], s[1]//90, :] = softmax((Q * (np.abs(Q - Q_max) < 10**-2) / 0.01).astype(int))
        pi[s[0], s[1]//90, :] = np.abs(Q - Q_max) < 10**-3
        pi[s[0], s[1]//90, :] /= np.sum(pi[s[0], s[1]//90, :])

    if TXT != None:
        TXT = visualize.log('... finished VI '+ str(iter)+' in '+str(time.time() - start)+' seconds',TXT)

    return pi, Q_sa, V

# def policy_iteration(env, pi, iter=100):
#
#     epsilon = env.player["epsilon"]
#     gamma = env.player["tilelevel_gamma"]
#
#     # helper function
#     def best_action(s, V):
#         V_temp = []
#         a_max = np.zeros((len(env.actions), ))
#         for a in env.actions:
#             s_p = env.T(s,a)
#             if not s_p[0] == 'wall':
#                 V_temp.append(env.R(s_p,a) + gamma*V[s_p[0], s_p[1]//90])
#             else:
#                 V_temp.append(0)
#
#         a_max = (V_temp == np.amax(V_temp)).astype(float)
#         V_max = np.amax(V_temp)
#
#         return a_max/np.sum(a_max), V_max
#
#     V = np.zeros((env.ntiles, 4))
#     ## ------------------------------
#     # Step 1 -- start with initial policy, for iteration up to iter
#     ## ------------------------------
#     for k in range(iter):
#
#         ## ------------------------------
#         # Step 2 -- policy evaluation: compute V(s) given pi
#         ## ------------------------------
#         for s in env.states:
#             V[s[0], s[1]//90] = 0
#             for a in range(len(env.actions)):
#                 if not pi[s[0], s[1]//90, a] == 0:
#                     s_p = env.T(s, env.actions[a])
#                     if not s_p[0] == 'wall':
#                         V[s[0], s[1]//90] += env.R(s_p, env.actions[a]) + gamma*V[s_p[0], s_p[1]//90]
#             V[s[0], s[1]//90] /= np.sum(pi[s[0], s[1]//90, :])
#
#         ## ------------------------------
#         # Step 3 -- Policy improvement: compute greedy policy pi that maximize V(s')
#         ## ------------------------------
#         V_p = np.zeros((env.ntiles, 4))
#         pi_new = np.zeros((env.ntiles, 4, len(env.actions)))
#         for s in env.states:
#             pi_new[s[0], s[1]//90, :], V_p[s[0], s[1]//90] = best_action(s, V)
#         pi = pi_new
#
#         # Stop if V(s') = V_p is close enough to V(s)
#         delta = np.amax(np.abs(V_p - V))
#         if delta < epsilon:
#             print('converge after ', k, ' iterations')
#             break
#
#     return pi, V

def policy_iteration(env, pi, V=None, iter=100):

    epsilon = env.player["epsilon"]
    gamma = env.player["tilelevel_gamma"]

    # helper function
    def best_action(s, V):
        V_temp = []
        a_max = np.zeros((len(env.actions), ))
        for a in env.actions:
            s_p = env.T(s,a)
            if not s_p[0] == 'wall':
                V_temp.append(env.R(s_p,a) + gamma*V[s_p[0], s_p[1]//90])
            else:
                V_temp.append(-np.Inf)

        a_max = (V_temp == np.amax(V_temp)).astype(float)
        V_max = np.amax(V_temp)

        return a_max/np.sum(a_max), V_max

    if V is None:
        V = np.zeros((env.ntiles, 4))
    ## ------------------------------
    # Step 1 -- start with initial policy, for iteration up to iter
    ## ------------------------------
    for k in range(iter):

        ## ------------------------------
        # Step 2 -- policy evaluation: compute V(s) given pi
        ## ------------------------------
        while True:
            delta = 0
            for s in env.states:
                # V[s[0], s[1]//90] = 0
                v = V[s[0], s[1]//90]
                # for a in range(len(env.actions)):
                #     if not pi[s[0], s[1]//90, a] == 0:
                #         s_p = env.T(s, env.actions[a])
                #         if not s_p[0] == 'wall':
                #             V[s[0], s[1]//90] += env.R(s_p, env.actions[a]) + gamma*V[s_p[0], s_p[1]//90]
                # V[s[0], s[1]//90] /= np.sum(pi[s[0], s[1]//90, :])
                # a = np.random.choice(len(env.actions), p=pi[s[0], s[1]//90, :])
                # s_p = env.T(s, env.actions[a])
                # if not s_p[0] == 'wall':
                #     V[s[0], s[1]//90] = env.R(s_p, env.actions[a]) + gamma*V[s_p[0], s_p[1]//90]
                a = np.nonzero((pi[s[0], s[1]//90, :] > 0).astype(np.float32))[0]
                idx = 0
                s_p = env.T(s, env.actions[a[idx]])
                while s_p[0] == "wall":
                    idx += 1
                    if idx >= len(a):
                        break
                    s_p = env.T(s, env.actions[a[idx]])
                if idx >= len(a):
                    pass
                else:
                    a = a[idx]
                    V[s[0], s[1]//90] = env.R(s_p, env.actions[a]) + gamma*V[s_p[0], s_p[1]//90]
                delta = max(delta, abs(v-V[s[0], s[1]//90]))

            if delta < epsilon: break

        ## ------------------------------
        # Step 3 -- Policy improvement: compute greedy policy pi that maximize V(s')
        ## ------------------------------
        V_p = np.zeros((env.ntiles, 4))
        pi_new = np.zeros((env.ntiles, 4, len(env.actions)))
        for s in env.states:
            pi_new[s[0], s[1]//90, :], V_p[s[0], s[1]//90] = best_action(s, V)
        pi = pi_new

        # Stop if V(s') = V_p is close enough to V(s)
        delta = np.amax(np.abs(V_p - V))
        if delta < epsilon:
            print('converge after ', k, ' iterations')
            break

    return pi, V

def draw_planning_window(player):

    x = -int(visualize.WINDOW_WIDTH/2) + 30

    # initialize observation board and score board writer turtle
    writer = turtle.Turtle()
    writer.hideturtle()
    writer.up()

    scores = turtle.Turtle()
    scores.hideturtle()
    scores.up()

    # initialize writer turtle
    titles = turtle.Turtle()
    titles.hideturtle()
    titles.up()
    titles.goto(x,260)
    titles.write('Expected Tile Reward r(s)', font=("Courier", 18, 'normal', 'bold', 'underline'))
    titles.goto(x,120)
    titles.write('Accumulated Rewards', font=("Courier", 18, 'normal', 'bold', 'underline'))
    titles.goto(x,90)
    titles.write('Total Saved Victims', font=("Courier", 18, 'normal', 'bold', 'underline'))
    titles.goto(x,45)
    titles.write('Player Profile: ', font=("Courier", 18, 'normal', 'bold', 'underline'))
    titles.goto(-240,45)
    titles.write(visualize.PLAYER_NAME, font=("Courier", 18, 'normal', 'bold'))
    titles.goto(x,-140)
    player_string = pprint.pformat(player, indent=1).replace('victim-yellow','chest-green').replace('victim','chest')
    titles.write(player_string, font=("Courier", 12, 'normal'))

    return writer, scores, titles

def plan(env, agent, dc, screen, ts, s0, TXT_name):
    """ play the game using value iteraction or policy iteraction """

    start = time.time()
    player = env.player
    s = s0
    trace = [s[0]]
    ep = [] # collect episode

    if TXT_name != None:
        TXT = open(TXT_name,"w")
    else:
        TXT = None
    TXT = visualize.log(str(player),TXT)

    ## intilize interface
    writer, scores, titles = draw_planning_window(player)
    visualize.update_maze(env,agent,dc,screen,ts,s,trace,real_pos=env.tilesummary[s[0]]['pos'])
    visualize.update_reward_board(env,writer,s,160)
    if visualize.GENERATE_GIF:
        visualize.take_screenshot(env, screen, 'VI_')
    sa_last = env.tilesummary[s[0]]['pos']

    ## initialize pi for PI
    pi = np.zeros((env.ntiles, 4, len(env.actions))).astype(int)
    pi[:, :, 0] = 1
    V1 = np.zeros((env.ntiles, 4))

    for iter in tqdm(range(visualize.MAX_ITER)):

        ## ------------------------------
        ##  Step 1 --- choose action
        ## ------------------------------
        if env.replan:

            ## return the policy, the Q values, and the converged V from the last iteration
            if 'planning_algo' not in env.player:
                pi, Qsa, V1 = value_iteration(env,V1,TXT)
            elif env.player['planning_algo'] == 'value_iteration':
                pi, Qsa, V1 = value_iteration(env,V1,TXT)
            elif env.player['planning_algo'] == 'policy_iteration':
                pi, V1 = policy_iteration(env, pi, V1)
            else:
                break
            env.replan = False

        a = np.random.choice(len(env.actions), p=pi[s[0], s[1]//90, :])

        ##  --------- perform action
        ## when s[0] == 'wall' program will quit
        # s_new = env.T(s,a)
        s_new = env.T(s,env.actions[a])
        while s_new[0] == "wall":
            # if player["POMDP_solver"] == "policy_iteration":
            #     a = np.random.choice(len(env.actions), p=pi[s[0], s[1]//90, :])
            # elif player["POMDP_solver"] == "value_iteration":
            a = np.random.choice(len(env.actions), p=pi[s[0], s[1]//90, :])

            s_new = env.T(s,env.actions[a])

        ep.append((s,a))
        s = s_new
        env._pos_agent = s
        trace.append(s[0])

        if env.check_turned_too_much(a): break

        ## ---------- print the expected value
        writer.clear()

        ## ---------- print action and new location
        pos = env.tilesummary[s[0]]['pos']
        TXT = visualize.log('t = ' +str(iter)+ ', '+ env.actions[a]+ ' to tile '+str(s[0])+ ' at'+ str(mapreader.coord(pos))+'\n',TXT)


        ## ------------------------------
        ##  Step 2 --- observe
        ## ------------------------------
        reward = env.tilesummary_truth[s[0]]['reward']
        visualize.update_maze(env,agent,dc,screen,ts,s,trace,real_pos=env.tilesummary[s[0]]['pos'],sa_last=sa_last,reward=reward)
        visualize.update_reward_board(env,writer,s,160)
        if visualize.GENERATE_GIF:
            visualize.take_screenshot(env, screen, 'VI_')
        sa_last = env.tilesummary[s[0]]['pos']


        ## ------------------------------
        ##  Step 3 --- collect rewards
        ## ------------------------------
        env.collect_reward()

        ## ------------- check termination
        if len(env.remaining_to_save.values()) == 0:
            print('\n\\\\\\\\\\\\\\\\\\\\ FOUND ALL VICTIMS /////////////')
            TXT = visualize.log('\\\\\\\\\\\\\\\\\\\\ FINISHED MISSION /////////////',TXT)
            break
        elif time.time() - start > 1000:
            TXT = visualize.log('\n\\\\\\\\\\\\\\\\\\\\ TIME OUT /////////////',TXT)
            break

    if JUST_TRAJECTORY:
        Q_table = None

        # for i in range(env.ntiles):
        #     for j in range(4):
        #         Q_table[(i,j*90)] = {}
        #         for k in range(len(env.actions)):
        #             Q_table[(i,j*90)][env.actions[k]] = Qsa[i,j,k]
        # s = ep[len(ep)-1][0]
        visualize.update_maze(env,agent,dc,screen,ts,s,Q_table,real_pos=env.tilesummary[s[0]]['pos'],trajectory=ep)
        visualize.take_screenshot(env, screen, 'VI_')

    TXT = visualize.log('\n... finished game in '+ str(time.time() - start)+ ' seconds',TXT)
    if TXT!= None: TXT.close()
    return ep
