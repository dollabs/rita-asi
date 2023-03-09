# This is the TD code for the original state and action space

import random
import numpy as np
from scipy.special import softmax

# TEMPERATURE = 1.0
# LEARNING_RATE = 0.5
# DISCOUNT_RATE = 0.99
# MAX_STEPS = 200
TEMPERATURE = 2
LEARNING_RATE = 0.1
DISCOUNT_RATE = 0.99
MAX_STEPS = 100
EPSILON = 0.05
EPSILON_DECAY = 0.98
EXPLORATION_REWARD = 0

actions = {'go_straight': 0, 'turn_left': 1, 'turn_right': 2}

def initialize_PI(env):
    return np.zeros((len(env.actions), ))

def initialize_Q(env):
    return np.zeros((env.ntiles, 4, len(env.actions)))

def get_softmax_policy(Q_sa, temperature=TEMPERATURE):
    return softmax(Q_sa/TEMPERATURE, axis = 2)

def available_actions(env, s):
    actions = [] # 'turn_left','turn_right'

    ## delete actions that go into walls
    if env.tilesummary[s[0]][s[1]] != 'wall':
        actions.append('go_straight')
    
    ## delete actions that turn towards walls
    next_state = env.T(s, 'turn_left')
    if env.tilesummary[next_state[0]][next_state[1]] != 'wall':
        actions.append('turn_left')
    next_state = env.T(s, 'turn_right')
    if env.tilesummary[next_state[0]][next_state[1]] != 'wall':
        actions.append('turn_right')

    ## ensure there's at least one kind of turning to prevent the corner case
    if 'turn_left' not in actions and 'turn_right' not in actions:
        actions.append(random.choice(['turn_left','turn_right']))

    return actions

def choose_action(env, pi, s, epsilon=0.5):

    # avail_actions = available_actions(env, s)
    flag = 'valid'

    # Choose an action according to the distribution
    if np.random.rand() <= epsilon:
        a = env.actions[np.random.choice(3)]
    else:
        # prob = pi[s]
        prob = pi[s[0], s[1]//90]
        # index = np.random.choice(3, p=prob)
        # index = np.argmax(prob)
        mask = (prob == np.amax(prob)).astype(float)
        mask /= np.sum(mask)
        # a = env.actions[index]
        a = env.actions[np.random.choice(3, p=mask)]

    if env.T(s,a)[0] == "wall":
        flag = 'invalid'
            
    return a, flag

## ------------------------------
##  Assume we are given a policy, we want to estimate V
## ------------------------------
def TD1_trajectory(max_steps=5000):
    reward = 0
    trajectory = []

    return reward, trajectory

def TD0_trajectory(max_steps=5000):
    reward = 0
    trajectory = []

    return reward, trajectory

def SARSA_trajectory(env, Q_sa, max_steps=5000, Q_Learning = False, epsilon=0.5):

    flag_stopping = False
    LEARNING_RATE = 0.1
    reward = 0
    trajectory = []
    visited = []
    if Q_sa is None:
        Q_sa = initialize_Q(env)

    # for tile in range(env.ntiles):
    #     for head in range(4):
    #         for action in range(len(env.actions)):
                # if Q_sa[tile][head][action]!=0:
                #     print(Q_sa[tile][head][action])

    s = env._pos_agent
    s_p = None
    pi = get_softmax_policy(Q_sa)
    a, flag = choose_action(env, pi, s)

    for t in range(max_steps):

        # if flag == 'valid':
        trajectory.append((s,a))
        visited.append(s)

        ## take action a and observe s_p, r
        if flag == 'invalid':
            s_p = s
            r = -0.5
        else:
            s_p = env.T(s,a)
            if s_p in visited:
                r = -0.25
            else:
                r = env.R(s_p,a)
            # r = env.R(s_p,a)   ## no cost in revisit
        reward += r

        ## SARSA: on-policy TD control
        if not Q_Learning:

            ## choose a_p using policy from Q
            pi = get_softmax_policy(Q_sa)
            a_p, flag1 = choose_action(env, pi, s_p)

            ## update Q - making sure the Q value of the last state be 1
            if env.remaining_to_save != 0:
                Q_sa[s[0], s[1]//90, actions[a]] += LEARNING_RATE * (r + DISCOUNT_RATE*Q_sa[s_p[0], s_p[1]//90, actions[a_p]] - Q_sa[s[0], s[1]//90, actions[a]])
            else:
                Q_sa[s[0], s[1]//90, actions[a]] += LEARNING_RATE * (r - Q_sa[s[0], s[1]//90, actions[a]])
            
            if flag == 'invalid':
                flag_stopping = True
                break
            a = a_p
            flag = flag1

        ## Q Learning: off-policy TD control
        else:
            ## update Q 
            Q_sa[s[0], s[1]//90, actions[a]] += LEARNING_RATE * (r + DISCOUNT_RATE*np.amax(Q_sa[s_p[0], s_p[1]//90, :]) - Q_sa[s[0], s[1]//90, actions[a]])
            if flag == 'invalid':
                flag_stopping = True
                break
            pi = get_softmax_policy(Q_sa)
            a, flag = choose_action(env, pi, s_p, epsilon=epsilon)

        if len(env.remaining_to_save.values()) == 0:
            break

        s = s_p
        
    return reward, trajectory, Q_sa, flag_stopping

def SARSA_human_trajectory(env, Q_sa, max_steps=5000, Q_Learning = False, trajectory = [], epsilon=0.5):

    flag_stopping = False
    LEARNING_RATE = 0.99
    reward = 0
    visited = []
    if Q_sa is None:
        Q_sa = initialize_Q(env)


    # Starting state
    s = trajectory['0'][0]
    s_p = None

    for t in range(len(trajectory)-1):

        s, a, r = trajectory[str(t)]
        s_p, a_p, _ = trajectory[str(t+1)]
        visited.append(s)

        if s_p in visited:
            r = -0.25
        reward += r

        ## SARSA: on-policy TD control
        if not Q_Learning:

            ## update Q 
            Q_sa[s[0], s[1]//90, actions[a]] += LEARNING_RATE * (r + DISCOUNT_RATE*Q_sa[s_p[0], s_p[1]//90, actions[a_p]] - Q_sa[s[0], s[1]//90, actions[a]])

        ## Q Learning: off-policy TD control
        else:
            ## update Q 
            Q_sa[s[0], s[1]//90, actions[a]] += LEARNING_RATE * (r + DISCOUNT_RATE*np.max(Q_sa[s_p[0], s_p[1]//90, :]) - Q_sa[s[0], s[1]//90, actions[a]])  

    ALL_ZERO = True
    for tile in range(env.ntiles):
        for head in range(4):
            for action in range(len(env.actions)):
                if Q_sa[tile][head][action]!=0:
                    print(Q_sa[tile][head][action])
                    ALL_ZERO = False
    print(ALL_ZERO)
    print(sum(sum(Q_sa)))

    return reward, trajectory, Q_sa, False

def Q_learning_trajectory(env, Q_sa, max_steps=5000, epsilon=0.5, trajectory=[]):
    if not len(trajectory) == 0:
        return SARSA_human_trajectory(env, Q_sa, max_steps=max_steps, Q_Learning = True, trajectory = trajectory, epsilon=epsilon)
    return SARSA_trajectory(env, Q_sa, max_steps=max_steps, Q_Learning = True, epsilon=epsilon)











