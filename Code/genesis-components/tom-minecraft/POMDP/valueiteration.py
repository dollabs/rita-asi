import numpy as np
import pdb
import time
import pprint
import random

def value_iteration(env,epsilon=1e-3,gamma=0.9):
    """ performs value iteration """

    # helper function
    def best_action(s):
        V_max = -float("inf")
        for a in env.actions:
            V_temp = sum((s_p == env.T(s,a))*(env.R(s_p) + gamma*V[s_p]) for s_p in env.states)
            V_max = max(V_max,V_temp)
        return V_max

    # initialize V arbitrarily
    V = {s:0 for s in env.states}

    count = 1

    # training loop
    while True:
        delta = 0
        for s in env.states:
            v = V[s]
            V[s] = best_action(s) # helper function used
            delta = max(delta, abs(v-V[s]))

        # print progress
        print('{}: {}'.format(count,delta))
        count += 1
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

def episode(env,s0,max_timesteps=50):
    """ extract an episode rollout that starts with s0 and follows the greedy policy """

    pi = policy(env)
    ep = [] # collect episode
    s = s0

    for _ in range(max_timesteps):
        a = random.choice(pi[s])[0]
        ep.append((s,a))
        s = env.T(s,a)
        if env.done(s): break

    ep.append((s,a))
        
    return ep

def value_iteration_b(env,epsilon=1e-3,gamma=0.9):
    """ performs value iteration """
    start = time.time()

    # helper function
    def best_action(b):
        V_max = -float("inf")
        for a in env.actions:
            V_temp = 0

            for i in env.record_Tb_index(b,a):
                b_p = env.B[i]
                V_temp += env.Tb(b,a,b_p) * (env.Rb(b_p) + gamma*V[b_p]) 

            V_max = max(V_max,V_temp)
        return V_max

    # initialize V arbitrarily
    V = {b:0 for b in env.B}

    count = 1

    # training loop
    while True:
        delta = 0
        for b in env.B:
            v = V[b]
            V[b] = best_action(b) # helper function used
            delta = max(delta, abs(v-V[b]))

        # print progress
        print('{}: {}'.format(count,delta))
        count += 1
        # termination condition
        if delta < epsilon: break

    print(time.time()-start)
    return V

def policy_b(env,gamma=0.9):
    """ uses greedy policy and state action value to extract policy for gridworld """

    V = value_iteration_b(env)
    policy = {}

    for b in env.B:
        Q = {}
        for a in env.actions:
            Q[a] = sum(env.Tb(b,a,env.B[i])*(env.Rb(env.B[i]) + gamma*V[env.B[i]]) for i in env.record_Tb_index(b,a))

        # highest state-action value
        Q_max = max(Q.values())
        # collect all actions that has Q value very close to Q_max
        policy[b] = tuple(filter(lambda x: x[0] if abs(x[1]-Q_max)<1e-2 else None, Q.items()))

    return policy

def episode_b(env,s0,max_timesteps=80):
    """ extract an episode rollout that starts with s0 and follows the greedy policy """

    pi = policy_b(env)

    # from policy import pre_computed
    # pi = pre_computed

    s = s0
    b = env.b0
    _,_,goals = s
    ep = [] # collect episode

    for _ in range(max_timesteps):
        a = random.choice(pi[b])[0]
        ep.append((b,a))
        s = env.T(s,a)+(goals,) # s_p
        o = env.Z(s)
        b,_ = env.update_belief(b,a,o) # b_p

        if env.done(s,b): break
        
    ep.append((b,a))
    return ep

if __name__ == '__main__':

    import mdp
    import pomdp
    MAP = '6by6.csv'

    pp = pprint.PrettyPrinter(compact=False,width=100)
    env = pomdp.POMDP(MAP)
    value_iteration_b(env)




