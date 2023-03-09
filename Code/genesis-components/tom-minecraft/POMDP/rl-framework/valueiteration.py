import numpy as np
import pdb
import time
import pprint
import random

import mdp

def cacher(func):
    memo = {}
    def check_memo(*args):
        if args not in memo:
            memo[args] = func(*args)
        return memo[args]
    return check_memo

def value_iteration(env,epsilon=1e-3):

    # helper function
    def best_action(s):
        V_max = -np.inf
        for a in env.A:
            V_temp = sum(env.P(s_p,s,a)*(env.R(s_p,s,a) + gamma*V[s_p]) for s_p in S)
            V_max = max(V_max,V_temp)
        return V_max

    # initialize V arbitrarily
    V = {s:0 for s in env.S}

    # training loop
    while True:
        delta = 0
        for s in S:
            v = V[s]
            V[s] = best_action(s)
            delta = max(delta, abs(v-V[s]))

        # termination condition
        if delta < epsilon: break

    return V

def policy(env):

    V = value_iteration(env)
    policy = {}

    for s in env.S:
        Q = {}
        for a in env.A:
            Q[a] = sum(env.P(s_p,s,a)*(env.R(s_p,s,a) + gamma*V[s_p]) for s_p in S)

        # highest state-action value
        Q_max = max(Q.values())
        # collect all actions that has Q value very close to Q_max
        policy[s] = filter(lambda x: x[0] if abs(x[1]-Q_max)<1e-2 else None, Q.items())

    return policy

def episode(env):

    pi = policy(env)
    ep = []
    s = env.s0

    while s != env.termination:
        a = random.choice(policy[s])
        ep.append((s,a))
        s = env.T(s,a)
        
    return ep

if __name__ == '__main__':


