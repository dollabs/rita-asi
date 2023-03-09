import numpy as np
import pdb
import time
import pprint
import random

import csv # for reading csv
import pandas as pd # for reading csv

import read

# NOTE
# agent should NOT know the number of goals
# agent should NOT know the position(s) of goal(S)

# XXX
# pre-assign possible positions of goals

def cacher(func):
    memo = {}
    def memoize(*args):
        if args not in memo:
            memo[args]=func(*args)
        return memo[args]
    return memoize

def round_array(items,sigdigits=3):
    return tuple(round(item,sigdigits) for item in items)

@cacher
def subsets_recur(current, sset):
    if sset:
        return subsets_recur(current, sset[1:]) + subsets_recur(current + (sset[0],), sset[1:])
    return (current,)

class POMDP():
    """ 
    state = (tile,head,(seen),(_goal1,_goal2,...,_goaln)) 
    belief state = (tile,head,((),(0,),(5,),(33,),(0,5),(5,33),(33,0),(0,5,33)))
    """

    def __init__(self,file_name):
        self.tilesummary = read.read_csv(file_name)
        self.actions = ('go_forth','turn_left','turn_right')
        
        # XXX : goal information
        # ----------------------------------------------------------------------
        self.potential_goal_tiles = (0,5,33)
        self.potential_goals = subsets_recur((),self.potential_goal_tiles)
        # ----------------------------------------------------------------------
        
        # XXX : initial states
        # ----------------------------------------------------------------------
        self.s0 = (30,90)
        self.b0 = (30,90,(), round_array(1/8 for _ in self.potential_goals) )
        self.B = self.belief_states()
        # ----------------------------------------------------------------------

    @cacher
    def T(self,s,a):
        """ next state from taking action a from current state s """

        tile,head = s[:2]
        
        if a == 'go_forth':
            new_tile = self.tilesummary[tile][head]
            tile = tile if new_tile=='wall' else new_tile
        elif a == 'turn_left':
            head = np.mod(head+90,360)
        elif a == 'turn_right':
            head = np.mod(head-90,360)
        else: # invalid action
            return None

        return (tile,head)

    @cacher
    def R(self,s_p):
        """ reward given current state """
        tile,head,goals = s_p
        if tile in goals:
            return 1 
        return 0

    @cacher
    def Z(self,s_p):
        """ return subtuple of ((0,None/True/False), (5,None/True/False), (33,None/True/False)) """
        tile,head,goals = s_p

        # potential goals that are visible
        goals_visible = set(self.tilesummary[tile]['ahead'][head]).intersection(self.potential_goal_tiles)

        obsv = ()
        for goal_tile in goals_visible: 
            obsv += ((goal_tile,True),) if goal_tile in goals else ((goal_tile,False),) 

        return obsv

    def belief_states(self):

        def neighbour_beliefs(b):

            neighbour_belief_states = {b}

            # branch out from current belief state
            for goals in self.potential_goals:
                for a in self.actions:
                    s = b[:2] + (goals,)
                    s_p = self.T(s,a) + (goals,)
                    o = self.Z(s_p)
                    try:
                        b_p = self.update_belief(b,a,o)[0]
                        neighbour_belief_states.add(b_p)
                    except AssertionError:
                        pass
            
            return neighbour_belief_states
        
        b = self.b0
        belief_states = {b}
        child_beliefs = {b}

        while True: # BFS
            temp = set()

            for b in child_beliefs:

                temp.update(neighbour_beliefs(b).difference(belief_states))
                belief_states.update(temp)

            if temp == set():
                print('Belief State Construction Done!:{}'.format(len(belief_states)))
                return sorted(tuple(belief_states))
            
            child_beliefs = temp

    def update_belief(self,b,a,o):
        """ returns updated belief b_p """

        numer,denom = [],0
        seen = []
        for i,goals in enumerate(self.potential_goals):
            s = b[:2] + (goals,) # potential current state
            s_p = self.T(s,a) + (goals,)
            num = (self.Z(s_p)==o) * b[-1][i]
            numer.append(num)
            denom += numer[i]
            if num != 0:
                seen.append(set(goals))

        saved = b[2]
        if seen:
            if s_p[0] in set.intersection(*seen):
                saved = set(saved + (s_p[0],))

        assert denom != 0
        return s_p[:2] + (tuple(sorted(saved)),) + (round_array(num/denom for num in numer),), denom

    @cacher
    def Tb(self,b,a,b_p):
        
        # physical transition must make sense
        if b_p[:2] != self.T(b,a):
            return 0

        # saved from previous must be a subset of saved from next
        if not set(b[2]).issubset(b_p[2]):
            return 0
        
        # zero probability cannot become nonzero
        for i,p in enumerate(b[-1]):
            if p==0 and b_p[-1][i]!=0:
                return 0

        # find all possible observations that lead to b_p
        observations = set()
        for goals in self.potential_goals:
            o = self.Z(b_p[:2]+(goals,))
            observations.add(o)

        # find transition probability of (b,a) -> b_p
        # iterate over all observations to get Tb
        trans_prob = 0
        for o in observations:
            try:
                update = self.update_belief(b,a,o)
                trans_prob += (update[0]==b_p)*update[1]
            except AssertionError:
                pass
        
        return trans_prob

    @cacher
    def record_Tb_index(self,b,a):

        indices = []

        for i,b_p in enumerate(self.B):
            if self.Tb(b,a,b_p) != 0:
                indices.append(i)
        
        return indices

    @cacher
    def Rb(self,b_p):
        
        belief_reward = 0

        # for i,goals in enumerate(self.potential_goals):
        #     s_p = b_p[:2] + (goals,)
        #     belief_reward += b_p[-1][i]*self.R(s_p)

        # offset_reward = b_p[2].count(0) # explores but does not go close to the goals
        # offset_reward = len(set.intersection(*nonzero_goals)) # explores but does not go close to the goals
        
        offset_reward = len(b_p[2])**2
        return belief_reward + offset_reward

    def reset(self):
        """ return initial starting state and belief state """
        return self.s0, self.b0

    @cacher
    def done(self,s,b):
        """ return true if the agent saved all lives """

        for i,goals in enumerate(self.potential_goals):
            if set(goals) == set(b[2]):
                if abs(b[-1][i]-1) < 1e-3:
                    return True
        return False

if __name__ == '__main__':
    pp = pprint.PrettyPrinter(compact=False,width=100)

    MAP = '6by6.csv'
    env = POMDP(MAP)
    pp.pprint(len(env.B))



