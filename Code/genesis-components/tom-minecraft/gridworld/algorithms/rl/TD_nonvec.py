import random
import numpy as np


TEMPERATURE = 5
LEARNING_RATE = 0.1
DISCOUNT_RATE = 0.99
MAX_STEPS = 200

def initialize_PI(env):
    pi = {}
    for s in env.states:
        pi[s] = 'up'
    return pi

def initialize_Q(env):
    Q_sa = {}
    for s in env.states:
        for a in env.actions:
            Q_sa[(s,a)] = 0
    return Q_sa

def get_softmax_policy(env, Q_sa, temperature=TEMPERATURE):
    pi = {}
    deno = {}
    for s in env.states:
        deno[s] = 0
        for a in env.actions:
            deno[s] += np.exp(Q_sa[(s,a)]/temperature)
    for s in env.states:    
        pi[s] = []
        for a in env.actions:
            pi[s].append(np.exp(Q_sa[(s,a)]/temperature) / deno[s])

    # print(pi)
    return pi

# def choose_action(env, pi, s, epsilon=0.5):
#     # Choose an action according to the distribution
#     if np.random.rand() <= epsilon:
#         a = env.actions[np.random.choice(3)]
#         while env.T(s,a)[0] == 'wall':
#             a = env.actions[np.random.choice(3)]
#     else:
#         a = env.actions[np.random.choice(np.arange(0,3), p=pi[s])]
#         while env.T(s,a)[0] == 'wall':
#             a = env.actions[np.random.choice(np.arange(0,3), p=pi[s])]

#     return a

# def available_actions(env, s):
#     actions = [] # 'turn_left','turn_right'

#     ## delete actions that go into walls
#     if env.tilesummary[s[0]][s[1]] != 'wall':
#         actions.append('go_straight')
    
#     ## delete actions that turn towards walls
#     next_state = env.T(s, 'turn_left')
#     if env.tilesummary[next_state[0]][next_state[1]] != 'wall':
#         actions.append('turn_left')
#     next_state = env.T(s, 'turn_right')
#     if env.tilesummary[next_state[0]][next_state[1]] != 'wall':
#         actions.append('turn_right')

#     ## ensure there's at least one kind of turning to prevent the corner case
#     if 'turn_left' not in actions and 'turn_right' not in actions:
#         actions.append(random.choice(['turn_left','turn_right']))

#     return actions

# def choose_action(env, pi, s, epsilon=0.5):

#     # Choose an action according to the distribution
#     if np.random.rand() <= epsilon:
#         avail_actions = available_actions(env, s)
#         a = avail_actions[np.random.choice(len(avail_actions))]
 
#     else:
#         avail_actions = available_actions(env, s)
#         # index = np.argsort(pi[s])[::-1]
#         # a = env.actions[index[0]]

#         # count = 0
#         # while not a in avail_actions:
#         #     count += 1
#         #     a = env.actions[index[count]]
#         prob = pi[s]
#         index = np.random.choice(3, p=prob)
#         a = env.actions[index]
#         while not a in avail_actions:
#             prob[index] = 0
#             prob /= np.sum(prob)
#             index = np.random.choice(3, p=prob)
#             a = env.actions[index]
            
#     return a

# def choose_action(env, pi, s, epsilon=0.5):

#     epsilon = 0.1
#     avail_actions = available_actions(env, s)
#     flag = 'valid'

#     # Choose an action according to the distribution
#     if np.random.rand() <= epsilon:
#         a = env.actions[np.random.choice(3)]
#     else:
#         prob = pi[s]
#         index = np.random.choice(3, p=prob)
#         a = env.actions[index]

#     if a in avail_actions:
#         flag = 'valid'
#     else:
#         flag = 'invalid'
            
#     return a, flag

def choose_action(env, pi, s, epsilon=0.5):

    epsilon = 0.2
    # avail_actions = available_actions(env, s)
    flag = 'valid'

    # Choose an action according to the distribution
    if np.random.rand() <= epsilon:
        a = env.actions[np.random.choice(4)]
    else:
        prob = pi[s]
        index = np.random.choice(4, p=prob)
        a = env.actions[index]

    if env.T(s,a) == "wall":
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

## ------------------------------
##  Now we will update pi based on estimated V
## ------------------------------
# def SARSA_trajectory(env, Q_sa, max_steps=5000, Q_Learning = False, epsilon=0.5):

#     reward = 0
#     trajectory = []
#     if Q_sa == None:
#         Q_sa = initialize_Q(env)

#     s = env._pos_agent
#     pi = get_softmax_policy(env, Q_sa)
#     a = choose_action(env, pi, s)

#     for t in range(max_steps):

#         trajectory.append((s,a))

#         ## take action a and observe s_p, r
#         s_p = env.T(s,a)
#         r = env.R(s,a)
#         reward += r

#         ## SARSA: on-policy TD control
#         if not Q_Learning:

#             ## choose a_p using policy from Q
#             pi = get_softmax_policy(env, Q_sa)
#             a_p = choose_action(env, pi, s_p)

#             ## update Q 
#             Q_sa[(s,a)] += LEARNING_RATE * (r + DISCOUNT_RATE*Q_sa[(s_p,a_p)] - Q_sa[(s,a)])
#             a = a_p

#         ## Q Learning: off-policy TD control
#         else:
#             ## update Q 
#             Q_sa[(s,a)] += LEARNING_RATE * (r + DISCOUNT_RATE*max(Q_sa[(s_p, 'go_straight')], Q_sa[(s_p, 'turn_left')], Q_sa[(s_p, 'turn_right')]) - Q_sa[(s,a)])
#             pi = get_softmax_policy(env, Q_sa)
#             a = choose_action(env, pi, s_p, epsilon=epsilon)

#         s = s_p
        

#     return reward, trajectory, Q_sa

def SARSA_trajectory(env, Q_sa, max_steps=5000, Q_Learning = False, epsilon=0.5):

    reward = 0
    trajectory = []
    visited = []
    if Q_sa == None:
        Q_sa = initialize_Q(env)

    s = env._pos_agent
    s_p = s
    pi = get_softmax_policy(env, Q_sa)
    a, flag = choose_action(env, pi, s)

    for t in range(max_steps):

        trajectory.append((s,a))
        visited.append(s)

        ## take action a and observe s_p, r
        if flag == 'invalid':
            s_p = s
            r = -0.75
        elif s_p in visited:
            s_p = env.T(s,a)
            r = -0.25
        else:
            s_p = env.T(s,a)
            r = env.R(s,a)
        reward += r

        if reward < -0.5 * len(env.tile_indices):
            break

        ## SARSA: on-policy TD control
        if not Q_Learning:

            ## choose a_p using policy from Q
            pi = get_softmax_policy(env, Q_sa)
            a_p = choose_action(env, pi, s_p)

            ## update Q 
            Q_sa[(s,a)] += LEARNING_RATE * (r + DISCOUNT_RATE*Q_sa[(s_p,a_p)] - Q_sa[(s,a)])
            a = a_p

        ## Q Learning: off-policy TD control
        else:
            ## update Q 
            Q_sa[(s,a)] += LEARNING_RATE * (r + DISCOUNT_RATE*np.max([Q_sa[(s_p, act)] for act in env.actions]) - Q_sa[(s,a)])
            pi = get_softmax_policy(env, Q_sa)
            a, flag = choose_action(env, pi, s_p, epsilon=epsilon)

        s = s_p
      

    return reward, trajectory, Q_sa

def Q_learning_trajectory(env, Q_sa, max_steps=5000, epsilon=0.5):
    return SARSA_trajectory(env, Q_sa, max_steps=max_steps, Q_Learning = True, epsilon=epsilon)











