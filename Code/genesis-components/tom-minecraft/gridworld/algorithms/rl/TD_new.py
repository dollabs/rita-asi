import random
import numpy as np
from scipy.special import softmax

# TEMPERATURE = 4
# LEARNING_RATE = 0.7
# DISCOUNT_RATE = 0.96
TEMPERATURE = 1
LEARNING_RATE = 1.0
DISCOUNT_RATE = 0.99
MAX_STEPS = 100
EPSILON = 0.1
EPSILON_DECAY = 1

actions = {'up': 0, 'down': 1, 'left': 2, 'right': 3}

def initialize_PI(env):
    return np.zeros((len(env.actions), ))

def initialize_Q(env):
    return np.zeros((len(env.states), len(env.actions)))

def get_softmax_policy(Q_sa, temperature=TEMPERATURE):
    return softmax(Q_sa/TEMPERATURE, axis = 1)

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

    # epsilon = 0
    # avail_actions = available_actions(env, s)
    flag = 'valid'

    # Choose an action according to the distribution
    if np.random.rand() <= epsilon:
        a = env.actions[np.random.choice(4)]
    else:
        prob = pi[s, :]
        # index = np.random.choice(4, p=prob)
        # index = np.argmax(prob)
        mask = (prob == np.amax(prob)).astype(float)
        mask /= np.sum(mask)
        # a = env.actions[index]
        a = env.actions[np.random.choice(4, p=mask)]

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
    if Q_sa is None:
        Q_sa = initialize_Q(env)

    s = env._pos_agent
    s_p = None
    pi = get_softmax_policy(Q_sa)
    a, flag = choose_action(env, pi, s)

    for t in range(max_steps):

        trajectory.append((s,a))
        visited.append(s)

        ## take action a and observe s_p, r
        if flag == 'invalid':
            s_p = s
            # r = -0.75
            # r = -0.2
            r = 0
            break
        else:
            s_p = env.T(s,a)
            # if s_p in visited:
            #     r = -0.08
            # else:
            r = env.R(s,a)

        reward += r

        if len(env.remaining_to_save.values()) == 0:
            break

        # if reward < -0.5 * len(env.tile_indices):
        #     break

        ## SARSA: on-policy TD control
        if not Q_Learning:

            ## choose a_p using policy from Q
            pi = get_softmax_policy(Q_sa)
            a_p, flag = choose_action(env, pi, s_p, epsilon=epsilon)

            while flag == 'invalid':
                a_p, flag = choose_action(env, pi, s_p, epsilon=epsilon)

            ## update Q 
            Q_sa[s,actions[a]] += LEARNING_RATE * (r + DISCOUNT_RATE*Q_sa[s_p,actions[a_p]] - Q_sa[s,actions[a]])
            a = a_p

        ## Q Learning: off-policy TD control
        else:
            ## update Q 
            Q_sa[s,actions[a]] += LEARNING_RATE * (r + DISCOUNT_RATE*np.max(Q_sa[s_p, :]) - Q_sa[s,actions[a]])
            pi = get_softmax_policy(Q_sa)
            a, flag = choose_action(env, pi, s_p, epsilon=epsilon)

        s = s_p
      
    print('no human',np.sum(Q_sa))   

    return reward, trajectory, Q_sa

def SARSA_human_trajectory(env, Q_sa, max_steps=5000, Q_Learning = False, trajectory = [], epsilon=0.5):

    reward = 0
    visited = []
    if Q_sa is None:
        Q_sa = initialize_Q(env)

    for t in range(len(trajectory)):

        s, a, r = trajectory[str(t)]
        # print(s,a,r)
        visited.append(s)
        reward += r

        ## SARSA: on-policy TD control
        if not Q_Learning:

            ## choose a_p using policy from Q
            pi = get_softmax_policy(Q_sa)
            a_p, flag = choose_action(env, pi, s_p, epsilon=epsilon)

            while flag == 'invalid':
                a_p, flag = choose_action(env, pi, s_p, epsilon=epsilon)

            ## update Q 
            Q_sa[s,actions[a]] += LEARNING_RATE * (r + DISCOUNT_RATE*Q_sa[s_p,actions[a_p]] - Q_sa[s,actions[a]])
            a = a_p

        ## Q Learning: off-policy TD control
        else:
            ## update Q 
            s_p = env.T(s, a)
            if s_p != 'wall':
                Q_sa[s,actions[a]] += LEARNING_RATE * (r + DISCOUNT_RATE*np.max(Q_sa[s_p, :]) - Q_sa[s,actions[a]])  

    print('human',np.sum(Q_sa))   

    return reward, trajectory, Q_sa

def Q_learning_trajectory(env, Q_sa, max_steps=5000, epsilon=0.5, trajectory = []):
    if not len(trajectory) == 0:
        return SARSA_human_trajectory(env, Q_sa, max_steps=max_steps, Q_Learning = True, trajectory = trajectory, epsilon=epsilon)
    return SARSA_trajectory(env, Q_sa, max_steps=max_steps, Q_Learning = True, epsilon=epsilon)











