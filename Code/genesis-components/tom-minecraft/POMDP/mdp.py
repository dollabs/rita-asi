import numpy as np
import pdb
import time
import pprint
import random

import csv # for reading csv
import pandas as pd # for reading csv

import read

class MDP():
    """ basic MDP with single goal """

    def __init__(self,file_name):
        self.tilesummary = read.read_csv(file_name)
        self.actions = ('go_forth','turn_left','turn_right')
        self.states = tuple([(tile,head)
                        for tile in range(len(self.tilesummary)) 
                        for head in (0,90,180,270)]) 

    def T(self,s,a):
        """ next state from taking action a from current state s """

        tile,head = s
        
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

    def R(self,s_p):
        """ reward given current state """
        return int(self.tilesummary[s_p[0]]['tag']=='G')

    def reset(self):
        """ return new initial starting point """
        return (30,90)

    def done(self,s):
        """ return True if agent reached termination """
        if s[0] in (5,9,33): 
            return True
        return False

class MDP_multiple_goals():
    """ MDP with visiting up multiple goals """

    def __init__(self,file_name):
        self.tilesummary = read.read_csv(file_name)
        self.actions = ('go_forth','turn_left','turn_right')
        self.states = tuple([(tile,head,saved)
                        for tile in range(len(self.tilesummary)) 
                        for head in (0,90,180,270)
                        for saved in ((),(5,),(9,),(33,),(5,9),(9,33),(33,5),(5,9,33)) ]) 

    def T(self,s,a):
        """ next state from taking action a from current state s """

        tile,head,saved = s
        
        if a == 'go_forth':
            new_tile = self.tilesummary[tile][head]
            tile = tile if new_tile=='wall' else new_tile
            if tile in (5,9,33):
                saved += (tile,)

        elif a == 'turn_left':
            head = np.mod(head+90,360)
        elif a == 'turn_right':
            head = np.mod(head-90,360)
        else: # invalid action
            return None

        return (tile,head,saved)

    def R(self,s):
        """ reward given current state """
        # return self.tilesummary[s[0]]['reward'] + len(s[-1]) # only retrieve the top two rewards
        return int(self.tilesummary[s[0]]['tag']=='G') + len(s[-1])**2 # successfully retrieved all 3 rewards

    def reset(self):
        """ return new initial starting point """
        return (30,90,())

    def done(self,s):
        """ return True if agent reached termination """
        if len(s[-1]) == 3: 
            return True
        return False


if __name__ == '__main__':
    pp = pprint.PrettyPrinter(compact=False,width=100)
    ans = read.read_csv('6by6_1.csv')
    pp.pprint(ans)
