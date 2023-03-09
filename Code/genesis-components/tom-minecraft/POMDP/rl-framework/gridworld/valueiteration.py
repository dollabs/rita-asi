import numpy as np
import pdb
import time
import pprint
import random

import mdp

def value_iteration(env,epsilon=1e-3,gamma=0.9):
    """ performs value iteration """

    # helper function
    def best_action(s):
        V_max = -np.inf
        for a in env.actions:
            V_temp = sum((s_p == env.T(s,a))*(env.R(s_p) + gamma*V[s_p]) for s_p in env.states)
            V_max = max(V_max,V_temp)
        return V_max

    # initialize V arbitrarily
    V = {s:0 for s in env.states}

    # training loop
    while True:
        delta = 0
        for s in env.states:
            v = V[s]
            V[s] = best_action(s) # helper function used
            delta = max(delta, abs(v-V[s]))

        # termination condition
        if delta < epsilon: break

    return V

def policy(env,gamma=0.9):
    """ uses greedy policy and state action value to extract policy for gridworld """

    V = value_iteration(env)
    policy = {}

    for s in env.states:
        Q = {}
        for a in env.actions:
            Q[a] = sum((s_p==env.T(s,a))*(env.R(s_p) + gamma*V[s_p]) for s_p in env.states)

        # highest state-action value
        Q_max = max(Q.values())
        # collect all actions that has Q value very close to Q_max
        policy[s] = tuple(filter(lambda x: x[0] if abs(x[1]-Q_max)<1e-2 else None, Q.items()))

    return policy

def episode(env,s0):
    """ extract an episode rollout that starts with s0 and follows the greedy policy """

    pi = policy(env)
    ep = [] # collect episode
    s = s0

    while True:
        a = random.choice(pi[s])[0]
        ep.append((s,a))
        s = env.T(s,a)
        if s[0] == 11: break

    ep.append((s,a))
        
    return ep

if __name__ == '__main__':

    pp = pprint.PrettyPrinter(compact=False,width=100)
    env = mdp.MDP()



